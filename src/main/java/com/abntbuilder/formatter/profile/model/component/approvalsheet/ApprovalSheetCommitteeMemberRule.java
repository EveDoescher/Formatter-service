package com.abntbuilder.formatter.profile.model.component.approvalsheet;

import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

import java.util.List;
import java.util.Objects;

public record ApprovalSheetCommitteeMemberRule(
        String signatureLine,
        List<String> lines
) {

    public ApprovalSheetCommitteeMemberRule {
        requireNonBlank(signatureLine, "signatureLine");
        Objects.requireNonNull(lines, "lines must not be null");

        if (lines.isEmpty()) {
            throw new InvalidProfileStructureException("lines must not be empty.");
        }

        lines = List.copyOf(lines);

        for (String line : lines) {
            requireNonBlank(line, "lines item");
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidProfileStructureException(fieldName + " must not be blank.");
        }
    }
}
