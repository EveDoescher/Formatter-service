package com.abntbuilder.formatter.rendering.sectioned;

import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyContentComponent;
import com.abntbuilder.formatter.engine.model.content.sectioned.SectionedContent;
import com.abntbuilder.formatter.engine.model.content.sectioned.SectionedItem;
import com.abntbuilder.formatter.engine.model.output.DocxBlock;
import com.abntbuilder.formatter.engine.model.output.DocxParagraph;
import com.abntbuilder.formatter.engine.model.output.DocxRun;
import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;
import com.abntbuilder.formatter.engine.model.profile.StyleRule;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.BodyContentComponentRule;
import com.abntbuilder.formatter.engine.model.profile.component.sectioned.SectionedComponentRule;
import com.abntbuilder.formatter.engine.model.profile.component.sectioned.SectionedComponentRule.IndexingStyle;
import com.abntbuilder.formatter.input.profile.ComponentRuleResolver;
import com.abntbuilder.formatter.input.profile.StyleResolver;
import com.abntbuilder.formatter.engine.contract.ComponentRenderResult;
import com.abntbuilder.formatter.engine.contract.Phase0ConsumingRenderer;
import com.abntbuilder.formatter.rendering.bodycontent.BodyContentRenderer;
import com.abntbuilder.formatter.rendering.phase0.Phase0Index;

import java.util.ArrayList;
import java.util.List;

public final class SectionedRenderer
        implements Phase0ConsumingRenderer<SectionedContent, ComponentRenderResult> {

    private final String componentId;

    public SectionedRenderer(String componentId) {
        if (componentId == null || componentId.isBlank())
            throw new IllegalArgumentException("SectionedRenderer.componentId must not be blank.");
        this.componentId = componentId;
    }

    @Override
    public String componentId() { return componentId; }

    @Override
    public Class<SectionedContent> componentType() { return SectionedContent.class; }

    @Override
    public ComponentRenderResult renderWithPhase0(
            SectionedContent component, DocumentProfile profile, Phase0Index phase0Index) {
        SectionedComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(componentId, SectionedComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());

        String bodyContentId = rule.bodyContentComponentId();
        BodyContentComponentRule bodyContentRule = new ComponentRuleResolver(profile)
                .resolve(bodyContentId, BodyContentComponentRule.class)
                .withSectionTitleStyleIds(rule.sectionTitleStyleIdsByLevel());
        List<DocxBlock> blocks = new ArrayList<>();
        BodyContentRenderer contentRenderer = new BodyContentRenderer(bodyContentId);
        for (int i = 0; i < component.items().size(); i++) {
            SectionedItem item = component.items().get(i);
            String marker = resolveMarker(rule.indexingStyle(), i);
            String heading = rule.headingTemplate()
                    .replace("{letter}", marker)
                    .replace("{number}", marker)
                    .replace("{title}", item.title());
            blocks.add(new DocxParagraph(List.of(DocxRun.of(heading, headingStyle)), headingStyle));

            if (!item.sections().isEmpty()) {
                BodyContentComponent sectionContent = new BodyContentComponent(bodyContentId, item.sections());
                blocks.addAll(contentRenderer.renderWithPhase0(
                        sectionContent, profile, phase0Index, bodyContentRule).blocks());
            }
        }
        return () -> List.copyOf(blocks);
    }

    static String resolveMarker(IndexingStyle style, int index) {
        return switch (style) {
            case ALPHABETIC       -> String.valueOf((char) ('A' + index));
            case ALPHABETIC_LOWER -> String.valueOf((char) ('a' + index));
            case NUMERIC          -> String.valueOf(index + 1);
            case ROMAN_UPPER      -> toRoman(index + 1).toUpperCase();
            case ROMAN_LOWER      -> toRoman(index + 1).toLowerCase();
        };
    }

    private static String toRoman(int n) {
        int[] values = {10, 9, 5, 4, 1};
        String[] symbols = {"x", "ix", "v", "iv", "i"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            while (n >= values[i]) {
                sb.append(symbols[i]);
                n -= values[i];
            }
        }
        return sb.toString();
    }
}
