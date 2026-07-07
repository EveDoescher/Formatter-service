package com.abntbuilder.formatter.engine.model.profile.component.bodycontent;

import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

import java.util.Objects;

public record CodeListingRule(
        String captionStyleId,
        String sourceStyleId,
        String codeStyleId,
        String captionTemplate,
        String sourceTemplate,
        DisplayObjectContinuationLabels continuationLabels,
        DisplayObjectSourcePlacement sourcePlacement,
        NumberingStrategy numberingStrategy,
        String label,
        String separator
) {

    public CodeListingRule {
        requireNonBlank(captionStyleId, "captionStyleId");
        requireNonBlank(sourceStyleId, "sourceStyleId");
        requireNonBlank(codeStyleId, "codeStyleId");
        requireNonBlank(captionTemplate, "captionTemplate");
        requireNonBlank(sourceTemplate, "sourceTemplate");
        Objects.requireNonNull(continuationLabels, "continuationLabels must not be null");
        Objects.requireNonNull(sourcePlacement, "sourcePlacement must not be null");
        Objects.requireNonNull(numberingStrategy, "numberingStrategy must not be null");
        requireNonBlank(label, "label");

        if (!captionTemplate.contains("{number}") || !captionTemplate.contains("{caption}")) {
            throw new InvalidProfileStructureException(
                    "codeListing.captionTemplate must contain {number} and {caption}."
            );
        }
        if (!sourceTemplate.contains("{source}")) {
            throw new InvalidProfileStructureException(
                    "codeListing.sourceTemplate must contain {source}."
            );
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidProfileStructureException("codeListing." + fieldName + " must not be blank.");
        }
    }
}
