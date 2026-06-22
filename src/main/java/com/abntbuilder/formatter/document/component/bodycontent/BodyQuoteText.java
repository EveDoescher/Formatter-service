package com.abntbuilder.formatter.document.component.bodycontent;

import java.util.Objects;

public record BodyQuoteText(
        BodyQuoteType type,
        String text,
        InlineFormatting formatting,
        java.util.List<BodyQuoteMarker> markers
) implements BodyInline {

    public BodyQuoteText(BodyQuoteType type, String text) {
        this(type, text, InlineFormatting.none(), java.util.List.of());
    }

    public BodyQuoteText {
        Objects.requireNonNull(type, "type must not be null");
        requireNonBlank(text, "text");
        Objects.requireNonNull(formatting, "formatting must not be null");
        Objects.requireNonNull(markers, "markers must not be null");
    }

    @Override
    public String renderedText() {
        String processed = applyMarkers(text, markers);
        return switch (type) {
            case SHORT -> "\"" + processed.trim() + "\"";
        };
    }

    private static String applyMarkers(String text, java.util.List<BodyQuoteMarker> markers) {
        java.util.List<BodyQuoteMarker> positionMarkers = markers.stream()
                .filter(m -> m.type() == BodyQuoteMarkerType.SUPPRESSION
                        || m.type() == BodyQuoteMarkerType.INTERPOLATION)
                .sorted(java.util.Comparator.comparingInt(BodyQuoteMarker::position).reversed())
                .toList();

        StringBuilder sb = new StringBuilder(text);
        for (BodyQuoteMarker marker : positionMarkers) {
            switch (marker.type()) {
                case SUPPRESSION -> {
                    int pos = Math.min(marker.position(), sb.length());
                    sb.insert(pos, "[...]");
                }
                case INTERPOLATION -> {
                    int start = Math.min(marker.position(), sb.length());
                    int end = Math.min(marker.endPosition().orElse(start), sb.length());
                    if (end < start) break;
                    sb.insert(end, "]");
                    sb.insert(start, "[");
                }
                default -> { /* EMPHASIS tratado externamente */ }
            }
        }
        return sb.toString();
    }

    private static void requireNoBoundaryQuotes(String value) {
        String trimmed = value.trim();
        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            throw new IllegalArgumentException(
                    "manual boundary quotation marks must not be provided for SHORT quote text."
            );
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
        requireNoBoundaryQuotes(value);
    }
}
