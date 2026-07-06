package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.references.AuthorFormatRule;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AuthorFormatRuleRequest(
        @NotNull Boolean surnameUppercase,
        @NotBlank String surnameGivenSeparator,
        @NotBlank String nameTerminator,
        @NotBlank String multiAuthorJoiner,
        @NotBlank String etAlLabel,
        @Min(1) int etAlThreshold
) {
    public AuthorFormatRule toDomain() {
        return new AuthorFormatRule(surnameUppercase, surnameGivenSeparator, nameTerminator,
                multiAuthorJoiner, etAlLabel, etAlThreshold);
    }
}
