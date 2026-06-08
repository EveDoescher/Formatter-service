package com.abntbuilder.formatter.rendering.component.cover.layout;

import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageRenderableArea;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CoverLayoutFailureDiagnosticTest {

    @Test
    void shouldCreateValidatedOverflowDiagnostic() {
        Map<String, Integer> blockLineCounts = new LinkedHashMap<>();
        blockLineCounts.put("cover.top", 1);
        blockLineCounts.put("cover.authors", 35);
        blockLineCounts.put("cover.title", 2);
        blockLineCounts.put("cover.bottom", 2);

        CoverLayoutFailureDiagnostic diagnostic = new CoverLayoutFailureDiagnostic(
                new SinglePageRenderableArea(34, 2, 32),
                40,
                8,
                blockLineCounts,
                BigDecimal.valueOf(18)
        );

        assertEquals(40, diagnostic.contentLineCount());
        assertEquals(8, diagnostic.overflowLineCount());
        assertEquals(32, diagnostic.renderableArea().safeLineCapacity());
        assertThrows(
                UnsupportedOperationException.class,
                () -> diagnostic.blockLineCounts().put("cover.extra", 1)
        );
    }

    @Test
    void shouldRejectDiagnosticWhenOverflowHeightDoesNotMatchSafeCapacity() {
        Map<String, Integer> blockLineCounts = Map.of(
                "cover.top", 1,
                "cover.authors", 35,
                "cover.title", 2,
                "cover.bottom", 2
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new CoverLayoutFailureDiagnostic(
                        new SinglePageRenderableArea(34, 2, 32),
                        40,
                        7,
                        blockLineCounts,
                        BigDecimal.valueOf(18)
                )
        );

        assertEquals(
                "overflowHeightTwips must match contentHeightTwips minus safeHeightTwips.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectDiagnosticWhenBlockCountsDoNotSumToContentLines() {
        Map<String, Integer> blockLineCounts = Map.of(
                "cover.top", 1,
                "cover.authors", 35
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new CoverLayoutFailureDiagnostic(
                        new SinglePageRenderableArea(34, 2, 32),
                        40,
                        8,
                        blockLineCounts,
                        BigDecimal.valueOf(18)
                )
        );

        assertEquals("blockLineCounts must sum to contentLineCount.", exception.getMessage());
    }
}
