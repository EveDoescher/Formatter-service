package com.abntbuilder.formatter.rendering.component.cover.layout;

import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageGapDistributor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public final class CoverGapDistributor {

    private final SinglePageGapDistributor delegate;

    public CoverGapDistributor() {
        this(new SinglePageGapDistributor());
    }

    public CoverGapDistributor(SinglePageGapDistributor delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
    }

    public int[] distribute(int availableGapLines, List<BigDecimal> gapWeights) {
        return delegate.distribute(availableGapLines, gapWeights);
    }
}
