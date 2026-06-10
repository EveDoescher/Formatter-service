package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.bodycontent.BodyBlock;
import com.abntbuilder.formatter.document.component.bodycontent.BodyParagraph;
import com.abntbuilder.formatter.document.component.bodycontent.BodySection;
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

    public BodySection toDomain() {
        return new BodySection(
                id,
                level,
                title == null ? Optional.empty() : Optional.of(title),
                resolveContent()
        );
    }

    private List<BodyBlock> resolveContent() {
        if (blocks != null) {
            return blocks.stream()
                    .map(BodyBlockRequest::toDomain)
                    .toList();
        }

        if (content != null) {
            return content.stream()
                    .map(BodyBlockRequest::toDomain)
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
