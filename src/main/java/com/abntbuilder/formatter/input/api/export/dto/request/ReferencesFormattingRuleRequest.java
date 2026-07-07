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
        @NotNull Map<String, List<@Valid EntrySegmentRuleRequest>> entryFormats
) {
    public ReferencesFormattingRule toDomain() {
        Map<String, List<EntrySegmentRule>> domainFormats = new HashMap<>();
        for (Map.Entry<String, List<EntrySegmentRuleRequest>> e : entryFormats.entrySet()) {
            List<EntrySegmentRule> segments = e.getValue().stream()
                    .map(EntrySegmentRuleRequest::toDomain)
                    .toList();
            domainFormats.put(e.getKey(), segments);
        }
        return new ReferencesFormattingRule(authorFormat.toDomain(), domainFormats);
    }
}
