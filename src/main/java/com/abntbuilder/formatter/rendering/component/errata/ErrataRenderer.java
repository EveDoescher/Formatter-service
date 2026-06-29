package com.abntbuilder.formatter.rendering.component.errata;

import com.abntbuilder.formatter.document.component.errata.ErrataComponent;
import com.abntbuilder.formatter.document.component.errata.ErrataEntry;
import com.abntbuilder.formatter.output.docx.api.DocxBlankLine;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.output.docx.api.DocxTableBlock;
import com.abntbuilder.formatter.output.docx.api.DocxTableCell;
import com.abntbuilder.formatter.output.docx.api.TableBorderStyle;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.profile.model.component.errata.ErrataComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class ErrataRenderer implements ComponentRenderer<ErrataComponent> {

    public static final String COMPONENT_ID = "errata";

    @Override
    public String componentId() { return COMPONENT_ID; }

    @Override
    public Class<ErrataComponent> componentType() { return ErrataComponent.class; }

    @Override
    public List<DocxBlock> render(ErrataComponent component, DocumentProfile profile) {
        ErrataComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(COMPONENT_ID, ErrataComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());
        StyleRule headerStyle = styleResolver.resolve(rule.tableHeaderStyleId());
        StyleRule cellStyle = styleResolver.resolve(rule.tableCellStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        blocks.add(new DocxParagraph(
                List.of(DocxRun.of(rule.headingText(), headingStyle)),
                headingStyle
        ));
        for (int i = 0; i < rule.blankLinesAfterHeading(); i++) {
            blocks.add(new DocxBlankLine(headingStyle));
        }

        List<List<DocxTableCell>> rows = new ArrayList<>();
        for (ErrataEntry entry : component.entries()) {
            rows.add(List.of(
                    new DocxTableCell(entry.page()),
                    new DocxTableCell(entry.line()),
                    new DocxTableCell(entry.incorrectText()),
                    new DocxTableCell(entry.correctText())
            ));
        }

        blocks.add(new DocxTableBlock(
                rule.tableHeaders(),
                rows,
                headerStyle,
                cellStyle,
                BigDecimal.valueOf(100),
                TextAlignment.CENTER,
                false,
                false,
                false,
                TableBorderStyle.CLOSED
        ));

        return List.copyOf(blocks);
    }
}
