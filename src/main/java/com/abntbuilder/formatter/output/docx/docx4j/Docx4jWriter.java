package com.abntbuilder.formatter.output.docx.docx4j;

import com.abntbuilder.formatter.output.docx.api.*;
import com.abntbuilder.formatter.profile.model.PageNumberingPlacement;
import com.abntbuilder.formatter.profile.model.PageOrientation;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.StyleType;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.shared.measurement.MeasurementConverter;
import com.abntbuilder.formatter.output.docx.api.DocxBlankLine;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.relationships.Relationship;
import com.abntbuilder.formatter.document.component.bodycontent.BodyListType;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.Lvl;
import org.docx4j.wml.NumFmt;
import org.docx4j.wml.NumberFormat;
import com.abntbuilder.formatter.document.component.bodycontent.InlineFormatting;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.U;
import org.docx4j.wml.UnderlineEnumeration;
import org.docx4j.wml.CTVerticalAlignRun;
import org.docx4j.wml.STVerticalAlignRun;
import org.docx4j.wml.CTPageNumber;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.FooterReference;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.HeaderReference;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.HdrFtrRef;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.R;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STLineSpacingRule;
import org.docx4j.wml.STPageOrientation;
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.docx4j.wml.Text;
import org.docx4j.wml.Br;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.STBrType;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.CTTabStop;
import org.docx4j.wml.STTabJc;
import org.docx4j.wml.STTabTlc;
import org.docx4j.wml.Tabs;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblBorders;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.TcPrInner;
import org.docx4j.wml.Tr;
import org.docx4j.wml.TrPr;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Docx4jWriter implements DocxWriter {

    private final ObjectFactory objectFactory = Context.getWmlObjectFactory();

    @Override
    public byte[] write(DocxDocument document) {
        Objects.requireNonNull(document, "document must not be null");

        try {
            WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage();

            clearDefaultBodyContent(wordPackage);
            applyHeadingStyleDefinitions(wordPackage, document.blocks());
            applyTocStyleDefinitions(wordPackage, document.blocks());

            Optional<DocxPageNumbering> currentSectionPageNumbering = document.initialPageNumbering();

            boolean hasLists = document.blocks().stream()
                    .anyMatch(b -> b instanceof DocxListItemParagraph);
            ListNumIds listNumIds = hasLists ? createListNumIds(wordPackage) : null;

            for (DocxBlock block : document.blocks()) {
                if (block instanceof DocxSectionBreak sectionBreak) {
                    writeSectionBreak(wordPackage, document.pageRule(), currentSectionPageNumbering);
                    currentSectionPageNumbering = Optional.of(sectionBreak.pageNumbering());
                    continue;
                }

                writeBlock(wordPackage, block, listNumIds);
            }

            applyPageRule(wordPackage, document.pageRule(), currentSectionPageNumbering);

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                wordPackage.save(outputStream);
                return outputStream.toByteArray();
            }
        } catch (Exception exception) {
            throw new DocxWriterException("Failed to write DOCX document.", exception);
        }
    }

    private void clearDefaultBodyContent(WordprocessingMLPackage wordPackage) {
        wordPackage
                .getMainDocumentPart()
                .getJaxbElement()
                .getBody()
                .getContent()
                .clear();
    }

    private void applyPageRule(
            WordprocessingMLPackage wordPackage,
            PageRule pageRule,
            Optional<DocxPageNumbering> pageNumbering
    ) throws Exception {
        SectPr sectionProperties = createSectionProperties(wordPackage, pageRule, pageNumbering);

        wordPackage
                .getMainDocumentPart()
                .getJaxbElement()
                .getBody()
                .setSectPr(sectionProperties);
    }

    private SectPr createSectionProperties(
            WordprocessingMLPackage wordPackage,
            PageRule pageRule,
            Optional<DocxPageNumbering> pageNumbering
    ) throws Exception {
        SectPr sectionProperties = objectFactory.createSectPr();

        SectPr.PgSz pageSize = objectFactory.createSectPrPgSz();
        pageSize.setW(BigInteger.valueOf(MeasurementConverter.centimetersToTwips(pageRule.widthCm())));
        pageSize.setH(BigInteger.valueOf(MeasurementConverter.centimetersToTwips(pageRule.heightCm())));
        pageSize.setOrient(mapPageOrientation(pageRule.orientation()));

        SectPr.PgMar pageMargins = objectFactory.createSectPrPgMar();
        pageMargins.setTop(BigInteger.valueOf(MeasurementConverter.centimetersToTwips(pageRule.marginTopCm())));
        pageMargins.setRight(BigInteger.valueOf(MeasurementConverter.centimetersToTwips(pageRule.marginRightCm())));
        pageMargins.setBottom(BigInteger.valueOf(MeasurementConverter.centimetersToTwips(pageRule.marginBottomCm())));
        pageMargins.setLeft(BigInteger.valueOf(MeasurementConverter.centimetersToTwips(pageRule.marginLeftCm())));
        pageMargins.setHeader(BigInteger.ZERO);
        pageMargins.setFooter(BigInteger.ZERO);
        pageMargins.setGutter(BigInteger.ZERO);

        pageNumbering.filter(DocxPageNumbering::visible)
                .ifPresent(numbering -> applyPageNumberingMargins(pageMargins, numbering));

        sectionProperties.setPgSz(pageSize);
        sectionProperties.setPgMar(pageMargins);

        if (pageNumbering.isPresent()) {
            DocxPageNumbering numbering = pageNumbering.orElseThrow();
            if (numbering.countingStarts()) {
                applyPageCountingBoundary(sectionProperties);
            }

            if (numbering.visible()) {
                addPageNumberingReference(wordPackage, sectionProperties, pageRule, numbering);
            }
        }

        return sectionProperties;
    }

    private void writeBlock(WordprocessingMLPackage wordPackage, DocxBlock block, ListNumIds listNumIds) {
        switch (block) {
            case DocxParagraph paragraph -> writeParagraph(wordPackage, paragraph);
            case DocxPageBreak ignored -> writePageBreak(wordPackage);
            case DocxBlankLine blankLine -> writeBlankLine(wordPackage, blankLine);
            case DocxImageBlock imageBlock -> writeImage(wordPackage, imageBlock);
            case DocxTableBlock tableBlock -> writeTable(wordPackage, tableBlock);
            case DocxListItemParagraph listItem -> writeListItem(wordPackage, listItem, listNumIds);
            case com.abntbuilder.formatter.output.docx.api.DocxFootnoteReferenceBlock fnBlock -> writeFootnoteReferenceBlock(wordPackage, fnBlock, listNumIds);
            case DocxTocBlock tocBlock -> writeToc(wordPackage, tocBlock);
            case DocxSectionBreak ignored -> throw new IllegalArgumentException(
                    "Section breaks must be handled by the document section state."
            );
        }
    }

    private void writeParagraph(WordprocessingMLPackage wordPackage, DocxParagraph paragraph) {
        P docxParagraph = objectFactory.createP();
        docxParagraph.setPPr(isHeadingStyle(paragraph.styleRule())
                ? createHeadingParagraphProperties(paragraph.styleRule())
                : createParagraphProperties(
                        paragraph.styleRule(),
                        paragraph.spacingBeforeOverridePt(),
                        paragraph.exactLineHeightPt(),
                        paragraph.layoutOverride()
                ));
        applyKeepOptions(docxParagraph.getPPr(), paragraph.keepWithNext(), paragraph.keepLines());

        for (DocxRun docxRun : paragraph.runs()) {
            String text = docxRun.text();
            if (text.startsWith("[FN:") && text.endsWith("]")) {
                int fnId = Integer.parseInt(text.substring(4, text.length() - 1));
                R refRun = objectFactory.createR();
                org.docx4j.wml.CTFtnEdnRef ref = objectFactory.createCTFtnEdnRef();
                ref.setId(java.math.BigInteger.valueOf(fnId));
                refRun.getContent().add(objectFactory.createRFootnoteReference(ref));
                docxParagraph.getContent().add(refRun);
            } else {
                R run = objectFactory.createR();

                if (!isHeadingStyle(paragraph.styleRule())) {
                    run.setRPr(buildRunProperties(docxRun.baseStyle(), docxRun.formatting()));
                }

                Text t = objectFactory.createText();
                t.setValue(resolveText(text, docxRun.baseStyle()));
                t.setSpace("preserve");
                run.getContent().add(t);
                docxParagraph.getContent().add(run);
            }
        }

        wordPackage.getMainDocumentPart().addObject(docxParagraph);
    }

    private void writeImage(WordprocessingMLPackage wordPackage, DocxImageBlock imageBlock) {
        try {
            BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(
                    wordPackage,
                    imageBlock.bytes()
            );
            org.docx4j.dml.wordprocessingDrawing.Inline inline = imagePart.createImageInline(
                    "image",
                    imageBlock.altText(),
                    Math.toIntExact(Math.floorMod(System.nanoTime(), Integer.MAX_VALUE)),
                    Math.toIntExact(Math.floorMod(System.nanoTime(), Integer.MAX_VALUE)),
                    false
            );
            inline.getExtent().setCx(centimetersToEmu(imageBlock.widthCm()));
            inline.getExtent().setCy(centimetersToEmu(imageBlock.heightCm()));

            Drawing drawing = objectFactory.createDrawing();
            drawing.getAnchorOrInline().add(inline);

            R run = objectFactory.createR();
            run.getContent().add(drawing);

            P paragraph = objectFactory.createP();
            PPr paragraphProperties = objectFactory.createPPr();

            Jc justification = objectFactory.createJc();
            justification.setVal(mapTextAlignment(imageBlock.alignment()));
            paragraphProperties.setJc(justification);
            applyKeepOptions(paragraphProperties, imageBlock.keepWithNext(), imageBlock.keepLines());

            paragraph.setPPr(paragraphProperties);
            paragraph.getContent().add(run);

            wordPackage.getMainDocumentPart().addObject(paragraph);
        } catch (Exception exception) {
            throw new DocxWriterException("Failed to write DOCX image.", exception);
        }
    }

    private void writeTable(WordprocessingMLPackage wordPackage, DocxTableBlock tableBlock) {
        Tbl table = objectFactory.createTbl();
        table.setTblPr(createTableProperties(tableBlock));

        Tr headerRow = createTableRow(
                tableBlock.headers(),
                tableBlock.headerStyleRule(),
                tableBlock.headers().size(),
                tableBlock.repeatHeaderOnPageBreak()
        );
        table.getContent().add(headerRow);

        for (List<com.abntbuilder.formatter.output.docx.api.DocxTableCell> row : tableBlock.rows()) {
            table.getContent().add(createTableRowFromCells(
                    row,
                    tableBlock.cellStyleRule(),
                    tableBlock.headers().size(),
                    false
            ));
        }

        wordPackage.getMainDocumentPart().addObject(table);
    }

    private TblPr createTableProperties(DocxTableBlock tableBlock) {
        TblPr tableProperties = objectFactory.createTblPr();

        TblWidth tableWidth = objectFactory.createTblWidth();
        tableWidth.setType("pct");
        tableWidth.setW(tableWidthPercentValue(tableBlock.widthPercent()));
        tableProperties.setTblW(tableWidth);

        Jc justification = objectFactory.createJc();
        justification.setVal(mapTextAlignment(tableBlock.alignment()));
        tableProperties.setJc(justification);

        TblBorders borders = objectFactory.createTblBorders();
        if (tableBlock.borderStyle() == com.abntbuilder.formatter.output.docx.api.TableBorderStyle.CLOSED) {
            borders.setTop(createTableBorder());
            borders.setBottom(createTableBorder());
            borders.setLeft(createTableBorder());
            borders.setRight(createTableBorder());
            borders.setInsideH(createTableBorder());
            borders.setInsideV(createTableBorder());
        } else {
            borders.setTop(createTableBorder());
            borders.setBottom(createTableBorder());
            borders.setInsideH(createTableBorder());
        }
        tableProperties.setTblBorders(borders);

        return tableProperties;
    }

    private Tr createTableRow(
            List<String> cells,
            StyleRule styleRule,
            int columnCount,
            boolean repeatHeaderOnPageBreak
    ) {
        Tr row = objectFactory.createTr();

        if (repeatHeaderOnPageBreak) {
            TrPr rowProperties = objectFactory.createTrPr();
            rowProperties.getCnfStyleOrDivIdOrGridBefore().add(
                    objectFactory.createCTTrPrBaseTblHeader(objectFactory.createBooleanDefaultTrue())
            );
            row.setTrPr(rowProperties);
        }

        for (String cellText : cells) {
            row.getContent().add(createTableCell(new com.abntbuilder.formatter.output.docx.api.DocxTableCell(cellText), styleRule, columnCount));
        }

        return row;
    }

    private Tr createTableRowFromCells(
            List<com.abntbuilder.formatter.output.docx.api.DocxTableCell> cells,
            StyleRule styleRule,
            int columnCount,
            boolean repeatHeaderOnPageBreak
    ) {
        Tr row = objectFactory.createTr();

        if (repeatHeaderOnPageBreak) {
            TrPr rowProperties = objectFactory.createTrPr();
            rowProperties.getCnfStyleOrDivIdOrGridBefore().add(
                    objectFactory.createCTTrPrBaseTblHeader(objectFactory.createBooleanDefaultTrue())
            );
            row.setTrPr(rowProperties);
        }

        for (com.abntbuilder.formatter.output.docx.api.DocxTableCell cell : cells) {
            row.getContent().add(createTableCell(cell, styleRule, columnCount));
        }

        return row;
    }

    private Tc createTableCell(com.abntbuilder.formatter.output.docx.api.DocxTableCell docxCell, StyleRule styleRule, int columnCount) {
        Tc cell = objectFactory.createTc();

        TcPr cellProperties = objectFactory.createTcPr();
        TblWidth cellWidth = objectFactory.createTblWidth();
        cellWidth.setType("pct");
        cellWidth.setW(BigInteger.valueOf(5000L * docxCell.colspan() / columnCount));
        cellProperties.setTcW(cellWidth);

        if (docxCell.colspan() > 1) {
            TcPrInner.GridSpan gridSpan = objectFactory.createTcPrInnerGridSpan();
            gridSpan.setVal(BigInteger.valueOf(docxCell.colspan()));
            cellProperties.setGridSpan(gridSpan);
        }

        if (docxCell.rowspanStart()) {
            TcPrInner.VMerge vMerge = objectFactory.createTcPrInnerVMerge();
            vMerge.setVal("restart");
            cellProperties.setVMerge(vMerge);
        } else if (docxCell.rowspanContinuation()) {
            TcPrInner.VMerge vMerge = objectFactory.createTcPrInnerVMerge();
            cellProperties.setVMerge(vMerge);
        }

        cell.setTcPr(cellProperties);
        String cellText = docxCell.text();

        P paragraph = objectFactory.createP();
        paragraph.setPPr(createParagraphProperties(
                styleRule,
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        ));

        R run = objectFactory.createR();
        run.setRPr(createRunProperties(styleRule));

        Text text = objectFactory.createText();
        text.setValue(resolveText(cellText, styleRule));

        run.getContent().add(text);
        paragraph.getContent().add(run);
        cell.getContent().add(paragraph);

        return cell;
    }

    private CTBorder createTableBorder() {
        CTBorder border = objectFactory.createCTBorder();
        border.setVal(STBorder.SINGLE);
        border.setSz(BigInteger.valueOf(4));
        border.setColor("auto");
        return border;
    }

    private BigInteger tableWidthPercentValue(BigDecimal widthPercent) {
        return widthPercent
                .multiply(BigDecimal.valueOf(50))
                .setScale(0, java.math.RoundingMode.HALF_UP)
                .toBigIntegerExact();
    }

    private void applyKeepOptions(PPr paragraphProperties, boolean keepWithNext, boolean keepLines) {
        if (keepWithNext) {
            paragraphProperties.setKeepNext(objectFactory.createBooleanDefaultTrue());
        }

        if (keepLines) {
            paragraphProperties.setKeepLines(objectFactory.createBooleanDefaultTrue());
        }
    }

    private static long centimetersToEmu(BigDecimal centimeters) {
        return centimeters
                .multiply(BigDecimal.valueOf(360000))
                .setScale(0, java.math.RoundingMode.HALF_UP)
                .longValueExact();
    }

    private void applyHeadingStyleDefinitions(
            WordprocessingMLPackage wordPackage,
            List<DocxBlock> blocks
    ) throws Exception {
        Map<String, StyleRule> headingRulesByStyleId = new LinkedHashMap<>();

        for (DocxBlock block : blocks) {
            if (block instanceof DocxParagraph paragraph && isHeadingStyle(paragraph.styleRule())) {
                String headingStyleId = resolveHeadingStyleId(paragraph.styleRule()).orElseThrow();
                StyleRule previousRule = headingRulesByStyleId.putIfAbsent(headingStyleId, paragraph.styleRule());

                if (previousRule != null && !previousRule.equals(paragraph.styleRule())) {
                    throw new IllegalArgumentException(
                            "Multiple style rules target the same Word heading style: " + headingStyleId
                    );
                }
            }
        }

        if (headingRulesByStyleId.isEmpty()) {
            return;
        }

        StyleDefinitionsPart stylesPart = wordPackage.getMainDocumentPart().getStyleDefinitionsPart();

        if (stylesPart == null) {
            stylesPart = new StyleDefinitionsPart();
            stylesPart.setJaxbElement(objectFactory.createStyles());
            wordPackage.getMainDocumentPart().addTargetPart(stylesPart);
        }

        Styles styles = (Styles) stylesPart.getJaxbElement();

        if (styles == null) {
            styles = objectFactory.createStyles();
            stylesPart.setJaxbElement(styles);
        }

        for (Map.Entry<String, StyleRule> entry : headingRulesByStyleId.entrySet()) {
            String headingStyleId = entry.getKey();

            styles.getStyle().removeIf(style -> headingStyleId.equals(style.getStyleId()));
            styles.getStyle().add(createHeadingStyle(headingStyleId, entry.getValue()));
        }
    }

    private void applyTocStyleDefinitions(
            WordprocessingMLPackage wordPackage,
            List<DocxBlock> blocks
    ) throws Exception {
        DocxTocBlock tocBlock = null;
        for (DocxBlock block : blocks) {
            if (block instanceof DocxTocBlock b) {
                tocBlock = b;
                break;
            }
        }
        if (tocBlock == null) return;

        StyleDefinitionsPart stylesPart = wordPackage.getMainDocumentPart().getStyleDefinitionsPart();
        if (stylesPart == null) {
            stylesPart = new StyleDefinitionsPart();
            stylesPart.setJaxbElement(objectFactory.createStyles());
            wordPackage.getMainDocumentPart().addTargetPart(stylesPart);
        }
        Styles styles = (Styles) stylesPart.getJaxbElement();
        if (styles == null) {
            styles = objectFactory.createStyles();
            stylesPart.setJaxbElement(styles);
        }

        // Tab stop at content right edge for dotted leader
        long rightEdgeTwips = MeasurementConverter.centimetersToTwips(new java.math.BigDecimal(tocBlock.contentWidthCm()));

        List<StyleRule> entryStyles = tocBlock.entryStylesByLevel();
        for (int level = 0; level < entryStyles.size(); level++) {
            StyleRule entryStyle = entryStyles.get(level);
            String styleId = "TOC" + (level + 1);
            final Styles finalStyles = styles;
            finalStyles.getStyle().removeIf(s -> styleId.equals(s.getStyleId()));

            Style style = objectFactory.createStyle();
            style.setType("paragraph");
            style.setStyleId(styleId);

            Style.Name name = objectFactory.createStyleName();
            name.setVal("TOC " + (level + 1));
            style.setName(name);

            Style.BasedOn basedOn = objectFactory.createStyleBasedOn();
            basedOn.setVal("Normal");
            style.setBasedOn(basedOn);

            PPr pPr = createParagraphProperties(entryStyle, Optional.empty(), Optional.empty(), Optional.empty());

            // Left indent increases per level (0.5 cm per level starting from level 2)
            if (level > 0) {
                PPrBase.Ind ind = pPr.getInd();
                if (ind == null) {
                    ind = objectFactory.createPPrBaseInd();
                    pPr.setInd(ind);
                }
                long indentTwips = MeasurementConverter.centimetersToTwips(new java.math.BigDecimal("0.5").multiply(new java.math.BigDecimal(level)));
                ind.setLeft(BigInteger.valueOf(indentTwips));
            }

            // Tab stop with dot leader at right edge
            CTTabStop tabStop = objectFactory.createCTTabStop();
            tabStop.setVal(STTabJc.RIGHT);
            tabStop.setLeader(STTabTlc.DOT);
            tabStop.setPos(BigInteger.valueOf(rightEdgeTwips));
            Tabs tabs = objectFactory.createTabs();
            tabs.getTab().add(tabStop);
            pPr.setTabs(tabs);

            style.setPPr(pPr);
            style.setRPr(createRunProperties(entryStyle));

            finalStyles.getStyle().add(style);
        }
    }

    private Style createHeadingStyle(String headingStyleId, StyleRule styleRule) {
        Style style = objectFactory.createStyle();
        style.setType("paragraph");
        style.setStyleId(headingStyleId);

        Style.Name name = objectFactory.createStyleName();
        name.setVal(resolveHeadingStyleName(styleRule.type()));
        style.setName(name);

        Style.BasedOn basedOn = objectFactory.createStyleBasedOn();
        basedOn.setVal("Normal");
        style.setBasedOn(basedOn);

        style.setPPr(createStyleParagraphProperties(styleRule));
        style.setRPr(createRunProperties(styleRule));
        style.setQFormat(objectFactory.createBooleanDefaultTrue());

        return style;
    }

    private PPr createParagraphProperties(
            StyleRule styleRule,
            Optional<BigDecimal> spacingBeforeOverridePt,
            Optional<BigDecimal> exactLineHeightPt,
            Optional<ParagraphLayoutOverride> layoutOverride
    ) {
        PPr paragraphProperties = objectFactory.createPPr();
        ParagraphLayoutOverride resolvedLayoutOverride = layoutOverride.orElseGet(ParagraphLayoutOverride::none);

        Jc justification = objectFactory.createJc();
        justification.setVal(mapTextAlignment(resolvedLayoutOverride.alignment().orElse(styleRule.alignment())));
        paragraphProperties.setJc(justification);

        PPrBase.Ind indentation = objectFactory.createPPrBaseInd();
        indentation.setFirstLine(BigInteger.valueOf(MeasurementConverter.centimetersToTwips(styleRule.firstLineIndentCm())));
        indentation.setLeft(BigInteger.valueOf(MeasurementConverter.centimetersToTwips(
                resolvedLayoutOverride.leftIndentCm().orElse(styleRule.leftIndentCm())
        )));
        indentation.setRight(BigInteger.valueOf(MeasurementConverter.centimetersToTwips(
                resolvedLayoutOverride.rightIndentCm().orElse(styleRule.rightIndentCm())
        )));
        paragraphProperties.setInd(indentation);

        BigDecimal spacingBeforePt = spacingBeforeOverridePt.orElse(styleRule.spacingBeforePt());

        PPrBase.Spacing spacing = objectFactory.createPPrBaseSpacing();
        spacing.setBefore(BigInteger.valueOf(MeasurementConverter.pointsToTwips(spacingBeforePt)));
        spacing.setAfter(BigInteger.valueOf(MeasurementConverter.pointsToTwips(styleRule.spacingAfterPt())));

        if (exactLineHeightPt.isPresent()) {
            spacing.setLine(BigInteger.valueOf(MeasurementConverter.pointsToTwips(exactLineHeightPt.get())));
            spacing.setLineRule(STLineSpacingRule.EXACT);
        } else {
            spacing.setLine(BigInteger.valueOf(MeasurementConverter.lineSpacingMultiplierToDocxLineValue(styleRule.lineSpacing())));
            spacing.setLineRule(STLineSpacingRule.AUTO);
        }

        paragraphProperties.setSpacing(spacing);

        return paragraphProperties;
    }

    private PPr createHeadingParagraphProperties(StyleRule styleRule) {
        PPr paragraphProperties = objectFactory.createPPr();

        PPrBase.PStyle paragraphStyle = objectFactory.createPPrBasePStyle();
        paragraphStyle.setVal(resolveHeadingStyleId(styleRule).orElseThrow());
        paragraphProperties.setPStyle(paragraphStyle);

        return paragraphProperties;
    }

    private PPr createStyleParagraphProperties(StyleRule styleRule) {
        PPr paragraphProperties = createParagraphProperties(
                styleRule,
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );

        resolveOutlineLevel(styleRule).ifPresent(level -> {
            PPrBase.OutlineLvl outlineLevel = objectFactory.createPPrBaseOutlineLvl();
            outlineLevel.setVal(BigInteger.valueOf(level));
            paragraphProperties.setOutlineLvl(outlineLevel);
        });

        return paragraphProperties;
    }

    private RPr createRunProperties(StyleRule styleRule) {
        RPr runProperties = objectFactory.createRPr();

        RFonts fonts = objectFactory.createRFonts();
        fonts.setAscii(styleRule.fontFamily());
        fonts.setHAnsi(styleRule.fontFamily());
        fonts.setCs(styleRule.fontFamily());
        runProperties.setRFonts(fonts);

        HpsMeasure fontSize = objectFactory.createHpsMeasure();
        fontSize.setVal(BigInteger.valueOf(MeasurementConverter.pointsToHalfPoints(styleRule.fontSizePt())));
        runProperties.setSz(fontSize);

        HpsMeasure complexScriptFontSize = objectFactory.createHpsMeasure();
        complexScriptFontSize.setVal(BigInteger.valueOf(MeasurementConverter.pointsToHalfPoints(styleRule.fontSizePt())));
        runProperties.setSzCs(complexScriptFontSize);

        if (styleRule.bold()) {
            BooleanDefaultTrue bold = objectFactory.createBooleanDefaultTrue();
            runProperties.setB(bold);
        }

        if (styleRule.italic()) {
            BooleanDefaultTrue italic = objectFactory.createBooleanDefaultTrue();
            runProperties.setI(italic);
        }

        return runProperties;
    }

    private RPr buildRunProperties(StyleRule baseStyle, InlineFormatting formatting) {
        RPr rPr = createRunProperties(baseStyle);

        formatting.bold().ifPresent(bold -> {
            if (bold) {
                rPr.setB(objectFactory.createBooleanDefaultTrue());
            } else {
                rPr.setB(null);
            }
        });

        formatting.italic().ifPresent(italic -> {
            if (italic) {
                rPr.setI(objectFactory.createBooleanDefaultTrue());
            } else {
                rPr.setI(null);
            }
        });

        formatting.underline().ifPresent(underline -> {
            if (underline) {
                U u = objectFactory.createU();
                u.setVal(UnderlineEnumeration.SINGLE);
                rPr.setU(u);
            }
        });

        formatting.superscript().ifPresent(sup -> {
            if (sup) {
                CTVerticalAlignRun vertAlign = objectFactory.createCTVerticalAlignRun();
                vertAlign.setVal(STVerticalAlignRun.SUPERSCRIPT);
                rPr.setVertAlign(vertAlign);
            }
        });

        formatting.subscript().ifPresent(sub -> {
            if (sub) {
                CTVerticalAlignRun vertAlign = objectFactory.createCTVerticalAlignRun();
                vertAlign.setVal(STVerticalAlignRun.SUBSCRIPT);
                rPr.setVertAlign(vertAlign);
            }
        });

        return rPr;
    }

    private String resolveText(String text, StyleRule styleRule) {
        if (styleRule.uppercase()) {
            return text.toUpperCase(Locale.ROOT);
        }

        return text;
    }

    private boolean isHeadingStyle(StyleRule styleRule) {
        return resolveHeadingStyleId(styleRule).isPresent();
    }

    private Optional<String> resolveHeadingStyleId(StyleRule styleRule) {
        return switch (styleRule.type()) {
            case HEADING_1 -> Optional.of("Heading1");
            case HEADING_2 -> Optional.of("Heading2");
            case HEADING_3 -> Optional.of("Heading3");
            case HEADING_4 -> Optional.of("Heading4");
            case HEADING_5 -> Optional.of("Heading5");
            case HEADING_6 -> Optional.of("Heading6");
            case PARAGRAPH, CHARACTER -> Optional.empty();
        };
    }

    private String resolveHeadingStyleName(StyleType styleType) {
        return switch (styleType) {
            case HEADING_1 -> "heading 1";
            case HEADING_2 -> "heading 2";
            case HEADING_3 -> "heading 3";
            case HEADING_4 -> "heading 4";
            case HEADING_5 -> "heading 5";
            case HEADING_6 -> "heading 6";
            case PARAGRAPH, CHARACTER -> throw new IllegalArgumentException(
                    "Style type is not a heading: " + styleType
            );
        };
    }

    private Optional<Integer> resolveOutlineLevel(StyleRule styleRule) {
        return switch (styleRule.type()) {
            case HEADING_1 -> Optional.of(0);
            case HEADING_2 -> Optional.of(1);
            case HEADING_3 -> Optional.of(2);
            case HEADING_4 -> Optional.of(3);
            case HEADING_5 -> Optional.of(4);
            case HEADING_6 -> Optional.of(5);
            case PARAGRAPH, CHARACTER -> Optional.empty();
        };
    }

    private STPageOrientation mapPageOrientation(PageOrientation orientation) {
        return switch (orientation) {
            case PORTRAIT -> STPageOrientation.PORTRAIT;
            case LANDSCAPE -> STPageOrientation.LANDSCAPE;
        };
    }

    private void writeFootnoteReferenceBlock(WordprocessingMLPackage wordPackage, com.abntbuilder.formatter.output.docx.api.DocxFootnoteReferenceBlock fnBlock, ListNumIds listNumIds) {
        try {
            org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart fnPart = wordPackage.getMainDocumentPart().getFootnotesPart();
            if (fnPart == null) {
                fnPart = new org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart();
                wordPackage.getMainDocumentPart().addTargetPart(fnPart);
                org.docx4j.wml.CTFootnotes footnotes = objectFactory.createCTFootnotes();
                fnPart.setJaxbElement(footnotes);
            }
            org.docx4j.wml.CTFootnotes ctFootnotes = fnPart.getJaxbElement();

            for (com.abntbuilder.formatter.output.docx.api.DocxFootnoteContent fn : fnBlock.footnotes()) {
                org.docx4j.wml.CTFtnEdn footnote = objectFactory.createCTFtnEdn();
                footnote.setId(java.math.BigInteger.valueOf(fn.id()));
                
                P fnParagraph = objectFactory.createP();
                for (com.abntbuilder.formatter.output.docx.api.DocxRun docxRun : fn.contentRuns()) {
                    R run = objectFactory.createR();
                    run.setRPr(buildRunProperties(docxRun.baseStyle(), docxRun.formatting()));
                    Text t = objectFactory.createText();
                    t.setValue(docxRun.text());
                    t.setSpace("preserve");
                    run.getContent().add(t);
                    fnParagraph.getContent().add(run);
                }
                footnote.getContent().add(fnParagraph);
                ctFootnotes.getFootnote().add(footnote);
            }
        } catch (org.docx4j.openpackaging.exceptions.InvalidFormatException e) {
            throw new RuntimeException("Failed to add FootnotesPart", e);
        }

        writeBlock(wordPackage, fnBlock.hostBlock(), listNumIds);
    }

    private JcEnumeration mapTextAlignment(TextAlignment alignment) {
        return switch (alignment) {
            case LEFT -> JcEnumeration.LEFT;
            case CENTER -> JcEnumeration.CENTER;
            case RIGHT -> JcEnumeration.RIGHT;
            case JUSTIFIED -> JcEnumeration.BOTH;
        };
    }

    private void writePageBreak(WordprocessingMLPackage wordPackage) {
        P paragraph = objectFactory.createP();

        R run = objectFactory.createR();

        Br pageBreak = objectFactory.createBr();
        pageBreak.setType(STBrType.PAGE);

        run.getContent().add(pageBreak);
        paragraph.getContent().add(run);

        wordPackage.getMainDocumentPart().addObject(paragraph);
    }

    private void writeToc(WordprocessingMLPackage wordPackage, DocxTocBlock tocBlock) {
        P p = objectFactory.createP();
        PPr pPr = createParagraphProperties(tocBlock.styleRule(), Optional.empty(), Optional.empty(), Optional.empty());
        p.setPPr(pPr);

        R beginRun = objectFactory.createR();
        FldChar beginFldChar = objectFactory.createFldChar();
        beginFldChar.setFldCharType(STFldCharType.BEGIN);
        beginFldChar.setDirty(true);
        beginRun.getContent().add(objectFactory.createRFldChar(beginFldChar));
        p.getContent().add(beginRun);

        R instrRun = objectFactory.createR();
        Text instrText = objectFactory.createText();
        instrText.setValue(tocBlock.tocInstruction());
        instrText.setSpace("preserve");
        instrRun.getContent().add(objectFactory.createRInstrText(instrText));
        p.getContent().add(instrRun);

        R endRun = objectFactory.createR();
        FldChar endFldChar = objectFactory.createFldChar();
        endFldChar.setFldCharType(STFldCharType.END);
        endRun.getContent().add(objectFactory.createRFldChar(endFldChar));
        p.getContent().add(endRun);

        wordPackage.getMainDocumentPart().addObject(p);
    }

    private void addPageNumberingReference(
            WordprocessingMLPackage wordPackage,
            SectPr sectionProperties,
            PageRule pageRule,
            DocxPageNumbering pageNumbering
    ) throws Exception {
        if (isHeaderPlacement(pageNumbering.placement())) {
            HeaderPart headerPart = new HeaderPart(new PartName("/word/header" + System.nanoTime() + ".xml"));
            Hdr header = objectFactory.createHdr();
            header.getContent().add(createPageNumberParagraph(pageNumbering, pageRule));
            headerPart.setJaxbElement(header);

            Relationship relationship = wordPackage.getMainDocumentPart().addTargetPart(headerPart);
            HeaderReference headerReference = objectFactory.createHeaderReference();
            headerReference.setType(HdrFtrRef.DEFAULT);
            headerReference.setId(relationship.getId());
            sectionProperties.getEGHdrFtrReferences().add(headerReference);
            return;
        }

        FooterPart footerPart = new FooterPart(new PartName("/word/footer" + System.nanoTime() + ".xml"));
        Ftr footer = objectFactory.createFtr();
        footer.getContent().add(createPageNumberParagraph(pageNumbering, pageRule));
        footerPart.setJaxbElement(footer);

        Relationship relationship = wordPackage.getMainDocumentPart().addTargetPart(footerPart);
        FooterReference footerReference = objectFactory.createFooterReference();
        footerReference.setType(HdrFtrRef.DEFAULT);
        footerReference.setId(relationship.getId());
        sectionProperties.getEGHdrFtrReferences().add(footerReference);
    }

    private void applyPageNumberingMargins(SectPr.PgMar pageMargins, DocxPageNumbering pageNumbering) {
        BigInteger verticalDistance = BigInteger.valueOf(MeasurementConverter.centimetersToTwips(
                pageNumbering.verticalDistanceFromPageEdgeCm()
        ));

        if (isHeaderPlacement(pageNumbering.placement())) {
            pageMargins.setHeader(verticalDistance);
            return;
        }

        pageMargins.setFooter(verticalDistance);
    }

    private void applyPageCountingBoundary(SectPr sectionProperties) {
        CTPageNumber pageNumber = objectFactory.createCTPageNumber();
        pageNumber.setStart(BigInteger.ONE);
        sectionProperties.setPgNumType(pageNumber);
    }

    private P createPageNumberParagraph(DocxPageNumbering pageNumbering, PageRule pageRule) {
        P paragraph = objectFactory.createP();
        paragraph.setPPr(createParagraphProperties(
                pageNumbering.styleRule(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(new ParagraphLayoutOverride(
                        Optional.empty(),
                        rightIndentForPageNumbering(pageNumbering, pageRule),
                        Optional.of(alignmentForPageNumbering(pageNumbering.placement()))
                ))
        ));

        R beginRun = objectFactory.createR();
        beginRun.setRPr(createRunProperties(pageNumbering.styleRule()));
        FldChar begin = objectFactory.createFldChar();
        begin.setFldCharType(STFldCharType.BEGIN);
        beginRun.getContent().add(objectFactory.createRFldChar(begin));

        R instructionRun = objectFactory.createR();
        instructionRun.setRPr(createRunProperties(pageNumbering.styleRule()));
        Text instruction = objectFactory.createText();
        instruction.setSpace("preserve");
        instruction.setValue(" PAGE ");
        instructionRun.getContent().add(objectFactory.createRInstrText(instruction));

        R endRun = objectFactory.createR();
        endRun.setRPr(createRunProperties(pageNumbering.styleRule()));
        FldChar end = objectFactory.createFldChar();
        end.setFldCharType(STFldCharType.END);
        endRun.getContent().add(objectFactory.createRFldChar(end));

        paragraph.getContent().add(beginRun);
        paragraph.getContent().add(instructionRun);
        paragraph.getContent().add(endRun);

        return paragraph;
    }

    private Optional<BigDecimal> rightIndentForPageNumbering(DocxPageNumbering pageNumbering, PageRule pageRule) {
        return switch (pageNumbering.placement()) {
            case HEADER_RIGHT, FOOTER_RIGHT -> {
                BigDecimal rightIndent = pageNumbering.horizontalDistanceFromPageEdgeCm()
                        .subtract(pageRule.marginRightCm());
                yield rightIndent.signum() > 0 ? Optional.of(rightIndent) : Optional.empty();
            }
            case HEADER_CENTER, FOOTER_CENTER -> Optional.empty();
        };
    }

    private boolean isHeaderPlacement(PageNumberingPlacement placement) {
        return switch (placement) {
            case HEADER_RIGHT, HEADER_CENTER -> true;
            case FOOTER_RIGHT, FOOTER_CENTER -> false;
        };
    }

    private TextAlignment alignmentForPageNumbering(PageNumberingPlacement placement) {
        return switch (placement) {
            case HEADER_RIGHT, FOOTER_RIGHT -> TextAlignment.RIGHT;
            case HEADER_CENTER, FOOTER_CENTER -> TextAlignment.CENTER;
        };
    }

    private void writeSectionBreak(
            WordprocessingMLPackage wordPackage,
            PageRule pageRule,
            Optional<DocxPageNumbering> currentSectionPageNumbering
    ) {
        try {
            SectPr sectionProperties = createSectionProperties(wordPackage, pageRule, currentSectionPageNumbering);

            P paragraph = objectFactory.createP();
            PPr paragraphProperties = objectFactory.createPPr();

            SectPr sectionBreak = objectFactory.createSectPr();
            sectionBreak.setPgSz(sectionProperties.getPgSz());
            sectionBreak.setPgMar(sectionProperties.getPgMar());
            sectionBreak.getEGHdrFtrReferences().addAll(sectionProperties.getEGHdrFtrReferences());

            SectPr.Type sectionBreakType = objectFactory.createSectPrType();
            sectionBreakType.setVal("nextPage");
            sectionBreak.setType(sectionBreakType);

            paragraphProperties.setSectPr(sectionBreak);
            paragraph.setPPr(paragraphProperties);

            wordPackage.getMainDocumentPart().addObject(paragraph);
        } catch (Exception exception) {
            throw new DocxWriterException("Failed to write DOCX section break.", exception);
        }
    }

    private ListNumIds createListNumIds(WordprocessingMLPackage wordPackage) {
        NumberingDefinitionsPart ndp;
        try {
            ndp = new NumberingDefinitionsPart();
            wordPackage.getMainDocumentPart().addTargetPart(ndp);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create NumberingDefinitionsPart", e);
        }
        Numbering numbering = objectFactory.createNumbering();
        ndp.setJaxbElement(numbering);

        // AbstractNum ordered (decimal)
        Numbering.AbstractNum abstractOrdered = objectFactory.createNumberingAbstractNum();
        abstractOrdered.setAbstractNumId(BigInteger.ZERO);
        Lvl lvlOrdered = objectFactory.createLvl();
        lvlOrdered.setIlvl(BigInteger.ZERO);
        NumFmt fmtOrdered = objectFactory.createNumFmt();
        fmtOrdered.setVal(NumberFormat.DECIMAL);
        lvlOrdered.setNumFmt(fmtOrdered);
        Lvl.LvlText lvlTextOrdered = objectFactory.createLvlLvlText();
        lvlTextOrdered.setVal("%1.");
        lvlOrdered.setLvlText(lvlTextOrdered);
        Lvl.Start startOrdered = objectFactory.createLvlStart();
        startOrdered.setVal(BigInteger.ONE);
        lvlOrdered.setStart(startOrdered);
        abstractOrdered.getLvl().add(lvlOrdered);
        numbering.getAbstractNum().add(abstractOrdered);

        // AbstractNum unordered (bullet)
        Numbering.AbstractNum abstractUnordered = objectFactory.createNumberingAbstractNum();
        abstractUnordered.setAbstractNumId(BigInteger.ONE);
        Lvl lvlUnordered = objectFactory.createLvl();
        lvlUnordered.setIlvl(BigInteger.ZERO);
        NumFmt fmtUnordered = objectFactory.createNumFmt();
        fmtUnordered.setVal(NumberFormat.BULLET);
        lvlUnordered.setNumFmt(fmtUnordered);
        Lvl.LvlText lvlTextUnordered = objectFactory.createLvlLvlText();
        lvlTextUnordered.setVal("•");
        lvlUnordered.setLvlText(lvlTextUnordered);
        abstractUnordered.getLvl().add(lvlUnordered);
        numbering.getAbstractNum().add(abstractUnordered);

        // Num ordered — numId 1
        Numbering.Num numOrdered = objectFactory.createNumberingNum();
        numOrdered.setNumId(BigInteger.ONE);
        Numbering.Num.AbstractNumId aoidOrdered = objectFactory.createNumberingNumAbstractNumId();
        aoidOrdered.setVal(BigInteger.ZERO);
        numOrdered.setAbstractNumId(aoidOrdered);
        numbering.getNum().add(numOrdered);

        // Num unordered — numId 2
        Numbering.Num numUnordered = objectFactory.createNumberingNum();
        numUnordered.setNumId(BigInteger.TWO);
        Numbering.Num.AbstractNumId aoidUnordered = objectFactory.createNumberingNumAbstractNumId();
        aoidUnordered.setVal(BigInteger.ONE);
        numUnordered.setAbstractNumId(aoidUnordered);
        numbering.getNum().add(numUnordered);

        return new ListNumIds(1, 2);
    }

    private void writeListItem(
            WordprocessingMLPackage wordPackage,
            DocxListItemParagraph listItem,
            ListNumIds listNumIds
    ) {
        P p = objectFactory.createP();
        PPr pPr = createParagraphProperties(
                listItem.styleRule(), Optional.empty(), Optional.empty(), Optional.empty()
        );
        PPrBase.NumPr numPr = objectFactory.createPPrBaseNumPr();
        PPrBase.NumPr.Ilvl ilvl = objectFactory.createPPrBaseNumPrIlvl();
        ilvl.setVal(BigInteger.valueOf(listItem.listLevel()));
        numPr.setIlvl(ilvl);
        PPrBase.NumPr.NumId numId = objectFactory.createPPrBaseNumPrNumId();
        int id = listItem.listType() == BodyListType.ORDERED
                ? listNumIds.orderedNumId()
                : listNumIds.unorderedNumId();
        numId.setVal(BigInteger.valueOf(id));
        numPr.setNumId(numId);
        pPr.setNumPr(numPr);
        p.setPPr(pPr);
        for (DocxRun run : listItem.runs()) {
            String text = run.text();
            if (text.startsWith("[FN:") && text.endsWith("]")) {
                int fnId = Integer.parseInt(text.substring(4, text.length() - 1));
                R refRun = objectFactory.createR();
                org.docx4j.wml.CTFtnEdnRef ref = objectFactory.createCTFtnEdnRef();
                ref.setId(java.math.BigInteger.valueOf(fnId));
                refRun.getContent().add(objectFactory.createRFootnoteReference(ref));
                p.getContent().add(refRun);
            } else {
                R r = objectFactory.createR();
                r.setRPr(buildRunProperties(run.baseStyle(), run.formatting()));
                Text t = objectFactory.createText();
                t.setValue(resolveText(text, run.baseStyle()));
                t.setSpace("preserve");
                r.getContent().add(t);
                p.getContent().add(r);
            }
        }
        wordPackage.getMainDocumentPart().addObject(p);
    }

    private record ListNumIds(int orderedNumId, int unorderedNumId) {}

    private void writeBlankLine(WordprocessingMLPackage wordPackage, DocxBlankLine blankLine) {
        P paragraph = objectFactory.createP();

        paragraph.setPPr(createParagraphProperties(
                blankLine.styleRule(),
                Optional.empty(),
                blankLine.exactLineHeightPt(),
                Optional.empty()
        ));

        R run = objectFactory.createR();
        run.setRPr(createRunProperties(blankLine.styleRule()));

        Text text = objectFactory.createText();
        text.setSpace("preserve");
        text.setValue(" ");

        run.getContent().add(text);
        paragraph.getContent().add(run);

        wordPackage.getMainDocumentPart().addObject(paragraph);
    }

}
