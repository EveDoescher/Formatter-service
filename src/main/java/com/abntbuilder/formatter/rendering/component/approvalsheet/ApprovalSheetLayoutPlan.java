package com.abntbuilder.formatter.rendering.component.approvalsheet;

import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutPlan;

import java.util.Objects;

public record ApprovalSheetLayoutPlan(
        SinglePageLayoutPlan layoutPlan
) {

    public ApprovalSheetLayoutPlan {
        Objects.requireNonNull(layoutPlan, "layoutPlan must not be null");
    }
}
