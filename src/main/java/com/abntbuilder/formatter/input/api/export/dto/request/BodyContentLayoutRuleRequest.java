package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.BodyContentLayoutRule;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public record BodyContentLayoutRuleRequest(
        @NotNull @Min(0) Integer blankLinesBeforeSectionTitleWhenPrecededByContent,
        @NotNull @Min(0) Integer blankLinesAfterSectionTitle,
        @NotNull Boolean pageBreakBeforePrimarySection,
        @NotBlank String blankLineStyleId
) {

    public BodyContentLayoutRule toDomain() {
        return new BodyContentLayoutRule(
                Objects.requireNonNull(
                        blankLinesBeforeSectionTitleWhenPrecededByContent,
                        "bodyContent.layout.blankLinesBeforeSectionTitleWhenPrecededByContent must not be null"
                ),
                Objects.requireNonNull(
                        blankLinesAfterSectionTitle,
                        "bodyContent.layout.blankLinesAfterSectionTitle must not be null"
                ),
                Objects.requireNonNull(
                        pageBreakBeforePrimarySection,
                        "bodyContent.layout.pageBreakBeforePrimarySection must not be null"
                ),
                blankLineStyleId
        );
    }
}
