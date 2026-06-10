package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.bodycontent.BodyContentComponentRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BodyContentComponentRuleRequest(
        @NotBlank String componentId,

        @Valid
        @NotNull
        BodyContentStyleMappingRequest styleMapping,

        @Valid
        @NotNull
        BodyContentNumberingRuleRequest numbering,

        @Valid
        @NotNull
        BodyContentLayoutRuleRequest layout
) {

    public BodyContentComponentRule toDomain() {
        return new BodyContentComponentRule(
                componentId,
                styleMapping.toDomain(),
                numbering.toDomain(),
                layout.toDomain()
        );
    }
}
