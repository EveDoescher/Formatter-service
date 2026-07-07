package com.abntbuilder.formatter.engine.model.profile.component.bodycontent;

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
        boolean etAlItalic
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
                authorYearSeparator, pageReferenceSeparator, parenOpen, parenClose, false);
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
