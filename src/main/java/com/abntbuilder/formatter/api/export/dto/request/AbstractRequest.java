package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.abstracten.AbstractComponent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AbstractRequest(
        @NotBlank String text,
        @NotEmpty List<@NotBlank String> keywords
) {
    public AbstractComponent toDomain() {
        return new AbstractComponent(text, keywords);
    }
}
