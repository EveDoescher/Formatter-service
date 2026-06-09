package com.abntbuilder.formatter.document.component.approvalsheet;

import java.util.Optional;

public record ApprovalCommitteeMember(
        String name,
        Optional<String> title,
        Optional<String> institutionName,
        Optional<String> role
) {

    public ApprovalCommitteeMember {
        requireNonBlank(name, "name");
        title = validateOptional(title, "title");
        institutionName = validateOptional(institutionName, "institutionName");
        role = validateOptional(role, "role");
    }

    private static Optional<String> validateOptional(Optional<String> value, String fieldName) {
        if (value == null) {
            return Optional.empty();
        }

        value.ifPresent(text -> requireNonBlank(text, fieldName));

        return value;
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
