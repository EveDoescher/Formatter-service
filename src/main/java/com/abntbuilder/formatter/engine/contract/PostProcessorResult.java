package com.abntbuilder.formatter.engine.contract;

import java.util.List;
import java.util.Objects;

public record PostProcessorResult(byte[] docxBytes, List<String> warnings) {
    public PostProcessorResult {
        Objects.requireNonNull(docxBytes, "docxBytes must not be null");
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    public static PostProcessorResult of(byte[] docxBytes) {
        return new PostProcessorResult(docxBytes, List.of());
    }

    public static PostProcessorResult of(byte[] docxBytes, List<String> warnings) {
        return new PostProcessorResult(docxBytes, warnings);
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
}
