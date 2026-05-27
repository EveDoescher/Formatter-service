package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.profile.model.PageOrientation;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.StyleType;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SinglePageTextLineBreakerTest {

    private final SinglePageTextLineBreaker lineBreaker = new SinglePageTextLineBreaker();

    @Test
    void shouldRespectConservativePageWidthWhenConfiguredLimitIsTooLarge() {
        List<String> lines = lineBreaker.breakText(
                "ESTE TITULO ACADEMICO POSSUI TEXTO SUFICIENTE PARA EXIGIR QUEBRA CONSERVADORA",
                200,
                validPageRule(),
                coverTitleStyle()
        );

        assertTrue(lines.size() > 1);
        assertTrue(lines.stream().allMatch(line -> line.length() <= 55));
    }

    @Test
    void shouldRejectWordThatCannotFitConservativeLineWidth() {
        assertThrows(
                IllegalArgumentException.class,
                () -> lineBreaker.breakText(
                        "PALAVRAEXTREMAMENTELONGAQUEULTRAPASSAALARGURACONSERVADORADALINHA",
                        200,
                        validPageRule(),
                        coverTitleStyle()
                )
        );
    }

    private static PageRule validPageRule() {
        return new PageRule(
                BigDecimal.valueOf(21),
                BigDecimal.valueOf(29.7),
                BigDecimal.valueOf(3),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(3),
                PageOrientation.PORTRAIT
        );
    }

    private static StyleRule coverTitleStyle() {
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
