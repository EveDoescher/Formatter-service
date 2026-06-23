package com.abntbuilder.formatter.rendering.component.resumo;

import com.abntbuilder.formatter.document.component.resumo.ResumoComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.resumo.ResumoComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;

import java.util.ArrayList;
import java.util.List;

public final class ResumoRenderer implements ComponentRenderer<ResumoComponent> {

    public static final String COMPONENT_ID = "resumo";

    @Override
    public String componentId() { return COMPONENT_ID; }

    @Override
    public Class<ResumoComponent> componentType() { return ResumoComponent.class; }

    @Override
    public List<DocxBlock> render(ResumoComponent component, DocumentProfile profile) {
        ResumoComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(COMPONENT_ID, ResumoComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());
        StyleRule textStyle = styleResolver.resolve(rule.textStyleId());
        StyleRule keywordsStyle = styleResolver.resolve(rule.keywordsStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        blocks.add(new DocxParagraph(List.of(DocxRun.of(rule.headingText(), headingStyle)), headingStyle));
        blocks.add(new DocxParagraph(List.of(DocxRun.of(component.text(), textStyle)), textStyle));

        String keywordsText = rule.keywordsLabel() + " " +
                String.join(rule.keywordsSeparator(), component.keywords());
        blocks.add(new DocxParagraph(List.of(DocxRun.of(keywordsText, keywordsStyle)), keywordsStyle));

        return List.copyOf(blocks);
    }
}
