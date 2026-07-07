package com.abntbuilder.formatter.engine.model.profile.component.singlepage;

import java.util.List;
import java.util.Objects;

public record ComposedTextSlotRule(
        boolean required,
        String template,
        List<String> fieldNames
) implements SlotRule {

    public ComposedTextSlotRule {
        if (template == null || template.isBlank()) {
            throw new IllegalArgumentException("ComposedTextSlotRule.template must not be blank.");
        }
        Objects.requireNonNull(fieldNames, "ComposedTextSlotRule.fieldNames must not be null.");
        if (fieldNames.isEmpty()) {
            throw new IllegalArgumentException("ComposedTextSlotRule.fieldNames must not be empty.");
        }
        fieldNames = List.copyOf(fieldNames);
    }
}
