package com.abntbuilder.formatter.rendering.references;

import com.abntbuilder.formatter.engine.model.content.bodycontent.InlineFormatting;
import com.abntbuilder.formatter.engine.model.content.references.ReferenceEntry;
import com.abntbuilder.formatter.engine.model.content.references.ReferencesComponent;
import com.abntbuilder.formatter.engine.model.output.DocxBlankLine;
import com.abntbuilder.formatter.engine.model.output.DocxBlock;
import com.abntbuilder.formatter.engine.model.output.DocxParagraph;
import com.abntbuilder.formatter.engine.model.output.DocxRun;
import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;
import com.abntbuilder.formatter.engine.model.profile.StyleRule;
import com.abntbuilder.formatter.engine.model.profile.component.references.ReferencesComponentRule;
import com.abntbuilder.formatter.input.profile.ComponentRuleResolver;
import com.abntbuilder.formatter.input.profile.StyleResolver;
import com.abntbuilder.formatter.engine.contract.ComponentRenderer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class ReferencesRenderer implements ComponentRenderer<ReferencesComponent> {

    private final String componentId;

    public ReferencesRenderer(String componentId) {
        if (componentId == null || componentId.isBlank())
            throw new IllegalArgumentException("ReferencesRenderer.componentId must not be blank.");
        this.componentId = componentId;
    }

    @Override
    public String componentId() { return componentId; }

    @Override
    public Class<ReferencesComponent> componentType() { return ReferencesComponent.class; }

    @Override
    public List<DocxBlock> render(ReferencesComponent component, DocumentProfile profile) {
        ReferencesComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(componentId, ReferencesComponentRule.class);
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

        List<ReferenceEntry> orderedEntries = sortEntries(component.entries(), rule);
        boolean first = true;
        for (ReferenceEntry entry : orderedEntries) {
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

    private static List<ReferenceEntry> sortEntries(
            List<ReferenceEntry> entries, ReferencesComponentRule rule) {
        return switch (rule.sortOrder()) {
            case AS_GIVEN -> entries;
            case ALPHABETICAL -> entries.stream()
                    .sorted(Comparator.comparing(e -> {
                        String surname = e.authors().isEmpty() ? e.title()
                                : e.authors().get(0).surname();
                        return rule.formattingRule().authorFormat().surnameUppercase()
                                ? surname.toUpperCase() : surname;
                    }))
                    .toList();
            case CITATION_ORDER -> entries;
        };
    }
}
