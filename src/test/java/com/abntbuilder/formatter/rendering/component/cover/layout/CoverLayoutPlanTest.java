package com.abntbuilder.formatter.rendering.component.cover.layout;

import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.StyleType;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageRenderableArea;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CoverLayoutPlanTest {

    @Test
    void shouldCreateImmutableValidatedPlan() {
        CoverLayoutPlan plan = new CoverLayoutPlan(
                List.of(
                        new CoverTextLines("cover.title", style(), List.of("Titulo", "Subtitulo")),
                        new CoverSpacerLines("cover.title-to-cover.bottom", 3, style())
                ),
                5,
                5,
                BigDecimal.valueOf(18),
                diagnostic()
        );

        assertEquals(5, plan.totalLines());
        assertEquals(5, plan.pageCapacityLines());
        assertThrows(
                UnsupportedOperationException.class,
                () -> plan.elements().add(new CoverSpacerLines("cover.extra", 1, style()))
        );
    }

    @Test
    void shouldRejectPlanWhenElementLineCountDoesNotMatchTotalLines() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new CoverLayoutPlan(
                        List.of(new CoverTextLines("cover.title", style(), List.of("Titulo"))),
                        2,
                        5,
                        BigDecimal.valueOf(18),
                        diagnostic()
                )
        );

        assertEquals("totalLines must match the sum of element line counts.", exception.getMessage());
    }

    @Test
    void shouldRejectPlanWhenDiagnosticCapacityDoesNotMatchPageCapacity() {
        CoverLayoutDiagnostic diagnostic = new CoverLayoutDiagnostic(
                new SinglePageRenderableArea(8, 2, 6),
                2,
                4,
                lineCounts("cover.title", 2),
                lineCounts("cover.title-to-cover.bottom", 4),
                BigDecimal.valueOf(18)
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new CoverLayoutPlan(
                        List.of(
                                new CoverTextLines("cover.title", style(), List.of("Titulo", "Subtitulo")),
                                new CoverSpacerLines("cover.title-to-cover.bottom", 3, style())
                        ),
                        5,
                        5,
                        BigDecimal.valueOf(18),
                        diagnostic
                )
        );

        assertEquals("diagnostic safeLineCapacity must match pageCapacityLines.", exception.getMessage());
    }

    private static CoverLayoutDiagnostic diagnostic() {
        return new CoverLayoutDiagnostic(
                new SinglePageRenderableArea(7, 2, 5),
                2,
                3,
                lineCounts("cover.title", 2),
                lineCounts("cover.title-to-cover.bottom", 3),
                BigDecimal.valueOf(18)
        );
    }

    private static Map<String, Integer> lineCounts(String key, int value) {
        Map<String, Integer> lineCounts = new LinkedHashMap<>();
        lineCounts.put(key, value);
        return lineCounts;
    }

    private static StyleRule style() {
        return new StyleRule(
                "cover.title",
                StyleType.PARAGRAPH,
                "Times New Roman",
                BigDecimal.valueOf(12),
                TextAlignment.CENTER,
                BigDecimal.valueOf(1.5),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                true,
                false,
                true
        );
    }
}
