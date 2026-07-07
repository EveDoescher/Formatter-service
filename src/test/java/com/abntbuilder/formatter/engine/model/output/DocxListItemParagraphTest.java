package com.abntbuilder.formatter.engine.model.output;

import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyListType;
import com.abntbuilder.formatter.engine.model.profile.StyleRule;
import com.abntbuilder.formatter.engine.model.profile.StyleType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.abntbuilder.formatter.engine.model.content.bodycontent.InlineFormatting;
import com.abntbuilder.formatter.engine.model.profile.TextAlignment;
import java.math.BigDecimal;

class DocxListItemParagraphTest {

    private final StyleRule style = new StyleRule(
            "id", StyleType.PARAGRAPH, "Arial", BigDecimal.valueOf(12),
            TextAlignment.LEFT, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
            BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, false, false, false
    );
    private final DocxRun run = new DocxRun("text", style, InlineFormatting.none());

    @Test
    void shouldCreateValidInstance() {
        DocxListItemParagraph p = new DocxListItemParagraph(List.of(run), style, BodyListType.ORDERED, 1);
        assertThat(p.runs()).hasSize(1);
        assertThat(p.styleRule()).isEqualTo(style);
        assertThat(p.listType()).isEqualTo(BodyListType.ORDERED);
        assertThat(p.listLevel()).isEqualTo(1);
    }

    @Test
    void shouldRejectNullRuns() {
        assertThatThrownBy(() -> new DocxListItemParagraph(null, style, BodyListType.ORDERED, 1))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectEmptyRuns() {
        assertThatThrownBy(() -> new DocxListItemParagraph(List.of(), style, BodyListType.ORDERED, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNullStyle() {
        assertThatThrownBy(() -> new DocxListItemParagraph(List.of(run), null, BodyListType.ORDERED, 1))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNullListType() {
        assertThatThrownBy(() -> new DocxListItemParagraph(List.of(run), style, null, 1))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNegativeListLevel() {
        assertThatThrownBy(() -> new DocxListItemParagraph(List.of(run), style, BodyListType.ORDERED, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
