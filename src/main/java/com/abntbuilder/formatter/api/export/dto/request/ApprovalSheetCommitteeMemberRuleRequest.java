package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.approvalsheet.ApprovalSheetCommitteeMemberRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ApprovalSheetCommitteeMemberRuleRequest(
        @Valid @NotNull ApprovalSheetSignatureLineRuleRequest signatureLine,
        @NotEmpty List<@NotBlank String> lineTemplates
) {

    public ApprovalSheetCommitteeMemberRule toDomain() {
        return new ApprovalSheetCommitteeMemberRule(signatureLine.toDomain(), lineTemplates);
    }
}
