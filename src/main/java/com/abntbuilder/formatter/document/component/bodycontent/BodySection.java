package com.abntbuilder.formatter.document.component.bodycontent;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record BodySection(
        String id,
        int level,
        Optional<String> title,
        List<BodyBlock> blocks
) {

    public BodySection {
        requireNonBlank(id, "id");
        if (level < 1 || level > 6) {
            throw new IllegalArgumentException("level must be between 1 and 6.");
        }

        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(blocks, "blocks must not be null");

        title.ifPresent(value -> requireNonBlank(value, "title"));

        if (title.isEmpty() && blocks.isEmpty()) {
            throw new IllegalArgumentException("bodyContent section without title must contain at least one block.");
        }

        blocks = List.copyOf(blocks);

        for (BodyBlock block : blocks) {
            Objects.requireNonNull(block, "blocks must not contain null values.");
        }
    }

    public List<BodyBlock> content() {
        return blocks;
    }

    public static BodySection fromParagraphs(
            String id,
            int level,
            Optional<String> title,
            List<String> paragraphs
    ) {
        Objects.requireNonNull(paragraphs, "paragraphs must not be null");

        return new BodySection(
                id,
                level,
                title,
                paragraphs.stream()
                        .map(BodyParagraph::new)
                        .map(BodyBlock.class::cast)
                        .toList()
        );
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
