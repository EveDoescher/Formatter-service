package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.DocumentComponent;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.component.approvalsheet.ApprovalSheetComponentRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.BodyContentComponentRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.CitationFormattingRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverComponentRule;
import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;

public record DocumentContentRequest(
        @Valid CoverRequest cover,
        @Valid TitlePageRequest titlePage,
        @Valid ApprovalSheetRequest approvalSheet,
        @Valid BodyContentRequest bodyContent,
        @Valid ErrataRequest errata,
        @Valid DedicationRequest dedication,
        @Valid EpigraphRequest epigraph,
        @Valid AcknowledgmentsRequest acknowledgments,
        @Valid ResumoRequest resumo,
        @JsonProperty("abstract") @Valid AbstractRequest abstractEn,
        @Valid ReferencesRequest references,
        @Valid AppendixRequest appendix,
        @Valid AnnexRequest annex,
        @Valid GlossaryRequest glossary
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

        if (errata != null) {
            components.add(errata.toDomain());
        }

        if (approvalSheet != null) {
            components.add(ruleResolver == null
                    ? approvalSheet.toDomain()
                    : approvalSheet.toDomain(
                            work,
                            ruleResolver.resolve("approvalSheet", ApprovalSheetComponentRule.class).contentBindings()
                    ));
        }

        if (dedication != null) {
            components.add(dedication.toDomain());
        }

        if (epigraph != null) {
            components.add(epigraph.toDomain());
        }

        if (acknowledgments != null) {
            components.add(acknowledgments.toDomain());
        }

        if (resumo != null) {
            components.add(resumo.toDomain());
        }

        if (abstractEn != null) {
            components.add(abstractEn.toDomain());
        }

        if (bodyContent != null || appendix != null || annex != null) {
            CitationFormattingRule citationFormatting = ruleResolver == null ? null
                    : ruleResolver.resolve("bodyContent", BodyContentComponentRule.class).citationFormatting();

            if (bodyContent != null) {
                components.add(bodyContent.toDomain(citationFormatting));
            }

            if (appendix != null) {
                components.add(appendix.toDomain(citationFormatting));
            }

            if (annex != null) {
                components.add(annex.toDomain(citationFormatting));
            }
        }

        if (references != null) {
            components.add(references.toDomain());
        }

        if (glossary != null) {
            components.add(glossary.toDomain());
        }

        return List.copyOf(components);
    }
}
