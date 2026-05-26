package com.abntbuilder.formatter.shared.exception;

public class MissingStyleRuleException extends RuntimeException {

    public MissingStyleRuleException(String styleId) {
        super("Missing style rule for id: " + styleId);
    }
}