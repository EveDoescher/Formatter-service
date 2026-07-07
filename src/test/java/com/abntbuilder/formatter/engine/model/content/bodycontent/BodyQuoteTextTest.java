package com.abntbuilder.formatter.engine.model.content.bodycontent;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BodyQuoteTextTest {

    @Test
    void shouldRenderShortQuoteWithBoundaryQuotes() {
        BodyQuoteText quote = new BodyQuoteText(BodyQuoteType.SHORT, "texto da citação");
        assertThat(quote.renderedText()).isEqualTo("\"texto da citação\"");
    }

    @Test
    void shouldApplySuppressionMarker() {
        BodyQuoteText quote = new BodyQuoteText(
                BodyQuoteType.SHORT,
                "texto longo aqui",
                InlineFormatting.none(),
                List.of(BodyQuoteMarker.suppression(5))
        );
        assertThat(quote.renderedText()).isEqualTo("\"texto[...] longo aqui\"");
    }

    @Test
    void shouldApplyInterpolationMarker() {
        BodyQuoteText quote = new BodyQuoteText(
                BodyQuoteType.SHORT,
                "texto aqui",
                InlineFormatting.none(),
                List.of(new BodyQuoteMarker(BodyQuoteMarkerType.INTERPOLATION, 6, java.util.Optional.of(10)))
        );
        assertThat(quote.renderedText()).isEqualTo("\"texto [aqui]\"");
    }

    @Test
    void shouldNotModifyTextForEmphasisMarkers() {
        BodyQuoteText quoteOurs = new BodyQuoteText(
                BodyQuoteType.SHORT,
                "texto",
                InlineFormatting.none(),
                List.of(BodyQuoteMarker.emphasisOurs())
        );
        BodyQuoteText quoteAuthor = new BodyQuoteText(
                BodyQuoteType.SHORT,
                "texto",
                InlineFormatting.none(),
                List.of(BodyQuoteMarker.emphasisAuthor())
        );
        assertThat(quoteOurs.renderedText()).isEqualTo("\"texto\"");
        assertThat(quoteAuthor.renderedText()).isEqualTo("\"texto\"");
    }

    @Test
    void shouldRejectBlankText() {
        assertThatThrownBy(() -> new BodyQuoteText(BodyQuoteType.SHORT, "  "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
