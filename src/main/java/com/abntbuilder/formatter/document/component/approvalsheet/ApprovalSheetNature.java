package com.abntbuilder.formatter.document.component.approvalsheet;

public record ApprovalSheetNature(
        String workType,
        String degreeObjective,
        String courseName,
        String institutionName
) {

    public ApprovalSheetNature {
        requireNonBlank(workType, "workType");
        requireNonBlank(degreeObjective, "degreeObjective");
        requireNonBlank(courseName, "courseName");
        requireNonBlank(institutionName, "institutionName");
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
