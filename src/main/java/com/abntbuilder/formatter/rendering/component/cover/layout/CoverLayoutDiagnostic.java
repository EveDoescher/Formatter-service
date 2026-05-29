package com.abntbuilder.formatter.rendering.component.cover.layout;

import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageRenderableArea;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record CoverLayoutDiagnostic(
        SinglePageRenderableArea renderableArea,
        int contentLineCount,
        int availableGapLines,
        Map<String, Integer> blockLineCounts,
        Map<String, Integer> gapLineCounts,
        BigDecimal exactLineHeightPt
) {

    public CoverLayoutDiagnostic {
        Objects.requireNonNull(renderableArea, "renderableArea must not be null");
        Objects.requireNonNull(blockLineCounts, "blockLineCounts must not be null");
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

        blockLineCounts = copyLineCounts(blockLineCounts, "blockLineCounts");
        gapLineCounts = copyLineCounts(gapLineCounts, "gapLineCounts");

        int blockLineSum = blockLineCounts.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        int gapLineSum = gapLineCounts.values().stream()
                .mapToInt(Integer::intValue)
                .sum();

        if (blockLineSum != contentLineCount) {
            throw new IllegalArgumentException("blockLineCounts must sum to contentLineCount.");
        }

        if (gapLineSum != availableGapLines) {
            throw new IllegalArgumentException("gapLineCounts must sum to availableGapLines.");
        }

        if (contentLineCount + availableGapLines != renderableArea.safeLineCapacity()) {
            throw new IllegalArgumentException(
                    "contentLineCount plus availableGapLines must match safeLineCapacity."
            );
        }
    }

    private static Map<String, Integer> copyLineCounts(Map<String, Integer> lineCounts, String fieldName) {
        Map<String, Integer> copy = new LinkedHashMap<>();

        for (Map.Entry<String, Integer> entry : lineCounts.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();

            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException(fieldName + " keys must not be blank.");
            }

            Objects.requireNonNull(value, fieldName + " values must not be null.");

            if (value < 0) {
                throw new IllegalArgumentException(fieldName + " values must not be negative.");
            }

            copy.put(key, value);
        }

        return Collections.unmodifiableMap(copy);
    }
}
