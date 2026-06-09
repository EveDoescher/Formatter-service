package com.abntbuilder.formatter.profile.resolution;

import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.component.approvalsheet.ApprovalSheetComponentRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverComponentRule;
import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageComponentRule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ClasspathJsonProfileProviderTest {

    @Test
    void shouldLoadOfficialAbntUnipProfileFromClasspathJson() {
        DocumentProfile profile = new ClasspathJsonProfileProvider().findById("abnt-unip-profile");

        assertEquals("ABNT UNIP Profile", profile.displayName());
        assertEquals(List.of("cover", "titlePage", "approvalSheet", "paragraphs"), profile.componentOrder());
        assertInstanceOf(CoverComponentRule.class, profile.componentRules().get(0));
        assertInstanceOf(TitlePageComponentRule.class, profile.componentRules().get(1));
        assertInstanceOf(ApprovalSheetComponentRule.class, profile.componentRules().get(2));
    }
}
