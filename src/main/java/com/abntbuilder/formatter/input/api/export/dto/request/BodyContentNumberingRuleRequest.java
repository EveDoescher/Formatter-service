package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.BodyContentNumberingRule;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public record BodyContentNumberingRuleRequest(
        @NotNull Boolean enabled,
        String separator,
        String primarySuffix
) {

    public BodyContentNumberingRule toDomain() {
        return new BodyContentNumberingRule(
                Objects.requireNonNull(enabled, "bodyContent.numbering.enabled must not be null"),
                separator,
                primarySuffix
        );
    }
}
