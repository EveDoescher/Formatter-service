package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.profile.component.singlepage.SinglePageComponentRule;
import com.abntbuilder.formatter.engine.model.profile.component.singlepage.SlotRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.stream.Collectors;

public record SinglePageComponentRuleRequest(
        @NotBlank String componentId,
        @Valid @NotNull Map<String, SlotRuleRequest> slots,
        @NotNull Map<@NotBlank String, @NotBlank String> styleMapping,
        @Valid @NotNull SinglePageLayoutRuleRequest layoutRule
) {

    public SinglePageComponentRule toDomain() {
        Map<String, SlotRule> domainSlots = slots.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        e -> e.getValue().toDomain()
                ));

        return new SinglePageComponentRule(
                componentId,
                domainSlots,
                styleMapping,
                layoutRule.toDomain()
        );
    }
}
