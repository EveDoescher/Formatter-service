package com.abntbuilder.formatter.rendering.singlepage;

import com.abntbuilder.formatter.engine.model.content.singlepage.ComposedTextValue;
import com.abntbuilder.formatter.engine.model.content.singlepage.SignatureBlockListValue;
import com.abntbuilder.formatter.engine.model.content.singlepage.SinglePageContent;
import com.abntbuilder.formatter.engine.model.content.singlepage.TextListValue;
import com.abntbuilder.formatter.engine.model.content.singlepage.TextValue;
import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;
import com.abntbuilder.formatter.engine.model.profile.PageOrientation;
import com.abntbuilder.formatter.engine.model.profile.PageRule;
import com.abntbuilder.formatter.engine.model.profile.StyleRule;
import com.abntbuilder.formatter.engine.model.profile.StyleType;
import com.abntbuilder.formatter.engine.model.profile.TextAlignment;
import com.abntbuilder.formatter.engine.model.profile.component.singlepage.ComposedTextSlotRule;
import com.abntbuilder.formatter.engine.model.profile.component.singlepage.SignatureBlockListSlotRule;
import com.abntbuilder.formatter.engine.model.profile.component.singlepage.SinglePageComponentRule;
import com.abntbuilder.formatter.engine.model.profile.component.singlepage.TextListSlotRule;
import com.abntbuilder.formatter.engine.model.profile.component.singlepage.TextSlotRule;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.HorizontalPlacementRule;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.SinglePageGroupRule;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.SinglePageItemRule;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.SinglePageLayoutPolicy;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.SinglePageLayoutRule;
import com.abntbuilder.formatter.rendering.singlepage.OrderedLayoutGapResolver;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageLayoutGroup;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageLayoutInput;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageLayoutItem;
import com.abntbuilder.formatter.rendering.text.ConservativeTextMeasurer;
import com.abntbuilder.formatter.shared.exception.InvalidSinglePageContentException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SinglePageLayoutAssemblerTest {

    private final SinglePageLayoutAssembler assembler = new SinglePageLayoutAssembler(
            new ConservativeTextMeasurer(),
            new OrderedLayoutGapResolver()
    );

    // --- TextValue ---

    @Test
    void shouldAssembleTextSlot() {
        SinglePageContent content = contentWith(Map.of("title", new TextValue("Título do Trabalho")));
        SinglePageComponentRule rule = ruleWith(
                Map.of("title", new TextSlotRule(true)),
                Map.of("title", "sp.title"),
                singleItemLayout("title")
        );
        DocumentProfile profile = profileWith(rule, styleRule("sp.title"));

        SinglePageLayoutInput input = assembler.assemble(content, profile, rule);

        List<SinglePageLayoutItem> items = allItems(input);
        assertEquals(1, items.size());
        assertEquals("title", items.get(0).id());
        assertTrue(items.get(0).paragraphText().contains("Título"));
    }

    // --- TextListValue ---

    @Test
    void shouldAssembleTextListSlotAsMultipleItems() {
        SinglePageContent content = contentWith(Map.of(
                "authors", new TextListValue(List.of("Ana Souza", "Carlos Lima"))));
        SinglePageComponentRule rule = ruleWith(
                Map.of("authors", new TextListSlotRule(true)),
                Map.of("authors", "sp.authors"),
                singleItemLayout("authors")
        );
        DocumentProfile profile = profileWith(rule, styleRule("sp.authors"));

        SinglePageLayoutInput input = assembler.assemble(content, profile, rule);

        List<SinglePageLayoutItem> items = allItems(input);
        assertEquals(2, items.size());
        assertEquals("authors[0]", items.get(0).id());
        assertEquals("authors[1]", items.get(1).id());
        assertEquals("Ana Souza", items.get(0).paragraphText());
        assertEquals("Carlos Lima", items.get(1).paragraphText());
    }

    // --- ComposedTextValue ---

    @Test
    void shouldAssembleComposedTextSlotWithTemplate() {
        SinglePageContent content = contentWith(Map.of(
                "nature", new ComposedTextValue(Map.of(
                        "workType", "TCC",
                        "courseName", "ADS"
                ))));
        SinglePageComponentRule rule = ruleWith(
                Map.of("nature", new ComposedTextSlotRule(true, "{workType} em {courseName}.", List.of("workType", "courseName"))),
                Map.of("nature", "sp.nature"),
                singleItemLayout("nature")
        );
        DocumentProfile profile = profileWith(rule, styleRule("sp.nature"));

        SinglePageLayoutInput input = assembler.assemble(content, profile, rule);

        List<SinglePageLayoutItem> items = allItems(input);
        assertEquals(1, items.size());
        assertEquals("TCC em ADS.", items.get(0).paragraphText());
    }

    @Test
    void shouldThrowWhenComposedTextMissingTemplateField() {
        SinglePageContent content = contentWith(Map.of(
                "nature", new ComposedTextValue(Map.of("workType", "TCC"))));
        SinglePageComponentRule rule = ruleWith(
                Map.of("nature", new ComposedTextSlotRule(true, "{workType} em {courseName}.", List.of("workType", "courseName"))),
                Map.of("nature", "sp.nature"),
                singleItemLayout("nature")
        );
        DocumentProfile profile = profileWith(rule, styleRule("sp.nature"));

        assertThrows(InvalidSinglePageContentException.class,
                () -> assembler.assemble(content, profile, rule));
    }

    // --- SignatureBlockListValue ---

    @Test
    void shouldAssembleSignatureBlockListSlot() {
        SinglePageContent content = contentWith(Map.of(
                "committee", new SignatureBlockListValue(List.of(
                        Map.of("name", "Prof. Dr. Carlos Lima", "role", "Orientador")
                ))));
        SinglePageComponentRule rule = ruleWith(
                Map.of("committee", new SignatureBlockListSlotRule(
                        false, true, "________",
                        List.of("{name}", "{role}"), List.of("name", "role"))),
                Map.of("committee", "sp.committee"),
                singleItemLayout("committee")
        );
        DocumentProfile profile = profileWith(rule, styleRule("sp.committee"));

        SinglePageLayoutInput input = assembler.assemble(content, profile, rule);

        List<SinglePageLayoutItem> items = allItems(input);
        // name + role + signature line = 3 items
        assertEquals(3, items.size());
        assertEquals("Prof. Dr. Carlos Lima", items.get(0).paragraphText());
        assertEquals("Orientador", items.get(1).paragraphText());
        assertEquals("________", items.get(2).paragraphText());
    }

    // --- Optional slot absent ---

    @Test
    void shouldSkipOptionalSlotWhenAbsent() {
        SinglePageContent content = contentWith(Map.of("title", new TextValue("Título")));
        SinglePageComponentRule rule = ruleWith(
                Map.of(
                        "title", new TextSlotRule(true),
                        "subtitle", new TextSlotRule(false)
                ),
                Map.of("title", "sp.title", "subtitle", "sp.subtitle"),
                twoItemLayout("title", "subtitle")
        );
        DocumentProfile profile = profileWith(rule, styleRule("sp.title"), styleRule("sp.subtitle"));

        SinglePageLayoutInput input = assembler.assemble(content, profile, rule);

        List<SinglePageLayoutItem> items = allItems(input);
        assertEquals(1, items.size());
        assertEquals("title", items.get(0).id());
    }

    // --- Required slot absent ---

    @Test
    void shouldThrowWhenRequiredSlotAbsent() {
        SinglePageContent content = contentWith(Map.of());
        SinglePageComponentRule rule = ruleWith(
                Map.of("title", new TextSlotRule(true)),
                Map.of("title", "sp.title"),
                singleItemLayout("title")
        );
        DocumentProfile profile = profileWith(rule, styleRule("sp.title"));

        assertThrows(InvalidSinglePageContentException.class,
                () -> assembler.assemble(content, profile, rule));
    }

    // --- Fixtures ---

    private static SinglePageContent contentWith(Map<String, com.abntbuilder.formatter.engine.model.content.singlepage.ContentValue> slots) {
        return new SinglePageContent("testComponent", slots);
    }

    private static SinglePageComponentRule ruleWith(
            Map<String, com.abntbuilder.formatter.engine.model.profile.component.singlepage.SlotRule> slots,
            Map<String, String> styleMapping,
            SinglePageLayoutRule layoutRule
    ) {
        return new SinglePageComponentRule("testComponent", slots, styleMapping, layoutRule);
    }

    private static DocumentProfile profileWith(SinglePageComponentRule rule, StyleRule... styles) {
        return new DocumentProfile(
                "test-profile",
                "Test Profile",
                pageRule(),
                List.of(styles),
                List.of(rule),
                List.of(rule.componentId())
        );
    }

    private static SinglePageLayoutRule singleItemLayout(String itemId) {
        SinglePageItemRule item = new SinglePageItemRule(itemId, true, Optional.empty(),
                HorizontalPlacementRule.fullContentWidth());
        SinglePageGroupRule group = new SinglePageGroupRule("main", true, List.of(item));
        return new SinglePageLayoutRule(List.of(group), List.of(), SinglePageLayoutPolicy.defaultSinglePagePolicy());
    }

    private static SinglePageLayoutRule twoItemLayout(String itemId1, String itemId2) {
        SinglePageItemRule item1 = new SinglePageItemRule(itemId1, true, Optional.empty(),
                HorizontalPlacementRule.fullContentWidth());
        SinglePageItemRule item2 = new SinglePageItemRule(itemId2, false, Optional.empty(),
                HorizontalPlacementRule.fullContentWidth());
        SinglePageGroupRule group = new SinglePageGroupRule("main", true, List.of(item1, item2));
        return new SinglePageLayoutRule(List.of(group), List.of(), SinglePageLayoutPolicy.defaultSinglePagePolicy());
    }

    private static StyleRule styleRule(String id) {
        return new StyleRule(
                id, StyleType.PARAGRAPH, "Times New Roman", BigDecimal.valueOf(12),
                TextAlignment.CENTER, BigDecimal.ONE,
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

    private static List<SinglePageLayoutItem> allItems(SinglePageLayoutInput input) {
        return input.groups().stream()
                .flatMap(g -> g.items().stream())
                .toList();
    }
}
