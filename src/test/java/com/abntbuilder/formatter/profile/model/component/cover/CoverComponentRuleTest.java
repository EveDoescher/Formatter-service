package com.abntbuilder.formatter.profile.model.component.cover;

import com.abntbuilder.formatter.profile.model.layout.singlepage.LayoutGapRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageGroupRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageItemRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutPolicy;
import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CoverComponentRuleTest {

    @Test
    void shouldCreateValidCoverComponentRule() {
        CoverComponentRule rule = new CoverComponentRule(
                "cover",
                validStyleMapping(),
                validLayoutRule()
        );

        assertEquals("cover", rule.componentId());
        assertEquals("cover.top", rule.styleMapping().institutionalLinesStyleId());
        assertEquals("cover.author", rule.styleMapping().authorsStyleId());
        assertEquals("cover.title", rule.styleMapping().titleStyleId());
        assertEquals("cover.subtitle", rule.styleMapping().subtitleStyleId());
        assertEquals("cover.bottom", rule.styleMapping().cityStyleId());
        assertEquals("cover.bottom", rule.styleMapping().yearStyleId());

        assertEquals(
                List.of(
                        CoverLayoutRule.INSTITUTION_GROUP_ID,
                        CoverLayoutRule.AUTHORS_GROUP_ID,
                        CoverLayoutRule.TITLE_GROUP_ID,
                        CoverLayoutRule.BOTTOM_GROUP_ID
                ),
                rule.layoutRule().declaredGroupOrder()
        );
        assertEquals(3, rule.layoutRule().gapRules().size());
    }

    @Test
    void shouldRejectBlankComponentId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new CoverComponentRule(
                " ",
                validStyleMapping(),
                validLayoutRule()
        ));

        assertEquals("componentId must not be blank.", exception.getMessage());
    }

    @Test
    void shouldRejectNullStyleMapping() {
        assertThrows(NullPointerException.class, () -> new CoverComponentRule(
                "cover",
                null,
                validLayoutRule()
        ));
    }

    @Test
    void shouldRejectNullLayoutRule() {
        assertThrows(NullPointerException.class, () -> new CoverComponentRule(
                "cover",
                validStyleMapping(),
                null
        ));
    }

    @Test
    void shouldRejectBlankStyleIdInsideMapping() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new CoverStyleMapping(
                " ",
                "cover.author",
                "cover.title",
                "cover.subtitle",
                "cover.bottom",
                "cover.bottom"
        ));

        assertEquals("institutionalLinesStyleId must not be blank.", exception.getMessage());
    }

    @Test
    void shouldRejectInvalidGapWeightThroughSinglePageRule() {
        InvalidProfileStructureException exception = assertThrows(InvalidProfileStructureException.class, () -> new CoverLayoutRule(
                validGroups(),
                List.of(
                        new LayoutGapRule(CoverLayoutRule.INSTITUTION_GROUP_ID, CoverLayoutRule.AUTHORS_GROUP_ID, BigDecimal.ZERO)
                ),
                SinglePageLayoutPolicy.defaultSinglePagePolicy()
        ));

        assertEquals("weight must be greater than zero.", exception.getMessage());
    }

    private static CoverStyleMapping validStyleMapping() {
        return new CoverStyleMapping(
                "cover.top",
                "cover.author",
                "cover.title",
                "cover.subtitle",
                "cover.bottom",
                "cover.bottom"
        );
    }

    private static CoverLayoutRule validLayoutRule() {
        return new CoverLayoutRule(
                validGroups(),
                List.of(
                        new LayoutGapRule(CoverLayoutRule.INSTITUTION_GROUP_ID, CoverLayoutRule.AUTHORS_GROUP_ID, BigDecimal.valueOf(30)),
                        new LayoutGapRule(CoverLayoutRule.AUTHORS_GROUP_ID, CoverLayoutRule.TITLE_GROUP_ID, BigDecimal.valueOf(10)),
                        new LayoutGapRule(CoverLayoutRule.TITLE_GROUP_ID, CoverLayoutRule.BOTTOM_GROUP_ID, BigDecimal.valueOf(60))
                ),
                SinglePageLayoutPolicy.defaultSinglePagePolicy()
        );
    }

    private static List<SinglePageGroupRule> validGroups() {
        return List.of(
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
        );
    }
}
