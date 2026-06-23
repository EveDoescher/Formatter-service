package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.references.ReferencesFormattingRule;
import jakarta.validation.constraints.NotBlank;

public record ReferencesFormattingRuleRequest(
        @NotBlank String availableAtLabel,
        @NotBlank String accessedAtLabel,
        @NotBlank String etAlLabel,
        @NotBlank String inLabel,
        @NotBlank String authorSurnameGivenSeparator,
        @NotBlank String authorNameTerminator,
        @NotBlank String multiAuthorJoiner,
        boolean authorSurnameUppercase
) {
    public ReferencesFormattingRule toDomain() {
        return new ReferencesFormattingRule(
                availableAtLabel, accessedAtLabel, etAlLabel, inLabel,
                authorSurnameGivenSeparator, authorNameTerminator, multiAuthorJoiner, authorSurnameUppercase
        );
    }
}
