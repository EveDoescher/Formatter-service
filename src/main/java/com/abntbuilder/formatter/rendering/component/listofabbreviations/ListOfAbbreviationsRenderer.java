package com.abntbuilder.formatter.rendering.component.listofabbreviations;

import com.abntbuilder.formatter.document.component.listofabbreviations.ListOfAbbreviationsComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlankLine;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.listofabbreviations.ListOfAbbreviationsComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.MetadataConsumingRenderer;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyAbbreviationMetadata;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyContentMetadata;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ListOfAbbreviationsRenderer
        implements MetadataConsumingRenderer<ListOfAbbreviationsComponent> {

    public static final String COMPONENT_ID = "listOfAbbreviations";

    @Override public String componentId() { return COMPONENT_ID; }
    @Override public Class<ListOfAbbreviationsComponent> componentType() {
        return ListOfAbbreviationsComponent.class;
    }

    @Override
    public List<DocxBlock> renderWithMetadata(
            ListOfAbbreviationsComponent component, DocumentProfile profile, BodyContentMetadata metadata) {
        ListOfAbbreviationsComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(COMPONENT_ID, ListOfAbbreviationsComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());
        StyleRule entryStyle = styleResolver.resolve(rule.entryStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        blocks.add(new DocxParagraph(List.of(DocxRun.of(rule.headingText(), headingStyle)), headingStyle));
        for (int i = 0; i < rule.blankLinesAfterHeading(); i++) {
            blocks.add(new DocxBlankLine(headingStyle));
        }

        List<BodyAbbreviationMetadata> abbreviations = new ArrayList<>(metadata.abbreviations());
        if (rule.sortAlphabetically()) {
            abbreviations.sort(Comparator.comparing(BodyAbbreviationMetadata::abbreviation));
        }

        for (BodyAbbreviationMetadata abbr : abbreviations) {
            String text = abbr.abbreviation() + rule.termSeparator() + abbr.expansion();
            blocks.add(new DocxParagraph(List.of(DocxRun.of(text, entryStyle)), entryStyle));
        }

        return List.copyOf(blocks);
    }
}
