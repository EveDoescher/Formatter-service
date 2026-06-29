package com.abntbuilder.formatter.rendering.component.epigraph;

import com.abntbuilder.formatter.document.component.epigraph.EpigraphComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.epigraph.EpigraphComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;

import java.util.ArrayList;
import java.util.List;

public final class EpigraphRenderer implements ComponentRenderer<EpigraphComponent> {

    public static final String COMPONENT_ID = "epigraph";

    @Override
    public String componentId() { return COMPONENT_ID; }

    @Override
    public Class<EpigraphComponent> componentType() { return EpigraphComponent.class; }

    @Override
    public List<DocxBlock> render(EpigraphComponent component, DocumentProfile profile) {
        EpigraphComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(COMPONENT_ID, EpigraphComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule textStyle = styleResolver.resolve(rule.textStyleId());
        StyleRule authorStyle = styleResolver.resolve(rule.authorStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        blocks.add(new DocxParagraph(List.of(DocxRun.of(component.text(), textStyle)), textStyle));

        String authorText = component.source()
                .map(src -> rule.authorTemplate()
                        .replace("{author}", component.author())
                        .replace("{source}", src))
                .orElse(rule.authorTemplate()
                        .replace("{author}", component.author())
                        .replace(", {source}", "")
                        .replace("{source}", ""));
        blocks.add(new DocxParagraph(List.of(DocxRun.of(authorText, authorStyle)), authorStyle));

        return List.copyOf(blocks);
    }
}
