package com.abntbuilder.formatter.input.profile;

import com.abntbuilder.formatter.engine.model.FontPreferences;
import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;
import com.abntbuilder.formatter.engine.model.profile.FontRoleRule;
import com.abntbuilder.formatter.engine.model.profile.StyleRule;
import com.abntbuilder.formatter.shared.exception.InvalidFontChoiceException;
import com.abntbuilder.formatter.shared.exception.MissingStyleRuleException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class StyleResolver {

    private final Map<String, StyleRule> stylesById;

    public StyleResolver(DocumentProfile profile, FontPreferences fontPreferences) {
        Objects.requireNonNull(profile, "profile must not be null");
        Objects.requireNonNull(fontPreferences, "fontPreferences must not be null");

        Map<String, String> styleIdToFont = resolveStyleIdToFont(profile, fontPreferences);

        Map<String, StyleRule> resolved = new HashMap<>();
        for (StyleRule styleRule : profile.styleRules()) {
            String overrideFont = styleIdToFont.get(styleRule.id());
            resolved.put(styleRule.id(), overrideFont != null
                    ? withFont(styleRule, overrideFont)
                    : styleRule);
        }
        this.stylesById = Map.copyOf(resolved);
    }

    public StyleResolver(DocumentProfile profile) {
        this(profile, FontPreferences.NONE);
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

    private static Map<String, String> resolveStyleIdToFont(
            DocumentProfile profile,
            FontPreferences fontPreferences
    ) {
        if (profile.fontRoles().isEmpty()) {
            return Map.of();
        }

        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, FontRoleRule> entry : profile.fontRoles().entrySet()) {
            String roleName = entry.getKey();
            FontRoleRule role = entry.getValue();

            String chosen = fontPreferences.choiceFor(roleName).orElse(role.defaultFont());

            if (role.allowsChoice() && !role.allowedValues().contains(chosen)) {
                throw new InvalidFontChoiceException(roleName, chosen, role.allowedValues());
            }

            for (String styleId : role.styleIds()) {
                result.put(styleId, chosen);
            }
        }
        return result;
    }

    private static StyleRule withFont(StyleRule original, String fontFamily) {
        return new StyleRule(
                original.id(),
                original.type(),
                fontFamily,
                original.fontSizePt(),
                original.alignment(),
                original.lineSpacing(),
                original.firstLineIndentCm(),
                original.leftIndentCm(),
                original.rightIndentCm(),
                original.spacingBeforePt(),
                original.spacingAfterPt(),
                original.bold(),
                original.italic(),
                original.uppercase()
        );
    }
}