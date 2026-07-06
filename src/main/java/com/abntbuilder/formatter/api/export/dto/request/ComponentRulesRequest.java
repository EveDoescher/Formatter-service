package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;

public record ComponentRulesRequest(
        @Valid SinglePageComponentRuleRequest cover,
        @Valid SinglePageComponentRuleRequest titlePage,
        @Valid SinglePageComponentRuleRequest approvalSheet,
        @Valid BodyContentComponentRuleRequest bodyContent,
        @Valid FlowTextualComponentRuleRequest errata,
        @Valid FlowTextualComponentRuleRequest dedication,
        @Valid FlowTextualComponentRuleRequest epigraph,
        @Valid FlowTextualComponentRuleRequest acknowledgments,
        @Valid FlowTextualComponentRuleRequest resumo,
        @JsonProperty("abstract") @Valid AbstractComponentRuleRequest abstractEn,
        @Valid ReferencesComponentRuleRequest references,
        @Valid AppendixComponentRuleRequest appendix,
        @Valid AnnexComponentRuleRequest annex,
        @Valid FlowTextualComponentRuleRequest glossary,
        @Valid SummaryComponentRuleRequest summary,
        @Valid IndexListComponentRuleRequest listOfFigures,
        @Valid IndexListComponentRuleRequest listOfTables,
        @Valid IndexListComponentRuleRequest listOfFrames,
        @Valid IndexListComponentRuleRequest listOfCharts,
        @Valid IndexListComponentRuleRequest listOfCodeListings,
        @Valid FlowTextualComponentRuleRequest listOfAbbreviations,
        @Valid FlowTextualComponentRuleRequest listOfSymbols
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
        if (summary != null) rules.add(summary.toDomain("summary"));
        if (listOfFigures != null) rules.add(listOfFigures.toDomain("listOfFigures"));
        if (listOfTables != null) rules.add(listOfTables.toDomain("listOfTables"));
        if (listOfFrames != null) rules.add(listOfFrames.toDomain("listOfFrames"));
        if (listOfCharts != null) rules.add(listOfCharts.toDomain("listOfCharts"));
        if (listOfCodeListings != null) rules.add(listOfCodeListings.toDomain("listOfCodeListings"));
        if (listOfAbbreviations != null) rules.add(listOfAbbreviations.toDomain());
        if (listOfSymbols != null) rules.add(listOfSymbols.toDomain());

        return List.copyOf(rules);
    }
}
