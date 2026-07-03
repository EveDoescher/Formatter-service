package com.abntbuilder.formatter.rendering.component.appendix;

import com.abntbuilder.formatter.document.component.appendix.AppendixComponent;
import com.abntbuilder.formatter.document.component.appendix.AppendixItem;
import com.abntbuilder.formatter.document.component.bodycontent.BodyContentComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.appendix.AppendixComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.ComponentRenderResult;
import com.abntbuilder.formatter.rendering.component.Phase0ConsumingRenderer;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyContentRenderer;
import com.abntbuilder.formatter.rendering.phase0.Phase0Index;

import java.util.ArrayList;
import java.util.List;

public final class AppendixRenderer
        implements Phase0ConsumingRenderer<AppendixComponent, ComponentRenderResult> {

    public static final String COMPONENT_ID = "appendix";

    @Override
    public String componentId() { return COMPONENT_ID; }

    @Override
    public Class<AppendixComponent> componentType() { return AppendixComponent.class; }

    @Override
    public ComponentRenderResult renderWithPhase0(
            AppendixComponent component, DocumentProfile profile, Phase0Index phase0Index) {
        AppendixComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(COMPONENT_ID, AppendixComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        BodyContentRenderer contentRenderer = new BodyContentRenderer();
        char letter = 'A';
        for (AppendixItem item : component.items()) {
            String heading = rule.headingTemplate()
                    .replace("{letter}", String.valueOf(letter))
                    .replace("{title}", item.title());
            blocks.add(new DocxParagraph(List.of(DocxRun.of(heading, headingStyle)), headingStyle));

            if (!item.sections().isEmpty()) {
                BodyContentComponent appendixContent = new BodyContentComponent(item.sections());
                blocks.addAll(contentRenderer.renderWithPhase0(appendixContent, profile, phase0Index).blocks());
            }
            letter++;
        }
        return () -> List.copyOf(blocks);
    }
}
