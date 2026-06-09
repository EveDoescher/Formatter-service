package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.approvalsheet.ApprovalSheetStyleMapping;
import jakarta.validation.constraints.NotBlank;

public record ApprovalSheetStyleMappingRequest(
        @NotBlank String authorsStyleId,
        @NotBlank String titleStyleId,
        @NotBlank String subtitleStyleId,
        @NotBlank String natureStyleId,
        @NotBlank String approvalTextStyleId,
        @NotBlank String committeeHeadingStyleId,
        @NotBlank String committeeMembersStyleId
) {

    public ApprovalSheetStyleMapping toDomain() {
        return new ApprovalSheetStyleMapping(
                authorsStyleId,
                titleStyleId,
                subtitleStyleId,
                natureStyleId,
                approvalTextStyleId,
                committeeHeadingStyleId,
                committeeMembersStyleId
        );
    }
}
