package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.SinglePageGroupRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Objects;

public record SinglePageGroupRuleRequest(
        @NotBlank String id,
        @NotNull Boolean required,
        @Valid @NotNull List<SinglePageItemRuleRequest> items
) {

    public SinglePageGroupRule toDomain() {
        return new SinglePageGroupRule(
                id,
                Objects.requireNonNull(required, "required must not be null"),
                Objects.requireNonNull(items, "items must not be null").stream()
                        .map(SinglePageItemRuleRequest::toDomain)
                        .toList()
        );
    }
}
