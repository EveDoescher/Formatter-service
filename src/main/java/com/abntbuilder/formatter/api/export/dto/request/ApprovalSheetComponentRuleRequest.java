package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.approvalsheet.ApprovalSheetComponentRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ApprovalSheetComponentRuleRequest(
        @NotBlank String componentId,

        @Valid
        @NotNull
        ApprovalSheetStyleMappingRequest styleMapping,

        @Valid
        @NotNull
        ApprovalSheetTextTemplateRuleRequest textTemplates,

        @Valid
        @NotNull
        SinglePageLayoutRuleRequest layoutRule
) {

    public ApprovalSheetComponentRule toDomain() {
        return new ApprovalSheetComponentRule(
                componentId,
                styleMapping.toDomain(),
                textTemplates.toDomain(),
                layoutRule.toDomain()
        );
    }
}
