package com.abntbuilder.formatter.rendering.layout.singlepage;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record SinglePageLayoutDiagnostic(
        SinglePageRenderableArea renderableArea,
        int contentLineCount,
        int availableGapLines,
        Map<String, Integer> groupLineCounts,
        Map<String, Integer> itemLineCounts,
        Map<String, Integer> gapLineCounts,
        BigDecimal exactLineHeightPt
) {

    public SinglePageLayoutDiagnostic {
        Objects.requireNonNull(renderableArea, "renderableArea must not be null");
        Objects.requireNonNull(groupLineCounts, "groupLineCounts must not be null");
        Objects.requireNonNull(itemLineCounts, "itemLineCounts must not be null");
        Objects.requireNonNull(gapLineCounts, "gapLineCounts must not be null");
        Objects.requireNonNull(exactLineHeightPt, "exactLineHeightPt must not be null");

        if (contentLineCount < 0) {
            throw new IllegalArgumentException("contentLineCount must not be negative.");
        }

        if (availableGapLines < 0) {
            throw new IllegalArgumentException("availableGapLines must not be negative.");
        }

        if (exactLineHeightPt.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("exactLineHeightPt must be greater than zero.");
        }

        groupLineCounts = Map.copyOf(new LinkedHashMap<>(groupLineCounts));
        itemLineCounts = Map.copyOf(new LinkedHashMap<>(itemLineCounts));
        gapLineCounts = Map.copyOf(new LinkedHashMap<>(gapLineCounts));

        if (sum(groupLineCounts) != contentLineCount) {
            throw new IllegalArgumentException("groupLineCounts must sum to contentLineCount.");
        }

        if (sum(itemLineCounts) != contentLineCount) {
            throw new IllegalArgumentException("itemLineCounts must sum to contentLineCount.");
        }

        if (sum(gapLineCounts) != availableGapLines) {
            throw new IllegalArgumentException("gapLineCounts must sum to availableGapLines.");
        }

        if (contentLineCount + availableGapLines != renderableArea.safeLineCapacity()) {
            throw new IllegalArgumentException(
                    "contentLineCount plus availableGapLines must match safeLineCapacity."
            );
        }
    }

    private static int sum(Map<String, Integer> lineCounts) {
        return lineCounts.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
    }
}
