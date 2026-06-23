package com.abntbuilder.formatter.rendering.component.errata;

import com.abntbuilder.formatter.document.component.errata.ErrataComponent;
import com.abntbuilder.formatter.document.component.errata.ErrataEntry;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.errata.ErrataComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;

import java.util.ArrayList;
import java.util.List;

public final class ErrataRenderer implements ComponentRenderer<ErrataComponent> {

    public static final String COMPONENT_ID = "errata";

    @Override
    public String componentId() { return COMPONENT_ID; }

    @Override
    public Class<ErrataComponent> componentType() { return ErrataComponent.class; }

    @Override
    public List<DocxBlock> render(ErrataComponent component, DocumentProfile profile) {
        ErrataComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(COMPONENT_ID, ErrataComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());
        StyleRule entryStyle = styleResolver.resolve(rule.entryStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        blocks.add(new DocxParagraph(
                List.of(DocxRun.of(rule.headingText(), headingStyle)),
                headingStyle
        ));

        for (ErrataEntry entry : component.entries()) {
            String text = rule.entryTemplate()
                    .replace("{page}", entry.page())
                    .replace("{line}", entry.line())
                    .replace("{incorrect}", entry.incorrectText())
                    .replace("{correct}", entry.correctText());
            blocks.add(new DocxParagraph(List.of(DocxRun.of(text, entryStyle)), entryStyle));
        }

        return List.copyOf(blocks);
    }
}
