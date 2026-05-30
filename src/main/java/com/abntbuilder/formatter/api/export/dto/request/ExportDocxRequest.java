package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.application.export.ExportDocxCommand;
import com.abntbuilder.formatter.document.component.cover.CoverComponent;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.resolution.ProfileProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Optional;

public record ExportDocxRequest(
        @NotBlank String fileName,

        String profileId,

        @Valid
        ProfileRequest profile,

        @Valid
        ExportOptionsRequest options,

        @Valid
        DocumentContentRequest document,

        @Deprecated(since = "cover-semantic-request")
        @Valid
        LegacyCoverRequest cover,

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
        Optional<CoverComponent> coverComponent = resolveCover();

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

    private Optional<CoverComponent> resolveCover() {
        if (document != null && document.cover() != null) {
            return Optional.of(document.cover().toDomain());
        }

        if (cover != null) {
            return Optional.of(cover.toDomain());
        }

        return Optional.empty();
    }

    private DocumentProfile resolveProfile(ProfileProvider profileProvider) {
        if (profileId == null || profileId.isBlank()) {
            throw new IllegalArgumentException("profileId must be provided when profile is not inline.");
        }

        return profileProvider.findById(profileId);
    }
}
