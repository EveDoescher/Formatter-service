package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.bodycontent.BodyBlock;
import com.abntbuilder.formatter.document.component.bodycontent.BodyCitationMode;
import com.abntbuilder.formatter.document.component.bodycontent.BodyLongQuote;
import com.abntbuilder.formatter.document.component.bodycontent.BodyParagraph;
import com.abntbuilder.formatter.profile.model.component.bodycontent.CitationFormattingRule;
import com.abntbuilder.formatter.shared.exception.InvalidBodyContentException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Optional;

public record BodyBlockRequest(
        @NotNull BodyBlockType type,
        BodyCitationMode mode,
        String text,
        @Valid java.util.List<BodyInlineRequest> content,
        @Valid CitationSourceRequest source,
        @Valid CitationSourceRequest originalSource,
        @Valid CitationSourceRequest consultedSource,
        @Valid BodyFigureRequest figure,
        @Valid BodyTableRequest table,
        @Valid BodyFrameRequest frame,
        @Valid BodyCodeListingRequest codeListing,
        @Valid BodyChartRequest chart,
        @Valid BodyEquationRequest equation,
        @Valid BodyListRequest list,
        @Valid java.util.List<BodyQuoteMarkerRequest> markers
) {


    public BodyBlock toDomain(CitationFormattingRule citationFormatting) {
        return switch (type) {
            case PARAGRAPH -> paragraph(citationFormatting);
            case DIRECT_LONG_QUOTE -> longQuote();
            case FIGURE -> figureBlock();
            case TABLE -> tableBlock();
            case FRAME -> frameBlock();
            case CODE_LISTING -> codeListingBlock();
            case CHART -> chartBlock();
            case EQUATION -> equationBlock();
            case ORDERED_LIST, UNORDERED_LIST -> {
                if (list == null) throw new InvalidBodyContentException(type + " block requires list.");
                yield list.toDomain(citationFormatting);
            }
            default -> throw new UnsupportedOperationException("Block type " + type + " is not yet implemented.");
        };
    }

    private BodyParagraph paragraph(CitationFormattingRule citationFormatting) {
        if (content != null) {
            return new BodyParagraph(content.stream()
                    .map(inline -> inline.toDomain(citationFormatting))
                    .toList());
        }
        if (text != null) {
            return new BodyParagraph(text);
        }
        throw new InvalidBodyContentException("PARAGRAPH block requires either content or text.");
    }

    private BodyLongQuote longQuote() {
        if (text == null || text.isBlank()) {
            throw new InvalidBodyContentException("DIRECT_LONG_QUOTE block requires text.");
        }
        if (mode == null) {
            throw new InvalidBodyContentException("mode must be provided for DIRECT_LONG_QUOTE block.");
        }
        return new BodyLongQuote(
                text,
                mode,
                source == null ? Optional.empty() : Optional.of(source.toDomain()),
                originalSource == null ? Optional.empty() : Optional.of(originalSource.toDomain()),
                consultedSource == null ? Optional.empty() : Optional.of(consultedSource.toDomain()),
                markers == null ? java.util.List.of() : markers.stream().map(BodyQuoteMarkerRequest::toDomain).toList()
        );
    }

    private BodyBlock figureBlock() {
        if (figure == null) {
            throw new InvalidBodyContentException("figure must be provided for FIGURE block.");
        }
        return figure.toDomain();
    }

    private BodyBlock tableBlock() {
        if (table == null) {
            throw new InvalidBodyContentException("table must be provided for TABLE block.");
        }
        return table.toDomain();
    }

    private BodyBlock frameBlock() {
        if (frame == null) {
            throw new InvalidBodyContentException("frame must be provided for FRAME block.");
        }
        return frame.toDomain();
    }

    private BodyBlock codeListingBlock() {
        if (codeListing == null) {
            throw new InvalidBodyContentException("codeListing must be provided for CODE_LISTING block.");
        }
        return codeListing.toDomain();
    }

    private BodyBlock chartBlock() {
        if (chart == null) {
            throw new InvalidBodyContentException("chart must be provided for CHART block.");
        }
        return chart.toDomain();
    }

    private BodyBlock equationBlock() {
        if (equation == null) {
            throw new InvalidBodyContentException("equation must be provided for EQUATION block.");
        }
        return equation.toDomain();
    }
}
