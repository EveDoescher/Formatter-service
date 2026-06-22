package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.bodycontent.BodyEquation;
import jakarta.validation.constraints.NotBlank;

import java.util.Optional;

public record BodyEquationRequest(
        @NotBlank String text,
        String label
) {
    public BodyEquation toDomain() {
        return new BodyEquation(
                text,
                Optional.ofNullable(label)
        );
    }
}
