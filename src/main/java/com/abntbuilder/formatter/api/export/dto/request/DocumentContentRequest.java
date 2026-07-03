package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.DocumentComponent;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.component.bodycontent.BodyContentComponentRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.CitationFormattingRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;

public record DocumentContentRequest(
        @Valid SinglePageContentRequest cover,
        @Valid SinglePageContentRequest titlePage,
        @Valid SinglePageContentRequest approvalSheet,
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
        @Valid GlossaryRequest glossary,
        @Valid SummaryRequest summary,
        @Valid ListOfFiguresRequest listOfFigures,
        @Valid ListOfTablesRequest listOfTables,
        @Valid ListOfFramesRequest listOfFrames,
        @Valid ListOfChartsRequest listOfCharts,
        @Valid ListOfCodeListingsRequest listOfCodeListings,
        @Valid ListOfAbbreviationsRequest listOfAbbreviations,
        @Valid ListOfSymbolsRequest listOfSymbols
) {
    public List<DocumentComponent> toComponents() {
        return toComponents(null);
    }

    public List<DocumentComponent> toComponents(DocumentProfile profile) {
        List<DocumentComponent> components = new ArrayList<>();
        ComponentRuleResolver ruleResolver = profile == null ? null : new ComponentRuleResolver(profile);

        if (cover != null && cover.hasSlots()) {
            components.add(cover.toDomain("cover"));
        }

        if (titlePage != null && titlePage.hasSlots()) {
            components.add(titlePage.toDomain("titlePage"));
        }

        if (errata != null) {
            components.add(errata.toDomain());
        }

        if (approvalSheet != null && approvalSheet.hasSlots()) {
            components.add(approvalSheet.toDomain("approvalSheet"));
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

        if (listOfAbbreviations != null) {
            components.add(listOfAbbreviations.toDomain());
        }

        if (listOfSymbols != null) {
            components.add(listOfSymbols.toDomain());
        }

        if (summary != null) {
            components.add(summary.toDomain());
        }

        if (listOfFigures != null) {
            components.add(listOfFigures.toDomain());
        }

        if (listOfTables != null) {
            components.add(listOfTables.toDomain());
        }

        if (listOfFrames != null) {
            components.add(listOfFrames.toDomain());
        }

        if (listOfCharts != null) {
            components.add(listOfCharts.toDomain());
        }

        if (listOfCodeListings != null) {
            components.add(listOfCodeListings.toDomain());
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
