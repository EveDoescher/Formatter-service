package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.output.docx.api.DocxBlankLine;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.shared.exception.InvalidSinglePageStyleException;
import com.abntbuilder.formatter.shared.exception.SinglePageLayoutOverflowException;
import com.abntbuilder.formatter.shared.measurement.MeasurementConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class SinglePageLayoutDocxMapper {

    private final SinglePageLayoutLineMetrics lineMetrics;
    private final SinglePageRenderableAreaCalculator renderableAreaCalculator;

    public SinglePageLayoutDocxMapper() {
        this(new SinglePageLayoutLineMetrics(), new SinglePageRenderableAreaCalculator());
    }

    public SinglePageLayoutDocxMapper(SinglePageLayoutLineMetrics lineMetrics) {
        this(lineMetrics, new SinglePageRenderableAreaCalculator());
    }

    public SinglePageLayoutDocxMapper(
            SinglePageLayoutLineMetrics lineMetrics,
            SinglePageRenderableAreaCalculator renderableAreaCalculator
    ) {
        this.lineMetrics = Objects.requireNonNull(lineMetrics, "lineMetrics must not be null");
        this.renderableAreaCalculator = Objects.requireNonNull(
                renderableAreaCalculator,
                "renderableAreaCalculator must not be null"
        );
    }

    public List<DocxBlock> mapToDocxBlocksAnchoringLastGroup(
            PageRule pageRule,
            List<SinglePageLayoutGroup> groups,
            List<BigDecimal> gapWeights
    ) {
        Objects.requireNonNull(pageRule, "pageRule must not be null");
        Objects.requireNonNull(groups, "groups must not be null");
        Objects.requireNonNull(gapWeights, "gapWeights must not be null");

        if (groups.isEmpty()) {
            throw new IllegalArgumentException("groups must not be empty.");
        }

        groups = List.copyOf(groups);
        gapWeights = List.copyOf(gapWeights);

        validateGapWeights(groups, gapWeights);
        validateSinglePageSpacing(groups);

        int layoutLineHeightTwips = lineMetrics.layoutLineHeightTwips(groups);
        int safeLineSlots = renderableAreaCalculator
                .calculate(pageRule, layoutLineHeightTwips)
                .safeLineCapacity();

        if (safeLineSlots <= 0) {
            throw SinglePageLayoutOverflowException.forLineSlots(1, safeLineSlots);
        }

        SinglePageLayoutGroup lastGroup = groups.getLast();
        int lastGroupLineSlots = lastGroup.lines().size();

        int lastGroupStartSlot = safeLineSlots - lastGroupLineSlots;

        if (lastGroupStartSlot < 0) {
            throw SinglePageLayoutOverflowException.forLineSlots(
                    lastGroupLineSlots,
                    safeLineSlots
            );
        }

        int preLastContentLineSlots = calculateContentLineSlotsBeforeLastGroup(groups);
        int availableGapLineSlots = lastGroupStartSlot - preLastContentLineSlots;

        if (availableGapLineSlots < 0) {
            throw SinglePageLayoutOverflowException.forLineSlots(
                    preLastContentLineSlots + lastGroupLineSlots,
                    safeLineSlots
            );
        }

        int[] gapLineSlots = distributeGapLineSlots(availableGapLineSlots, gapWeights);
        BigDecimal exactLayoutLineHeightPt = MeasurementConverter.twipsToPoints(layoutLineHeightTwips);

        List<DocxBlock> blocks = new ArrayList<>();

        for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
            SinglePageLayoutGroup group = groups.get(groupIndex);

            if (groupIndex > 0) {
                StyleRule spacerStyle = group.lines().getFirst().styleRule();

                addBlankLines(
                        blocks,
                        spacerStyle,
                        gapLineSlots[groupIndex - 1],
                        exactLayoutLineHeightPt
                );
            }

            addGroup(blocks, group, exactLayoutLineHeightPt);
        }

        return List.copyOf(blocks);
    }

    private static void validateGapWeights(
            List<SinglePageLayoutGroup> groups,
            List<BigDecimal> gapWeights
    ) {
        int expectedGapCount = Math.max(groups.size() - 1, 0);

        if (gapWeights.size() != expectedGapCount) {
            throw new IllegalArgumentException("gapWeights size must be equal to groups size minus one.");
        }

        for (BigDecimal weight : gapWeights) {
            Objects.requireNonNull(weight, "gapWeights must not contain null values.");

            if (weight.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("gapWeights must contain only positive values.");
            }
        }
    }

    private static void validateSinglePageSpacing(List<SinglePageLayoutGroup> groups) {
        for (SinglePageLayoutGroup group : groups) {
            for (SinglePageLayoutTextLine line : group.lines()) {
                StyleRule styleRule = line.styleRule();

                if (styleRule.spacingBeforePt().compareTo(BigDecimal.ZERO) != 0) {
                    throw InvalidSinglePageStyleException.spacingBeforeMustBeZero();
                }

                if (styleRule.spacingAfterPt().compareTo(BigDecimal.ZERO) != 0) {
                    throw InvalidSinglePageStyleException.spacingAfterMustBeZero();
                }
            }
        }
    }

    private static int calculateContentLineSlotsBeforeLastGroup(List<SinglePageLayoutGroup> groups) {
        int count = 0;

        for (int index = 0; index < groups.size() - 1; index++) {
            count += groups.get(index).lines().size();
        }

        return count;
    }

    private static int[] distributeGapLineSlots(
            int availableGapLineSlots,
            List<BigDecimal> gapWeights
    ) {
        int[] gaps = new int[gapWeights.size()];

        if (availableGapLineSlots <= 0 || gapWeights.isEmpty()) {
            return gaps;
        }

        BigDecimal totalWeight = gapWeights.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int assignedSlots = 0;

        for (int index = 0; index < gapWeights.size() - 1; index++) {
            int gapSlots = BigDecimal.valueOf(availableGapLineSlots)
                    .multiply(gapWeights.get(index))
                    .divide(totalWeight, 0, RoundingMode.FLOOR)
                    .intValueExact();

            gaps[index] = gapSlots;
            assignedSlots += gapSlots;
        }

        gaps[gapWeights.size() - 1] = availableGapLineSlots - assignedSlots;

        return gaps;
    }

    private static void addBlankLines(
            List<DocxBlock> blocks,
            StyleRule spacerStyle,
            int count,
            BigDecimal exactLayoutLineHeightPt
    ) {
        if (count <= 0) {
            return;
        }

        for (int index = 0; index < count; index++) {
            blocks.add(new DocxBlankLine(
                    spacerStyle,
                    Optional.of(exactLayoutLineHeightPt)
            ));
        }
    }

    private static void addGroup(
            List<DocxBlock> blocks,
            SinglePageLayoutGroup group,
            BigDecimal exactLayoutLineHeightPt
    ) {
        for (SinglePageLayoutTextLine line : group.lines()) {
            blocks.add(new DocxParagraph(
                    line.text(),
                    line.styleRule(),
                    Optional.empty(),
                    Optional.of(exactLayoutLineHeightPt)
            ));
        }
    }
}
