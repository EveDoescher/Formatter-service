package com.abntbuilder.formatter.engine.model.content.bodycontent;

import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.CitationFormattingRule;

import java.util.Objects;
import java.util.Optional;

public record BodyCitationCall(
        BodyCitationType citationType,
        BodyCitationMode mode,
        CitationFormattingRule formatting,
        Optional<CitationSource> source,
        Optional<CitationSource> originalSource,
        Optional<CitationSource> consultedSource,
        boolean emphasisOurs,
        boolean emphasisAuthor
) implements BodyInline {

    public BodyCitationCall(
            BodyCitationType citationType,
            BodyCitationMode mode,
            CitationFormattingRule formatting,
            Optional<CitationSource> source,
            Optional<CitationSource> originalSource,
            Optional<CitationSource> consultedSource
    ) {
        this(citationType, mode, formatting, source, originalSource, consultedSource, false, false);
    }

    public BodyCitationCall {
        Objects.requireNonNull(citationType, "citationType must not be null");
        Objects.requireNonNull(mode, "mode must not be null");
        Objects.requireNonNull(formatting, "formatting must not be null");
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(originalSource, "originalSource must not be null");
        Objects.requireNonNull(consultedSource, "consultedSource must not be null");

        validateSources(citationType, source, originalSource, consultedSource);
    }

    @Override
    public String renderedText() {
        return switch (citationType) {
            case DIRECT_SHORT, DIRECT_LONG, INDIRECT -> renderRegularCitation();
            case CITATION_OF_CITATION -> renderCitationOfCitation();
            case VERBAL -> renderVerbalCitation();
        };
    }

    private String renderRegularCitation() {
        CitationSource citationSource = source.orElseThrow();
        return switch (mode) {
            case PARENTHETICAL -> "(" + citationSource.parentheticalText(formatting) + renderEmphasis() + ")";
            case NARRATIVE -> citationSource.narrativeReferenceText(formatting) + renderNarrativeEmphasis();
        };
    }

    private String renderEmphasis() {
        if (emphasisOurs) return ", " + formatting.emphasisOursLabel();
        if (emphasisAuthor) return ", " + formatting.emphasisAuthorLabel();
        return "";
    }

    private String renderNarrativeEmphasis() {
        if (emphasisOurs) return " (" + formatting.emphasisOursLabel() + ")";
        if (emphasisAuthor) return " (" + formatting.emphasisAuthorLabel() + ")";
        return "";
    }

    private String renderVerbalCitation() {
        CitationSource citationSource = source.orElseThrow();
        String label = formatting.verbalCitationLabel();
        return switch (mode) {
            case PARENTHETICAL -> "(" + citationSource.authorText(formatting) + ", " + label + ")";
            case NARRATIVE -> citationSource.authorText(formatting) + " (" + label + ")";
        };
    }

    private String renderCitationOfCitation() {
        CitationSource original = originalSource.orElseThrow();
        CitationSource consulted = consultedSource.orElseThrow();
        String apudReference = original.authorText(formatting)
                + ", "
                + original.year()
                + formatting.apudConnector()
                + consulted.parentheticalText(formatting);

        return switch (mode) {
            case PARENTHETICAL -> "(" + apudReference + renderEmphasis() + ")";
            case NARRATIVE -> original.authorText(formatting)
                    + " ("
                    + original.year()
                    + formatting.apudConnector()
                    + consulted.parentheticalText(formatting)
                    + renderEmphasis()
                    + ")";
        };
    }

    private static void validateSources(
            BodyCitationType citationType,
            Optional<CitationSource> source,
            Optional<CitationSource> originalSource,
            Optional<CitationSource> consultedSource
    ) {
        switch (citationType) {
            case DIRECT_SHORT, DIRECT_LONG -> {
                CitationSource citationSource = source.orElseThrow(() ->
                        new IllegalArgumentException(citationType + " citation source must be provided.")
                );
                citationSource.requirePage(citationType.name());
                requireAbsent(originalSource, "originalSource");
                requireAbsent(consultedSource, "consultedSource");
            }
            case INDIRECT, VERBAL -> {
                if (source.isEmpty()) {
                    throw new IllegalArgumentException(citationType + " citation source must be provided.");
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
                consulted.requirePage(citationType.name());
            }
        }
    }

    private static void requireAbsent(Optional<CitationSource> source, String fieldName) {
        if (source.isPresent()) {
            throw new IllegalArgumentException(fieldName + " must not be provided for this citation type.");
        }
    }
}
