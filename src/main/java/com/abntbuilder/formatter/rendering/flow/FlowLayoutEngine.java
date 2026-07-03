package com.abntbuilder.formatter.rendering.flow;

import com.abntbuilder.formatter.document.component.bodycontent.BodyBlock;
import com.abntbuilder.formatter.document.component.bodycontent.BodyLongQuote;
import com.abntbuilder.formatter.document.component.bodycontent.BodyParagraph;
import com.abntbuilder.formatter.document.component.bodycontent.BodySection;
import com.abntbuilder.formatter.output.docx.api.DocxBlankLine;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxPageBreak;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodySectionMetadata;

import java.util.ArrayList;
import java.util.List;

public final class FlowLayoutEngine {

    public List<DocxBlock> render(List<BodySection> sections, FlowRenderingContext ctx) {
        List<DocxBlock> blocks = new ArrayList<>();
        boolean previousBlockWasTextualContent = false;

        for (BodySection section : sections) {
            if (section.title().isPresent()) {
                if (ctx.rule.layout().pageBreakBeforePrimarySection()
                        && section.level() == 1
                        && !blocks.isEmpty()) {
                    blocks.add(new DocxPageBreak());
                } else if (previousBlockWasTextualContent) {
                    addBlankLines(
                            blocks,
                            ctx.blankLineStyle,
                            ctx.rule.layout().blankLinesBeforeSectionTitleWhenPrecededByContent()
                    );
                }

                StyleRule titleStyle = ctx.styleResolver.resolve(
                        ctx.rule.styleMapping().sectionTitleStyleIdForLevel(section.level())
                );
                String renderedTitle = ctx.sectionNumberingState.resolveTitle(
                        section.level(), section.title().orElseThrow()
                );
                String renderedNumber = ctx.sectionNumberingState.resolveNumber(section.level());
                blocks.add(new DocxParagraph(
                        List.of(DocxRun.of(renderedTitle, titleStyle)),
                        titleStyle
                ));
                ctx.sectionMetas.add(new BodySectionMetadata(
                        section.id(), section.level(), renderedTitle, renderedNumber
                ));
                previousBlockWasTextualContent = false;

                addBlankLines(blocks, ctx.blankLineStyle, ctx.rule.layout().blankLinesAfterSectionTitle());
            }

            for (int i = 0; i < section.blocks().size(); i++) {
                BodyBlock block = section.blocks().get(i);
                try {
                    blocks.addAll(TextTypeRegistry.dispatch(block, ctx));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(
                            "Error in section '" + section.id() + "', block[" + i + "] ("
                                    + block.getClass().getSimpleName() + "): " + e.getMessage(), e
                    );
                }
                previousBlockWasTextualContent = block instanceof BodyParagraph
                        || block instanceof BodyLongQuote;
            }
        }

        return List.copyOf(blocks);
    }

    private static void addBlankLines(List<DocxBlock> blocks, StyleRule styleRule, int count) {
        for (int i = 0; i < count; i++) {
            blocks.add(new DocxBlankLine(styleRule));
        }
    }
}
