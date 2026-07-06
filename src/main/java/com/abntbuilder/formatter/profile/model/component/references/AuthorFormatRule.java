package com.abntbuilder.formatter.profile.model.component.references;

public record AuthorFormatRule(
        boolean surnameUppercase,
        String surnameGivenSeparator,
        String nameTerminator,
        String multiAuthorJoiner,
        String etAlLabel,
        int etAlThreshold
) {
    public AuthorFormatRule {
        requireNonBlank(surnameGivenSeparator, "surnameGivenSeparator");
        requireNonBlank(nameTerminator, "nameTerminator");
        requireNonBlank(multiAuthorJoiner, "multiAuthorJoiner");
        requireNonBlank(etAlLabel, "etAlLabel");
        if (etAlThreshold < 1) throw new IllegalArgumentException("etAlThreshold must be >= 1");
    }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
