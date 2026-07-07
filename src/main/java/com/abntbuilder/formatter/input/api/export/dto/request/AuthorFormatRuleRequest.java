package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.profile.component.references.AuthorFormatRule;
import com.abntbuilder.formatter.engine.model.profile.component.references.AuthorFormatRule.NameOrder;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Optional;

public record AuthorFormatRuleRequest(
        @NotNull Boolean surnameUppercase,
        @NotBlank String surnameGivenSeparator,
        @NotBlank String nameTerminator,
        @NotBlank String multiAuthorJoiner,
        @NotBlank String etAlLabel,
        @Min(1) int etAlThreshold,
        String lastAuthorJoiner,
        NameOrder nameOrder,
        Boolean initialsOnly,
        Boolean initialsDotted,
        Boolean initialsSpaced
) {
    public AuthorFormatRule toDomain() {
        return new AuthorFormatRule(surnameUppercase, surnameGivenSeparator, nameTerminator,
                multiAuthorJoiner, etAlLabel, etAlThreshold,
                Optional.ofNullable(lastAuthorJoiner),
                nameOrder != null ? nameOrder : NameOrder.SURNAME_FIRST,
                initialsOnly != null && initialsOnly,
                initialsDotted != null && initialsDotted,
                initialsSpaced != null && initialsSpaced);
    }
}
