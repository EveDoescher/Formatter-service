package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.cover.CoverLayoutRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutPolicy;

import java.util.List;

public record CoverLayoutRuleRequest(
        List<SinglePageGroupRuleRequest> groups,
        List<LayoutGapRuleRequest> gapRules,
        SinglePageLayoutPolicyRequest policy
) {
    public CoverLayoutRule toDomain() {
        return new CoverLayoutRule(
                (groups == null ? List.<SinglePageGroupRuleRequest>of() : groups).stream()
                        .map(SinglePageGroupRuleRequest::toDomain)
                        .toList(),
                (gapRules == null ? List.<LayoutGapRuleRequest>of() : gapRules).stream()
                        .map(LayoutGapRuleRequest::toDomain)
                        .toList(),
                policy == null ? SinglePageLayoutPolicy.defaultSinglePagePolicy() : policy.toDomain()
        );
    }
}
