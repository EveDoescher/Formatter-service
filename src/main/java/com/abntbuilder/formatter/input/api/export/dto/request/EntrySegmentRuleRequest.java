package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.profile.component.references.EntrySegmentRule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EntrySegmentRuleRequest(
        @NotBlank String source,
        boolean bold,
        boolean italic,
        @NotNull String prefix,
        @NotNull String suffix,
        boolean optional
) {
    public EntrySegmentRule toDomain() {
        return new EntrySegmentRule(source, bold, italic, prefix, suffix, optional);
    }
}
