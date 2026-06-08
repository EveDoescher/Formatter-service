package com.abntbuilder.formatter.rendering.component.cover.layout;

import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutDiagnostic;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageRenderableArea;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record CoverLayoutDiagnostic(
        SinglePageRenderableArea renderableArea,
        int contentLineCount,
        int availableGapLines,
        Map<String, Integer> blockLineCounts,
        Map<String, Integer> itemLineCounts,
        Map<String, Integer> gapLineCounts,
        BigDecimal exactLineHeightPt
) {

    public CoverLayoutDiagnostic(
            SinglePageRenderableArea renderableArea,
            int contentLineCount,
            int availableGapLines,
            Map<String, Integer> blockLineCounts,
            Map<String, Integer> gapLineCounts,
            BigDecimal exactLineHeightPt
    ) {
        this(
                renderableArea,
                contentLineCount,
                availableGapLines,
                blockLineCounts,
                blockLineCounts,
                gapLineCounts,
                exactLineHeightPt
        );
    }

    public CoverLayoutDiagnostic {
        Objects.requireNonNull(renderableArea, "renderableArea must not be null");
        Objects.requireNonNull(blockLineCounts, "blockLineCounts must not be null");
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

        blockLineCounts = Map.copyOf(new LinkedHashMap<>(blockLineCounts));
        itemLineCounts = Map.copyOf(new LinkedHashMap<>(itemLineCounts));
        gapLineCounts = Map.copyOf(new LinkedHashMap<>(gapLineCounts));

        if (sum(blockLineCounts) != contentLineCount) {
            throw new IllegalArgumentException("blockLineCounts must sum to contentLineCount.");
        }

        if (sum(itemLineCounts) != contentLineCount) {
            throw new IllegalArgumentException("itemLineCounts must sum to contentLineCount.");
        }

        if (sum(gapLineCounts) != availableGapLines) {
            throw new IllegalArgumentException("gapLineCounts must sum to availableGapLines.");
        }
    }

    public static CoverLayoutDiagnostic from(SinglePageLayoutDiagnostic diagnostic) {
        return new CoverLayoutDiagnostic(
                diagnostic.renderableArea(),
                diagnostic.contentLineCount(),
                diagnostic.availableGapLines(),
                diagnostic.groupLineCounts(),
                diagnostic.itemLineCounts(),
                diagnostic.gapLineCounts(),
                diagnostic.exactLineHeightPt()
        );
    }

    public Map<String, Integer> groupLineCounts() {
        return blockLineCounts;
    }

    private static int sum(Map<String, Integer> lineCounts) {
        return lineCounts.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
    }
}
