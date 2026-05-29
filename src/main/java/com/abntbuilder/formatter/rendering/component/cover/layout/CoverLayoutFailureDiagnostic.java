package com.abntbuilder.formatter.rendering.component.cover.layout;

import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageRenderableArea;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record CoverLayoutFailureDiagnostic(
        SinglePageRenderableArea renderableArea,
        int contentLineCount,
        int overflowLineCount,
        Map<String, Integer> blockLineCounts,
        BigDecimal exactLineHeightPt
) {

    public CoverLayoutFailureDiagnostic {
        Objects.requireNonNull(renderableArea, "renderableArea must not be null");
        Objects.requireNonNull(blockLineCounts, "blockLineCounts must not be null");
        Objects.requireNonNull(exactLineHeightPt, "exactLineHeightPt must not be null");

        if (contentLineCount < 0) {
            throw new IllegalArgumentException("contentLineCount must not be negative.");
        }

        if (overflowLineCount <= 0) {
            throw new IllegalArgumentException("overflowLineCount must be greater than zero.");
        }

        if (exactLineHeightPt.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("exactLineHeightPt must be greater than zero.");
        }

        blockLineCounts = copyLineCounts(blockLineCounts);

        int blockLineSum = blockLineCounts.values().stream()
                .mapToInt(Integer::intValue)
                .sum();

        if (blockLineSum != contentLineCount) {
            throw new IllegalArgumentException("blockLineCounts must sum to contentLineCount.");
        }

        int expectedOverflowLineCount = contentLineCount - renderableArea.safeLineCapacity();

        if (expectedOverflowLineCount != overflowLineCount) {
            throw new IllegalArgumentException(
                    "overflowLineCount must match contentLineCount minus safeLineCapacity."
            );
        }
    }

    private static Map<String, Integer> copyLineCounts(Map<String, Integer> lineCounts) {
        Map<String, Integer> copy = new LinkedHashMap<>();

        for (Map.Entry<String, Integer> entry : lineCounts.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();

            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("blockLineCounts keys must not be blank.");
            }

            Objects.requireNonNull(value, "blockLineCounts values must not be null.");

            if (value < 0) {
                throw new IllegalArgumentException("blockLineCounts values must not be negative.");
            }

            copy.put(key, value);
        }

        return Collections.unmodifiableMap(copy);
    }
}
