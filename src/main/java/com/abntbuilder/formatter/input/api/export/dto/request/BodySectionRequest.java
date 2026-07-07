package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyBlock;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyParagraph;
import com.abntbuilder.formatter.engine.model.content.bodycontent.BodySection;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.CitationFormattingRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Optional;

public record BodySectionRequest(
        @NotBlank String id,
        @NotNull @Min(1) @Max(6) Integer level,
        String title,
        List<@NotBlank String> paragraphs,
        @Valid List<BodyBlockRequest> content,
        @Valid List<BodyBlockRequest> blocks
) {

    public BodySection toDomain(CitationFormattingRule citationFormatting) {
        return new BodySection(
                id,
                level,
                title == null ? Optional.empty() : Optional.of(title),
                resolveContent(citationFormatting)
        );
    }

    public BodySection toDomain() {
        throw new UnsupportedOperationException(
                "toDomain() requires a CitationFormattingRule. Use toDomain(CitationFormattingRule) instead."
        );
    }

    private List<BodyBlock> resolveContent(CitationFormattingRule citationFormatting) {
        if (blocks != null) {
            return blocks.stream()
                    .map(b -> b.toDomain(citationFormatting))
                    .toList();
        }

        if (content != null) {
            return content.stream()
                    .map(b -> b.toDomain(citationFormatting))
                    .toList();
        }

        return paragraphs == null
                ? List.of()
                : paragraphs.stream()
                .map(BodyParagraph::new)
                .map(BodyBlock.class::cast)
                .toList();
    }
}
