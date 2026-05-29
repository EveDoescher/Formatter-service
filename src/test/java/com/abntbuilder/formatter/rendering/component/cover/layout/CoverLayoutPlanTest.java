package com.abntbuilder.formatter.rendering.component.cover.layout;

import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.StyleType;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutDiagnostic;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutPlan;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageRenderableArea;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageSpacerLines;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageTextLines;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CoverLayoutPlanTest {

    @Test
    void shouldWrapImmutableValidatedSinglePagePlan() {
        CoverLayoutPlan plan = new CoverLayoutPlan(singlePagePlan());

        assertEquals(5, plan.totalLines());
        assertEquals(5, plan.pageCapacityLines());
        assertThrows(
                UnsupportedOperationException.class,
                () -> plan.elements().add(new SinglePageSpacerLines(
                        "cover.extra",
                        "cover.titleBlock",
                        "cover.bottom",
                        1,
                        style()
                ))
        );
    }

    @Test
    void shouldExposeCoverDiagnosticFromSinglePagePlan() {
        CoverLayoutDiagnostic diagnostic = new CoverLayoutPlan(singlePagePlan()).diagnostic();

        assertEquals(2, diagnostic.blockLineCounts().get("cover.titleBlock"));
        assertEquals(3, diagnostic.gapLineCounts().get("cover.titleBlock->cover.bottom"));
    }

    private static SinglePageLayoutPlan singlePagePlan() {
        return new SinglePageLayoutPlan(
                List.of(
                        new SinglePageTextLines(
                                "cover.titleBlock",
                                "title",
                                style(),
                                List.of("Titulo", "Subtitulo")
                        ),
                        new SinglePageSpacerLines(
                                "cover.titleBlock->cover.bottom",
                                "cover.titleBlock",
                                "cover.bottom",
                                3,
                                style()
                        )
                ),
                5,
                5,
                BigDecimal.valueOf(18),
                new SinglePageLayoutDiagnostic(
                        new SinglePageRenderableArea(7, 2, 5),
                        2,
                        3,
                        lineCounts("cover.titleBlock", 2),
                        lineCounts("cover.titleBlock.title", 2),
                        lineCounts("cover.titleBlock->cover.bottom", 3),
                        BigDecimal.valueOf(18)
                )
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
