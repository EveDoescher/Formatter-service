package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.cover.CoverLayoutRule;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record CoverLayoutRuleRequest(
        List<SinglePageGroupRuleRequest> groups,
        List<LayoutGapRuleRequest> gapRules,
        SinglePageLayoutPolicyRequest policy,
        @Positive BigDecimal topToAuthorWeight,
        @Positive BigDecimal authorToTitleWeight,
        @Positive BigDecimal titleToBottomWeight
) {
    public CoverLayoutRule toDomain() {
        if (groups != null || gapRules != null || policy != null) {
            return new CoverLayoutRule(
                    (groups == null ? List.<SinglePageGroupRuleRequest>of() : groups).stream()
                            .map(SinglePageGroupRuleRequest::toDomain)
                            .toList(),
                    (gapRules == null ? List.<LayoutGapRuleRequest>of() : gapRules).stream()
                            .map(LayoutGapRuleRequest::toDomain)
                            .toList(),
                    policy == null
                            ? com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutPolicy
                                    .defaultSinglePagePolicy()
                            : policy.toDomain()
            );
        }

        return new CoverLayoutRule(
                topToAuthorWeight,
                authorToTitleWeight,
                titleToBottomWeight
        );
    }
}
