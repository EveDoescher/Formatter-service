package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.DocumentComponent;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.component.approvalsheet.ApprovalSheetComponentRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.BodyContentComponentRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.CitationFormattingRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverComponentRule;
import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;

public record DocumentContentRequest(
        @Valid CoverRequest cover,
        @Valid TitlePageRequest titlePage,
        @Valid ApprovalSheetRequest approvalSheet,
        @Valid BodyContentRequest bodyContent
) {
    public List<DocumentComponent> toComponents() {
        return toComponents(null, null);
    }

    public List<DocumentComponent> toComponents(AcademicWorkRequest work, DocumentProfile profile) {
        List<DocumentComponent> components = new ArrayList<>();
        ComponentRuleResolver ruleResolver = profile == null ? null : new ComponentRuleResolver(profile);

        if (cover != null) {
            components.add(ruleResolver == null
                    ? cover.toDomain()
                    : cover.toDomain(
                            work,
                            ruleResolver.resolve("cover", CoverComponentRule.class).contentBindings()
                    ));
        }

        if (titlePage != null) {
            components.add(ruleResolver == null
                    ? titlePage.toDomain()
                    : titlePage.toDomain(
                            work,
                            ruleResolver.resolve("titlePage", TitlePageComponentRule.class).contentBindings()
                    ));
        }

        if (approvalSheet != null) {
            components.add(ruleResolver == null
                    ? approvalSheet.toDomain()
                    : approvalSheet.toDomain(
                            work,
                            ruleResolver.resolve("approvalSheet", ApprovalSheetComponentRule.class).contentBindings()
                    ));
        }

        if (bodyContent != null) {
            CitationFormattingRule citationFormatting = ruleResolver == null ? null
                    : ruleResolver.resolve("bodyContent", BodyContentComponentRule.class).citationFormatting();
            components.add(bodyContent.toDomain(citationFormatting));
        }

        return List.copyOf(components);
    }
}
