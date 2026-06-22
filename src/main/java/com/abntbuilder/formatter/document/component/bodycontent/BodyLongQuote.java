package com.abntbuilder.formatter.document.component.bodycontent;

import com.abntbuilder.formatter.profile.model.component.bodycontent.CitationFormattingRule;

import java.util.Objects;
import java.util.Optional;

public record BodyLongQuote(
        String text,
        BodyCitationMode mode,
        Optional<CitationSource> source,
        Optional<CitationSource> originalSource,
        Optional<CitationSource> consultedSource,
        java.util.List<BodyQuoteMarker> markers
) implements BodyBlock {

    public BodyLongQuote(
            String text,
            BodyCitationMode mode,
            Optional<CitationSource> source,
            Optional<CitationSource> originalSource,
            Optional<CitationSource> consultedSource
    ) {
        this(text, mode, source, originalSource, consultedSource, java.util.List.of());
    }

    public BodyLongQuote {
        requireNonBlank(text, "text");
        Objects.requireNonNull(mode, "mode must not be null");
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(originalSource, "originalSource must not be null");
        Objects.requireNonNull(consultedSource, "consultedSource must not be null");

        CitationSource citationSource = source.orElseThrow(() ->
                new IllegalArgumentException("DIRECT_LONG citation source must be provided.")
        );
        citationSource.requirePage("DIRECT_LONG");
        Objects.requireNonNull(markers, "markers must not be null");
    }

    public String renderedText(CitationFormattingRule formatting) {
        boolean emphasisOurs = markers.stream().anyMatch(m -> m.type() == BodyQuoteMarkerType.EMPHASIS_OURS);
        boolean emphasisAuthor = markers.stream().anyMatch(m -> m.type() == BodyQuoteMarkerType.EMPHASIS_AUTHOR);
        BodyCitationCall call = new BodyCitationCall(
                BodyCitationType.DIRECT_LONG, mode, formatting, source, originalSource, consultedSource, emphasisOurs, emphasisAuthor
        );
        String processedText = applyMarkers(text, markers);
        return switch (mode) {
            case PARENTHETICAL -> ensureNoFinalPeriod(processedText) + " " + call.renderedText() + ".";
            case NARRATIVE -> call.renderedText() + ": " + ensureFinalPeriod(processedText);
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
                default -> { }
            }
        }
        return sb.toString();
    }

    private static String ensureNoFinalPeriod(String value) {
        String trimmed = value.trim();
        return trimmed.endsWith(".") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }

    private static String ensureFinalPeriod(String value) {
        String trimmed = value.trim();
        return trimmed.endsWith(".") ? trimmed : trimmed + ".";
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
