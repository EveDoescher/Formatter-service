package com.abntbuilder.formatter.output.docx.docx4j;

import com.abntbuilder.formatter.output.docx.api.*;
import com.abntbuilder.formatter.profile.model.PageOrientation;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.StyleType;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.shared.measurement.MeasurementConverter;
import com.abntbuilder.formatter.output.docx.api.DocxBlankLine;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.wml.BooleanDefaultTrue;
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
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.docx4j.wml.Text;
import org.docx4j.wml.Br;
import org.docx4j.wml.STBrType;

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
            applyPageRule(wordPackage, document.pageRule());
            applyHeadingStyleDefinitions(wordPackage, document.blocks());

            for (DocxBlock block : document.blocks()) {
                writeBlock(wordPackage, block);
            }

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

    private void applyPageRule(WordprocessingMLPackage wordPackage, PageRule pageRule) {
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

        sectionProperties.setPgSz(pageSize);
        sectionProperties.setPgMar(pageMargins);

        wordPackage
                .getMainDocumentPart()
                .getJaxbElement()
                .getBody()
                .setSectPr(sectionProperties);
    }

    private void writeBlock(WordprocessingMLPackage wordPackage, DocxBlock block) {
        switch (block) {
            case DocxParagraph paragraph -> writeParagraph(wordPackage, paragraph);
            case DocxPageBreak ignored -> writePageBreak(wordPackage);
            case DocxBlankLine blankLine -> writeBlankLine(wordPackage, blankLine);
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

        R run = objectFactory.createR();

        if (!isHeadingStyle(paragraph.styleRule())) {
            run.setRPr(createRunProperties(paragraph.styleRule()));
        }

        Text text = objectFactory.createText();
        text.setValue(resolveText(paragraph.text(), paragraph.styleRule()));

        run.getContent().add(text);
        docxParagraph.getContent().add(run);

        wordPackage.getMainDocumentPart().addObject(docxParagraph);
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

    private Style createHeadingStyle(String headingStyleId, StyleRule styleRule) {
        Style style = objectFactory.createStyle();
        style.setType("paragraph");
        style.setStyleId(headingStyleId);

        Style.Name name = objectFactory.createStyleName();
        name.setVal(resolveHeadingStyleName(styleRule.type()));
        style.setName(name);

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
