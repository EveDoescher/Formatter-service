package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.acknowledgments.AcknowledgmentsComponent;
import jakarta.validation.constraints.NotBlank;

public record AcknowledgmentsRequest(@NotBlank String text) {
    public AcknowledgmentsComponent toDomain() {
        return new AcknowledgmentsComponent(text);
    }
}
