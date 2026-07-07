package com.abntbuilder.formatter.engine.model.profile.component.bodycontent;

import java.util.Optional;

public record CitationFormattingRule(
        String pagePrefix,
        String multiAuthorJoiner,
        String etAl,
        String apudConnector,
        String suppressionMarker,
        String emphasisOursLabel,
        String emphasisAuthorLabel,
        String verbalCitationLabel,
        String authorYearSeparator,
        String pageReferenceSeparator,
        String parenOpen,
        String parenClose,
        boolean etAlItalic,
        Optional<String> numericPrefix,
        Optional<String> numericSuffix,
        Optional<String> numericRangeSeparator,
        Optional<String> numericListSeparator,
        boolean numericRangeCollapse,
        String multiSourceSeparator
) {

    public CitationFormattingRule {
        requireNonBlank(pagePrefix, "pagePrefix");
        requireNonBlank(multiAuthorJoiner, "multiAuthorJoiner");
        requireNonBlank(etAl, "etAl");
        requireNonBlank(apudConnector, "apudConnector");
        requireNonBlank(suppressionMarker, "suppressionMarker");
        requireNonBlank(emphasisOursLabel, "emphasisOursLabel");
        requireNonBlank(emphasisAuthorLabel, "emphasisAuthorLabel");
        requireNonBlank(verbalCitationLabel, "verbalCitationLabel");
        if (authorYearSeparator == null) authorYearSeparator = ", ";
        if (pageReferenceSeparator == null) pageReferenceSeparator = ", ";
        if (parenOpen == null) parenOpen = "(";
        if (parenClose == null) parenClose = ")";
        if (numericPrefix == null) numericPrefix = Optional.of("(");
        if (numericSuffix == null) numericSuffix = Optional.of(")");
        if (numericRangeSeparator == null) numericRangeSeparator = Optional.of("–");
        if (numericListSeparator == null) numericListSeparator = Optional.of(",");
        if (multiSourceSeparator == null) multiSourceSeparator = "; ";
    }

    public CitationFormattingRule(
            String pagePrefix,
            String multiAuthorJoiner,
            String etAl,
            String apudConnector,
            String suppressionMarker,
            String emphasisOursLabel,
            String emphasisAuthorLabel,
            String verbalCitationLabel,
            String authorYearSeparator,
            String pageReferenceSeparator,
            String parenOpen,
            String parenClose
    ) {
        this(pagePrefix, multiAuthorJoiner, etAl, apudConnector, suppressionMarker,
                emphasisOursLabel, emphasisAuthorLabel, verbalCitationLabel,
                authorYearSeparator, pageReferenceSeparator, parenOpen, parenClose, false,
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), false, null);
    }

    public CitationFormattingRule(
            String pagePrefix,
            String multiAuthorJoiner,
            String etAl,
            String apudConnector,
            String suppressionMarker,
            String emphasisOursLabel,
            String emphasisAuthorLabel,
            String verbalCitationLabel,
            String authorYearSeparator,
            String pageReferenceSeparator,
            String parenOpen,
            String parenClose,
            boolean etAlItalic
    ) {
        this(pagePrefix, multiAuthorJoiner, etAl, apudConnector, suppressionMarker,
                emphasisOursLabel, emphasisAuthorLabel, verbalCitationLabel,
                authorYearSeparator, pageReferenceSeparator, parenOpen, parenClose, etAlItalic,
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), false, null);
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
