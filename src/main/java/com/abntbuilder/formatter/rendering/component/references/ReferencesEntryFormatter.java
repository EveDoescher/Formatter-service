package com.abntbuilder.formatter.rendering.component.references;

import com.abntbuilder.formatter.document.component.references.ReferenceAuthor;
import com.abntbuilder.formatter.document.component.references.ReferenceEntry;
import com.abntbuilder.formatter.profile.model.component.references.ReferencesFormattingRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ReferencesEntryFormatter {

    private final ReferencesFormattingRule rule;

    public ReferencesEntryFormatter(ReferencesFormattingRule rule) {
        this.rule = Objects.requireNonNull(rule, "rule must not be null");
    }

    public List<ReferenceSegment> format(ReferenceEntry entry) {
        return switch (entry.type()) {
            case BOOK -> formatBook(entry);
            case BOOK_CHAPTER -> formatBookChapter(entry);
            case JOURNAL -> formatJournal(entry);
            case WEBSITE -> formatWebsite(entry);
            case LEGISLATION -> formatLegislation(entry);
            case THESIS -> formatThesis(entry);
        };
    }

    private List<ReferenceSegment> formatBook(ReferenceEntry e) {
        List<ReferenceSegment> segments = new ArrayList<>();
        segments.add(new ReferenceSegment(renderAuthors(e.authors()), false));
        segments.addAll(renderTitle(e.title(), e.subtitle()));
        e.edition().ifPresent(ed -> segments.add(new ReferenceSegment(" " + ed + " ed.", false)));
        segments.add(new ReferenceSegment(". ", false));
        e.city().ifPresent(c -> segments.add(new ReferenceSegment(c + ": ", false)));
        e.publisher().ifPresent(p -> segments.add(new ReferenceSegment(p + ", ", false)));
        segments.add(new ReferenceSegment(e.year() + ".", false));
        return List.copyOf(segments);
    }

    private List<ReferenceSegment> formatBookChapter(ReferenceEntry e) {
        // AUTOR DO CAPÍTULO. Título do capítulo. In: AUTOR DO LIVRO (url field). Título do livro (subtitle field). ed. Cidade: Editora, Ano. p. páginas.
        List<ReferenceSegment> segments = new ArrayList<>();
        segments.add(new ReferenceSegment(renderAuthors(e.authors()), false));
        segments.addAll(renderTitle(e.title(), Optional.empty()));
        segments.add(new ReferenceSegment(" " + rule.inLabel(), false));
        e.url().ifPresent(bookAuthor -> segments.add(new ReferenceSegment(bookAuthor.toUpperCase() + ". ", false)));
        e.subtitle().ifPresent(bookTitle -> {
            segments.add(new ReferenceSegment(bookTitle, true));
            segments.add(new ReferenceSegment(". ", false));
        });
        e.edition().ifPresent(ed -> segments.add(new ReferenceSegment(ed + " ed. ", false)));
        e.city().ifPresent(c -> segments.add(new ReferenceSegment(c + ": ", false)));
        e.publisher().ifPresent(p -> segments.add(new ReferenceSegment(p + ", ", false)));
        segments.add(new ReferenceSegment(e.year() + ".", false));
        e.pages().ifPresent(p -> segments.add(new ReferenceSegment(" p. " + p + ".", false)));
        return List.copyOf(segments);
    }

    private List<ReferenceSegment> formatJournal(ReferenceEntry e) {
        List<ReferenceSegment> segments = new ArrayList<>();
        segments.add(new ReferenceSegment(renderAuthors(e.authors()), false));
        segments.addAll(renderTitle(e.title(), e.subtitle()));
        e.publisher().ifPresent(journal -> segments.add(new ReferenceSegment(" " + journal, false)));
        e.pages().ifPresent(p -> segments.add(new ReferenceSegment(", p. " + p, false)));
        segments.add(new ReferenceSegment(", " + e.year() + ".", false));
        return List.copyOf(segments);
    }

    private List<ReferenceSegment> formatWebsite(ReferenceEntry e) {
        List<ReferenceSegment> segments = new ArrayList<>();
        segments.add(new ReferenceSegment(renderAuthors(e.authors()), false));
        segments.addAll(renderTitle(e.title(), e.subtitle()));
        e.url().ifPresent(u -> segments.add(new ReferenceSegment(" " + rule.availableAtLabel() + u + ".", false)));
        e.accessDate().ifPresent(d -> segments.add(new ReferenceSegment(" " + rule.accessedAtLabel() + d + ".", false)));
        return List.copyOf(segments);
    }

    private List<ReferenceSegment> formatLegislation(ReferenceEntry e) {
        List<ReferenceSegment> segments = new ArrayList<>();
        segments.add(new ReferenceSegment(e.title(), false));
        e.subtitle().ifPresent(s -> segments.add(new ReferenceSegment(". " + s, false)));
        segments.add(new ReferenceSegment(". " + e.year() + ".", false));
        e.url().ifPresent(u -> segments.add(new ReferenceSegment(" " + rule.availableAtLabel() + u + ".", false)));
        return List.copyOf(segments);
    }

    private List<ReferenceSegment> formatThesis(ReferenceEntry e) {
        List<ReferenceSegment> segments = new ArrayList<>();
        segments.add(new ReferenceSegment(renderAuthors(e.authors()), false));
        segments.addAll(renderTitle(e.title(), e.subtitle()));
        segments.add(new ReferenceSegment(". " + e.year() + ".", false));
        return List.copyOf(segments);
    }

    private String renderAuthors(List<ReferenceAuthor> authors) {
        if (authors.isEmpty()) return "";
        if (authors.size() > 3) {
            return formatAuthor(authors.get(0)) + " " + rule.etAlLabel() + " ";
        }
        return authors.stream()
                .map(this::formatAuthor)
                .collect(Collectors.joining(rule.multiAuthorJoiner())) + " ";
    }

    private String formatAuthor(ReferenceAuthor author) {
        String surname = rule.authorSurnameUppercase()
                ? author.surname().toUpperCase() : author.surname();
        return author.givenNames()
                .map(given -> surname + rule.authorSurnameGivenSeparator() + given + rule.authorNameTerminator())
                .orElse(surname + rule.authorNameTerminator());
    }

    private List<ReferenceSegment> renderTitle(String title, Optional<String> subtitle) {
        List<ReferenceSegment> segments = new ArrayList<>();
        segments.add(new ReferenceSegment(title, true));
        subtitle.ifPresent(s -> segments.add(new ReferenceSegment(": " + s, false)));
        return segments;
    }
}
