package com.abntbuilder.formatter.rendering.component.cover.layout;

import com.abntbuilder.formatter.document.component.cover.CoverComponent;
import com.abntbuilder.formatter.profile.model.component.cover.CoverComponentRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverLayoutRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverStyleMapping;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageGroupRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageItemRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutPolicy;
import com.abntbuilder.formatter.shared.exception.InvalidCoverContentException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CoverProfileContentValidatorTest {

    private final CoverProfileContentValidator validator = new CoverProfileContentValidator();

    @Test
    void shouldFailWhenRequiredItemHasNoContent() {
        CoverComponent cover = new CoverComponent(
                List.of("Universidade"),
                List.of(),
                "Titulo",
                Optional.empty(),
                "Limeira",
                "2026"
        );

        InvalidCoverContentException exception = assertThrows(
                InvalidCoverContentException.class,
                () -> validator.validate(cover, ruleWithRequiredAuthors())
        );

        assertEquals("cover required item has no content: authors.", exception.getMessage());
    }

    @Test
    void shouldFailWhenRequiredGroupHasNoContent() {
        CoverComponent cover = new CoverComponent(
                List.of("Universidade"),
                List.of(),
                "Titulo",
                Optional.empty(),
                "Limeira",
                "2026"
        );

        InvalidCoverContentException exception = assertThrows(
                InvalidCoverContentException.class,
                () -> validator.validate(cover, ruleWithRequiredOptionalOnlyGroup())
        );

        assertEquals("cover required group has no content: cover.optional.", exception.getMessage());
    }

    @Test
    void shouldFailWhenProfileDeclaresUnknownCoverItem() {
        CoverComponent cover = new CoverComponent(
                List.of("Universidade"),
                List.of("Autor"),
                "Titulo",
                Optional.empty(),
                "Limeira",
                "2026"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(cover, ruleWithUnknownItem())
        );

        assertEquals("Unknown cover item id: unknownItem", exception.getMessage());
    }

    private static CoverComponentRule ruleWithRequiredAuthors() {
        return new CoverComponentRule(
                "cover",
                new com.abntbuilder.formatter.profile.model.component.ComponentContentBindings(java.util.Map.of()),
                styleMapping(),
                new CoverLayoutRule(
                        List.of(new SinglePageGroupRule(
                                "cover.authors",
                                true,
                                List.of(new SinglePageItemRule("authors", true, Optional.empty()))
                        )),
                        List.of(),
                        SinglePageLayoutPolicy.defaultSinglePagePolicy()
                )
        );
    }

    private static CoverComponentRule ruleWithRequiredOptionalOnlyGroup() {
        return new CoverComponentRule(
                "cover",
                new com.abntbuilder.formatter.profile.model.component.ComponentContentBindings(java.util.Map.of()),
                styleMapping(),
                new CoverLayoutRule(
                        List.of(new SinglePageGroupRule(
                                "cover.optional",
                                true,
                                List.of(new SinglePageItemRule("subtitle", false, Optional.empty()))
                        )),
                        List.of(),
                        SinglePageLayoutPolicy.defaultSinglePagePolicy()
                )
        );
    }

    private static CoverComponentRule ruleWithUnknownItem() {
        return new CoverComponentRule(
                "cover",
                new com.abntbuilder.formatter.profile.model.component.ComponentContentBindings(java.util.Map.of()),
                styleMapping(),
                new CoverLayoutRule(
                        List.of(new SinglePageGroupRule(
                                "cover.unknown",
                                true,
                                List.of(new SinglePageItemRule("unknownItem", true, Optional.empty()))
                        )),
                        List.of(),
                        SinglePageLayoutPolicy.defaultSinglePagePolicy()
                )
        );
    }

    private static CoverStyleMapping styleMapping() {
        return new CoverStyleMapping(
                "cover.top",
                "cover.author",
                "cover.title",
                "cover.subtitle",
                "cover.bottom",
                "cover.bottom"
        );
    }
}
