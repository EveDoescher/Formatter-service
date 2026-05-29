package com.abntbuilder.formatter.shared.exception;

public class TextMeasurementException extends IllegalArgumentException {

    public TextMeasurementException(String message) {
        super(message);
    }

    public static TextMeasurementException blankText() {
        return new TextMeasurementException("text must not be blank.");
    }

    public static TextMeasurementException wordExceedsAvailableWidth() {
        return new TextMeasurementException("word width exceeds available text width.");
    }

    public static TextMeasurementException unavailableTextWidth() {
        return new TextMeasurementException("available text width must be greater than zero.");
    }
}
