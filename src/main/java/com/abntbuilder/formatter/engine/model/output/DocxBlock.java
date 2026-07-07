package com.abntbuilder.formatter.engine.model.output;

public sealed interface DocxBlock permits DocxParagraph, DocxPageBreak, DocxBlankLine, DocxSectionBreak,
        DocxImageBlock, DocxTableBlock, DocxListItemParagraph, DocxFootnoteReferenceBlock, DocxTocBlock {
}
