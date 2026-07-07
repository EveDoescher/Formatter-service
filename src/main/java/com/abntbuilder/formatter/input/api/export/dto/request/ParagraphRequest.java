package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.application.export.ExportDocxCommand;
import jakarta.validation.constraints.NotBlank;

public record ParagraphRequest(
        @NotBlank String text,
        @NotBlank String styleId
) {
    public ExportDocxCommand.ParagraphCommand toCommand() {
        return new ExportDocxCommand.ParagraphCommand(text, styleId);
    }
}