package com.abntbuilder.formatter.profile.provider;

import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.PageOrientation;
import com.abntbuilder.formatter.profile.model.PageNumberingPlacement;
import com.abntbuilder.formatter.profile.model.PageNumberingRule;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.StyleType;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.profile.model.component.ComponentContentBindings;
import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import com.abntbuilder.formatter.profile.model.component.approvalsheet.ApprovalSheetCommitteeMemberRule;
import com.abntbuilder.formatter.profile.model.component.approvalsheet.ApprovalSheetComponentRule;
import com.abntbuilder.formatter.profile.model.component.approvalsheet.ApprovalSheetSignatureLineRule;
import com.abntbuilder.formatter.profile.model.component.approvalsheet.ApprovalSheetStyleMapping;
import com.abntbuilder.formatter.profile.model.component.approvalsheet.ApprovalSheetTextTemplateRule;
import com.abntbuilder.formatter.profile.model.component.abstracten.AbstractComponentRule;
import com.abntbuilder.formatter.profile.model.component.acknowledgments.AcknowledgmentsComponentRule;
import com.abntbuilder.formatter.profile.model.component.annex.AnnexComponentRule;
import com.abntbuilder.formatter.profile.model.component.appendix.AppendixComponentRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.BodyContentComponentRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.BodyContentLayoutRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.BodyContentNumberingRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.BodyContentStyleMapping;
import com.abntbuilder.formatter.profile.model.component.bodycontent.DisplayObjectContinuationLabels;
import com.abntbuilder.formatter.profile.model.component.bodycontent.DisplayObjectSourcePlacement;
import com.abntbuilder.formatter.profile.model.component.bodycontent.FigureRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.FrameRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.CodeListingRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.ChartRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.ImageFitPolicy;
import com.abntbuilder.formatter.profile.model.component.bodycontent.NumberingStrategy;
import com.abntbuilder.formatter.profile.model.component.bodycontent.TableRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverComponentRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverLayoutRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverStyleMapping;
import com.abntbuilder.formatter.profile.model.component.dedication.DedicationComponentRule;
import com.abntbuilder.formatter.profile.model.component.epigraph.EpigraphComponentRule;
import com.abntbuilder.formatter.profile.model.component.errata.ErrataComponentRule;
import com.abntbuilder.formatter.profile.model.component.glossary.GlossaryComponentRule;
import com.abntbuilder.formatter.profile.model.component.indexlist.IndexListComponentRule;
import com.abntbuilder.formatter.profile.model.component.listofabbreviations.ListOfAbbreviationsComponentRule;
import com.abntbuilder.formatter.profile.model.component.listofsymbols.ListOfSymbolsComponentRule;
import com.abntbuilder.formatter.profile.model.component.references.ReferencesComponentRule;
import com.abntbuilder.formatter.profile.model.component.summary.SummaryComponentRule;
import com.abntbuilder.formatter.profile.model.component.references.ReferencesFormattingRule;
import com.abntbuilder.formatter.profile.model.component.resumo.ResumoComponentRule;
import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageComponentRule;
import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageStyleMapping;
import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageTextTemplateRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.HorizontalPlacementRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.HorizontalPlacementStrategy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.LayoutGapRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageAnchorStrategy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageGroupRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageItemRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutPolicy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLineHeightStrategy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageSafetyPolicyId;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SpacerStylePolicy;
import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record ProfileDefinition(
        String id,
        String displayName,
        PageRuleDefinition pageRule,
        PageNumberingRuleDefinition pageNumbering,
        List<StyleRuleDefinition> styleRules,
        ComponentRulesDefinition componentRules,
        List<String> componentOrder
) {

    public DocumentProfile toDomain() {
        requireNonNull(pageRule, "pageRule");
        requireNonEmpty(styleRules, "styleRules");
        requireNonNull(componentRules, "componentRules");
        requireNonEmpty(componentOrder, "componentOrder");

        List<ComponentRule> resolvedComponentRules = componentRules.toDomain();

        return new DocumentProfile(
                id,
                displayName,
                pageRule.toDomain(),
                Optional.ofNullable(pageNumbering).map(PageNumberingRuleDefinition::toDomain),
                styleRules.stream()
                        .map(StyleRuleDefinition::toDomain)
                        .toList(),
                resolvedComponentRules,
                componentOrder
        );
    }

    public record PageRuleDefinition(
            BigDecimal widthCm,
            BigDecimal heightCm,
            BigDecimal marginTopCm,
            BigDecimal marginRightCm,
            BigDecimal marginBottomCm,
            BigDecimal marginLeftCm,
            PageOrientation orientation
    ) {
        PageRule toDomain() {
            return new PageRule(
                    widthCm,
                    heightCm,
                    marginTopCm,
                    marginRightCm,
                    marginBottomCm,
                    marginLeftCm,
                    orientation
            );
        }
    }

    public record PageNumberingRuleDefinition(
            Boolean enabled,
            String countFromComponentId,
            String visibleFromComponentId,
            String styleId,
            PageNumberingPlacement placement,
            BigDecimal verticalDistanceFromPageEdgeCm,
            BigDecimal horizontalDistanceFromPageEdgeCm
    ) {
        PageNumberingRule toDomain() {
            requireNonNull(enabled, "pageNumbering.enabled");

            return new PageNumberingRule(
                    enabled,
                    countFromComponentId,
                    visibleFromComponentId,
                    styleId,
                    placement,
                    verticalDistanceFromPageEdgeCm,
                    horizontalDistanceFromPageEdgeCm
            );
        }
    }

    public record StyleRuleDefinition(
            String id,
            StyleType type,
            String fontFamily,
            BigDecimal fontSizePt,
            TextAlignment alignment,
            BigDecimal lineSpacing,
            BigDecimal firstLineIndentCm,
            BigDecimal leftIndentCm,
            BigDecimal rightIndentCm,
            BigDecimal spacingBeforePt,
            BigDecimal spacingAfterPt,
            Boolean bold,
            Boolean italic,
            Boolean uppercase
    ) {
        StyleRule toDomain() {
            requireNonNull(bold, "style.bold");
            requireNonNull(italic, "style.italic");
            requireNonNull(uppercase, "style.uppercase");

            return new StyleRule(
                    id,
                    type,
                    fontFamily,
                    fontSizePt,
                    alignment,
                    lineSpacing,
                    firstLineIndentCm,
                    leftIndentCm,
                    rightIndentCm,
                    spacingBeforePt,
                    spacingAfterPt,
                    bold,
                    italic,
                    uppercase
            );
        }
    }

    public record ComponentRulesDefinition(
            CoverComponentRuleDefinition cover,
            TitlePageComponentRuleDefinition titlePage,
            ApprovalSheetComponentRuleDefinition approvalSheet,
            BodyContentComponentRuleDefinition bodyContent,
            ErrataComponentRuleDefinition errata,
            DedicationComponentRuleDefinition dedication,
            EpigraphComponentRuleDefinition epigraph,
            AcknowledgmentsComponentRuleDefinition acknowledgments,
            ResumoComponentRuleDefinition resumo,
            @JsonProperty("abstract") AbstractComponentRuleDefinition abstractEn,
            ReferencesComponentRuleDefinition references,
            AppendixComponentRuleDefinition appendix,
            AnnexComponentRuleDefinition annex,
            GlossaryComponentRuleDefinition glossary,
            SummaryComponentRuleDefinition summary,
            IndexListComponentRuleDefinition listOfFigures,
            IndexListComponentRuleDefinition listOfTables,
            IndexListComponentRuleDefinition listOfFrames,
            IndexListComponentRuleDefinition listOfCharts,
            IndexListComponentRuleDefinition listOfCodeListings,
            ListOfAbbreviationsComponentRuleDefinition listOfAbbreviations,
            ListOfSymbolsComponentRuleDefinition listOfSymbols
    ) {
        List<ComponentRule> toDomain() {
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
            if (summary != null) rules.add(summary.toDomain());
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

    public record CoverComponentRuleDefinition(
            String componentId,
            Map<String, String> contentBindings,
            CoverStyleMappingDefinition styleMapping,
            CoverLayoutRuleDefinition layoutRule
    ) {
        CoverComponentRule toDomain() {
            requireNonNull(styleMapping, "cover.styleMapping");
            requireNonNull(layoutRule, "cover.layoutRule");

            return new CoverComponentRule(
                    componentId,
                    createContentBindings(contentBindings),
                    styleMapping.toDomain(),
                    layoutRule.toDomain()
            );
        }
    }

    public record CoverStyleMappingDefinition(
            String institutionalLinesStyleId,
            String authorsStyleId,
            String titleStyleId,
            String subtitleStyleId,
            String cityStyleId,
            String yearStyleId
    ) {
        CoverStyleMapping toDomain() {
            return new CoverStyleMapping(
                    institutionalLinesStyleId,
                    authorsStyleId,
                    titleStyleId,
                    subtitleStyleId,
                    cityStyleId,
                    yearStyleId
            );
        }
    }

    public record CoverLayoutRuleDefinition(
            List<SinglePageGroupRuleDefinition> groups,
            List<LayoutGapRuleDefinition> gapRules,
            SinglePageLayoutPolicyDefinition policy
    ) {
        CoverLayoutRule toDomain() {
            requireNonEmpty(groups, "cover.layoutRule.groups");
            requireNonNull(gapRules, "cover.layoutRule.gapRules");
            requireNonNull(policy, "cover.layoutRule.policy");

            return new CoverLayoutRule(
                    groups.stream()
                            .map(SinglePageGroupRuleDefinition::toDomain)
                            .toList(),
                    gapRules.stream()
                            .map(LayoutGapRuleDefinition::toDomain)
                            .toList(),
                    policy.toDomain()
            );
        }
    }

    public record TitlePageComponentRuleDefinition(
            String componentId,
            Map<String, String> contentBindings,
            TitlePageStyleMappingDefinition styleMapping,
            TitlePageTextTemplateRuleDefinition textTemplates,
            SinglePageLayoutRuleDefinition layoutRule
    ) {
        TitlePageComponentRule toDomain() {
            requireNonNull(styleMapping, "titlePage.styleMapping");
            requireNonNull(textTemplates, "titlePage.textTemplates");
            requireNonNull(layoutRule, "titlePage.layoutRule");

            return new TitlePageComponentRule(
                    componentId,
                    createContentBindings(contentBindings),
                    styleMapping.toDomain(),
                    textTemplates.toDomain(),
                    layoutRule.toDomain()
            );
        }
    }

    public record TitlePageStyleMappingDefinition(
            String authorsStyleId,
            String titleStyleId,
            String subtitleStyleId,
            String natureStyleId,
            String advisorStyleId,
            String coadvisorStyleId,
            String cityStyleId,
            String yearStyleId
    ) {
        TitlePageStyleMapping toDomain() {
            return new TitlePageStyleMapping(
                    authorsStyleId,
                    titleStyleId,
                    subtitleStyleId,
                    natureStyleId,
                    advisorStyleId,
                    coadvisorStyleId,
                    cityStyleId,
                    yearStyleId
            );
        }
    }

    public record TitlePageTextTemplateRuleDefinition(
            String natureTemplate,
            String advisorTemplate,
            String coadvisorTemplate
    ) {
        TitlePageTextTemplateRule toDomain() {
            return new TitlePageTextTemplateRule(
                    natureTemplate,
                    advisorTemplate,
                    coadvisorTemplate
            );
        }
    }

    public record ApprovalSheetComponentRuleDefinition(
            String componentId,
            Map<String, String> contentBindings,
            ApprovalSheetStyleMappingDefinition styleMapping,
            ApprovalSheetTextTemplateRuleDefinition textTemplates,
            SinglePageLayoutRuleDefinition layoutRule
    ) {
        ApprovalSheetComponentRule toDomain() {
            requireNonNull(styleMapping, "approvalSheet.styleMapping");
            requireNonNull(textTemplates, "approvalSheet.textTemplates");
            requireNonNull(layoutRule, "approvalSheet.layoutRule");

            return new ApprovalSheetComponentRule(
                    componentId,
                    createContentBindings(contentBindings),
                    styleMapping.toDomain(),
                    textTemplates.toDomain(),
                    layoutRule.toDomain()
            );
        }
    }

    public record ApprovalSheetStyleMappingDefinition(
            String authorsStyleId,
            String titleStyleId,
            String subtitleStyleId,
            String natureStyleId,
            String approvalTextStyleId,
            String committeeHeadingStyleId,
            String committeeMembersStyleId
    ) {
        ApprovalSheetStyleMapping toDomain() {
            return new ApprovalSheetStyleMapping(
                    authorsStyleId,
                    titleStyleId,
                    subtitleStyleId,
                    natureStyleId,
                    approvalTextStyleId,
                    committeeHeadingStyleId,
                    committeeMembersStyleId
            );
        }
    }

    public record ApprovalSheetTextTemplateRuleDefinition(
            String natureTemplate,
            String approvalTextTemplate,
            String committeeHeadingTemplate,
            ApprovalSheetCommitteeMemberRuleDefinition committeeMemberTemplate
    ) {
        ApprovalSheetTextTemplateRule toDomain() {
            requireNonNull(committeeMemberTemplate, "approvalSheet.textTemplates.committeeMemberTemplate");

            return new ApprovalSheetTextTemplateRule(
                    natureTemplate,
                    approvalTextTemplate,
                    committeeHeadingTemplate,
                    committeeMemberTemplate.toDomain()
            );
        }
    }

    public record ApprovalSheetCommitteeMemberRuleDefinition(
            ApprovalSheetSignatureLineRuleDefinition signatureLine,
            List<String> lineTemplates
    ) {
        ApprovalSheetCommitteeMemberRule toDomain() {
            requireNonNull(signatureLine, "approvalSheet.textTemplates.committeeMemberTemplate.signatureLine");
            requireNonEmpty(lineTemplates, "approvalSheet.textTemplates.committeeMemberTemplate.lineTemplates");

            return new ApprovalSheetCommitteeMemberRule(signatureLine.toDomain(), lineTemplates);
        }
    }

    public record ApprovalSheetSignatureLineRuleDefinition(
            Boolean enabled,
            String text
    ) {
        ApprovalSheetSignatureLineRule toDomain() {
            requireNonNull(enabled, "approvalSheet.textTemplates.committeeMemberTemplate.signatureLine.enabled");

            return new ApprovalSheetSignatureLineRule(enabled, text);
        }
    }

    public record BodyContentComponentRuleDefinition(
            String componentId,
            BodyContentStyleMappingDefinition styleMapping,
            BodyContentNumberingRuleDefinition numbering,
            BodyContentLayoutRuleDefinition layout,
            FigureRuleDefinition figure,
            TableRuleDefinition table,
            FrameRuleDefinition frame,
            CodeListingRuleDefinition codeListing,
            ChartRuleDefinition chart,
            CitationFormattingRuleDefinition citationFormatting,
            CrossReferenceLabelsRuleDefinition crossReferenceLabels
    ) {
        BodyContentComponentRule toDomain() {
            requireNonNull(styleMapping, "bodyContent.styleMapping");
            requireNonNull(numbering, "bodyContent.numbering");
            requireNonNull(layout, "bodyContent.layout");
            requireNonNull(figure, "bodyContent.figure");
            requireNonNull(table, "bodyContent.table");
            requireNonNull(frame, "bodyContent.frame");
            requireNonNull(codeListing, "bodyContent.codeListing");
            requireNonNull(chart, "bodyContent.chart");
            requireNonNull(citationFormatting, "bodyContent.citationFormatting");
            requireNonNull(crossReferenceLabels, "bodyContent.crossReferenceLabels");

            return new BodyContentComponentRule(
                    componentId,
                    styleMapping.toDomain(),
                    numbering.toDomain(),
                    layout.toDomain(),
                    figure.toDomain(),
                    table.toDomain(),
                    frame.toDomain(),
                    codeListing.toDomain(),
                    chart.toDomain(),
                    citationFormatting.toDomain(),
                    crossReferenceLabels.toDomain()
            );
        }
    }

    public record CrossReferenceLabelsRuleDefinition(
            String sectionLabel,
            String figureLabel,
            String tableLabel,
            String frameLabel,
            String chartLabel,
            String codeListingLabel,
            String equationLabel
    ) {
        com.abntbuilder.formatter.profile.model.component.bodycontent.CrossReferenceLabelsRule toDomain() {
            return new com.abntbuilder.formatter.profile.model.component.bodycontent.CrossReferenceLabelsRule(
                    sectionLabel,
                    figureLabel,
                    tableLabel,
                    frameLabel,
                    chartLabel,
                    codeListingLabel,
                    equationLabel
            );
        }
    }

    public record CitationFormattingRuleDefinition(
            String pagePrefix,
            String multiAuthorJoiner,
            String etAl,
            String apudConnector,
            String suppressionMarker,
            String emphasisOursLabel,
            String emphasisAuthorLabel,
            String verbalCitationLabel
    ) {
        com.abntbuilder.formatter.profile.model.component.bodycontent.CitationFormattingRule toDomain() {
            return new com.abntbuilder.formatter.profile.model.component.bodycontent.CitationFormattingRule(
                    pagePrefix,
                    multiAuthorJoiner,
                    etAl,
                    apudConnector,
                    suppressionMarker,
                    emphasisOursLabel,
                    emphasisAuthorLabel,
                    verbalCitationLabel
            );
        }
    }

    public record BodyContentStyleMappingDefinition(
            List<String> sectionTitleStyleIdsByLevel,
            String paragraphStyleId,
            String directShortQuoteStyleId,
            String directLongQuoteStyleId,
            String indirectCitationStyleId,
            String citationOfCitationStyleId,
            String listOrderedStyleId,
            String listUnorderedStyleId,
            String equationStyleId,
            String footnoteCallStyleId,
            String footnoteTextStyleId
    ) {
        BodyContentStyleMapping toDomain() {
            requireNonEmpty(sectionTitleStyleIdsByLevel, "bodyContent.styleMapping.sectionTitleStyleIdsByLevel");

            return new BodyContentStyleMapping(
                    sectionTitleStyleIdsByLevel,
                    paragraphStyleId,
                    directShortQuoteStyleId,
                    directLongQuoteStyleId,
                    indirectCitationStyleId,
                    citationOfCitationStyleId,
                    listOrderedStyleId,
                    listUnorderedStyleId,
                    equationStyleId,
                    footnoteCallStyleId,
                    footnoteTextStyleId
            );
        }
    }

    public record FigureRuleDefinition(
            String captionStyleId,
            String sourceStyleId,
            String captionTemplate,
            String sourceTemplate,
            DisplayObjectContinuationLabelsDefinition continuationLabels,
            DisplayObjectSourcePlacement sourcePlacement,
            TextAlignment imageAlignment,
            BigDecimal maxWidthCm,
            BigDecimal maxHeightCm,
            BigDecimal defaultDpi,
            Integer maxImageBytes,
            Integer urlFetchTimeoutSeconds,
            ImageFitPolicy fitPolicy,
            NumberingStrategy numberingStrategy,
            String label,
            String separator
    ) {
        FigureRule toDomain() {
            requireNonNull(continuationLabels, "bodyContent.figure.continuationLabels");

            return new FigureRule(
                    captionStyleId,
                    sourceStyleId,
                    captionTemplate,
                    sourceTemplate,
                    continuationLabels.toDomain(),
                    sourcePlacement,
                    imageAlignment,
                    maxWidthCm,
                    maxHeightCm,
                    defaultDpi,
                    maxImageBytes,
                    urlFetchTimeoutSeconds,
                    fitPolicy,
                    numberingStrategy,
                    label,
                    separator
            );
        }
    }

    public record TableRuleDefinition(
            String captionStyleId,
            String sourceStyleId,
            String headerStyleId,
            String cellStyleId,
            String captionTemplate,
            String sourceTemplate,
            DisplayObjectContinuationLabelsDefinition continuationLabels,
            DisplayObjectSourcePlacement sourcePlacement,
            TextAlignment tableAlignment,
            BigDecimal widthPercent,
            Boolean repeatHeaderOnPageBreak,
            NumberingStrategy numberingStrategy,
            String label,
            String separator
    ) {
        TableRule toDomain() {
            requireNonNull(continuationLabels, "bodyContent.table.continuationLabels");

            return new TableRule(
                    captionStyleId,
                    sourceStyleId,
                    headerStyleId,
                    cellStyleId,
                    captionTemplate,
                    sourceTemplate,
                    continuationLabels.toDomain(),
                    sourcePlacement,
                    tableAlignment,
                    widthPercent,
                    repeatHeaderOnPageBreak,
                    numberingStrategy,
                    label,
                    separator
            );
        }
    }

    public record FrameRuleDefinition(
            String captionStyleId,
            String sourceStyleId,
            String headerStyleId,
            String cellStyleId,
            String captionTemplate,
            String sourceTemplate,
            DisplayObjectContinuationLabelsDefinition continuationLabels,
            DisplayObjectSourcePlacement sourcePlacement,
            TextAlignment tableAlignment,
            BigDecimal widthPercent,
            Boolean repeatHeaderOnPageBreak,
            NumberingStrategy numberingStrategy,
            String label,
            String separator
    ) {
        FrameRule toDomain() {
            requireNonNull(continuationLabels, "bodyContent.frame.continuationLabels");

            return new FrameRule(
                    captionStyleId,
                    sourceStyleId,
                    headerStyleId,
                    cellStyleId,
                    captionTemplate,
                    sourceTemplate,
                    continuationLabels.toDomain(),
                    sourcePlacement,
                    tableAlignment,
                    widthPercent,
                    repeatHeaderOnPageBreak,
                    numberingStrategy,
                    label,
                    separator
            );
        }
    }

    public record CodeListingRuleDefinition(
            String captionStyleId,
            String sourceStyleId,
            String codeStyleId,
            String captionTemplate,
            String sourceTemplate,
            DisplayObjectContinuationLabelsDefinition continuationLabels,
            DisplayObjectSourcePlacement sourcePlacement,
            NumberingStrategy numberingStrategy,
            String label,
            String separator
    ) {
        CodeListingRule toDomain() {
            requireNonNull(continuationLabels, "bodyContent.codeListing.continuationLabels");

            return new CodeListingRule(
                    captionStyleId,
                    sourceStyleId,
                    codeStyleId,
                    captionTemplate,
                    sourceTemplate,
                    continuationLabels.toDomain(),
                    sourcePlacement,
                    numberingStrategy,
                    label,
                    separator
            );
        }
    }

    public record ChartRuleDefinition(
            String captionStyleId,
            String sourceStyleId,
            String captionTemplate,
            String sourceTemplate,
            DisplayObjectContinuationLabelsDefinition continuationLabels,
            DisplayObjectSourcePlacement sourcePlacement,
            FigureRuleDefinition imageRule,
            NumberingStrategy numberingStrategy,
            String label,
            String separator
    ) {
        ChartRule toDomain() {
            requireNonNull(continuationLabels, "bodyContent.chart.continuationLabels");
            requireNonNull(imageRule, "bodyContent.chart.imageRule");

            return new ChartRule(
                    captionStyleId,
                    sourceStyleId,
                    captionTemplate,
                    sourceTemplate,
                    continuationLabels.toDomain(),
                    sourcePlacement,
                    imageRule.toDomain(),
                    numberingStrategy,
                    label,
                    separator
            );
        }
    }

    public record DisplayObjectContinuationLabelsDefinition(
            String first,
            String middle,
            String last
    ) {
        DisplayObjectContinuationLabels toDomain() {
            return new DisplayObjectContinuationLabels(first, middle, last);
        }
    }

    public record BodyContentNumberingRuleDefinition(
            Boolean enabled,
            String separator,
            String primarySuffix
    ) {
        BodyContentNumberingRule toDomain() {
            requireNonNull(enabled, "bodyContent.numbering.enabled");

            return new BodyContentNumberingRule(enabled, separator, primarySuffix);
        }
    }

    public record BodyContentLayoutRuleDefinition(
            Integer blankLinesBeforeSectionTitleWhenPrecededByContent,
            Integer blankLinesAfterSectionTitle,
            Boolean pageBreakBeforePrimarySection,
            String blankLineStyleId
    ) {
        BodyContentLayoutRule toDomain() {
            requireNonNull(
                    blankLinesBeforeSectionTitleWhenPrecededByContent,
                    "bodyContent.layout.blankLinesBeforeSectionTitleWhenPrecededByContent"
            );
            requireNonNull(blankLinesAfterSectionTitle, "bodyContent.layout.blankLinesAfterSectionTitle");
            requireNonNull(pageBreakBeforePrimarySection, "bodyContent.layout.pageBreakBeforePrimarySection");

            return new BodyContentLayoutRule(
                    blankLinesBeforeSectionTitleWhenPrecededByContent,
                    blankLinesAfterSectionTitle,
                    pageBreakBeforePrimarySection,
                    blankLineStyleId
            );
        }
    }

    public record SinglePageLayoutRuleDefinition(
            List<SinglePageGroupRuleDefinition> groups,
            List<LayoutGapRuleDefinition> gapRules,
            SinglePageLayoutPolicyDefinition policy
    ) {
        SinglePageLayoutRule toDomain() {
            requireNonEmpty(groups, "layoutRule.groups");
            requireNonNull(gapRules, "layoutRule.gapRules");
            requireNonNull(policy, "layoutRule.policy");

            return new SinglePageLayoutRule(
                    groups.stream()
                            .map(SinglePageGroupRuleDefinition::toDomain)
                            .toList(),
                    gapRules.stream()
                            .map(LayoutGapRuleDefinition::toDomain)
                            .toList(),
                    policy.toDomain()
            );
        }
    }

    public record SinglePageGroupRuleDefinition(
            String id,
            Boolean required,
            List<SinglePageItemRuleDefinition> items
    ) {
        SinglePageGroupRule toDomain() {
            requireNonNull(required, "group.required");
            requireNonEmpty(items, "group.items");

            return new SinglePageGroupRule(
                    id,
                    required,
                    items.stream()
                            .map(SinglePageItemRuleDefinition::toDomain)
                            .toList()
            );
        }
    }

    public record SinglePageItemRuleDefinition(
            String id,
            Boolean required,
            Integer maxVisualLinesPerValue,
            HorizontalPlacementRuleDefinition horizontalPlacement,
            Integer blankLinesAfter
    ) {
        SinglePageItemRule toDomain() {
            requireNonNull(required, "item.required");
            requireNonNull(horizontalPlacement, "item.horizontalPlacement");

            return new SinglePageItemRule(
                    id,
                    required,
                    Optional.ofNullable(maxVisualLinesPerValue),
                    horizontalPlacement.toDomain(),
                    blankLinesAfter == null ? 0 : blankLinesAfter
            );
        }
    }

    public record HorizontalPlacementRuleDefinition(
            HorizontalPlacementStrategy strategy
    ) {
        HorizontalPlacementRule toDomain() {
            return new HorizontalPlacementRule(strategy);
        }
    }

    public record LayoutGapRuleDefinition(
            String fromGroupId,
            String toGroupId,
            BigDecimal weight
    ) {
        LayoutGapRule toDomain() {
            return new LayoutGapRule(fromGroupId, toGroupId, weight);
        }
    }

    public record SinglePageLayoutPolicyDefinition(
            SinglePageAnchorStrategy anchorStrategy,
            SinglePageLineHeightStrategy lineHeightStrategy,
            SpacerStylePolicy spacerStylePolicy,
            SinglePageSafetyPolicyId safetyPolicy
    ) {
        SinglePageLayoutPolicy toDomain() {
            requireNonNull(anchorStrategy, "policy.anchorStrategy");
            requireNonNull(lineHeightStrategy, "policy.lineHeightStrategy");
            requireNonNull(spacerStylePolicy, "policy.spacerStylePolicy");
            requireNonNull(safetyPolicy, "policy.safetyPolicy");

            return new SinglePageLayoutPolicy(
                    anchorStrategy,
                    lineHeightStrategy,
                    spacerStylePolicy,
                    safetyPolicy
            );
        }
    }

    public record ErrataComponentRuleDefinition(
            String componentId,
            String headingStyleId,
            String headingText,
            String tableHeaderStyleId,
            String tableCellStyleId,
            List<String> tableHeaders,
            Integer blankLinesAfterHeading
    ) {
        ErrataComponentRule toDomain() {
            requireNonNull(tableHeaders, "errata.tableHeaders");
            return new ErrataComponentRule(componentId, headingStyleId, headingText,
                    tableHeaderStyleId, tableCellStyleId, tableHeaders,
                    blankLinesAfterHeading != null ? blankLinesAfterHeading : 0);
        }
    }

    public record DedicationComponentRuleDefinition(
            String componentId,
            String textStyleId,
            Integer blankLinesBefore
    ) {
        DedicationComponentRule toDomain() {
            requireNonNull(blankLinesBefore, "dedication.blankLinesBefore");
            return new DedicationComponentRule(componentId, textStyleId, blankLinesBefore);
        }
    }

    public record EpigraphComponentRuleDefinition(
            String componentId,
            String textStyleId,
            String authorStyleId,
            String authorTemplate
    ) {
        EpigraphComponentRule toDomain() {
            return new EpigraphComponentRule(componentId, textStyleId, authorStyleId, authorTemplate);
        }
    }

    public record AcknowledgmentsComponentRuleDefinition(
            String componentId,
            String headingStyleId,
            String headingText,
            String textStyleId,
            Integer blankLinesAfterHeading
    ) {
        AcknowledgmentsComponentRule toDomain() {
            return new AcknowledgmentsComponentRule(componentId, headingStyleId, headingText, textStyleId,
                    blankLinesAfterHeading != null ? blankLinesAfterHeading : 0);
        }
    }

    public record ResumoComponentRuleDefinition(
            String componentId,
            String headingStyleId,
            String headingText,
            String textStyleId,
            String keywordsStyleId,
            String keywordsLabel,
            String keywordsSeparator,
            String keywordsTerminator,
            Integer blankLinesAfterHeading
    ) {
        ResumoComponentRule toDomain() {
            requireNonNull(keywordsTerminator, "resumo.keywordsTerminator");
            return new ResumoComponentRule(componentId, headingStyleId, headingText,
                    textStyleId, keywordsStyleId, keywordsLabel, keywordsSeparator, keywordsTerminator,
                    blankLinesAfterHeading != null ? blankLinesAfterHeading : 0);
        }
    }

    public record AbstractComponentRuleDefinition(
            String componentId,
            String headingStyleId,
            String textStyleId,
            String keywordsStyleId,
            String keywordsSeparator,
            String keywordsTerminator,
            Integer blankLinesAfterHeading
    ) {
        AbstractComponentRule toDomain() {
            requireNonNull(keywordsTerminator, "abstract.keywordsTerminator");
            return new AbstractComponentRule(componentId, headingStyleId,
                    textStyleId, keywordsStyleId, keywordsSeparator, keywordsTerminator,
                    blankLinesAfterHeading != null ? blankLinesAfterHeading : 0);
        }
    }

    public record ReferencesFormattingRuleDefinition(
            String availableAtLabel,
            String accessedAtLabel,
            String etAlLabel,
            String inLabel,
            String authorSurnameGivenSeparator,
            String authorNameTerminator,
            String multiAuthorJoiner,
            Boolean authorSurnameUppercase
    ) {
        ReferencesFormattingRule toDomain() {
            requireNonNull(authorSurnameUppercase, "references.formattingRule.authorSurnameUppercase");
            return new ReferencesFormattingRule(
                    availableAtLabel, accessedAtLabel, etAlLabel, inLabel,
                    authorSurnameGivenSeparator, authorNameTerminator, multiAuthorJoiner, authorSurnameUppercase
            );
        }
    }

    public record ReferencesComponentRuleDefinition(
            String componentId,
            String headingStyleId,
            String headingText,
            String entryStyleId,
            Integer blankLinesBetweenEntries,
            Integer blankLinesAfterHeading,
            ReferencesFormattingRuleDefinition formattingRule
    ) {
        ReferencesComponentRule toDomain() {
            requireNonNull(blankLinesBetweenEntries, "references.blankLinesBetweenEntries");
            requireNonNull(formattingRule, "references.formattingRule");
            return new ReferencesComponentRule(componentId, headingStyleId, headingText,
                    entryStyleId, blankLinesBetweenEntries, formattingRule.toDomain(),
                    blankLinesAfterHeading != null ? blankLinesAfterHeading : 0);
        }
    }

    public record AppendixComponentRuleDefinition(
            String componentId,
            String headingTemplate,
            String headingStyleId,
            String paragraphStyleId,
            List<String> sectionTitleStyleIdsByLevel
    ) {
        AppendixComponentRule toDomain() {
            requireNonNull(sectionTitleStyleIdsByLevel, "appendix.sectionTitleStyleIdsByLevel");
            return new AppendixComponentRule(componentId, headingTemplate, headingStyleId,
                    paragraphStyleId, sectionTitleStyleIdsByLevel);
        }
    }

    public record AnnexComponentRuleDefinition(
            String componentId,
            String headingTemplate,
            String headingStyleId,
            String paragraphStyleId,
            List<String> sectionTitleStyleIdsByLevel
    ) {
        AnnexComponentRule toDomain() {
            requireNonNull(sectionTitleStyleIdsByLevel, "annex.sectionTitleStyleIdsByLevel");
            return new AnnexComponentRule(componentId, headingTemplate, headingStyleId,
                    paragraphStyleId, sectionTitleStyleIdsByLevel);
        }
    }

    public record GlossaryComponentRuleDefinition(
            String componentId,
            String headingStyleId,
            String headingText,
            String entryStyleId,
            String termSeparator,
            Integer blankLinesAfterHeading
    ) {
        GlossaryComponentRule toDomain() {
            return new GlossaryComponentRule(componentId, headingStyleId, headingText, entryStyleId, termSeparator,
                    blankLinesAfterHeading != null ? blankLinesAfterHeading : 0);
        }
    }

    private static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new InvalidProfileStructureException(fieldName + " must be provided.");
        }
    }

    private static void requireNonEmpty(List<?> value, String fieldName) {
        requireNonNull(value, fieldName);

        if (value.isEmpty()) {
            throw new InvalidProfileStructureException(fieldName + " must not be empty.");
        }
    }

    private static ComponentContentBindings createContentBindings(Map<String, String> value) {
        return new ComponentContentBindings(value == null ? Map.of() : value);
    }

    public record SummaryComponentRuleDefinition(
            String componentId,
            String headingStyleId,
            String headingText,
            List<String> entryStyleIdsByLevel,
            Boolean useTocField
    ) {
        SummaryComponentRule toDomain() {
            return new SummaryComponentRule(
                    componentId,
                    headingStyleId,
                    headingText,
                    entryStyleIdsByLevel,
                    useTocField != null && useTocField
            );
        }
    }

    public record IndexListComponentRuleDefinition(
            String componentId,
            String headingStyleId,
            String headingText,
            String entryStyleId,
            String entryTemplate,
            Integer blankLinesAfterHeading
    ) {
        IndexListComponentRule toDomain(String resolvedComponentId) {
            return new IndexListComponentRule(
                    resolvedComponentId,
                    headingStyleId,
                    headingText,
                    entryStyleId,
                    entryTemplate,
                    blankLinesAfterHeading != null ? blankLinesAfterHeading : 0
            );
        }
    }

    public record ListOfAbbreviationsComponentRuleDefinition(
            String componentId,
            String headingStyleId,
            String headingText,
            String entryStyleId,
            String termSeparator,
            Boolean sortAlphabetically,
            Integer blankLinesAfterHeading
    ) {
        ListOfAbbreviationsComponentRule toDomain() {
            return new ListOfAbbreviationsComponentRule(
                    componentId,
                    headingStyleId,
                    headingText,
                    entryStyleId,
                    termSeparator,
                    sortAlphabetically != null && sortAlphabetically,
                    blankLinesAfterHeading != null ? blankLinesAfterHeading : 0
            );
        }
    }

    public record ListOfSymbolsComponentRuleDefinition(
            String componentId,
            String headingStyleId,
            String headingText,
            String entryStyleId,
            String termSeparator,
            Integer blankLinesAfterHeading
    ) {
        ListOfSymbolsComponentRule toDomain() {
            return new ListOfSymbolsComponentRule(
                    componentId,
                    headingStyleId,
                    headingText,
                    entryStyleId,
                    termSeparator,
                    blankLinesAfterHeading != null ? blankLinesAfterHeading : 0
            );
        }
    }
}
