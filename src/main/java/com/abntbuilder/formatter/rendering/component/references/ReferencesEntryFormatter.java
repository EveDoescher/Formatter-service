package com.abntbuilder.formatter.rendering.component.references;

import com.abntbuilder.formatter.document.component.references.ReferenceAuthor;
import com.abntbuilder.formatter.document.component.references.ReferenceEntry;
import com.abntbuilder.formatter.profile.model.component.references.AuthorFormatRule;
import com.abntbuilder.formatter.profile.model.component.references.EntrySegmentRule;
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
        List<EntrySegmentRule> segmentRules = rule.entryFormats().get(entry.type());
        if (segmentRules == null) {
            throw new IllegalArgumentException(
                    "No entryFormat declared for reference type: " + entry.type());
        }

        List<ReferenceSegment> result = new ArrayList<>();
        for (EntrySegmentRule segRule : segmentRules) {
            Optional<String> value = resolveSource(segRule.source(), entry);
            if (value.isEmpty()) {
                if (!segRule.optional()) {
                    throw new IllegalArgumentException(
                            "Required field '" + segRule.source() + "' is missing in entry: " + entry.id());
                }
                continue;
            }
            String text = segRule.prefix() + value.get() + segRule.suffix();
            result.add(new ReferenceSegment(text, segRule.bold()));
        }
        return List.copyOf(result);
    }

    private Optional<String> resolveSource(String source, ReferenceEntry e) {
        return switch (source) {
            case "authors" -> {
                if (e.authors().isEmpty()) yield Optional.empty();
                yield Optional.of(renderAuthors(e.authors(), rule.authorFormat()));
            }
            case "bookAuthors" -> e.bookAuthors()
                    .filter(list -> !list.isEmpty())
                    .map(list -> renderAuthors(list, rule.authorFormat()).stripTrailing() + " (org.). ");
            case "title" -> Optional.of(e.title());
            case "subtitle" -> e.subtitle();
            case "bookTitle" -> e.bookTitle();
            case "edition" -> e.edition();
            case "city" -> e.city();
            case "publisher" -> e.publisher();
            case "year" -> Optional.of(e.year());
            case "pages" -> e.pages();
            case "url" -> e.url();
            case "accessDate" -> e.accessDate();
            case "volume" -> e.volume();
            case "issue" -> e.issue();
            case "doi" -> e.doi().map(d -> "https://doi.org/" + d);
            case "degree" -> e.degree();
            case "institutionName" -> e.institutionName();
            default -> {
                if (source.startsWith("literal:")) yield Optional.of(source.substring("literal:".length()));
                throw new IllegalArgumentException("Unknown segment source: " + source);
            }
        };
    }

    private String renderAuthors(List<ReferenceAuthor> authors, AuthorFormatRule fmt) {
        if (authors.size() > fmt.etAlThreshold()) {
            return formatAuthor(authors.get(0), fmt) + " " + fmt.etAlLabel() + " ";
        }
        return authors.stream()
                .map(a -> formatAuthor(a, fmt))
                .collect(Collectors.joining(fmt.multiAuthorJoiner())) + " ";
    }

    private String formatAuthor(ReferenceAuthor author, AuthorFormatRule fmt) {
        String surname = fmt.surnameUppercase()
                ? author.surname().toUpperCase() : author.surname();
        return author.givenNames()
                .map(given -> surname + fmt.surnameGivenSeparator() + given + fmt.nameTerminator())
                .orElse(surname + fmt.nameTerminator());
    }
}
