package com.abntbuilder.formatter.engine.model.profile.component.references;

import java.util.Objects;
import java.util.Optional;

public record AuthorFormatRule(
        boolean surnameUppercase,
        String surnameGivenSeparator,
        String nameTerminator,
        String multiAuthorJoiner,
        String etAlLabel,
        int etAlThreshold,
        Optional<String> lastAuthorJoiner,
        NameOrder nameOrder,
        boolean initialsOnly,
        boolean initialsDotted,
        boolean initialsSpaced
) {
    public enum NameOrder { SURNAME_FIRST, GIVEN_FIRST }

    public AuthorFormatRule {
        requireNonBlank(surnameGivenSeparator, "surnameGivenSeparator");
        requireNonBlank(nameTerminator, "nameTerminator");
        requireNonBlank(multiAuthorJoiner, "multiAuthorJoiner");
        requireNonBlank(etAlLabel, "etAlLabel");
        if (etAlThreshold < 1) throw new IllegalArgumentException("etAlThreshold must be >= 1");
        Objects.requireNonNull(lastAuthorJoiner, "lastAuthorJoiner must not be null");
        Objects.requireNonNull(nameOrder, "nameOrder must not be null");
    }

    public AuthorFormatRule(
            boolean surnameUppercase,
            String surnameGivenSeparator,
            String nameTerminator,
            String multiAuthorJoiner,
            String etAlLabel,
            int etAlThreshold
    ) {
        this(surnameUppercase, surnameGivenSeparator, nameTerminator, multiAuthorJoiner, etAlLabel,
                etAlThreshold, Optional.empty(), NameOrder.SURNAME_FIRST, false, false, false);
    }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
