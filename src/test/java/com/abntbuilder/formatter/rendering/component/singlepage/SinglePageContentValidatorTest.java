package com.abntbuilder.formatter.rendering.component.singlepage;

import com.abntbuilder.formatter.document.component.singlepage.ComposedTextValue;
import com.abntbuilder.formatter.document.component.singlepage.ContentValue;
import com.abntbuilder.formatter.document.component.singlepage.SignatureBlockListValue;
import com.abntbuilder.formatter.document.component.singlepage.SinglePageContent;
import com.abntbuilder.formatter.document.component.singlepage.TextListValue;
import com.abntbuilder.formatter.document.component.singlepage.TextValue;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.PageOrientation;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.StyleType;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.profile.model.component.singlepage.ComposedTextSlotRule;
import com.abntbuilder.formatter.profile.model.component.singlepage.SignatureBlockListSlotRule;
import com.abntbuilder.formatter.profile.model.component.singlepage.SinglePageComponentRule;
import com.abntbuilder.formatter.profile.model.component.singlepage.SlotRule;
import com.abntbuilder.formatter.profile.model.component.singlepage.TextListSlotRule;
import com.abntbuilder.formatter.profile.model.component.singlepage.TextSlotRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.HorizontalPlacementRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageGroupRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageItemRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutPolicy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutRule;
import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;
import com.abntbuilder.formatter.shared.exception.InvalidSinglePageContentException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SinglePageContentValidatorTest {

    private final SinglePageContentValidator validator = new SinglePageContentValidator();

    @Test
    void shouldPassWhenAllRequiredSlotsPresent() {
        SinglePageContent content = content(Map.of("title", new TextValue("Título")));
        SinglePageComponentRule rule = rule(Map.of("title", new TextSlotRule(true)), Map.of("title", "sp.title"));
        DocumentProfile profile = profile(rule, style("sp.title"));

        assertDoesNotThrow(() -> validator.validate(content, rule, profile));
    }

    @Test
    void shouldPassWhenOptionalSlotAbsent() {
        SinglePageContent content = content(Map.of("title", new TextValue("Título")));
        SinglePageComponentRule rule = rule(
                Map.of("title", new TextSlotRule(true), "subtitle", new TextSlotRule(false)),
                Map.of("title", "sp.title", "subtitle", "sp.subtitle")
        );
        DocumentProfile profile = profile(rule, style("sp.title"), style("sp.subtitle"));

        assertDoesNotThrow(() -> validator.validate(content, rule, profile));
    }

    @Test
    void shouldThrowWhenRequiredSlotMissing() {
        SinglePageContent content = content(Map.of());
        SinglePageComponentRule rule = rule(Map.of("title", new TextSlotRule(true)), Map.of("title", "sp.title"));
        DocumentProfile profile = profile(rule, style("sp.title"));

        assertThrows(InvalidSinglePageContentException.class,
                () -> validator.validate(content, rule, profile));
    }

    @Test
    void shouldThrowOnTextValueInTextListSlot() {
        SinglePageContent content = content(Map.of("authors", new TextValue("Ana Souza")));
        SinglePageComponentRule rule = rule(
                Map.of("authors", new TextListSlotRule(true)),
                Map.of("authors", "sp.authors")
        );
        DocumentProfile profile = profile(rule, style("sp.authors"));

        assertThrows(InvalidSinglePageContentException.class,
                () -> validator.validate(content, rule, profile));
    }

    @Test
    void shouldThrowOnTextValueInComposedTextSlot() {
        SinglePageContent content = content(Map.of("nature", new TextValue("TCC")));
        SinglePageComponentRule rule = rule(
                Map.of("nature", new ComposedTextSlotRule(true, "{workType}", List.of("workType"))),
                Map.of("nature", "sp.nature")
        );
        DocumentProfile profile = profile(rule, style("sp.nature"));

        assertThrows(InvalidSinglePageContentException.class,
                () -> validator.validate(content, rule, profile));
    }

    @Test
    void shouldThrowOnTextValueInSignatureBlockListSlot() {
        SinglePageContent content = content(Map.of("committee", new TextValue("alguém")));
        SinglePageComponentRule rule = rule(
                Map.of("committee", new SignatureBlockListSlotRule(false, false, null, List.of("{name}"), List.of("name"))),
                Map.of("committee", "sp.committee")
        );
        DocumentProfile profile = profile(rule, style("sp.committee"));

        assertThrows(InvalidSinglePageContentException.class,
                () -> validator.validate(content, rule, profile));
    }

    @Test
    void shouldPassForAllCompatibleTypes() {
        SinglePageContent content = content(Map.of(
                "title", new TextValue("Título"),
                "authors", new TextListValue(List.of("Ana Souza")),
                "nature", new ComposedTextValue(Map.of("workType", "TCC")),
                "committee", new SignatureBlockListValue(List.of(Map.of("name", "Prof. Dr. Carlos Lima")))
        ));
        SinglePageComponentRule rule = rule(
                Map.of(
                        "title", new TextSlotRule(true),
                        "authors", new TextListSlotRule(true),
                        "nature", new ComposedTextSlotRule(true, "{workType}", List.of("workType")),
                        "committee", new SignatureBlockListSlotRule(false, false, null, List.of("{name}"), List.of("name"))
                ),
                Map.of(
                        "title", "sp.title",
                        "authors", "sp.authors",
                        "nature", "sp.nature",
                        "committee", "sp.committee"
                )
        );
        DocumentProfile profile = profile(rule, style("sp.title"), style("sp.authors"),
                style("sp.nature"), style("sp.committee"));

        assertDoesNotThrow(() -> validator.validate(content, rule, profile));
    }

    @Test
    void shouldThrowOnDocumentProfileConstructionWhenStyleMappingReferencesUnknownStyle() {
        SinglePageComponentRule rule = rule(
                Map.of("title", new TextSlotRule(true)),
                Map.of("title", "nonexistent.style")
        );

        // DocumentProfile itself rejects unknown style references at construction time
        assertThrows(IllegalArgumentException.class,
                () -> profile(rule, style("sp.title")));
    }

    @Test
    void shouldIgnoreSlotPresentInContentButAbsentInRule() {
        SinglePageContent content = content(Map.of(
                "title", new TextValue("Título"),
                "extra", new TextValue("dado extra")
        ));
        SinglePageComponentRule rule = rule(
                Map.of("title", new TextSlotRule(true)),
                Map.of("title", "sp.title")
        );
        DocumentProfile profile = profile(rule, style("sp.title"));

        assertDoesNotThrow(() -> validator.validate(content, rule, profile));
    }

    // --- Fixtures ---

    private static SinglePageContent content(Map<String, ContentValue> slots) {
        return new SinglePageContent("testComponent", slots);
    }

    private static SinglePageComponentRule rule(
            Map<String, SlotRule> slots,
            Map<String, String> styleMapping
    ) {
        SinglePageItemRule item = new SinglePageItemRule("title", true, Optional.empty(),
                HorizontalPlacementRule.fullContentWidth());
        SinglePageGroupRule group = new SinglePageGroupRule("main", true, List.of(item));
        SinglePageLayoutRule layout = new SinglePageLayoutRule(
                List.of(group), List.of(), SinglePageLayoutPolicy.defaultSinglePagePolicy());
        return new SinglePageComponentRule("testComponent", slots, styleMapping, layout);
    }

    private static DocumentProfile profile(SinglePageComponentRule rule, StyleRule... styles) {
        return new DocumentProfile(
                "test-profile", "Test Profile",
                pageRule(),
                List.of(styles),
                List.of(rule),
                List.of(rule.componentId())
        );
    }

    private static StyleRule style(String id) {
        return new StyleRule(
                id, StyleType.PARAGRAPH, "Times New Roman", BigDecimal.valueOf(12),
                TextAlignment.LEFT, BigDecimal.ONE,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO,
                false, false, false
        );
    }

    private static PageRule pageRule() {
        return new PageRule(
                BigDecimal.valueOf(21), BigDecimal.valueOf(29.7),
                BigDecimal.valueOf(3), BigDecimal.valueOf(2),
                BigDecimal.valueOf(2), BigDecimal.valueOf(3),
                PageOrientation.PORTRAIT
        );
    }
}
