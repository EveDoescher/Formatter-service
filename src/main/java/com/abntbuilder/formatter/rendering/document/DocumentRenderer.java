package com.abntbuilder.formatter.rendering.document;

import com.abntbuilder.formatter.application.export.ExportDocxCommand;
import com.abntbuilder.formatter.document.component.DocumentComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxDocument;
import com.abntbuilder.formatter.output.docx.api.DocxPageNumbering;
import com.abntbuilder.formatter.output.docx.api.DocxPageBreak;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.output.docx.api.DocxSectionBreak;
import com.abntbuilder.formatter.profile.model.PageNumberingRule;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.ComponentRendererRegistry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
        Optional<PageNumberingRule> pageNumberingRule = command.profile().pageNumberingRule()
                .filter(PageNumberingRule::enabled);
        PageNumberingState pageNumberingState = new PageNumberingState(pageNumberingRule, styleResolver);
        Optional<DocxPageNumbering> initialPageNumbering = Optional.empty();

        validateSelectedContent(command, documentComponentsById);
        validateSelectedPageNumberingComponents(command.selectedComponents(), componentOrder, pageNumberingRule);

        for (String componentId : componentOrder) {
            if (!selectionResolver.shouldRender(componentId, command.selectedComponents())) {
                continue;
            }

            if (PARAGRAPHS_COMPONENT_ID.equals(componentId)) {
                if (!command.paragraphs().isEmpty()) {
                    Optional<DocxPageNumbering> pageNumbering = pageNumberingState.beforeRendering(componentId);
                    if (blocks.isEmpty() && pageNumbering.isPresent()) {
                        initialPageNumbering = pageNumbering;
                    }

                    addBlocks(blocks, pageNumbering, command.paragraphs()
                            .stream()
                            .map(paragraph -> new DocxParagraph(
                                    List.of(DocxRun.of(paragraph.text(), styleResolver.resolve(paragraph.styleId()))),
                                    styleResolver.resolve(paragraph.styleId())
                            ))
                            .map(DocxBlock.class::cast)
                            .toList());
                    pageNumberingState.afterRendering();
                }
                continue;
            }

            DocumentComponent component = documentComponentsById.get(componentId);

            if (component != null) {
                Optional<DocxPageNumbering> pageNumbering = pageNumberingState.beforeRendering(componentId);
                if (blocks.isEmpty() && pageNumbering.isPresent()) {
                    initialPageNumbering = pageNumbering;
                }

                addBlocks(
                        blocks,
                        pageNumbering,
                        rendererRegistry.get(componentId).renderComponent(component, command.profile())
                );
                pageNumberingState.afterRendering();
            }
        }

        if (blocks.isEmpty()) {
            throw new IllegalArgumentException("document must contain at least one selected renderable component.");
        }

        return new DocxDocument(
                command.profile().pageRule(),
                initialPageNumbering,
                blocks
        );
    }

    private static void addBlocks(
            List<DocxBlock> blocks,
            Optional<DocxPageNumbering> pageNumbering,
            List<DocxBlock> newBlocks
    ) {
        if (newBlocks.isEmpty()) {
            return;
        }

        if (!blocks.isEmpty()) {
            blocks.add(pageNumbering
                    .<DocxBlock>map(DocxSectionBreak::new)
                    .orElseGet(DocxPageBreak::new));
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

    private static void validateSelectedPageNumberingComponents(
            List<String> selectedComponents,
            List<String> componentOrder,
            Optional<PageNumberingRule> pageNumberingRule
    ) {
        if (selectedComponents.isEmpty() || pageNumberingRule.isEmpty()) {
            return;
        }

        PageNumberingRule rule = pageNumberingRule.orElseThrow();
        int countFromIndex = componentOrder.indexOf(rule.countFromComponentId());
        int visibleFromIndex = componentOrder.indexOf(rule.visibleFromComponentId());

        boolean selectedReachesCountingArea = selectedComponents.stream()
                .mapToInt(componentOrder::indexOf)
                .anyMatch(index -> index >= countFromIndex);
        boolean selectedReachesVisibleArea = selectedComponents.stream()
                .mapToInt(componentOrder::indexOf)
                .anyMatch(index -> index >= visibleFromIndex);

        if (selectedReachesCountingArea && !selectedComponents.contains(rule.countFromComponentId())) {
            throw new IllegalArgumentException(
                    "selectedComponents must include pageNumbering.countFromComponentId: "
                            + rule.countFromComponentId()
            );
        }

        if (selectedReachesVisibleArea && !selectedComponents.contains(rule.visibleFromComponentId())) {
            throw new IllegalArgumentException(
                    "selectedComponents must include pageNumbering.visibleFromComponentId: "
                            + rule.visibleFromComponentId()
            );
        }
    }

    private static final class PageNumberingState {
        private final Optional<PageNumberingRule> rule;
        private final StyleResolver styleResolver;
        private boolean countingStarted;

        private PageNumberingState(Optional<PageNumberingRule> rule, StyleResolver styleResolver) {
            this.rule = Objects.requireNonNull(rule, "rule must not be null");
            this.styleResolver = Objects.requireNonNull(styleResolver, "styleResolver must not be null");
        }

        private Optional<DocxPageNumbering> beforeRendering(String componentId) {
            if (rule.isEmpty()) {
                return Optional.empty();
            }

            PageNumberingRule resolvedRule = rule.orElseThrow();
            boolean startsCountingHere = resolvedRule.countFromComponentId().equals(componentId);
            boolean becomesVisibleHere = resolvedRule.visibleFromComponentId().equals(componentId);

            if (startsCountingHere) {
                countingStarted = true;
            }

            if (!startsCountingHere && !becomesVisibleHere) {
                return Optional.empty();
            }

            if (becomesVisibleHere && !countingStarted) {
                countingStarted = true;
            }

            return Optional.of(new DocxPageNumbering(
                    styleResolver.resolve(resolvedRule.styleId()),
                    resolvedRule.placement(),
                    startsCountingHere,
                    becomesVisibleHere,
                    resolvedRule.verticalDistanceFromPageEdgeCm(),
                    resolvedRule.horizontalDistanceFromPageEdgeCm()
            ));
        }

        private void afterRendering() {
        }
    }
}
