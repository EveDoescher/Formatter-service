package com.abntbuilder.formatter.engine.model.profile;

import com.abntbuilder.formatter.engine.model.profile.component.ComponentRule;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.BodyContentComponentRule;
import com.abntbuilder.formatter.engine.model.profile.component.singlepage.SinglePageComponentRule;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record DocumentProfile(
        String id,
        String displayName,
        PageRule pageRule,
        Optional<PageNumberingRule> pageNumberingRule,
        Optional<PostProcessingRule> postProcessingRule,
        List<StyleRule> styleRules,
        List<ComponentRule> componentRules,
        List<String> componentOrder,
        Map<String, FontRoleRule> fontRoles
) {
    public static final String PARAGRAPHS_INTERNAL_COMPONENT_ID = "paragraphs";
    private static final Set<String> INTERNAL_COMPONENT_IDS = Set.of(PARAGRAPHS_INTERNAL_COMPONENT_ID);

    public DocumentProfile {
        requireNonBlank(id, "id");
        requireNonBlank(displayName, "displayName");
        Objects.requireNonNull(pageRule, "pageRule must not be null");
        Objects.requireNonNull(pageNumberingRule, "pageNumberingRule must not be null");
        Objects.requireNonNull(postProcessingRule, "postProcessingRule must not be null");
        Objects.requireNonNull(styleRules, "styleRules must not be null");
        Objects.requireNonNull(componentRules, "componentRules must not be null");
        Objects.requireNonNull(componentOrder, "componentOrder must not be null");
        Objects.requireNonNull(fontRoles, "fontRoles must not be null");

        if (styleRules.isEmpty()) {
            throw new IllegalArgumentException("styleRules must not be empty.");
        }

        styleRules = List.copyOf(styleRules);
        componentRules = List.copyOf(componentRules);
        componentOrder = List.copyOf(componentOrder);
        fontRoles = Map.copyOf(fontRoles);

        validateStyleRules(styleRules);
        validateComponentRules(componentRules);
        validateComponentOrder(componentRules, componentOrder);
        validateComponentStyleMappings(styleRules, componentRules);
        validatePageNumberingRule(styleRules, componentRules, componentOrder, pageNumberingRule);
        validateFontRoles(styleRules, fontRoles);
    }

    public DocumentProfile(
            String id,
            String displayName,
            PageRule pageRule,
            List<StyleRule> styleRules,
            List<ComponentRule> componentRules,
            List<String> componentOrder
    ) {
        this(id, displayName, pageRule, Optional.empty(), Optional.empty(), styleRules, componentRules, componentOrder, Map.of());
    }

    public DocumentProfile(
            String id,
            String displayName,
            PageRule pageRule,
            Optional<PageNumberingRule> pageNumberingRule,
            List<StyleRule> styleRules,
            List<ComponentRule> componentRules,
            List<String> componentOrder
    ) {
        this(id, displayName, pageRule, pageNumberingRule, Optional.empty(), styleRules, componentRules, componentOrder, Map.of());
    }

    public DocumentProfile(
            String id,
            String displayName,
            PageRule pageRule,
            Optional<PageNumberingRule> pageNumberingRule,
            Optional<PostProcessingRule> postProcessingRule,
            List<StyleRule> styleRules,
            List<ComponentRule> componentRules,
            List<String> componentOrder
    ) {
        this(id, displayName, pageRule, pageNumberingRule, postProcessingRule, styleRules, componentRules, componentOrder, Map.of());
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

    private static void validatePageNumberingRule(
            List<StyleRule> styleRules,
            List<ComponentRule> componentRules,
            List<String> componentOrder,
            Optional<PageNumberingRule> pageNumberingRule
    ) {
        if (pageNumberingRule.isEmpty() || !pageNumberingRule.orElseThrow().enabled()) {
            return;
        }

        PageNumberingRule rule = pageNumberingRule.orElseThrow();
        Set<String> styleIds = styleRules.stream()
                .map(StyleRule::id)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());

        if (!styleIds.contains(rule.styleId())) {
            throw new IllegalArgumentException(
                    "Page numbering references unknown style id: " + rule.styleId()
            );
        }

        Set<String> componentIds = componentRules.stream()
                .map(ComponentRule::componentId)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());

        validatePageNumberingComponentId(componentIds, rule.countFromComponentId());
        validatePageNumberingComponentId(componentIds, rule.visibleFromComponentId());

        int countFromIndex = componentOrder.indexOf(rule.countFromComponentId());
        int visibleFromIndex = componentOrder.indexOf(rule.visibleFromComponentId());

        if (countFromIndex < 0) {
            throw new IllegalArgumentException(
                    "Page numbering countFromComponentId is not present in componentOrder: "
                            + rule.countFromComponentId()
            );
        }

        if (visibleFromIndex < 0) {
            throw new IllegalArgumentException(
                    "Page numbering visibleFromComponentId is not present in componentOrder: "
                            + rule.visibleFromComponentId()
            );
        }

        if (countFromIndex > visibleFromIndex) {
            throw new IllegalArgumentException(
                    "Page numbering countFromComponentId must not come after visibleFromComponentId."
            );
        }
    }

    private static void validatePageNumberingComponentId(Set<String> componentIds, String componentId) {
        if (!componentIds.contains(componentId) && !INTERNAL_COMPONENT_IDS.contains(componentId)) {
            throw new IllegalArgumentException("Page numbering references unknown component id: " + componentId);
        }
    }

    private static Collection<String> styleIdsFor(ComponentRule componentRule) {
        return switch (componentRule) {
            case SinglePageComponentRule singlePageRule -> singlePageRule.styleMapping().values().stream().toList();
            case BodyContentComponentRule bodyContentRule -> {
                List<String> styleIds = new java.util.ArrayList<>(
                        bodyContentRule.styleMapping().sectionTitleStyleIdsByLevel()
                );
                styleIds.add(bodyContentRule.styleMapping().paragraphStyleId());
                styleIds.add(bodyContentRule.styleMapping().directShortQuoteStyleId());
                styleIds.add(bodyContentRule.styleMapping().directLongQuoteStyleId());
                styleIds.add(bodyContentRule.styleMapping().indirectCitationStyleId());
                styleIds.add(bodyContentRule.styleMapping().citationOfCitationStyleId());
                styleIds.add(bodyContentRule.layout().blankLineStyleId());
                yield styleIds;
            }
            default -> List.of();
        };
    }

    private static void validateFontRoles(List<StyleRule> styleRules, Map<String, FontRoleRule> fontRoles) {
        if (fontRoles.isEmpty()) {
            return;
        }

        Set<String> styleIds = styleRules.stream()
                .map(StyleRule::id)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());

        for (Map.Entry<String, FontRoleRule> entry : fontRoles.entrySet()) {
            String roleName = entry.getKey();
            for (String styleId : entry.getValue().styleIds()) {
                if (!styleIds.contains(styleId)) {
                    throw new IllegalArgumentException(
                            "fontRoles[" + roleName + "] references unknown style id: " + styleId
                    );
                }
            }
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
