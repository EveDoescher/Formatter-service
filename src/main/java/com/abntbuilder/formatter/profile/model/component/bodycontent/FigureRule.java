package com.abntbuilder.formatter.profile.model.component.bodycontent;

import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

import java.math.BigDecimal;
import java.util.Objects;

public record FigureRule(
        String captionStyleId,
        String sourceStyleId,
        String captionTemplate,
        String sourceTemplate,
        DisplayObjectContinuationLabels continuationLabels,
        DisplayObjectSourcePlacement sourcePlacement,
        TextAlignment imageAlignment,
        BigDecimal maxWidthCm,
        BigDecimal maxHeightCm,
        BigDecimal defaultDpi,
        Integer maxImageBytes,
        Integer urlFetchTimeoutSeconds,
        ImageFitPolicy fitPolicy
) {

    public FigureRule {
        requireNonBlank(captionStyleId, "captionStyleId");
        requireNonBlank(sourceStyleId, "sourceStyleId");
        requireNonBlank(captionTemplate, "captionTemplate");
        requireNonBlank(sourceTemplate, "sourceTemplate");
        Objects.requireNonNull(continuationLabels, "continuationLabels must not be null");
        Objects.requireNonNull(sourcePlacement, "sourcePlacement must not be null");
        Objects.requireNonNull(imageAlignment, "imageAlignment must not be null");
        requirePositive(maxWidthCm, "maxWidthCm");
        requirePositive(maxHeightCm, "maxHeightCm");
        requirePositive(defaultDpi, "defaultDpi");
        requirePositive(maxImageBytes, "maxImageBytes");
        requirePositive(urlFetchTimeoutSeconds, "urlFetchTimeoutSeconds");
        Objects.requireNonNull(fitPolicy, "fitPolicy must not be null");

        if (!captionTemplate.contains("{number}") || !captionTemplate.contains("{caption}")) {
            throw new InvalidProfileStructureException(
                    "figure.captionTemplate must contain {number} and {caption}."
            );
        }

        if (!sourceTemplate.contains("{source}")) {
            throw new InvalidProfileStructureException("figure.sourceTemplate must contain {source}.");
        }
    }

    private static void requirePositive(BigDecimal value, String fieldName) {
        if (value == null || value.signum() <= 0) {
            throw new InvalidProfileStructureException("figure." + fieldName + " must be greater than zero.");
        }
    }

    private static void requirePositive(Integer value, String fieldName) {
        if (value == null || value <= 0) {
            throw new InvalidProfileStructureException("figure." + fieldName + " must be greater than zero.");
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidProfileStructureException("figure." + fieldName + " must not be blank.");
        }
    }
}
