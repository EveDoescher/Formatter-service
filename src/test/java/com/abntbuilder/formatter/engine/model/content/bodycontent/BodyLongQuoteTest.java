package com.abntbuilder.formatter.engine.model.content.bodycontent;

import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.CitationFormattingRule;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BodyLongQuoteTest {

    private static final CitationFormattingRule FORMATTING =
            new CitationFormattingRule("p. ", "; ", "et al.", " apud ", "[...]", "grifo nosso", "grifo do autor", "informação verbal", ", ", ", ", "(", ")");

    private static final CitationSource SOURCE = new CitationSource(
            List.of(CitationAuthor.person("Sobrenome")),
            "2020",
            Optional.of("42")
    );

    @Test
    void shouldCreateWithValidData() {
        BodyLongQuote quote = new BodyLongQuote(
                "texto longo da citação",
                BodyCitationMode.PARENTHETICAL,
                Optional.of(SOURCE),
                Optional.empty(),
                Optional.empty()
        );
        assertThat(quote.text()).isEqualTo("texto longo da citação");
    }

    @Test
    void shouldRejectBlankText() {
        assertThatThrownBy(() -> new BodyLongQuote(
                "  ",
                BodyCitationMode.PARENTHETICAL,
                Optional.of(SOURCE),
                Optional.empty(),
                Optional.empty()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectMissingSource() {
        assertThatThrownBy(() -> new BodyLongQuote(
                "texto",
                BodyCitationMode.PARENTHETICAL,
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectSourceWithoutPage() {
        CitationSource noPage = new CitationSource(
                List.of(CitationAuthor.person("Sobrenome")),
                "2020",
                Optional.empty()
        );
        assertThatThrownBy(() -> new BodyLongQuote(
                "texto",
                BodyCitationMode.PARENTHETICAL,
                Optional.of(noPage),
                Optional.empty(),
                Optional.empty()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRenderParentheticalText() {
        BodyLongQuote quote = new BodyLongQuote(
                "texto da citação.",
                BodyCitationMode.PARENTHETICAL,
                Optional.of(SOURCE),
                Optional.empty(),
                Optional.empty()
        );
        String rendered = quote.renderedText(FORMATTING);
        assertThat(rendered).contains("Sobrenome").contains("2020").contains("p. 42");
    }
}
