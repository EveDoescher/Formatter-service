package com.abntbuilder.formatter.profile.model.component.singlepage;

import java.util.List;
import java.util.Objects;

public record SignatureBlockListSlotRule(
        boolean required,
        boolean signatureLineEnabled,
        String signatureLineText,
        List<String> lineTemplates,
        List<String> knownFieldNames
) implements SlotRule {

    public SignatureBlockListSlotRule {
        if (signatureLineEnabled && (signatureLineText == null || signatureLineText.isBlank())) {
            throw new IllegalArgumentException(
                    "SignatureBlockListSlotRule.signatureLineText must not be blank when signatureLineEnabled is true.");
        }
        Objects.requireNonNull(lineTemplates, "SignatureBlockListSlotRule.lineTemplates must not be null.");
        if (lineTemplates.isEmpty()) {
            throw new IllegalArgumentException("SignatureBlockListSlotRule.lineTemplates must not be empty.");
        }
        Objects.requireNonNull(knownFieldNames, "SignatureBlockListSlotRule.knownFieldNames must not be null.");
        lineTemplates = List.copyOf(lineTemplates);
        knownFieldNames = List.copyOf(knownFieldNames);
    }
}
