package com.abntbuilder.formatter.engine.model.content.bodycontent;

import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.CitationFormattingRule;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record CitationSource(
        List<CitationAuthor> authors,
        String year,
        Optional<String> page
) {

    public CitationSource {
        Objects.requireNonNull(authors, "authors must not be null");
        requireNonBlank(year, "year");
        Objects.requireNonNull(page, "page must not be null");

        page.ifPresent(value -> requireNonBlank(value, "page"));

        if (authors.isEmpty()) {
            throw new IllegalArgumentException("authors must not be empty.");
        }

        authors = authors.stream()
                .map(author -> Objects.requireNonNull(author, "authors must not contain null values."))
                .toList();
    }

    public void requirePage(String context) {
        if (page.isEmpty()) {
            throw new IllegalArgumentException(context + " citation source page must be provided.");
        }
    }

    public String authorText(CitationFormattingRule formatting) {
        if (!authors.isEmpty()) {
            return switch (authors.size()) {
                case 1 -> authors.getFirst().renderedName();
                case 2 -> authors.get(0).renderedName() + formatting.multiAuthorJoiner() + authors.get(1).renderedName();
                default -> authors.getFirst().renderedName() + " " + formatting.etAl();
            };
        }
        throw new IllegalStateException("authors must not be empty.");
    }

    public String yearAndPageText(CitationFormattingRule formatting) {
        return page
                .map(value -> year + formatting.pageReferenceSeparator() + formatting.pagePrefix() + value)
                .orElse(year);
    }

    public String parentheticalText(CitationFormattingRule formatting) {
        return authorText(formatting) + formatting.authorYearSeparator() + yearAndPageText(formatting);
    }

    public String narrativeReferenceText(CitationFormattingRule formatting) {
        return authorText(formatting) + " " + formatting.parenOpen() + yearAndPageText(formatting) + formatting.parenClose();
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
