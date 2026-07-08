package com.abntbuilder.formatter.rendering.phase0;

import com.abntbuilder.formatter.engine.model.content.DocumentComponent;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyBlock;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyChart;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyAbbreviation;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyCitationCall;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyCitationType;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyCodeListing;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyContentComponent;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyFigure;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyFrame;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyInline;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyParagraph;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodySection;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyTable;
import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.BodyContentComponentRule;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.NumberingStrategy;
import com.abntbuilder.formatter.engine.model.profile.component.elementindex.ElementType;
import com.abntbuilder.formatter.input.profile.ComponentRuleResolver;
import com.abntbuilder.formatter.rendering.bodycontent.BodyAbbreviationMetadata;
import com.abntbuilder.formatter.rendering.bodycontent.BodyDisplayObjectMetadata;
import com.abntbuilder.formatter.rendering.bodycontent.BodySectionMetadata;
import com.abntbuilder.formatter.rendering.bodycontent.DisplayObjectContinuationPart;
import com.abntbuilder.formatter.rendering.bodycontent.DisplayObjectRenderingState;
import com.abntbuilder.formatter.rendering.bodycontent.SectionNumberingState;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class DisplayObjectCollector {

    public Phase0Index collect(List<DocumentComponent> selectedComponents, DocumentProfile profile) {
        Objects.requireNonNull(selectedComponents, "selectedComponents must not be null");
        Objects.requireNonNull(profile, "profile must not be null");

        Phase0Index merged = Phase0Index.empty();
        for (DocumentComponent component : selectedComponents) {
            if (!(component instanceof BodyContentComponent bodyContent)) continue;

            BodyContentComponentRule rule = new ComponentRuleResolver(profile)
                    .resolve(bodyContent.componentId(), BodyContentComponentRule.class);
            merged = merged.mergedWith(collectFromBodyContent(bodyContent, rule));
        }
        return merged;
    }

    Phase0Index collectFromBodyContent(BodyContentComponent component, BodyContentComponentRule rule) {
        List<BodyFigure> allFigures = figuresFrom(component.sections());
        List<BodyTable> allTables = tablesFrom(component.sections());
        List<BodyFrame> allFrames = framesFrom(component.sections());
        List<BodyCodeListing> allCodeListings = codeListingsFrom(component.sections());
        List<BodyChart> allCharts = chartsFrom(component.sections());

        DisplayObjectRenderingState<BodyFigure> figureState =
                new DisplayObjectRenderingState<>(allFigures);
        DisplayObjectRenderingState<BodyTable> tableState =
                new DisplayObjectRenderingState<>(allTables);
        DisplayObjectRenderingState<BodyFrame> frameState =
                new DisplayObjectRenderingState<>(allFrames);
        DisplayObjectRenderingState<BodyChart> chartState =
                new DisplayObjectRenderingState<>(allCharts);
        DisplayObjectRenderingState<BodyCodeListing> codeListingState =
                new DisplayObjectRenderingState<>(allCodeListings);

        SectionNumberingState numberingState = new SectionNumberingState(rule.numbering());

        Map<String, BodySectionMetadata> sectionIndex = new LinkedHashMap<>();
        Map<String, BodyDisplayObjectMetadata> figureIndex = new LinkedHashMap<>();
        Map<String, BodyDisplayObjectMetadata> tableIndex = new LinkedHashMap<>();
        Map<String, BodyDisplayObjectMetadata> frameIndex = new LinkedHashMap<>();
        Map<String, BodyDisplayObjectMetadata> chartIndex = new LinkedHashMap<>();
        Map<String, BodyDisplayObjectMetadata> codeListingIndex = new LinkedHashMap<>();
        List<BodyAbbreviationMetadata> abbreviations = new ArrayList<>();

        for (BodySection section : component.sections()) {
            if (section.title().isPresent()) {
                String renderedTitle = numberingState.resolveTitle(
                        section.level(), section.title().orElseThrow());
                String renderedNumber = numberingState.resolveNumber(section.level());
                sectionIndex.put(section.id(),
                        new BodySectionMetadata(section.id(), section.level(), renderedTitle, renderedNumber));
            }

            for (BodyBlock block : section.blocks()) {
                if (block instanceof BodyParagraph paragraph) {
                    for (BodyInline inline : paragraph.content()) {
                        if (inline instanceof BodyAbbreviation abbr) {
                            boolean alreadySeen = abbreviations.stream()
                                    .anyMatch(m -> m.abbreviation().equals(abbr.abbreviation()));
                            if (!alreadySeen) {
                                abbreviations.add(new BodyAbbreviationMetadata(
                                        abbr.abbreviation(), abbr.expansion()));
                            }
                        }
                    }
                } else if (block instanceof BodyFigure figure) {
                    DisplayObjectContinuationPart part = figureState.nextPart(
                            figure, rule.figure().continuationLabels());
                    if (part.index() == 1) {
                        figureIndex.put(figure.displayGroupKey(),
                                new BodyDisplayObjectMetadata(
                                        figure.displayGroupKey(),
                                        resolveNumber(part.number(), section.level(),
                                                rule.figure().numberingStrategy(),
                                                rule.figure().separator()),
                                        figure.caption()));
                    }
                } else if (block instanceof BodyTable table) {
                    DisplayObjectContinuationPart part = tableState.nextPart(
                            table, rule.table().continuationLabels());
                    if (part.index() == 1) {
                        tableIndex.put(table.displayGroupKey(),
                                new BodyDisplayObjectMetadata(
                                        table.displayGroupKey(),
                                        resolveNumber(part.number(), section.level(),
                                                rule.table().numberingStrategy(),
                                                rule.table().separator()),
                                        table.caption()));
                    }
                } else if (block instanceof BodyFrame frame) {
                    DisplayObjectContinuationPart part = frameState.nextPart(
                            frame, rule.frame().continuationLabels());
                    if (part.index() == 1) {
                        frameIndex.put(frame.displayGroupKey(),
                                new BodyDisplayObjectMetadata(
                                        frame.displayGroupKey(),
                                        resolveNumber(part.number(), section.level(),
                                                rule.frame().numberingStrategy(),
                                                rule.frame().separator()),
                                        frame.caption()));
                    }
                } else if (block instanceof BodyChart chart) {
                    DisplayObjectContinuationPart part = chartState.nextPart(
                            chart, rule.chart().continuationLabels());
                    if (part.index() == 1) {
                        chartIndex.put(chart.displayGroupKey(),
                                new BodyDisplayObjectMetadata(
                                        chart.displayGroupKey(),
                                        resolveNumber(part.number(), section.level(),
                                                rule.chart().numberingStrategy(),
                                                rule.chart().separator()),
                                        chart.caption()));
                    }
                } else if (block instanceof BodyCodeListing codeListing) {
                    DisplayObjectContinuationPart part = codeListingState.nextPart(
                            codeListing, rule.codeListing().continuationLabels());
                    if (part.index() == 1) {
                        codeListingIndex.put(codeListing.displayGroupKey(),
                                new BodyDisplayObjectMetadata(
                                        codeListing.displayGroupKey(),
                                        resolveNumber(part.number(), section.level(),
                                                rule.codeListing().numberingStrategy(),
                                                rule.codeListing().separator()),
                                        codeListing.caption()));
                    }
                }
            }
        }

        Map<ElementType, Map<String, BodyDisplayObjectMetadata>> elementMap = new EnumMap<>(ElementType.class);
        if (!figureIndex.isEmpty()) elementMap.put(ElementType.FIGURE, figureIndex);
        if (!tableIndex.isEmpty()) elementMap.put(ElementType.TABLE, tableIndex);
        if (!frameIndex.isEmpty()) elementMap.put(ElementType.FRAME, frameIndex);
        if (!chartIndex.isEmpty()) elementMap.put(ElementType.CHART, chartIndex);
        if (!codeListingIndex.isEmpty()) elementMap.put(ElementType.CODE_LISTING, codeListingIndex);

        Map<String, Integer> numericCitationNumbers = collectNumericCitationNumbers(component.sections());

        return new Phase0Index(Map.copyOf(sectionIndex), elementMap, List.copyOf(abbreviations), numericCitationNumbers);
    }

    private static String resolveNumber(int globalNumber, int chapterLevel,
                                       NumberingStrategy strategy, String separator) {
        return switch (strategy) {
            case GLOBAL_SEQUENTIAL -> String.valueOf(globalNumber);
            case BY_CHAPTER -> chapterLevel + separator + globalNumber;
        };
    }

    private static Map<String, Integer> collectNumericCitationNumbers(List<BodySection> sections) {
        Map<String, Integer> numbers = new LinkedHashMap<>();
        for (BodySection section : sections) {
            for (BodyBlock block : section.blocks()) {
                if (block instanceof BodyParagraph paragraph) {
                    for (BodyInline inline : paragraph.content()) {
                        if (inline instanceof BodyCitationCall citation
                                && citation.citationType() == BodyCitationType.NUMERIC) {
                            for (String refId : citation.numericReferenceIds()) {
                                numbers.computeIfAbsent(refId, k -> numbers.size() + 1);
                            }
                        }
                    }
                }
            }
        }
        return Map.copyOf(numbers);
    }

    private static List<BodyFigure> figuresFrom(List<BodySection> sections) {
        return sections.stream()
                .flatMap(s -> s.blocks().stream())
                .filter(BodyFigure.class::isInstance)
                .map(BodyFigure.class::cast)
                .toList();
    }

    private static List<BodyTable> tablesFrom(List<BodySection> sections) {
        return sections.stream()
                .flatMap(s -> s.blocks().stream())
                .filter(BodyTable.class::isInstance)
                .map(BodyTable.class::cast)
                .toList();
    }

    private static List<BodyFrame> framesFrom(List<BodySection> sections) {
        return sections.stream()
                .flatMap(s -> s.blocks().stream())
                .filter(BodyFrame.class::isInstance)
                .map(BodyFrame.class::cast)
                .toList();
    }

    private static List<BodyCodeListing> codeListingsFrom(List<BodySection> sections) {
        return sections.stream()
                .flatMap(s -> s.blocks().stream())
                .filter(BodyCodeListing.class::isInstance)
                .map(BodyCodeListing.class::cast)
                .toList();
    }

    private static List<BodyChart> chartsFrom(List<BodySection> sections) {
        return sections.stream()
                .flatMap(s -> s.blocks().stream())
                .filter(BodyChart.class::isInstance)
                .map(BodyChart.class::cast)
                .toList();
    }
}
