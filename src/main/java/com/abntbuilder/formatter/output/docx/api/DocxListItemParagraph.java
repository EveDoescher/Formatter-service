package com.abntbuilder.formatter.output.docx.api;

import com.abntbuilder.formatter.document.component.bodycontent.BodyListType;
import com.abntbuilder.formatter.profile.model.StyleRule;

import java.util.List;
import java.util.Objects;

public record DocxListItemParagraph(
        List<DocxRun> runs,
        StyleRule styleRule,
        BodyListType listType,
        int listLevel
) implements DocxBlock {

    public DocxListItemParagraph {
        Objects.requireNonNull(runs, "runs must not be null");
        if (runs.isEmpty()) {
            throw new IllegalArgumentException("runs must not be empty.");
        }
        runs = List.copyOf(runs);
        Objects.requireNonNull(styleRule, "styleRule must not be null");
        Objects.requireNonNull(listType, "listType must not be null");
        if (listLevel < 0) {
            throw new IllegalArgumentException("listLevel must be >= 0.");
        }
    }
}
