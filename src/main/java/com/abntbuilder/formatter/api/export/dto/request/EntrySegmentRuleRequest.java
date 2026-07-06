package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.references.EntrySegmentRule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EntrySegmentRuleRequest(
        @NotBlank String source,
        boolean bold,
        @NotNull String prefix,
        @NotNull String suffix,
        boolean optional
) {
    public EntrySegmentRule toDomain() {
        return new EntrySegmentRule(source, bold, prefix, suffix, optional);
    }
}
