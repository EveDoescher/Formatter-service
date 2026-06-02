package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.profile.model.PageOrientation;
import com.abntbuilder.formatter.profile.model.PageRule;
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
                new HorizontalPlacementRule(HorizontalPlacementStrategy.FULL_CONTENT_WIDTH)
        );

        assertEquals(0, BigDecimal.valueOf(16).compareTo(area.availableWidthCm()));
        assertEquals(0, BigDecimal.ZERO.compareTo(area.leftIndentCm()));
        assertEquals(0, BigDecimal.ZERO.compareTo(area.rightIndentCm()));
    }

    @Test
    void shouldResolveFromPageCenterToRightMarginUsingUsableAreaCenter() {
        TextMeasurementArea area = resolver.resolve(
                validPageRule(),
                new HorizontalPlacementRule(HorizontalPlacementStrategy.FROM_PAGE_CENTER_TO_RIGHT_MARGIN)
        );

        assertEquals(0, BigDecimal.valueOf(8).setScale(4).compareTo(area.availableWidthCm()));
        assertEquals(0, BigDecimal.valueOf(8).setScale(4).compareTo(area.leftIndentCm()));
        assertEquals(0, BigDecimal.ZERO.compareTo(area.rightIndentCm()));
    }

    @Test
    void shouldRejectNullRule() {
        assertThrows(NullPointerException.class, () -> resolver.resolve(validPageRule(), null));
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
}
