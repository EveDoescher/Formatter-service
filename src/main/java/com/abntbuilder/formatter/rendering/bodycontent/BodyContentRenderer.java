package com.abntbuilder.formatter.rendering.bodycontent;

import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyBlock;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyChart;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyCodeListing;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyContentComponent;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyFigure;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyFrame;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodySection;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyTable;
import com.abntbuilder.formatter.engine.model.output.DocxBlock;
import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.BodyContentComponentRule;
import com.abntbuilder.formatter.input.profile.ComponentRuleResolver;
import com.abntbuilder.formatter.input.profile.StyleResolver;
import com.abntbuilder.formatter.engine.contract.Phase0ConsumingRenderer;
import com.abntbuilder.formatter.rendering.bodycontent.FlowLayoutEngine;
import com.abntbuilder.formatter.rendering.bodycontent.FlowRenderingContext;
import com.abntbuilder.formatter.rendering.phase0.Phase0Index;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class BodyContentRenderer
        implements Phase0ConsumingRenderer<BodyContentComponent, BodyContentRenderResult> {

    private final String componentId;

    public BodyContentRenderer(String componentId) {
        if (componentId == null || componentId.isBlank())
            throw new IllegalArgumentException("BodyContentRenderer.componentId must not be blank.");
        this.componentId = componentId;
    }

    @Override
    public String componentId() {
        return componentId;
    }

    @Override
    public Class<BodyContentComponent> componentType() {
        return BodyContentComponent.class;
    }

    @Override
    public BodyContentRenderResult renderWithPhase0(
            BodyContentComponent component, DocumentProfile profile, Phase0Index phase0Index) {
        Objects.requireNonNull(component, "component must not be null");
        Objects.requireNonNull(profile, "profile must not be null");
        Objects.requireNonNull(phase0Index, "phase0Index must not be null");

        BodyContentComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(component.componentId(), BodyContentComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);

        FlowRenderingContext ctx = new FlowRenderingContext(
                phase0Index,
                rule,
                styleResolver,
                new DisplayObjectRenderingState<>(figuresFrom(component.sections())),
                new DisplayObjectRenderingState<>(tablesFrom(component.sections())),
                new DisplayObjectRenderingState<>(framesFrom(component.sections())),
                new DisplayObjectRenderingState<>(codeListingsFrom(component.sections())),
                new DisplayObjectRenderingState<>(chartsFrom(component.sections()))
        );

        List<DocxBlock> blocks = new FlowLayoutEngine().render(component.sections(), ctx);

        BodyContentMetadata metadata = new BodyContentMetadata(
                ctx.sectionMetas(),
                ctx.figureMetas(),
                ctx.tableMetas(),
                ctx.frameMetas(),
                ctx.chartMetas(),
                ctx.codeListingMetas(),
                ctx.abbreviationMetas()
        );
        return new BodyContentRenderResult(blocks, metadata);
    }

    private static List<BodyFigure> figuresFrom(List<BodySection> sections) {
        List<BodyFigure> figures = new ArrayList<>();
        for (BodySection section : sections) {
            for (BodyBlock block : section.blocks()) {
                if (block instanceof BodyFigure figure) figures.add(figure);
            }
        }
        return List.copyOf(figures);
    }

    private static List<BodyTable> tablesFrom(List<BodySection> sections) {
        List<BodyTable> tables = new ArrayList<>();
        for (BodySection section : sections) {
            for (BodyBlock block : section.blocks()) {
                if (block instanceof BodyTable table) tables.add(table);
            }
        }
        return List.copyOf(tables);
    }

    private static List<BodyFrame> framesFrom(List<BodySection> sections) {
        List<BodyFrame> frames = new ArrayList<>();
        for (BodySection section : sections) {
            for (BodyBlock block : section.blocks()) {
                if (block instanceof BodyFrame frame) frames.add(frame);
            }
        }
        return List.copyOf(frames);
    }

    private static List<BodyCodeListing> codeListingsFrom(List<BodySection> sections) {
        List<BodyCodeListing> codeListings = new ArrayList<>();
        for (BodySection section : sections) {
            for (BodyBlock block : section.blocks()) {
                if (block instanceof BodyCodeListing codeListing) codeListings.add(codeListing);
            }
        }
        return List.copyOf(codeListings);
    }

    private static List<BodyChart> chartsFrom(List<BodySection> sections) {
        List<BodyChart> charts = new ArrayList<>();
        for (BodySection section : sections) {
            for (BodyBlock block : section.blocks()) {
                if (block instanceof BodyChart chart) charts.add(chart);
            }
        }
        return List.copyOf(charts);
    }
}
