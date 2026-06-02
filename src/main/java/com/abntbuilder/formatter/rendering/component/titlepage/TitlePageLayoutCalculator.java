package com.abntbuilder.formatter.rendering.component.titlepage;

import com.abntbuilder.formatter.document.component.titlepage.TitlePageComponent;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutEngine;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutInput;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutPlan;

import java.util.Objects;

public final class TitlePageLayoutCalculator {

    private final TitlePageLayoutAssembler assembler;
    private final SinglePageLayoutEngine layoutEngine;

    public TitlePageLayoutCalculator(
            TitlePageLayoutAssembler assembler,
            SinglePageLayoutEngine layoutEngine
    ) {
        this.assembler = Objects.requireNonNull(assembler, "assembler must not be null");
        this.layoutEngine = Objects.requireNonNull(layoutEngine, "layoutEngine must not be null");
    }

    public TitlePageLayoutPlan calculate(
            TitlePageComponent component,
            DocumentProfile profile
    ) {
        Objects.requireNonNull(component, "component must not be null");
        Objects.requireNonNull(profile, "profile must not be null");

        TitlePageComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(TitlePageRenderer.COMPONENT_ID, TitlePageComponentRule.class);
        SinglePageLayoutInput input = assembler.assemble(component, profile, rule);
        SinglePageLayoutPlan layoutPlan = layoutEngine.calculate(input);

        return new TitlePageLayoutPlan(layoutPlan);
    }
}
