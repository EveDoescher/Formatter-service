package com.abntbuilder.formatter.engine.model;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record FontPreferences(Map<String, String> fonts) {

    public static final FontPreferences NONE = new FontPreferences(Map.of());

    public FontPreferences {
        Objects.requireNonNull(fonts, "fonts must not be null");
        fonts = Map.copyOf(fonts);
    }

    public Optional<String> choiceFor(String roleName) {
        return Optional.ofNullable(fonts.get(roleName));
    }
}
