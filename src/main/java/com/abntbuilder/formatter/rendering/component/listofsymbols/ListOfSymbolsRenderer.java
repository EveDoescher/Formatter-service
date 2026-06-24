package com.abntbuilder.formatter.rendering.component.listofsymbols;

import com.abntbuilder.formatter.document.component.listofsymbols.ListOfSymbolsComponent;
import com.abntbuilder.formatter.document.component.listofsymbols.SymbolEntry;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.listofsymbols.ListOfSymbolsComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;

import java.util.ArrayList;
import java.util.List;

public final class ListOfSymbolsRenderer implements ComponentRenderer<ListOfSymbolsComponent> {

    public static final String COMPONENT_ID = "listOfSymbols";

    @Override public String componentId() { return COMPONENT_ID; }
    @Override public Class<ListOfSymbolsComponent> componentType() { return ListOfSymbolsComponent.class; }

    @Override
    public List<DocxBlock> render(ListOfSymbolsComponent component, DocumentProfile profile) {
        ListOfSymbolsComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(COMPONENT_ID, ListOfSymbolsComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());
        StyleRule entryStyle = styleResolver.resolve(rule.entryStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        blocks.add(new DocxParagraph(List.of(DocxRun.of(rule.headingText(), headingStyle)), headingStyle));
        for (SymbolEntry entry : component.entries()) {
            String text = entry.symbol() + rule.termSeparator() + entry.meaning();
            blocks.add(new DocxParagraph(List.of(DocxRun.of(text, entryStyle)), entryStyle));
        }
        return List.copyOf(blocks);
    }
}
