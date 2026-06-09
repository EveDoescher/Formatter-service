package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalCommitteeMember;
import jakarta.validation.constraints.NotBlank;

import java.util.Optional;

public record ApprovalCommitteeMemberRequest(
        @NotBlank String name,
        String title,
        String institutionName,
        String role
) {

    public ApprovalCommitteeMember toDomain() {
        return new ApprovalCommitteeMember(
                name,
                optional(title),
                optional(institutionName),
                optional(role)
        );
    }

    private static Optional<String> optional(String value) {
        return value == null ? Optional.empty() : Optional.of(value);
    }
}
