package com.abntbuilder.formatter.profile.model;

import com.abntbuilder.formatter.profile.model.component.singlepage.SinglePageComponentRule;
import com.abntbuilder.formatter.profile.model.component.singlepage.TextSlotRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.LayoutGapRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageGroupRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageItemRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutPolicy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DocumentProfileTest {

    @Test
    void shouldCreateValidDocumentProfile() {
        DocumentProfile profile = new DocumentProfile(
                "test-profile",
                "Test Profile",
                validPageRule(),
                List.of(validStyleRule("body")),
                List.of(),
                paragraphsOrder()
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
                validCoverStyleRules(),
                List.of(validCoverComponentRule("cover")),
                coverOrder()
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
                List.of(),
                paragraphsOrder()
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
                List.of(),
                paragraphsOrder()
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
                List.of(),
                paragraphsOrder()
        ));
    }

    @Test
    void shouldRejectNullStyleRules() {
        assertThrows(NullPointerException.class, () -> new DocumentProfile(
                "test-profile",
                "Test Profile",
                validPageRule(),
                null,
                List.of(),
                paragraphsOrder()
        ));
    }

    @Test
    void shouldRejectNullComponentRules() {
        assertThrows(NullPointerException.class, () -> new DocumentProfile(
                "test-profile",
                "Test Profile",
                validPageRule(),
                List.of(validStyleRule("body")),
                null,
                paragraphsOrder()
        ));
    }

    @Test
    void shouldRejectEmptyStyleRules() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new DocumentProfile(
                "test-profile",
                "Test Profile",
                validPageRule(),
                List.of(),
                List.of(),
                paragraphsOrder()
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
                List.of(),
                paragraphsOrder()
        ));

        assertEquals("Duplicate style rule id: body", exception.getMessage());
    }

    @Test
    void shouldRejectDuplicateComponentRuleIds() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new DocumentProfile(
                "test-profile",
                "Test Profile",
                validPageRule(),
                validCoverStyleRules(),
                List.of(
                        validCoverComponentRule("cover"),
                        validCoverComponentRule("cover")
                ),
                coverOrder()
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
                List.of(),
                paragraphsOrder()
        );

        assertThrows(UnsupportedOperationException.class, () -> profile.styleRules().add(validStyleRule("title")));
    }

    @Test
    void shouldMakeComponentRulesImmutable() {
        DocumentProfile profile = new DocumentProfile(
                "test-profile",
                "Test Profile",
                validPageRule(),
                validCoverStyleRules(),
                List.of(validCoverComponentRule("cover")),
                coverOrder()
        );

        assertThrows(UnsupportedOperationException.class, () -> profile.componentRules().add(validCoverComponentRule("title-page")));
    }

    @Test
    void shouldRejectUnknownComponentOrderId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new DocumentProfile(
                "test-profile",
                "Test Profile",
                validPageRule(),
                List.of(validStyleRule("body")),
                List.of(),
                List.of("nonExistentComponent")
        ));

        assertEquals("Unknown component order id: nonExistentComponent", exception.getMessage());
    }

    @Test
    void shouldAllowParagraphsAsInternalComponentOrderId() {
        DocumentProfile profile = new DocumentProfile(
                "test-profile",
                "Test Profile",
                validPageRule(),
                List.of(validStyleRule("body")),
                List.of(),
                List.of(DocumentProfile.PARAGRAPHS_INTERNAL_COMPONENT_ID)
        );

        assertEquals(List.of(DocumentProfile.PARAGRAPHS_INTERNAL_COMPONENT_ID), profile.componentOrder());
    }

    @Test
    void shouldRejectPageNumberingWhenCountStartComesAfterVisibilityStart() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new DocumentProfile(
                "test-profile",
                "Test Profile",
                validPageRule(),
                Optional.of(new PageNumberingRule(
                        true,
                        DocumentProfile.PARAGRAPHS_INTERNAL_COMPONENT_ID,
                        "cover",
                        "pageNumber",
                        PageNumberingPlacement.HEADER_RIGHT,
                        BigDecimal.valueOf(2),
                        BigDecimal.valueOf(2)
                )),
                validCoverAndPageNumberStyleRules(),
                List.of(validCoverComponentRule("cover")),
                coverOrder()
        ));

        assertEquals(
                "Page numbering countFromComponentId must not come after visibleFromComponentId.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectPageNumberingWhenCountStartIsNotInComponentOrder() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new DocumentProfile(
                "test-profile",
                "Test Profile",
                validPageRule(),
                Optional.of(new PageNumberingRule(
                        true,
                        "cover",
                        DocumentProfile.PARAGRAPHS_INTERNAL_COMPONENT_ID,
                        "pageNumber",
                        PageNumberingPlacement.HEADER_RIGHT,
                        BigDecimal.valueOf(2),
                        BigDecimal.valueOf(2)
                )),
                validCoverAndPageNumberStyleRules(),
                List.of(validCoverComponentRule("cover")),
                paragraphsOrder()
        ));

        assertEquals(
                "Page numbering countFromComponentId is not present in componentOrder: cover",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectComponentStyleMappingThatReferencesUnknownStyleId() {
        // validCoverComponentRule maps "title" -> "cover.top", but we only provide "cover.nonexistent"
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new DocumentProfile(
                "test-profile",
                "Test Profile",
                validPageRule(),
                List.of(validStyleRule("cover.nonexistent.style")),
                List.of(validCoverComponentRule("cover")),
                coverOrder()
        ));

        assertEquals(
                "Component style mapping references unknown style id: cover.top",
                exception.getMessage()
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

    private static List<StyleRule> validCoverStyleRules() {
        return List.of(
                validStyleRule("cover.top"),
                validStyleRule("cover.author"),
                validStyleRule("cover.title"),
                validStyleRule("cover.subtitle"),
                validStyleRule("cover.bottom")
        );
    }

    private static List<StyleRule> validCoverAndPageNumberStyleRules() {
        return List.of(
                validStyleRule("pageNumber"),
                validStyleRule("cover.top"),
                validStyleRule("cover.author"),
                validStyleRule("cover.title"),
                validStyleRule("cover.subtitle"),
                validStyleRule("cover.bottom")
        );
    }

    private static List<String> paragraphsOrder() {
        return List.of(DocumentProfile.PARAGRAPHS_INTERNAL_COMPONENT_ID);
    }

    private static List<String> coverOrder() {
        return List.of("cover", DocumentProfile.PARAGRAPHS_INTERNAL_COMPONENT_ID);
    }

    private static SinglePageComponentRule validCoverComponentRule(String componentId) {
        SinglePageGroupRule group = new SinglePageGroupRule("top", true,
                List.of(new SinglePageItemRule("title", true, Optional.empty())));
        com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutRule layoutRule =
                new com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutRule(
                        List.of(group), List.of(), SinglePageLayoutPolicy.defaultSinglePagePolicy());
        return new SinglePageComponentRule(
                componentId,
                java.util.Map.of("title", new TextSlotRule(true)),
                java.util.Map.of("title", "cover.top"),
                layoutRule
        );
    }
}
