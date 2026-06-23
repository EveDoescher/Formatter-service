package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.dedication.DedicationComponent;
import jakarta.validation.constraints.NotBlank;

public record DedicationRequest(@NotBlank String text) {
    public DedicationComponent toDomain() {
        return new DedicationComponent(text);
    }
}
