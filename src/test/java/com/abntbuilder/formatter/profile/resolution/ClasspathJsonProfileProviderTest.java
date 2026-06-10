package com.abntbuilder.formatter.profile.resolution;

import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.component.approvalsheet.ApprovalSheetComponentRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.BodyContentComponentRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverComponentRule;
import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageComponentRule;
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
        assertEquals(List.of("cover", "titlePage", "approvalSheet", "bodyContent", "paragraphs"), profile.componentOrder());
        assertEquals("titlePage", profile.pageNumberingRule().orElseThrow().countFromComponentId());
        assertEquals("bodyContent", profile.pageNumberingRule().orElseThrow().visibleFromComponentId());
        assertEquals("pageNumber", profile.pageNumberingRule().orElseThrow().styleId());
        assertEquals(BigDecimal.valueOf(2), profile.pageNumberingRule().orElseThrow().verticalDistanceFromPageEdgeCm());
        assertEquals(BigDecimal.valueOf(2), profile.pageNumberingRule().orElseThrow().horizontalDistanceFromPageEdgeCm());
        CoverComponentRule coverRule = assertInstanceOf(CoverComponentRule.class, profile.componentRules().get(0));
        assertInstanceOf(TitlePageComponentRule.class, profile.componentRules().get(1));
        assertInstanceOf(ApprovalSheetComponentRule.class, profile.componentRules().get(2));
        BodyContentComponentRule bodyContentRule = assertInstanceOf(
                BodyContentComponentRule.class,
                profile.componentRules().get(3)
        );
        assertEquals(1, bodyContentRule.layout().blankLinesBeforeSectionTitleWhenPrecededByContent());
        assertEquals(1, bodyContentRule.layout().blankLinesAfterSectionTitle());
        assertEquals(false, bodyContentRule.layout().pageBreakBeforePrimarySection());
        assertEquals("bodyContent.paragraph", bodyContentRule.layout().blankLineStyleId());
        assertEquals("work.authors", coverRule.contentBindings().sourceFor("authors").orElseThrow());
        assertEquals("work.title", coverRule.contentBindings().sourceFor("title").orElseThrow());
    }
}
