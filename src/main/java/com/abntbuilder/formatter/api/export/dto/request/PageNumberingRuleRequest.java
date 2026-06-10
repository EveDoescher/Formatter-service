package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.PageNumberingPlacement;
import com.abntbuilder.formatter.profile.model.PageNumberingRule;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Objects;

public record PageNumberingRuleRequest(
        @NotNull Boolean enabled,
        String countFromComponentId,
        String visibleFromComponentId,
        String styleId,
        PageNumberingPlacement placement,
        BigDecimal verticalDistanceFromPageEdgeCm,
        BigDecimal horizontalDistanceFromPageEdgeCm
) {

    public PageNumberingRule toDomain() {
        return new PageNumberingRule(
                Objects.requireNonNull(enabled, "pageNumbering.enabled must not be null"),
                countFromComponentId,
                visibleFromComponentId,
                styleId,
                placement,
                verticalDistanceFromPageEdgeCm,
                horizontalDistanceFromPageEdgeCm
        );
    }
}
