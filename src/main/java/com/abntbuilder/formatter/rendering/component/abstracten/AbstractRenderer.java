package com.abntbuilder.formatter.rendering.component.abstracten;

import com.abntbuilder.formatter.document.component.abstracten.AbstractComponent;
import com.abntbuilder.formatter.document.component.abstracten.AbstractEntry;
import com.abntbuilder.formatter.document.component.bodycontent.InlineFormatting;
import com.abntbuilder.formatter.output.docx.api.DocxBlankLine;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxPageBreak;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.abstracten.AbstractComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class AbstractRenderer implements ComponentRenderer<AbstractComponent> {

    public static final String COMPONENT_ID = "abstract";

    @Override
    public String componentId() { return COMPONENT_ID; }

    @Override
    public Class<AbstractComponent> componentType() { return AbstractComponent.class; }

    @Override
    public List<DocxBlock> render(AbstractComponent component, DocumentProfile profile) {
        AbstractComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(COMPONENT_ID, AbstractComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());
        StyleRule textStyle = styleResolver.resolve(rule.textStyleId());
        StyleRule keywordsStyle = styleResolver.resolve(rule.keywordsStyleId());

        List<DocxBlock> blocks = new ArrayList<>();

        boolean first = true;
        for (AbstractEntry entry : component.entries()) {
            if (!first) {
                blocks.add(new DocxPageBreak());
            }
            first = false;

            blocks.add(new DocxParagraph(List.of(DocxRun.of(entry.headingText(), headingStyle)), headingStyle));
            for (int i = 0; i < rule.blankLinesAfterHeading(); i++) {
                blocks.add(new DocxBlankLine(headingStyle));
            }
            blocks.add(new DocxParagraph(List.of(DocxRun.of(entry.text(), textStyle)), textStyle));

            InlineFormatting boldFormatting = new InlineFormatting(
                    Optional.of(true), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
            DocxRun labelRun = new DocxRun(entry.keywordsLabel(), keywordsStyle, boldFormatting);
            String keywordsBody = " " + String.join(rule.keywordsSeparator(), entry.keywords()) + rule.keywordsTerminator();
            DocxRun keywordsRun = DocxRun.of(keywordsBody, keywordsStyle);
            blocks.add(new DocxParagraph(List.of(labelRun, keywordsRun), keywordsStyle));
        }

        return List.copyOf(blocks);
    }
}
