package com.abntbuilder.formatter.document.component.bodycontent;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

import java.util.List;
import java.util.Objects;

public record BodyContentComponent(
        List<BodySection> sections
) implements DocumentComponent {

    public BodyContentComponent {
        Objects.requireNonNull(sections, "sections must not be null");

        if (sections.isEmpty()) {
            throw new IllegalArgumentException("sections must not be empty.");
        }

        sections = List.copyOf(sections);

        for (BodySection section : sections) {
            Objects.requireNonNull(section, "sections must not contain null values.");
        }

        validateSectionHierarchy(sections);
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
}
