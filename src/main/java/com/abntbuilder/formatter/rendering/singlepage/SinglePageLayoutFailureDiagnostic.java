package com.abntbuilder.formatter.rendering.singlepage;

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
        int contentHeightTwips,
        int overflowHeightTwips,
        Map<String, Integer> groupHeightTwips,
        Map<String, Integer> itemHeightTwips,
        BigDecimal exactLineHeightPt
) {

    public SinglePageLayoutFailureDiagnostic(
            SinglePageRenderableArea renderableArea,
            int contentLineCount,
            int overflowLineCount,
            Map<String, Integer> groupLineCounts,
            Map<String, Integer> itemLineCounts,
            BigDecimal exactLineHeightPt
    ) {
        this(
                lineBasedRenderableArea(renderableArea),
                contentLineCount,
                overflowLineCount,
                groupLineCounts,
                itemLineCounts,
                contentLineCount,
                overflowLineCount,
                groupLineCounts,
                itemLineCounts,
                exactLineHeightPt
        );
    }

    public SinglePageLayoutFailureDiagnostic {
        Objects.requireNonNull(renderableArea, "renderableArea must not be null");
        Objects.requireNonNull(groupLineCounts, "groupLineCounts must not be null");
        Objects.requireNonNull(itemLineCounts, "itemLineCounts must not be null");
        Objects.requireNonNull(groupHeightTwips, "groupHeightTwips must not be null");
        Objects.requireNonNull(itemHeightTwips, "itemHeightTwips must not be null");
        Objects.requireNonNull(exactLineHeightPt, "exactLineHeightPt must not be null");

        if (contentLineCount < 0) {
            throw new IllegalArgumentException("contentLineCount must not be negative.");
        }

        if (overflowLineCount <= 0) {
            throw new IllegalArgumentException("overflowLineCount must be greater than zero.");
        }

        if (contentHeightTwips < 0) {
            throw new IllegalArgumentException("contentHeightTwips must not be negative.");
        }

        if (overflowHeightTwips <= 0) {
            throw new IllegalArgumentException("overflowHeightTwips must be greater than zero.");
        }

        if (contentHeightTwips - renderableArea.safeHeightTwips() != overflowHeightTwips) {
            throw new IllegalArgumentException(
                    "overflowHeightTwips must match contentHeightTwips minus safeHeightTwips."
            );
        }

        if (exactLineHeightPt.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("exactLineHeightPt must be greater than zero.");
        }

        groupLineCounts = Map.copyOf(new LinkedHashMap<>(groupLineCounts));
        itemLineCounts = Map.copyOf(new LinkedHashMap<>(itemLineCounts));
        groupHeightTwips = Map.copyOf(new LinkedHashMap<>(groupHeightTwips));
        itemHeightTwips = Map.copyOf(new LinkedHashMap<>(itemHeightTwips));

        if (sum(groupLineCounts) != contentLineCount) {
            throw new IllegalArgumentException("groupLineCounts must sum to contentLineCount.");
        }

        if (sum(itemLineCounts) != contentLineCount) {
            throw new IllegalArgumentException("itemLineCounts must sum to contentLineCount.");
        }

        if (sum(groupHeightTwips) != contentHeightTwips) {
            throw new IllegalArgumentException("groupHeightTwips must sum to contentHeightTwips.");
        }

        if (sum(itemHeightTwips) != contentHeightTwips) {
            throw new IllegalArgumentException("itemHeightTwips must sum to contentHeightTwips.");
        }
    }

    private static int sum(Map<String, Integer> lineCounts) {
        return lineCounts.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    private static SinglePageRenderableArea lineBasedRenderableArea(SinglePageRenderableArea renderableArea) {
        Objects.requireNonNull(renderableArea, "renderableArea must not be null");

        return new SinglePageRenderableArea(
                renderableArea.physicalLineCapacity(),
                renderableArea.boundarySafetyLineCount(),
                renderableArea.safeLineCapacity()
        );
    }
}
