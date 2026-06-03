package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public record ProfileRequest(
        @NotBlank String id,
        @NotBlank String displayName,

        @Valid
        @NotNull
        PageRuleRequest pageRule,

        @Valid
        @NotEmpty
        List<StyleRuleRequest> styleRules,

        @Valid
        ComponentRulesRequest componentRules,

        List<String> componentOrder
) {
    public DocumentProfile toDomain() {
        List<ComponentRule> resolvedComponentRules = componentRules == null
                ? List.of()
                : componentRules.toDomain();

        return new DocumentProfile(
                id,
                displayName,
                pageRule.toDomain(),
                styleRules.stream()
                        .map(StyleRuleRequest::toDomain)
                        .toList(),
                resolvedComponentRules,
                componentOrder == null ? defaultComponentOrder(resolvedComponentRules) : componentOrder
        );
    }

    private static List<String> defaultComponentOrder(List<ComponentRule> componentRules) {
        List<String> order = new ArrayList<>(componentRules.stream()
                .map(ComponentRule::componentId)
                .toList());

        order.add("paragraphs");

        return List.copyOf(order);
    }
}
