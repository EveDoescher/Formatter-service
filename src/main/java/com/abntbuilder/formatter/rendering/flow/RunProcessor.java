package com.abntbuilder.formatter.rendering.flow;

import com.abntbuilder.formatter.document.component.bodycontent.BodyAbbreviation;
import com.abntbuilder.formatter.document.component.bodycontent.BodyCitationCall;
import com.abntbuilder.formatter.document.component.bodycontent.BodyCrossReference;
import com.abntbuilder.formatter.document.component.bodycontent.BodyFootnote;
import com.abntbuilder.formatter.document.component.bodycontent.BodyInline;
import com.abntbuilder.formatter.document.component.bodycontent.BodyQuoteMarkerType;
import com.abntbuilder.formatter.document.component.bodycontent.BodyQuoteText;
import com.abntbuilder.formatter.document.component.bodycontent.BodyText;
import com.abntbuilder.formatter.document.component.bodycontent.InlineFormatting;
import com.abntbuilder.formatter.output.docx.api.DocxFootnoteContent;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyAbbreviationMetadata;

import java.util.ArrayList;
import java.util.List;

final class RunProcessor {

    static List<DocxRun> processAll(
            List<BodyInline> inlines,
            StyleRule baseStyle,
            FlowRenderingContext ctx,
            List<DocxFootnoteContent> footnoteAccumulator
    ) {
        List<DocxRun> runs = new ArrayList<>();
        for (BodyInline inline : inlines) {
            runs.addAll(process(inline, baseStyle, ctx, footnoteAccumulator));
        }
        return runs;
    }

    static List<DocxRun> process(
            BodyInline inline,
            StyleRule baseStyle,
            FlowRenderingContext ctx,
            List<DocxFootnoteContent> footnoteAccumulator
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
                            " (" + ctx.rule.citationFormatting().emphasisOursLabel() + ")",
                            baseStyle,
                            InlineFormatting.none()
                    ));
                } else if (hasEmphasisAuthor) {
                    runs.add(new DocxRun(
                            " (" + ctx.rule.citationFormatting().emphasisAuthorLabel() + ")",
                            baseStyle,
                            InlineFormatting.none()
                    ));
                }
                yield List.copyOf(runs);
            }
            case BodyCitationCall call -> {
                StyleRule citationStyle = ctx.styleResolver.resolve(
                        ctx.rule.styleMapping().styleIdForCitation(call.citationType())
                );
                yield List.of(new DocxRun(call.renderedText(), citationStyle, InlineFormatting.none()));
            }
            case BodyAbbreviation abbr -> {
                if (ctx.abbreviationMetas.stream().noneMatch(m -> m.abbreviation().equals(abbr.abbreviation()))) {
                    ctx.abbreviationMetas.add(new BodyAbbreviationMetadata(abbr.abbreviation(), abbr.expansion()));
                }
                yield List.of(new DocxRun(abbr.renderedText(), baseStyle, InlineFormatting.none()));
            }
            case BodyFootnote footnote -> {
                int fnId = ++ctx.footnoteCounter[0];
                StyleRule footnoteTextStyle = ctx.styleResolver.resolve(ctx.rule.styleMapping().footnoteTextStyleId());
                List<DocxRun> fnRuns = new ArrayList<>();
                for (BodyInline fi : footnote.content()) {
                    fnRuns.addAll(process(fi, footnoteTextStyle, ctx, footnoteAccumulator));
                }
                footnoteAccumulator.add(new DocxFootnoteContent(fnId, fnRuns));
                StyleRule footnoteCallStyle = ctx.styleResolver.resolve(ctx.rule.styleMapping().footnoteCallStyleId());
                yield List.of(new DocxRun("[FN:" + fnId + "]", footnoteCallStyle, InlineFormatting.none()));
            }
            case BodyCrossReference ref -> {
                String resolved = ctx.phase0Index.resolveCrossReference(
                        ref.targetId(),
                        ref.targetType(),
                        ref.displayMode(),
                        ctx.rule.crossReferenceLabels()
                );
                yield List.of(new DocxRun(resolved, baseStyle, InlineFormatting.none()));
            }
        };
    }
}
