package com.abntbuilder.formatter.rendering.component.references;

import com.abntbuilder.formatter.document.component.bodycontent.InlineFormatting;
import com.abntbuilder.formatter.document.component.references.ReferenceEntry;
import com.abntbuilder.formatter.document.component.references.ReferencesComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlankLine;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.references.ReferencesComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ReferencesRenderer implements ComponentRenderer<ReferencesComponent> {

    public static final String COMPONENT_ID = "references";

    @Override
    public String componentId() { return COMPONENT_ID; }

    @Override
    public Class<ReferencesComponent> componentType() { return ReferencesComponent.class; }

    @Override
    public List<DocxBlock> render(ReferencesComponent component, DocumentProfile profile) {
        ReferencesComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(COMPONENT_ID, ReferencesComponentRule.class);
        ReferencesEntryFormatter formatter = new ReferencesEntryFormatter(rule.formattingRule());
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());
        StyleRule entryStyle = styleResolver.resolve(rule.entryStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        blocks.add(new DocxParagraph(
                List.of(DocxRun.of(rule.headingText(), headingStyle)), headingStyle
        ));
        for (int i = 0; i < rule.blankLinesAfterHeading(); i++) {
            blocks.add(new DocxBlankLine(headingStyle));
        }

        boolean first = true;
        for (ReferenceEntry entry : component.entries()) {
            if (!first && rule.blankLinesBetweenEntries() > 0) {
                for (int i = 0; i < rule.blankLinesBetweenEntries(); i++) {
                    blocks.add(new DocxBlankLine(entryStyle));
                }
            }
            List<ReferenceSegment> segments = formatter.format(entry);
            List<DocxRun> runs = segments.stream()
                    .map(seg -> {
                        InlineFormatting fmt = seg.bold()
                                ? new InlineFormatting(Optional.of(true), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())
                                : InlineFormatting.none();
                        return new DocxRun(seg.text(), entryStyle, fmt);
                    })
                    .toList();
            blocks.add(new DocxParagraph(runs, entryStyle));
            first = false;
        }

        return List.copyOf(blocks);
    }
}
