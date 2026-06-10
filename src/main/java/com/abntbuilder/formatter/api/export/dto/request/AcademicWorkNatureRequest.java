package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalSheetNature;
import com.abntbuilder.formatter.document.component.titlepage.TitlePageNature;

public record AcademicWorkNatureRequest(
        String workType,
        String degreeObjective,
        String courseName,
        String institutionName
) {

    public TitlePageNature toTitlePageNature() {
        return new TitlePageNature(workType, degreeObjective, courseName, institutionName);
    }

    public ApprovalSheetNature toApprovalSheetNature() {
        return new ApprovalSheetNature(workType, degreeObjective, courseName, institutionName);
    }

    public TitlePageNatureRequest toTitlePageNatureRequest() {
        return new TitlePageNatureRequest(workType, degreeObjective, courseName, institutionName);
    }

    public ApprovalSheetNatureRequest toApprovalSheetNatureRequest() {
        return new ApprovalSheetNatureRequest(workType, degreeObjective, courseName, institutionName);
    }
}
