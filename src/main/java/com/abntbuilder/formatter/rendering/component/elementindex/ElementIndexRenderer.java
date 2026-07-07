package com.abntbuilder.formatter.rendering.component.elementindex;

import com.abntbuilder.formatter.document.component.elementindex.ElementIndexContent;
import com.abntbuilder.formatter.output.docx.api.DocxBlankLine;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.elementindex.ElementIndexComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.MetadataConsumingRenderer;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyDisplayObjectMetadata;
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
                    .replace("{number}", String.valueOf(item.number()))
                    .replace("{caption}", item.caption());
            blocks.add(new DocxParagraph(List.of(DocxRun.of(text, entryStyle)), entryStyle));
        }

        return List.copyOf(blocks);
    }

    private List<BodyDisplayObjectMetadata> resolveCollection(
            ElementIndexComponentRule rule, Phase0Index phase0Index) {
        return switch (rule.elementType()) {
            case FIGURE -> new ArrayList<>(phase0Index.figures().values());
            case TABLE -> new ArrayList<>(phase0Index.tables().values());
            case FRAME -> new ArrayList<>(phase0Index.frames().values());
            case CHART -> new ArrayList<>(phase0Index.charts().values());
            case CODE_LISTING -> new ArrayList<>(phase0Index.codeListings().values());
        };
    }
}
