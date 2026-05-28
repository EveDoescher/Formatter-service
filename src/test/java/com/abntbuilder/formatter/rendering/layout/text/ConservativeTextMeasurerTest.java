package com.abntbuilder.formatter.rendering.layout.text;

import com.abntbuilder.formatter.profile.model.PageOrientation;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.StyleType;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ConservativeTextMeasurerTest {

    private final ConservativeTextMeasurer measurer = new ConservativeTextMeasurer();

    @Test
    void shouldKeepShortTextInSingleLine() {
        MeasuredText measuredText = measurer.measure("Titulo curto", validPageRule(), regularStyle());

        assertEquals(1, measuredText.lineCount());
    }

    @Test
    void shouldBreakLongTextByAvailableWidth() {
        MeasuredText measuredText = measurer.measure(
                "Este titulo academico possui texto suficiente para exigir quebra conservadora",
                validPageRule(),
                regularStyle()
        );

        assertTrue(measuredText.lineCount() > 1);
    }

    @Test
    void shouldRespectExplicitLineBreaks() {
        MeasuredText measuredText = measurer.measure("Linha um\nLinha dois", validPageRule(), regularStyle());

        assertEquals(2, measuredText.lineCount());
    }

    @Test
    void shouldApplyUppercaseBeforeMeasuring() {
        MeasuredText measuredText = measurer.measure("titulo curto", validPageRule(), uppercaseStyle());

        assertEquals("TITULO CURTO", measuredText.visualLines().getFirst());
    }

    @Test
    void shouldUseMoreConservativeWidthForBoldUppercase() {
        String text = "Titulo academico com quantidade suficiente de palavras para comparar";

        MeasuredText regular = measurer.measure(text, validPageRule(), regularStyle());
        MeasuredText boldUppercase = measurer.measure(text, validPageRule(), boldUppercaseStyle());

        assertTrue(boldUppercase.lineCount() >= regular.lineCount());
    }

    @Test
    void shouldRejectWordThatDoesNotFit() {
        assertThrows(
                IllegalArgumentException.class,
                () -> measurer.measure(
                        "PALAVRAEXTREMAMENTELONGAQUEULTRAPASSAALARGURADISPONIVEL",
                        narrowPageRule(),
                        boldUppercaseStyle()
                )
        );
    }

    @Test
    void shouldRejectBlankText() {
        assertThrows(
                IllegalArgumentException.class,
                () -> measurer.measure(" ", validPageRule(), regularStyle())
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

    private static PageRule narrowPageRule() {
        return new PageRule(
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(29.7),
                BigDecimal.valueOf(3),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(3),
                PageOrientation.PORTRAIT
        );
    }

    private static StyleRule regularStyle() {
        return style(false, false);
    }

    private static StyleRule uppercaseStyle() {
        return style(false, true);
    }

    private static StyleRule boldUppercaseStyle() {
        return style(true, true);
    }

    private static StyleRule style(boolean bold, boolean uppercase) {
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
                bold,
                false,
                uppercase
        );
    }
}
