package com.abntbuilder.formatter.shared.exception;

public class InvalidCoverContentException extends IllegalArgumentException {

    public InvalidCoverContentException(String message) {
        super(message);
    }

    public static InvalidCoverContentException missingBottomLines() {
        return new InvalidCoverContentException(
                "cover must contain city and year for anchored bottom layout."
        );
    }

    public static InvalidCoverContentException invalidBottomLines() {
        return new InvalidCoverContentException(
                "cover city and year must each fit in exactly one visual line."
        );
    }

    public static InvalidCoverContentException missingRequiredGroup(String groupId) {
        return new InvalidCoverContentException(
                "cover required group has no content: " + groupId + "."
        );
    }

    public static InvalidCoverContentException missingRequiredItem(String itemId) {
        return new InvalidCoverContentException(
                "cover required item has no content: " + itemId + "."
        );
    }

    public static InvalidCoverContentException itemExceedsMaxVisualLines(String itemId, int maxVisualLines) {
        if ("city".equals(itemId) || "year".equals(itemId)) {
            return invalidBottomLines();
        }

        return new InvalidCoverContentException(
                "cover item exceeds max visual lines: " + itemId + " must fit in "
                        + maxVisualLines
                        + " visual line(s)."
        );
    }
}
