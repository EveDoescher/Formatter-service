package com.abntbuilder.formatter.rendering.component.titlepage;

import com.abntbuilder.formatter.document.component.titlepage.TitlePageComponent;
import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageComponentRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageGroupRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageItemRule;
import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;
import com.abntbuilder.formatter.shared.exception.InvalidTitlePageContentException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class TitlePageProfileContentValidator {

    public void validate(TitlePageComponent component, TitlePageComponentRule rule) {
        Objects.requireNonNull(component, "component must not be null");
        Objects.requireNonNull(rule, "rule must not be null");

        if (!TitlePageRenderer.COMPONENT_ID.equals(rule.componentId())) {
            throw new InvalidProfileStructureException("titlePage componentId must be titlePage.");
        }

        Set<String> groupIds = new HashSet<>();

        for (SinglePageGroupRule groupRule : rule.layoutRule().groups()) {
            if (!groupIds.add(groupRule.id())) {
                throw new InvalidProfileStructureException("Duplicate titlePage group id: " + groupRule.id());
            }

            boolean groupHasContent = false;
            Set<String> itemIds = new HashSet<>();

            for (SinglePageItemRule itemRule : groupRule.items()) {
                if (!itemIds.add(itemRule.id())) {
                    throw new InvalidProfileStructureException(
                            "Duplicate titlePage item id in group " + groupRule.id() + ": " + itemRule.id()
                    );
                }

                boolean itemHasContent = hasContent(component, itemRule.id());

                if (itemRule.required() && !itemHasContent) {
                    throw InvalidTitlePageContentException.missingRequiredItem(itemRule.id());
                }

                groupHasContent = groupHasContent || itemHasContent;
                rule.styleMapping().styleIdForItem(itemRule.id());
            }

            if (groupRule.required() && !groupHasContent) {
                throw InvalidTitlePageContentException.missingRequiredGroup(groupRule.id());
            }
        }
    }

    private static boolean hasContent(TitlePageComponent component, String itemId) {
        return switch (itemId) {
            case "authors" -> !component.authors().isEmpty();
            case "title" -> !component.title().isBlank();
            case "subtitle" -> component.subtitle().isPresent();
            case "nature" -> true;
            case "advisor" -> component.advisor().isPresent();
            case "coadvisor" -> component.coadvisor().isPresent();
            case "city" -> !component.city().isBlank();
            case "year" -> !component.year().isBlank();
            default -> throw new InvalidProfileStructureException("Unknown titlePage item id: " + itemId);
        };
    }
}
