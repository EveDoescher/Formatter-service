package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.bodycontent.InlineFormatting;

import java.util.Optional;

public record InlineFormattingRequest(
        Boolean bold,
        Boolean italic,
        Boolean underline
) {

    public InlineFormatting toDomain() {
        return new InlineFormatting(
                Optional.ofNullable(bold),
                Optional.ofNullable(italic),
                Optional.ofNullable(underline)
        );
    }
}
