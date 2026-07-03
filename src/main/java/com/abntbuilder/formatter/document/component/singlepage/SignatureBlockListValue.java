package com.abntbuilder.formatter.document.component.singlepage;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record SignatureBlockListValue(List<Map<String, String>> entries) implements ContentValue {

    public SignatureBlockListValue {
        Objects.requireNonNull(entries, "SignatureBlockListValue.entries must not be null.");
        if (entries.isEmpty()) {
            throw new IllegalArgumentException("SignatureBlockListValue.entries must not be empty.");
        }
        for (Map<String, String> entry : entries) {
            Objects.requireNonNull(entry, "SignatureBlockListValue.entries must not contain null maps.");
            if (entry.isEmpty()) {
                throw new IllegalArgumentException("SignatureBlockListValue.entries must not contain empty maps.");
            }
            for (Map.Entry<String, String> field : entry.entrySet()) {
                if (field.getKey() == null || field.getKey().isBlank()) {
                    throw new IllegalArgumentException("SignatureBlockListValue.entries must not contain blank field keys.");
                }
                if (field.getValue() == null || field.getValue().isBlank()) {
                    throw new IllegalArgumentException("SignatureBlockListValue.entries must not contain blank field values.");
                }
            }
        }
        entries = entries.stream().map(Map::copyOf).toList();
    }
}
