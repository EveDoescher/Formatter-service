package com.abntbuilder.formatter.profile.model;

import com.abntbuilder.formatter.profile.model.component.cover.CoverComponentRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverLayoutRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverStyleMapping;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocumentProfileTest {

    @Test
    void shouldCreateValidDocumentProfile() {
        DocumentProfile profile = new DocumentProfile(
                "test-profile",
                "Test Profile",
                validPageRule(),
                List.of(validStyleRule("body")),
                List.of()
        );

        assertEquals("test-profile", profile.id());
        assertEquals("Test Profile", profile.displayName());
        assertEquals(1, profile.styleRules().size());
        assertTrue(profile.componentRules().isEmpty());
    }

    @Test
    void shouldCreateValidDocumentProfileWithComponentRules() {
        DocumentProfile profile = new DocumentProfile(
                "test-profile",
                "Test Profile",
                validPageRule(),
                List.of(validStyleRule("body")),
                List.of(validCoverComponentRule("cover"))
        );

        assertEquals(1, profile.componentRules().size());
        assertEquals("cover", profile.componentRules().getFirst().componentId());
    }

    @Test
    void shouldRejectBlankId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new DocumentProfile(
                " ",
                "Test Profile",
                validPageRule(),
                List.of(validStyleRule("body")),
                List.of()
        ));

        assertEquals("id must not be blank.", exception.getMessage());
    }

    @Test
    void shouldRejectBlankDisplayName() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new DocumentProfile(
                "test-profile",
                "",
                validPageRule(),
                List.of(validStyleRule("body")),
                List.of()
        ));

        assertEquals("displayName must not be blank.", exception.getMessage());
    }

    @Test
    void shouldRejectNullPageRule() {
        assertThrows(NullPointerException.class, () -> new DocumentProfile(
                "test-profile",
                "Test Profile",
                null,
                List.of(validStyleRule("body")),
                List.of()
        ));
    }

    @Test
    void shouldRejectNullStyleRules() {
        assertThrows(NullPointerException.class, () -> new DocumentProfile(
                "test-profile",
                "Test Profile",
                validPageRule(),
                null,
                List.of()
        ));
    }

    @Test
    void shouldRejectNullComponentRules() {
        assertThrows(NullPointerException.class, () -> new DocumentProfile(
                "test-profile",
                "Test Profile",
                validPageRule(),
                List.of(validStyleRule("body")),
                null
        ));
    }

    @Test
    void shouldRejectEmptyStyleRules() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new DocumentProfile(
                "test-profile",
                "Test Profile",
                validPageRule(),
                List.of(),
                List.of()
        ));

        assertEquals("styleRules must not be empty.", exception.getMessage());
    }

    @Test
    void shouldRejectDuplicateStyleRuleIds() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new DocumentProfile(
                "test-profile",
                "Test Profile",
                validPageRule(),
                List.of(
                        validStyleRule("body"),
                        validStyleRule("body")
                ),
                List.of()
        ));

        assertEquals("Duplicate style rule id: body", exception.getMessage());
    }

    @Test
    void shouldRejectDuplicateComponentRuleIds() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new DocumentProfile(
                "test-profile",
                "Test Profile",
                validPageRule(),
                List.of(validStyleRule("body")),
                List.of(
                        validCoverComponentRule("cover"),
                        validCoverComponentRule("cover")
                )
        ));

        assertEquals("Duplicate component rule id: cover", exception.getMessage());
    }

    @Test
    void shouldMakeStyleRulesImmutable() {
        DocumentProfile profile = new DocumentProfile(
                "test-profile",
                "Test Profile",
                validPageRule(),
                List.of(validStyleRule("body")),
                List.of()
        );

        assertThrows(UnsupportedOperationException.class, () -> profile.styleRules().add(validStyleRule("title")));
    }

    @Test
    void shouldMakeComponentRulesImmutable() {
        DocumentProfile profile = new DocumentProfile(
                "test-profile",
                "Test Profile",
                validPageRule(),
                List.of(validStyleRule("body")),
                List.of(validCoverComponentRule("cover"))
        );

        assertThrows(UnsupportedOperationException.class, () -> profile.componentRules().add(validCoverComponentRule("title-page")));
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

    private static CoverComponentRule validCoverComponentRule(String componentId) {
        return new CoverComponentRule(
                componentId,
                new CoverStyleMapping(
                        "cover.top",
                        "cover.author",
                        "cover.title",
                        "cover.subtitle",
                        "cover.bottom"
                ),
                validCoverLayoutRule()
        );
    }

    private static CoverLayoutRule validCoverLayoutRule() {
        return new CoverLayoutRule(
                BigDecimal.valueOf(45),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(40),
                0
        );
    }
}