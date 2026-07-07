package com.abntbuilder.formatter.rendering.singlepage;

import com.abntbuilder.formatter.engine.model.content.singlepage.SinglePageContent;
import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;
import com.abntbuilder.formatter.engine.model.profile.component.singlepage.SinglePageComponentRule;
import com.abntbuilder.formatter.input.profile.ComponentRuleResolver;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageLayoutEngine;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageLayoutInput;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageLayoutPlan;
import com.abntbuilder.formatter.shared.exception.SinglePageLayoutOverflowException;

import java.util.Objects;

public final class SinglePageLayoutCalculator {

    private final SinglePageContentValidator validator;
    private final SinglePageLayoutAssembler assembler;
    private final SinglePageLayoutEngine layoutEngine;

    public SinglePageLayoutCalculator(
            SinglePageContentValidator validator,
            SinglePageLayoutAssembler assembler,
            SinglePageLayoutEngine layoutEngine
    ) {
        this.validator = Objects.requireNonNull(validator, "validator must not be null");
        this.assembler = Objects.requireNonNull(assembler, "assembler must not be null");
        this.layoutEngine = Objects.requireNonNull(layoutEngine, "layoutEngine must not be null");
    }

    public SinglePageLayoutPlan calculate(SinglePageContent content, DocumentProfile profile) {
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(profile, "profile must not be null");

        SinglePageComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(content.componentId(), SinglePageComponentRule.class);

        validator.validate(content, rule, profile);

        SinglePageLayoutInput input = assembler.assemble(content, profile, rule);

        try {
            return layoutEngine.calculate(input);
        } catch (SinglePageLayoutOverflowException exception) {
            throw new SinglePageLayoutOverflowException(
                    exception.singlePageDiagnostic()
                            .orElseThrow(() -> exception)
            );
        }
    }
}
