package com.abntbuilder.formatter.profile.model.component.references;

public record ReferencesFormattingRule(
        String availableAtLabel,
        String accessedAtLabel,
        String etAlLabel,
        String inLabel,
        String authorSurnameGivenSeparator,
        String authorNameTerminator,
        String multiAuthorJoiner,
        boolean authorSurnameUppercase
) {
    public ReferencesFormattingRule {
        requireNonBlank(availableAtLabel, "availableAtLabel");
        requireNonBlank(accessedAtLabel, "accessedAtLabel");
        requireNonBlank(etAlLabel, "etAlLabel");
        requireNonBlank(inLabel, "inLabel");
        requireNonBlank(authorSurnameGivenSeparator, "authorSurnameGivenSeparator");
        requireNonBlank(authorNameTerminator, "authorNameTerminator");
        requireNonBlank(multiAuthorJoiner, "multiAuthorJoiner");
    }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
