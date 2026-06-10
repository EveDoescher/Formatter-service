package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.ComponentContentBindings;
import com.abntbuilder.formatter.profile.model.component.approvalsheet.ApprovalSheetComponentRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record ApprovalSheetComponentRuleRequest(
        @NotBlank String componentId,
        Map<@NotBlank String, @NotBlank String> contentBindings,

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
                new ComponentContentBindings(contentBindings == null ? Map.of() : contentBindings),
                styleMapping.toDomain(),
                textTemplates.toDomain(),
                layoutRule.toDomain()
        );
    }
}
