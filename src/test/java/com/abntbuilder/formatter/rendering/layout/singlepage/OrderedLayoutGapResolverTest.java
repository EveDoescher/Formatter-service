package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.profile.model.layout.singlepage.LayoutGapRule;
import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderedLayoutGapResolverTest {

    private final OrderedLayoutGapResolver resolver = new OrderedLayoutGapResolver();

    @Test
    void shouldResolveDirectGapsBetweenPresentGroups() {
        List<ResolvedLayoutGap> gaps = resolver.resolve(
                List.of("a", "b", "c"),
                List.of("a", "b", "c"),
                List.of(gap("a", "b", 30), gap("b", "c", 70))
        );

        assertEquals(2, gaps.size());
        assertEquals("a->b", gaps.get(0).id());
        assertEquals(0, BigDecimal.valueOf(30).compareTo(gaps.get(0).weight()));
        assertEquals(List.of(gap("a", "b", 30)), gaps.get(0).sourceGapRules());
    }

    @Test
    void shouldSumIntermediateGapsWhenOptionalGroupIsAbsent() {
        List<ResolvedLayoutGap> gaps = resolver.resolve(
                List.of("institution", "authors", "title", "bottom"),
                List.of("institution", "title", "bottom"),
                List.of(
                        gap("institution", "authors", 30),
                        gap("authors", "title", 10),
                        gap("title", "bottom", 60)
                )
        );

        assertEquals(2, gaps.size());
        assertEquals("institution->title", gaps.get(0).id());
        assertEquals(0, BigDecimal.valueOf(40).compareTo(gaps.get(0).weight()));
        assertEquals(2, gaps.get(0).sourceGapRules().size());
    }

    @Test
    void shouldFailWhenPresentGroupIsUnknown() {
        InvalidProfileStructureException exception = assertThrows(
                InvalidProfileStructureException.class,
                () -> resolver.resolve(
                        List.of("a", "b"),
                        List.of("a", "c"),
                        List.of(gap("a", "b", 1))
                )
        );

        assertEquals("Present group is not declared: c", exception.getMessage());
    }

    @Test
    void shouldFailWhenPresentGroupsAreOutOfOrder() {
        InvalidProfileStructureException exception = assertThrows(
                InvalidProfileStructureException.class,
                () -> resolver.resolve(
                        List.of("a", "b", "c"),
                        List.of("a", "c", "b"),
                        List.of(gap("a", "b", 1), gap("b", "c", 1))
                )
        );

        assertEquals("presentGroupOrder must follow declaredGroupOrder.", exception.getMessage());
    }

    @Test
    void shouldFailWhenDeclaredAdjacentGapIsMissing() {
        InvalidProfileStructureException exception = assertThrows(
                InvalidProfileStructureException.class,
                () -> resolver.resolve(
                        List.of("a", "b", "c"),
                        List.of("a", "c"),
                        List.of(gap("a", "b", 1))
                )
        );

        assertEquals("Missing declared gap rule between groups: b and c.", exception.getMessage());
    }

    @Test
    void shouldFailWhenGapRuleIsDuplicated() {
        InvalidProfileStructureException exception = assertThrows(
                InvalidProfileStructureException.class,
                () -> resolver.resolve(
                        List.of("a", "b"),
                        List.of("a", "b"),
                        List.of(gap("a", "b", 1), gap("a", "b", 2))
                )
        );

        assertEquals("Duplicate gap rule: a->b", exception.getMessage());
    }

    @Test
    void shouldFailWhenGapRuleDoesNotConnectAdjacentDeclaredGroups() {
        InvalidProfileStructureException exception = assertThrows(
                InvalidProfileStructureException.class,
                () -> resolver.resolve(
                        List.of("a", "b", "c"),
                        List.of("a", "c"),
                        List.of(gap("a", "c", 1), gap("a", "b", 1), gap("b", "c", 1))
                )
        );

        assertEquals("Gap rule must connect adjacent declared groups: a->c", exception.getMessage());
    }

    private static LayoutGapRule gap(String from, String to, int weight) {
        return new LayoutGapRule(from, to, BigDecimal.valueOf(weight));
    }
}
