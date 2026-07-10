package com.abntbuilder.formatter.rendering.bodycontent;

import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyBlock;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyLongQuote;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyParagraph;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodySection;
import com.abntbuilder.formatter.engine.model.output.DocxBlankLine;
import com.abntbuilder.formatter.engine.model.output.DocxBlock;
import com.abntbuilder.formatter.engine.model.output.DocxPageBreak;
import com.abntbuilder.formatter.engine.model.output.DocxParagraph;
import com.abntbuilder.formatter.engine.model.output.DocxRun;
import com.abntbuilder.formatter.engine.model.profile.StyleRule;
import com.abntbuilder.formatter.rendering.bodycontent.BodySectionMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
                boolean keepWithNext = ctx.rule.layout().keepWithNextOnHeadings();
                blocks.add(new DocxParagraph(
                        List.of(DocxRun.of(renderedTitle, titleStyle)),
                        titleStyle,
                        Optional.empty(), Optional.empty(), Optional.empty(),
                        keepWithNext, false
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
