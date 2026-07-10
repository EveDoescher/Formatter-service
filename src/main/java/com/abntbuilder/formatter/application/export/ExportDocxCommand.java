package com.abntbuilder.formatter.application.export;

import com.abntbuilder.formatter.engine.model.FontPreferences;
import com.abntbuilder.formatter.engine.model.content.DocumentComponent;
import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;

import java.util.List;
import java.util.Objects;

public record ExportDocxCommand(
        String fileName,
        DocumentProfile profile,
        List<DocumentComponent> documentComponents,
        List<String> selectedComponents,
        List<ParagraphCommand> paragraphs,
        FontPreferences fontPreferences
) {
    public ExportDocxCommand(String fileName, DocumentProfile profile, List<ParagraphCommand> paragraphs) {
        this(fileName, profile, List.of(), List.of(), paragraphs, FontPreferences.NONE);
    }

    public ExportDocxCommand(
            String fileName,
            DocumentProfile profile,
            List<DocumentComponent> documentComponents,
            List<String> selectedComponents,
            List<ParagraphCommand> paragraphs
    ) {
        this(fileName, profile, documentComponents, selectedComponents, paragraphs, FontPreferences.NONE);
    }

    public ExportDocxCommand {
        requireNonBlank(fileName, "fileName");
        Objects.requireNonNull(profile, "profile must not be null");
        Objects.requireNonNull(documentComponents, "documentComponents must not be null");
        Objects.requireNonNull(selectedComponents, "selectedComponents must not be null");
        Objects.requireNonNull(paragraphs, "paragraphs must not be null");
        Objects.requireNonNull(fontPreferences, "fontPreferences must not be null");

        documentComponents = List.copyOf(documentComponents);
        selectedComponents = List.copyOf(selectedComponents);
        paragraphs = List.copyOf(paragraphs);

        for (DocumentComponent component : documentComponents) {
            Objects.requireNonNull(component, "documentComponents must not contain null values.");
        }

        for (String selectedComponent : selectedComponents) {
            requireNonBlank(selectedComponent, "selectedComponents item");
        }

        for (ParagraphCommand paragraph : paragraphs) {
            Objects.requireNonNull(paragraph, "paragraphs must not contain null values.");
        }

        if (documentComponents.isEmpty() && paragraphs.isEmpty()) {
            throw new IllegalArgumentException("document must contain at least one renderable component.");
        }
    }

    public record ParagraphCommand(
            String text,
            String styleId
    ) {
        public ParagraphCommand {
            requireNonBlank(text, "text");
            requireNonBlank(styleId, "styleId");
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
