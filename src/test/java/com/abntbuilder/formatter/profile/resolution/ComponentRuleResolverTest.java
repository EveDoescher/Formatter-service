package com.abntbuilder.formatter.profile.resolution;

import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.PageOrientation;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.StyleType;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverComponentRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverLayoutRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverStyleMapping;
import com.abntbuilder.formatter.profile.model.layout.singlepage.LayoutGapRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageGroupRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageItemRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutPolicy;
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

        CoverComponentRule rule = resolver.resolve("cover", CoverComponentRule.class);

        assertEquals("cover", rule.componentId());
        assertEquals("cover.title", rule.styleMapping().titleStyleId());

        assertEquals(
                List.of(
                        CoverLayoutRule.INSTITUTION_GROUP_ID,
                        CoverLayoutRule.AUTHORS_GROUP_ID,
                        CoverLayoutRule.TITLE_GROUP_ID,
                        CoverLayoutRule.BOTTOM_GROUP_ID
                ),
                rule.layoutRule().declaredGroupOrder()
        );
        assertEquals(0, BigDecimal.valueOf(30).compareTo(rule.layoutRule().gapRules().get(0).weight()));
        assertEquals(0, BigDecimal.valueOf(10).compareTo(rule.layoutRule().gapRules().get(1).weight()));
        assertEquals(0, BigDecimal.valueOf(60).compareTo(rule.layoutRule().gapRules().get(2).weight()));
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
                "Component rule for id: cover must be FakeComponentRule but was CoverComponentRule.",
                exception.getMessage()
        );
    }

    private static DocumentProfile validProfileWithCoverRule() {
        return new DocumentProfile(
                "test-profile",
                "Test Profile",
                validPageRule(),
                List.of(validStyleRule("body")),
                List.of(validCoverComponentRule())
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

    private static CoverComponentRule validCoverComponentRule() {
        return new CoverComponentRule(
                "cover",
                new CoverStyleMapping(
                        "cover.top",
                        "cover.author",
                        "cover.title",
                        "cover.subtitle",
                        "cover.bottom",
                        "cover.bottom"
                ),
                validCoverLayoutRule()
        );
    }

    private static CoverLayoutRule validCoverLayoutRule() {
        return new CoverLayoutRule(
                List.of(
                        new SinglePageGroupRule(
                                CoverLayoutRule.INSTITUTION_GROUP_ID,
                                true,
                                List.of(new SinglePageItemRule("institutionalLines", true, Optional.empty()))
                        ),
                        new SinglePageGroupRule(
                                CoverLayoutRule.AUTHORS_GROUP_ID,
                                false,
                                List.of(new SinglePageItemRule("authors", false, Optional.empty()))
                        ),
                        new SinglePageGroupRule(
                                CoverLayoutRule.TITLE_GROUP_ID,
                                true,
                                List.of(
                                        new SinglePageItemRule("title", true, Optional.empty()),
                                        new SinglePageItemRule("subtitle", false, Optional.empty())
                                )
                        ),
                        new SinglePageGroupRule(
                                CoverLayoutRule.BOTTOM_GROUP_ID,
                                true,
                                List.of(
                                        new SinglePageItemRule("city", true, Optional.of(1)),
                                        new SinglePageItemRule("year", true, Optional.of(1))
                                )
                        )
                ),
                List.of(
                        new LayoutGapRule(CoverLayoutRule.INSTITUTION_GROUP_ID, CoverLayoutRule.AUTHORS_GROUP_ID, BigDecimal.valueOf(30)),
                        new LayoutGapRule(CoverLayoutRule.AUTHORS_GROUP_ID, CoverLayoutRule.TITLE_GROUP_ID, BigDecimal.valueOf(10)),
                        new LayoutGapRule(CoverLayoutRule.TITLE_GROUP_ID, CoverLayoutRule.BOTTOM_GROUP_ID, BigDecimal.valueOf(60))
                ),
                SinglePageLayoutPolicy.defaultSinglePagePolicy()
        );
    }

    private record FakeComponentRule(String componentId) implements ComponentRule {
    }
}
