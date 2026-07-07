package com.abntbuilder.formatter.engine.model.content.bodycontent;

import java.util.Objects;

public record BodyImageSource(
        ImageSourceType sourceType,
        String dataUri,
        String altText,
        String url
) {

    public BodyImageSource(ImageSourceType sourceType, String dataUri, String altText) {
        this(sourceType, dataUri, altText, null);
    }

    public BodyImageSource {
        Objects.requireNonNull(sourceType, "sourceType must not be null");
        requireNonBlank(altText, "altText");

        switch (sourceType) {
            case DATA_URI -> requireNonBlank(dataUri, "dataUri");
            case URL -> requireNonBlank(url, "url");
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
