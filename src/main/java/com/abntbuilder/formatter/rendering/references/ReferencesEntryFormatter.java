package com.abntbuilder.formatter.rendering.references;

import com.abntbuilder.formatter.engine.model.content.references.ReferenceAuthor;
import com.abntbuilder.formatter.engine.model.content.references.ReferenceEntry;
import com.abntbuilder.formatter.engine.model.profile.component.references.AuthorFormatRule;
import com.abntbuilder.formatter.engine.model.profile.component.references.EntrySegmentRule;
import com.abntbuilder.formatter.engine.model.profile.component.references.ReferencesFormattingRule;

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
                    "No entryFormat declared for reference type: \"" + entry.type() + "\"");
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
            result.add(new ReferenceSegment(text, segRule.bold(), segRule.italic()));
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
            case "doi"     -> e.doi().map(d -> "https://doi.org/" + d);
            case "doi:raw" -> e.doi();
            case "degree" -> e.degree();
            case "institutionName" -> e.institutionName();
            default -> {
                if (source.startsWith("literal:")) yield Optional.of(source.substring("literal:".length()));
                String custom = e.customFields().get(source);
                if (custom != null) yield Optional.of(custom);
                throw new IllegalArgumentException("Unknown segment source: " + source);
            }
        };
    }

    private String renderAuthors(List<ReferenceAuthor> authors, AuthorFormatRule fmt) {
        if (authors.size() > fmt.etAlThreshold()) {
            return renderName(authors.get(0), fmt) + " " + fmt.etAlLabel() + " ";
        }
        if (authors.size() == 1 || fmt.lastAuthorJoiner().isEmpty()) {
            return authors.stream()
                    .map(a -> renderName(a, fmt))
                    .collect(Collectors.joining(fmt.multiAuthorJoiner())) + " ";
        }
        List<String> rendered = authors.stream().map(a -> renderName(a, fmt)).toList();
        String allButLast = String.join(fmt.multiAuthorJoiner(), rendered.subList(0, rendered.size() - 1));
        return allButLast + fmt.lastAuthorJoiner().get() + rendered.get(rendered.size() - 1) + " ";
    }

    private String renderName(ReferenceAuthor author, AuthorFormatRule fmt) {
        String surname = fmt.surnameUppercase()
                ? author.surname().toUpperCase() : author.surname();
        String given = author.givenNames()
                .map(g -> fmt.initialsOnly() ? toInitials(g, fmt) : g)
                .orElse(null);
        if (given == null) return surname + fmt.nameTerminator();
        return switch (fmt.nameOrder()) {
            case SURNAME_FIRST -> surname + fmt.surnameGivenSeparator() + given + fmt.nameTerminator();
            case GIVEN_FIRST   -> given + " " + surname + fmt.nameTerminator();
        };
    }

    private static String toInitials(String givenNames, AuthorFormatRule fmt) {
        String[] parts = givenNames.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) continue;
            char initial = parts[i].charAt(0);
            if (i > 0 && fmt.initialsSpaced()) sb.append(" ");
            sb.append(initial);
            if (fmt.initialsDotted()) sb.append(".");
        }
        return sb.toString();
    }
}
