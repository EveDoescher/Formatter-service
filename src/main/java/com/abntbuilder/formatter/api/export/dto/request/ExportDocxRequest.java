package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.application.export.ExportDocxCommand;
import com.abntbuilder.formatter.document.component.cover.CoverComponent;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Optional;

public record ExportDocxRequest(
        @NotBlank String fileName,

        @Valid
        @NotNull
        ProfileRequest profile,

        @Valid
        CoverRequest cover,

        @Valid
        List<ParagraphRequest> paragraphs
) {
    public ExportDocxCommand toCommand() {
        DocumentProfile documentProfile = profile.toDomain();

        Optional<CoverComponent> coverComponent = cover == null
                ? Optional.empty()
                : Optional.of(cover.toDomain());

        List<ExportDocxCommand.ParagraphCommand> paragraphCommands = paragraphs == null
                ? List.of()
                : paragraphs.stream()
                .map(ParagraphRequest::toCommand)
                .toList();

        return new ExportDocxCommand(
                fileName,
                documentProfile,
                coverComponent,
                paragraphCommands
        );
    }
}