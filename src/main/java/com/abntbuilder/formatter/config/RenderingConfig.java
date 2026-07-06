package com.abntbuilder.formatter.config;

import com.abntbuilder.formatter.profile.resolution.ProfileProvider;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;
import com.abntbuilder.formatter.rendering.component.ComponentRendererRegistry;
import com.abntbuilder.formatter.rendering.component.abstracten.AbstractRenderer;
import com.abntbuilder.formatter.rendering.component.annex.AnnexRenderer;
import com.abntbuilder.formatter.rendering.component.appendix.AppendixRenderer;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyContentRenderer;
import com.abntbuilder.formatter.rendering.component.flowtextual.FlowTextualRenderer;
import com.abntbuilder.formatter.rendering.component.listofcharts.ListOfChartsRenderer;
import com.abntbuilder.formatter.rendering.component.listofcodelistings.ListOfCodeListingsRenderer;
import com.abntbuilder.formatter.rendering.component.listoffigures.ListOfFiguresRenderer;
import com.abntbuilder.formatter.rendering.component.listofframes.ListOfFramesRenderer;
import com.abntbuilder.formatter.rendering.component.listoftables.ListOfTablesRenderer;
import com.abntbuilder.formatter.rendering.component.references.ReferencesRenderer;
import com.abntbuilder.formatter.rendering.component.singlepage.SinglePageContentValidator;
import com.abntbuilder.formatter.rendering.component.singlepage.SinglePageLayoutAssembler;
import com.abntbuilder.formatter.rendering.component.singlepage.SinglePageLayoutCalculator;
import com.abntbuilder.formatter.rendering.component.singlepage.SinglePageRenderer;
import com.abntbuilder.formatter.rendering.component.summary.SummaryRenderer;
import com.abntbuilder.formatter.rendering.layout.singlepage.HorizontalPlacementResolver;
import com.abntbuilder.formatter.rendering.layout.singlepage.MarginBasedSinglePageSafetyPolicy;
import com.abntbuilder.formatter.rendering.layout.singlepage.OrderedLayoutGapResolver;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageGapDistributor;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutEngine;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutLineMetrics;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutRenderer;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageSafetyPolicy;
import com.abntbuilder.formatter.rendering.orchestration.ComponentSelectionResolver;
import com.abntbuilder.formatter.rendering.orchestration.DocumentRenderer;
import com.abntbuilder.formatter.rendering.layout.text.FontMetricsTextMeasurer;
import com.abntbuilder.formatter.rendering.layout.text.TextMeasurer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties(TextMeasurementProperties.class)
public class RenderingConfig {

    @Bean
    public TextMeasurer textMeasurer(TextMeasurementProperties properties) {
        return new FontMetricsTextMeasurer(properties.getMissingFontPolicy());
    }

    @Bean
    public ProfileProvider profileProvider() {
        return new ClasspathJsonProfileProvider();
    }

    @Bean
    public SinglePageLayoutLineMetrics singlePageLayoutLineMetrics() {
        return new SinglePageLayoutLineMetrics();
    }

    @Bean
    public SinglePageSafetyPolicy singlePageSafetyPolicy() {
        return new MarginBasedSinglePageSafetyPolicy();
    }

    @Bean
    public SinglePageGapDistributor singlePageGapDistributor() {
        return new SinglePageGapDistributor();
    }

    @Bean
    public OrderedLayoutGapResolver orderedLayoutGapResolver() {
        return new OrderedLayoutGapResolver();
    }

    @Bean
    public HorizontalPlacementResolver horizontalPlacementResolver() {
        return new HorizontalPlacementResolver();
    }

    @Bean
    public SinglePageLayoutEngine singlePageLayoutEngine(
            SinglePageLayoutLineMetrics lineMetrics,
            SinglePageSafetyPolicy safetyPolicy,
            SinglePageGapDistributor gapDistributor
    ) {
        return new SinglePageLayoutEngine(lineMetrics, safetyPolicy, gapDistributor);
    }

    @Bean
    public SinglePageLayoutRenderer singlePageLayoutRenderer() {
        return new SinglePageLayoutRenderer();
    }

    @Bean
    public SinglePageContentValidator singlePageContentValidator() {
        return new SinglePageContentValidator();
    }

    @Bean
    public SinglePageLayoutAssembler singlePageLayoutAssembler(
            TextMeasurer textMeasurer,
            OrderedLayoutGapResolver orderedLayoutGapResolver,
            HorizontalPlacementResolver horizontalPlacementResolver
    ) {
        return new SinglePageLayoutAssembler(textMeasurer, orderedLayoutGapResolver, horizontalPlacementResolver);
    }

    @Bean
    public SinglePageLayoutCalculator singlePageLayoutCalculator(
            SinglePageContentValidator validator,
            SinglePageLayoutAssembler assembler,
            SinglePageLayoutEngine layoutEngine
    ) {
        return new SinglePageLayoutCalculator(validator, assembler, layoutEngine);
    }

    @Bean
    public SinglePageRenderer coverRenderer(
            SinglePageLayoutCalculator layoutCalculator,
            SinglePageLayoutRenderer layoutRenderer
    ) {
        return new SinglePageRenderer("cover", layoutCalculator, layoutRenderer);
    }

    @Bean
    public SinglePageRenderer titlePageRenderer(
            SinglePageLayoutCalculator layoutCalculator,
            SinglePageLayoutRenderer layoutRenderer
    ) {
        return new SinglePageRenderer("titlePage", layoutCalculator, layoutRenderer);
    }

    @Bean
    public SinglePageRenderer approvalSheetRenderer(
            SinglePageLayoutCalculator layoutCalculator,
            SinglePageLayoutRenderer layoutRenderer
    ) {
        return new SinglePageRenderer("approvalSheet", layoutCalculator, layoutRenderer);
    }

    @Bean
    public BodyContentRenderer bodyContentRenderer() {
        return new BodyContentRenderer();
    }

    @Bean
    public FlowTextualRenderer errataRenderer() {
        return new FlowTextualRenderer("errata");
    }

    @Bean
    public FlowTextualRenderer dedicationRenderer() {
        return new FlowTextualRenderer("dedication");
    }

    @Bean
    public FlowTextualRenderer epigraphRenderer() {
        return new FlowTextualRenderer("epigraph");
    }

    @Bean
    public FlowTextualRenderer acknowledgmentsRenderer() {
        return new FlowTextualRenderer("acknowledgments");
    }

    @Bean
    public FlowTextualRenderer resumoRenderer() {
        return new FlowTextualRenderer("resumo");
    }

    @Bean
    public AbstractRenderer abstractRenderer() {
        return new AbstractRenderer();
    }

    @Bean
    public ReferencesRenderer referencesRenderer() {
        return new ReferencesRenderer();
    }

    @Bean
    public AppendixRenderer appendixRenderer() {
        return new AppendixRenderer();
    }

    @Bean
    public AnnexRenderer annexRenderer() {
        return new AnnexRenderer();
    }

    @Bean
    public FlowTextualRenderer glossaryRenderer() {
        return new FlowTextualRenderer("glossary");
    }

    @Bean
    public SummaryRenderer summaryRenderer() {
        return new SummaryRenderer();
    }

    @Bean
    public ListOfFiguresRenderer listOfFiguresRenderer() {
        return new ListOfFiguresRenderer();
    }

    @Bean
    public ListOfTablesRenderer listOfTablesRenderer() {
        return new ListOfTablesRenderer();
    }

    @Bean
    public ListOfFramesRenderer listOfFramesRenderer() {
        return new ListOfFramesRenderer();
    }

    @Bean
    public ListOfChartsRenderer listOfChartsRenderer() {
        return new ListOfChartsRenderer();
    }

    @Bean
    public ListOfCodeListingsRenderer listOfCodeListingsRenderer() {
        return new ListOfCodeListingsRenderer();
    }

    @Bean
    public FlowTextualRenderer listOfAbbreviationsRenderer() {
        return new FlowTextualRenderer("listOfAbbreviations");
    }

    @Bean
    public FlowTextualRenderer listOfSymbolsRenderer() {
        return new FlowTextualRenderer("listOfSymbols");
    }

    @Bean
    public ComponentRendererRegistry componentRendererRegistry(List<ComponentRenderer<?>> renderers) {
        return new ComponentRendererRegistry(renderers);
    }

    @Bean
    public ComponentSelectionResolver componentSelectionResolver() {
        return new ComponentSelectionResolver();
    }

    @Bean
    public DocumentRenderer documentRenderer(
            ComponentRendererRegistry rendererRegistry,
            ComponentSelectionResolver selectionResolver
    ) {
        return new DocumentRenderer(rendererRegistry, selectionResolver);
    }
}
