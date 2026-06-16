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
        BodyQuoteType quoteType,
        BodyCitationType citationType,
        BodyCitationMode mode,
        @Valid CitationSourceRequest source,
        @Valid CitationSourceRequest originalSource,
        @Valid CitationSourceRequest consultedSource,
        InlineFormattingRequest formatting
) {

    BodyInline toDomain(CitationFormattingRule citationFormatting) {
        InlineFormatting fmt = formatting != null ? formatting.toDomain() : InlineFormatting.none();
        return switch (type) {
            case TEXT -> new BodyText(text, fmt);
            case QUOTE_TEXT -> new BodyQuoteText(
                    quoteType == null ? BodyQuoteType.SHORT : quoteType,
                    text,
                    fmt
            );
            case CITATION -> new BodyCitationCall(
                    requireCitationType(),
                    mode == null ? BodyCitationMode.PARENTHETICAL : mode,
                    citationFormatting,
                    source == null ? Optional.empty() : Optional.of(source.toDomain()),
                    originalSource == null ? Optional.empty() : Optional.of(originalSource.toDomain()),
                    consultedSource == null ? Optional.empty() : Optional.of(consultedSource.toDomain())
            );
        };
    }

    BodyInline toDomain() {
        return toDomain(new CitationFormattingRule("p. ", "; ", "et al.", " apud "));
    }

    private BodyCitationType requireCitationType() {
        if (citationType == null) {
            throw new IllegalArgumentException("citationType must be provided for CITATION inline content.");
        }
        return citationType;
    }
}
