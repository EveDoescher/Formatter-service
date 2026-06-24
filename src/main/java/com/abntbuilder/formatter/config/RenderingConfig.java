package com.abntbuilder.formatter.config;

import com.abntbuilder.formatter.profile.resolution.ProfileProvider;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;
import com.abntbuilder.formatter.rendering.component.ComponentRendererRegistry;
import com.abntbuilder.formatter.rendering.component.approvalsheet.ApprovalSheetLayoutAssembler;
import com.abntbuilder.formatter.rendering.component.approvalsheet.ApprovalSheetLayoutCalculator;
import com.abntbuilder.formatter.rendering.component.approvalsheet.ApprovalSheetProfileContentValidator;
import com.abntbuilder.formatter.rendering.component.approvalsheet.ApprovalSheetRenderer;
import com.abntbuilder.formatter.rendering.component.approvalsheet.ApprovalSheetTextTemplateResolver;
import com.abntbuilder.formatter.rendering.component.abstracten.AbstractRenderer;
import com.abntbuilder.formatter.rendering.component.acknowledgments.AcknowledgmentsRenderer;
import com.abntbuilder.formatter.rendering.component.annex.AnnexRenderer;
import com.abntbuilder.formatter.rendering.component.appendix.AppendixRenderer;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyContentRenderer;
import com.abntbuilder.formatter.rendering.component.cover.CoverRenderer;
import com.abntbuilder.formatter.rendering.component.dedication.DedicationRenderer;
import com.abntbuilder.formatter.rendering.component.epigraph.EpigraphRenderer;
import com.abntbuilder.formatter.rendering.component.errata.ErrataRenderer;
import com.abntbuilder.formatter.rendering.component.glossary.GlossaryRenderer;
import com.abntbuilder.formatter.rendering.component.listofabbreviations.ListOfAbbreviationsRenderer;
import com.abntbuilder.formatter.rendering.component.listofcharts.ListOfChartsRenderer;
import com.abntbuilder.formatter.rendering.component.listofcodelistings.ListOfCodeListingsRenderer;
import com.abntbuilder.formatter.rendering.component.listoffigures.ListOfFiguresRenderer;
import com.abntbuilder.formatter.rendering.component.listofframes.ListOfFramesRenderer;
import com.abntbuilder.formatter.rendering.component.listoftables.ListOfTablesRenderer;
import com.abntbuilder.formatter.rendering.component.listofsymbols.ListOfSymbolsRenderer;
import com.abntbuilder.formatter.rendering.component.references.ReferencesRenderer;
import com.abntbuilder.formatter.rendering.component.resumo.ResumoRenderer;
import com.abntbuilder.formatter.rendering.component.summary.SummaryRenderer;
import com.abntbuilder.formatter.rendering.component.cover.layout.CoverLayoutAssembler;
import com.abntbuilder.formatter.rendering.component.cover.layout.CoverLayoutCalculator;
import com.abntbuilder.formatter.rendering.component.cover.layout.CoverProfileContentValidator;
import com.abntbuilder.formatter.rendering.component.titlepage.TitlePageLayoutAssembler;
import com.abntbuilder.formatter.rendering.component.titlepage.TitlePageLayoutCalculator;
import com.abntbuilder.formatter.rendering.component.titlepage.TitlePageProfileContentValidator;
import com.abntbuilder.formatter.rendering.component.titlepage.TitlePageRenderer;
import com.abntbuilder.formatter.rendering.component.titlepage.TitlePageTextTemplateResolver;
import com.abntbuilder.formatter.rendering.orchestration.ComponentSelectionResolver;
import com.abntbuilder.formatter.rendering.orchestration.DocumentRenderer;
import com.abntbuilder.formatter.rendering.layout.singlepage.HorizontalPlacementResolver;
import com.abntbuilder.formatter.rendering.layout.singlepage.MarginBasedSinglePageSafetyPolicy;
import com.abntbuilder.formatter.rendering.layout.singlepage.OrderedLayoutGapResolver;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageGapDistributor;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutEngine;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutLineMetrics;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutRenderer;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageSafetyPolicy;
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
    public CoverProfileContentValidator coverProfileContentValidator() {
        return new CoverProfileContentValidator();
    }

    @Bean
    public CoverLayoutAssembler coverLayoutAssembler(
            TextMeasurer textMeasurer,
            OrderedLayoutGapResolver gapResolver,
            CoverProfileContentValidator validator,
            HorizontalPlacementResolver horizontalPlacementResolver
    ) {
        return new CoverLayoutAssembler(textMeasurer, gapResolver, validator, horizontalPlacementResolver);
    }

    @Bean
    public CoverLayoutCalculator coverLayoutCalculator(
            CoverLayoutAssembler assembler,
            SinglePageLayoutEngine layoutEngine
    ) {
        return new CoverLayoutCalculator(assembler, layoutEngine);
    }

    @Bean
    public SinglePageLayoutRenderer singlePageLayoutRenderer() {
        return new SinglePageLayoutRenderer();
    }

    @Bean
    public CoverRenderer coverRenderer(
            CoverLayoutCalculator layoutCalculator,
            SinglePageLayoutRenderer singlePageRenderer
    ) {
        return new CoverRenderer(layoutCalculator, singlePageRenderer);
    }

    @Bean
    public TitlePageProfileContentValidator titlePageProfileContentValidator() {
        return new TitlePageProfileContentValidator();
    }

    @Bean
    public TitlePageTextTemplateResolver titlePageTextTemplateResolver() {
        return new TitlePageTextTemplateResolver();
    }

    @Bean
    public TitlePageLayoutAssembler titlePageLayoutAssembler(
            TextMeasurer textMeasurer,
            OrderedLayoutGapResolver gapResolver,
            TitlePageProfileContentValidator validator,
            TitlePageTextTemplateResolver templateResolver,
            HorizontalPlacementResolver horizontalPlacementResolver
    ) {
        return new TitlePageLayoutAssembler(
                textMeasurer,
                gapResolver,
                validator,
                templateResolver,
                horizontalPlacementResolver
        );
    }

    @Bean
    public TitlePageLayoutCalculator titlePageLayoutCalculator(
            TitlePageLayoutAssembler assembler,
            SinglePageLayoutEngine layoutEngine
    ) {
        return new TitlePageLayoutCalculator(assembler, layoutEngine);
    }

    @Bean
    public TitlePageRenderer titlePageRenderer(
            TitlePageLayoutCalculator layoutCalculator,
            SinglePageLayoutRenderer singlePageRenderer
    ) {
        return new TitlePageRenderer(layoutCalculator, singlePageRenderer);
    }

    @Bean
    public ApprovalSheetProfileContentValidator approvalSheetProfileContentValidator() {
        return new ApprovalSheetProfileContentValidator();
    }

    @Bean
    public ApprovalSheetTextTemplateResolver approvalSheetTextTemplateResolver() {
        return new ApprovalSheetTextTemplateResolver();
    }

    @Bean
    public ApprovalSheetLayoutAssembler approvalSheetLayoutAssembler(
            TextMeasurer textMeasurer,
            OrderedLayoutGapResolver gapResolver,
            ApprovalSheetProfileContentValidator validator,
            ApprovalSheetTextTemplateResolver templateResolver,
            HorizontalPlacementResolver horizontalPlacementResolver
    ) {
        return new ApprovalSheetLayoutAssembler(
                textMeasurer,
                gapResolver,
                validator,
                templateResolver,
                horizontalPlacementResolver
        );
    }

    @Bean
    public ApprovalSheetLayoutCalculator approvalSheetLayoutCalculator(
            ApprovalSheetLayoutAssembler assembler,
            SinglePageLayoutEngine layoutEngine
    ) {
        return new ApprovalSheetLayoutCalculator(assembler, layoutEngine);
    }

    @Bean
    public ApprovalSheetRenderer approvalSheetRenderer(
            ApprovalSheetLayoutCalculator layoutCalculator,
            SinglePageLayoutRenderer singlePageRenderer
    ) {
        return new ApprovalSheetRenderer(layoutCalculator, singlePageRenderer);
    }

    @Bean
    public BodyContentRenderer bodyContentRenderer() {
        return new BodyContentRenderer();
    }

    @Bean
    public ErrataRenderer errataRenderer() {
        return new ErrataRenderer();
    }

    @Bean
    public DedicationRenderer dedicationRenderer() {
        return new DedicationRenderer();
    }

    @Bean
    public EpigraphRenderer epigraphRenderer() {
        return new EpigraphRenderer();
    }

    @Bean
    public AcknowledgmentsRenderer acknowledgmentsRenderer() {
        return new AcknowledgmentsRenderer();
    }

    @Bean
    public ResumoRenderer resumoRenderer() {
        return new ResumoRenderer();
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
    public GlossaryRenderer glossaryRenderer() {
        return new GlossaryRenderer();
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
    public ListOfAbbreviationsRenderer listOfAbbreviationsRenderer() {
        return new ListOfAbbreviationsRenderer();
    }

    @Bean
    public ListOfSymbolsRenderer listOfSymbolsRenderer() {
        return new ListOfSymbolsRenderer();
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
