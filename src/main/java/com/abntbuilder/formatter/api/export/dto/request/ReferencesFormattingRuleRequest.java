package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.references.ReferenceType;
import com.abntbuilder.formatter.profile.model.component.references.EntrySegmentRule;
import com.abntbuilder.formatter.profile.model.component.references.ReferencesFormattingRule;
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
        Map<ReferenceType, List<EntrySegmentRule>> domainFormats = new HashMap<>();
        for (Map.Entry<String, List<EntrySegmentRuleRequest>> e : entryFormats.entrySet()) {
            ReferenceType type = ReferenceType.valueOf(e.getKey());
            List<EntrySegmentRule> segments = e.getValue().stream()
                    .map(EntrySegmentRuleRequest::toDomain)
                    .toList();
            domainFormats.put(type, segments);
        }
        return new ReferencesFormattingRule(authorFormat.toDomain(), domainFormats);
    }
}
