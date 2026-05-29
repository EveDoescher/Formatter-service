package com.abntbuilder.formatter.rendering.component.cover.layout;

import com.abntbuilder.formatter.document.component.cover.CoverComponent;
import com.abntbuilder.formatter.profile.model.component.cover.CoverComponentRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageGroupRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageItemRule;
import com.abntbuilder.formatter.shared.exception.InvalidCoverContentException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class CoverProfileContentValidator {

    public void validate(CoverComponent cover, CoverComponentRule rule) {
        Objects.requireNonNull(cover, "cover must not be null");
        Objects.requireNonNull(rule, "rule must not be null");

        Set<String> groupIds = new HashSet<>();

        for (SinglePageGroupRule groupRule : rule.layoutRule().groups()) {
            if (!groupIds.add(groupRule.id())) {
                throw new IllegalArgumentException("Duplicate cover group id: " + groupRule.id());
            }

            boolean groupHasContent = false;
            Set<String> itemIds = new HashSet<>();

            for (SinglePageItemRule itemRule : groupRule.items()) {
                if (!itemIds.add(itemRule.id())) {
                    throw new IllegalArgumentException(
                            "Duplicate cover item id in group " + groupRule.id() + ": " + itemRule.id()
                    );
                }

                boolean itemHasContent = hasContent(cover, itemRule.id());

                if (itemRule.required() && !itemHasContent) {
                    throw InvalidCoverContentException.missingRequiredItem(itemRule.id());
                }

                groupHasContent = groupHasContent || itemHasContent;
                rule.styleMapping().styleIdForItem(itemRule.id());
            }

            if (groupRule.required() && !groupHasContent) {
                throw InvalidCoverContentException.missingRequiredGroup(groupRule.id());
            }
        }
    }

    private static boolean hasContent(CoverComponent cover, String itemId) {
        return switch (itemId) {
            case "institutionalLines" -> !cover.institutionalLines().isEmpty();
            case "authors" -> !cover.authors().isEmpty();
            case "title" -> !cover.title().isBlank();
            case "subtitle" -> cover.subtitle().isPresent();
            case "city" -> !cover.city().isBlank();
            case "year" -> !cover.year().isBlank();
            default -> throw new IllegalArgumentException("Unknown cover item id: " + itemId);
        };
    }
}
