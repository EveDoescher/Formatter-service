package com.abntbuilder.formatter.rendering.component.cover.layout;

import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageRenderableArea;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CoverLayoutDiagnosticTest {

    @Test
    void shouldCreateImmutableValidatedDiagnostic() {
        CoverLayoutDiagnostic diagnostic = new CoverLayoutDiagnostic(
                new SinglePageRenderableArea(7, 2, 5),
                2,
                3,
                lineCounts("cover.title", 2),
                lineCounts("cover.title-to-cover.bottom", 3),
                BigDecimal.valueOf(18)
        );

        assertEquals(5, diagnostic.renderableArea().safeLineCapacity());
        assertEquals(2, diagnostic.contentLineCount());
        assertEquals(3, diagnostic.availableGapLines());
        assertThrows(
                UnsupportedOperationException.class,
                () -> diagnostic.blockLineCounts().put("cover.extra", 1)
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> diagnostic.gapLineCounts().put("cover.extra-gap", 1)
        );
    }

    @Test
    void shouldRejectDiagnosticWhenBlockCountsDoNotSumToContentLines() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new CoverLayoutDiagnostic(
                        new SinglePageRenderableArea(7, 2, 5),
                        2,
                        3,
                        lineCounts("cover.title", 1),
                        lineCounts("cover.title-to-cover.bottom", 3),
                        BigDecimal.valueOf(18)
                )
        );

        assertEquals("blockLineCounts must sum to contentLineCount.", exception.getMessage());
    }

    @Test
    void shouldRejectDiagnosticWhenGapCountsDoNotSumToAvailableGapLines() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new CoverLayoutDiagnostic(
                        new SinglePageRenderableArea(7, 2, 5),
                        2,
                        3,
                        lineCounts("cover.title", 2),
                        lineCounts("cover.title-to-cover.bottom", 2),
                        BigDecimal.valueOf(18)
                )
        );

        assertEquals("gapLineCounts must sum to availableGapLines.", exception.getMessage());
    }

    private static Map<String, Integer> lineCounts(String key, int value) {
        Map<String, Integer> lineCounts = new LinkedHashMap<>();
        lineCounts.put(key, value);
        return lineCounts;
    }
}
