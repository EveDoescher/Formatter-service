package com.abntbuilder.formatter.profile.model.component.approvalsheet;

import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

public record ApprovalSheetSignatureLineRule(
        boolean enabled,
        String text
) {

    public ApprovalSheetSignatureLineRule {
        if (enabled && (text == null || text.isBlank())) {
            throw new InvalidProfileStructureException("signatureLine.text must not be blank.");
        }
    }
}
