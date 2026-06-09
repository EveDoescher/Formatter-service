package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalEvent;

import java.util.Optional;

public record ApprovalEventRequest(
        String location,
        String date,
        String approvalTextData
) {

    public ApprovalEvent toDomain() {
        return new ApprovalEvent(
                optional(location),
                optional(date),
                optional(approvalTextData)
        );
    }

    public boolean hasContent() {
        return location != null || date != null || approvalTextData != null;
    }

    private static Optional<String> optional(String value) {
        return value == null ? Optional.empty() : Optional.of(value);
    }
}
