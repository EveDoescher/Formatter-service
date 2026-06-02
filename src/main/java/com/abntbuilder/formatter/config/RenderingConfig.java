package com.abntbuilder.formatter.config;

import com.abntbuilder.formatter.profile.resolution.InMemoryProfileProvider;
import com.abntbuilder.formatter.profile.resolution.ProfileProvider;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;
import com.abntbuilder.formatter.rendering.component.ComponentRendererRegistry;
import com.abntbuilder.formatter.rendering.component.cover.CoverRenderer;
import com.abntbuilder.formatter.rendering.component.cover.layout.CoverLayoutAssembler;
import com.abntbuilder.formatter.rendering.component.cover.layout.CoverLayoutCalculator;
import com.abntbuilder.formatter.rendering.component.cover.layout.CoverProfileContentValidator;
import com.abntbuilder.formatter.rendering.component.titlepage.TitlePageLayoutAssembler;
import com.abntbuilder.formatter.rendering.component.titlepage.TitlePageLayoutCalculator;
import com.abntbuilder.formatter.rendering.component.titlepage.TitlePageProfileContentValidator;
import com.abntbuilder.formatter.rendering.component.titlepage.TitlePageRenderer;
import com.abntbuilder.formatter.rendering.component.titlepage.TitlePageTextTemplateResolver;
import com.abntbuilder.formatter.rendering.document.ComponentSelectionResolver;
import com.abntbuilder.formatter.rendering.document.DocumentRenderer;
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
        return new InMemoryProfileProvider();
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
