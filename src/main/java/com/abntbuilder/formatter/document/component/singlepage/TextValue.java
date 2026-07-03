package com.abntbuilder.formatter.document.component.singlepage;

public record TextValue(String text) implements ContentValue {

    public TextValue {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("TextValue.text must not be blank.");
        }
    }
}
