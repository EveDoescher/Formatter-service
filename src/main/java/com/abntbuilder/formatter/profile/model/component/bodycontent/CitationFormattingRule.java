package com.abntbuilder.formatter.profile.model.component.bodycontent;

public record CitationFormattingRule(
        String pagePrefix,
        String multiAuthorJoiner,
        String etAl,
        String apudConnector,
        String suppressionMarker,
        String emphasisOursLabel,
        String emphasisAuthorLabel,
        String verbalCitationLabel
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
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
