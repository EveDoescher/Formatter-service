package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.application.export.ExportDocxCommand;
import com.abntbuilder.formatter.engine.model.FontPreferences;
import com.abntbuilder.formatter.engine.model.content.DocumentComponent;
import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;
import com.abntbuilder.formatter.input.profile.ProfileProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ExportDocxRequest(
        @NotBlank String fileName,

        String profileId,

        @Valid
        ProfileRequest profile,

        @Valid
        ExportOptionsRequest options,

        @Valid
        AcademicWorkRequest work,

        @Valid
        DocumentContentRequest document,

        @Valid
        List<ParagraphRequest> paragraphs
) {
    public ExportDocxCommand toCommand() {
        if (profile == null) {
            throw new IllegalArgumentException("profile must be provided for inline export.");
        }

        return toCommand(profile.toDomain());
    }

    public ExportDocxCommand toCommand(ProfileProvider profileProvider) {
        DocumentProfile documentProfile = profile == null
                ? resolveProfile(profileProvider)
                : profile.toDomain();

        return toCommand(documentProfile);
    }

    private ExportDocxCommand toCommand(DocumentProfile documentProfile) {
        List<DocumentComponent> documentComponents = document == null
                ? List.of()
                : document.toComponents(documentProfile);

        List<ExportDocxCommand.ParagraphCommand> paragraphCommands = paragraphs == null
                ? List.of()
                : paragraphs.stream()
                .map(ParagraphRequest::toCommand)
                .toList();

        return new ExportDocxCommand(
                fileName,
                documentProfile,
                documentComponents,
                options == null || options.selectedComponents() == null
                        ? List.of()
                        : options.selectedComponents(),
                paragraphCommands,
                options == null ? FontPreferences.NONE : options.toFontPreferences()
        );
    }

    private DocumentProfile resolveProfile(ProfileProvider profileProvider) {
        if (profileId == null || profileId.isBlank()) {
            throw new IllegalArgumentException("profileId must be provided when profile is not inline.");
        }

        return profileProvider.findById(profileId);
    }
}
