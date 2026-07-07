package com.abntbuilder.formatter.engine.model.content.bodycontent;

import com.abntbuilder.formatter.engine.model.content.ComponentType;
import com.abntbuilder.formatter.engine.model.content.DocumentComponent;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record BodyContentComponent(
        String componentId,
        List<BodySection> sections
) implements DocumentComponent {

    public BodyContentComponent {
        if (componentId == null || componentId.isBlank())
            throw new IllegalArgumentException("componentId must not be blank.");
        Objects.requireNonNull(sections, "sections must not be null");

        if (sections.isEmpty()) {
            throw new IllegalArgumentException("sections must not be empty.");
        }

        sections = List.copyOf(sections);

        for (BodySection section : sections) {
            Objects.requireNonNull(section, "sections must not contain null values.");
        }

        validateSectionHierarchy(sections);
        validateDisplayObjectIds(sections);
    }

    @Override
    public ComponentType type() {
        return ComponentType.BODY_CONTENT;
    }

    private static void validateSectionHierarchy(List<BodySection> sections) {
        int previousTitledLevel = 0;

        for (BodySection section : sections) {
            if (section.title().isEmpty()) {
                continue;
            }

            int currentLevel = section.level();

            if (previousTitledLevel == 0 && currentLevel > 1) {
                throw new IllegalArgumentException(
                        "bodyContent section hierarchy cannot start at level " + currentLevel + "."
                );
            }

            if (previousTitledLevel > 0 && currentLevel > previousTitledLevel + 1) {
                throw new IllegalArgumentException(
                        "bodyContent section hierarchy cannot jump from level "
                                + previousTitledLevel
                                + " to level "
                                + currentLevel
                                + "."
                );
            }

            previousTitledLevel = currentLevel;
        }
    }

    private static void validateDisplayObjectIds(List<BodySection> sections) {
        Set<String> figureIds = new HashSet<>();
        Set<String> tableIds = new HashSet<>();
        Set<String> frameIds = new HashSet<>();
        Set<String> codeListingIds = new HashSet<>();
        Set<String> chartIds = new HashSet<>();

        for (BodySection section : sections) {
            for (BodyBlock block : section.blocks()) {
                if (block instanceof BodyFigure figure && !figureIds.add(figure.id())) {
                    throw new IllegalArgumentException("bodyContent figure id must be unique: " + figure.id());
                }
                if (block instanceof BodyTable table && !tableIds.add(table.id())) {
                    throw new IllegalArgumentException("bodyContent table id must be unique: " + table.id());
                }
                if (block instanceof BodyFrame frame && !frameIds.add(frame.id())) {
                    throw new IllegalArgumentException("bodyContent frame id must be unique: " + frame.id());
                }
                if (block instanceof BodyCodeListing codeListing && !codeListingIds.add(codeListing.id())) {
                    throw new IllegalArgumentException("bodyContent codeListing id must be unique: " + codeListing.id());
                }
                if (block instanceof BodyChart chart && !chartIds.add(chart.id())) {
                    throw new IllegalArgumentException("bodyContent chart id must be unique: " + chart.id());
                }
            }
        }
    }
}
