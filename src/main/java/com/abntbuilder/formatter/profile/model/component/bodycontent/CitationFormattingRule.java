package com.abntbuilder.formatter.profile.model.component.bodycontent;

public record CitationFormattingRule(
        String pagePrefix,
        String multiAuthorJoiner,
        String etAl,
        String apudConnector
) {

    public CitationFormattingRule {
        requireNonBlank(pagePrefix, "pagePrefix");
        requireNonBlank(multiAuthorJoiner, "multiAuthorJoiner");
        requireNonBlank(etAl, "etAl");
        requireNonBlank(apudConnector, "apudConnector");
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
