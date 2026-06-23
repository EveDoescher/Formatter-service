package com.abntbuilder.formatter.rendering.component.glossary;

import com.abntbuilder.formatter.document.component.glossary.GlossaryComponent;
import com.abntbuilder.formatter.document.component.glossary.GlossaryEntry;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.glossary.GlossaryComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;

import java.util.ArrayList;
import java.util.List;

public final class GlossaryRenderer implements ComponentRenderer<GlossaryComponent> {

    public static final String COMPONENT_ID = "glossary";

    @Override
    public String componentId() { return COMPONENT_ID; }

    @Override
    public Class<GlossaryComponent> componentType() { return GlossaryComponent.class; }

    @Override
    public List<DocxBlock> render(GlossaryComponent component, DocumentProfile profile) {
        GlossaryComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(COMPONENT_ID, GlossaryComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());
        StyleRule entryStyle = styleResolver.resolve(rule.entryStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        blocks.add(new DocxParagraph(List.of(DocxRun.of(rule.headingText(), headingStyle)), headingStyle));
        for (GlossaryEntry entry : component.entries()) {
            String text = entry.term() + rule.termSeparator() + entry.definition();
            blocks.add(new DocxParagraph(List.of(DocxRun.of(text, entryStyle)), entryStyle));
        }
        return List.copyOf(blocks);
    }
}
