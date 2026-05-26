package com.abntbuilder.formatter.profile.resolution;

import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import com.abntbuilder.formatter.shared.exception.ComponentRuleTypeMismatchException;
import com.abntbuilder.formatter.shared.exception.MissingComponentRuleException;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ComponentRuleResolver {

    private final Map<String, ComponentRule> rulesByComponentId;

    public ComponentRuleResolver(DocumentProfile profile) {
        Objects.requireNonNull(profile, "profile must not be null");

        this.rulesByComponentId = profile.componentRules()
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                        ComponentRule::componentId,
                        Function.identity()
                ));
    }

    public ComponentRule resolve(String componentId) {
        requireNonBlank(componentId, "componentId");

        ComponentRule rule = rulesByComponentId.get(componentId);

        if (rule == null) {
            throw new MissingComponentRuleException(componentId);
        }

        return rule;
    }

    public <T extends ComponentRule> T resolve(String componentId, Class<T> expectedType) {
        Objects.requireNonNull(expectedType, "expectedType must not be null");

        ComponentRule rule = resolve(componentId);

        if (!expectedType.isInstance(rule)) {
            throw new ComponentRuleTypeMismatchException(componentId, expectedType, rule);
        }

        return expectedType.cast(rule);
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}