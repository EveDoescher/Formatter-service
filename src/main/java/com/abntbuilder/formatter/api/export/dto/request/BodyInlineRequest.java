package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.bodycontent.BodyCitationCall;
import com.abntbuilder.formatter.document.component.bodycontent.BodyCitationMode;
import com.abntbuilder.formatter.document.component.bodycontent.BodyCitationType;
import com.abntbuilder.formatter.document.component.bodycontent.BodyInline;
import com.abntbuilder.formatter.document.component.bodycontent.BodyQuoteText;
import com.abntbuilder.formatter.document.component.bodycontent.BodyQuoteType;
import com.abntbuilder.formatter.document.component.bodycontent.BodyText;
import com.abntbuilder.formatter.document.component.bodycontent.InlineFormatting;
import com.abntbuilder.formatter.profile.model.component.bodycontent.CitationFormattingRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Optional;

public record BodyInlineRequest(
        @NotNull BodyInlineType type,
        String text,
        String expansion,
        BodyQuoteType quoteType,
        BodyCitationType citationType,
        BodyCitationMode mode,
        @Valid CitationSourceRequest source,
        @Valid CitationSourceRequest originalSource,
        @Valid CitationSourceRequest consultedSource,
        InlineFormattingRequest formatting,
        @Valid java.util.List<BodyQuoteMarkerRequest> markers,
        @Valid java.util.List<BodyInlineRequest> content,
        @Valid BodyCrossReferenceRequest crossReference
) {

    BodyInline toDomain(CitationFormattingRule citationFormatting) {
        InlineFormatting fmt = formatting != null ? formatting.toDomain() : InlineFormatting.none();
        return switch (type) {
            case TEXT -> new BodyText(text, fmt);
            case QUOTE_TEXT -> new BodyQuoteText(
                    quoteType == null ? BodyQuoteType.SHORT : quoteType,
                    text,
                    fmt,
                    markers == null ? java.util.List.of() : markers.stream().map(BodyQuoteMarkerRequest::toDomain).toList()
            );
            case CITATION -> new BodyCitationCall(
                    requireCitationType(),
                    requireMode(),
                    requireCitationFormatting(citationFormatting),
                    source == null ? Optional.empty() : Optional.of(source.toDomain()),
                    originalSource == null ? Optional.empty() : Optional.of(originalSource.toDomain()),
                    consultedSource == null ? Optional.empty() : Optional.of(consultedSource.toDomain())
            );
            case ABBREVIATION -> {
                if (text == null || text.isBlank()) {
                    throw new IllegalArgumentException("ABBREVIATION.text must not be blank.");
                }
                if (expansion == null || expansion.isBlank()) {
                    throw new IllegalArgumentException("ABBREVIATION.expansion must not be blank.");
                }
                yield new com.abntbuilder.formatter.document.component.bodycontent.BodyAbbreviation(text, expansion);
            }
            case FOOTNOTE -> {
                if (content == null || content.isEmpty()) {
                    throw new IllegalArgumentException("FOOTNOTE.content must not be empty.");
                }
                yield new com.abntbuilder.formatter.document.component.bodycontent.BodyFootnote(
                        content.stream().map(inline -> inline.toDomain(citationFormatting)).toList()
                );
            }
            case CROSS_REFERENCE -> {
                if (crossReference == null) {
                    throw new IllegalArgumentException("crossReference must be provided for CROSS_REFERENCE inline.");
                }
                yield crossReference.toDomain();
            }
        };
    }

    private BodyCitationType requireCitationType() {
        if (citationType == null) {
            throw new IllegalArgumentException("citationType must be provided for CITATION inline content.");
        }
        return citationType;
    }

    private BodyCitationMode requireMode() {
        if (mode == null) {
            throw new IllegalArgumentException("mode must be provided for CITATION inline content.");
        }
        return mode;
    }

    private static CitationFormattingRule requireCitationFormatting(CitationFormattingRule citationFormatting) {
        if (citationFormatting == null) {
            throw new IllegalArgumentException(
                    "citationFormatting must be provided for CITATION inline content. "
                            + "Ensure the request includes a profile with bodyContent.citationFormatting."
            );
        }
        return citationFormatting;
    }
}
