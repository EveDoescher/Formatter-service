package com.abntbuilder.formatter.rendering.component.cover.layout;

import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutDiagnostic;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageRenderableArea;

import java.math.BigDecimal;
import java.util.Map;

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
        SinglePageLayoutDiagnostic diagnostic = new SinglePageLayoutDiagnostic(
                renderableArea,
                contentLineCount,
                availableGapLines,
                blockLineCounts,
                itemLineCounts,
                gapLineCounts,
                exactLineHeightPt
        );

        renderableArea = diagnostic.renderableArea();
        contentLineCount = diagnostic.contentLineCount();
        availableGapLines = diagnostic.availableGapLines();
        blockLineCounts = diagnostic.groupLineCounts();
        itemLineCounts = diagnostic.itemLineCounts();
        gapLineCounts = diagnostic.gapLineCounts();
        exactLineHeightPt = diagnostic.exactLineHeightPt();
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
}
