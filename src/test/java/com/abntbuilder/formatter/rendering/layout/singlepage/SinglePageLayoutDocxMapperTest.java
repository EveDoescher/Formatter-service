package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.profile.model.PageOrientation;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.StyleType;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.shared.exception.SinglePageLayoutOverflowException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SinglePageLayoutDocxMapperTest {

    private final SinglePageLayoutDocxMapper mapper = new SinglePageLayoutDocxMapper();

    @Test
    void shouldFailWhenContentDoesNotFitRenderablePageArea() {
        SinglePageLayoutGroup group = new SinglePageLayoutGroup(
                "cover.title",
                List.of(
                        new SinglePageLayoutTextLine("Linha um", validStyle()),
                        new SinglePageLayoutTextLine("Linha dois", validStyle())
                )
        );

        assertThrows(
                SinglePageLayoutOverflowException.class,
                () -> mapper.mapToDocxBlocksAnchoringLastGroup(
                        pageWithTwoUsableLineSlots(),
                        List.of(group),
                        List.of()
                )
        );
    }

    private static PageRule pageWithTwoUsableLineSlots() {
        return new PageRule(
                BigDecimal.valueOf(21),
                BigDecimal.valueOf(5.3),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(3),
                PageOrientation.PORTRAIT
        );
    }

    private static StyleRule validStyle() {
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
