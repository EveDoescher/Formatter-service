package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.listofabbreviations.ListOfAbbreviationsComponentRule;
import jakarta.validation.constraints.NotBlank;

public record ListOfAbbreviationsComponentRuleRequest(
        @NotBlank String headingStyleId,
        @NotBlank String headingText,
        @NotBlank String entryStyleId,
        @NotBlank String termSeparator,
        boolean sortAlphabetically,
        int blankLinesAfterHeading
) {
    public ListOfAbbreviationsComponentRule toDomain(String componentId) {
        return new ListOfAbbreviationsComponentRule(componentId, headingStyleId, headingText,
                entryStyleId, termSeparator, sortAlphabetically, blankLinesAfterHeading);
    }
}
