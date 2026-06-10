package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageAnchorStrategy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutPolicy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLineHeightStrategy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageSafetyPolicyId;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SpacerStylePolicy;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public record SinglePageLayoutPolicyRequest(
        @NotNull SinglePageAnchorStrategy anchorStrategy,
        @NotNull SinglePageLineHeightStrategy lineHeightStrategy,
        @NotNull SpacerStylePolicy spacerStylePolicy,
        @NotNull SinglePageSafetyPolicyId safetyPolicy
) {

    public SinglePageLayoutPolicy toDomain() {
        return new SinglePageLayoutPolicy(
                Objects.requireNonNull(anchorStrategy, "anchorStrategy must not be null"),
                Objects.requireNonNull(lineHeightStrategy, "lineHeightStrategy must not be null"),
                Objects.requireNonNull(spacerStylePolicy, "spacerStylePolicy must not be null"),
                Objects.requireNonNull(safetyPolicy, "safetyPolicy must not be null")
        );
    }
}
