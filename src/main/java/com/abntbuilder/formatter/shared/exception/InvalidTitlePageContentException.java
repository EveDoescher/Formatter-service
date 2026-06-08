package com.abntbuilder.formatter.shared.exception;

public class InvalidTitlePageContentException extends IllegalArgumentException {

    public InvalidTitlePageContentException(String message) {
        super(message);
    }

    public static InvalidTitlePageContentException missingRequiredGroup(String groupId) {
        return new InvalidTitlePageContentException(
                "titlePage required group has no content: " + groupId + "."
        );
    }

    public static InvalidTitlePageContentException missingRequiredItem(String itemId) {
        return new InvalidTitlePageContentException(
                "titlePage required item has no content: " + itemId + "."
        );
    }

    public static InvalidTitlePageContentException itemExceedsMaxVisualLines(String itemId, int maxVisualLines) {
        return new InvalidTitlePageContentException(
                "titlePage item exceeds max visual lines: " + itemId + " must fit in "
                        + maxVisualLines
                        + " visual line(s)."
        );
    }
}
