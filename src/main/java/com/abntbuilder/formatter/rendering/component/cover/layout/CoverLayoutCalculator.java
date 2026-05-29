package com.abntbuilder.formatter.rendering.component.cover.layout;

import com.abntbuilder.formatter.document.component.cover.CoverComponent;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverComponentRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverLayoutRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageGapDistributor;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageRenderableArea;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageRenderableAreaCalculator;
import com.abntbuilder.formatter.rendering.layout.text.FontMetricsTextMeasurer;
import com.abntbuilder.formatter.rendering.layout.text.TextMeasurer;
import com.abntbuilder.formatter.shared.exception.InvalidCoverContentException;
import com.abntbuilder.formatter.shared.exception.InvalidSinglePageStyleException;
import com.abntbuilder.formatter.shared.measurement.MeasurementConverter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class CoverLayoutCalculator {

    private static final String COVER_COMPONENT_ID = "cover";
    private static final String TOP_BLOCK_ID = "cover.top";
    private static final String AUTHORS_BLOCK_ID = "cover.authors";
    private static final String TITLE_BLOCK_ID = "cover.title";
    private static final String SUBTITLE_BLOCK_ID = "cover.subtitle";
    private static final String BOTTOM_BLOCK_ID = "cover.bottom";
    private static final int REQUIRED_BOTTOM_LINE_COUNT = 2;

    private final TextMeasurer textMeasurer;
    private final SinglePageRenderableAreaCalculator renderableAreaCalculator;
    private final CoverGapDistributor gapDistributor;

    public CoverLayoutCalculator() {
        this(
                new FontMetricsTextMeasurer(),
                new SinglePageRenderableAreaCalculator(),
                new CoverGapDistributor(new SinglePageGapDistributor())
        );
    }

    public CoverLayoutCalculator(
            TextMeasurer textMeasurer,
            SinglePageRenderableAreaCalculator renderableAreaCalculator,
            CoverGapDistributor gapDistributor
    ) {
        this.textMeasurer = Objects.requireNonNull(textMeasurer, "textMeasurer must not be null");
        this.renderableAreaCalculator = Objects.requireNonNull(
                renderableAreaCalculator,
                "renderableAreaCalculator must not be null"
        );
        this.gapDistributor = Objects.requireNonNull(gapDistributor, "gapDistributor must not be null");
    }

    public CoverLayoutPlan calculate(CoverComponent cover, DocumentProfile profile) {
        Objects.requireNonNull(cover, "cover must not be null");
        Objects.requireNonNull(profile, "profile must not be null");

        StyleResolver styleResolver = new StyleResolver(profile);
        CoverComponentRule coverRule = new ComponentRuleResolver(profile).resolve(
                COVER_COMPONENT_ID,
                CoverComponentRule.class
        );
        CoverLayoutRule layoutRule = coverRule.layoutRule();
        PageRule pageRule = profile.pageRule();

        StyleRule topStyle = styleResolver.resolve(coverRule.styleMapping().topLinesStyleId());
        StyleRule authorStyle = styleResolver.resolve(coverRule.styleMapping().authorLinesStyleId());
        StyleRule titleStyle = styleResolver.resolve(coverRule.styleMapping().titleStyleId());
        StyleRule subtitleStyle = styleResolver.resolve(coverRule.styleMapping().subtitleStyleId());
        StyleRule bottomStyle = styleResolver.resolve(coverRule.styleMapping().bottomLinesStyleId());

        List<LayoutGroup> groups = new ArrayList<>();

        measureOptionalBlock(
                TOP_BLOCK_ID,
                cover.topLines(),
                topStyle,
                pageRule
        ).ifPresent(block -> groups.add(LayoutGroup.of(TOP_BLOCK_ID, List.of(block.toTextElement()))));

        measureOptionalBlock(
                AUTHORS_BLOCK_ID,
                cover.authorLines(),
                authorStyle,
                pageRule
        ).ifPresent(block -> groups.add(LayoutGroup.of(AUTHORS_BLOCK_ID, List.of(block.toTextElement()))));

        List<CoverLayoutElement> titleElements = new ArrayList<>();
        titleElements.add(measureRequiredText(
                TITLE_BLOCK_ID,
                cover.title(),
                titleStyle,
                pageRule
        ).toTextElement());

        cover.subtitle()
                .map(subtitle -> measureRequiredText(
                        SUBTITLE_BLOCK_ID,
                        subtitle,
                        subtitleStyle,
                        pageRule
                ))
                .map(MeasuredCoverBlock::toTextElement)
                .ifPresent(titleElements::add);

        groups.add(LayoutGroup.of(TITLE_BLOCK_ID, titleElements));

        MeasuredCoverBlock bottomBlock = measureBottomBlock(
                cover.bottomLines(),
                bottomStyle,
                pageRule
        );

        groups.add(LayoutGroup.of(BOTTOM_BLOCK_ID, List.of(bottomBlock.toTextElement())));

        int lineHeightTwips = calculateLayoutLineHeightTwips(groups);
        SinglePageRenderableArea renderableArea = renderableAreaCalculator.calculate(pageRule, lineHeightTwips);
        int pageCapacityLines = renderableArea.safeLineCapacity();

        int contentLines = groups.stream()
                .mapToInt(LayoutGroup::lineCount)
                .sum();

        if (contentLines > pageCapacityLines) {
            throw new CoverLayoutOverflowException(new CoverLayoutFailureDiagnostic(
                    renderableArea,
                    contentLines,
                    contentLines - pageCapacityLines,
                    createBlockLineCounts(groups),
                    MeasurementConverter.twipsToPoints(lineHeightTwips)
            ));
        }

        int availableGapLines = pageCapacityLines - contentLines;
        List<BigDecimal> gapWeights = createGapWeights(groups, layoutRule);
        int[] gapLineCounts = gapDistributor.distribute(availableGapLines, gapWeights);
        BigDecimal exactLineHeightPt = MeasurementConverter.twipsToPoints(lineHeightTwips);

        List<CoverLayoutElement> elements = assembleElements(groups, gapLineCounts);
        CoverLayoutDiagnostic diagnostic = new CoverLayoutDiagnostic(
                renderableArea,
                contentLines,
                availableGapLines,
                createBlockLineCounts(groups),
                createGapLineCounts(groups, gapLineCounts),
                exactLineHeightPt
        );

        return new CoverLayoutPlan(
                elements,
                pageCapacityLines,
                pageCapacityLines,
                exactLineHeightPt,
                diagnostic
        );
    }

    private Optional<MeasuredCoverBlock> measureOptionalBlock(
            String blockId,
            List<String> sourceLines,
            StyleRule styleRule,
            PageRule pageRule
    ) {
        if (sourceLines.isEmpty()) {
            return Optional.empty();
        }

        List<String> measuredLines = new ArrayList<>();

        for (String sourceLine : sourceLines) {
            measuredLines.addAll(textMeasurer.measure(
                    sourceLine,
                    pageRule,
                    styleRule
            ).visualLines());
        }

        return Optional.of(new MeasuredCoverBlock(blockId, styleRule, measuredLines));
    }

    private MeasuredCoverBlock measureBottomBlock(
            List<String> sourceLines,
            StyleRule styleRule,
            PageRule pageRule
    ) {
        if (sourceLines.isEmpty()) {
            throw InvalidCoverContentException.missingBottomLines();
        }

        if (sourceLines.size() != REQUIRED_BOTTOM_LINE_COUNT) {
            throw InvalidCoverContentException.invalidBottomLines();
        }

        List<String> measuredLines = new ArrayList<>();

        for (String sourceLine : sourceLines) {
            List<String> visualLines = textMeasurer.measure(
                    sourceLine,
                    pageRule,
                    styleRule
            ).visualLines();

            if (visualLines.size() != 1) {
                throw InvalidCoverContentException.invalidBottomLines();
            }

            measuredLines.add(visualLines.getFirst());
        }

        return new MeasuredCoverBlock(BOTTOM_BLOCK_ID, styleRule, measuredLines);
    }

    private MeasuredCoverBlock measureRequiredText(
            String blockId,
            String text,
            StyleRule styleRule,
            PageRule pageRule
    ) {
        return new MeasuredCoverBlock(
                blockId,
                styleRule,
                textMeasurer.measure(text, pageRule, styleRule).visualLines()
        );
    }

    private static int calculateLayoutLineHeightTwips(List<LayoutGroup> groups) {
        int maxLineHeightTwips = 0;

        for (LayoutGroup group : groups) {
            for (CoverLayoutElement element : group.elements()) {
                if (element instanceof CoverTextLines textLines) {
                    validateSinglePageSpacing(textLines.styleRule());
                    maxLineHeightTwips = Math.max(
                            maxLineHeightTwips,
                            exactLineHeightTwips(textLines.styleRule())
                    );
                }
            }
        }

        if (maxLineHeightTwips <= 0) {
            throw new IllegalArgumentException("layout line height must be greater than zero.");
        }

        return maxLineHeightTwips;
    }

    private static int exactLineHeightTwips(StyleRule styleRule) {
        return MeasurementConverter.pointsToTwips(styleRule.fontSizePt().multiply(styleRule.lineSpacing()));
    }

    private static void validateSinglePageSpacing(StyleRule styleRule) {
        if (styleRule.spacingBeforePt().compareTo(BigDecimal.ZERO) != 0) {
            throw InvalidSinglePageStyleException.spacingBeforeMustBeZero();
        }

        if (styleRule.spacingAfterPt().compareTo(BigDecimal.ZERO) != 0) {
            throw InvalidSinglePageStyleException.spacingAfterMustBeZero();
        }
    }

    private static List<BigDecimal> createGapWeights(
            List<LayoutGroup> groups,
            CoverLayoutRule layoutRule
    ) {
        List<BigDecimal> weights = new ArrayList<>();

        for (int index = 0; index < groups.size() - 1; index++) {
            weights.add(resolveGapWeight(groups.get(index).id(), groups.get(index + 1).id(), layoutRule));
        }

        return List.copyOf(weights);
    }

    private static BigDecimal resolveGapWeight(
            String currentGroupId,
            String nextGroupId,
            CoverLayoutRule layoutRule
    ) {
        if (TOP_BLOCK_ID.equals(currentGroupId) && AUTHORS_BLOCK_ID.equals(nextGroupId)) {
            return layoutRule.topToAuthorWeight();
        }

        if (AUTHORS_BLOCK_ID.equals(currentGroupId) && TITLE_BLOCK_ID.equals(nextGroupId)) {
            return layoutRule.authorToTitleWeight();
        }

        if (TOP_BLOCK_ID.equals(currentGroupId) && TITLE_BLOCK_ID.equals(nextGroupId)) {
            return layoutRule.topToAuthorWeight().add(layoutRule.authorToTitleWeight());
        }

        if (TITLE_BLOCK_ID.equals(currentGroupId) && BOTTOM_BLOCK_ID.equals(nextGroupId)) {
            return layoutRule.titleToBottomWeight();
        }

        throw new IllegalArgumentException(
                "Unsupported cover gap between groups: " + currentGroupId + " and " + nextGroupId + "."
        );
    }

    private static List<CoverLayoutElement> assembleElements(
            List<LayoutGroup> groups,
            int[] gapLineCounts
    ) {
        List<CoverLayoutElement> elements = new ArrayList<>();

        for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
            LayoutGroup group = groups.get(groupIndex);

            if (groupIndex > 0) {
                int gapLineCount = gapLineCounts[groupIndex - 1];

                if (gapLineCount > 0) {
                    elements.add(new CoverSpacerLines(
                            groups.get(groupIndex - 1).id() + "-to-" + group.id(),
                            gapLineCount,
                            group.firstStyleRule()
                    ));
                }
            }

            elements.addAll(group.elements());
        }

        return List.copyOf(elements);
    }

    private static Map<String, Integer> createBlockLineCounts(List<LayoutGroup> groups) {
        Map<String, Integer> lineCounts = new LinkedHashMap<>();

        for (LayoutGroup group : groups) {
            lineCounts.put(group.id(), group.lineCount());
        }

        return lineCounts;
    }

    private static Map<String, Integer> createGapLineCounts(List<LayoutGroup> groups, int[] gapLineCounts) {
        Map<String, Integer> lineCounts = new LinkedHashMap<>();

        for (int index = 0; index < gapLineCounts.length; index++) {
            lineCounts.put(
                    groups.get(index).id() + "-to-" + groups.get(index + 1).id(),
                    gapLineCounts[index]
            );
        }

        return lineCounts;
    }

    private record LayoutGroup(
            String id,
            List<CoverLayoutElement> elements,
            int lineCount,
            StyleRule firstStyleRule
    ) {

        private LayoutGroup {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("id must not be blank.");
            }

            Objects.requireNonNull(elements, "elements must not be null");
            Objects.requireNonNull(firstStyleRule, "firstStyleRule must not be null");
            elements = List.copyOf(elements);

            if (elements.isEmpty()) {
                throw new IllegalArgumentException("elements must not be empty.");
            }
        }

        static LayoutGroup of(String id, List<CoverLayoutElement> elements) {
            StyleRule firstStyleRule = null;
            int lineCount = 0;

            for (CoverLayoutElement element : elements) {
                lineCount += element.lineCount();

                if (firstStyleRule == null && element instanceof CoverTextLines textLines) {
                    firstStyleRule = textLines.styleRule();
                }
            }

            if (firstStyleRule == null) {
                throw new IllegalArgumentException("layout group must contain text lines.");
            }

            return new LayoutGroup(id, elements, lineCount, firstStyleRule);
        }
    }

}
