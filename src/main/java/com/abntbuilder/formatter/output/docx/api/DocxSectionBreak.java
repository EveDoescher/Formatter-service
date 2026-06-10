package com.abntbuilder.formatter.output.docx.api;

import java.util.Objects;

public record DocxSectionBreak(
        DocxPageNumbering pageNumbering
) implements DocxBlock {

    public DocxSectionBreak {
        Objects.requireNonNull(pageNumbering, "pageNumbering must not be null");
    }
}
