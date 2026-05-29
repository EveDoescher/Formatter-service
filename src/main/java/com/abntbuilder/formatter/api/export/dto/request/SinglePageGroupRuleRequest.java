package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageGroupRule;

import java.util.List;

public record SinglePageGroupRuleRequest(
        String id,
        Boolean required,
        List<SinglePageItemRuleRequest> items
) {

    public SinglePageGroupRule toDomain() {
        return new SinglePageGroupRule(
                id,
                Boolean.TRUE.equals(required),
                items.stream()
                        .map(SinglePageItemRuleRequest::toDomain)
                        .toList()
        );
    }
}
