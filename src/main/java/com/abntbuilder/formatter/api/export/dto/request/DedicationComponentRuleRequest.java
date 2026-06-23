package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.dedication.DedicationComponentRule;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record DedicationComponentRuleRequest(
        @NotBlank String componentId,
        @NotBlank String textStyleId,
        @Min(0) int blankLinesBefore
) {
    public DedicationComponentRule toDomain() {
        return new DedicationComponentRule(componentId, textStyleId, blankLinesBefore);
    }
}
