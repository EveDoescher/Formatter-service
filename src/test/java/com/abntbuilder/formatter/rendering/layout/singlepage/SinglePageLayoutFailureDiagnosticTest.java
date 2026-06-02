package com.abntbuilder.formatter.rendering.layout.singlepage;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SinglePageLayoutFailureDiagnosticTest {

    @Test
    void shouldAllowOverflowLineCountToBeHeightBasedApproximation() {
        SinglePageLayoutFailureDiagnostic diagnostic = new SinglePageLayoutFailureDiagnostic(
                new SinglePageRenderableArea(34, 2, 32, 1200, 0, 1200),
                40,
                1,
                Map.of("group", 40),
                Map.of("group.item", 40),
                1320,
                120,
                Map.of("group", 1320),
                Map.of("group.item", 1320),
                BigDecimal.valueOf(18)
        );

        assertEquals(1, diagnostic.overflowLineCount());
        assertEquals(120, diagnostic.overflowHeightTwips());
    }
}
