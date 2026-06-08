package com.abntbuilder.formatter.shared.exception;

public class MissingComponentRendererException extends RuntimeException {

    public MissingComponentRendererException(String componentId) {
        super("Missing component renderer for id: " + componentId);
    }
}
