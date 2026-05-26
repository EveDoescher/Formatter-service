package com.abntbuilder.formatter.profile.resolution;

import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.PageOrientation;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.StyleType;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.shared.exception.MissingStyleRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StyleResolverTest {

    @Test
    void shouldResolveExistingStyleById() {
        DocumentProfile profile = new DocumentProfile(
                "test-profile",
                "Test Profile",
                validPageRule(),
                List.of(
                        validStyleRule("body"),
                        validStyleRule("cover.title")
                ),
                List.of()
        );

        StyleResolver resolver = new StyleResolver(profile);

        StyleRule resolved = resolver.resolve("cover.title");

        assertEquals("cover.title", resolved.id());
    }

    @Test
    void shouldRejectNullProfile() {
        assertThrows(NullPointerException.class, () -> new StyleResolver(null));
    }

    @Test
    void shouldRejectBlankStyleId() {
        StyleResolver resolver = new StyleResolver(validProfile());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> resolver.resolve(" "));

        assertEquals("styleId must not be blank.", exception.getMessage());
    }

    @Test
    void shouldThrowClearExceptionWhenStyleDoesNotExist() {
        StyleResolver resolver = new StyleResolver(validProfile());

        MissingStyleRuleException exception = assertThrows(MissingStyleRuleException.class, () -> resolver.resolve("missing.style"));

        assertEquals("Missing style rule for id: missing.style", exception.getMessage());
    }

    private static DocumentProfile validProfile() {
        return new DocumentProfile(
                "test-profile",
                "Test Profile",
                validPageRule(),
                List.of(validStyleRule("body")),
                List.of()
        );
    }

    private static PageRule validPageRule() {
        return new PageRule(
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                PageOrientation.PORTRAIT
        );
    }

    private static StyleRule validStyleRule(String id) {
        return new StyleRule(
                id,
                StyleType.PARAGRAPH,
                "Test Font",
                BigDecimal.valueOf(12),
                TextAlignment.JUSTIFIED,
                BigDecimal.valueOf(1.5),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                false,
                false
        );
    }
}