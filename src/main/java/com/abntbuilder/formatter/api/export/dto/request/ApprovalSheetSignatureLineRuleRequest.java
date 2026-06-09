package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.approvalsheet.ApprovalSheetSignatureLineRule;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public record ApprovalSheetSignatureLineRuleRequest(
        @NotNull Boolean enabled,
        String text
) {

    public ApprovalSheetSignatureLineRule toDomain() {
        return new ApprovalSheetSignatureLineRule(
                Objects.requireNonNull(enabled, "signatureLine.enabled must not be null"),
                text
        );
    }
}
