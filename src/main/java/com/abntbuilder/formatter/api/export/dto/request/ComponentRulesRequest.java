package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;

public record ComponentRulesRequest(
        @Valid CoverComponentRuleRequest cover,
        @Valid TitlePageComponentRuleRequest titlePage,
        @Valid ApprovalSheetComponentRuleRequest approvalSheet,
        @Valid BodyContentComponentRuleRequest bodyContent,
        @Valid ErrataComponentRuleRequest errata,
        @Valid DedicationComponentRuleRequest dedication,
        @Valid EpigraphComponentRuleRequest epigraph,
        @Valid AcknowledgmentsComponentRuleRequest acknowledgments,
        @Valid ResumoComponentRuleRequest resumo,
        @Valid AbstractComponentRuleRequest abstractEn,
        @Valid ReferencesComponentRuleRequest references,
        @Valid AppendixComponentRuleRequest appendix,
        @Valid AnnexComponentRuleRequest annex,
        @Valid GlossaryComponentRuleRequest glossary
) {
    public List<ComponentRule> toDomain() {
        List<ComponentRule> rules = new ArrayList<>();

        if (cover != null) rules.add(cover.toDomain());
        if (titlePage != null) rules.add(titlePage.toDomain());
        if (approvalSheet != null) rules.add(approvalSheet.toDomain());
        if (bodyContent != null) rules.add(bodyContent.toDomain());
        if (errata != null) rules.add(errata.toDomain());
        if (dedication != null) rules.add(dedication.toDomain());
        if (epigraph != null) rules.add(epigraph.toDomain());
        if (acknowledgments != null) rules.add(acknowledgments.toDomain());
        if (resumo != null) rules.add(resumo.toDomain());
        if (abstractEn != null) rules.add(abstractEn.toDomain());
        if (references != null) rules.add(references.toDomain());
        if (appendix != null) rules.add(appendix.toDomain());
        if (annex != null) rules.add(annex.toDomain());
        if (glossary != null) rules.add(glossary.toDomain());

        return List.copyOf(rules);
    }
}
