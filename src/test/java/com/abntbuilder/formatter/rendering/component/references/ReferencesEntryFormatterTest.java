package com.abntbuilder.formatter.rendering.component.references;

import com.abntbuilder.formatter.document.component.references.ReferenceAuthor;
import com.abntbuilder.formatter.document.component.references.ReferenceEntry;
import com.abntbuilder.formatter.document.component.references.ReferenceType;
import com.abntbuilder.formatter.profile.model.component.references.ReferencesFormattingRule;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ReferencesEntryFormatterTest {

    private final ReferencesFormattingRule abntRule = new ReferencesFormattingRule(
            "Disponível em: ", "Acesso em: ", "et al.", "In: ",
            ", ", ".", "; ", true
    );
    private final ReferencesEntryFormatter formatter = new ReferencesEntryFormatter(abntRule);

    @Test
    void shouldFormatBookWithOneAuthor() {
        ReferenceEntry entry = new ReferenceEntry(
                "ref1", ReferenceType.BOOK,
                List.of(new ReferenceAuthor("Lima", Optional.of("Carlos Eduardo"))),
                "Fundamentos de Sistemas Distribuídos", Optional.empty(), Optional.empty(),
                Optional.of("São Paulo"), Optional.of("Editora Exemplo"),
                "2021", Optional.empty(), Optional.empty(), Optional.empty()
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
                "ref2", ReferenceType.BOOK,
                List.of(
                        new ReferenceAuthor("Rocha", Optional.of("Beatriz")),
                        new ReferenceAuthor("Souza", Optional.of("Ana")),
                        new ReferenceAuthor("Lima", Optional.of("Carlos"))
                ),
                "Arquiteturas de Software", Optional.empty(), Optional.empty(),
                Optional.of("Limeira"), Optional.of("Editora Fictícia"),
                "2022", Optional.empty(), Optional.empty(), Optional.empty()
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
                "ref3", ReferenceType.BOOK,
                List.of(
                        new ReferenceAuthor("Ferreira", Optional.of("João")),
                        new ReferenceAuthor("Alves", Optional.of("Maria")),
                        new ReferenceAuthor("Costa", Optional.of("Pedro")),
                        new ReferenceAuthor("Nunes", Optional.of("Laura"))
                ),
                "Computação em Nuvem", Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), "2020",
                Optional.empty(), Optional.empty(), Optional.empty()
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
                "ref4", ReferenceType.WEBSITE,
                List.of(new ReferenceAuthor("Souza", Optional.of("Ana Paula"))),
                "Guia prático de microsserviços", Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), "2024",
                Optional.empty(),
                Optional.of("https://exemplo.ficticio.br/guia"),
                Optional.of("10 maio 2024")
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
                "ref5", ReferenceType.JOURNAL,
                List.of(new ReferenceAuthor("Rocha", Optional.of("Beatriz"))),
                "Análise de desempenho em ambientes distribuídos", Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.of("Revista Fictícia de Computação, v. 15, n. 2"),
                "2023", Optional.of("123-145"), Optional.empty(), Optional.empty()
        );

        List<ReferenceSegment> segments = formatter.format(entry);
        String full = segments.stream().map(ReferenceSegment::text).reduce("", String::concat);

        assertThat(full).contains("Revista Fictícia de Computação");
        assertThat(full).contains("123-145");
        assertThat(full).contains("2023");
    }
}
