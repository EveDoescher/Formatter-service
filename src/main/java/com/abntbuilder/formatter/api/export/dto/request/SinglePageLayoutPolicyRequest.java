package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageAnchorStrategy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutPolicy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLineHeightStrategy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageSafetyPolicyId;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SpacerStylePolicy;

public record SinglePageLayoutPolicyRequest(
        SinglePageAnchorStrategy anchorStrategy,
        SinglePageLineHeightStrategy lineHeightStrategy,
        SpacerStylePolicy spacerStylePolicy,
        SinglePageSafetyPolicyId safetyPolicy
) {

    public SinglePageLayoutPolicy toDomain() {
        if (anchorStrategy == null
                && lineHeightStrategy == null
                && spacerStylePolicy == null
                && safetyPolicy == null) {
            return SinglePageLayoutPolicy.defaultSinglePagePolicy();
        }

        return new SinglePageLayoutPolicy(
                anchorStrategy == null
                        ? SinglePageAnchorStrategy.LAST_GROUP_AT_SAFE_AREA_END
                        : anchorStrategy,
                lineHeightStrategy == null
                        ? SinglePageLineHeightStrategy.MAX_EXACT_LINE_HEIGHT
                        : lineHeightStrategy,
                spacerStylePolicy == null
                        ? SpacerStylePolicy.NEXT_GROUP_STYLE
                        : spacerStylePolicy,
                safetyPolicy == null
                        ? SinglePageSafetyPolicyId.MARGIN_BASED
                        : safetyPolicy
        );
    }
}
