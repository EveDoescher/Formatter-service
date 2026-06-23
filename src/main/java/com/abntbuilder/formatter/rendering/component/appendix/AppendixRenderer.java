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
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyContentRenderer;

import java.util.ArrayList;
import java.util.List;

public final class AppendixRenderer implements ComponentRenderer<AppendixComponent> {

    public static final String COMPONENT_ID = "appendix";

    @Override
    public String componentId() { return COMPONENT_ID; }

    @Override
    public Class<AppendixComponent> componentType() { return AppendixComponent.class; }

    @Override
    public List<DocxBlock> render(AppendixComponent component, DocumentProfile profile) {
        AppendixComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(COMPONENT_ID, AppendixComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        char letter = 'A';
        for (AppendixItem item : component.items()) {
            String heading = rule.headingTemplate()
                    .replace("{letter}", String.valueOf(letter))
                    .replace("{title}", item.title());
            blocks.add(new DocxParagraph(List.of(DocxRun.of(heading, headingStyle)), headingStyle));

            if (!item.sections().isEmpty()) {
                BodyContentComponent appendixContent = new BodyContentComponent(item.sections());
                BodyContentRenderer contentRenderer = new BodyContentRenderer();
                blocks.addAll(contentRenderer.render(appendixContent, profile));
            }
            letter++;
        }
        return List.copyOf(blocks);
    }
}
