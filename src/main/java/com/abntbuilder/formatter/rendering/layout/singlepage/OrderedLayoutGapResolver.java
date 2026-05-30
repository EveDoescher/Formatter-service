package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.profile.model.layout.singlepage.LayoutGapRule;
import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class OrderedLayoutGapResolver {

    public List<ResolvedLayoutGap> resolve(
            List<String> declaredGroupOrder,
            List<String> presentGroupOrder,
            List<LayoutGapRule> declaredGapRules
    ) {
        Objects.requireNonNull(declaredGroupOrder, "declaredGroupOrder must not be null");
        Objects.requireNonNull(presentGroupOrder, "presentGroupOrder must not be null");
        Objects.requireNonNull(declaredGapRules, "declaredGapRules must not be null");

        if (declaredGroupOrder.isEmpty()) {
            throw new InvalidProfileStructureException("declaredGroupOrder must not be empty.");
        }

        if (presentGroupOrder.isEmpty()) {
            throw new InvalidProfileStructureException("presentGroupOrder must not be empty.");
        }

        declaredGroupOrder = List.copyOf(declaredGroupOrder);
        presentGroupOrder = List.copyOf(presentGroupOrder);
        declaredGapRules = List.copyOf(declaredGapRules);

        Map<String, Integer> declaredIndexes = validateDeclaredGroupOrder(declaredGroupOrder);
        validatePresentGroupOrder(presentGroupOrder, declaredIndexes);
        Map<String, LayoutGapRule> gapRulesById = validateGapRules(
                declaredGroupOrder,
                declaredIndexes,
                declaredGapRules
        );

        List<ResolvedLayoutGap> resolvedGaps = new ArrayList<>();

        for (int presentIndex = 0; presentIndex < presentGroupOrder.size() - 1; presentIndex++) {
            String fromPresentGroupId = presentGroupOrder.get(presentIndex);
            String toPresentGroupId = presentGroupOrder.get(presentIndex + 1);
            int fromDeclaredIndex = declaredIndexes.get(fromPresentGroupId);
            int toDeclaredIndex = declaredIndexes.get(toPresentGroupId);
            List<LayoutGapRule> sourceGapRules = new ArrayList<>();
            BigDecimal weight = BigDecimal.ZERO;

            for (int declaredIndex = fromDeclaredIndex; declaredIndex < toDeclaredIndex; declaredIndex++) {
                String fromGroupId = declaredGroupOrder.get(declaredIndex);
                String toGroupId = declaredGroupOrder.get(declaredIndex + 1);
                LayoutGapRule gapRule = gapRulesById.get(gapId(fromGroupId, toGroupId));

                if (gapRule == null) {
                    throw new InvalidProfileStructureException(
                            "Missing declared gap rule between groups: " + fromGroupId + " and " + toGroupId + "."
                    );
                }

                sourceGapRules.add(gapRule);
                weight = weight.add(gapRule.weight());
            }

            resolvedGaps.add(new ResolvedLayoutGap(
                    fromPresentGroupId,
                    toPresentGroupId,
                    weight,
                    sourceGapRules
            ));
        }

        return List.copyOf(resolvedGaps);
    }

    private static Map<String, Integer> validateDeclaredGroupOrder(List<String> declaredGroupOrder) {
        Map<String, Integer> indexes = new HashMap<>();

        for (int index = 0; index < declaredGroupOrder.size(); index++) {
            String groupId = declaredGroupOrder.get(index);
            requireNonBlank(groupId, "declaredGroupOrder item");

            if (indexes.put(groupId, index) != null) {
                throw new InvalidProfileStructureException("Duplicate declared group id: " + groupId);
            }
        }

        return indexes;
    }

    private static void validatePresentGroupOrder(
            List<String> presentGroupOrder,
            Map<String, Integer> declaredIndexes
    ) {
        Set<String> presentGroupIds = new HashSet<>();
        int previousDeclaredIndex = -1;

        for (String presentGroupId : presentGroupOrder) {
            requireNonBlank(presentGroupId, "presentGroupOrder item");

            if (!presentGroupIds.add(presentGroupId)) {
                throw new InvalidProfileStructureException("Duplicate present group id: " + presentGroupId);
            }

            Integer declaredIndex = declaredIndexes.get(presentGroupId);

            if (declaredIndex == null) {
                throw new InvalidProfileStructureException("Present group is not declared: " + presentGroupId);
            }

            if (declaredIndex <= previousDeclaredIndex) {
                throw new InvalidProfileStructureException("presentGroupOrder must follow declaredGroupOrder.");
            }

            previousDeclaredIndex = declaredIndex;
        }
    }

    private static Map<String, LayoutGapRule> validateGapRules(
            List<String> declaredGroupOrder,
            Map<String, Integer> declaredIndexes,
            List<LayoutGapRule> declaredGapRules
    ) {
        Set<String> declaredGroupIds = new HashSet<>(declaredGroupOrder);
        Map<String, LayoutGapRule> gapRulesById = new HashMap<>();

        for (LayoutGapRule gapRule : declaredGapRules) {
            Objects.requireNonNull(gapRule, "declaredGapRules must not contain null values.");

            if (!declaredGroupIds.contains(gapRule.fromGroupId())) {
                throw new InvalidProfileStructureException(
                        "Gap rule has unknown fromGroupId: " + gapRule.fromGroupId()
                );
            }

            if (!declaredGroupIds.contains(gapRule.toGroupId())) {
                throw new InvalidProfileStructureException(
                        "Gap rule has unknown toGroupId: " + gapRule.toGroupId()
                );
            }

            int fromIndex = declaredIndexes.get(gapRule.fromGroupId());
            int toIndex = declaredIndexes.get(gapRule.toGroupId());

            if (toIndex - fromIndex != 1) {
                throw new InvalidProfileStructureException(
                        "Gap rule must connect adjacent declared groups: " + gapRule.id()
                );
            }

            if (gapRulesById.put(gapRule.id(), gapRule) != null) {
                throw new InvalidProfileStructureException("Duplicate gap rule: " + gapRule.id());
            }
        }

        return gapRulesById;
    }

    private static String gapId(String fromGroupId, String toGroupId) {
        return fromGroupId + "->" + toGroupId;
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidProfileStructureException(fieldName + " must not be blank.");
        }
    }
}
