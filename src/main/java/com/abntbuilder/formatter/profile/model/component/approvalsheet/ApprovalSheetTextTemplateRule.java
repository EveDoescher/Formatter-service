package com.abntbuilder.formatter.profile.model.component.approvalsheet;

import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

public record ApprovalSheetTextTemplateRule(
        String natureTemplate,
        String approvalTextTemplate,
        String committeeHeadingTemplate,
        ApprovalSheetCommitteeMemberRule committeeMemberTemplate
) {

    public ApprovalSheetTextTemplateRule {
        requireNonBlank(natureTemplate, "natureTemplate");
        requireNonBlank(approvalTextTemplate, "approvalTextTemplate");
        requireNonBlank(committeeHeadingTemplate, "committeeHeadingTemplate");
        if (committeeMemberTemplate == null) {
            throw new InvalidProfileStructureException("committeeMemberTemplate must not be null.");
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidProfileStructureException(fieldName + " must not be blank.");
        }
    }
}
