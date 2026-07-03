package com.abntbuilder.formatter.document.component.singlepage;

import java.util.Map;
import java.util.Objects;

public record ComposedTextValue(Map<String, String> fields) implements ContentValue {

    public ComposedTextValue {
        Objects.requireNonNull(fields, "ComposedTextValue.fields must not be null.");
        if (fields.isEmpty()) {
            throw new IllegalArgumentException("ComposedTextValue.fields must not be empty.");
        }
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()) {
                throw new IllegalArgumentException("ComposedTextValue.fields must not contain blank keys.");
            }
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                throw new IllegalArgumentException("ComposedTextValue.fields must not contain blank values.");
            }
        }
        fields = Map.copyOf(fields);
    }
}
