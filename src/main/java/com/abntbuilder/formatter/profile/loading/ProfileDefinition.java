package com.abntbuilder.formatter.profile.loading;

import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.PageOrientation;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.StyleType;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverComponentRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverLayoutRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverStyleMapping;
import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageComponentRule;
import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageStyleMapping;
import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageTextTemplateRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.HorizontalPlacementRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.HorizontalPlacementStrategy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.LayoutGapRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageAnchorStrategy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageGroupRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageItemRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutPolicy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLineHeightStrategy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageSafetyPolicyId;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SpacerStylePolicy;
import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record ProfileDefinition(
        String id,
        String displayName,
        PageRuleDefinition pageRule,
        List<StyleRuleDefinition> styleRules,
        ComponentRulesDefinition componentRules,
        List<String> componentOrder
) {

    public DocumentProfile toDomain() {
        requireNonNull(pageRule, "pageRule");
        requireNonEmpty(styleRules, "styleRules");
        requireNonNull(componentRules, "componentRules");
        requireNonEmpty(componentOrder, "componentOrder");

        List<ComponentRule> resolvedComponentRules = componentRules.toDomain();

        return new DocumentProfile(
                id,
                displayName,
                pageRule.toDomain(),
                styleRules.stream()
                        .map(StyleRuleDefinition::toDomain)
                        .toList(),
                resolvedComponentRules,
                componentOrder
        );
    }

    public record PageRuleDefinition(
            BigDecimal widthCm,
            BigDecimal heightCm,
            BigDecimal marginTopCm,
            BigDecimal marginRightCm,
            BigDecimal marginBottomCm,
            BigDecimal marginLeftCm,
            PageOrientation orientation
    ) {
        PageRule toDomain() {
            return new PageRule(
                    widthCm,
                    heightCm,
                    marginTopCm,
                    marginRightCm,
                    marginBottomCm,
                    marginLeftCm,
                    orientation
            );
        }
    }

    public record StyleRuleDefinition(
            String id,
            StyleType type,
            String fontFamily,
            BigDecimal fontSizePt,
            TextAlignment alignment,
            BigDecimal lineSpacing,
            BigDecimal firstLineIndentCm,
            BigDecimal leftIndentCm,
            BigDecimal rightIndentCm,
            BigDecimal spacingBeforePt,
            BigDecimal spacingAfterPt,
            Boolean bold,
            Boolean italic,
            Boolean uppercase
    ) {
        StyleRule toDomain() {
            requireNonNull(bold, "style.bold");
            requireNonNull(italic, "style.italic");
            requireNonNull(uppercase, "style.uppercase");

            return new StyleRule(
                    id,
                    type,
                    fontFamily,
                    fontSizePt,
                    alignment,
                    lineSpacing,
                    firstLineIndentCm,
                    leftIndentCm,
                    rightIndentCm,
                    spacingBeforePt,
                    spacingAfterPt,
                    bold,
                    italic,
                    uppercase
            );
        }
    }

    public record ComponentRulesDefinition(
            CoverComponentRuleDefinition cover,
            TitlePageComponentRuleDefinition titlePage
    ) {
        List<ComponentRule> toDomain() {
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

    public record CoverComponentRuleDefinition(
            String componentId,
            CoverStyleMappingDefinition styleMapping,
            CoverLayoutRuleDefinition layoutRule
    ) {
        CoverComponentRule toDomain() {
            requireNonNull(styleMapping, "cover.styleMapping");
            requireNonNull(layoutRule, "cover.layoutRule");

            return new CoverComponentRule(
                    componentId,
                    styleMapping.toDomain(),
                    layoutRule.toDomain()
            );
        }
    }

    public record CoverStyleMappingDefinition(
            String institutionalLinesStyleId,
            String authorsStyleId,
            String titleStyleId,
            String subtitleStyleId,
            String cityStyleId,
            String yearStyleId
    ) {
        CoverStyleMapping toDomain() {
            return new CoverStyleMapping(
                    institutionalLinesStyleId,
                    authorsStyleId,
                    titleStyleId,
                    subtitleStyleId,
                    cityStyleId,
                    yearStyleId
            );
        }
    }

    public record CoverLayoutRuleDefinition(
            List<SinglePageGroupRuleDefinition> groups,
            List<LayoutGapRuleDefinition> gapRules,
            SinglePageLayoutPolicyDefinition policy
    ) {
        CoverLayoutRule toDomain() {
            requireNonEmpty(groups, "cover.layoutRule.groups");
            requireNonNull(gapRules, "cover.layoutRule.gapRules");
            requireNonNull(policy, "cover.layoutRule.policy");

            return new CoverLayoutRule(
                    groups.stream()
                            .map(SinglePageGroupRuleDefinition::toDomain)
                            .toList(),
                    gapRules.stream()
                            .map(LayoutGapRuleDefinition::toDomain)
                            .toList(),
                    policy.toDomain()
            );
        }
    }

    public record TitlePageComponentRuleDefinition(
            String componentId,
            TitlePageStyleMappingDefinition styleMapping,
            TitlePageTextTemplateRuleDefinition textTemplates,
            SinglePageLayoutRuleDefinition layoutRule
    ) {
        TitlePageComponentRule toDomain() {
            requireNonNull(styleMapping, "titlePage.styleMapping");
            requireNonNull(textTemplates, "titlePage.textTemplates");
            requireNonNull(layoutRule, "titlePage.layoutRule");

            return new TitlePageComponentRule(
                    componentId,
                    styleMapping.toDomain(),
                    textTemplates.toDomain(),
                    layoutRule.toDomain()
            );
        }
    }

    public record TitlePageStyleMappingDefinition(
            String authorsStyleId,
            String titleStyleId,
            String subtitleStyleId,
            String natureStyleId,
            String advisorStyleId,
            String coadvisorStyleId,
            String cityStyleId,
            String yearStyleId
    ) {
        TitlePageStyleMapping toDomain() {
            return new TitlePageStyleMapping(
                    authorsStyleId,
                    titleStyleId,
                    subtitleStyleId,
                    natureStyleId,
                    advisorStyleId,
                    coadvisorStyleId,
                    cityStyleId,
                    yearStyleId
            );
        }
    }

    public record TitlePageTextTemplateRuleDefinition(
            String natureTemplate,
            String advisorTemplate,
            String coadvisorTemplate
    ) {
        TitlePageTextTemplateRule toDomain() {
            return new TitlePageTextTemplateRule(
                    natureTemplate,
                    advisorTemplate,
                    coadvisorTemplate
            );
        }
    }

    public record SinglePageLayoutRuleDefinition(
            List<SinglePageGroupRuleDefinition> groups,
            List<LayoutGapRuleDefinition> gapRules,
            SinglePageLayoutPolicyDefinition policy
    ) {
        SinglePageLayoutRule toDomain() {
            requireNonEmpty(groups, "layoutRule.groups");
            requireNonNull(gapRules, "layoutRule.gapRules");
            requireNonNull(policy, "layoutRule.policy");

            return new SinglePageLayoutRule(
                    groups.stream()
                            .map(SinglePageGroupRuleDefinition::toDomain)
                            .toList(),
                    gapRules.stream()
                            .map(LayoutGapRuleDefinition::toDomain)
                            .toList(),
                    policy.toDomain()
            );
        }
    }

    public record SinglePageGroupRuleDefinition(
            String id,
            Boolean required,
            List<SinglePageItemRuleDefinition> items
    ) {
        SinglePageGroupRule toDomain() {
            requireNonNull(required, "group.required");
            requireNonEmpty(items, "group.items");

            return new SinglePageGroupRule(
                    id,
                    required,
                    items.stream()
                            .map(SinglePageItemRuleDefinition::toDomain)
                            .toList()
            );
        }
    }

    public record SinglePageItemRuleDefinition(
            String id,
            Boolean required,
            Integer maxVisualLinesPerValue,
            HorizontalPlacementRuleDefinition horizontalPlacement,
            Integer blankLinesAfter
    ) {
        SinglePageItemRule toDomain() {
            requireNonNull(required, "item.required");
            requireNonNull(horizontalPlacement, "item.horizontalPlacement");

            return new SinglePageItemRule(
                    id,
                    required,
                    Optional.ofNullable(maxVisualLinesPerValue),
                    horizontalPlacement.toDomain(),
                    blankLinesAfter == null ? 0 : blankLinesAfter
            );
        }
    }

    public record HorizontalPlacementRuleDefinition(
            HorizontalPlacementStrategy strategy
    ) {
        HorizontalPlacementRule toDomain() {
            return new HorizontalPlacementRule(strategy);
        }
    }

    public record LayoutGapRuleDefinition(
            String fromGroupId,
            String toGroupId,
            BigDecimal weight
    ) {
        LayoutGapRule toDomain() {
            return new LayoutGapRule(fromGroupId, toGroupId, weight);
        }
    }

    public record SinglePageLayoutPolicyDefinition(
            SinglePageAnchorStrategy anchorStrategy,
            SinglePageLineHeightStrategy lineHeightStrategy,
            SpacerStylePolicy spacerStylePolicy,
            SinglePageSafetyPolicyId safetyPolicy
    ) {
        SinglePageLayoutPolicy toDomain() {
            requireNonNull(anchorStrategy, "policy.anchorStrategy");
            requireNonNull(lineHeightStrategy, "policy.lineHeightStrategy");
            requireNonNull(spacerStylePolicy, "policy.spacerStylePolicy");
            requireNonNull(safetyPolicy, "policy.safetyPolicy");

            return new SinglePageLayoutPolicy(
                    anchorStrategy,
                    lineHeightStrategy,
                    spacerStylePolicy,
                    safetyPolicy
            );
        }
    }

    private static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new InvalidProfileStructureException(fieldName + " must be provided.");
        }
    }

    private static void requireNonEmpty(List<?> value, String fieldName) {
        requireNonNull(value, fieldName);

        if (value.isEmpty()) {
            throw new InvalidProfileStructureException(fieldName + " must not be empty.");
        }
    }
}
