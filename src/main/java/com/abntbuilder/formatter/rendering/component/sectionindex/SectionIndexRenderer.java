package com.abntbuilder.formatter.rendering.component.sectionindex;

import com.abntbuilder.formatter.document.component.sectionindex.SectionIndexContent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.output.docx.api.DocxTocBlock;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.sectionindex.SectionIndexComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.MetadataConsumingRenderer;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodySectionMetadata;
import com.abntbuilder.formatter.rendering.phase0.Phase0Index;

import java.util.ArrayList;
import java.util.List;

public final class SectionIndexRenderer implements MetadataConsumingRenderer<SectionIndexContent> {

    private final String componentId;

    public SectionIndexRenderer(String componentId) {
        if (componentId == null || componentId.isBlank())
            throw new IllegalArgumentException("SectionIndexRenderer.componentId must not be blank.");
        this.componentId = componentId;
    }

    @Override
    public String componentId() { return componentId; }

    @Override
    public Class<SectionIndexContent> componentType() { return SectionIndexContent.class; }

    @Override
    public List<DocxBlock> renderWithMetadata(
            SectionIndexContent component, DocumentProfile profile, Phase0Index phase0Index) {
        SectionIndexComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(componentId, SectionIndexComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        blocks.add(new DocxParagraph(
                List.of(DocxRun.of(rule.headingText(), headingStyle)), headingStyle
        ));

        if (rule.useTocField()) {
            int maxLevel = rule.entryStyleIdsByLevel().size();
            String tocInstruction = "TOC \\o \"1-" + maxLevel + "\" \\h \\z \\u";
            List<StyleRule> entryStyles = rule.entryStyleIdsByLevel().stream()
                    .map(styleResolver::resolve)
                    .toList();
            double contentWidthCm = profile.pageRule().widthCm().doubleValue()
                    - profile.pageRule().marginLeftCm().doubleValue()
                    - profile.pageRule().marginRightCm().doubleValue();
            blocks.add(new DocxTocBlock(headingStyle, tocInstruction, entryStyles, contentWidthCm));
        } else {
            for (BodySectionMetadata section : phase0Index.sections().values()) {
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
