package com.abntbuilder.formatter.rendering.component.cover.layout;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record CoverLayoutPlan(
        List<CoverLayoutElement> elements,
        int totalLines,
        int pageCapacityLines,
        BigDecimal exactLineHeightPt
) {

    public CoverLayoutPlan {
        Objects.requireNonNull(elements, "elements must not be null");
        Objects.requireNonNull(exactLineHeightPt, "exactLineHeightPt must not be null");

        if (elements.isEmpty()) {
            throw new IllegalArgumentException("elements must not be empty.");
        }

        if (totalLines < 0) {
            throw new IllegalArgumentException("totalLines must not be negative.");
        }

        if (pageCapacityLines <= 0) {
            throw new IllegalArgumentException("pageCapacityLines must be greater than zero.");
        }

        if (exactLineHeightPt.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("exactLineHeightPt must be greater than zero.");
        }

        elements = List.copyOf(elements);

        int elementLineCount = 0;

        for (CoverLayoutElement element : elements) {
            Objects.requireNonNull(element, "elements must not contain null values.");
            elementLineCount += element.lineCount();
        }

        if (elementLineCount != totalLines) {
            throw new IllegalArgumentException("totalLines must match the sum of element line counts.");
        }

        if (totalLines > pageCapacityLines) {
            throw new IllegalArgumentException("totalLines must not exceed pageCapacityLines.");
        }
    }
}
