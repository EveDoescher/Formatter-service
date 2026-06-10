package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.cover.CoverLayoutRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Objects;

public record CoverLayoutRuleRequest(
        @Valid @NotNull List<SinglePageGroupRuleRequest> groups,
        @Valid @NotNull List<LayoutGapRuleRequest> gapRules,
        @Valid @NotNull SinglePageLayoutPolicyRequest policy
) {
    public CoverLayoutRule toDomain() {
        return new CoverLayoutRule(
                Objects.requireNonNull(groups, "groups must not be null").stream()
                        .map(SinglePageGroupRuleRequest::toDomain)
                        .toList(),
                Objects.requireNonNull(gapRules, "gapRules must not be null").stream()
                        .map(LayoutGapRuleRequest::toDomain)
                        .toList(),
                Objects.requireNonNull(policy, "policy must not be null").toDomain()
        );
    }
}
