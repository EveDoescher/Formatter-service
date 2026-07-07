package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.LayoutGapRule;

import java.math.BigDecimal;

public record LayoutGapRuleRequest(
        String fromGroupId,
        String toGroupId,
        BigDecimal weight
) {

    public LayoutGapRule toDomain() {
        return new LayoutGapRule(fromGroupId, toGroupId, weight);
    }
}
