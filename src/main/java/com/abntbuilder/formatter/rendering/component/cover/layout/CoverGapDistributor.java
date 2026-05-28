package com.abntbuilder.formatter.rendering.component.cover.layout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public final class CoverGapDistributor {

    public int[] distribute(int availableGapLines, List<BigDecimal> gapWeights) {
        Objects.requireNonNull(gapWeights, "gapWeights must not be null");

        if (availableGapLines < 0) {
            throw new IllegalArgumentException("availableGapLines must not be negative.");
        }

        gapWeights = List.copyOf(gapWeights);

        if (gapWeights.isEmpty()) {
            if (availableGapLines == 0) {
                return new int[0];
            }

            throw new IllegalArgumentException("Cannot distribute gap lines without gaps.");
        }

        for (BigDecimal weight : gapWeights) {
            Objects.requireNonNull(weight, "gapWeights must not contain null values.");

            if (weight.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("gapWeights must contain only positive values.");
            }
        }

        int[] gaps = new int[gapWeights.size()];

        if (availableGapLines == 0) {
            return gaps;
        }

        int minimumLinesPerGap = availableGapLines >= gapWeights.size()
                ? 1
                : 0;
        int remainingGapLines = availableGapLines - (minimumLinesPerGap * gapWeights.size());

        if (minimumLinesPerGap > 0) {
            for (int index = 0; index < gaps.length; index++) {
                gaps[index] = minimumLinesPerGap;
            }
        }

        if (remainingGapLines == 0) {
            return gaps;
        }

        BigDecimal totalWeight = gapWeights.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int assignedLines = 0;

        for (int index = 0; index < gapWeights.size() - 1; index++) {
            int gapLines = BigDecimal.valueOf(remainingGapLines)
                    .multiply(gapWeights.get(index))
                    .divide(totalWeight, 0, RoundingMode.FLOOR)
                    .intValueExact();

            gaps[index] += gapLines;
            assignedLines += gapLines;
        }

        gaps[gapWeights.size() - 1] += remainingGapLines - assignedLines;

        return gaps;
    }
}
