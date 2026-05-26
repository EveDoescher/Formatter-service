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

    private static String format(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }
}