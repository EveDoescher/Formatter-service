package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.approvalsheet.ApprovalSheetTextTemplateRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ApprovalSheetTextTemplateRuleRequest(
        @NotBlank String natureTemplate,
        @NotBlank String approvalTextTemplate,
        @NotBlank String committeeHeadingTemplate,
        @Valid @NotNull ApprovalSheetCommitteeMemberRuleRequest committeeMemberTemplate
) {

    public ApprovalSheetTextTemplateRule toDomain() {
        return new ApprovalSheetTextTemplateRule(
                natureTemplate,
                approvalTextTemplate,
                committeeHeadingTemplate,
                committeeMemberTemplate.toDomain()
        );
    }
}
