package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.output.docx.api.DocxBlankLine;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class SinglePageLayoutRenderer {

    public List<DocxBlock> render(SinglePageLayoutPlan plan) {
        Objects.requireNonNull(plan, "plan must not be null");

        List<DocxBlock> blocks = new ArrayList<>();

        for (SinglePageLayoutElement element : plan.elements()) {
            switch (element) {
                case SinglePageTextLines textLines -> addTextLines(blocks, textLines);
                case SinglePageSpacerLines spacerLines -> addSpacerLines(blocks, spacerLines);
            }
        }

        return List.copyOf(blocks);
    }

    private static void addTextLines(
            List<DocxBlock> blocks,
            SinglePageTextLines textLines
    ) {
        for (String line : textLines.lines()) {
            blocks.add(new DocxParagraph(
                    line,
                    textLines.styleRule(),
                    Optional.empty(),
                    Optional.of(textLines.exactLineHeightPt()),
                    Optional.of(textLines.layoutOverride())
            ));
        }
    }

    private static void addSpacerLines(
            List<DocxBlock> blocks,
            SinglePageSpacerLines spacerLines
    ) {
        for (int index = 0; index < spacerLines.lineCount(); index++) {
            blocks.add(new DocxBlankLine(
                    spacerLines.styleRule(),
                    Optional.of(spacerLines.exactLineHeightPt())
            ));
        }
    }
}
