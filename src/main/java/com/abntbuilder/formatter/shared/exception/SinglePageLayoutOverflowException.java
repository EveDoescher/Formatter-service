package com.abntbuilder.formatter.shared.exception;

import com.abntbuilder.formatter.rendering.singlepage.SinglePageLayoutFailureDiagnostic;

import java.math.BigDecimal;
import java.util.Optional;

public class SinglePageLayoutOverflowException extends RuntimeException {

    private final SinglePageLayoutFailureDiagnostic diagnostic;

    public SinglePageLayoutOverflowException(BigDecimal requiredHeightCm, BigDecimal usableHeightCm) {
        super("Single-page layout overflow. Required height: "
                + format(requiredHeightCm)
                + " cm; usable height: "
                + format(usableHeightCm)
                + " cm.");
        this.diagnostic = null;
    }

    protected SinglePageLayoutOverflowException(String message) {
        super(message);
        this.diagnostic = null;
    }

    public SinglePageLayoutOverflowException(SinglePageLayoutFailureDiagnostic diagnostic) {
        super(lineSlotsMessage(
                diagnostic.contentLineCount(),
                diagnostic.renderableArea().safeLineCapacity()
        ));
        this.diagnostic = diagnostic;
    }

    public static SinglePageLayoutOverflowException forLineSlots(
            int requiredLineSlots,
            int usableLineSlots
    ) {
        return new SinglePageLayoutOverflowException(lineSlotsMessage(requiredLineSlots, usableLineSlots));
    }

    protected static String lineSlotsMessage(
            int requiredLineSlots,
            int usableLineSlots
    ) {
        return "Single-page layout overflow. Required line slots: "
                + requiredLineSlots
                + "; usable line slots: "
                + usableLineSlots
                + ".";
    }

    public Optional<SinglePageLayoutFailureDiagnostic> singlePageDiagnostic() {
        return Optional.ofNullable(diagnostic);
    }

    private static String format(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }
}
