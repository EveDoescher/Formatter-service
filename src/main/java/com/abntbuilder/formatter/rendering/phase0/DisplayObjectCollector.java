package com.abntbuilder.formatter.rendering.phase0;

import com.abntbuilder.formatter.document.component.DocumentComponent;
import com.abntbuilder.formatter.document.component.bodycontent.BodyBlock;
import com.abntbuilder.formatter.document.component.bodycontent.BodyChart;
import com.abntbuilder.formatter.document.component.bodycontent.BodyCodeListing;
import com.abntbuilder.formatter.document.component.bodycontent.BodyContentComponent;
import com.abntbuilder.formatter.document.component.bodycontent.BodyFigure;
import com.abntbuilder.formatter.document.component.bodycontent.BodyFrame;
import com.abntbuilder.formatter.document.component.bodycontent.BodySection;
import com.abntbuilder.formatter.document.component.bodycontent.BodyTable;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.component.bodycontent.BodyContentComponentRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.NumberingStrategy;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyAbbreviationMetadata;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyDisplayObjectMetadata;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodySectionMetadata;
import com.abntbuilder.formatter.rendering.component.bodycontent.DisplayObjectContinuationPart;
import com.abntbuilder.formatter.rendering.component.bodycontent.DisplayObjectRenderingState;
import com.abntbuilder.formatter.rendering.component.bodycontent.SectionNumberingState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class DisplayObjectCollector {

    public Phase0Index collect(List<DocumentComponent> selectedComponents, DocumentProfile profile) {
        Objects.requireNonNull(selectedComponents, "selectedComponents must not be null");
        Objects.requireNonNull(profile, "profile must not be null");

        BodyContentComponent bodyContent = selectedComponents.stream()
                .filter(c -> c instanceof BodyContentComponent)
                .map(BodyContentComponent.class::cast)
                .findFirst()
                .orElse(null);

        if (bodyContent == null) {
            return Phase0Index.empty();
        }

        BodyContentComponentRule rule = new ComponentRuleResolver(profile)
                .resolve("bodyContent", BodyContentComponentRule.class);

        return collectFromBodyContent(bodyContent, rule);
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
                if (block instanceof BodyFigure figure) {
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

        return new Phase0Index(
                Map.copyOf(sectionIndex),
                Map.copyOf(figureIndex),
                Map.copyOf(tableIndex),
                Map.copyOf(frameIndex),
                Map.copyOf(chartIndex),
                Map.copyOf(codeListingIndex),
                List.copyOf(abbreviations)
        );
    }

    private static int resolveNumber(int globalNumber, int chapterLevel,
                                     NumberingStrategy strategy, String separator) {
        return switch (strategy) {
            case GLOBAL_SEQUENTIAL -> globalNumber;
            case BY_CHAPTER -> globalNumber;
        };
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
