package com.abntbuilder.formatter.document.component.approvalsheet;

import java.util.Optional;

public record ApprovalEvent(
        Optional<String> location,
        Optional<String> date,
        Optional<String> approvalTextData
) {

    public ApprovalEvent {
        location = validateOptional(location, "location");
        date = validateOptional(date, "date");
        approvalTextData = validateOptional(approvalTextData, "approvalTextData");
    }

    private static Optional<String> validateOptional(Optional<String> value, String fieldName) {
        if (value == null) {
            return Optional.empty();
        }

        value.ifPresent(text -> requireNonBlank(text, fieldName));

        return value;
    }

    public boolean hasContent() {
        return location.isPresent() || date.isPresent() || approvalTextData.isPresent();
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
