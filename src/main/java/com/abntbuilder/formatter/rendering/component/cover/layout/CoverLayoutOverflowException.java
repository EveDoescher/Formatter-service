package com.abntbuilder.formatter.rendering.component.cover.layout;

import com.abntbuilder.formatter.shared.exception.SinglePageLayoutOverflowException;

import java.util.Objects;

public final class CoverLayoutOverflowException extends SinglePageLayoutOverflowException {

    private final CoverLayoutFailureDiagnostic diagnostic;

    public CoverLayoutOverflowException(CoverLayoutFailureDiagnostic diagnostic) {
        super(createMessage(diagnostic));
        this.diagnostic = Objects.requireNonNull(diagnostic, "diagnostic must not be null");
    }

    public CoverLayoutFailureDiagnostic diagnostic() {
        return diagnostic;
    }

    private static String createMessage(CoverLayoutFailureDiagnostic diagnostic) {
        Objects.requireNonNull(diagnostic, "diagnostic must not be null");

        return lineSlotsMessage(
                diagnostic.contentLineCount(),
                diagnostic.renderableArea().safeLineCapacity()
        );
    }
}
