package com.abntbuilder.formatter.rendering.component.cover;

import com.abntbuilder.formatter.document.component.cover.CoverComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlankLine;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.rendering.component.cover.layout.CoverLayoutCalculator;
import com.abntbuilder.formatter.rendering.component.cover.layout.CoverLayoutElement;
import com.abntbuilder.formatter.rendering.component.cover.layout.CoverLayoutPlan;
import com.abntbuilder.formatter.rendering.component.cover.layout.CoverSpacerLines;
import com.abntbuilder.formatter.rendering.component.cover.layout.CoverTextLines;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class CoverRenderer {

    private final CoverLayoutCalculator layoutCalculator;

    public CoverRenderer() {
        this(new CoverLayoutCalculator());
    }

    public CoverRenderer(CoverLayoutCalculator layoutCalculator) {
        this.layoutCalculator = Objects.requireNonNull(layoutCalculator, "layoutCalculator must not be null");
    }

    public List<DocxBlock> render(CoverComponent cover, DocumentProfile profile) {
        Objects.requireNonNull(cover, "cover must not be null");
        Objects.requireNonNull(profile, "profile must not be null");

        return renderPlan(layoutCalculator.calculate(cover, profile));
    }

    private static List<DocxBlock> renderPlan(CoverLayoutPlan plan) {
        List<DocxBlock> blocks = new ArrayList<>();

        for (CoverLayoutElement element : plan.elements()) {
            switch (element) {
                case CoverTextLines textLines -> addTextLines(blocks, textLines, plan);
                case CoverSpacerLines spacerLines -> addSpacerLines(blocks, spacerLines, plan);
            }
        }

        return List.copyOf(blocks);
    }

    private static void addTextLines(
            List<DocxBlock> blocks,
            CoverTextLines textLines,
            CoverLayoutPlan plan
    ) {
        for (String line : textLines.lines()) {
            blocks.add(new DocxParagraph(
                    line,
                    textLines.styleRule(),
                    Optional.empty(),
                    Optional.of(plan.exactLineHeightPt())
            ));
        }
    }

    private static void addSpacerLines(
            List<DocxBlock> blocks,
            CoverSpacerLines spacerLines,
            CoverLayoutPlan plan
    ) {
        for (int index = 0; index < spacerLines.lineCount(); index++) {
            blocks.add(new DocxBlankLine(
                    spacerLines.styleRule(),
                    Optional.of(plan.exactLineHeightPt())
            ));
        }
    }
}
