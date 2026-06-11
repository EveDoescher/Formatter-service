package com.abntbuilder.formatter.rendering.component.bodycontent;

import com.abntbuilder.formatter.document.component.bodycontent.BodyContentComponent;
import com.abntbuilder.formatter.document.component.bodycontent.BodyBlock;
import com.abntbuilder.formatter.document.component.bodycontent.BodyCitation;
import com.abntbuilder.formatter.document.component.bodycontent.BodyFigure;
import com.abntbuilder.formatter.document.component.bodycontent.BodyImageSource;
import com.abntbuilder.formatter.document.component.bodycontent.ImageSourceType;
import com.abntbuilder.formatter.document.component.bodycontent.BodyParagraph;
import com.abntbuilder.formatter.document.component.bodycontent.BodySection;
import com.abntbuilder.formatter.output.docx.api.DocxBlankLine;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxImageBlock;
import com.abntbuilder.formatter.output.docx.api.DocxPageBreak;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.BodyContentComponentRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.BodyContentNumberingRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.DisplayObjectSourcePlacement;
import com.abntbuilder.formatter.profile.model.component.bodycontent.FigureRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.ImageFitPolicy;
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
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        FigureRenderingState figureRenderingState = new FigureRenderingState(component.sections());

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

                blocks.add(new DocxParagraph(
                        numberingState.resolveTitle(section.level(), section.title().orElseThrow()),
                        styleResolver.resolve(rule.styleMapping().sectionTitleStyleIdForLevel(section.level()))
                ));
                previousRenderedTextWasBodyParagraph = false;

                addBlankLines(blocks, blankLineStyle, rule.layout().blankLinesAfterSectionTitle());
            }

            for (BodyBlock contentBlock : section.blocks()) {
                blocks.addAll(renderContentBlock(contentBlock, rule, styleResolver, figureRenderingState));
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
            FigureRenderingState figureRenderingState
    ) {
        return switch (contentBlock) {
            case BodyParagraph paragraph -> List.of(new DocxParagraph(
                    paragraph.text(),
                    styleResolver.resolve(rule.styleMapping().paragraphStyleId())
            ));
            case BodyCitation citation -> List.of(new DocxParagraph(
                    citation.renderedText(),
                    styleResolver.resolve(rule.styleMapping().styleIdForCitation(citation.type()))
            ));
            case BodyFigure figure -> renderFigure(figure, rule.figure(), styleResolver, figureRenderingState);
        };
    }

    private static List<DocxBlock> renderFigure(
            BodyFigure figure,
            FigureRule rule,
            StyleResolver styleResolver,
            FigureRenderingState figureRenderingState
    ) {
        FigurePart part = figureRenderingState.nextPart(figure, rule);
        ResolvedImage resolvedImage = resolveImage(figure.image(), rule);
        List<DocxBlock> blocks = new ArrayList<>();

        blocks.add(new DocxParagraph(
                resolveCaptionText(figure, rule, part),
                styleResolver.resolve(rule.captionStyleId()),
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
            blocks.add(new DocxParagraph(
                    rule.sourceTemplate().replace("{source}", source),
                    styleResolver.resolve(rule.sourceStyleId()),
                    java.util.Optional.empty(),
                    java.util.Optional.empty(),
                    java.util.Optional.empty(),
                    false,
                    true
            ));
        }

        return List.copyOf(blocks);
    }

    private static String resolveCaptionText(BodyFigure figure, FigureRule rule, FigurePart part) {
        String caption = rule.captionTemplate()
                .replace("{number}", String.valueOf(part.number()))
                .replace("{caption}", figure.caption());

        return part.continuationLabel()
                .map(label -> caption + " (" + label + ")")
                .orElse(caption);
    }

    private static boolean shouldRenderSource(
            BodyFigure figure,
            FigureRule rule,
            FigurePart part,
            FigureRenderingState figureRenderingState
    ) {
        if (figureRenderingState.sourceFor(figure).isEmpty()) {
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

    private record FigurePart(int number, int index, int count, Optional<String> continuationLabel) {

        boolean last() {
            return index == count;
        }
    }

    private static final class FigureRenderingState {

        private final Map<String, Integer> numbersByGroupKey = new LinkedHashMap<>();
        private final Map<String, Integer> countsByGroupKey = new HashMap<>();
        private final Map<String, Integer> currentIndexByGroupKey = new HashMap<>();
        private final Map<String, String> sourceByGroupKey = new HashMap<>();

        private FigureRenderingState(List<BodySection> sections) {
            int nextNumber = 1;

            for (BodySection section : sections) {
                for (BodyBlock block : section.blocks()) {
                    if (block instanceof BodyFigure figure) {
                        String groupKey = figure.displayGroupKey();
                        countsByGroupKey.merge(groupKey, 1, Integer::sum);

                        if (!numbersByGroupKey.containsKey(groupKey)) {
                            numbersByGroupKey.put(groupKey, nextNumber);
                            nextNumber++;
                        }

                        figure.source().ifPresent(source -> registerSource(groupKey, source));
                    }
                }
            }
        }

        private Optional<String> sourceFor(BodyFigure figure) {
            return Optional.ofNullable(sourceByGroupKey.get(figure.displayGroupKey()));
        }

        private FigurePart nextPart(BodyFigure figure, FigureRule rule) {
            String groupKey = figure.displayGroupKey();
            int index = currentIndexByGroupKey.merge(groupKey, 1, Integer::sum);
            int count = countsByGroupKey.get(groupKey);
            int number = numbersByGroupKey.get(groupKey);

            return new FigurePart(number, index, count, continuationLabel(index, count, rule));
        }

        private void registerSource(String groupKey, String source) {
            String previousSource = sourceByGroupKey.putIfAbsent(groupKey, source);

            if (previousSource != null && !previousSource.equals(source)) {
                throw new IllegalArgumentException(
                        "figure continuation group source must be consistent: " + groupKey
                );
            }
        }

        private Optional<String> continuationLabel(int index, int count, FigureRule rule) {
            if (count == 1) {
                return Optional.empty();
            }

            if (index == 1) {
                return Optional.of(rule.continuationLabels().first());
            }

            if (index == count) {
                return Optional.of(rule.continuationLabels().last());
            }

            return Optional.of(rule.continuationLabels().middle());
        }
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
