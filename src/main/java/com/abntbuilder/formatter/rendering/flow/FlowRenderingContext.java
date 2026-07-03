package com.abntbuilder.formatter.rendering.flow;

import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.BodyContentComponentRule;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyAbbreviationMetadata;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyDisplayObjectMetadata;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodySectionMetadata;
import com.abntbuilder.formatter.rendering.component.bodycontent.DisplayObjectRenderingState;
import com.abntbuilder.formatter.rendering.component.bodycontent.SectionNumberingState;
import com.abntbuilder.formatter.document.component.bodycontent.BodyChart;
import com.abntbuilder.formatter.document.component.bodycontent.BodyCodeListing;
import com.abntbuilder.formatter.document.component.bodycontent.BodyFigure;
import com.abntbuilder.formatter.document.component.bodycontent.BodyFrame;
import com.abntbuilder.formatter.document.component.bodycontent.BodyTable;
import com.abntbuilder.formatter.rendering.phase0.Phase0Index;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class FlowRenderingContext {

    final Phase0Index phase0Index;
    final BodyContentComponentRule rule;
    final StyleResolver styleResolver;
    final StyleRule blankLineStyle;
    final SectionNumberingState sectionNumberingState;

    final DisplayObjectRenderingState<BodyFigure> figureState;
    final DisplayObjectRenderingState<BodyTable> tableState;
    final DisplayObjectRenderingState<BodyFrame> frameState;
    final DisplayObjectRenderingState<BodyCodeListing> codeListingState;
    final DisplayObjectRenderingState<BodyChart> chartState;

    final List<BodySectionMetadata> sectionMetas = new ArrayList<>();
    final List<BodyDisplayObjectMetadata> figureMetas = new ArrayList<>();
    final List<BodyDisplayObjectMetadata> tableMetas = new ArrayList<>();
    final List<BodyDisplayObjectMetadata> frameMetas = new ArrayList<>();
    final List<BodyDisplayObjectMetadata> chartMetas = new ArrayList<>();
    final List<BodyDisplayObjectMetadata> codeListingMetas = new ArrayList<>();
    final List<BodyAbbreviationMetadata> abbreviationMetas = new ArrayList<>();
    final int[] footnoteCounter = new int[1];

    public List<BodySectionMetadata> sectionMetas() { return sectionMetas; }
    public List<BodyDisplayObjectMetadata> figureMetas() { return figureMetas; }
    public List<BodyDisplayObjectMetadata> tableMetas() { return tableMetas; }
    public List<BodyDisplayObjectMetadata> frameMetas() { return frameMetas; }
    public List<BodyDisplayObjectMetadata> chartMetas() { return chartMetas; }
    public List<BodyDisplayObjectMetadata> codeListingMetas() { return codeListingMetas; }
    public List<BodyAbbreviationMetadata> abbreviationMetas() { return abbreviationMetas; }

    public FlowRenderingContext(
            Phase0Index phase0Index,
            BodyContentComponentRule rule,
            StyleResolver styleResolver,
            DisplayObjectRenderingState<BodyFigure> figureState,
            DisplayObjectRenderingState<BodyTable> tableState,
            DisplayObjectRenderingState<BodyFrame> frameState,
            DisplayObjectRenderingState<BodyCodeListing> codeListingState,
            DisplayObjectRenderingState<BodyChart> chartState
    ) {
        this.phase0Index = Objects.requireNonNull(phase0Index, "phase0Index must not be null");
        this.rule = Objects.requireNonNull(rule, "rule must not be null");
        this.styleResolver = Objects.requireNonNull(styleResolver, "styleResolver must not be null");
        this.blankLineStyle = styleResolver.resolve(rule.layout().blankLineStyleId());
        this.sectionNumberingState = new SectionNumberingState(rule.numbering());
        this.figureState = Objects.requireNonNull(figureState, "figureState must not be null");
        this.tableState = Objects.requireNonNull(tableState, "tableState must not be null");
        this.frameState = Objects.requireNonNull(frameState, "frameState must not be null");
        this.codeListingState = Objects.requireNonNull(codeListingState, "codeListingState must not be null");
        this.chartState = Objects.requireNonNull(chartState, "chartState must not be null");
    }
}
