package com.abntbuilder.formatter.rendering.component.indexlist;

import com.abntbuilder.formatter.document.component.DocumentComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.indexlist.IndexListComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.MetadataConsumingRenderer;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyContentMetadata;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyDisplayObjectMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractIndexListRenderer<T extends DocumentComponent>
        implements MetadataConsumingRenderer<T> {

    protected abstract Function<BodyContentMetadata, List<BodyDisplayObjectMetadata>> metadataExtractor();

    @Override
    public List<DocxBlock> renderWithMetadata(
            T component, DocumentProfile profile, BodyContentMetadata metadata) {
        IndexListComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(componentId(), IndexListComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());
        StyleRule entryStyle = styleResolver.resolve(rule.entryStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        blocks.add(new DocxParagraph(
                List.of(DocxRun.of(rule.headingText(), headingStyle)), headingStyle));

        for (BodyDisplayObjectMetadata item : metadataExtractor().apply(metadata)) {
            String text = rule.entryTemplate()
                    .replace("{number}", String.valueOf(item.number()))
                    .replace("{caption}", item.caption());
            blocks.add(new DocxParagraph(List.of(DocxRun.of(text, entryStyle)), entryStyle));
        }

        return List.copyOf(blocks);
    }
}
