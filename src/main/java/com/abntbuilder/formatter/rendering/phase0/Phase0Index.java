package com.abntbuilder.formatter.rendering.phase0;

import com.abntbuilder.formatter.document.component.bodycontent.CrossReferenceDisplayMode;
import com.abntbuilder.formatter.document.component.bodycontent.CrossReferenceTargetType;
import com.abntbuilder.formatter.profile.model.component.bodycontent.CrossReferenceLabelsRule;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyAbbreviationMetadata;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyDisplayObjectMetadata;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodySectionMetadata;
import com.abntbuilder.formatter.shared.exception.InvalidBodyContentException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record Phase0Index(
        Map<String, BodySectionMetadata> sections,
        Map<String, BodyDisplayObjectMetadata> figures,
        Map<String, BodyDisplayObjectMetadata> tables,
        Map<String, BodyDisplayObjectMetadata> frames,
        Map<String, BodyDisplayObjectMetadata> charts,
        Map<String, BodyDisplayObjectMetadata> codeListings,
        List<BodyAbbreviationMetadata> abbreviations
) {

    public Phase0Index {
        Objects.requireNonNull(sections, "sections must not be null");
        Objects.requireNonNull(figures, "figures must not be null");
        Objects.requireNonNull(tables, "tables must not be null");
        Objects.requireNonNull(frames, "frames must not be null");
        Objects.requireNonNull(charts, "charts must not be null");
        Objects.requireNonNull(codeListings, "codeListings must not be null");
        Objects.requireNonNull(abbreviations, "abbreviations must not be null");
        sections = Map.copyOf(sections);
        figures = Map.copyOf(figures);
        tables = Map.copyOf(tables);
        frames = Map.copyOf(frames);
        charts = Map.copyOf(charts);
        codeListings = Map.copyOf(codeListings);
        abbreviations = List.copyOf(abbreviations);
    }

    public static Phase0Index empty() {
        return new Phase0Index(
                Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), List.of()
        );
    }

    public Phase0Index mergedWith(Phase0Index other) {
        Objects.requireNonNull(other, "other must not be null");
        Map<String, BodySectionMetadata> mergedSections = new LinkedHashMap<>(sections);
        mergedSections.putAll(other.sections);
        Map<String, BodyDisplayObjectMetadata> mergedFigures = new LinkedHashMap<>(figures);
        mergedFigures.putAll(other.figures);
        Map<String, BodyDisplayObjectMetadata> mergedTables = new LinkedHashMap<>(tables);
        mergedTables.putAll(other.tables);
        Map<String, BodyDisplayObjectMetadata> mergedFrames = new LinkedHashMap<>(frames);
        mergedFrames.putAll(other.frames);
        Map<String, BodyDisplayObjectMetadata> mergedCharts = new LinkedHashMap<>(charts);
        mergedCharts.putAll(other.charts);
        Map<String, BodyDisplayObjectMetadata> mergedCodeListings = new LinkedHashMap<>(codeListings);
        mergedCodeListings.putAll(other.codeListings);
        List<BodyAbbreviationMetadata> mergedAbbreviations = new ArrayList<>(abbreviations);
        mergedAbbreviations.addAll(other.abbreviations);
        return new Phase0Index(
                Map.copyOf(mergedSections),
                Map.copyOf(mergedFigures),
                Map.copyOf(mergedTables),
                Map.copyOf(mergedFrames),
                Map.copyOf(mergedCharts),
                Map.copyOf(mergedCodeListings),
                List.copyOf(mergedAbbreviations)
        );
    }

    public String resolveCrossReference(
            String targetId,
            CrossReferenceTargetType targetType,
            CrossReferenceDisplayMode displayMode,
            CrossReferenceLabelsRule labels
    ) {
        return switch (displayMode) {
            case NUMBER_ONLY -> resolveNumber(targetId, targetType);
            case LABEL_AND_NUMBER -> labels.labelFor(targetType) + " " + resolveNumber(targetId, targetType);
            case CAPTION -> resolveCaption(targetId, targetType);
        };
    }

    private String resolveNumber(String targetId, CrossReferenceTargetType targetType) {
        return switch (targetType) {
            case SECTION -> {
                BodySectionMetadata m = sections.get(targetId);
                if (m == null) throw new InvalidBodyContentException(
                        "CROSS_REFERENCE targetId not found: '" + targetId + "' (type: SECTION).");
                yield m.renderedNumber();
            }
            case FIGURE -> resolveDisplayObjectNumber(targetId, figures, "FIGURE");
            case TABLE -> resolveDisplayObjectNumber(targetId, tables, "TABLE");
            case FRAME -> resolveDisplayObjectNumber(targetId, frames, "FRAME");
            case CHART -> resolveDisplayObjectNumber(targetId, charts, "CHART");
            case CODE_LISTING -> resolveDisplayObjectNumber(targetId, codeListings, "CODE_LISTING");
            case EQUATION -> throw new InvalidBodyContentException(
                    "CROSS_REFERENCE to EQUATION is not yet supported.");
        };
    }

    private String resolveDisplayObjectNumber(
            String targetId, Map<String, BodyDisplayObjectMetadata> index, String typeName) {
        BodyDisplayObjectMetadata m = index.get(targetId);
        if (m == null) throw new InvalidBodyContentException(
                "CROSS_REFERENCE targetId not found: '" + targetId + "' (type: " + typeName + ").");
        return String.valueOf(m.number());
    }

    private String resolveCaption(String targetId, CrossReferenceTargetType targetType) {
        return switch (targetType) {
            case SECTION -> {
                BodySectionMetadata m = sections.get(targetId);
                if (m == null) throw new InvalidBodyContentException(
                        "CROSS_REFERENCE targetId not found: '" + targetId + "' (type: SECTION).");
                yield m.renderedTitle();
            }
            case FIGURE -> resolveDisplayObjectCaption(targetId, figures, "FIGURE");
            case TABLE -> resolveDisplayObjectCaption(targetId, tables, "TABLE");
            case FRAME -> resolveDisplayObjectCaption(targetId, frames, "FRAME");
            case CHART -> resolveDisplayObjectCaption(targetId, charts, "CHART");
            case CODE_LISTING -> resolveDisplayObjectCaption(targetId, codeListings, "CODE_LISTING");
            case EQUATION -> throw new InvalidBodyContentException(
                    "CROSS_REFERENCE to EQUATION is not yet supported.");
        };
    }

    private String resolveDisplayObjectCaption(
            String targetId, Map<String, BodyDisplayObjectMetadata> index, String typeName) {
        BodyDisplayObjectMetadata m = index.get(targetId);
        if (m == null) throw new InvalidBodyContentException(
                "CROSS_REFERENCE targetId not found: '" + targetId + "' (type: " + typeName + ").");
        return m.caption();
    }
}
