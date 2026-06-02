package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;

public record ComponentRulesRequest(
        @Valid CoverComponentRuleRequest cover,
        @Valid TitlePageComponentRuleRequest titlePage
) {
    public List<ComponentRule> toDomain() {
        List<ComponentRule> rules = new ArrayList<>();

        if (cover != null) {
            rules.add(cover.toDomain());
        }

        if (titlePage != null) {
            rules.add(titlePage.toDomain());
        }

        return List.copyOf(rules);
    }
}
