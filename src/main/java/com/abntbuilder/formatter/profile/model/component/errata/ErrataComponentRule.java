package com.abntbuilder.formatter.profile.model.component.errata;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record ErrataComponentRule(
        String componentId,
        String headingStyleId,
        String headingText,
        String tableHeaderStyleId,
        String tableCellStyleId,
        List<String> tableHeaders,
        int blankLinesAfterHeading
) implements ComponentRule {
    public ErrataComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(headingStyleId, "headingStyleId");
        requireNonBlank(headingText, "headingText");
        requireNonBlank(tableHeaderStyleId, "tableHeaderStyleId");
        requireNonBlank(tableCellStyleId, "tableCellStyleId");
        Objects.requireNonNull(tableHeaders, "tableHeaders must not be null");
        if (tableHeaders.isEmpty()) throw new IllegalArgumentException("tableHeaders must not be empty.");
        tableHeaders = List.copyOf(tableHeaders);
        if (blankLinesAfterHeading < 0) throw new IllegalArgumentException("blankLinesAfterHeading must be >= 0.");
    }

    public Map<String, String> contentBindings() { return Map.of(); }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
