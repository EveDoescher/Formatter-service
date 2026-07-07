package com.abntbuilder.formatter.config;

import com.abntbuilder.formatter.input.profile.ClasspathJsonProfileProvider;
import com.abntbuilder.formatter.input.profile.ProfileProvider;
import com.abntbuilder.formatter.engine.contract.ComponentRenderer;
import com.abntbuilder.formatter.engine.contract.ComponentRendererRegistry;
import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;
import com.abntbuilder.formatter.engine.model.profile.component.ComponentRule;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.BodyContentComponentRule;
import com.abntbuilder.formatter.engine.model.profile.component.elementindex.ElementIndexComponentRule;
import com.abntbuilder.formatter.engine.model.profile.component.flowtextual.FlowTextualComponentRule;
import com.abntbuilder.formatter.engine.model.profile.component.references.ReferencesComponentRule;
import com.abntbuilder.formatter.engine.model.profile.component.sectionindex.SectionIndexComponentRule;
import com.abntbuilder.formatter.engine.model.profile.component.sectioned.SectionedComponentRule;
import com.abntbuilder.formatter.engine.model.profile.component.singlepage.SinglePageComponentRule;
import com.abntbuilder.formatter.rendering.bodycontent.BodyContentRenderer;
import com.abntbuilder.formatter.rendering.elementindex.ElementIndexRenderer;
import com.abntbuilder.formatter.rendering.flowtextual.FlowTextualRenderer;
import com.abntbuilder.formatter.rendering.references.ReferencesRenderer;
import com.abntbuilder.formatter.rendering.sectionindex.SectionIndexRenderer;
import com.abntbuilder.formatter.rendering.sectioned.SectionedRenderer;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageContentValidator;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageLayoutAssembler;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageLayoutCalculator;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageRenderer;
import com.abntbuilder.formatter.rendering.singlepage.HorizontalPlacementResolver;
import com.abntbuilder.formatter.rendering.singlepage.MarginBasedSinglePageSafetyPolicy;
import com.abntbuilder.formatter.rendering.singlepage.OrderedLayoutGapResolver;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageGapDistributor;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageLayoutEngine;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageLayoutLineMetrics;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageLayoutRenderer;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageSafetyPolicy;
import com.abntbuilder.formatter.rendering.orchestration.ComponentSelectionResolver;
import com.abntbuilder.formatter.rendering.orchestration.DocumentRenderer;
import com.abntbuilder.formatter.rendering.text.FontMetricsTextMeasurer;
import com.abntbuilder.formatter.rendering.text.TextMeasurer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
    public List<ComponentRenderer<?>> componentRenderers(
            ProfileProvider profileProvider,
            SinglePageLayoutCalculator singlePageLayoutCalculator,
            SinglePageLayoutRenderer singlePageLayoutRenderer
    ) {
        Set<String> registered = new LinkedHashSet<>();
        List<ComponentRenderer<?>> renderers = new ArrayList<>();

        for (DocumentProfile profile : profileProvider.allProfiles()) {
            for (ComponentRule rule : profile.componentRules()) {
                String id = rule.componentId();
                if (!registered.add(id)) continue;

                ComponentRenderer<?> renderer = switch (rule) {
                    case SinglePageComponentRule ignored ->
                            new SinglePageRenderer(id, singlePageLayoutCalculator, singlePageLayoutRenderer);
                    case FlowTextualComponentRule ignored -> new FlowTextualRenderer(id);
                    case BodyContentComponentRule ignored -> new BodyContentRenderer(id);
                    case ReferencesComponentRule ignored -> new ReferencesRenderer(id);
                    case SectionedComponentRule ignored -> new SectionedRenderer(id);
                    case SectionIndexComponentRule ignored -> new SectionIndexRenderer(id);
                    case ElementIndexComponentRule ignored -> new ElementIndexRenderer(id);
                    default -> throw new IllegalStateException(
                            "No renderer factory for ComponentRule type: " + rule.getClass().getSimpleName()
                    );
                };
                renderers.add(renderer);
            }
        }

        return renderers;
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
