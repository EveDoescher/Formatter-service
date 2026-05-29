package com.abntbuilder.formatter.rendering.layout.singlepage;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record SinglePageLayoutFailureDiagnostic(
        SinglePageRenderableArea renderableArea,
        int contentLineCount,
        int overflowLineCount,
        Map<String, Integer> groupLineCounts,
        Map<String, Integer> itemLineCounts,
        BigDecimal exactLineHeightPt
) {

    public SinglePageLayoutFailureDiagnostic {
        Objects.requireNonNull(renderableArea, "renderableArea must not be null");
        Objects.requireNonNull(groupLineCounts, "groupLineCounts must not be null");
        Objects.requireNonNull(itemLineCounts, "itemLineCounts must not be null");
        Objects.requireNonNull(exactLineHeightPt, "exactLineHeightPt must not be null");

        if (contentLineCount < 0) {
            throw new IllegalArgumentException("contentLineCount must not be negative.");
        }

        if (overflowLineCount <= 0) {
            throw new IllegalArgumentException("overflowLineCount must be greater than zero.");
        }

        if (contentLineCount - renderableArea.safeLineCapacity() != overflowLineCount) {
            throw new IllegalArgumentException(
                    "overflowLineCount must match contentLineCount minus safeLineCapacity."
            );
        }

        if (exactLineHeightPt.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("exactLineHeightPt must be greater than zero.");
        }

        groupLineCounts = Map.copyOf(new LinkedHashMap<>(groupLineCounts));
        itemLineCounts = Map.copyOf(new LinkedHashMap<>(itemLineCounts));

        if (sum(groupLineCounts) != contentLineCount) {
            throw new IllegalArgumentException("groupLineCounts must sum to contentLineCount.");
        }

        if (sum(itemLineCounts) != contentLineCount) {
            throw new IllegalArgumentException("itemLineCounts must sum to contentLineCount.");
        }
    }

    private static int sum(Map<String, Integer> lineCounts) {
        return lineCounts.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
    }
}
