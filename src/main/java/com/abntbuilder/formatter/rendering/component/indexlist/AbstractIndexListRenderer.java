package com.abntbuilder.formatter.rendering.component.indexlist;

import com.abntbuilder.formatter.document.component.DocumentComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlankLine;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.indexlist.IndexListComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.MetadataConsumingRenderer;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyDisplayObjectMetadata;
import com.abntbuilder.formatter.rendering.phase0.Phase0Index;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractIndexListRenderer<T extends DocumentComponent>
        implements MetadataConsumingRenderer<T> {

    protected abstract Function<Phase0Index, List<BodyDisplayObjectMetadata>> metadataExtractor();

    @Override
    public List<DocxBlock> renderWithMetadata(
            T component, DocumentProfile profile, Phase0Index phase0Index) {
        IndexListComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(componentId(), IndexListComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());
        StyleRule entryStyle = styleResolver.resolve(rule.entryStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        blocks.add(new DocxParagraph(
                List.of(DocxRun.of(rule.headingText(), headingStyle)), headingStyle));
        for (int i = 0; i < rule.blankLinesAfterHeading(); i++) {
            blocks.add(new DocxBlankLine(headingStyle));
        }

        for (BodyDisplayObjectMetadata item : metadataExtractor().apply(phase0Index)) {
            String text = rule.entryTemplate()
                    .replace("{number}", String.valueOf(item.number()))
                    .replace("{caption}", item.caption());
            blocks.add(new DocxParagraph(List.of(DocxRun.of(text, entryStyle)), entryStyle));
        }

        return List.copyOf(blocks);
    }
}
