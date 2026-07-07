package com.abntbuilder.formatter.rendering.references;

import com.abntbuilder.formatter.engine.model.content.references.ReferenceAuthor;
import com.abntbuilder.formatter.engine.model.content.references.ReferenceEntry;
import com.abntbuilder.formatter.engine.model.profile.component.references.AuthorFormatRule;
import com.abntbuilder.formatter.engine.model.profile.component.references.EntrySegmentRule;
import com.abntbuilder.formatter.engine.model.profile.component.references.ReferencesFormattingRule;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReferencesEntryFormatterTest {

    private static final AuthorFormatRule ABNT_AUTHOR = new AuthorFormatRule(
            true, ", ", ".", "; ", "et al.", 3
    );

    private static final List<EntrySegmentRule> BOOK_FORMAT = List.of(
            new EntrySegmentRule("authors",   false, "",    "",    false),
            new EntrySegmentRule("title",     true,  "",    "",    false),
            new EntrySegmentRule("subtitle",  false, ": ",  "",    true),
            new EntrySegmentRule("edition",   false, ". ",  ". ed.", true),
            new EntrySegmentRule("city",      false, ". ", ": ",   true),
            new EntrySegmentRule("publisher", false, "",    ", ",  true),
            new EntrySegmentRule("year",      false, "",    ".",   false)
    );

    private static final List<EntrySegmentRule> WEBSITE_FORMAT = List.of(
            new EntrySegmentRule("authors",    false, "",    "",    true),
            new EntrySegmentRule("title",      true,  "",    "",    false),
            new EntrySegmentRule("subtitle",   false, ": ",  "",    true),
            new EntrySegmentRule("url",        false, " Disponível em: ", ".", true),
            new EntrySegmentRule("accessDate", false, " Acesso em: ",     ".", true)
    );

    private static final List<EntrySegmentRule> JOURNAL_FORMAT = List.of(
            new EntrySegmentRule("authors",   false, "",      "",    false),
            new EntrySegmentRule("title",     true,  "",      "",    false),
            new EntrySegmentRule("subtitle",  false, ": ",    "",    true),
            new EntrySegmentRule("publisher", false, " ",     "",    true),
            new EntrySegmentRule("volume",    false, ", v. ", "",    true),
            new EntrySegmentRule("issue",     false, ", n. ", "",    true),
            new EntrySegmentRule("pages",     false, ", p. ", "",    true),
            new EntrySegmentRule("year",      false, ", ",    ".",   false),
            new EntrySegmentRule("doi",       false, " ",     ".",   true)
    );

    private static final List<EntrySegmentRule> BOOK_CHAPTER_FORMAT = List.of(
            new EntrySegmentRule("authors",         false, "",      "",    false),
            new EntrySegmentRule("title",           true,  "",      "",    false),
            new EntrySegmentRule("literal:In: ",    false, "",      "",    false),
            new EntrySegmentRule("bookAuthors",     false, "",      "",    true),
            new EntrySegmentRule("bookTitle",       true,  "",      ". ",  true),
            new EntrySegmentRule("edition",         false, "",      ". ed. ", true),
            new EntrySegmentRule("city",            false, "",      ": ",  true),
            new EntrySegmentRule("publisher",       false, "",      ", ",  true),
            new EntrySegmentRule("year",            false, "",      ".",   false),
            new EntrySegmentRule("pages",           false, " p. ", ".",   true)
    );

    private static final ReferencesFormattingRule ABNT_RULE = new ReferencesFormattingRule(
            ABNT_AUTHOR,
            Map.of(
                    "BOOK", BOOK_FORMAT,
                    "WEBSITE", WEBSITE_FORMAT,
                    "JOURNAL", JOURNAL_FORMAT,
                    "BOOK_CHAPTER", BOOK_CHAPTER_FORMAT
            )
    );

    private final ReferencesEntryFormatter formatter = new ReferencesEntryFormatter(ABNT_RULE);

    @Test
    void shouldFormatBookWithOneAuthor() {
        ReferenceEntry entry = new ReferenceEntry(
                "ref1", "BOOK",
                List.of(new ReferenceAuthor("Lima", Optional.of("Carlos Eduardo"))),
                "Fundamentos de Sistemas Distribuídos", Optional.empty(), Optional.empty(),
                Optional.of("São Paulo"), Optional.of("Editora Exemplo"),
                "2021", Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Map.of()
        );

        List<ReferenceSegment> segments = formatter.format(entry);
        String full = segments.stream().map(ReferenceSegment::text).reduce("", String::concat);

        assertThat(full).contains("LIMA, Carlos Eduardo.");
        assertThat(segments.stream().filter(ReferenceSegment::bold).findFirst())
                .map(ReferenceSegment::text)
                .hasValue("Fundamentos de Sistemas Distribuídos");
        assertThat(full).contains("São Paulo");
        assertThat(full).contains("2021");
    }

    @Test
    void shouldFormatBookWithThreeAuthors() {
        ReferenceEntry entry = new ReferenceEntry(
                "ref2", "BOOK",
                List.of(
                        new ReferenceAuthor("Rocha", Optional.of("Beatriz")),
                        new ReferenceAuthor("Souza", Optional.of("Ana")),
                        new ReferenceAuthor("Lima", Optional.of("Carlos"))
                ),
                "Arquiteturas de Software", Optional.empty(), Optional.empty(),
                Optional.of("Limeira"), Optional.of("Editora Fictícia"),
                "2022", Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Map.of()
        );

        List<ReferenceSegment> segments = formatter.format(entry);
        String full = segments.stream().map(ReferenceSegment::text).reduce("", String::concat);

        assertThat(full).contains("ROCHA, Beatriz.");
        assertThat(full).contains("SOUZA, Ana.");
        assertThat(full).contains("LIMA, Carlos.");
    }

    @Test
    void shouldFormatBookWithMoreThanThreeAuthors() {
        ReferenceEntry entry = new ReferenceEntry(
                "ref3", "BOOK",
                List.of(
                        new ReferenceAuthor("Ferreira", Optional.of("João")),
                        new ReferenceAuthor("Alves", Optional.of("Maria")),
                        new ReferenceAuthor("Costa", Optional.of("Pedro")),
                        new ReferenceAuthor("Nunes", Optional.of("Laura"))
                ),
                "Computação em Nuvem", Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), "2020",
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Map.of()
        );

        List<ReferenceSegment> segments = formatter.format(entry);
        String full = segments.stream().map(ReferenceSegment::text).reduce("", String::concat);

        assertThat(full).contains("FERREIRA, João.");
        assertThat(full).contains("et al.");
        assertThat(full).doesNotContain("ALVES");
    }

    @Test
    void shouldFormatWebsiteWithAccessDate() {
        ReferenceEntry entry = new ReferenceEntry(
                "ref4", "WEBSITE",
                List.of(new ReferenceAuthor("Souza", Optional.of("Ana Paula"))),
                "Guia prático de microsserviços", Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), "2024",
                Optional.empty(),
                Optional.of("https://exemplo.ficticio.br/guia"),
                Optional.of("10 maio 2024"),
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Map.of()
        );

        List<ReferenceSegment> segments = formatter.format(entry);
        String full = segments.stream().map(ReferenceSegment::text).reduce("", String::concat);

        assertThat(full).contains("Disponível em: ");
        assertThat(full).contains("Acesso em: ");
        assertThat(full).contains("10 maio 2024");
    }

    @Test
    void shouldFormatJournalArticle() {
        ReferenceEntry entry = new ReferenceEntry(
                "ref5", "JOURNAL",
                List.of(new ReferenceAuthor("Rocha", Optional.of("Beatriz"))),
                "Análise de desempenho em ambientes distribuídos", Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.of("Revista Fictícia de Computação"),
                "2023", Optional.of("123-145"), Optional.empty(), Optional.empty(),
                Optional.of("15"), Optional.of("2"), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Map.of()
        );

        List<ReferenceSegment> segments = formatter.format(entry);
        String full = segments.stream().map(ReferenceSegment::text).reduce("", String::concat);

        assertThat(full).contains("Revista Fictícia de Computação");
        assertThat(full).contains("123-145");
        assertThat(full).contains("2023");
    }

    @Test
    void shouldOmitOptionalSegmentWhenFieldAbsent() {
        ReferenceEntry entry = new ReferenceEntry(
                "ref6", "BOOK",
                List.of(new ReferenceAuthor("Lima", Optional.of("Carlos"))),
                "Título Sem Subtítulo", Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), "2020",
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Map.of()
        );

        List<ReferenceSegment> segments = formatter.format(entry);
        String full = segments.stream().map(ReferenceSegment::text).reduce("", String::concat);

        assertThat(full).doesNotContain(": ");
        assertThat(full).contains("LIMA, Carlos.");
        assertThat(full).contains("2020.");
    }

    @Test
    void shouldThrowWhenRequiredFieldMissing() {
        List<EntrySegmentRule> strictFormat = List.of(
                new EntrySegmentRule("publisher", false, "", "", false)
        );
        ReferencesFormattingRule strictRule = new ReferencesFormattingRule(
                ABNT_AUTHOR, Map.of("BOOK", strictFormat)
        );
        ReferencesEntryFormatter strictFormatter = new ReferencesEntryFormatter(strictRule);

        ReferenceEntry entry = new ReferenceEntry(
                "ref7", "BOOK",
                List.of(), "Título", Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), "2020",
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Map.of()
        );

        assertThatThrownBy(() -> strictFormatter.format(entry))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("publisher");
    }

    @Test
    void shouldThrowWhenEntryTypeHasNoFormat() {
        ReferencesFormattingRule ruleWithoutThesis = new ReferencesFormattingRule(
                ABNT_AUTHOR, Map.of("BOOK", BOOK_FORMAT)
        );
        ReferencesEntryFormatter f = new ReferencesEntryFormatter(ruleWithoutThesis);

        ReferenceEntry entry = new ReferenceEntry(
                "ref8", "THESIS",
                List.of(), "Título", Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), "2020",
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Map.of()
        );

        assertThatThrownBy(() -> f.format(entry))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("THESIS");
    }

    @Test
    void shouldRespectAlternativeNormWithDifferentAuthorFormat() {
        // Norma alternativa: sobrenome minúsculo, et al. a partir de 2 autores, sem separador vírgula
        AuthorFormatRule apaLikeAuthor = new AuthorFormatRule(
                false, ", ", ".", " & ", "et al.", 1
        );
        List<EntrySegmentRule> apaBookFormat = List.of(
                new EntrySegmentRule("authors", false, "", "",   false),
                new EntrySegmentRule("year",    false, "(", "). ", false),
                new EntrySegmentRule("title",   true,  "",  ".",  false),
                new EntrySegmentRule("city",    false, " ", ":",  true),
                new EntrySegmentRule("publisher", false, " ", ".", true)
        );
        ReferencesFormattingRule apaRule = new ReferencesFormattingRule(
                apaLikeAuthor, Map.of("BOOK", apaBookFormat)
        );
        ReferencesEntryFormatter apaFormatter = new ReferencesEntryFormatter(apaRule);

        ReferenceEntry entry = new ReferenceEntry(
                "ref9", "BOOK",
                List.of(
                        new ReferenceAuthor("Rocha", Optional.of("Beatriz")),
                        new ReferenceAuthor("Lima", Optional.of("Carlos"))
                ),
                "Sistemas Distribuídos", Optional.empty(), Optional.empty(),
                Optional.of("Limeira"), Optional.of("Editora Fictícia"),
                "2022", Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Map.of()
        );

        List<ReferenceSegment> segments = apaFormatter.format(entry);
        String full = segments.stream().map(ReferenceSegment::text).reduce("", String::concat);

        // Com etAlThreshold=1 e 2 autores, usa et al. (só o primeiro autor)
        assertThat(full).contains("Rocha");
        assertThat(full).doesNotContain("Lima");
        assertThat(full).contains("et al.");
        // Sobrenome não está em maiúsculas
        assertThat(full).doesNotContain("ROCHA");
        // Ano entre parênteses
        assertThat(full).contains("(2022)");
    }
}
