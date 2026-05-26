package com.abntbuilder.formatter.profile.resolution;

import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.shared.exception.MissingStyleRuleException;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class StyleResolver {

    private final Map<String, StyleRule> stylesById;

    public StyleResolver(DocumentProfile profile) {
        Objects.requireNonNull(profile, "profile must not be null");

        this.stylesById = profile.styleRules()
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                        StyleRule::id,
                        Function.identity()
                ));
    }

    public StyleRule resolve(String styleId) {
        if (styleId == null || styleId.isBlank()) {
            throw new IllegalArgumentException("styleId must not be blank.");
        }

        StyleRule styleRule = stylesById.get(styleId);

        if (styleRule == null) {
            throw new MissingStyleRuleException(styleId);
        }

        return styleRule;
    }
}