package com.abntbuilder.formatter.rendering.component.annex;

import com.abntbuilder.formatter.document.component.annex.AnnexComponent;
import com.abntbuilder.formatter.document.component.annex.AnnexItem;
import com.abntbuilder.formatter.document.component.bodycontent.BodyContentComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.annex.AnnexComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyContentRenderer;

import java.util.ArrayList;
import java.util.List;

public final class AnnexRenderer implements ComponentRenderer<AnnexComponent> {

    public static final String COMPONENT_ID = "annex";

    @Override
    public String componentId() { return COMPONENT_ID; }

    @Override
    public Class<AnnexComponent> componentType() { return AnnexComponent.class; }

    @Override
    public List<DocxBlock> render(AnnexComponent component, DocumentProfile profile) {
        AnnexComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(COMPONENT_ID, AnnexComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        char letter = 'A';
        for (AnnexItem item : component.items()) {
            String heading = rule.headingTemplate()
                    .replace("{letter}", String.valueOf(letter))
                    .replace("{title}", item.title());
            blocks.add(new DocxParagraph(List.of(DocxRun.of(heading, headingStyle)), headingStyle));

            if (!item.sections().isEmpty()) {
                BodyContentComponent annexContent = new BodyContentComponent(item.sections());
                BodyContentRenderer contentRenderer = new BodyContentRenderer();
                blocks.addAll(contentRenderer.render(annexContent, profile));
            }
            letter++;
        }
        return List.copyOf(blocks);
    }
}
