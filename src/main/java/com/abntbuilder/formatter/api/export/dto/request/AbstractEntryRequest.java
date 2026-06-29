package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.abstracten.AbstractEntry;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AbstractEntryRequest(
        @NotBlank String headingText,
        @NotBlank String text,
        @NotEmpty List<@NotBlank String> keywords,
        @NotBlank String keywordsLabel
) {
    public AbstractEntry toDomain() {
        return new AbstractEntry(headingText, text, keywords, keywordsLabel);
    }
}
