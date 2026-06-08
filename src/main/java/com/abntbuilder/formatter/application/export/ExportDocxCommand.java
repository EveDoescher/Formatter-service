package com.abntbuilder.formatter.application.export;

import com.abntbuilder.formatter.document.component.DocumentComponent;
import com.abntbuilder.formatter.document.component.cover.CoverComponent;
import com.abntbuilder.formatter.document.component.titlepage.TitlePageComponent;
import com.abntbuilder.formatter.profile.model.DocumentProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record ExportDocxCommand(
        String fileName,
        DocumentProfile profile,
        Optional<CoverComponent> cover,
        Optional<TitlePageComponent> titlePage,
        List<String> selectedComponents,
        List<ParagraphCommand> paragraphs
) {
    public ExportDocxCommand(String fileName, DocumentProfile profile, List<ParagraphCommand> paragraphs) {
        this(fileName, profile, Optional.empty(), Optional.empty(), List.of(), paragraphs);
    }

    public ExportDocxCommand(
            String fileName,
            DocumentProfile profile,
            Optional<CoverComponent> cover,
            List<ParagraphCommand> paragraphs
    ) {
        this(fileName, profile, cover, Optional.empty(), List.of(), paragraphs);
    }

    public ExportDocxCommand {
        requireNonBlank(fileName, "fileName");
        Objects.requireNonNull(profile, "profile must not be null");
        Objects.requireNonNull(cover, "cover must not be null");
        Objects.requireNonNull(titlePage, "titlePage must not be null");
        Objects.requireNonNull(selectedComponents, "selectedComponents must not be null");
        Objects.requireNonNull(paragraphs, "paragraphs must not be null");

        selectedComponents = List.copyOf(selectedComponents);
        paragraphs = List.copyOf(paragraphs);

        for (String selectedComponent : selectedComponents) {
            requireNonBlank(selectedComponent, "selectedComponents item");
        }

        for (ParagraphCommand paragraph : paragraphs) {
            Objects.requireNonNull(paragraph, "paragraphs must not contain null values.");
        }

        if (cover.isEmpty() && titlePage.isEmpty() && paragraphs.isEmpty()) {
            throw new IllegalArgumentException("document must contain at least one renderable component.");
        }
    }

    public List<DocumentComponent> documentComponents() {
        List<DocumentComponent> components = new ArrayList<>();

        cover.ifPresent(components::add);
        titlePage.ifPresent(components::add);

        return List.copyOf(components);
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
