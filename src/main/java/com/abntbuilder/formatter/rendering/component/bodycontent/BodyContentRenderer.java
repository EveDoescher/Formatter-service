package com.abntbuilder.formatter.rendering.component.bodycontent;

import com.abntbuilder.formatter.document.component.bodycontent.BodyContentComponent;
import com.abntbuilder.formatter.document.component.bodycontent.BodyBlock;
import com.abntbuilder.formatter.document.component.bodycontent.BodyCitation;
import com.abntbuilder.formatter.document.component.bodycontent.BodyFigure;
import com.abntbuilder.formatter.document.component.bodycontent.BodyImageSource;
import com.abntbuilder.formatter.document.component.bodycontent.ImageSourceType;
import com.abntbuilder.formatter.document.component.bodycontent.BodyParagraph;
import com.abntbuilder.formatter.document.component.bodycontent.BodySection;
import com.abntbuilder.formatter.document.component.bodycontent.BodyTable;
import com.abntbuilder.formatter.output.docx.api.DocxBlankLine;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxImageBlock;
import com.abntbuilder.formatter.output.docx.api.DocxPageBreak;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.output.docx.api.DocxTableBlock;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.BodyContentComponentRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.BodyContentNumberingRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.DisplayObjectSourcePlacement;
import com.abntbuilder.formatter.profile.model.component.bodycontent.FigureRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.ImageFitPolicy;
import com.abntbuilder.formatter.profile.model.component.bodycontent.TableRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;

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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class BodyContentRenderer implements ComponentRenderer<BodyContentComponent> {

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
    public List<DocxBlock> render(BodyContentComponent component, DocumentProfile profile) {
        Objects.requireNonNull(component, "component must not be null");
        Objects.requireNonNull(profile, "profile must not be null");

        BodyContentComponentRule rule = bodyContentRule(profile);
        StyleResolver styleResolver = new StyleResolver(profile);
        List<DocxBlock> blocks = new ArrayList<>();
        SectionNumberingState numberingState = new SectionNumberingState(rule.numbering());
        StyleRule blankLineStyle = styleResolver.resolve(rule.layout().blankLineStyleId());
        boolean previousRenderedTextWasBodyParagraph = false;
        DisplayObjectRenderingState<BodyFigure> figureRenderingState = new DisplayObjectRenderingState<>(
                figuresFrom(component.sections())
        );
        DisplayObjectRenderingState<BodyTable> tableRenderingState = new DisplayObjectRenderingState<>(
                tablesFrom(component.sections())
        );

        for (BodySection section : component.sections()) {
            if (section.title().isPresent()) {
                if (rule.layout().pageBreakBeforePrimarySection() && section.level() == 1 && !blocks.isEmpty()) {
                    blocks.add(new DocxPageBreak());
                } else if (previousRenderedTextWasBodyParagraph) {
                    addBlankLines(
                            blocks,
                            blankLineStyle,
                            rule.layout().blankLinesBeforeSectionTitleWhenPrecededByContent()
                    );
                }

                StyleRule titleStyle = styleResolver.resolve(rule.styleMapping().sectionTitleStyleIdForLevel(section.level()));
                blocks.add(new DocxParagraph(
                        List.of(DocxRun.of(numberingState.resolveTitle(section.level(), section.title().orElseThrow()), titleStyle)),
                        titleStyle
                ));
                previousRenderedTextWasBodyParagraph = false;

                addBlankLines(blocks, blankLineStyle, rule.layout().blankLinesAfterSectionTitle());
            }

            for (BodyBlock contentBlock : section.blocks()) {
                blocks.addAll(renderContentBlock(
                        contentBlock,
                        rule,
                        styleResolver,
                        figureRenderingState,
                        tableRenderingState
                ));
                previousRenderedTextWasBodyParagraph = true;
            }
        }

        return List.copyOf(blocks);
    }

    private static BodyContentComponentRule bodyContentRule(DocumentProfile profile) {
        return new ComponentRuleResolver(profile).resolve(COMPONENT_ID, BodyContentComponentRule.class);
    }

    private static void addBlankLines(List<DocxBlock> blocks, StyleRule styleRule, int count) {
        for (int index = 0; index < count; index++) {
            blocks.add(new DocxBlankLine(styleRule));
        }
    }

    private static List<DocxBlock> renderContentBlock(
            BodyBlock contentBlock,
            BodyContentComponentRule rule,
            StyleResolver styleResolver,
            DisplayObjectRenderingState<BodyFigure> figureRenderingState,
            DisplayObjectRenderingState<BodyTable> tableRenderingState
    ) {
        return switch (contentBlock) {
            case BodyParagraph paragraph -> {
                StyleRule paragraphStyle = styleResolver.resolve(rule.styleMapping().paragraphStyleId());
                yield List.of(new DocxParagraph(
                        List.of(DocxRun.of(paragraph.text(), paragraphStyle)),
                        paragraphStyle
                ));
            }
            case BodyCitation citation -> {
                StyleRule citationStyle = styleResolver.resolve(rule.styleMapping().styleIdForCitation(citation.type()));
                yield List.of(new DocxParagraph(
                        List.of(DocxRun.of(citation.renderedText(), citationStyle)),
                        citationStyle
                ));
            }
            case BodyFigure figure -> renderFigure(figure, rule.figure(), styleResolver, figureRenderingState);
            case BodyTable table -> renderTable(table, rule.table(), styleResolver, tableRenderingState);
        };
    }

    private static List<DocxBlock> renderFigure(
            BodyFigure figure,
            FigureRule rule,
            StyleResolver styleResolver,
            DisplayObjectRenderingState<BodyFigure> figureRenderingState
    ) {
        DisplayObjectContinuationPart part = figureRenderingState.nextPart(figure, rule.continuationLabels());
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
        blocks.add(new DocxImageBlock(
                resolvedImage.bytes(),
                resolvedImage.mimeType(),
                figure.image().altText(),
                resolvedImage.widthCm(),
                resolvedImage.heightCm(),
                rule.imageAlignment(),
                shouldRenderSource(figure, rule, part, figureRenderingState),
                true
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
            DisplayObjectRenderingState<BodyTable> tableRenderingState
    ) {
        DisplayObjectContinuationPart part = tableRenderingState.nextPart(table, rule.continuationLabels());
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
                table.rows().stream().map(row -> row.cells()).toList(),
                styleResolver.resolve(rule.headerStyleId()),
                styleResolver.resolve(rule.cellStyleId()),
                rule.widthPercent(),
                rule.tableAlignment(),
                rule.repeatHeaderOnPageBreak(),
                renderSource,
                true
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

    private static final class SectionNumberingState {

        private static final int MAX_LEVEL = 6;

        private final BodyContentNumberingRule numberingRule;
        private final int[] counters = new int[MAX_LEVEL];

        private SectionNumberingState(BodyContentNumberingRule numberingRule) {
            this.numberingRule = Objects.requireNonNull(numberingRule, "numberingRule must not be null");
        }

        private String resolveTitle(int level, String title) {
            if (!numberingRule.enabled()) {
                return title;
            }

            increment(level);

            return sectionNumber(level) + " " + title;
        }

        private void increment(int level) {
            int index = level - 1;

            for (int currentIndex = 0; currentIndex < index; currentIndex++) {
                if (counters[currentIndex] == 0) {
                    counters[currentIndex] = 1;
                }
            }

            counters[index]++;

            for (int currentIndex = index + 1; currentIndex < counters.length; currentIndex++) {
                counters[currentIndex] = 0;
            }
        }

        private String sectionNumber(int level) {
            if (level == 1) {
                return counters[0] + numberingRule.primarySuffix();
            }

            List<String> parts = new ArrayList<>();

            for (int index = 0; index < level; index++) {
                parts.add(String.valueOf(counters[index]));
            }

            return String.join(numberingRule.separator(), parts);
        }
    }
}
