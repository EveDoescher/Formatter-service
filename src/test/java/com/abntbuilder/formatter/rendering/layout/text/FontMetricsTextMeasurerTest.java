package com.abntbuilder.formatter.rendering.layout.text;

import com.abntbuilder.formatter.profile.model.PageOrientation;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.StyleType;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.shared.exception.TextMeasurementException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FontMetricsTextMeasurerTest {

    private final FontMetricsTextMeasurer measurer = new FontMetricsTextMeasurer();

    @Test
    void shouldKeepShortTextInSingleLine() {
        MeasuredText measuredText = measurer.measure("Titulo curto", validPageRule(), regularStyle());

        assertEquals(1, measuredText.lineCount());
    }

    @Test
    void shouldBreakLongTextByMeasuredFontWidth() {
        MeasuredText measuredText = measurer.measure(
                "Este titulo academico possui texto suficiente para exigir quebra baseada em metrica de fonte",
                narrowPageRule(),
                regularStyle()
        );

        assertTrue(measuredText.lineCount() > 1);
    }

    @Test
    void shouldRespectExplicitLineBreaks() {
        MeasuredText measuredText = measurer.measure("Linha um\nLinha dois", validPageRule(), regularStyle());

        assertEquals(2, measuredText.lineCount());
        assertEquals("Linha um", measuredText.visualLines().get(0));
        assertEquals("Linha dois", measuredText.visualLines().get(1));
    }

    @Test
    void shouldApplyUppercaseBeforeMeasuring() {
        MeasuredText measuredText = measurer.measure("titulo curto", validPageRule(), uppercaseStyle());

        assertEquals("TITULO CURTO", measuredText.visualLines().getFirst());
    }

    @Test
    void shouldRejectWordThatDoesNotFit() {
        assertThrows(
                TextMeasurementException.class,
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
                TextMeasurementException.class,
                () -> measurer.measure(" ", validPageRule(), regularStyle())
        );
    }

    @Test
    void shouldRejectUnavailableTextWidth() {
        assertThrows(
                TextMeasurementException.class,
                () -> measurer.measure("Texto", invalidWidthByIndentPageRule(), indentedStyle())
        );
    }

    @Test
    void shouldRejectUnavailableFontFamilyWhenPolicyFails() {
        TextMeasurementException exception = assertThrows(
                TextMeasurementException.class,
                () -> new FontMetricsTextMeasurer(MissingFontPolicy.FAIL).measure(
                        "Texto",
                        validPageRule(),
                        styleWithFontFamily("Fonte Inexistente Formatter Teste")
                )
        );

        assertEquals(
                "font family is not available: Fonte Inexistente Formatter Teste.",
                exception.getMessage()
        );
    }

    @Test
    void shouldAllowUnavailableFontFamilyWhenFallbackPolicyAllowsIt() {
        MeasuredText measuredText = new FontMetricsTextMeasurer(MissingFontPolicy.ALLOW_FALLBACK).measure(
                "Texto",
                validPageRule(),
                styleWithFontFamily("Fonte Inexistente Formatter Teste")
        );

        assertEquals(1, measuredText.lineCount());
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

    private static PageRule invalidWidthByIndentPageRule() {
        return new PageRule(
                BigDecimal.valueOf(12),
                BigDecimal.valueOf(29.7),
                BigDecimal.valueOf(3),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(3),
                PageOrientation.PORTRAIT
        );
    }

    private static StyleRule regularStyle() {
        return style(false, false, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private static StyleRule uppercaseStyle() {
        return style(false, true, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private static StyleRule boldUppercaseStyle() {
        return style(true, true, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private static StyleRule indentedStyle() {
        return style(false, false, BigDecimal.valueOf(4), BigDecimal.valueOf(4));
    }

    private static StyleRule style(
            boolean bold,
            boolean uppercase,
            BigDecimal leftIndentCm,
            BigDecimal rightIndentCm
    ) {
        return style(
                bold,
                uppercase,
                leftIndentCm,
                rightIndentCm,
                "Times New Roman"
        );
    }

    private static StyleRule styleWithFontFamily(String fontFamily) {
        return style(false, false, BigDecimal.ZERO, BigDecimal.ZERO, fontFamily);
    }

    private static StyleRule style(
            boolean bold,
            boolean uppercase,
            BigDecimal leftIndentCm,
            BigDecimal rightIndentCm,
            String fontFamily
    ) {
        return new StyleRule(
                "cover.title",
                StyleType.PARAGRAPH,
                fontFamily,
                BigDecimal.valueOf(12),
                TextAlignment.CENTER,
                BigDecimal.valueOf(1.5),
                BigDecimal.ZERO,
                leftIndentCm,
                rightIndentCm,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                bold,
                false,
                uppercase
        );
    }
}
