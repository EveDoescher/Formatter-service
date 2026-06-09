package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalSheetNature;
import jakarta.validation.constraints.NotBlank;

public record ApprovalSheetNatureRequest(
        @NotBlank String workType,
        @NotBlank String degreeObjective,
        @NotBlank String courseName,
        @NotBlank String institutionName
) {

    public ApprovalSheetNature toDomain() {
        return new ApprovalSheetNature(
                workType,
                degreeObjective,
                courseName,
                institutionName
        );
    }
}
