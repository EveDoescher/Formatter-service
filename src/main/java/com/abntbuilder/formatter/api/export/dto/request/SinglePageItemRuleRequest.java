package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageItemRule;

import java.util.Optional;

public record SinglePageItemRuleRequest(
        String id,
        Boolean required,
        Integer maxVisualLinesPerValue
) {

    public SinglePageItemRule toDomain() {
        return new SinglePageItemRule(
                id,
                Boolean.TRUE.equals(required),
                Optional.ofNullable(maxVisualLinesPerValue)
        );
    }
}
