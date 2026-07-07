package com.abntbuilder.formatter.rendering.component.sectioned;

import com.abntbuilder.formatter.document.component.bodycontent.BodyContentComponent;
import com.abntbuilder.formatter.document.component.sectioned.SectionedContent;
import com.abntbuilder.formatter.document.component.sectioned.SectionedItem;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.sectioned.SectionedComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.ComponentRenderResult;
import com.abntbuilder.formatter.rendering.component.Phase0ConsumingRenderer;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyContentRenderer;
import com.abntbuilder.formatter.rendering.phase0.Phase0Index;

import java.util.ArrayList;
import java.util.List;

public final class SectionedRenderer
        implements Phase0ConsumingRenderer<SectionedContent, ComponentRenderResult> {

    private final String componentId;

    public SectionedRenderer(String componentId) {
        if (componentId == null || componentId.isBlank())
            throw new IllegalArgumentException("SectionedRenderer.componentId must not be blank.");
        this.componentId = componentId;
    }

    @Override
    public String componentId() { return componentId; }

    @Override
    public Class<SectionedContent> componentType() { return SectionedContent.class; }

    @Override
    public ComponentRenderResult renderWithPhase0(
            SectionedContent component, DocumentProfile profile, Phase0Index phase0Index) {
        SectionedComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(componentId, SectionedComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        BodyContentRenderer contentRenderer = new BodyContentRenderer(componentId);
        char letter = 'A';
        for (SectionedItem item : component.items()) {
            String heading = rule.headingTemplate()
                    .replace("{letter}", String.valueOf(letter))
                    .replace("{title}", item.title());
            blocks.add(new DocxParagraph(List.of(DocxRun.of(heading, headingStyle)), headingStyle));

            if (!item.sections().isEmpty()) {
                BodyContentComponent sectionContent = new BodyContentComponent(item.sections());
                blocks.addAll(contentRenderer.renderWithPhase0(sectionContent, profile, phase0Index).blocks());
            }
            letter++;
        }
        return () -> List.copyOf(blocks);
    }
}
