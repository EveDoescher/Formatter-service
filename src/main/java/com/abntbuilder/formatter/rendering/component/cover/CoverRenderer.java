package com.abntbuilder.formatter.rendering.component.cover;

import com.abntbuilder.formatter.document.component.cover.CoverComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.rendering.component.cover.layout.CoverLayoutCalculator;
import com.abntbuilder.formatter.rendering.component.cover.layout.CoverLayoutPlan;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutRenderer;

import java.util.List;
import java.util.Objects;

public final class CoverRenderer {

    private final CoverLayoutCalculator layoutCalculator;
    private final SinglePageLayoutRenderer singlePageRenderer;

    public CoverRenderer(
            CoverLayoutCalculator layoutCalculator,
            SinglePageLayoutRenderer singlePageRenderer
    ) {
        this.layoutCalculator = Objects.requireNonNull(layoutCalculator, "layoutCalculator must not be null");
        this.singlePageRenderer = Objects.requireNonNull(singlePageRenderer, "singlePageRenderer must not be null");
    }

    public List<DocxBlock> render(CoverComponent cover, DocumentProfile profile) {
        Objects.requireNonNull(cover, "cover must not be null");
        Objects.requireNonNull(profile, "profile must not be null");

        CoverLayoutPlan plan = layoutCalculator.calculate(cover, profile);

        return singlePageRenderer.render(plan.layoutPlan());
    }
}
