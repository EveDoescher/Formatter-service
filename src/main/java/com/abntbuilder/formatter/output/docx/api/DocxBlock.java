package com.abntbuilder.formatter.output.docx.api;

public sealed interface DocxBlock permits DocxParagraph, DocxPageBreak, DocxBlankLine, DocxMultilineParagraph {
}