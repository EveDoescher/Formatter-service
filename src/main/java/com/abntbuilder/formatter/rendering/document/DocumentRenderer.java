package com.abntbuilder.formatter.rendering.document;

import com.abntbuilder.formatter.application.export.ExportDocxCommand;
import com.abntbuilder.formatter.document.component.DocumentComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxDocument;
import com.abntbuilder.formatter.output.docx.api.DocxPageBreak;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.ComponentRendererRegistry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class DocumentRenderer {

    public static final String PARAGRAPHS_COMPONENT_ID = "paragraphs";

    private final ComponentRendererRegistry rendererRegistry;
    private final ComponentSelectionResolver selectionResolver;

    public DocumentRenderer(
            ComponentRendererRegistry rendererRegistry,
            ComponentSelectionResolver selectionResolver
    ) {
        this.rendererRegistry = Objects.requireNonNull(rendererRegistry, "rendererRegistry must not be null");
        this.selectionResolver = Objects.requireNonNull(selectionResolver, "selectionResolver must not be null");
    }

    public DocxDocument render(ExportDocxCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        List<String> componentOrder = command.profile().componentOrder();
        selectionResolver.validateSupportedSelections(
                command.selectedComponents(),
                new LinkedHashSet<>(componentOrder)
        );

        StyleResolver styleResolver = new StyleResolver(command.profile());
        List<DocxBlock> blocks = new ArrayList<>();
        Map<String, DocumentComponent> documentComponentsById = documentComponentsById(command);

        validateSelectedContent(command, documentComponentsById);

        for (String componentId : componentOrder) {
            if (!selectionResolver.shouldRender(componentId, command.selectedComponents())) {
                continue;
            }

            if (PARAGRAPHS_COMPONENT_ID.equals(componentId)) {
                if (!command.paragraphs().isEmpty()) {
                    addBlocks(blocks, command.paragraphs()
                            .stream()
                            .map(paragraph -> new DocxParagraph(
                                    paragraph.text(),
                                    styleResolver.resolve(paragraph.styleId())
                            ))
                            .map(DocxBlock.class::cast)
                            .toList());
                }
                continue;
            }

            DocumentComponent component = documentComponentsById.get(componentId);

            if (component != null) {
                addBlocks(
                        blocks,
                        rendererRegistry.get(componentId).renderComponent(component, command.profile())
                );
            }
        }

        if (blocks.isEmpty()) {
            throw new IllegalArgumentException("document must contain at least one selected renderable component.");
        }

        return new DocxDocument(
                command.profile().pageRule(),
                blocks
        );
    }

    private static void addBlocks(List<DocxBlock> blocks, List<DocxBlock> newBlocks) {
        if (newBlocks.isEmpty()) {
            return;
        }

        if (!blocks.isEmpty()) {
            blocks.add(new DocxPageBreak());
        }

        blocks.addAll(newBlocks);
    }

    private Map<String, DocumentComponent> documentComponentsById(ExportDocxCommand command) {
        Map<String, DocumentComponent> componentsById = new LinkedHashMap<>();

        for (DocumentComponent component : command.documentComponents()) {
            String componentId = rendererRegistry.componentIdFor(component);

            if (componentsById.put(componentId, component) != null) {
                throw new IllegalArgumentException("Duplicate document component content for id: " + componentId);
            }
        }

        return Map.copyOf(componentsById);
    }

    private static void validateSelectedContent(
            ExportDocxCommand command,
            Map<String, DocumentComponent> documentComponentsById
    ) {
        if (command.selectedComponents().isEmpty()) {
            return;
        }

        for (String selectedComponent : command.selectedComponents()) {
            boolean hasContent = PARAGRAPHS_COMPONENT_ID.equals(selectedComponent)
                    ? !command.paragraphs().isEmpty()
                    : documentComponentsById.containsKey(selectedComponent);

            if (!hasContent) {
                throw new IllegalArgumentException("selected component has no content: " + selectedComponent);
            }
        }
    }
}
