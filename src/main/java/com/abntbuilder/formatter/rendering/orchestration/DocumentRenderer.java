package com.abntbuilder.formatter.rendering.orchestration;

import com.abntbuilder.formatter.application.export.ExportDocxCommand;
import com.abntbuilder.formatter.engine.model.content.DocumentComponent;
import com.abntbuilder.formatter.engine.model.output.DocxBlock;
import com.abntbuilder.formatter.engine.model.output.DocxDocument;
import com.abntbuilder.formatter.engine.model.output.DocxPageNumbering;
import com.abntbuilder.formatter.engine.model.output.DocxPageBreak;
import com.abntbuilder.formatter.engine.model.output.DocxParagraph;
import com.abntbuilder.formatter.engine.model.output.DocxRun;
import com.abntbuilder.formatter.engine.model.output.DocxSectionBreak;
import com.abntbuilder.formatter.engine.model.profile.PageNumberingRule;
import com.abntbuilder.formatter.input.profile.StyleResolver;
import com.abntbuilder.formatter.engine.contract.ComponentRenderResult;
import com.abntbuilder.formatter.engine.contract.ComponentRendererRegistry;
import com.abntbuilder.formatter.engine.contract.MetadataConsumingRenderer;
import com.abntbuilder.formatter.engine.contract.MetadataEmittingRenderer;
import com.abntbuilder.formatter.engine.contract.Phase0ConsumingRenderer;
import com.abntbuilder.formatter.rendering.bodycontent.BodyContentMetadata;
import com.abntbuilder.formatter.rendering.bodycontent.BodyContentRenderResult;
import com.abntbuilder.formatter.rendering.phase0.DisplayObjectCollector;
import com.abntbuilder.formatter.rendering.phase0.Phase0Index;

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
    private final DisplayObjectCollector displayObjectCollector;

    public DocumentRenderer(
            ComponentRendererRegistry rendererRegistry,
            ComponentSelectionResolver selectionResolver
    ) {
        this.rendererRegistry = Objects.requireNonNull(rendererRegistry, "rendererRegistry must not be null");
        this.selectionResolver = Objects.requireNonNull(selectionResolver, "selectionResolver must not be null");
        this.displayObjectCollector = new DisplayObjectCollector();
    }

    public DocxDocument render(ExportDocxCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        List<String> componentOrder = command.profile().componentOrder();
        selectionResolver.validateSupportedSelections(
                command.selectedComponents(),
                new LinkedHashSet<>(componentOrder)
        );

        StyleResolver styleResolver = new StyleResolver(command.profile(), command.fontPreferences());
        List<DocxBlock> blocks = new ArrayList<>();
        Map<String, DocumentComponent> documentComponentsById = documentComponentsById(command);
        Optional<PageNumberingRule> pageNumberingRule = command.profile().pageNumberingRule()
                .filter(PageNumberingRule::enabled);
        PageNumberingState pageNumberingState = new PageNumberingState(pageNumberingRule, styleResolver);
        Optional<DocxPageNumbering> initialPageNumbering = Optional.empty();

        validateSelectedContent(command, documentComponentsById);
        validateSelectedPageNumberingComponents(command.selectedComponents(), componentOrder, pageNumberingRule);

        Phase0Index phase0Index = displayObjectCollector.collect(
                command.documentComponents(), command.profile());
        BodyContentMetadata bodyContentMetadata = BodyContentMetadata.empty();

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

                var renderer = rendererRegistry.get(componentId);
                List<DocxBlock> componentBlocks;

                try {
                    if (renderer instanceof Phase0ConsumingRenderer<?,?> phase0Consumer) {
                        ComponentRenderResult result = phase0Consumer.renderComponentWithPhase0(component, command.profile(), phase0Index);
                        componentBlocks = result.blocks();
                        if (result instanceof BodyContentRenderResult bcr) {
                            bodyContentMetadata = bcr.metadata();
                        }
                    } else if (renderer instanceof MetadataEmittingRenderer<?,?> emitting) {
                        ComponentRenderResult result = emitting.renderComponentWithMetadata(component, command.profile());
                        componentBlocks = result.blocks();
                        if (result instanceof BodyContentRenderResult bcr) {
                            bodyContentMetadata = bcr.metadata();
                        }
                    } else if (renderer instanceof MetadataConsumingRenderer<?> consuming) {
                        componentBlocks = consuming.renderComponentWithMetadata(component, command.profile(), phase0Index);
                    } else {
                        componentBlocks = renderer.renderComponent(component, command.profile());
                    }
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(
                            "Error rendering component '" + componentId + "': " + e.getMessage(), e);
                }

                addBlocks(blocks, pageNumbering, componentBlocks);
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
