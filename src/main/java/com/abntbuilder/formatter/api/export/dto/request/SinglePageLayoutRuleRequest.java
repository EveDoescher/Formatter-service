package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutPolicy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutRule;

import java.util.List;

public record SinglePageLayoutRuleRequest(
        List<SinglePageGroupRuleRequest> groups,
        List<LayoutGapRuleRequest> gapRules,
        SinglePageLayoutPolicyRequest policy
) {

    public SinglePageLayoutRule toDomain() {
        return new SinglePageLayoutRule(
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
