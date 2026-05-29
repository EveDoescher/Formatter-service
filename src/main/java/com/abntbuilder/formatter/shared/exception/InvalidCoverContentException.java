package com.abntbuilder.formatter.shared.exception;

public class InvalidCoverContentException extends IllegalArgumentException {

    public InvalidCoverContentException(String message) {
        super(message);
    }

    public static InvalidCoverContentException missingBottomLines() {
        return new InvalidCoverContentException(
                "cover must contain bottomLines for anchored bottom layout."
        );
    }

    public static InvalidCoverContentException invalidBottomLines() {
        return new InvalidCoverContentException(
                "cover bottomLines must contain exactly city and year."
        );
    }
}
