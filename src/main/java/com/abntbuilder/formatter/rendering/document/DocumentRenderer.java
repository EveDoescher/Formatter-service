package com.abntbuilder.formatter.rendering.document;

import com.abntbuilder.formatter.application.export.ExportDocxCommand;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxDocument;
import com.abntbuilder.formatter.output.docx.api.DocxPageBreak;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.ComponentRendererRegistry;
import com.abntbuilder.formatter.rendering.component.titlepage.TitlePageRenderer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

public final class DocumentRenderer {

    public static final String COVER_COMPONENT_ID = "cover";
    public static final String TITLE_PAGE_COMPONENT_ID = TitlePageRenderer.COMPONENT_ID;
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

        validateSelectedContent(command);

        for (String componentId : componentOrder) {
            if (!selectionResolver.shouldRender(componentId, command.selectedComponents())) {
                continue;
            }

            switch (componentId) {
                case COVER_COMPONENT_ID -> command.cover().ifPresent(cover -> addBlocks(
                        blocks,
                        rendererRegistry.get(COVER_COMPONENT_ID).renderComponent(cover, command.profile())
                ));
                case TITLE_PAGE_COMPONENT_ID -> command.titlePage().ifPresent(titlePage -> addBlocks(
                        blocks,
                        rendererRegistry.get(TITLE_PAGE_COMPONENT_ID).renderComponent(titlePage, command.profile())
                ));
                case PARAGRAPHS_COMPONENT_ID -> {
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
                }
                default -> throw new IllegalArgumentException("unsupported render component: " + componentId);
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

    private static void validateSelectedContent(ExportDocxCommand command) {
        if (command.selectedComponents().isEmpty()) {
            return;
        }

        for (String selectedComponent : command.selectedComponents()) {
            boolean hasContent = switch (selectedComponent) {
                case COVER_COMPONENT_ID -> command.cover().isPresent();
                case TITLE_PAGE_COMPONENT_ID -> command.titlePage().isPresent();
                case PARAGRAPHS_COMPONENT_ID -> !command.paragraphs().isEmpty();
                default -> true;
            };

            if (!hasContent) {
                throw new IllegalArgumentException("selected component has no content: " + selectedComponent);
            }
        }
    }
}
