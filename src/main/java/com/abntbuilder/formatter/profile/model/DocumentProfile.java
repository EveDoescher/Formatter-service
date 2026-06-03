package com.abntbuilder.formatter.profile.model;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record DocumentProfile(
        String id,
        String displayName,
        PageRule pageRule,
        List<StyleRule> styleRules,
        List<ComponentRule> componentRules,
        List<String> componentOrder
) {
    public DocumentProfile(
            String id,
            String displayName,
            PageRule pageRule,
            List<StyleRule> styleRules,
            List<ComponentRule> componentRules
    ) {
        this(
                id,
                displayName,
                pageRule,
                styleRules,
                componentRules,
                componentRules.stream()
                        .map(ComponentRule::componentId)
                        .toList()
        );
    }

    public DocumentProfile {
        requireNonBlank(id, "id");
        requireNonBlank(displayName, "displayName");
        Objects.requireNonNull(pageRule, "pageRule must not be null");
        Objects.requireNonNull(styleRules, "styleRules must not be null");
        Objects.requireNonNull(componentRules, "componentRules must not be null");
        Objects.requireNonNull(componentOrder, "componentOrder must not be null");

        if (styleRules.isEmpty()) {
            throw new IllegalArgumentException("styleRules must not be empty.");
        }

        styleRules = List.copyOf(styleRules);
        componentRules = List.copyOf(componentRules);
        componentOrder = List.copyOf(componentOrder);

        validateStyleRules(styleRules);
        validateComponentRules(componentRules);
        validateComponentOrder(componentOrder);
    }

    private static void validateStyleRules(List<StyleRule> styleRules) {
        Set<String> styleIds = new HashSet<>();

        for (StyleRule styleRule : styleRules) {
            Objects.requireNonNull(styleRule, "styleRules must not contain null values.");

            if (!styleIds.add(styleRule.id())) {
                throw new IllegalArgumentException("Duplicate style rule id: " + styleRule.id());
            }
        }
    }

    private static void validateComponentRules(List<ComponentRule> componentRules) {
        Set<String> componentIds = new HashSet<>();

        for (ComponentRule componentRule : componentRules) {
            Objects.requireNonNull(componentRule, "componentRules must not contain null values.");

            if (!componentIds.add(componentRule.componentId())) {
                throw new IllegalArgumentException("Duplicate component rule id: " + componentRule.componentId());
            }
        }
    }

    private static void validateComponentOrder(List<String> componentOrder) {
        Set<String> componentIds = new HashSet<>();

        for (String componentId : componentOrder) {
            requireNonBlank(componentId, "componentOrder item");

            if (!componentIds.add(componentId)) {
                throw new IllegalArgumentException("Duplicate component order id: " + componentId);
            }
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
