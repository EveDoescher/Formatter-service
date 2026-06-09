package com.abntbuilder.formatter.profile.model.component.approvalsheet;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutRule;

import java.util.Objects;

public record ApprovalSheetComponentRule(
        String componentId,
        ApprovalSheetStyleMapping styleMapping,
        ApprovalSheetTextTemplateRule textTemplates,
        SinglePageLayoutRule layoutRule
) implements ComponentRule {

    public ApprovalSheetComponentRule {
        requireNonBlank(componentId, "componentId");
        Objects.requireNonNull(styleMapping, "styleMapping must not be null");
        Objects.requireNonNull(textTemplates, "textTemplates must not be null");
        Objects.requireNonNull(layoutRule, "layoutRule must not be null");
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
