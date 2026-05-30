package com.abntbuilder.formatter.profile.model.component.cover;

import com.abntbuilder.formatter.profile.model.layout.singlepage.LayoutGapRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageGroupRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutPolicy;
import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record CoverLayoutRule(
        List<SinglePageGroupRule> groups,
        List<LayoutGapRule> gapRules,
        SinglePageLayoutPolicy policy
) {

    public static final String INSTITUTION_GROUP_ID = "cover.institution";
    public static final String AUTHORS_GROUP_ID = "cover.authors";
    public static final String TITLE_GROUP_ID = "cover.titleBlock";
    public static final String BOTTOM_GROUP_ID = "cover.bottom";

    public CoverLayoutRule {
        Objects.requireNonNull(groups, "groups must not be null");
        Objects.requireNonNull(gapRules, "gapRules must not be null");
        Objects.requireNonNull(policy, "policy must not be null");

        if (groups.isEmpty()) {
            throw new InvalidProfileStructureException("groups must not be empty.");
        }

        groups = List.copyOf(groups);
        gapRules = List.copyOf(gapRules);

        validateGroups(groups);
        validateGapRules(groups, gapRules);
    }

    public List<String> declaredGroupOrder() {
        return groups.stream()
                .map(SinglePageGroupRule::id)
                .toList();
    }

    public Optional<SinglePageGroupRule> groupById(String groupId) {
        return groups.stream()
                .filter(group -> group.id().equals(groupId))
                .findFirst();
    }

    private static void validateGroups(List<SinglePageGroupRule> groups) {
        Set<String> groupIds = new HashSet<>();

        for (SinglePageGroupRule group : groups) {
            Objects.requireNonNull(group, "groups must not contain null values.");

            if (!groupIds.add(group.id())) {
                throw new InvalidProfileStructureException("Duplicate single-page group id: " + group.id());
            }
        }
    }

    private static void validateGapRules(
            List<SinglePageGroupRule> groups,
            List<LayoutGapRule> gapRules
    ) {
        Set<String> groupIds = groups.stream()
                .map(SinglePageGroupRule::id)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        Set<String> gapIds = new HashSet<>();

        if (groups.size() > 1 && gapRules.isEmpty()) {
            throw new InvalidProfileStructureException(
                    "gapRules must not be empty when more than one group is declared."
            );
        }

        for (LayoutGapRule gapRule : gapRules) {
            Objects.requireNonNull(gapRule, "gapRules must not contain null values.");

            if (!groupIds.contains(gapRule.fromGroupId())) {
                throw new InvalidProfileStructureException("Unknown gap fromGroupId: " + gapRule.fromGroupId());
            }

            if (!groupIds.contains(gapRule.toGroupId())) {
                throw new InvalidProfileStructureException("Unknown gap toGroupId: " + gapRule.toGroupId());
            }

            if (!gapIds.add(gapRule.id())) {
                throw new InvalidProfileStructureException("Duplicate gap rule: " + gapRule.id());
            }
        }
    }
}
