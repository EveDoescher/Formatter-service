package com.abntbuilder.formatter.document.component.bodycontent;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BodyCitationTest {

    @Test
    void shouldRenderParentheticalIndirectCitationWithFinalPeriodAfterCitation() {
        BodyCitation citation = new BodyCitation(
                BodyCitationType.INDIRECT,
                BodyCitationMode.PARENTHETICAL,
                "A organização documental integra a padronização acadêmica.",
                Optional.of(source("SOBRENOME TESTE UM", "2020", null)),
                Optional.empty(),
                Optional.empty()
        );

        assertEquals(
                "A organização documental integra a padronização acadêmica (Sobrenome Teste Um, 2020).",
                citation.renderedText()
        );
    }

    @Test
    void shouldRenderNarrativeDirectShortCitation() {
        BodyCitation citation = new BodyCitation(
                BodyCitationType.DIRECT_SHORT,
                BodyCitationMode.NARRATIVE,
                "a organização documental depende de critérios formais",
                Optional.of(source("SOBRENOME TESTE UM", "2020", "10")),
                Optional.empty(),
                Optional.empty()
        );

        assertEquals(
                "Sobrenome Teste Um (2020, p. 10), \"a organização documental depende de critérios formais\".",
                citation.renderedText()
        );
    }

    @Test
    void shouldRejectManualBoundaryQuotationMarksForShortQuoteText() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BodyQuoteText(BodyQuoteType.SHORT, "\"texto citado manualmente\"")
        );

        assertEquals(
                "manual boundary quotation marks must not be provided for SHORT quote text.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectManualBoundaryQuotationMarksForDirectShortCitationBlock() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BodyCitation(
                        BodyCitationType.DIRECT_SHORT,
                        BodyCitationMode.PARENTHETICAL,
                        "\"Texto citado manualmente\"",
                        Optional.of(source("SOBRENOME TESTE UM", "2020", "10")),
                        Optional.empty(),
                        Optional.empty()
                )
        );

        assertEquals(
                "manual boundary quotation marks must not be provided for SHORT quote text.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRequirePageForDirectCitation() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BodyCitation(
                        BodyCitationType.DIRECT_SHORT,
                        BodyCitationMode.PARENTHETICAL,
                        "\"Texto citado\"",
                        Optional.of(source("SOBRENOME TESTE UM", "2020", null)),
                        Optional.empty(),
                        Optional.empty()
                )
        );

        assertEquals("DIRECT_SHORT citation source page must be provided.", exception.getMessage());
    }

    @Test
    void shouldRenderCitationOfCitationWithConsultedSourcePage() {
        BodyCitation citation = new BodyCitation(
                BodyCitationType.CITATION_OF_CITATION,
                BodyCitationMode.PARENTHETICAL,
                "A padronização contribui para a recuperação das informações",
                Optional.empty(),
                Optional.of(source("SOBRENOME TESTE TRES", "1998", null)),
                Optional.of(source("SOBRENOME TESTE DOIS", "2020", "35"))
        );

        assertEquals(
                "A padronização contribui para a recuperação das informações (Sobrenome Teste Tres, 1998 apud Sobrenome Teste Dois, 2020, p. 35).",
                citation.renderedText()
        );
    }

    @Test
    void shouldFormatThreeOrMoreAuthorsAsEtAl() {
        CitationSource source = new CitationSource(
                List.of(
                        CitationAuthor.person("Sobrenome Teste Um"),
                        CitationAuthor.person("Sobrenome Teste Dois"),
                        CitationAuthor.person("Sobrenome Teste Tres")
                ),
                "2020",
                Optional.empty()
        );

        assertEquals("Sobrenome Teste Um et al., 2020", source.parentheticalText());
    }

    @Test
    void shouldFormatOrganizationAuthorUsingDisplayNameWhenProvided() {
        CitationSource source = new CitationSource(
                List.of(new CitationAuthor(
                        CitationAuthorType.ORGANIZATION,
                        Optional.empty(),
                        Optional.of("Instituicao Teste de Pesquisa"),
                        Optional.of("ITP"),
                        Optional.empty()
                )),
                "2020",
                Optional.empty()
        );

        assertEquals("ITP, 2020", source.parentheticalText());
    }

    @Test
    void shouldFormatTitleAuthoringWhenWorkHasNoAuthor() {
        CitationSource source = new CitationSource(
                List.of(new CitationAuthor(
                        CitationAuthorType.TITLE,
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.of("Manual Teste de Normalizacao")
                )),
                "2020",
                Optional.empty()
        );

        assertEquals("Manual Teste de Normalizacao, 2020", source.parentheticalText());
    }

    private static CitationSource source(String author, String year, String page) {
        return new CitationSource(
                List.of(CitationAuthor.person(toDisplayName(author))),
                year,
                page == null ? Optional.empty() : Optional.of(page)
        );
    }

    private static String toDisplayName(String author) {
        return switch (author) {
            case "SOBRENOME TESTE UM" -> "Sobrenome Teste Um";
            case "SOBRENOME TESTE DOIS" -> "Sobrenome Teste Dois";
            case "SOBRENOME TESTE TRES" -> "Sobrenome Teste Tres";
            default -> author;
        };
    }
}
