package com.abntbuilder.formatter.profile.model;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverComponentRule;
import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageComponentRule;

import java.util.Collection;
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
    public static final String PARAGRAPHS_INTERNAL_COMPONENT_ID = "paragraphs";
    private static final Set<String> INTERNAL_COMPONENT_IDS = Set.of(PARAGRAPHS_INTERNAL_COMPONENT_ID);

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
        validateComponentOrder(componentRules, componentOrder);
        validateComponentStyleMappings(styleRules, componentRules);
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

    private static void validateComponentOrder(
            List<ComponentRule> componentRules,
            List<String> componentOrder
    ) {
        if (componentOrder.isEmpty()) {
            throw new IllegalArgumentException("componentOrder must not be empty.");
        }

        Set<String> declaredComponentIds = componentRules.stream()
                .map(ComponentRule::componentId)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        Set<String> orderedComponentIds = new HashSet<>();

        for (String componentId : componentOrder) {
            requireNonBlank(componentId, "componentOrder item");

            if (!orderedComponentIds.add(componentId)) {
                throw new IllegalArgumentException("Duplicate component order id: " + componentId);
            }

            if (!declaredComponentIds.contains(componentId) && !INTERNAL_COMPONENT_IDS.contains(componentId)) {
                throw new IllegalArgumentException("Unknown component order id: " + componentId);
            }
        }
    }

    private static void validateComponentStyleMappings(
            List<StyleRule> styleRules,
            List<ComponentRule> componentRules
    ) {
        Set<String> styleIds = styleRules.stream()
                .map(StyleRule::id)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());

        for (ComponentRule componentRule : componentRules) {
            for (String styleId : styleIdsFor(componentRule)) {
                if (!styleIds.contains(styleId)) {
                    throw new IllegalArgumentException(
                            "Component style mapping references unknown style id: " + styleId
                    );
                }
            }
        }
    }

    private static Collection<String> styleIdsFor(ComponentRule componentRule) {
        return switch (componentRule) {
            case CoverComponentRule coverRule -> List.of(
                    coverRule.styleMapping().institutionalLinesStyleId(),
                    coverRule.styleMapping().authorsStyleId(),
                    coverRule.styleMapping().titleStyleId(),
                    coverRule.styleMapping().subtitleStyleId(),
                    coverRule.styleMapping().cityStyleId(),
                    coverRule.styleMapping().yearStyleId()
            );
            case TitlePageComponentRule titlePageRule -> List.of(
                    titlePageRule.styleMapping().authorsStyleId(),
                    titlePageRule.styleMapping().titleStyleId(),
                    titlePageRule.styleMapping().subtitleStyleId(),
                    titlePageRule.styleMapping().natureStyleId(),
                    titlePageRule.styleMapping().advisorStyleId(),
                    titlePageRule.styleMapping().coadvisorStyleId(),
                    titlePageRule.styleMapping().cityStyleId(),
                    titlePageRule.styleMapping().yearStyleId()
            );
            default -> List.of();
        };
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
