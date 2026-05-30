package com.abntbuilder.formatter.profile.model.component.cover;

import com.abntbuilder.formatter.profile.model.layout.singlepage.LayoutGapRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageGroupRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageItemRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutPolicy;
import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

import java.math.BigDecimal;
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

    public CoverLayoutRule(
            BigDecimal topToAuthorWeight,
            BigDecimal authorToTitleWeight,
            BigDecimal titleToBottomWeight
    ) {
        this(
                defaultGroups(),
                List.of(
                        new LayoutGapRule(
                                INSTITUTION_GROUP_ID,
                                AUTHORS_GROUP_ID,
                                requireLegacyPositiveWeight(topToAuthorWeight, "topToAuthorWeight")
                        ),
                        new LayoutGapRule(
                                AUTHORS_GROUP_ID,
                                TITLE_GROUP_ID,
                                requireLegacyPositiveWeight(authorToTitleWeight, "authorToTitleWeight")
                        ),
                        new LayoutGapRule(
                                TITLE_GROUP_ID,
                                BOTTOM_GROUP_ID,
                                requireLegacyPositiveWeight(titleToBottomWeight, "titleToBottomWeight")
                        )
                ),
                SinglePageLayoutPolicy.defaultSinglePagePolicy()
        );
    }

    public BigDecimal topToAuthorWeight() {
        return weightBetween(INSTITUTION_GROUP_ID, AUTHORS_GROUP_ID);
    }

    public BigDecimal authorToTitleWeight() {
        return weightBetween(AUTHORS_GROUP_ID, TITLE_GROUP_ID);
    }

    public BigDecimal titleToBottomWeight() {
        return weightBetween(TITLE_GROUP_ID, BOTTOM_GROUP_ID);
    }

    public BigDecimal totalWeight() {
        return gapRules.stream()
                .map(LayoutGapRule::weight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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

    private BigDecimal weightBetween(String fromGroupId, String toGroupId) {
        return gapRules.stream()
                .filter(gap -> gap.fromGroupId().equals(fromGroupId) && gap.toGroupId().equals(toGroupId))
                .map(LayoutGapRule::weight)
                .findFirst()
                .orElseThrow(() -> new InvalidProfileStructureException(
                        "Missing cover gap rule between groups: " + fromGroupId + " and " + toGroupId + "."
                ));
    }

    private static List<SinglePageGroupRule> defaultGroups() {
        return List.of(
                new SinglePageGroupRule(
                        INSTITUTION_GROUP_ID,
                        true,
                        List.of(new SinglePageItemRule("institutionalLines", true, Optional.empty()))
                ),
                new SinglePageGroupRule(
                        AUTHORS_GROUP_ID,
                        false,
                        List.of(new SinglePageItemRule("authors", false, Optional.empty()))
                ),
                new SinglePageGroupRule(
                        TITLE_GROUP_ID,
                        true,
                        List.of(
                                new SinglePageItemRule("title", true, Optional.empty()),
                                new SinglePageItemRule("subtitle", false, Optional.empty())
                        )
                ),
                new SinglePageGroupRule(
                        BOTTOM_GROUP_ID,
                        true,
                        List.of(
                                new SinglePageItemRule("city", true, Optional.of(1)),
                                new SinglePageItemRule("year", true, Optional.of(1))
                        )
                )
        );
    }

    private static BigDecimal requireLegacyPositiveWeight(BigDecimal value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");

        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidProfileStructureException(fieldName + " must be greater than zero.");
        }

        return value;
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
