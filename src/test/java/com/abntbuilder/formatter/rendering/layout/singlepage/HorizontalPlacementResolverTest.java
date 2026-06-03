package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.profile.model.PageOrientation;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.StyleType;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.profile.model.layout.singlepage.HorizontalPlacementRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.HorizontalPlacementStrategy;
import com.abntbuilder.formatter.rendering.layout.text.TextMeasurementArea;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HorizontalPlacementResolverTest {

    private final HorizontalPlacementResolver resolver = new HorizontalPlacementResolver();

    @Test
    void shouldResolveFullContentWidth() {
        TextMeasurementArea area = resolver.resolve(
                validPageRule(),
                style(BigDecimal.ONE, BigDecimal.valueOf(0.5)),
                new HorizontalPlacementRule(HorizontalPlacementStrategy.FULL_CONTENT_WIDTH)
        );

        assertEquals(0, BigDecimal.valueOf(14.5).compareTo(area.availableWidthCm()));
        assertEquals(0, BigDecimal.ONE.compareTo(area.leftIndentCm()));
        assertEquals(0, BigDecimal.valueOf(0.5).compareTo(area.rightIndentCm()));
    }

    @Test
    void shouldResolveFromPageCenterToRightMarginUsingUsableAreaCenter() {
        TextMeasurementArea area = resolver.resolve(
                validPageRule(),
                style(BigDecimal.valueOf(0.75), BigDecimal.valueOf(0.25)),
                new HorizontalPlacementRule(HorizontalPlacementStrategy.FROM_PAGE_CENTER_TO_RIGHT_MARGIN)
        );

        assertEquals(0, BigDecimal.valueOf(7).setScale(4).compareTo(area.availableWidthCm()));
        assertEquals(0, BigDecimal.valueOf(8.75).setScale(4).compareTo(area.leftIndentCm()));
        assertEquals(0, BigDecimal.valueOf(0.25).compareTo(area.rightIndentCm()));
    }

    @Test
    void shouldRejectNullRule() {
        assertThrows(NullPointerException.class, () -> resolver.resolve(validPageRule(), validStyle(), null));
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

    private static StyleRule validStyle() {
        return style(BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private static StyleRule style(BigDecimal leftIndentCm, BigDecimal rightIndentCm) {
        return new StyleRule(
                "body",
                StyleType.PARAGRAPH,
                "Times New Roman",
                BigDecimal.valueOf(12),
                TextAlignment.LEFT,
                BigDecimal.valueOf(1.5),
                BigDecimal.ZERO,
                leftIndentCm,
                rightIndentCm,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                false,
                false
        );
    }
}
