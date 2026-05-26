package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.shared.exception.SinglePageLayoutOverflowException;
import com.abntbuilder.formatter.shared.measurement.MeasurementConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SinglePageLayoutEngine {

    private static final int LAYOUT_SCALE = 6;

    public SinglePageLayoutResult layout(
            PageRule pageRule,
            List<SinglePageLayoutGroup> groups,
            BigDecimal minimumGapBetweenGroupsCm
    ) {
        Objects.requireNonNull(pageRule, "pageRule must not be null");
        Objects.requireNonNull(groups, "groups must not be null");
        requireNonNegative(minimumGapBetweenGroupsCm, "minimumGapBetweenGroupsCm");

        if (groups.isEmpty()) {
            throw new IllegalArgumentException("groups must not be empty.");
        }

        groups = List.copyOf(groups);

        for (SinglePageLayoutGroup group : groups) {
            Objects.requireNonNull(group, "groups must not contain null values.");
        }

        BigDecimal usableHeightCm = pageRule.usableHeightCm();
        BigDecimal contentHeightCm = calculateContentHeight(groups);
        int gapCount = Math.max(groups.size() - 1, 0);

        BigDecimal minimumRequiredGapHeight = minimumGapBetweenGroupsCm.multiply(BigDecimal.valueOf(gapCount));
        BigDecimal requiredHeightCm = contentHeightCm.add(minimumRequiredGapHeight);

        if (requiredHeightCm.compareTo(usableHeightCm) > 0) {
            throw new SinglePageLayoutOverflowException(requiredHeightCm, usableHeightCm);
        }

        BigDecimal gapBetweenGroupsCm = calculateDistributedGap(
                usableHeightCm,
                contentHeightCm,
                gapCount
        );

        List<PositionedLayoutGroup> positionedGroups = positionGroups(
                groups,
                usableHeightCm,
                gapBetweenGroupsCm
        );

        return new SinglePageLayoutResult(
                usableHeightCm,
                contentHeightCm,
                gapBetweenGroupsCm,
                positionedGroups
        );
    }

    private static BigDecimal calculateContentHeight(List<SinglePageLayoutGroup> groups) {
        BigDecimal totalHeight = BigDecimal.ZERO;

        for (SinglePageLayoutGroup group : groups) {
            totalHeight = totalHeight.add(calculateGroupHeight(group));
        }

        return totalHeight;
    }

    private static BigDecimal calculateGroupHeight(SinglePageLayoutGroup group) {
        BigDecimal groupHeight = BigDecimal.ZERO;

        for (SinglePageLayoutTextLine line : group.lines()) {
            groupHeight = groupHeight.add(calculateLineHeight(line.styleRule()));
        }

        return groupHeight;
    }

    private static BigDecimal calculateLineHeight(StyleRule styleRule) {
        BigDecimal textLineHeightPt = styleRule.fontSizePt().multiply(styleRule.lineSpacing());

        BigDecimal totalLineHeightPt = styleRule.spacingBeforePt()
                .add(textLineHeightPt)
                .add(styleRule.spacingAfterPt());

        return MeasurementConverter.pointsToCentimeters(totalLineHeightPt);
    }

    private static BigDecimal calculateDistributedGap(
            BigDecimal usableHeightCm,
            BigDecimal contentHeightCm,
            int gapCount
    ) {
        if (gapCount == 0) {
            return BigDecimal.ZERO;
        }

        return usableHeightCm
                .subtract(contentHeightCm)
                .divide(BigDecimal.valueOf(gapCount), LAYOUT_SCALE, RoundingMode.HALF_UP);
    }

    private static List<PositionedLayoutGroup> positionGroups(
            List<SinglePageLayoutGroup> groups,
            BigDecimal usableHeightCm,
            BigDecimal gapBetweenGroupsCm
    ) {
        List<PositionedLayoutGroup> positionedGroups = new ArrayList<>();
        BigDecimal cursorCm = BigDecimal.ZERO;

        for (int index = 0; index < groups.size(); index++) {
            SinglePageLayoutGroup group = groups.get(index);
            BigDecimal groupHeightCm = calculateGroupHeight(group);

            BigDecimal yStartCm;

            if (groups.size() > 1 && index == groups.size() - 1) {
                yStartCm = usableHeightCm.subtract(groupHeightCm);
            } else {
                yStartCm = cursorCm;
            }

            positionedGroups.add(new PositionedLayoutGroup(
                    group.id(),
                    yStartCm,
                    groupHeightCm,
                    group.lines()
            ));

            cursorCm = yStartCm
                    .add(groupHeightCm)
                    .add(gapBetweenGroupsCm);
        }

        return List.copyOf(positionedGroups);
    }

    private static void requireNonNegative(BigDecimal value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative.");
        }
    }
}