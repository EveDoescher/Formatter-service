package com.abntbuilder.formatter.rendering.component.summary;

import com.abntbuilder.formatter.document.component.summary.SummaryComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.output.docx.api.DocxTocBlock;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.summary.SummaryComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.MetadataConsumingRenderer;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyContentMetadata;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodySectionMetadata;

import java.util.ArrayList;
import java.util.List;

public final class SummaryRenderer implements MetadataConsumingRenderer<SummaryComponent> {

    public static final String COMPONENT_ID = "summary";

    @Override public String componentId() { return COMPONENT_ID; }
    @Override public Class<SummaryComponent> componentType() { return SummaryComponent.class; }

    @Override
    public List<DocxBlock> renderWithMetadata(
            SummaryComponent component, DocumentProfile profile, BodyContentMetadata metadata) {
        SummaryComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(COMPONENT_ID, SummaryComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        blocks.add(new DocxParagraph(
                List.of(DocxRun.of(rule.headingText(), headingStyle)), headingStyle
        ));

        if (rule.useTocField()) {
            int maxLevel = rule.entryStyleIdsByLevel().size();
            String tocInstruction = "TOC \\o \"1-" + maxLevel + "\" \\h \\z \\u";
            blocks.add(new DocxTocBlock(headingStyle, tocInstruction));
        } else {
            for (BodySectionMetadata section : metadata.sections()) {
                int level = section.level();
                int styleIndex = Math.min(level - 1, rule.entryStyleIdsByLevel().size() - 1);
                StyleRule entryStyle = styleResolver.resolve(rule.entryStyleIdsByLevel().get(styleIndex));
                blocks.add(new DocxParagraph(
                        List.of(DocxRun.of(section.renderedTitle(), entryStyle)), entryStyle
                ));
            }
        }

        return List.copyOf(blocks);
    }
}
