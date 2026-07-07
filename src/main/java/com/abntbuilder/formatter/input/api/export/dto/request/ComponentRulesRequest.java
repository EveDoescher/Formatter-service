package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.profile.component.ComponentRule;
import com.abntbuilder.formatter.input.profile.ProfileDefinition;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ComponentRulesRequest {

    private final Map<String, ProfileDefinition.ComponentRuleDefinition> rules = new LinkedHashMap<>();

    @JsonAnySetter
    public void addRule(String key, ProfileDefinition.ComponentRuleDefinition value) {
        if (value != null) rules.put(key, value);
    }

    public List<ComponentRule> toDomain() {
        return rules.values().stream()
                .map(ProfileDefinition.ComponentRuleDefinition::toDomain)
                .collect(java.util.stream.Collectors.toList());
    }
}
