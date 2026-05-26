package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import jakarta.validation.Valid;

import java.util.List;

public record ComponentRulesRequest(
        @Valid CoverComponentRuleRequest cover
) {
    public List<ComponentRule> toDomain() {
        if (cover == null) {
            return List.of();
        }

        return List.of(cover.toDomain());
    }
}