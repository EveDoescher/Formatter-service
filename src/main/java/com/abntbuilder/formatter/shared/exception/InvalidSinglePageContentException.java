package com.abntbuilder.formatter.shared.exception;

public class InvalidSinglePageContentException extends IllegalArgumentException {

    public InvalidSinglePageContentException(String message) {
        super(message);
    }

    public static InvalidSinglePageContentException missingRequiredSlot(String componentId, String slotId) {
        return new InvalidSinglePageContentException(
                "Component '" + componentId + "' is missing required slot: " + slotId + "."
        );
    }

    public static InvalidSinglePageContentException slotTypeMismatch(
            String componentId, String slotId, String expected, String actual) {
        return new InvalidSinglePageContentException(
                "Component '" + componentId + "' slot '" + slotId
                        + "' expected " + expected + " but got " + actual + "."
        );
    }

    public static InvalidSinglePageContentException itemExceedsMaxVisualLines(String slotId, int maxVisualLines) {
        return new InvalidSinglePageContentException(
                "Slot '" + slotId + "' exceeds max visual lines: must fit in "
                        + maxVisualLines + " visual line(s)."
        );
    }

    public static InvalidSinglePageContentException missingTemplateField(
            String componentId, String slotId, String fieldName) {
        return new InvalidSinglePageContentException(
                "Component '" + componentId + "' slot '" + slotId
                        + "' is missing required template field: " + fieldName + "."
        );
    }
}
