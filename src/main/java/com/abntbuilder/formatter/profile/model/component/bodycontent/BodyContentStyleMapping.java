package com.abntbuilder.formatter.profile.model.component.bodycontent;

import com.abntbuilder.formatter.document.component.bodycontent.BodyCitationType;
import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

import java.util.List;
import java.util.Objects;

public record BodyContentStyleMapping(
        List<String> sectionTitleStyleIdsByLevel,
        String paragraphStyleId,
        String directShortQuoteStyleId,
        String directLongQuoteStyleId,
        String indirectCitationStyleId,
        String citationOfCitationStyleId
) {

    public BodyContentStyleMapping {
        Objects.requireNonNull(sectionTitleStyleIdsByLevel, "sectionTitleStyleIdsByLevel must not be null");

        if (sectionTitleStyleIdsByLevel.isEmpty()) {
            throw new InvalidProfileStructureException("sectionTitleStyleIdsByLevel must not be empty.");
        }

        sectionTitleStyleIdsByLevel = List.copyOf(sectionTitleStyleIdsByLevel);

        for (String sectionTitleStyleId : sectionTitleStyleIdsByLevel) {
            requireNonBlank(sectionTitleStyleId, "sectionTitleStyleIdsByLevel item");
        }

        requireNonBlank(paragraphStyleId, "paragraphStyleId");
        requireNonBlank(directShortQuoteStyleId, "directShortQuoteStyleId");
        requireNonBlank(directLongQuoteStyleId, "directLongQuoteStyleId");
        requireNonBlank(indirectCitationStyleId, "indirectCitationStyleId");
        requireNonBlank(citationOfCitationStyleId, "citationOfCitationStyleId");
    }

    public String sectionTitleStyleIdForLevel(int level) {
        if (level < 1 || level > sectionTitleStyleIdsByLevel.size()) {
            throw new InvalidProfileStructureException(
                    "No bodyContent section title style mapped for level: " + level
            );
        }

        return sectionTitleStyleIdsByLevel.get(level - 1);
    }

    public String styleIdForCitation(BodyCitationType type) {
        return switch (type) {
            case DIRECT_SHORT -> directShortQuoteStyleId;
            case DIRECT_LONG -> directLongQuoteStyleId;
            case INDIRECT -> indirectCitationStyleId;
            case CITATION_OF_CITATION -> citationOfCitationStyleId;
        };
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidProfileStructureException(fieldName + " must not be blank.");
        }
    }
}
