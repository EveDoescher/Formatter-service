package com.abntbuilder.formatter.document.component.bodycontent;

import java.util.Objects;
import java.util.Optional;

public record BodyCitation(
        BodyCitationType type,
        BodyCitationMode mode,
        String text,
        Optional<CitationSource> source,
        Optional<CitationSource> originalSource,
        Optional<CitationSource> consultedSource
) implements BodyBlock {

    public BodyCitation {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(mode, "mode must not be null");
        requireNonBlank(text, "text");
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(originalSource, "originalSource must not be null");
        Objects.requireNonNull(consultedSource, "consultedSource must not be null");

        validateSources(type, source, originalSource, consultedSource);
        if (type == BodyCitationType.DIRECT_SHORT) {
            new BodyQuoteText(BodyQuoteType.SHORT, text);
        }
    }

    public String renderedText() {
        return switch (type) {
            case DIRECT_SHORT -> renderDirectShort();
            case DIRECT_LONG -> renderDirectLong();
            case INDIRECT -> renderIndirect();
            case CITATION_OF_CITATION -> renderCitationOfCitation();
        };
    }

    private String renderDirectShort() {
        BodyCitationCall citationCall = citationCall();
        BodyQuoteText quoteText = new BodyQuoteText(BodyQuoteType.SHORT, text);

        return switch (mode) {
            case PARENTHETICAL -> quoteText.renderedText() + " " + citationCall.renderedText() + ".";
            case NARRATIVE -> citationCall.renderedText() + ", " + ensureFinalPeriod(quoteText.renderedText());
        };
    }

    private String renderDirectLong() {
        BodyCitationCall citationCall = citationCall();

        return switch (mode) {
            case PARENTHETICAL -> ensureNoFinalPeriod(text) + " " + citationCall.renderedText() + ".";
            case NARRATIVE -> citationCall.renderedText() + ": " + ensureFinalPeriod(text);
        };
    }

    private String renderIndirect() {
        BodyCitationCall citationCall = citationCall();

        return switch (mode) {
            case PARENTHETICAL -> ensureNoFinalPeriod(text) + " " + citationCall.renderedText() + ".";
            case NARRATIVE -> citationCall.renderedText() + ", " + ensureFinalPeriod(text);
        };
    }

    private String renderCitationOfCitation() {
        BodyCitationCall citationCall = citationCall();

        return switch (mode) {
            case PARENTHETICAL -> ensureNoFinalPeriod(text) + " " + citationCall.renderedText() + ".";
            case NARRATIVE -> citationCall.renderedText() + ", " + ensureFinalPeriod(text);
        };
    }

    private BodyCitationCall citationCall() {
        com.abntbuilder.formatter.profile.model.component.bodycontent.CitationFormattingRule fmt =
                new com.abntbuilder.formatter.profile.model.component.bodycontent.CitationFormattingRule(
                        "p. ", "; ", "et al.", " apud "
                );
        return new BodyCitationCall(type, mode, fmt, source, originalSource, consultedSource);
    }

    private static void validateSources(
            BodyCitationType type,
            Optional<CitationSource> source,
            Optional<CitationSource> originalSource,
            Optional<CitationSource> consultedSource
    ) {
        switch (type) {
            case DIRECT_SHORT, DIRECT_LONG -> {
                CitationSource citationSource = source.orElseThrow(() ->
                        new IllegalArgumentException(type + " citation source must be provided.")
                );
                citationSource.requirePage(type.name());
                requireAbsent(originalSource, "originalSource");
                requireAbsent(consultedSource, "consultedSource");
            }
            case INDIRECT -> {
                if (source.isEmpty()) {
                    throw new IllegalArgumentException("INDIRECT citation source must be provided.");
                }
                requireAbsent(originalSource, "originalSource");
                requireAbsent(consultedSource, "consultedSource");
            }
            case CITATION_OF_CITATION -> {
                requireAbsent(source, "source");
                if (originalSource.isEmpty()) {
                    throw new IllegalArgumentException("CITATION_OF_CITATION originalSource must be provided.");
                }
                CitationSource consulted = consultedSource.orElseThrow(() ->
                        new IllegalArgumentException("CITATION_OF_CITATION consultedSource must be provided.")
                );
                consulted.requirePage(type.name());
            }
        }
    }

    private static void requireAbsent(Optional<CitationSource> source, String fieldName) {
        if (source.isPresent()) {
            throw new IllegalArgumentException(fieldName + " must not be provided for this citation type.");
        }
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
