package com.abntbuilder.formatter.shared.exception;

import java.math.BigDecimal;

public class SinglePageLayoutOverflowException extends RuntimeException {

    public SinglePageLayoutOverflowException(BigDecimal requiredHeightCm, BigDecimal usableHeightCm) {
        super("Single-page layout overflow. Required height: "
                + format(requiredHeightCm)
                + " cm; usable height: "
                + format(usableHeightCm)
                + " cm.");
    }

    private SinglePageLayoutOverflowException(String message) {
        super(message);
    }

    public static SinglePageLayoutOverflowException forLineSlots(
            int requiredLineSlots,
            int usableLineSlots
    ) {
        return new SinglePageLayoutOverflowException(
                "Single-page layout overflow. Required line slots: "
                        + requiredLineSlots
                        + "; usable line slots: "
                        + usableLineSlots
                        + "."
        );
    }

    private static String format(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }
}