package com.abntbuilder.formatter.rendering.singlepage;

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
        int contentHeightTwips,
        int availableGapHeightTwips,
        int allocatedGapHeightTwips,
        Map<String, Integer> groupHeightTwips,
        Map<String, Integer> itemHeightTwips,
        Map<String, Integer> gapHeightTwips,
        BigDecimal exactLineHeightPt
) {

    public SinglePageLayoutDiagnostic(
            SinglePageRenderableArea renderableArea,
            int contentLineCount,
            int availableGapLines,
            Map<String, Integer> groupLineCounts,
            Map<String, Integer> itemLineCounts,
            Map<String, Integer> gapLineCounts,
            BigDecimal exactLineHeightPt
    ) {
        this(
                lineBasedRenderableArea(renderableArea),
                contentLineCount,
                availableGapLines,
                groupLineCounts,
                itemLineCounts,
                gapLineCounts,
                contentLineCount,
                availableGapLines,
                availableGapLines,
                groupLineCounts,
                itemLineCounts,
                gapLineCounts,
                exactLineHeightPt
        );
    }

    public SinglePageLayoutDiagnostic {
        Objects.requireNonNull(renderableArea, "renderableArea must not be null");
        Objects.requireNonNull(groupLineCounts, "groupLineCounts must not be null");
        Objects.requireNonNull(itemLineCounts, "itemLineCounts must not be null");
        Objects.requireNonNull(gapLineCounts, "gapLineCounts must not be null");
        Objects.requireNonNull(groupHeightTwips, "groupHeightTwips must not be null");
        Objects.requireNonNull(itemHeightTwips, "itemHeightTwips must not be null");
        Objects.requireNonNull(gapHeightTwips, "gapHeightTwips must not be null");
        Objects.requireNonNull(exactLineHeightPt, "exactLineHeightPt must not be null");

        if (contentLineCount < 0) {
            throw new IllegalArgumentException("contentLineCount must not be negative.");
        }

        if (availableGapLines < 0) {
            throw new IllegalArgumentException("availableGapLines must not be negative.");
        }

        if (contentHeightTwips < 0) {
            throw new IllegalArgumentException("contentHeightTwips must not be negative.");
        }

        if (availableGapHeightTwips < 0) {
            throw new IllegalArgumentException("availableGapHeightTwips must not be negative.");
        }

        if (allocatedGapHeightTwips < 0) {
            throw new IllegalArgumentException("allocatedGapHeightTwips must not be negative.");
        }

        if (exactLineHeightPt.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("exactLineHeightPt must be greater than zero.");
        }

        groupLineCounts = Map.copyOf(new LinkedHashMap<>(groupLineCounts));
        itemLineCounts = Map.copyOf(new LinkedHashMap<>(itemLineCounts));
        gapLineCounts = Map.copyOf(new LinkedHashMap<>(gapLineCounts));
        groupHeightTwips = Map.copyOf(new LinkedHashMap<>(groupHeightTwips));
        itemHeightTwips = Map.copyOf(new LinkedHashMap<>(itemHeightTwips));
        gapHeightTwips = Map.copyOf(new LinkedHashMap<>(gapHeightTwips));

        if (sum(groupLineCounts) != contentLineCount) {
            throw new IllegalArgumentException("groupLineCounts must sum to contentLineCount.");
        }

        if (sum(itemLineCounts) != contentLineCount) {
            throw new IllegalArgumentException("itemLineCounts must sum to contentLineCount.");
        }

        if (sum(gapLineCounts) != availableGapLines) {
            throw new IllegalArgumentException("gapLineCounts must sum to availableGapLines.");
        }

        if (usesLineCountsAsHeights(renderableArea)
                && contentLineCount + availableGapLines != renderableArea.safeLineCapacity()) {
            throw new IllegalArgumentException(
                    "contentLineCount plus availableGapLines must match safeLineCapacity."
            );
        }

        if (sum(groupHeightTwips) != contentHeightTwips) {
            throw new IllegalArgumentException("groupHeightTwips must sum to contentHeightTwips.");
        }

        if (sum(itemHeightTwips) != contentHeightTwips) {
            throw new IllegalArgumentException("itemHeightTwips must sum to contentHeightTwips.");
        }

        if (sum(gapHeightTwips) != allocatedGapHeightTwips) {
            throw new IllegalArgumentException("gapHeightTwips must sum to allocatedGapHeightTwips.");
        }

        if (contentHeightTwips + availableGapHeightTwips != renderableArea.safeHeightTwips()) {
            throw new IllegalArgumentException(
                    "contentHeightTwips plus availableGapHeightTwips must match safeHeightTwips."
            );
        }

        if (allocatedGapHeightTwips > availableGapHeightTwips) {
            throw new IllegalArgumentException("allocatedGapHeightTwips must not exceed availableGapHeightTwips.");
        }
    }

    private static int sum(Map<String, Integer> lineCounts) {
        return lineCounts.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    private static boolean usesLineCountsAsHeights(SinglePageRenderableArea renderableArea) {
        return renderableArea.physicalHeightTwips() == renderableArea.physicalLineCapacity()
                && renderableArea.boundarySafetyHeightTwips() == renderableArea.boundarySafetyLineCount()
                && renderableArea.safeHeightTwips() == renderableArea.safeLineCapacity();
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
