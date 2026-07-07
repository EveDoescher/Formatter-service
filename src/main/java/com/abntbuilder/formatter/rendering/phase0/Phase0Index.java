package com.abntbuilder.formatter.rendering.phase0;

import com.abntbuilder.formatter.engine.model.content.bodycontent.CrossReferenceDisplayMode;
import com.abntbuilder.formatter.engine.model.content.bodycontent.CrossReferenceTargetType;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.CrossReferenceLabelsRule;
import com.abntbuilder.formatter.engine.model.profile.component.elementindex.ElementType;
import com.abntbuilder.formatter.rendering.bodycontent.BodyAbbreviationMetadata;
import com.abntbuilder.formatter.rendering.bodycontent.BodyDisplayObjectMetadata;
import com.abntbuilder.formatter.rendering.bodycontent.BodySectionMetadata;
import com.abntbuilder.formatter.shared.exception.InvalidBodyContentException;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record Phase0Index(
        Map<String, BodySectionMetadata> sections,
        Map<ElementType, Map<String, BodyDisplayObjectMetadata>> elements,
        List<BodyAbbreviationMetadata> abbreviations
) {

    public Phase0Index {
        Objects.requireNonNull(sections, "sections must not be null");
        Objects.requireNonNull(elements, "elements must not be null");
        Objects.requireNonNull(abbreviations, "abbreviations must not be null");
        sections = Map.copyOf(sections);
        Map<ElementType, Map<String, BodyDisplayObjectMetadata>> immutableElements = new EnumMap<>(ElementType.class);
        for (Map.Entry<ElementType, Map<String, BodyDisplayObjectMetadata>> e : elements.entrySet()) {
            immutableElements.put(e.getKey(), Map.copyOf(e.getValue()));
        }
        elements = Map.copyOf(immutableElements);
        abbreviations = List.copyOf(abbreviations);
    }

    public static Phase0Index empty() {
        return new Phase0Index(Map.of(), Map.of(), List.of());
    }

    public Map<String, BodyDisplayObjectMetadata> elements(ElementType type) {
        return elements.getOrDefault(type, Map.of());
    }

    public Phase0Index mergedWith(Phase0Index other) {
        Objects.requireNonNull(other, "other must not be null");
        Map<String, BodySectionMetadata> mergedSections = new LinkedHashMap<>(sections);
        mergedSections.putAll(other.sections);

        Map<ElementType, Map<String, BodyDisplayObjectMetadata>> mergedElements = new EnumMap<>(ElementType.class);
        for (ElementType type : ElementType.values()) {
            Map<String, BodyDisplayObjectMetadata> merged = new LinkedHashMap<>(elements(type));
            merged.putAll(other.elements(type));
            if (!merged.isEmpty()) mergedElements.put(type, merged);
        }

        List<BodyAbbreviationMetadata> mergedAbbreviations = new ArrayList<>(abbreviations);
        mergedAbbreviations.addAll(other.abbreviations);
        return new Phase0Index(
                Map.copyOf(mergedSections),
                mergedElements,
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
            case FIGURE -> resolveDisplayObjectNumber(targetId, ElementType.FIGURE, "FIGURE");
            case TABLE -> resolveDisplayObjectNumber(targetId, ElementType.TABLE, "TABLE");
            case FRAME -> resolveDisplayObjectNumber(targetId, ElementType.FRAME, "FRAME");
            case CHART -> resolveDisplayObjectNumber(targetId, ElementType.CHART, "CHART");
            case CODE_LISTING -> resolveDisplayObjectNumber(targetId, ElementType.CODE_LISTING, "CODE_LISTING");
            case EQUATION -> throw new InvalidBodyContentException(
                    "CROSS_REFERENCE to EQUATION is not yet supported.");
        };
    }

    private String resolveDisplayObjectNumber(String targetId, ElementType type, String typeName) {
        BodyDisplayObjectMetadata m = elements(type).get(targetId);
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
            case FIGURE -> resolveDisplayObjectCaption(targetId, ElementType.FIGURE, "FIGURE");
            case TABLE -> resolveDisplayObjectCaption(targetId, ElementType.TABLE, "TABLE");
            case FRAME -> resolveDisplayObjectCaption(targetId, ElementType.FRAME, "FRAME");
            case CHART -> resolveDisplayObjectCaption(targetId, ElementType.CHART, "CHART");
            case CODE_LISTING -> resolveDisplayObjectCaption(targetId, ElementType.CODE_LISTING, "CODE_LISTING");
            case EQUATION -> throw new InvalidBodyContentException(
                    "CROSS_REFERENCE to EQUATION is not yet supported.");
        };
    }

    private String resolveDisplayObjectCaption(String targetId, ElementType type, String typeName) {
        BodyDisplayObjectMetadata m = elements(type).get(targetId);
        if (m == null) throw new InvalidBodyContentException(
                "CROSS_REFERENCE targetId not found: '" + targetId + "' (type: " + typeName + ").");
        return m.caption();
    }
}
