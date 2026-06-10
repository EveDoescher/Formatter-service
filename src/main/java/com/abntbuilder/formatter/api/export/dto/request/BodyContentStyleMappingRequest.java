package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.bodycontent.BodyContentStyleMapping;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BodyContentStyleMappingRequest(
        @NotEmpty List<@NotBlank String> sectionTitleStyleIdsByLevel,
        @NotBlank String paragraphStyleId,
        @NotBlank String directShortQuoteStyleId,
        @NotBlank String directLongQuoteStyleId,
        @NotBlank String indirectCitationStyleId,
        @NotBlank String citationOfCitationStyleId
) {

    public BodyContentStyleMapping toDomain() {
        return new BodyContentStyleMapping(
                sectionTitleStyleIdsByLevel,
                paragraphStyleId,
                directShortQuoteStyleId,
                directLongQuoteStyleId,
                indirectCitationStyleId,
                citationOfCitationStyleId
        );
    }
}
