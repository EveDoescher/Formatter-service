package com.abntbuilder.formatter.input.profile;

import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;
import com.abntbuilder.formatter.engine.model.profile.PageOrientation;
import com.abntbuilder.formatter.engine.model.profile.PageRule;
import com.abntbuilder.formatter.engine.model.profile.StyleRule;
import com.abntbuilder.formatter.engine.model.profile.StyleType;
import com.abntbuilder.formatter.engine.model.profile.TextAlignment;
import com.abntbuilder.formatter.engine.model.profile.component.ComponentRule;
import com.abntbuilder.formatter.engine.model.profile.component.singlepage.SinglePageComponentRule;
import com.abntbuilder.formatter.engine.model.profile.component.singlepage.TextSlotRule;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.LayoutGapRule;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.SinglePageGroupRule;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.SinglePageItemRule;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.SinglePageLayoutPolicy;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.SinglePageLayoutRule;
import com.abntbuilder.formatter.shared.exception.ComponentRuleTypeMismatchException;
import com.abntbuilder.formatter.shared.exception.MissingComponentRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ComponentRuleResolverTest {

    @Test
    void shouldResolveExistingComponentRuleById() {
        ComponentRuleResolver resolver = new ComponentRuleResolver(validProfileWithCoverRule());

        ComponentRule rule = resolver.resolve("cover");

        assertEquals("cover", rule.componentId());
    }

    @Test
    void shouldResolveExistingComponentRuleByIdAndType() {
        ComponentRuleResolver resolver = new ComponentRuleResolver(validProfileWithCoverRule());

        SinglePageComponentRule rule = resolver.resolve("cover", SinglePageComponentRule.class);

        assertEquals("cover", rule.componentId());
        assertEquals("cover.title", rule.styleMapping().get("title"));
        assertEquals(List.of("top", "bottom"), rule.layoutRule().declaredGroupOrder());
        assertEquals(0, BigDecimal.valueOf(60).compareTo(rule.layoutRule().gapRules().get(0).weight()));
    }

    @Test
    void shouldRejectNullProfile() {
        assertThrows(NullPointerException.class, () -> new ComponentRuleResolver(null));
    }

    @Test
    void shouldRejectBlankComponentId() {
        ComponentRuleResolver resolver = new ComponentRuleResolver(validProfileWithCoverRule());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> resolver.resolve(" ")
        );

        assertEquals("componentId must not be blank.", exception.getMessage());
    }

    @Test
    void shouldRejectNullExpectedType() {
        ComponentRuleResolver resolver = new ComponentRuleResolver(validProfileWithCoverRule());

        assertThrows(NullPointerException.class, () -> resolver.resolve("cover", null));
    }

    @Test
    void shouldThrowClearExceptionWhenComponentRuleDoesNotExist() {
        ComponentRuleResolver resolver = new ComponentRuleResolver(validProfileWithCoverRule());

        MissingComponentRuleException exception = assertThrows(
                MissingComponentRuleException.class,
                () -> resolver.resolve("title-page")
        );

        assertEquals("Missing component rule for id: title-page", exception.getMessage());
    }

    @Test
    void shouldThrowClearExceptionWhenComponentRuleHasUnexpectedType() {
        ComponentRuleResolver resolver = new ComponentRuleResolver(validProfileWithCoverRule());

        ComponentRuleTypeMismatchException exception = assertThrows(
                ComponentRuleTypeMismatchException.class,
                () -> resolver.resolve("cover", FakeComponentRule.class)
        );

        assertEquals(
                "Component rule for id: cover must be FakeComponentRule but was SinglePageComponentRule.",
                exception.getMessage()
        );
    }

    private static DocumentProfile validProfileWithCoverRule() {
        return new DocumentProfile(
                "test-profile",
                "Test Profile",
                validPageRule(),
                validCoverStyleRules(),
                List.of(validCoverComponentRule()),
                List.of("cover", DocumentProfile.PARAGRAPHS_INTERNAL_COMPONENT_ID)
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
        return List.of(validStyleRule("cover.top"), validStyleRule("cover.title"));
    }

    private static SinglePageComponentRule validCoverComponentRule() {
        SinglePageLayoutRule layout = new SinglePageLayoutRule(
                List.of(
                        new SinglePageGroupRule("top", true,
                                List.of(new SinglePageItemRule("title", true, Optional.empty()))),
                        new SinglePageGroupRule("bottom", true,
                                List.of(new SinglePageItemRule("city", true, Optional.of(1))))
                ),
                List.of(new LayoutGapRule("top", "bottom", BigDecimal.valueOf(60))),
                SinglePageLayoutPolicy.defaultSinglePagePolicy()
        );
        return new SinglePageComponentRule(
                "cover",
                true,
                null,
                java.util.Map.of("title", new TextSlotRule(true, null, null), "city", new TextSlotRule(true, null, null)),
                java.util.Map.of("title", "cover.title", "city", "cover.top"),
                layout
        );
    }

    private record FakeComponentRule(String componentId) implements ComponentRule {
        public boolean required() { return true; }
        public String description() { return null; }
    }
}
