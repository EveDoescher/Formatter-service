package com.abntbuilder.formatter.rendering.bodycontent;

import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyBlock;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyChart;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyCodeListing;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyEquation;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyFigure;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyFrame;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyImageSource;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyList;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyListType;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyLongQuote;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyParagraph;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyTable;
import com.abntbuilder.formatter.engine.model.content.bodycontent.ImageSourceType;
import com.abntbuilder.formatter.engine.model.output.DocxBlankLine;
import com.abntbuilder.formatter.engine.model.output.DocxBlock;
import com.abntbuilder.formatter.engine.model.output.DocxBookmarkParagraph;
import com.abntbuilder.formatter.engine.model.output.DocxFootnoteContent;
import com.abntbuilder.formatter.engine.model.output.DocxFootnoteReferenceBlock;
import com.abntbuilder.formatter.engine.model.output.DocxImageBlock;
import com.abntbuilder.formatter.engine.model.output.DocxListItemParagraph;
import com.abntbuilder.formatter.engine.model.output.DocxParagraph;
import com.abntbuilder.formatter.engine.model.output.DocxRun;
import com.abntbuilder.formatter.engine.model.output.DocxTableBlock;
import com.abntbuilder.formatter.engine.model.output.DocxTableCell;
import com.abntbuilder.formatter.engine.model.output.TableBorderStyle;
import com.abntbuilder.formatter.engine.model.profile.StyleRule;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.ChartRule;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.CodeListingRule;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.DisplayObjectSourcePlacement;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.FigureRule;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.FrameRule;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.ImageFitPolicy;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.TableRule;
import com.abntbuilder.formatter.rendering.bodycontent.DisplayObjectContinuationPart;
import com.abntbuilder.formatter.rendering.bodycontent.DisplayObjectRenderingState;
import com.abntbuilder.formatter.rendering.bodycontent.BodyDisplayObjectMetadata;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

final class TextTypeRegistry {

    static List<DocxBlock> dispatch(BodyBlock block, FlowRenderingContext ctx) {
        return switch (block) {
            case BodyParagraph paragraph -> renderParagraph(paragraph, ctx);
            case BodyLongQuote longQuote -> renderLongQuote(longQuote, ctx);
            case BodyFigure figure -> renderFigure(figure, ctx);
            case BodyTable table -> renderTable(table, ctx);
            case BodyList list -> renderList(list, ctx, 0);
            case BodyFrame frame -> renderFrame(frame, ctx);
            case BodyCodeListing codeListing -> renderCodeListing(codeListing, ctx);
            case BodyChart chart -> renderChart(chart, ctx);
            case BodyEquation equation -> renderEquation(equation, ctx);
        };
    }

    // --- Block renderers ---

    private static List<DocxBlock> renderParagraph(BodyParagraph paragraph, FlowRenderingContext ctx) {
        StyleRule paragraphStyle = ctx.styleResolver.resolve(ctx.rule.styleMapping().paragraphStyleId());
        List<DocxFootnoteContent> footnoteAccumulator = new ArrayList<>();
        List<DocxRun> runs = RunProcessor.processAll(paragraph.content(), paragraphStyle, ctx, footnoteAccumulator);
        DocxParagraph docxParagraph = new DocxParagraph(runs, paragraphStyle);
        if (!footnoteAccumulator.isEmpty()) {
            return List.of(new DocxFootnoteReferenceBlock(docxParagraph, footnoteAccumulator));
        }
        return List.of(docxParagraph);
    }

    private static List<DocxBlock> renderLongQuote(BodyLongQuote longQuote, FlowRenderingContext ctx) {
        StyleRule longQuoteStyle = ctx.styleResolver.resolve(ctx.rule.styleMapping().directLongQuoteStyleId());
        return List.of(new DocxParagraph(
                List.of(DocxRun.of(longQuote.renderedText(ctx.rule.citationFormatting()), longQuoteStyle)),
                longQuoteStyle
        ));
    }

    private static List<DocxBlock> renderFigure(BodyFigure figure, FlowRenderingContext ctx) {
        FigureRule rule = ctx.rule.figure();
        DisplayObjectContinuationPart part = ctx.figureState.nextPart(figure, rule.continuationLabels());
        if (part.continuationLabel().isEmpty()) {
            ctx.figureMetas.add(new BodyDisplayObjectMetadata(figure.id(), part.number(), figure.caption()));
        }
        return renderFigureBlocks(figure, rule, ctx, part);
    }

    private static List<DocxBlock> renderFigureBlocks(
            BodyFigure figure, FigureRule rule, FlowRenderingContext ctx, DisplayObjectContinuationPart part) {
        boolean renderSource = shouldRenderSource(figure, rule, part, ctx.figureState);
        List<DocxBlock> blocks = new ArrayList<>();
        StyleRule captionStyle = ctx.styleResolver.resolve(rule.captionStyleId());
        String captionText = resolveCaptionText(figure.caption(), rule.captionTemplate(), part);
        if (part.continuationLabel().isEmpty()) {
            blocks.add(new DocxBookmarkParagraph(
                    List.of(DocxRun.of(captionText, captionStyle)), captionStyle, "elem_" + figure.id()));
        } else {
            blocks.add(new DocxParagraph(
                    List.of(DocxRun.of(captionText, captionStyle)),
                    captionStyle, Optional.empty(), Optional.empty(), Optional.empty(), true, true));
        }
        blocks.add(renderImageBlock(figure.image(), rule, renderSource));
        if (renderSource) {
            String source = ctx.figureState.sourceFor(figure).orElseThrow();
            StyleRule sourceStyle = ctx.styleResolver.resolve(rule.sourceStyleId());
            blocks.add(new DocxParagraph(
                    List.of(DocxRun.of(rule.sourceTemplate().replace("{source}", source), sourceStyle)),
                    sourceStyle, Optional.empty(), Optional.empty(), Optional.empty(), false, true
            ));
        }
        return List.copyOf(blocks);
    }

    private static List<DocxBlock> renderTable(BodyTable table, FlowRenderingContext ctx) {
        TableRule rule = ctx.rule.table();
        DisplayObjectContinuationPart part = ctx.tableState.nextPart(table, rule.continuationLabels());
        if (part.continuationLabel().isEmpty()) {
            ctx.tableMetas.add(new BodyDisplayObjectMetadata(table.id(), part.number(), table.caption()));
        }
        boolean renderSource = shouldRenderSource(table, rule, part, ctx.tableState);
        List<DocxBlock> blocks = new ArrayList<>();
        StyleRule captionStyle = ctx.styleResolver.resolve(rule.captionStyleId());
        String tableCaptionText = resolveCaptionText(table.caption(), rule.captionTemplate(), part);
        if (part.continuationLabel().isEmpty()) {
            blocks.add(new DocxBookmarkParagraph(
                    List.of(DocxRun.of(tableCaptionText, captionStyle)), captionStyle, "elem_" + table.id()));
        } else {
            blocks.add(new DocxParagraph(
                    List.of(DocxRun.of(tableCaptionText, captionStyle)),
                    captionStyle, Optional.empty(), Optional.empty(), Optional.empty(), true, true));
        }
        blocks.add(new DocxTableBlock(
                table.columns().stream().map(c -> c.header()).toList(),
                table.rows().stream()
                        .map(row -> row.cells().stream()
                                .map(cell -> new DocxTableCell(
                                        cell.text(), cell.colspan(), cell.rowspanStart(), cell.rowspanContinuation()))
                                .toList())
                        .toList(),
                ctx.styleResolver.resolve(rule.headerStyleId()),
                ctx.styleResolver.resolve(rule.cellStyleId()),
                rule.widthPercent(),
                rule.tableAlignment(),
                rule.repeatHeaderOnPageBreak(),
                renderSource,
                true,
                TableBorderStyle.OPEN
        ));
        if (renderSource) {
            String source = ctx.tableState.sourceFor(table).orElseThrow();
            StyleRule sourceStyle = ctx.styleResolver.resolve(rule.sourceStyleId());
            blocks.add(new DocxParagraph(
                    List.of(DocxRun.of(rule.sourceTemplate().replace("{source}", source), sourceStyle)),
                    sourceStyle, Optional.empty(), Optional.empty(), Optional.empty(), false, true
            ));
        }
        return List.copyOf(blocks);
    }

    private static List<DocxBlock> renderList(BodyList list, FlowRenderingContext ctx, int nestingLevel) {
        StyleRule itemStyle = ctx.styleResolver.resolve(
                list.type() == BodyListType.ORDERED
                        ? ctx.rule.styleMapping().listOrderedStyleId()
                        : ctx.rule.styleMapping().listUnorderedStyleId()
        );
        List<DocxBlock> blocks = new ArrayList<>();
        for (var item : list.items()) {
            List<DocxFootnoteContent> footnoteAccumulator = new ArrayList<>();
            List<DocxRun> runs = RunProcessor.processAll(item.content(), itemStyle, ctx, footnoteAccumulator);
            DocxBlock listItem = new DocxListItemParagraph(runs, itemStyle, list.type(), nestingLevel);
            if (!footnoteAccumulator.isEmpty()) {
                blocks.add(new DocxFootnoteReferenceBlock(listItem, footnoteAccumulator));
            } else {
                blocks.add(listItem);
            }
            item.subList().ifPresent(subList -> blocks.addAll(renderList(subList, ctx, nestingLevel + 1)));
        }
        return blocks;
    }

    private static List<DocxBlock> renderFrame(BodyFrame frame, FlowRenderingContext ctx) {
        FrameRule rule = ctx.rule.frame();
        DisplayObjectContinuationPart part = ctx.frameState.nextPart(frame, rule.continuationLabels());
        if (part.continuationLabel().isEmpty()) {
            ctx.frameMetas.add(new BodyDisplayObjectMetadata(frame.id(), part.number(), frame.caption()));
        }
        boolean renderSource = shouldRenderSource(frame, rule, part, ctx.frameState);
        List<DocxBlock> blocks = new ArrayList<>();
        StyleRule captionStyle = ctx.styleResolver.resolve(rule.captionStyleId());
        String frameCaptionText = resolveCaptionText(frame.caption(), rule.captionTemplate(), part);
        if (part.continuationLabel().isEmpty()) {
            blocks.add(new DocxBookmarkParagraph(
                    List.of(DocxRun.of(frameCaptionText, captionStyle)), captionStyle, "elem_" + frame.id()));
        } else {
            blocks.add(new DocxParagraph(
                    List.of(DocxRun.of(frameCaptionText, captionStyle)),
                    captionStyle, Optional.empty(), Optional.empty(), Optional.empty(), true, true));
        }
        blocks.add(new DocxTableBlock(
                frame.columns().stream().map(c -> c.header()).toList(),
                frame.rows().stream()
                        .map(row -> row.cells().stream()
                                .map(cell -> new DocxTableCell(
                                        cell.text(), cell.colspan(), cell.rowspanStart(), cell.rowspanContinuation()))
                                .toList())
                        .toList(),
                ctx.styleResolver.resolve(rule.headerStyleId()),
                ctx.styleResolver.resolve(rule.cellStyleId()),
                rule.widthPercent(),
                rule.tableAlignment(),
                rule.repeatHeaderOnPageBreak(),
                renderSource,
                true,
                TableBorderStyle.CLOSED
        ));
        if (renderSource) {
            String source = ctx.frameState.sourceFor(frame).orElseThrow();
            StyleRule sourceStyle = ctx.styleResolver.resolve(rule.sourceStyleId());
            blocks.add(new DocxParagraph(
                    List.of(DocxRun.of(rule.sourceTemplate().replace("{source}", source), sourceStyle)),
                    sourceStyle, Optional.empty(), Optional.empty(), Optional.empty(), false, true
            ));
        }
        return List.copyOf(blocks);
    }

    private static List<DocxBlock> renderCodeListing(BodyCodeListing codeListing, FlowRenderingContext ctx) {
        CodeListingRule rule = ctx.rule.codeListing();
        DisplayObjectContinuationPart part = ctx.codeListingState.nextPart(codeListing, rule.continuationLabels());
        if (part.continuationLabel().isEmpty()) {
            ctx.codeListingMetas.add(new BodyDisplayObjectMetadata(codeListing.id(), part.number(), codeListing.caption()));
        }
        List<DocxBlock> blocks = new ArrayList<>();
        StyleRule captionStyle = ctx.styleResolver.resolve(rule.captionStyleId());
        String codeListingCaptionText = resolveCaptionText(codeListing.caption(), rule.captionTemplate(), part);
        if (part.continuationLabel().isEmpty()) {
            blocks.add(new DocxBookmarkParagraph(
                    List.of(DocxRun.of(codeListingCaptionText, captionStyle)), captionStyle, "elem_" + codeListing.id()));
        } else {
            blocks.add(new DocxParagraph(
                    List.of(DocxRun.of(codeListingCaptionText, captionStyle)),
                    captionStyle, Optional.empty(), Optional.empty(), Optional.empty(), true, true));
        }
        StyleRule codeStyle = ctx.styleResolver.resolve(rule.codeStyleId());
        for (String line : codeListing.code().split("\n", -1)) {
            if (line.isBlank()) {
                blocks.add(new DocxBlankLine(codeStyle));
            } else {
                blocks.add(new DocxParagraph(List.of(DocxRun.of(line, codeStyle)), codeStyle));
            }
        }
        if (shouldRenderSource(codeListing, rule, part, ctx.codeListingState)) {
            String source = ctx.codeListingState.sourceFor(codeListing).orElseThrow();
            StyleRule sourceStyle = ctx.styleResolver.resolve(rule.sourceStyleId());
            blocks.add(new DocxParagraph(
                    List.of(DocxRun.of(rule.sourceTemplate().replace("{source}", source), sourceStyle)),
                    sourceStyle, Optional.empty(), Optional.empty(), Optional.empty(), false, true
            ));
        }
        return List.copyOf(blocks);
    }

    private static List<DocxBlock> renderChart(BodyChart chart, FlowRenderingContext ctx) {
        ChartRule rule = ctx.rule.chart();
        DisplayObjectContinuationPart part = ctx.chartState.nextPart(chart, rule.continuationLabels());
        if (part.continuationLabel().isEmpty()) {
            ctx.chartMetas.add(new BodyDisplayObjectMetadata(chart.id(), part.number(), chart.caption()));
        }
        boolean renderSource = shouldRenderSource(chart, rule, part, ctx.chartState);
        List<DocxBlock> blocks = new ArrayList<>();
        StyleRule captionStyle = ctx.styleResolver.resolve(rule.captionStyleId());
        String chartCaptionText = resolveCaptionText(chart.caption(), rule.captionTemplate(), part);
        if (part.continuationLabel().isEmpty()) {
            blocks.add(new DocxBookmarkParagraph(
                    List.of(DocxRun.of(chartCaptionText, captionStyle)), captionStyle, "elem_" + chart.id()));
        } else {
            blocks.add(new DocxParagraph(
                    List.of(DocxRun.of(chartCaptionText, captionStyle)),
                    captionStyle, Optional.empty(), Optional.empty(), Optional.empty(), true, true));
        }
        blocks.add(renderImageBlock(chart.image(), rule.imageRule(), renderSource));
        if (renderSource) {
            String source = ctx.chartState.sourceFor(chart).orElseThrow();
            StyleRule sourceStyle = ctx.styleResolver.resolve(rule.sourceStyleId());
            blocks.add(new DocxParagraph(
                    List.of(DocxRun.of(rule.sourceTemplate().replace("{source}", source), sourceStyle)),
                    sourceStyle, Optional.empty(), Optional.empty(), Optional.empty(), false, true
            ));
        }
        return List.copyOf(blocks);
    }

    private static List<DocxBlock> renderEquation(BodyEquation equation, FlowRenderingContext ctx) {
        StyleRule equationStyle = ctx.styleResolver.resolve(ctx.rule.styleMapping().equationStyleId());
        return List.of(new DocxParagraph(
                List.of(DocxRun.of(equation.text(), equationStyle)),
                equationStyle
        ));
    }

    // --- shouldRenderSource overloads ---

    private static boolean shouldRenderSource(
            BodyFigure figure, FigureRule rule,
            DisplayObjectContinuationPart part,
            DisplayObjectRenderingState<BodyFigure> state
    ) {
        if (state.sourceFor(figure).isEmpty()) return false;
        return switch (rule.sourcePlacement()) {
            case EVERY_PART -> true;
            case LAST_PART_ONLY -> part.last();
        };
    }

    private static boolean shouldRenderSource(
            BodyTable table, TableRule rule,
            DisplayObjectContinuationPart part,
            DisplayObjectRenderingState<BodyTable> state
    ) {
        if (state.sourceFor(table).isEmpty()) return false;
        return switch (rule.sourcePlacement()) {
            case EVERY_PART -> true;
            case LAST_PART_ONLY -> part.last();
        };
    }

    private static boolean shouldRenderSource(
            BodyFrame frame, FrameRule rule,
            DisplayObjectContinuationPart part,
            DisplayObjectRenderingState<BodyFrame> state
    ) {
        if (state.sourceFor(frame).isEmpty()) return false;
        return switch (rule.sourcePlacement()) {
            case EVERY_PART -> true;
            case LAST_PART_ONLY -> part.last();
        };
    }

    private static boolean shouldRenderSource(
            BodyCodeListing codeListing, CodeListingRule rule,
            DisplayObjectContinuationPart part,
            DisplayObjectRenderingState<BodyCodeListing> state
    ) {
        if (state.sourceFor(codeListing).isEmpty()) return false;
        return switch (rule.sourcePlacement()) {
            case EVERY_PART -> true;
            case LAST_PART_ONLY -> part.last();
        };
    }

    private static boolean shouldRenderSource(
            BodyChart chart, ChartRule rule,
            DisplayObjectContinuationPart part,
            DisplayObjectRenderingState<BodyChart> state
    ) {
        if (state.sourceFor(chart).isEmpty()) return false;
        return switch (rule.sourcePlacement()) {
            case EVERY_PART -> true;
            case LAST_PART_ONLY -> part.last();
        };
    }

    // --- Caption helpers ---

    static String resolveCaptionText(String captionText, String captionTemplate, DisplayObjectContinuationPart part) {
        String caption = captionTemplate
                .replace("{number}", String.valueOf(part.number()))
                .replace("{caption}", captionText);
        return part.continuationLabel()
                .map(label -> caption + " (" + label + ")")
                .orElse(caption);
    }

    // --- Image rendering ---

    private static DocxImageBlock renderImageBlock(
            BodyImageSource imageSource, FigureRule figureRule, boolean renderSource) {
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
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("figure.image could not be read.", e);
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
        return new ResolvedImageBytes(
                parts[0].substring("data:".length(), parts[0].length() - ";base64".length()),
                bytes
        );
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
                throw new IllegalArgumentException(
                        "figure.image.url returned HTTP status " + response.statusCode() + ".");
            }
            String contentType = response.headers()
                    .firstValue("content-type")
                    .map(value -> value.split(";", 2)[0].trim())
                    .orElse("image/unknown");
            if (!contentType.startsWith("image/")) {
                throw new IllegalArgumentException("figure.image.url must return an image content type.");
            }
            return new ResolvedImageBytes(contentType, readLimited(response.body(), rule.maxImageBytes()));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("figure.image.url could not be fetched.", e);
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

    private record ResolvedImageBytes(String mimeType, byte[] bytes) {}
    private record ResolvedImage(byte[] bytes, String mimeType, BigDecimal widthCm, BigDecimal heightCm) {}
}
