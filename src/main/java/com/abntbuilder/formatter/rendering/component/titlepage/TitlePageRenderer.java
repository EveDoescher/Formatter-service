package com.abntbuilder.formatter.rendering.component.titlepage;

import com.abntbuilder.formatter.document.component.titlepage.TitlePageComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutRenderer;

import java.util.List;
import java.util.Objects;

public final class TitlePageRenderer implements ComponentRenderer<TitlePageComponent> {

    public static final String COMPONENT_ID = "titlePage";

    private final TitlePageLayoutCalculator layoutCalculator;
    private final SinglePageLayoutRenderer singlePageRenderer;

    public TitlePageRenderer(
            TitlePageLayoutCalculator layoutCalculator,
            SinglePageLayoutRenderer singlePageRenderer
    ) {
        this.layoutCalculator = Objects.requireNonNull(layoutCalculator, "layoutCalculator must not be null");
        this.singlePageRenderer = Objects.requireNonNull(singlePageRenderer, "singlePageRenderer must not be null");
    }

    @Override
    public String componentId() {
        return COMPONENT_ID;
    }

    @Override
    public Class<TitlePageComponent> componentType() {
        return TitlePageComponent.class;
    }

    @Override
    public List<DocxBlock> render(TitlePageComponent component, DocumentProfile profile) {
        Objects.requireNonNull(component, "component must not be null");
        Objects.requireNonNull(profile, "profile must not be null");

        TitlePageLayoutPlan plan = layoutCalculator.calculate(component, profile);

        return singlePageRenderer.render(plan.layoutPlan());
    }
}
