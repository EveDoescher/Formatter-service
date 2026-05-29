package com.abntbuilder.formatter.profile.model.component.cover;

import com.abntbuilder.formatter.profile.model.layout.singlepage.LayoutGapRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageGroupRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageItemRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutPolicy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CoverLayoutRuleStructureTest {

    @Test
    void shouldRejectEmptyGroups() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new CoverLayoutRule(
                        List.of(),
                        List.of(),
                        SinglePageLayoutPolicy.defaultSinglePagePolicy()
                )
        );

        assertEquals("groups must not be empty.", exception.getMessage());
    }

    @Test
    void shouldRejectMissingGapRulesWhenMultipleGroupsAreDeclared() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new CoverLayoutRule(
                        List.of(group("a"), group("b")),
                        List.of(),
                        SinglePageLayoutPolicy.defaultSinglePagePolicy()
                )
        );

        assertEquals("gapRules must not be empty when more than one group is declared.", exception.getMessage());
    }

    @Test
    void shouldRejectUnknownGapGroup() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new CoverLayoutRule(
                        List.of(group("a"), group("b")),
                        List.of(new LayoutGapRule("a", "c", BigDecimal.ONE)),
                        SinglePageLayoutPolicy.defaultSinglePagePolicy()
                )
        );

        assertEquals("Unknown gap toGroupId: c", exception.getMessage());
    }

    @Test
    void shouldRejectDuplicatedGroup() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new CoverLayoutRule(
                        List.of(group("a"), group("a")),
                        List.of(new LayoutGapRule("a", "a", BigDecimal.ONE)),
                        SinglePageLayoutPolicy.defaultSinglePagePolicy()
                )
        );

        assertEquals("Duplicate single-page group id: a", exception.getMessage());
    }

    private static SinglePageGroupRule group(String id) {
        return new SinglePageGroupRule(
                id,
                true,
                List.of(new SinglePageItemRule("title", true, Optional.empty()))
        );
    }
}
