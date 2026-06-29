package com.abntbuilder.formatter.rendering.component.acknowledgments;

import com.abntbuilder.formatter.document.component.acknowledgments.AcknowledgmentsComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlankLine;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.acknowledgments.AcknowledgmentsComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;

import java.util.ArrayList;
import java.util.List;

public final class AcknowledgmentsRenderer implements ComponentRenderer<AcknowledgmentsComponent> {

    public static final String COMPONENT_ID = "acknowledgments";

    @Override
    public String componentId() { return COMPONENT_ID; }

    @Override
    public Class<AcknowledgmentsComponent> componentType() { return AcknowledgmentsComponent.class; }

    @Override
    public List<DocxBlock> render(AcknowledgmentsComponent component, DocumentProfile profile) {
        AcknowledgmentsComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(COMPONENT_ID, AcknowledgmentsComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());
        StyleRule textStyle = styleResolver.resolve(rule.textStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        blocks.add(new DocxParagraph(List.of(DocxRun.of(rule.headingText(), headingStyle)), headingStyle));
        for (int i = 0; i < rule.blankLinesAfterHeading(); i++) {
            blocks.add(new DocxBlankLine(headingStyle));
        }
        blocks.add(new DocxParagraph(List.of(DocxRun.of(component.text(), textStyle)), textStyle));
        return List.copyOf(blocks);
    }
}
