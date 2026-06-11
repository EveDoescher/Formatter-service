package com.abntbuilder.formatter.output.docx.api;

import com.abntbuilder.formatter.profile.model.TextAlignment;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;

public record DocxImageBlock(
        byte[] bytes,
        String mimeType,
        String altText,
        BigDecimal widthCm,
        BigDecimal heightCm,
        TextAlignment alignment,
        boolean keepWithNext,
        boolean keepLines
) implements DocxBlock {

    public DocxImageBlock {
        Objects.requireNonNull(bytes, "bytes must not be null");
        if (bytes.length == 0) {
            throw new IllegalArgumentException("bytes must not be empty.");
        }
        requireNonBlank(mimeType, "mimeType");
        requireNonBlank(altText, "altText");
        requirePositive(widthCm, "widthCm");
        requirePositive(heightCm, "heightCm");
        Objects.requireNonNull(alignment, "alignment must not be null");

        bytes = Arrays.copyOf(bytes, bytes.length);
    }

    @Override
    public byte[] bytes() {
        return Arrays.copyOf(bytes, bytes.length);
    }

    private static void requirePositive(BigDecimal value, String fieldName) {
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero.");
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
