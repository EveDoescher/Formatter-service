package com.abntbuilder.formatter.profile.model.component.approvalsheet;

import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

import java.util.List;
import java.util.Objects;

public record ApprovalSheetCommitteeMemberRule(
        ApprovalSheetSignatureLineRule signatureLine,
        List<String> lineTemplates
) {

    public ApprovalSheetCommitteeMemberRule {
        Objects.requireNonNull(signatureLine, "signatureLine must not be null");
        Objects.requireNonNull(lineTemplates, "lineTemplates must not be null");

        if (lineTemplates.isEmpty()) {
            throw new InvalidProfileStructureException("lineTemplates must not be empty.");
        }

        lineTemplates = List.copyOf(lineTemplates);

        for (String lineTemplate : lineTemplates) {
            requireNonBlank(lineTemplate, "lineTemplates item");
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidProfileStructureException(fieldName + " must not be blank.");
        }
    }
}
