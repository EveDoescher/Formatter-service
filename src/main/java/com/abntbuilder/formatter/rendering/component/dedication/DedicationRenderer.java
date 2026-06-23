package com.abntbuilder.formatter.rendering.component.dedication;

import com.abntbuilder.formatter.document.component.dedication.DedicationComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlankLine;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxPageBreak;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.dedication.DedicationComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;

import java.util.ArrayList;
import java.util.List;

public final class DedicationRenderer implements ComponentRenderer<DedicationComponent> {

    public static final String COMPONENT_ID = "dedication";

    @Override
    public String componentId() { return COMPONENT_ID; }

    @Override
    public Class<DedicationComponent> componentType() { return DedicationComponent.class; }

    @Override
    public List<DocxBlock> render(DedicationComponent component, DocumentProfile profile) {
        DedicationComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(COMPONENT_ID, DedicationComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule textStyle = styleResolver.resolve(rule.textStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        blocks.add(new DocxPageBreak());
        for (int i = 0; i < rule.blankLinesBefore(); i++) {
            blocks.add(new DocxBlankLine(textStyle));
        }
        blocks.add(new DocxParagraph(List.of(DocxRun.of(component.text(), textStyle)), textStyle));
        return List.copyOf(blocks);
    }
}
