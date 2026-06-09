package com.abntbuilder.formatter.rendering.component.approvalsheet;

import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalSheetComponent;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.component.approvalsheet.ApprovalSheetComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutEngine;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutInput;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutPlan;

import java.util.Objects;

public final class ApprovalSheetLayoutCalculator {

    private final ApprovalSheetLayoutAssembler assembler;
    private final SinglePageLayoutEngine layoutEngine;

    public ApprovalSheetLayoutCalculator(
            ApprovalSheetLayoutAssembler assembler,
            SinglePageLayoutEngine layoutEngine
    ) {
        this.assembler = Objects.requireNonNull(assembler, "assembler must not be null");
        this.layoutEngine = Objects.requireNonNull(layoutEngine, "layoutEngine must not be null");
    }

    public ApprovalSheetLayoutPlan calculate(
            ApprovalSheetComponent component,
            DocumentProfile profile
    ) {
        Objects.requireNonNull(component, "component must not be null");
        Objects.requireNonNull(profile, "profile must not be null");

        ApprovalSheetComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(ApprovalSheetRenderer.COMPONENT_ID, ApprovalSheetComponentRule.class);
        SinglePageLayoutInput input = assembler.assemble(component, profile, rule);
        SinglePageLayoutPlan layoutPlan = layoutEngine.calculate(input);

        return new ApprovalSheetLayoutPlan(layoutPlan);
    }
}
