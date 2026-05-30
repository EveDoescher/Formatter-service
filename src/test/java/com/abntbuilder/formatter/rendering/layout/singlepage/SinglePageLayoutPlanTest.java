package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.StyleType;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SinglePageLayoutPlanTest {

    @Test
    void shouldRejectPlanWhenDiagnosticLineBudgetDoesNotMatchTotalLines() {
        StyleRule style = style();
        BigDecimal exactLineHeightPt = BigDecimal.valueOf(18);
        SinglePageLayoutDiagnostic diagnostic = new SinglePageLayoutDiagnostic(
                new SinglePageRenderableArea(5, 0, 5),
                3,
                2,
                Map.of("cover.title", 2, "cover.bottom", 1),
                Map.of("cover.title.title", 2, "cover.bottom.year", 1),
                Map.of("cover.title->cover.bottom", 2),
                exactLineHeightPt
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new SinglePageLayoutPlan(
                List.of(
                        new SinglePageTextLines("cover.title", "title", style, List.of("Titulo", "Linha dois")),
                        new SinglePageSpacerLines("cover.title->cover.bottom", "cover.title", "cover.bottom", 1, style),
                        new SinglePageTextLines("cover.bottom", "year", style, List.of("2026"))
                ),
                4,
                5,
                exactLineHeightPt,
                diagnostic
        ));

        assertEquals("diagnostic contentLineCount plus availableGapLines must match totalLines.", exception.getMessage());
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
                false,
                false,
                false
        );
    }
}
