package com.abntbuilder.formatter.config;

import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.component.bodycontent.BodyContentComponentRule;
import com.abntbuilder.formatter.profile.model.component.singlepage.SinglePageComponentRule;
import com.abntbuilder.formatter.profile.model.component.singlepage.TextListSlotRule;
import com.abntbuilder.formatter.profile.model.component.singlepage.ComposedTextSlotRule;
import com.abntbuilder.formatter.profile.model.component.singlepage.SignatureBlockListSlotRule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ClasspathJsonProfileProviderTest {

    @Test
    void shouldLoadOfficialAbntUnipProfileFromClasspathJson() {
        DocumentProfile profile = new ClasspathJsonProfileProvider().findById("abnt-unip-profile");

        assertEquals("ABNT UNIP Profile", profile.displayName());
        assertEquals(List.of("cover", "titlePage", "errata", "approvalSheet", "dedication", "acknowledgments",
                "epigraph", "resumo", "abstract",
                "listOfAbbreviations", "listOfSymbols", "summary",
                "listOfFigures", "listOfTables", "listOfFrames", "listOfCharts", "listOfCodeListings",
                "bodyContent", "references", "appendix", "annex", "glossary"),
                profile.componentOrder());
        assertEquals("titlePage", profile.pageNumberingRule().orElseThrow().countFromComponentId());
        assertEquals("bodyContent", profile.pageNumberingRule().orElseThrow().visibleFromComponentId());
        assertEquals("pageNumber", profile.pageNumberingRule().orElseThrow().styleId());
        assertEquals(BigDecimal.valueOf(2), profile.pageNumberingRule().orElseThrow().verticalDistanceFromPageEdgeCm());
        assertEquals(BigDecimal.valueOf(2), profile.pageNumberingRule().orElseThrow().horizontalDistanceFromPageEdgeCm());

        SinglePageComponentRule coverRule = assertInstanceOf(SinglePageComponentRule.class, profile.componentRules().get(0));
        assertEquals("cover", coverRule.componentId());
        assertInstanceOf(TextListSlotRule.class, coverRule.slots().get("authors"));
        assertInstanceOf(TextListSlotRule.class, coverRule.slots().get("institutionalLines"));
        assertEquals("cover.title", coverRule.styleMapping().get("title"));
        assertEquals("cover.author", coverRule.styleMapping().get("authors"));

        SinglePageComponentRule titlePageRule = assertInstanceOf(SinglePageComponentRule.class, profile.componentRules().get(1));
        assertEquals("titlePage", titlePageRule.componentId());
        assertInstanceOf(ComposedTextSlotRule.class, titlePageRule.slots().get("nature"));
        assertInstanceOf(ComposedTextSlotRule.class, titlePageRule.slots().get("advisor"));

        SinglePageComponentRule approvalRule = assertInstanceOf(SinglePageComponentRule.class, profile.componentRules().get(2));
        assertEquals("approvalSheet", approvalRule.componentId());
        assertInstanceOf(SignatureBlockListSlotRule.class, approvalRule.slots().get("committeeMembers"));

        BodyContentComponentRule bodyContentRule = assertInstanceOf(
                BodyContentComponentRule.class,
                profile.componentRules().get(3)
        );
        assertEquals(1, bodyContentRule.layout().blankLinesBeforeSectionTitleWhenPrecededByContent());
        assertEquals(1, bodyContentRule.layout().blankLinesAfterSectionTitle());
        assertEquals(false, bodyContentRule.layout().pageBreakBeforePrimarySection());
        assertEquals("bodyContent.paragraph", bodyContentRule.layout().blankLineStyleId());
        assertEquals("bodyContent.figure.caption", bodyContentRule.figure().captionStyleId());
        assertEquals("Figura {number} - {caption}", bodyContentRule.figure().captionTemplate());
    }
}
