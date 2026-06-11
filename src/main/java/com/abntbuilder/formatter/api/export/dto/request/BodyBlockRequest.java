package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.bodycontent.BodyBlock;
import com.abntbuilder.formatter.document.component.bodycontent.BodyCitation;
import com.abntbuilder.formatter.document.component.bodycontent.BodyCitationMode;
import com.abntbuilder.formatter.document.component.bodycontent.BodyCitationType;
import com.abntbuilder.formatter.document.component.bodycontent.BodyParagraph;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Optional;

public record BodyBlockRequest(
        @NotNull BodyBlockType type,
        BodyCitationMode mode,
        String text,
        @Valid List<BodyInlineRequest> content,
        @Valid CitationSourceRequest source,
        @Valid CitationSourceRequest originalSource,
        @Valid CitationSourceRequest consultedSource,
        @Valid BodyFigureRequest figure,
        @Valid BodyTableRequest table
) {

    public BodyBlock toDomain() {
        return switch (type) {
            case PARAGRAPH -> paragraph();
            case DIRECT_SHORT_QUOTE -> citation(BodyCitationType.DIRECT_SHORT);
            case DIRECT_LONG_QUOTE -> citation(BodyCitationType.DIRECT_LONG);
            case INDIRECT_CITATION -> citation(BodyCitationType.INDIRECT);
            case CITATION_OF_CITATION -> citation(BodyCitationType.CITATION_OF_CITATION);
            case FIGURE -> figureBlock();
            case TABLE -> tableBlock();
        };
    }

    private BodyParagraph paragraph() {
        if (content != null) {
            return new BodyParagraph(content.stream()
                    .map(BodyInlineRequest::toDomain)
                    .toList());
        }

        return new BodyParagraph(text);
    }

    private BodyCitation citation(BodyCitationType citationType) {
        return new BodyCitation(
                citationType,
                mode == null ? BodyCitationMode.PARENTHETICAL : mode,
                text,
                source == null ? Optional.empty() : Optional.of(source.toDomain()),
                originalSource == null ? Optional.empty() : Optional.of(originalSource.toDomain()),
                consultedSource == null ? Optional.empty() : Optional.of(consultedSource.toDomain())
        );
    }

    private BodyBlock figureBlock() {
        if (figure == null) {
            throw new IllegalArgumentException("figure must be provided for FIGURE block.");
        }

        return figure.toDomain();
    }

    private BodyBlock tableBlock() {
        if (table == null) {
            throw new IllegalArgumentException("table must be provided for TABLE block.");
        }

        return table.toDomain();
    }
}
