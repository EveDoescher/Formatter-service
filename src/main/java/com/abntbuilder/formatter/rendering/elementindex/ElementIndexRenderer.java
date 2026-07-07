package com.abntbuilder.formatter.rendering.elementindex;

import com.abntbuilder.formatter.engine.model.content.elementindex.ElementIndexContent;
import com.abntbuilder.formatter.engine.model.output.DocxBlankLine;
import com.abntbuilder.formatter.engine.model.output.DocxBlock;
import com.abntbuilder.formatter.engine.model.output.DocxParagraph;
import com.abntbuilder.formatter.engine.model.output.DocxRun;
import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;
import com.abntbuilder.formatter.engine.model.profile.StyleRule;
import com.abntbuilder.formatter.engine.model.profile.component.elementindex.ElementIndexComponentRule;
import com.abntbuilder.formatter.input.profile.ComponentRuleResolver;
import com.abntbuilder.formatter.input.profile.StyleResolver;
import com.abntbuilder.formatter.engine.contract.MetadataConsumingRenderer;
import com.abntbuilder.formatter.rendering.bodycontent.BodyDisplayObjectMetadata;
import com.abntbuilder.formatter.rendering.phase0.Phase0Index;

import java.util.ArrayList;
import java.util.List;

public final class ElementIndexRenderer implements MetadataConsumingRenderer<ElementIndexContent> {

    private final String componentId;

    public ElementIndexRenderer(String componentId) {
        if (componentId == null || componentId.isBlank())
            throw new IllegalArgumentException("ElementIndexRenderer.componentId must not be blank.");
        this.componentId = componentId;
    }

    @Override
    public String componentId() { return componentId; }

    @Override
    public Class<ElementIndexContent> componentType() { return ElementIndexContent.class; }

    @Override
    public List<DocxBlock> renderWithMetadata(
            ElementIndexContent component, DocumentProfile profile, Phase0Index phase0Index) {
        ElementIndexComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(componentId, ElementIndexComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());
        StyleRule entryStyle = styleResolver.resolve(rule.entryStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        blocks.add(new DocxParagraph(
                List.of(DocxRun.of(rule.headingText(), headingStyle)), headingStyle));
        for (int i = 0; i < rule.blankLinesAfterHeading(); i++) {
            blocks.add(new DocxBlankLine(headingStyle));
        }

        for (BodyDisplayObjectMetadata item : resolveCollection(rule, phase0Index)) {
            String text = rule.entryTemplate()
                    .replace("{number}", item.number())
                    .replace("{caption}", item.caption());
            blocks.add(new DocxParagraph(List.of(DocxRun.of(text, entryStyle)), entryStyle));
        }

        return List.copyOf(blocks);
    }

    private List<BodyDisplayObjectMetadata> resolveCollection(
            ElementIndexComponentRule rule, Phase0Index phase0Index) {
        return new ArrayList<>(phase0Index.elements(rule.elementType()).values());
    }
}
