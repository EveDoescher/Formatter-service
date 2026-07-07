package com.abntbuilder.formatter.engine.model.content.bodycontent;

import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.CitationFormattingRule;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BodyCitationCallTest {

    private static final CitationFormattingRule FORMATTING =
            new CitationFormattingRule("p. ", "; ", "et al.", " apud ", "[...]", "grifo nosso", "grifo do autor", "informação verbal", ", ", ", ", "(", ")");

    private static final CitationSource INDIRECT_SOURCE = new CitationSource(
            List.of(CitationAuthor.person("Sobrenome")),
            "2020",
            Optional.empty()
    );

    private static final CitationSource DIRECT_SOURCE = new CitationSource(
            List.of(CitationAuthor.person("Sobrenome")),
            "2020",
            Optional.of("42")
    );

    @Test
    void shouldRenderParentheticalIndirectCitation() {
        BodyCitationCall call = new BodyCitationCall(
                BodyCitationType.INDIRECT,
                BodyCitationMode.PARENTHETICAL,
                FORMATTING,
                Optional.of(INDIRECT_SOURCE),
                Optional.empty(),
                Optional.empty()
        );
        assertThat(call.renderedText()).isEqualTo("(Sobrenome, 2020)");
    }

    @Test
    void shouldRenderNarrativeIndirectCitation() {
        BodyCitationCall call = new BodyCitationCall(
                BodyCitationType.INDIRECT,
                BodyCitationMode.NARRATIVE,
                FORMATTING,
                Optional.of(INDIRECT_SOURCE),
                Optional.empty(),
                Optional.empty()
        );
        assertThat(call.renderedText()).isEqualTo("Sobrenome (2020)");
    }

    @Test
    void shouldRenderDirectShortWithPage() {
        BodyCitationCall call = new BodyCitationCall(
                BodyCitationType.DIRECT_SHORT,
                BodyCitationMode.PARENTHETICAL,
                FORMATTING,
                Optional.of(DIRECT_SOURCE),
                Optional.empty(),
                Optional.empty()
        );
        assertThat(call.renderedText()).isEqualTo("(Sobrenome, 2020, p. 42)");
    }

    @Test
    void shouldRenderCitationOfCitation() {
        CitationSource original = new CitationSource(
                List.of(CitationAuthor.person("Original")),
                "1990",
                Optional.empty()
        );
        CitationSource consulted = new CitationSource(
                List.of(CitationAuthor.person("Consultado")),
                "2020",
                Optional.of("10")
        );
        BodyCitationCall call = new BodyCitationCall(
                BodyCitationType.CITATION_OF_CITATION,
                BodyCitationMode.PARENTHETICAL,
                FORMATTING,
                Optional.empty(),
                Optional.of(original),
                Optional.of(consulted)
        );
        assertThat(call.renderedText()).isEqualTo("(Original, 1990 apud Consultado, 2020, p. 10)");
    }

    @Test
    void shouldRenderVerbalCitationParentheticalFromProfile() {
        CitationSource source = new CitationSource(
                List.of(CitationAuthor.person("LIMA")),
                "2023",
                Optional.empty()
        );
        BodyCitationCall call = new BodyCitationCall(
                BodyCitationType.VERBAL,
                BodyCitationMode.PARENTHETICAL,
                FORMATTING,
                Optional.of(source),
                Optional.empty(),
                Optional.empty()
        );
        assertThat(call.renderedText()).isEqualTo("(LIMA, informação verbal)");
    }

    @Test
    void shouldRenderVerbalCitationNarrativeFromProfile() {
        CitationSource source = new CitationSource(
                List.of(CitationAuthor.person("LIMA")),
                "2023",
                Optional.empty()
        );
        BodyCitationCall call = new BodyCitationCall(
                BodyCitationType.VERBAL,
                BodyCitationMode.NARRATIVE,
                FORMATTING,
                Optional.of(source),
                Optional.empty(),
                Optional.empty()
        );
        assertThat(call.renderedText()).isEqualTo("LIMA (informação verbal)");
    }

    @Test
    void shouldRenderNoteBibliographyPlaceholder() {
        BodyCitationCall call = new BodyCitationCall(
                BodyCitationType.NOTE_BIBLIOGRAPHY,
                BodyCitationMode.PARENTHETICAL,
                FORMATTING,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(),
                Optional.of("refId-1"),
                false, false
        );
        assertThat(call.renderedText()).isEqualTo("[note?]");
    }

    @Test
    void shouldRejectNoteBibliographyWithoutNoteReferenceId() {
        assertThatThrownBy(() -> new BodyCitationCall(
                BodyCitationType.NOTE_BIBLIOGRAPHY,
                BodyCitationMode.PARENTHETICAL,
                FORMATTING,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(),
                Optional.empty(),
                false, false
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("noteReferenceId");
    }

    @Test
    void shouldRejectDirectShortWithoutPage() {
        CitationSource noPage = new CitationSource(
                List.of(CitationAuthor.person("Sobrenome")),
                "2020",
                Optional.empty()
        );
        assertThatThrownBy(() -> new BodyCitationCall(
                BodyCitationType.DIRECT_SHORT,
                BodyCitationMode.PARENTHETICAL,
                FORMATTING,
                Optional.of(noPage),
                Optional.empty(),
                Optional.empty()
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
