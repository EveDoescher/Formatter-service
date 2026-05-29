package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.LayoutGapRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutPolicy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Deprecated
public final class SinglePageLayoutDocxMapper {

    private final SinglePageLayoutEngine layoutEngine;
    private final SinglePageLayoutRenderer layoutRenderer;

    public SinglePageLayoutDocxMapper() {
        this(new SinglePageLayoutEngine(), new SinglePageLayoutRenderer());
    }

    public SinglePageLayoutDocxMapper(SinglePageLayoutLineMetrics lineMetrics) {
        this(
                new SinglePageLayoutEngine(
                        lineMetrics,
                        new MarginBasedSinglePageSafetyPolicy(),
                        new SinglePageGapDistributor()
                ),
                new SinglePageLayoutRenderer()
        );
    }

    public SinglePageLayoutDocxMapper(
            SinglePageLayoutLineMetrics lineMetrics,
            SinglePageRenderableAreaCalculator renderableAreaCalculator
    ) {
        this(
                new SinglePageLayoutEngine(
                        lineMetrics,
                        new MarginBasedSinglePageSafetyPolicy(renderableAreaCalculator),
                        new SinglePageGapDistributor()
                ),
                new SinglePageLayoutRenderer()
        );
    }

    public SinglePageLayoutDocxMapper(
            SinglePageLayoutLineMetrics lineMetrics,
            SinglePageRenderableAreaCalculator renderableAreaCalculator,
            SinglePageGapDistributor gapDistributor
    ) {
        this(
                new SinglePageLayoutEngine(
                        lineMetrics,
                        new MarginBasedSinglePageSafetyPolicy(renderableAreaCalculator),
                        gapDistributor
                ),
                new SinglePageLayoutRenderer()
        );
    }

    public SinglePageLayoutDocxMapper(
            SinglePageLayoutEngine layoutEngine,
            SinglePageLayoutRenderer layoutRenderer
    ) {
        this.layoutEngine = Objects.requireNonNull(layoutEngine, "layoutEngine must not be null");
        this.layoutRenderer = Objects.requireNonNull(layoutRenderer, "layoutRenderer must not be null");
    }

    public List<DocxBlock> mapToDocxBlocksAnchoringLastGroup(
            PageRule pageRule,
            List<SinglePageLayoutGroup> groups,
            List<BigDecimal> gapWeights
    ) {
        Objects.requireNonNull(pageRule, "pageRule must not be null");
        Objects.requireNonNull(groups, "groups must not be null");
        Objects.requireNonNull(gapWeights, "gapWeights must not be null");

        List<ResolvedLayoutGap> gaps = createResolvedGaps(groups, gapWeights);
        SinglePageLayoutPlan plan = layoutEngine.calculate(new SinglePageLayoutInput(
                pageRule,
                groups,
                gaps,
                SinglePageLayoutPolicy.defaultSinglePagePolicy()
        ));

        return layoutRenderer.render(plan);
    }

    private static List<ResolvedLayoutGap> createResolvedGaps(
            List<SinglePageLayoutGroup> groups,
            List<BigDecimal> gapWeights
    ) {
        int expectedGapCount = Math.max(groups.size() - 1, 0);

        if (gapWeights.size() != expectedGapCount) {
            throw new IllegalArgumentException("gapWeights size must be equal to groups size minus one.");
        }

        List<ResolvedLayoutGap> gaps = new ArrayList<>();

        for (int index = 0; index < gapWeights.size(); index++) {
            String fromGroupId = groups.get(index).id();
            String toGroupId = groups.get(index + 1).id();
            LayoutGapRule gapRule = new LayoutGapRule(fromGroupId, toGroupId, gapWeights.get(index));

            gaps.add(new ResolvedLayoutGap(
                    fromGroupId,
                    toGroupId,
                    gapWeights.get(index),
                    List.of(gapRule)
            ));
        }

        return List.copyOf(gaps);
    }
}
