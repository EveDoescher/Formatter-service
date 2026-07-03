package com.abntbuilder.formatter.rendering.component.singlepage;

import com.abntbuilder.formatter.document.component.singlepage.SinglePageContent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutPlan;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutRenderer;

import java.util.List;
import java.util.Objects;

public final class SinglePageRenderer implements ComponentRenderer<SinglePageContent> {

    private final String componentId;
    private final SinglePageLayoutCalculator layoutCalculator;
    private final SinglePageLayoutRenderer layoutRenderer;

    public SinglePageRenderer(
            String componentId,
            SinglePageLayoutCalculator layoutCalculator,
            SinglePageLayoutRenderer layoutRenderer
    ) {
        if (componentId == null || componentId.isBlank()) {
            throw new IllegalArgumentException("componentId must not be blank.");
        }
        this.componentId = componentId;
        this.layoutCalculator = Objects.requireNonNull(layoutCalculator, "layoutCalculator must not be null");
        this.layoutRenderer = Objects.requireNonNull(layoutRenderer, "layoutRenderer must not be null");
    }

    @Override
    public String componentId() {
        return componentId;
    }

    @Override
    public Class<SinglePageContent> componentType() {
        return SinglePageContent.class;
    }

    @Override
    public List<DocxBlock> render(SinglePageContent content, DocumentProfile profile) {
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(profile, "profile must not be null");

        SinglePageLayoutPlan plan = layoutCalculator.calculate(content, profile);
        return layoutRenderer.render(plan);
    }
}
