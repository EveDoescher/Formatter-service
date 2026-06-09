package com.abntbuilder.formatter.rendering.component.approvalsheet;

import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalSheetComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutRenderer;

import java.util.List;
import java.util.Objects;

public final class ApprovalSheetRenderer implements ComponentRenderer<ApprovalSheetComponent> {

    public static final String COMPONENT_ID = "approvalSheet";

    private final ApprovalSheetLayoutCalculator layoutCalculator;
    private final SinglePageLayoutRenderer singlePageRenderer;

    public ApprovalSheetRenderer(
            ApprovalSheetLayoutCalculator layoutCalculator,
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
    public Class<ApprovalSheetComponent> componentType() {
        return ApprovalSheetComponent.class;
    }

    @Override
    public List<DocxBlock> render(ApprovalSheetComponent component, DocumentProfile profile) {
        Objects.requireNonNull(component, "component must not be null");
        Objects.requireNonNull(profile, "profile must not be null");

        ApprovalSheetLayoutPlan plan = layoutCalculator.calculate(component, profile);

        return singlePageRenderer.render(plan.layoutPlan());
    }
}
