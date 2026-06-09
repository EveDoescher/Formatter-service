package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.approvalsheet.ApprovalSheetCommitteeMemberRule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ApprovalSheetCommitteeMemberRuleRequest(
        @NotBlank String signatureLine,
        @NotEmpty List<@NotBlank String> lines
) {

    public ApprovalSheetCommitteeMemberRule toDomain() {
        return new ApprovalSheetCommitteeMemberRule(signatureLine, lines);
    }
}
