package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.content.bodycontent.InlineFormatting;

import java.util.Optional;

public record InlineFormattingRequest(
        Boolean bold,
        Boolean italic,
        Boolean underline,
        Boolean superscript,
        Boolean subscript
) {

    public InlineFormatting toDomain() {
        return new InlineFormatting(
                Optional.ofNullable(bold),
                Optional.ofNullable(italic),
                Optional.ofNullable(underline),
                Optional.ofNullable(superscript),
                Optional.ofNullable(subscript)
        );
    }
}
