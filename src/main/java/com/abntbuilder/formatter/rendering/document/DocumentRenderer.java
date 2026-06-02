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
import java.util.List;
import java.util.Objects;
import java.util.Set;

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

        selectionResolver.validateSupportedSelections(
                command.selectedComponents(),
                Set.of(COVER_COMPONENT_ID, TITLE_PAGE_COMPONENT_ID, PARAGRAPHS_COMPONENT_ID)
        );

        StyleResolver styleResolver = new StyleResolver(command.profile());
        List<DocxBlock> blocks = new ArrayList<>();
        boolean shouldRenderCover = selectionResolver.shouldRender(COVER_COMPONENT_ID, command.selectedComponents());
        boolean shouldRenderParagraphs = selectionResolver.shouldRender(
                PARAGRAPHS_COMPONENT_ID,
                command.selectedComponents()
        );
        boolean shouldRenderTitlePage = selectionResolver.shouldRender(
                TITLE_PAGE_COMPONENT_ID,
                command.selectedComponents()
        );

        validateSelectedContent(command);

        if (shouldRenderCover) {
            command.cover().ifPresent(cover -> addBlocks(
                    blocks,
                    rendererRegistry.get(COVER_COMPONENT_ID).renderComponent(cover, command.profile())
            ));
        }

        if (shouldRenderTitlePage) {
            command.titlePage().ifPresent(titlePage -> addBlocks(
                    blocks,
                    rendererRegistry.get(TITLE_PAGE_COMPONENT_ID).renderComponent(titlePage, command.profile())
            ));
        }

        if (shouldRenderParagraphs && !command.paragraphs().isEmpty()) {
            addBlocks(blocks, command.paragraphs()
                    .stream()
                    .map(paragraph -> new DocxParagraph(
                            paragraph.text(),
                            styleResolver.resolve(paragraph.styleId())
                    ))
                    .map(DocxBlock.class::cast)
                    .toList());
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
