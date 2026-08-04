package com.abntbuilder.formatter.config;

import com.abntbuilder.formatter.input.profile.ClasspathJsonProfileProvider;
import com.abntbuilder.formatter.input.profile.ProfileProvider;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageContentValidator;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageLayoutAssembler;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageLayoutCalculator;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageLayoutRenderer;
import com.abntbuilder.formatter.rendering.singlepage.HorizontalPlacementResolver;
import com.abntbuilder.formatter.rendering.singlepage.MarginBasedSinglePageSafetyPolicy;
import com.abntbuilder.formatter.rendering.singlepage.OrderedLayoutGapResolver;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageGapDistributor;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageLayoutEngine;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageLayoutLineMetrics;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageSafetyPolicy;
import com.abntbuilder.formatter.rendering.orchestration.ComponentSelectionResolver;
import com.abntbuilder.formatter.rendering.orchestration.DocumentRenderer;
import com.abntbuilder.formatter.rendering.text.FontMetricsTextMeasurer;
import com.abntbuilder.formatter.rendering.text.TextMeasurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TextMeasurementProperties.class)
public class RenderingConfig {

    @Bean
    public TextMeasurer textMeasurer(TextMeasurementProperties properties) {
        return new FontMetricsTextMeasurer(properties.getMissingFontPolicy());
    }

    @Bean
    @ConditionalOnMissingBean(ProfileProvider.class)
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
    public ComponentSelectionResolver componentSelectionResolver() {
        return new ComponentSelectionResolver();
    }

    @Bean
    public DocumentRenderer documentRenderer(
            ComponentSelectionResolver selectionResolver,
            SinglePageLayoutCalculator singlePageLayoutCalculator,
            SinglePageLayoutRenderer singlePageLayoutRenderer
    ) {
        return new DocumentRenderer(selectionResolver, singlePageLayoutCalculator, singlePageLayoutRenderer);
    }
}
