package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.profile.component.references.EntrySegmentRule;
import com.abntbuilder.formatter.engine.model.profile.component.references.ReferencesFormattingRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ReferencesFormattingRuleRequest(
        @NotNull @Valid AuthorFormatRuleRequest authorFormat,
        @NotNull Map<String, List<@Valid EntrySegmentRuleRequest>> entryFormats,
        Map<String, List<@Valid EntrySegmentRuleRequest>> noteFormats,
        Map<String, List<@Valid EntrySegmentRuleRequest>> shortNoteFormats,
        Boolean ibidEnabled
) {
    public ReferencesFormattingRule toDomain() {
        return new ReferencesFormattingRule(
                authorFormat.toDomain(),
                toSegmentMap(entryFormats),
                noteFormats != null ? toSegmentMap(noteFormats) : Map.of(),
                shortNoteFormats != null ? toSegmentMap(shortNoteFormats) : Map.of(),
                ibidEnabled != null && ibidEnabled
        );
    }

    private static Map<String, List<EntrySegmentRule>> toSegmentMap(
            Map<String, List<EntrySegmentRuleRequest>> source
    ) {
        Map<String, List<EntrySegmentRule>> result = new HashMap<>();
        for (Map.Entry<String, List<EntrySegmentRuleRequest>> e : source.entrySet()) {
            result.put(e.getKey(), e.getValue().stream().map(EntrySegmentRuleRequest::toDomain).toList());
        }
        return result;
    }
}
