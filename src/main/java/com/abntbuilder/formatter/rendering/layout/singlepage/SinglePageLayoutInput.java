package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutPolicy;

import java.util.List;
import java.util.Objects;

public record SinglePageLayoutInput(
        PageRule pageRule,
        List<SinglePageLayoutGroup> groups,
        List<ResolvedLayoutGap> gaps,
        SinglePageLayoutPolicy policy
) {

    public SinglePageLayoutInput {
        Objects.requireNonNull(pageRule, "pageRule must not be null");
        Objects.requireNonNull(groups, "groups must not be null");
        Objects.requireNonNull(gaps, "gaps must not be null");
        Objects.requireNonNull(policy, "policy must not be null");

        if (groups.isEmpty()) {
            throw new IllegalArgumentException("groups must not be empty.");
        }

        groups = List.copyOf(groups);
        gaps = List.copyOf(gaps);
    }
}
