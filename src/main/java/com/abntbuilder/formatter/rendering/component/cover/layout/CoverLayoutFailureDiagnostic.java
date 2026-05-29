package com.abntbuilder.formatter.rendering.component.cover.layout;

import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutFailureDiagnostic;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageRenderableArea;

import java.math.BigDecimal;
import java.util.Map;

public record CoverLayoutFailureDiagnostic(
        SinglePageRenderableArea renderableArea,
        int contentLineCount,
        int overflowLineCount,
        Map<String, Integer> blockLineCounts,
        Map<String, Integer> itemLineCounts,
        BigDecimal exactLineHeightPt
) {

    public CoverLayoutFailureDiagnostic(
            SinglePageRenderableArea renderableArea,
            int contentLineCount,
            int overflowLineCount,
            Map<String, Integer> blockLineCounts,
            BigDecimal exactLineHeightPt
    ) {
        this(
                renderableArea,
                contentLineCount,
                overflowLineCount,
                blockLineCounts,
                blockLineCounts,
                exactLineHeightPt
        );
    }

    public CoverLayoutFailureDiagnostic {
        SinglePageLayoutFailureDiagnostic diagnostic = new SinglePageLayoutFailureDiagnostic(
                renderableArea,
                contentLineCount,
                overflowLineCount,
                blockLineCounts,
                itemLineCounts,
                exactLineHeightPt
        );

        renderableArea = diagnostic.renderableArea();
        contentLineCount = diagnostic.contentLineCount();
        overflowLineCount = diagnostic.overflowLineCount();
        blockLineCounts = diagnostic.groupLineCounts();
        itemLineCounts = diagnostic.itemLineCounts();
        exactLineHeightPt = diagnostic.exactLineHeightPt();
    }

    public static CoverLayoutFailureDiagnostic from(SinglePageLayoutFailureDiagnostic diagnostic) {
        return new CoverLayoutFailureDiagnostic(
                diagnostic.renderableArea(),
                diagnostic.contentLineCount(),
                diagnostic.overflowLineCount(),
                diagnostic.groupLineCounts(),
                diagnostic.itemLineCounts(),
                diagnostic.exactLineHeightPt()
        );
    }

    public Map<String, Integer> groupLineCounts() {
        return blockLineCounts;
    }
}
