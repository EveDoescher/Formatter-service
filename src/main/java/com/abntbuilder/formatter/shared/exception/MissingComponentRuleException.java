package com.abntbuilder.formatter.shared.exception;

public class MissingComponentRuleException extends RuntimeException {

    public MissingComponentRuleException(String componentId) {
        super("Missing component rule for id: " + componentId);
    }
}