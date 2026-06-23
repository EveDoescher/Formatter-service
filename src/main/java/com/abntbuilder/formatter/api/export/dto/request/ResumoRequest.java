package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.resumo.ResumoComponent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ResumoRequest(
        @NotBlank String text,
        @NotEmpty List<@NotBlank String> keywords
) {
    public ResumoComponent toDomain() {
        return new ResumoComponent(text, keywords);
    }
}
