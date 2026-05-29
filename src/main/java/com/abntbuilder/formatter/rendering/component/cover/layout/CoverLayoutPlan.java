package com.abntbuilder.formatter.rendering.component.cover.layout;

import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutElement;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutPlan;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record CoverLayoutPlan(
        SinglePageLayoutPlan layoutPlan
) {

    public CoverLayoutPlan {
        Objects.requireNonNull(layoutPlan, "layoutPlan must not be null");
    }

    public List<SinglePageLayoutElement> elements() {
        return layoutPlan.elements();
    }

    public int totalLines() {
        return layoutPlan.totalLines();
    }

    public int pageCapacityLines() {
        return layoutPlan.pageCapacityLines();
    }

    public BigDecimal exactLineHeightPt() {
        return layoutPlan.exactLineHeightPt();
    }

    public CoverLayoutDiagnostic diagnostic() {
        return CoverLayoutDiagnostic.from(layoutPlan.diagnostic());
    }

}
