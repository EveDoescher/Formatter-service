package com.abntbuilder.formatter.rendering.component.titlepage;

import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutPlan;

import java.util.Objects;

public record TitlePageLayoutPlan(
        SinglePageLayoutPlan layoutPlan
) {

    public TitlePageLayoutPlan {
        Objects.requireNonNull(layoutPlan, "layoutPlan must not be null");
    }
}
