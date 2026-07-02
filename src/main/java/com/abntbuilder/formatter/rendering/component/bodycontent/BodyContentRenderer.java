package com.abntbuilder.formatter.rendering.component.bodycontent;

import com.abntbuilder.formatter.document.component.bodycontent.BodyContentComponent;
import com.abntbuilder.formatter.document.component.bodycontent.BodyCrossReference;
import com.abntbuilder.formatter.document.component.bodycontent.CrossReferenceDisplayMode;
import com.abntbuilder.formatter.document.component.bodycontent.CrossReferenceTargetType;
import com.abntbuilder.formatter.document.component.bodycontent.BodyBlock;
import com.abntbuilder.formatter.document.component.bodycontent.BodyAbbreviation;
import com.abntbuilder.formatter.document.component.bodycontent.BodyCitationCall;
import com.abntbuilder.formatter.document.component.bodycontent.BodyCitationType;
import com.abntbuilder.formatter.document.component.bodycontent.BodyChart;
import com.abntbuilder.formatter.document.component.bodycontent.BodyCodeListing;
import com.abntbuilder.formatter.document.component.bodycontent.BodyEquation;
import com.abntbuilder.formatter.document.component.bodycontent.BodyFigure;
import com.abntbuilder.formatter.document.component.bodycontent.BodyFrame;
import com.abntbuilder.formatter.document.component.bodycontent.BodyInline;
import com.abntbuilder.formatter.document.component.bodycontent.BodyLongQuote;
import com.abntbuilder.formatter.document.component.bodycontent.BodyImageSource;
import com.abntbuilder.formatter.document.component.bodycontent.ImageSourceType;
import com.abntbuilder.formatter.document.component.bodycontent.BodyParagraph;
import com.abntbuilder.formatter.document.component.bodycontent.BodyFootnote;
import com.abntbuilder.formatter.document.component.bodycontent.BodyQuoteMarkerType;
import com.abntbuilder.formatter.document.component.bodycontent.BodyQuoteText;
import com.abntbuilder.formatter.document.component.bodycontent.BodyText;
import com.abntbuilder.formatter.document.component.bodycontent.InlineFormatting;
import com.abntbuilder.formatter.document.component.bodycontent.BodySection;
import com.abntbuilder.formatter.document.component.bodycontent.BodyTable;
import com.abntbuilder.formatter.document.component.bodycontent.BodyList;
import com.abntbuilder.formatter.output.docx.api.DocxBlankLine;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxImageBlock;
import com.abntbuilder.formatter.output.docx.api.DocxPageBreak;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.output.docx.api.DocxTableBlock;
import com.abntbuilder.formatter.output.docx.api.DocxListItemParagraph;
import com.abntbuilder.formatter.document.component.bodycontent.BodyListType;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.BodyContentComponentRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.BodyContentNumberingRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.ChartRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.CodeListingRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.DisplayObjectSourcePlacement;
import com.abntbuilder.formatter.profile.model.component.bodycontent.FigureRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.FrameRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.ImageFitPolicy;
import com.abntbuilder.formatter.profile.model.component.bodycontent.CrossReferenceLabelsRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.TableRule;
import com.abntbuilder.formatter.output.docx.api.TableBorderStyle;
import com.abntbuilder.formatter.shared.exception.InvalidBodyContentException;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;
import com.abntbuilder.formatter.rendering.component.MetadataEmittingRenderer;
import com.abntbuilder.formatter.rendering.component.Phase0ConsumingRenderer;
import com.abntbuilder.formatter.rendering.phase0.Phase0Index;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class BodyContentRenderer
        implements Phase0ConsumingRenderer<BodyContentComponent, BodyContentRenderResult> {

    public static final String COMPONENT_ID = "bodyContent";

    @Override
    public String componentId() {
        return COMPONENT_ID;
    }

    @Override
    public Class<BodyContentComponent> componentType() {
        return BodyContentComponent.class;
    }

    @Override
    public BodyContentRenderResult renderWithPhase0(BodyContentComponent component, DocumentProfile profile, Phase0Index phase0Index) {
        Objects.requireNonNull(component, "component must not be null");
        Objects.requireNonNull(profile, "profile must not be null");
        Objects.requireNonNull(phase0Index, "phase0Index must not be null");

        BodyContentComponentRule rule = bodyContentRule(profile);
        StyleResolver styleResolver = new StyleResolver(profile);
        SectionNumberingState numberingState = new SectionNumberingState(rule.numbering());
        StyleRule blankLineStyle = styleResolver.resolve(rule.layout().blankLineStyleId());
        boolean previousBlockWasTextualContent = false;
        DisplayObjectRenderingState<BodyFigure> figureRenderingState = new DisplayObjectRenderingState<>(
                figuresFrom(component.sections())
        );
        DisplayObjectRenderingState<BodyTable> tableRenderingState = new DisplayObjectRenderingState<>(
                tablesFrom(component.sections())
        );
        DisplayObjectRenderingState<BodyFrame> frameRenderingState = new DisplayObjectRenderingState<>(
                framesFrom(component.sections())
        );
        DisplayObjectRenderingState<BodyCodeListing> codeListingRenderingState = new DisplayObjectRenderingState<>(
                codeListingsFrom(component.sections())
        );
        DisplayObjectRenderingState<BodyChart> chartRenderingState = new DisplayObjectRenderingState<>(
                chartsFrom(component.sections())
        );

        int[] footnoteCounter = new int[1];

        List<BodySectionMetadata> sectionMetas = new ArrayList<>();
        List<BodyDisplayObjectMetadata> figureMetas = new ArrayList<>();
        List<BodyDisplayObjectMetadata> tableMetas = new ArrayList<>();
        List<BodyDisplayObjectMetadata> frameMetas = new ArrayList<>();
        List<BodyDisplayObjectMetadata> chartMetas = new ArrayList<>();
        List<BodyDisplayObjectMetadata> codeListingMetas = new ArrayList<>();
        List<BodyAbbreviationMetadata> abbreviationMetas = new ArrayList<>();

        List<DocxBlock> blocks = new ArrayList<>();

        for (BodySection section : component.sections()) {
            if (section.title().isPresent()) {
                if (rule.layout().pageBreakBeforePrimarySection() && section.level() == 1 && !blocks.isEmpty()) {
                    blocks.add(new DocxPageBreak());
                } else if (previousBlockWasTextualContent) {
                    addBlankLines(
                            blocks,
                            blankLineStyle,
                            rule.layout().blankLinesBeforeSectionTitleWhenPrecededByContent()
                    );
                }

                StyleRule titleStyle = styleResolver.resolve(rule.styleMapping().sectionTitleStyleIdForLevel(section.level()));
                String renderedTitle = numberingState.resolveTitle(section.level(), section.title().orElseThrow());
                String renderedNumber = numberingState.resolveNumber(section.level());
                blocks.add(new DocxParagraph(
                        List.of(DocxRun.of(renderedTitle, titleStyle)),
                        titleStyle
                ));
                sectionMetas.add(new BodySectionMetadata(section.id(), section.level(), renderedTitle, renderedNumber));
                previousBlockWasTextualContent = false;

                addBlankLines(blocks, blankLineStyle, rule.layout().blankLinesAfterSectionTitle());
            }

            for (int blockIndex = 0; blockIndex < section.blocks().size(); blockIndex++) {
                BodyBlock contentBlock = section.blocks().get(blockIndex);
                try {
                    blocks.addAll(renderContentBlock(
                            contentBlock,
                            rule,
                            styleResolver,
                            figureRenderingState,
                            tableRenderingState,
                            frameRenderingState,
                            codeListingRenderingState,
                            chartRenderingState,
                            abbreviationMetas,
                            figureMetas,
                            tableMetas,
                            frameMetas,
                            chartMetas,
                            codeListingMetas,
                            footnoteCounter,
                            phase0Index
                    ));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(
                            "Error in section '" + section.id() + "', block[" + blockIndex + "] ("
                                    + contentBlock.getClass().getSimpleName() + "): " + e.getMessage(), e);
                }
                previousBlockWasTextualContent = contentBlock instanceof BodyParagraph
                        || contentBlock instanceof BodyLongQuote;
            }
        }

        BodyContentMetadata metadata = new BodyContentMetadata(
                List.copyOf(sectionMetas),
                List.copyOf(figureMetas),
                List.copyOf(tableMetas),
                List.copyOf(frameMetas),
                List.copyOf(chartMetas),
                List.copyOf(codeListingMetas),
                List.copyOf(abbreviationMetas)
        );
        return new BodyContentRenderResult(List.copyOf(blocks), metadata);
    }

    private static BodyContentComponentRule bodyContentRule(DocumentProfile profile) {
        return new ComponentRuleResolver(profile).resolve(COMPONENT_ID, BodyContentComponentRule.class);
    }

    private static void addBlankLines(List<DocxBlock> blocks, StyleRule styleRule, int count) {
        for (int index = 0; index < count; index++) {
            blocks.add(new DocxBlankLine(styleRule));
        }
    }

    private static List<DocxBlock> renderListItems(
            BodyList list,
            StyleRule itemStyle,
            int nestingLevel,
            BodyContentComponentRule rule,
            StyleResolver styleResolver,
            List<BodyAbbreviationMetadata> abbreviationMetas,
            int[] footnoteCounter,
            Phase0Index phase0Index
    ) {
        List<DocxBlock> blocks = new ArrayList<>();
        for (com.abntbuilder.formatter.document.component.bodycontent.BodyListItem item : list.items()) {
            java.util.List<com.abntbuilder.formatter.output.docx.api.DocxFootnoteContent> footnoteAccumulator = new java.util.ArrayList<>();
            List<DocxRun> runs = item.content().stream()
                    .flatMap(inline -> toDocxRun(inline, itemStyle, rule, styleResolver, abbreviationMetas, footnoteAccumulator, footnoteCounter, phase0Index).stream())
                    .toList();
            DocxBlock listItem = new DocxListItemParagraph(runs, itemStyle, list.type(), nestingLevel);
            if (!footnoteAccumulator.isEmpty()) {
                blocks.add(new com.abntbuilder.formatter.output.docx.api.DocxFootnoteReferenceBlock(listItem, footnoteAccumulator));
            } else {
                blocks.add(listItem);
            }
            item.subList().ifPresent(subList -> {
                StyleRule subItemStyle = styleResolver.resolve(
                        subList.type() == BodyListType.ORDERED
                                ? rule.styleMapping().listOrderedStyleId()
                                : rule.styleMapping().listUnorderedStyleId()
                );
                blocks.addAll(renderListItems(subList, subItemStyle, nestingLevel + 1, rule, styleResolver, abbreviationMetas, footnoteCounter, phase0Index));
            });
        }
        return blocks;
    }

    private static List<DocxBlock> renderContentBlock(
            BodyBlock contentBlock,
            BodyContentComponentRule rule,
            StyleResolver styleResolver,
            DisplayObjectRenderingState<BodyFigure> figureRenderingState,
            DisplayObjectRenderingState<BodyTable> tableRenderingState,
            DisplayObjectRenderingState<BodyFrame> frameRenderingState,
            DisplayObjectRenderingState<BodyCodeListing> codeListingRenderingState,
            DisplayObjectRenderingState<BodyChart> chartRenderingState,
            List<BodyAbbreviationMetadata> abbreviationMetas,
            List<BodyDisplayObjectMetadata> figureMetas,
            List<BodyDisplayObjectMetadata> tableMetas,
            List<BodyDisplayObjectMetadata> frameMetas,
            List<BodyDisplayObjectMetadata> chartMetas,
            List<BodyDisplayObjectMetadata> codeListingMetas,
            int[] footnoteCounter,
            Phase0Index phase0Index
    ) {
        return switch (contentBlock) {
            case BodyParagraph paragraph -> {
                StyleRule paragraphStyle = styleResolver.resolve(rule.styleMapping().paragraphStyleId());
                java.util.List<com.abntbuilder.formatter.output.docx.api.DocxFootnoteContent> footnoteAccumulator = new java.util.ArrayList<>();
                List<DocxRun> runs = paragraph.content().stream()
                        .flatMap(inline -> toDocxRun(inline, paragraphStyle, rule, styleResolver, abbreviationMetas, footnoteAccumulator, footnoteCounter, phase0Index).stream())
                        .toList();
                DocxParagraph docxParagraph = new DocxParagraph(runs, paragraphStyle);
                if (!footnoteAccumulator.isEmpty()) {
                    yield List.of(new com.abntbuilder.formatter.output.docx.api.DocxFootnoteReferenceBlock(docxParagraph, footnoteAccumulator));
                }
                yield List.of(docxParagraph);
            }
            case BodyLongQuote longQuote -> {
                StyleRule longQuoteStyle = styleResolver.resolve(rule.styleMapping().directLongQuoteStyleId());
                yield List.of(new DocxParagraph(
                        List.of(DocxRun.of(longQuote.renderedText(rule.citationFormatting()), longQuoteStyle)),
                        longQuoteStyle
                ));
            }
            case BodyFigure figure -> {
                DisplayObjectContinuationPart part = figureRenderingState.nextPart(figure, rule.figure().continuationLabels());
                if (part.continuationLabel().isEmpty()) {
                    figureMetas.add(new BodyDisplayObjectMetadata(figure.id(), part.number(), figure.caption()));
                }
                yield renderFigure(figure, rule.figure(), styleResolver, figureRenderingState, part);
            }
            case BodyTable table -> {
                DisplayObjectContinuationPart part = tableRenderingState.nextPart(table, rule.table().continuationLabels());
                if (part.continuationLabel().isEmpty()) {
                    tableMetas.add(new BodyDisplayObjectMetadata(table.id(), part.number(), table.caption()));
                }
                yield renderTable(table, rule.table(), styleResolver, tableRenderingState, part);
            }
            case BodyList list -> {
                StyleRule itemStyle = styleResolver.resolve(
                        list.type() == BodyListType.ORDERED
                                ? rule.styleMapping().listOrderedStyleId()
                                : rule.styleMapping().listUnorderedStyleId()
                );
                yield renderListItems(list, itemStyle, 0, rule, styleResolver, abbreviationMetas, footnoteCounter, phase0Index);
            }
            case BodyFrame frame -> {
                DisplayObjectContinuationPart part = frameRenderingState.nextPart(frame, rule.frame().continuationLabels());
                if (part.continuationLabel().isEmpty()) {
                    frameMetas.add(new BodyDisplayObjectMetadata(frame.id(), part.number(), frame.caption()));
                }
                yield renderFrame(frame, rule.frame(), styleResolver, frameRenderingState, part);
            }
            case BodyCodeListing codeListing -> {
                DisplayObjectContinuationPart part = codeListingRenderingState.nextPart(codeListing, rule.codeListing().continuationLabels());
                if (part.continuationLabel().isEmpty()) {
                    codeListingMetas.add(new BodyDisplayObjectMetadata(codeListing.id(), part.number(), codeListing.caption()));
                }
                yield renderCodeListing(codeListing, rule.codeListing(), styleResolver, codeListingRenderingState, part);
            }
            case BodyChart chart -> {
                DisplayObjectContinuationPart part = chartRenderingState.nextPart(chart, rule.chart().continuationLabels());
                if (part.continuationLabel().isEmpty()) {
                    chartMetas.add(new BodyDisplayObjectMetadata(chart.id(), part.number(), chart.caption()));
                }
                yield renderChart(chart, rule.chart(), styleResolver, chartRenderingState, part);
            }
            case BodyEquation equation -> {
                StyleRule equationStyle = styleResolver.resolve(rule.styleMapping().equationStyleId());
                yield List.of(new DocxParagraph(
                        List.of(DocxRun.of(equation.text(), equationStyle)),
                        equationStyle
                ));
            }
        };
    }

    private static List<DocxRun> toDocxRun(
            BodyInline inline,
            StyleRule baseStyle,
            BodyContentComponentRule rule,
            StyleResolver styleResolver,
            List<BodyAbbreviationMetadata> abbreviationMetas,
            java.util.List<com.abntbuilder.formatter.output.docx.api.DocxFootnoteContent> footnoteAccumulator,
            int[] footnoteCounter,
            Phase0Index phase0Index
    ) {
        return switch (inline) {
            case BodyText text -> List.of(new DocxRun(text.text(), baseStyle, text.formatting()));
            case BodyQuoteText quote -> {
                List<DocxRun> runs = new ArrayList<>();
                runs.add(new DocxRun(quote.renderedText(), baseStyle, quote.formatting()));
                boolean hasEmphasisOurs = quote.markers().stream()
                        .anyMatch(m -> m.type() == BodyQuoteMarkerType.EMPHASIS_OURS);
                boolean hasEmphasisAuthor = quote.markers().stream()
                        .anyMatch(m -> m.type() == BodyQuoteMarkerType.EMPHASIS_AUTHOR);
                if (hasEmphasisOurs) {
                    runs.add(new DocxRun(
                            " (" + rule.citationFormatting().emphasisOursLabel() + ")",
                            baseStyle,
                            InlineFormatting.none()
                    ));
                } else if (hasEmphasisAuthor) {
                    runs.add(new DocxRun(
                            " (" + rule.citationFormatting().emphasisAuthorLabel() + ")",
                            baseStyle,
                            InlineFormatting.none()
                    ));
                }
                yield List.copyOf(runs);
            }
            case BodyCitationCall call -> {
                StyleRule citationStyle = styleResolver.resolve(
                        rule.styleMapping().styleIdForCitation(call.citationType())
                );
                yield List.of(new DocxRun(call.renderedText(), citationStyle, InlineFormatting.none()));
            }
            case BodyAbbreviation abbr -> {
                if (abbreviationMetas.stream().noneMatch(m -> m.abbreviation().equals(abbr.abbreviation()))) {
                    abbreviationMetas.add(new BodyAbbreviationMetadata(abbr.abbreviation(), abbr.expansion()));
                }
                yield List.of(new DocxRun(abbr.renderedText(), baseStyle, InlineFormatting.none()));
            }
            case BodyFootnote footnote -> {
                int fnId = ++footnoteCounter[0];
                StyleRule footnoteTextStyle = styleResolver.resolve(rule.styleMapping().footnoteTextStyleId());
                List<DocxRun> fnRuns = footnote.content().stream()
                        .flatMap(fi -> toDocxRun(fi, footnoteTextStyle, rule, styleResolver, abbreviationMetas, footnoteAccumulator, footnoteCounter, phase0Index).stream())
                        .toList();
                footnoteAccumulator.add(new com.abntbuilder.formatter.output.docx.api.DocxFootnoteContent(fnId, fnRuns));
                StyleRule footnoteCallStyle = styleResolver.resolve(rule.styleMapping().footnoteCallStyleId());
                yield List.of(new DocxRun("[FN:" + fnId + "]", footnoteCallStyle, InlineFormatting.none()));
            }
            case BodyCrossReference ref -> {
                String resolved = phase0Index.resolveCrossReference(
                        ref.targetId(),
                        ref.targetType(),
                        ref.displayMode(),
                        rule.crossReferenceLabels()
                );
                yield List.of(new DocxRun(resolved, baseStyle, InlineFormatting.none()));
            }
        };
    }

    private static List<DocxBlock> renderFigure(
            BodyFigure figure,
            FigureRule rule,
            StyleResolver styleResolver,
            DisplayObjectRenderingState<BodyFigure> figureRenderingState,
            DisplayObjectContinuationPart part
    ) {
        ResolvedImage resolvedImage = resolveImage(figure.image(), rule);
        List<DocxBlock> blocks = new ArrayList<>();

        StyleRule figureCaptionStyle = styleResolver.resolve(rule.captionStyleId());
        blocks.add(new DocxParagraph(
                List.of(DocxRun.of(resolveCaptionText(figure, rule, part), figureCaptionStyle)),
                figureCaptionStyle,
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                true,
                true
        ));
        blocks.add(renderImageDisplayObject(
                figure.image(),
                rule,
                shouldRenderSource(figure, rule, part, figureRenderingState)
        ));

        if (shouldRenderSource(figure, rule, part, figureRenderingState)) {
            String source = figureRenderingState.sourceFor(figure).orElseThrow();
            StyleRule figureSourceStyle = styleResolver.resolve(rule.sourceStyleId());
            blocks.add(new DocxParagraph(
                    List.of(DocxRun.of(rule.sourceTemplate().replace("{source}", source), figureSourceStyle)),
                    figureSourceStyle,
                    java.util.Optional.empty(),
                    java.util.Optional.empty(),
                    java.util.Optional.empty(),
                    false,
                    true
            ));
        }

        return List.copyOf(blocks);
    }

    private static String resolveCaptionText(BodyFigure figure, FigureRule rule, DisplayObjectContinuationPart part) {
        return resolveCaptionText(figure.caption(), rule.captionTemplate(), part);
    }

    private static boolean shouldRenderSource(
            BodyFigure figure,
            FigureRule rule,
            DisplayObjectContinuationPart part,
            DisplayObjectRenderingState<BodyFigure> figureRenderingState
    ) {
        if (figureRenderingState.sourceFor(figure).isEmpty()) {
            return false;
        }

        return switch (rule.sourcePlacement()) {
            case EVERY_PART -> true;
            case LAST_PART_ONLY -> part.last();
        };
    }

    private static List<DocxBlock> renderTable(
            BodyTable table,
            TableRule rule,
            StyleResolver styleResolver,
            DisplayObjectRenderingState<BodyTable> tableRenderingState,
            DisplayObjectContinuationPart part
    ) {
        boolean renderSource = shouldRenderSource(table, rule, part, tableRenderingState);
        List<DocxBlock> blocks = new ArrayList<>();

        StyleRule tableCaptionStyle = styleResolver.resolve(rule.captionStyleId());
        blocks.add(new DocxParagraph(
                List.of(DocxRun.of(resolveCaptionText(table.caption(), rule.captionTemplate(), part), tableCaptionStyle)),
                tableCaptionStyle,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                true,
                true
        ));
        blocks.add(new DocxTableBlock(
                table.columns().stream().map(column -> column.header()).toList(),
                table.rows().stream()
                        .map(row -> row.cells().stream()
                                .map(cell -> new com.abntbuilder.formatter.output.docx.api.DocxTableCell(
                                        cell.text(), cell.colspan(), cell.rowspanStart(), cell.rowspanContinuation()))
                                .toList())
                        .toList(),
                styleResolver.resolve(rule.headerStyleId()),
                styleResolver.resolve(rule.cellStyleId()),
                rule.widthPercent(),
                rule.tableAlignment(),
                rule.repeatHeaderOnPageBreak(),
                renderSource,
                true,
                TableBorderStyle.OPEN
        ));

        if (renderSource) {
            String source = tableRenderingState.sourceFor(table).orElseThrow();
            StyleRule tableSourceStyle = styleResolver.resolve(rule.sourceStyleId());
            blocks.add(new DocxParagraph(
                    List.of(DocxRun.of(rule.sourceTemplate().replace("{source}", source), tableSourceStyle)),
                    tableSourceStyle,
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    false,
                    true
            ));
        }

        return List.copyOf(blocks);
    }

    private static String resolveCaptionText(String captionText, String captionTemplate, DisplayObjectContinuationPart part) {
        String caption = captionTemplate
                .replace("{number}", String.valueOf(part.number()))
                .replace("{caption}", captionText);

        return part.continuationLabel()
                .map(label -> caption + " (" + label + ")")
                .orElse(caption);
    }

    private static boolean shouldRenderSource(
            BodyTable table,
            TableRule rule,
            DisplayObjectContinuationPart part,
            DisplayObjectRenderingState<BodyTable> tableRenderingState
    ) {
        if (tableRenderingState.sourceFor(table).isEmpty()) {
            return false;
        }

        return switch (rule.sourcePlacement()) {
            case EVERY_PART -> true;
            case LAST_PART_ONLY -> part.last();
        };
    }

    private static ResolvedImage resolveImage(BodyImageSource imageSource, FigureRule rule) {
        ResolvedImageBytes imageBytes = resolveImageBytes(imageSource, rule);

        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes.bytes()));
            if (image == null) {
                throw new IllegalArgumentException("figure.image must contain a supported image.");
            }

            BigDecimal widthCm = pixelsToCentimeters(image.getWidth(), rule.defaultDpi());
            BigDecimal heightCm = pixelsToCentimeters(image.getHeight(), rule.defaultDpi());
            BigDecimal scale = resolveScale(widthCm, heightCm, rule);

            return new ResolvedImage(
                    imageBytes.bytes(),
                    imageBytes.mimeType(),
                    widthCm.multiply(scale, MathContext.DECIMAL64),
                    heightCm.multiply(scale, MathContext.DECIMAL64)
            );
        } catch (Exception exception) {
            if (exception instanceof IllegalArgumentException illegalArgumentException) {
                throw illegalArgumentException;
            }

            throw new IllegalArgumentException("figure.image could not be read.", exception);
        }
    }

    private static ResolvedImageBytes resolveImageBytes(BodyImageSource imageSource, FigureRule rule) {
        return switch (imageSource.sourceType()) {
            case DATA_URI -> decodeDataUri(imageSource.dataUri(), rule.maxImageBytes());
            case URL -> fetchUrlImage(imageSource.url(), rule);
        };
    }

    private static ResolvedImageBytes decodeDataUri(String dataUri, int maxImageBytes) {
        String[] parts = dataUri.split(",", 2);
        if (parts.length != 2 || !parts[0].startsWith("data:image/") || !parts[0].endsWith(";base64")) {
            throw new IllegalArgumentException("figure.image.dataUri must be a base64 image data URI.");
        }

        byte[] bytes = Base64.getDecoder().decode(parts[1]);
        requireWithinMaxImageBytes(bytes.length, maxImageBytes);

        return new ResolvedImageBytes(parts[0].substring("data:".length(), parts[0].length() - ";base64".length()), bytes);
    }

    private static ResolvedImageBytes fetchUrlImage(String url, FigureRule rule) {
        URI uri = URI.create(url);
        String scheme = uri.getScheme();

        if (!"https".equalsIgnoreCase(scheme) && !"http".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("figure.image.url must use http or https.");
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(rule.urlFetchTimeoutSeconds()))
                    .build();
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(rule.urlFetchTimeoutSeconds()))
                    .header("User-Agent", "formatter-service/1.0")
                    .GET()
                    .build();
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalArgumentException("figure.image.url returned HTTP status " + response.statusCode() + ".");
            }

            String contentType = response.headers()
                    .firstValue("content-type")
                    .map(value -> value.split(";", 2)[0].trim())
                    .orElse("image/unknown");

            if (!contentType.startsWith("image/")) {
                throw new IllegalArgumentException("figure.image.url must return an image content type.");
            }

            return new ResolvedImageBytes(contentType, readLimited(response.body(), rule.maxImageBytes()));
        } catch (Exception exception) {
            if (exception instanceof IllegalArgumentException illegalArgumentException) {
                throw illegalArgumentException;
            }

            throw new IllegalArgumentException("figure.image.url could not be fetched.", exception);
        }
    }

    private static byte[] readLimited(InputStream inputStream, int maxImageBytes) throws java.io.IOException {
        try (inputStream; ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int totalBytes = 0;
            int readBytes;

            while ((readBytes = inputStream.read(buffer)) != -1) {
                totalBytes += readBytes;
                requireWithinMaxImageBytes(totalBytes, maxImageBytes);
                outputStream.write(buffer, 0, readBytes);
            }

            return outputStream.toByteArray();
        }
    }

    private static void requireWithinMaxImageBytes(int byteCount, int maxImageBytes) {
        if (byteCount > maxImageBytes) {
            throw new IllegalArgumentException("figure.image exceeds maxImageBytes.");
        }
    }

    private static BigDecimal pixelsToCentimeters(int pixels, BigDecimal defaultDpi) {
        return BigDecimal.valueOf(pixels)
                .divide(defaultDpi, MathContext.DECIMAL64)
                .multiply(BigDecimal.valueOf(2.54), MathContext.DECIMAL64);
    }

    private static BigDecimal resolveScale(BigDecimal widthCm, BigDecimal heightCm, FigureRule rule) {
        if (rule.fitPolicy() != ImageFitPolicy.SCALE_DOWN_PRESERVE_ASPECT_RATIO) {
            throw new IllegalArgumentException("Unsupported figure fit policy: " + rule.fitPolicy());
        }

        BigDecimal widthScale = rule.maxWidthCm().divide(widthCm, MathContext.DECIMAL64);
        BigDecimal heightScale = rule.maxHeightCm().divide(heightCm, MathContext.DECIMAL64);
        BigDecimal scale = widthScale.min(heightScale).min(BigDecimal.ONE);

        if (scale.signum() <= 0) {
            throw new IllegalArgumentException("figure image scale must be greater than zero.");
        }

        return scale;
    }

    private record ResolvedImageBytes(String mimeType, byte[] bytes) {
    }

    private record ResolvedImage(byte[] bytes, String mimeType, BigDecimal widthCm, BigDecimal heightCm) {
    }

    private static List<BodyFigure> figuresFrom(List<BodySection> sections) {
        List<BodyFigure> figures = new ArrayList<>();

        for (BodySection section : sections) {
            for (BodyBlock block : section.blocks()) {
                if (block instanceof BodyFigure figure) {
                    figures.add(figure);
                }
            }
        }

        return List.copyOf(figures);
    }

    private static List<BodyTable> tablesFrom(List<BodySection> sections) {
        List<BodyTable> tables = new ArrayList<>();

        for (BodySection section : sections) {
            for (BodyBlock block : section.blocks()) {
                if (block instanceof BodyTable table) {
                    tables.add(table);
                }
            }
        }
        return List.copyOf(tables);
    }

    private static List<BodyFrame> framesFrom(List<BodySection> sections) {
        List<BodyFrame> frames = new ArrayList<>();
        for (BodySection section : sections) {
            for (BodyBlock block : section.blocks()) {
                if (block instanceof BodyFrame frame) {
                    frames.add(frame);
                }
            }
        }
        return List.copyOf(frames);
    }

    private static List<BodyCodeListing> codeListingsFrom(List<BodySection> sections) {
        List<BodyCodeListing> codeListings = new ArrayList<>();
        for (BodySection section : sections) {
            for (BodyBlock block : section.blocks()) {
                if (block instanceof BodyCodeListing codeListing) {
                    codeListings.add(codeListing);
                }
            }
        }
        return List.copyOf(codeListings);
    }

    private static List<BodyChart> chartsFrom(List<BodySection> sections) {
        List<BodyChart> charts = new ArrayList<>();
        for (BodySection section : sections) {
            for (BodyBlock block : section.blocks()) {
                if (block instanceof BodyChart chart) {
                    charts.add(chart);
                }
            }
        }
        return List.copyOf(charts);
    }

    private static boolean shouldRenderSource(
            BodyFrame frame,
            FrameRule rule,
            DisplayObjectContinuationPart part,
            DisplayObjectRenderingState<BodyFrame> frameRenderingState
    ) {
        if (frameRenderingState.sourceFor(frame).isEmpty()) {
            return false;
        }

        return switch (rule.sourcePlacement()) {
            case EVERY_PART -> true;
            case LAST_PART_ONLY -> part.last();
        };
    }

    private static List<DocxBlock> renderFrame(
            BodyFrame frame,
            FrameRule rule,
            StyleResolver styleResolver,
            DisplayObjectRenderingState<BodyFrame> frameRenderingState,
            DisplayObjectContinuationPart part
    ) {
        boolean renderSource = shouldRenderSource(frame, rule, part, frameRenderingState);
        List<DocxBlock> blocks = new ArrayList<>();

        StyleRule captionStyle = styleResolver.resolve(rule.captionStyleId());
        blocks.add(new DocxParagraph(
                List.of(DocxRun.of(resolveCaptionText(frame.caption(), rule.captionTemplate(), part), captionStyle)),
                captionStyle,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                true,
                true
        ));
        blocks.add(new DocxTableBlock(
                frame.columns().stream().map(column -> column.header()).toList(),
                frame.rows().stream()
                        .map(row -> row.cells().stream()
                                .map(cell -> new com.abntbuilder.formatter.output.docx.api.DocxTableCell(
                                        cell.text(), cell.colspan(), cell.rowspanStart(), cell.rowspanContinuation()))
                                .toList())
                        .toList(),
                styleResolver.resolve(rule.headerStyleId()),
                styleResolver.resolve(rule.cellStyleId()),
                rule.widthPercent(),
                rule.tableAlignment(),
                rule.repeatHeaderOnPageBreak(),
                renderSource,
                true,
                TableBorderStyle.CLOSED
        ));

        if (renderSource) {
            String source = frameRenderingState.sourceFor(frame).orElseThrow();
            StyleRule sourceStyle = styleResolver.resolve(rule.sourceStyleId());
            blocks.add(new DocxParagraph(
                    List.of(DocxRun.of(rule.sourceTemplate().replace("{source}", source), sourceStyle)),
                    sourceStyle,
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    false,
                    true
            ));
        }

        return List.copyOf(blocks);
    }

    private static DocxImageBlock renderImageDisplayObject(
            BodyImageSource imageSource,
            FigureRule figureRule,
            boolean renderSource
    ) {
        ResolvedImage resolved = resolveImage(imageSource, figureRule);
        return new DocxImageBlock(
                resolved.bytes(),
                resolved.mimeType(),
                imageSource.altText(),
                resolved.widthCm(),
                resolved.heightCm(),
                figureRule.imageAlignment(),
                renderSource,
                true
        );
    }

    private static List<DocxBlock> renderCodeListing(
            BodyCodeListing codeListing,
            CodeListingRule rule,
            StyleResolver styleResolver,
            DisplayObjectRenderingState<BodyCodeListing> codeListingRenderingState,
            DisplayObjectContinuationPart part
    ) {
        List<DocxBlock> blocks = new ArrayList<>();
        StyleRule captionStyle = styleResolver.resolve(rule.captionStyleId());
        blocks.add(new DocxParagraph(
                List.of(DocxRun.of(resolveCaptionText(codeListing.caption(), rule.captionTemplate(), part), captionStyle)),
                captionStyle, java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty(), true, true
        ));
        StyleRule codeStyle = styleResolver.resolve(rule.codeStyleId());
        String[] lines = codeListing.code().split("\n", -1);
        for (String line : lines) {
            if (line.isBlank()) {
                blocks.add(new DocxBlankLine(codeStyle));
            } else {
                blocks.add(new DocxParagraph(List.of(DocxRun.of(line, codeStyle)), codeStyle));
            }
        }
        if (shouldRenderSource(codeListing, rule, part, codeListingRenderingState)) {
            String source = codeListingRenderingState.sourceFor(codeListing).orElseThrow();
            StyleRule sourceStyle = styleResolver.resolve(rule.sourceStyleId());
            blocks.add(new DocxParagraph(
                    List.of(DocxRun.of(rule.sourceTemplate().replace("{source}", source), sourceStyle)),
                    sourceStyle, java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty(), false, true
            ));
        }
        return List.copyOf(blocks);
    }

    private static boolean shouldRenderSource(
            BodyCodeListing codeListing,
            CodeListingRule rule,
            DisplayObjectContinuationPart part,
            DisplayObjectRenderingState<BodyCodeListing> codeListingRenderingState
    ) {
        if (codeListingRenderingState.sourceFor(codeListing).isEmpty()) {
            return false;
        }
        return switch (rule.sourcePlacement()) {
            case EVERY_PART -> true;
            case LAST_PART_ONLY -> part.last();
        };
    }

    private static List<DocxBlock> renderChart(
            BodyChart chart,
            ChartRule rule,
            StyleResolver styleResolver,
            DisplayObjectRenderingState<BodyChart> chartRenderingState,
            DisplayObjectContinuationPart part
    ) {
        List<DocxBlock> blocks = new ArrayList<>();
        StyleRule captionStyle = styleResolver.resolve(rule.captionStyleId());
        blocks.add(new DocxParagraph(
                List.of(DocxRun.of(resolveCaptionText(chart.caption(), rule.captionTemplate(), part), captionStyle)),
                captionStyle, java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty(), true, true
        ));
        boolean renderSource = shouldRenderSource(chart, rule, part, chartRenderingState);
        blocks.add(renderImageDisplayObject(
                chart.image(),
                rule.imageRule(),
                renderSource
        ));

        if (renderSource) {
            String source = chartRenderingState.sourceFor(chart).orElseThrow();
            StyleRule sourceStyle = styleResolver.resolve(rule.sourceStyleId());
            blocks.add(new DocxParagraph(
                    List.of(DocxRun.of(rule.sourceTemplate().replace("{source}", source), sourceStyle)),
                    sourceStyle, java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty(), false, true
            ));
        }
        return List.copyOf(blocks);
    }

    private static boolean shouldRenderSource(
            BodyChart chart,
            ChartRule rule,
            DisplayObjectContinuationPart part,
            DisplayObjectRenderingState<BodyChart> chartRenderingState
    ) {
        if (chartRenderingState.sourceFor(chart).isEmpty()) {
            return false;
        }
        return switch (rule.sourcePlacement()) {
            case EVERY_PART -> true;
            case LAST_PART_ONLY -> part.last();
        };
    }



}
