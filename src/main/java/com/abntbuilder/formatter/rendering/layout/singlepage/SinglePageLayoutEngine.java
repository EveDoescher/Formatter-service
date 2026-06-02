package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageAnchorStrategy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLineHeightStrategy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageSafetyPolicyId;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SpacerStylePolicy;
import com.abntbuilder.formatter.shared.exception.InvalidSinglePageStyleException;
import com.abntbuilder.formatter.shared.exception.SinglePageLayoutOverflowException;
import com.abntbuilder.formatter.shared.exception.UnsupportedLayoutPolicyException;
import com.abntbuilder.formatter.shared.measurement.MeasurementConverter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class SinglePageLayoutEngine {

    private final SinglePageLayoutLineMetrics lineMetrics;
    private final SinglePageSafetyPolicy safetyPolicy;
    private final SinglePageGapDistributor gapDistributor;

    public SinglePageLayoutEngine(
            SinglePageLayoutLineMetrics lineMetrics,
            SinglePageSafetyPolicy safetyPolicy,
            SinglePageGapDistributor gapDistributor
    ) {
        this.lineMetrics = Objects.requireNonNull(lineMetrics, "lineMetrics must not be null");
        this.safetyPolicy = Objects.requireNonNull(safetyPolicy, "safetyPolicy must not be null");
        this.gapDistributor = Objects.requireNonNull(gapDistributor, "gapDistributor must not be null");
    }

    public SinglePageLayoutPlan calculate(SinglePageLayoutInput input) {
        Objects.requireNonNull(input, "input must not be null");
        validatePolicy(input);
        validateGaps(input.groups(), input.gaps());
        validateSinglePageSpacing(input.groups());

        int lineHeightTwips = lineMetrics.layoutLineHeightTwips(input.groups());
        SinglePageRenderableArea renderableArea = safetyPolicy.calculate(input.pageRule(), lineHeightTwips);
        BigDecimal exactLineHeightPt = MeasurementConverter.twipsToPoints(lineHeightTwips);
        int contentLineCount = lineMetrics.contentLineCount(input.groups());
        int contentHeightTwips = lineMetrics.contentHeightTwips(input.groups());
        Map<String, Integer> groupLineCounts = createGroupLineCounts(input.groups());
        Map<String, Integer> itemLineCounts = createItemLineCounts(input.groups());
        Map<String, Integer> groupHeightTwips = createGroupHeightTwips(input.groups(), lineMetrics);
        Map<String, Integer> itemHeightTwips = createItemHeightTwips(input.groups(), lineMetrics);

        if (contentHeightTwips > renderableArea.safeHeightTwips()) {
            int overflowHeightTwips = contentHeightTwips - renderableArea.safeHeightTwips();
            throw new SinglePageLayoutOverflowException(new SinglePageLayoutFailureDiagnostic(
                    renderableArea,
                    contentLineCount,
                    Math.max(1, roundUp(overflowHeightTwips, lineHeightTwips)),
                    groupLineCounts,
                    itemLineCounts,
                    contentHeightTwips,
                    overflowHeightTwips,
                    groupHeightTwips,
                    itemHeightTwips,
                    exactLineHeightPt
            ));
        }

        int availableGapLines = input.gaps().isEmpty()
                ? 0
                : Math.max(renderableArea.safeLineCapacity() - contentLineCount, 0);
        int availableGapHeightTwips = input.gaps().isEmpty()
                ? 0
                : renderableArea.safeHeightTwips() - contentHeightTwips;
        int[] gapLineCounts = input.gaps().isEmpty()
                ? new int[0]
                : gapDistributor.distribute(
                        availableGapLines,
                        input.gaps().stream().map(ResolvedLayoutGap::weight).toList()
                );
        int[] gapHeightTwips = input.gaps().isEmpty()
                ? new int[0]
                : gapDistributor.distribute(
                        availableGapHeightTwips,
                        input.gaps().stream().map(ResolvedLayoutGap::weight).toList()
                );
        Map<String, Integer> gapLineCountMap = createGapLineCounts(input.gaps(), gapLineCounts);
        Map<String, Integer> gapHeightTwipsMap = createGapLineCounts(input.gaps(), gapHeightTwips);
        List<SinglePageLayoutElement> elements = assembleElements(input, gapLineCounts, lineHeightTwips);
        SinglePageLayoutDiagnostic diagnostic = new SinglePageLayoutDiagnostic(
                renderableArea,
                contentLineCount,
                availableGapLines,
                groupLineCounts,
                itemLineCounts,
                gapLineCountMap,
                contentHeightTwips,
                availableGapHeightTwips,
                sum(gapHeightTwipsMap),
                groupHeightTwips,
                itemHeightTwips,
                gapHeightTwipsMap,
                exactLineHeightPt
        );

        return new SinglePageLayoutPlan(
                elements,
                renderableArea.safeLineCapacity(),
                renderableArea.safeLineCapacity(),
                exactLineHeightPt,
                diagnostic
        );
    }

    private static void validatePolicy(SinglePageLayoutInput input) {
        if (input.policy().anchorStrategy() != SinglePageAnchorStrategy.LAST_GROUP_AT_SAFE_AREA_END) {
            throw new UnsupportedLayoutPolicyException("Unsupported single-page anchor strategy: "
                    + input.policy().anchorStrategy());
        }

        if (input.policy().lineHeightStrategy() != SinglePageLineHeightStrategy.MAX_EXACT_LINE_HEIGHT) {
            throw new UnsupportedLayoutPolicyException("Unsupported single-page line height strategy: "
                    + input.policy().lineHeightStrategy());
        }

        if (input.policy().safetyPolicy() != SinglePageSafetyPolicyId.MARGIN_BASED) {
            throw new UnsupportedLayoutPolicyException("Unsupported single-page safety policy: "
                    + input.policy().safetyPolicy());
        }
    }

    private static void validateGaps(
            List<SinglePageLayoutGroup> groups,
            List<ResolvedLayoutGap> gaps
    ) {
        int expectedGapCount = Math.max(groups.size() - 1, 0);

        if (gaps.size() != expectedGapCount) {
            throw new IllegalArgumentException("gaps size must be equal to groups size minus one.");
        }

        for (int index = 0; index < gaps.size(); index++) {
            ResolvedLayoutGap gap = gaps.get(index);

            if (!gap.fromPresentGroupId().equals(groups.get(index).id())) {
                throw new IllegalArgumentException("gap fromPresentGroupId must match previous group id.");
            }

            if (!gap.toPresentGroupId().equals(groups.get(index + 1).id())) {
                throw new IllegalArgumentException("gap toPresentGroupId must match next group id.");
            }
        }
    }

    private static void validateSinglePageSpacing(List<SinglePageLayoutGroup> groups) {
        for (SinglePageLayoutGroup group : groups) {
            for (SinglePageLayoutItem item : group.items()) {
                StyleRule styleRule = item.styleRule();

                if (styleRule.spacingBeforePt().compareTo(BigDecimal.ZERO) != 0) {
                    throw InvalidSinglePageStyleException.spacingBeforeMustBeZero();
                }

                if (styleRule.spacingAfterPt().compareTo(BigDecimal.ZERO) != 0) {
                    throw InvalidSinglePageStyleException.spacingAfterMustBeZero();
                }
            }
        }
    }

    private static List<SinglePageLayoutElement> assembleElements(
            SinglePageLayoutInput input,
            int[] gapLineCounts,
            int lineHeightTwips
    ) {
        List<SinglePageLayoutElement> elements = new ArrayList<>();

        for (int groupIndex = 0; groupIndex < input.groups().size(); groupIndex++) {
            SinglePageLayoutGroup group = input.groups().get(groupIndex);

            if (groupIndex > 0) {
                int gapLineCount = gapLineCounts[groupIndex - 1];

                if (gapLineCount > 0) {
                    ResolvedLayoutGap gap = input.gaps().get(groupIndex - 1);
                    elements.add(new SinglePageSpacerLines(
                            gap.id(),
                            gap.fromPresentGroupId(),
                            gap.toPresentGroupId(),
                            gapLineCount,
                            resolveSpacerStyle(input, groupIndex),
                            lineHeightTwips
                    ));
                }
            }

            for (SinglePageLayoutItem item : group.items()) {
                elements.add(new SinglePageTextLines(
                        group.id(),
                        item.id(),
                        item.styleRule(),
                        item.visualLines(),
                        item.measurementArea(),
                        item.layoutOverride(),
                        lineMetricsFromStyle(item.styleRule())
                ));
            }
        }

        return List.copyOf(elements);
    }

    private static StyleRule resolveSpacerStyle(SinglePageLayoutInput input, int currentGroupIndex) {
        return switch (input.policy().spacerStylePolicy()) {
            case PREVIOUS_GROUP_STYLE -> input.groups().get(currentGroupIndex - 1).firstItem().styleRule();
            case NEXT_GROUP_STYLE -> input.groups().get(currentGroupIndex).firstItem().styleRule();
        };
    }

    private static Map<String, Integer> createGroupLineCounts(List<SinglePageLayoutGroup> groups) {
        Map<String, Integer> lineCounts = new LinkedHashMap<>();

        for (SinglePageLayoutGroup group : groups) {
            lineCounts.put(group.id(), group.lineCount());
        }

        return lineCounts;
    }

    private static Map<String, Integer> createItemLineCounts(List<SinglePageLayoutGroup> groups) {
        Map<String, Integer> lineCounts = new LinkedHashMap<>();

        for (SinglePageLayoutGroup group : groups) {
            for (SinglePageLayoutItem item : group.items()) {
                lineCounts.put(group.id() + "." + item.id(), item.lineCount());
            }
        }

        return lineCounts;
    }

    private static Map<String, Integer> createGroupHeightTwips(
            List<SinglePageLayoutGroup> groups,
            SinglePageLayoutLineMetrics lineMetrics
    ) {
        Map<String, Integer> heightTwips = new LinkedHashMap<>();

        for (SinglePageLayoutGroup group : groups) {
            int groupHeightTwips = group.items()
                    .stream()
                    .mapToInt(lineMetrics::itemHeightTwips)
                    .sum();
            heightTwips.put(group.id(), groupHeightTwips);
        }

        return heightTwips;
    }

    private static Map<String, Integer> createItemHeightTwips(
            List<SinglePageLayoutGroup> groups,
            SinglePageLayoutLineMetrics lineMetrics
    ) {
        Map<String, Integer> heightTwips = new LinkedHashMap<>();

        for (SinglePageLayoutGroup group : groups) {
            for (SinglePageLayoutItem item : group.items()) {
                heightTwips.put(group.id() + "." + item.id(), lineMetrics.itemHeightTwips(item));
            }
        }

        return heightTwips;
    }

    private static Map<String, Integer> createGapLineCounts(
            List<ResolvedLayoutGap> gaps,
            int[] gapLineCounts
    ) {
        Map<String, Integer> lineCounts = new LinkedHashMap<>();

        for (int index = 0; index < gapLineCounts.length; index++) {
            lineCounts.put(gaps.get(index).id(), gapLineCounts[index]);
        }

        return lineCounts;
    }

    private static int sum(Map<String, Integer> values) {
        return values.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    private static int roundUp(int value, int divisor) {
        return (value + divisor - 1) / divisor;
    }

    private static int lineMetricsFromStyle(StyleRule styleRule) {
        return MeasurementConverter.pointsToTwips(styleRule.fontSizePt().multiply(styleRule.lineSpacing()));
    }
}
