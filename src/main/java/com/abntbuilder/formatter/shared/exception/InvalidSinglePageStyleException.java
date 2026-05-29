package com.abntbuilder.formatter.shared.exception;

public class InvalidSinglePageStyleException extends IllegalArgumentException {

    public InvalidSinglePageStyleException(String message) {
        super(message);
    }

    public static InvalidSinglePageStyleException spacingBeforeMustBeZero() {
        return new InvalidSinglePageStyleException(
                "single-page layout styles must have spacingBeforePt equal to zero."
        );
    }

    public static InvalidSinglePageStyleException spacingAfterMustBeZero() {
        return new InvalidSinglePageStyleException(
                "single-page layout styles must have spacingAfterPt equal to zero."
        );
    }
}
