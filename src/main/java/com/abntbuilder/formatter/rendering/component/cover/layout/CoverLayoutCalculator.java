package com.abntbuilder.formatter.rendering.component.cover.layout;

import com.abntbuilder.formatter.document.component.cover.CoverComponent;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.component.cover.CoverComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.rendering.layout.singlepage.MarginBasedSinglePageSafetyPolicy;
import com.abntbuilder.formatter.rendering.layout.singlepage.OrderedLayoutGapResolver;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageGapDistributor;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutEngine;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutInput;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutLineMetrics;
import com.abntbuilder.formatter.rendering.layout.text.FontMetricsTextMeasurer;
import com.abntbuilder.formatter.shared.exception.SinglePageLayoutOverflowException;

import java.util.Objects;

public final class CoverLayoutCalculator {

    private static final String COVER_COMPONENT_ID = "cover";

    private final CoverLayoutAssembler assembler;
    private final SinglePageLayoutEngine layoutEngine;

    public CoverLayoutCalculator() {
        this(
                new CoverLayoutAssembler(
                        new FontMetricsTextMeasurer(),
                        new OrderedLayoutGapResolver(),
                        new CoverProfileContentValidator()
                ),
                new SinglePageLayoutEngine(
                        new SinglePageLayoutLineMetrics(),
                        new MarginBasedSinglePageSafetyPolicy(),
                        new SinglePageGapDistributor()
                )
        );
    }

    public CoverLayoutCalculator(
            CoverLayoutAssembler assembler,
            SinglePageLayoutEngine layoutEngine
    ) {
        this.assembler = Objects.requireNonNull(assembler, "assembler must not be null");
        this.layoutEngine = Objects.requireNonNull(layoutEngine, "layoutEngine must not be null");
    }

    public CoverLayoutPlan calculate(CoverComponent cover, DocumentProfile profile) {
        Objects.requireNonNull(cover, "cover must not be null");
        Objects.requireNonNull(profile, "profile must not be null");

        CoverComponentRule coverRule = new ComponentRuleResolver(profile).resolve(
                COVER_COMPONENT_ID,
                CoverComponentRule.class
        );
        SinglePageLayoutInput input = assembler.assemble(cover, profile, coverRule);

        try {
            return new CoverLayoutPlan(layoutEngine.calculate(input));
        } catch (SinglePageLayoutOverflowException exception) {
            CoverLayoutFailureDiagnostic diagnostic = exception.singlePageDiagnostic()
                    .map(CoverLayoutFailureDiagnostic::from)
                    .orElseThrow(() -> exception);

            throw new CoverLayoutOverflowException(diagnostic);
        }
    }
}
