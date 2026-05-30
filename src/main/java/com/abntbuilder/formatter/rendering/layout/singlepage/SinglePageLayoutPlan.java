package com.abntbuilder.formatter.rendering.layout.singlepage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record SinglePageLayoutPlan(
        List<SinglePageLayoutElement> elements,
        int totalLines,
        int pageCapacityLines,
        BigDecimal exactLineHeightPt,
        SinglePageLayoutDiagnostic diagnostic
) {

    public SinglePageLayoutPlan {
        Objects.requireNonNull(elements, "elements must not be null");
        Objects.requireNonNull(exactLineHeightPt, "exactLineHeightPt must not be null");
        Objects.requireNonNull(diagnostic, "diagnostic must not be null");

        if (elements.isEmpty()) {
            throw new IllegalArgumentException("elements must not be empty.");
        }

        if (totalLines <= 0) {
            throw new IllegalArgumentException("totalLines must be greater than zero.");
        }

        if (pageCapacityLines < totalLines) {
            throw new IllegalArgumentException("pageCapacityLines must be greater than or equal to totalLines.");
        }

        if (exactLineHeightPt.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("exactLineHeightPt must be greater than zero.");
        }

        elements = List.copyOf(elements);

        int elementLineCount = elements.stream()
                .mapToInt(SinglePageLayoutElement::lineCount)
                .sum();

        if (elementLineCount != totalLines) {
            throw new IllegalArgumentException("totalLines must match the sum of element line counts.");
        }

        if (diagnostic.renderableArea().safeLineCapacity() != pageCapacityLines) {
            throw new IllegalArgumentException("diagnostic safeLineCapacity must match pageCapacityLines.");
        }

        if (diagnostic.contentLineCount() + diagnostic.availableGapLines() != totalLines) {
            throw new IllegalArgumentException(
                    "diagnostic contentLineCount plus availableGapLines must match totalLines."
            );
        }
    }
}
