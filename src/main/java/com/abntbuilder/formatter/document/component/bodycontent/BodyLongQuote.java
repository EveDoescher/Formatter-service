package com.abntbuilder.formatter.document.component.bodycontent;

import com.abntbuilder.formatter.profile.model.component.bodycontent.CitationFormattingRule;

import java.util.Objects;
import java.util.Optional;

public record BodyLongQuote(
        String text,
        BodyCitationMode mode,
        Optional<CitationSource> source,
        Optional<CitationSource> originalSource,
        Optional<CitationSource> consultedSource
) implements BodyBlock {

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
    }

    public String renderedText(CitationFormattingRule formatting) {
        BodyCitationCall call = new BodyCitationCall(
                BodyCitationType.DIRECT_LONG, mode, formatting, source, originalSource, consultedSource
        );
        return switch (mode) {
            case PARENTHETICAL -> ensureNoFinalPeriod(text) + " " + call.renderedText() + ".";
            case NARRATIVE -> call.renderedText() + ": " + ensureFinalPeriod(text);
        };
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
