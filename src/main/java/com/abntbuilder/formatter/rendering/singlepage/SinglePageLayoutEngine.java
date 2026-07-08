package com.abntbuilder.formatter.rendering.singlepage;

import com.abntbuilder.formatter.engine.model.profile.StyleRule;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.SinglePageAnchorStrategy;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.SinglePageLineHeightStrategy;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.SinglePageSafetyPolicyId;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.SpacerStylePolicy;
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

        int availableGapHeightTwips = renderableArea.safeHeightTwips() - contentHeightTwips;
        int[] gapHeightTwips = input.gaps().isEmpty()
                ? new int[0]
                : gapDistributor.distribute(
                        availableGapHeightTwips,
                        input.gaps().stream().map(ResolvedLayoutGap::weight).toList()
                );
        int[] gapLineCounts = createGapLineCounts(gapHeightTwips, lineHeightTwips);
        int anchorSpacerLineCount = input.gaps().isEmpty() && availableGapHeightTwips > 0
                ? Math.max(1, availableGapHeightTwips / lineHeightTwips)
                : 0;
        int availableGapLines = input.gaps().isEmpty()
                ? anchorSpacerLineCount
                : sum(gapLineCounts);
        Map<String, Integer> gapLineCountMap = createGapLineCounts(input.gaps(), gapLineCounts);
        Map<String, Integer> gapHeightTwipsMap = createGapLineCounts(input.gaps(), gapHeightTwips);
        List<SinglePageLayoutElement> elements = assembleElements(
                input, gapLineCounts, gapHeightTwips, availableGapHeightTwips, anchorSpacerLineCount, lineHeightTwips);
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
                contentLineCount + availableGapLines,
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
            int[] gapHeightTwips,
            int anchorSpacerHeightTwips,
            int anchorSpacerLineCount,
            int lineHeightTwips
    ) {
        List<SinglePageLayoutElement> elements = new ArrayList<>();

        if (input.gaps().isEmpty() && anchorSpacerHeightTwips > 0 && anchorSpacerLineCount > 0) {
            SinglePageLayoutGroup firstGroup = input.groups().getFirst();
            StyleRule anchorStyle = firstGroup.firstItem().styleRule();
            elements.add(new SinglePageSpacerLines(
                    firstGroup.id() + ".anchorSpacer",
                    firstGroup.id(),
                    firstGroup.id(),
                    anchorSpacerLineCount,
                    anchorStyle,
                    lineHeightTwips,
                    anchorSpacerHeightTwips
            ));
        }

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
                            lineHeightTwips,
                            gapHeightTwips[groupIndex - 1]
                    ));
                }
            }

            for (SinglePageLayoutItem item : group.items()) {
                int itemLineHeightTwips = lineMetricsFromStyle(item.styleRule());
                elements.add(new SinglePageTextLines(
                        group.id(),
                        item.id(),
                        item.styleRule(),
                        item.paragraphText(),
                        item.visualLines(),
                        item.measurementArea(),
                        item.layoutOverride(),
                        itemLineHeightTwips
                ));

                if (item.blankLinesAfter() > 0) {
                    elements.add(new SinglePageSpacerLines(
                            group.id() + "." + item.id() + ".blankLinesAfter",
                            group.id(),
                            group.id(),
                            item.blankLinesAfter(),
                            item.styleRule(),
                            itemLineHeightTwips
                    ));
                }
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

    private static int[] createGapLineCounts(int[] gapHeightTwips, int lineHeightTwips) {
        int[] gapLineCounts = new int[gapHeightTwips.length];

        for (int index = 0; index < gapHeightTwips.length; index++) {
            if (gapHeightTwips[index] > 0) {
                gapLineCounts[index] = Math.max(1, gapHeightTwips[index] / lineHeightTwips);
            }
        }

        return gapLineCounts;
    }

    private static int sum(Map<String, Integer> values) {
        return values.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    private static int sum(int[] values) {
        int total = 0;

        for (int value : values) {
            total += value;
        }

        return total;
    }

    private static int roundUp(int value, int divisor) {
        return (value + divisor - 1) / divisor;
    }

    private static int lineMetricsFromStyle(StyleRule styleRule) {
        return MeasurementConverter.pointsToTwips(styleRule.fontSizePt().multiply(styleRule.lineSpacing()));
    }
}
