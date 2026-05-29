package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.profile.model.PageRule;

import java.util.Objects;

public final class MarginBasedSinglePageSafetyPolicy implements SinglePageSafetyPolicy {

    private final SinglePageRenderableAreaCalculator renderableAreaCalculator;

    public MarginBasedSinglePageSafetyPolicy() {
        this(new SinglePageRenderableAreaCalculator());
    }

    public MarginBasedSinglePageSafetyPolicy(SinglePageRenderableAreaCalculator renderableAreaCalculator) {
        this.renderableAreaCalculator = Objects.requireNonNull(
                renderableAreaCalculator,
                "renderableAreaCalculator must not be null"
        );
    }

    @Override
    public SinglePageRenderableArea calculate(PageRule pageRule, int lineHeightTwips) {
        return renderableAreaCalculator.calculate(pageRule, lineHeightTwips);
    }
}
