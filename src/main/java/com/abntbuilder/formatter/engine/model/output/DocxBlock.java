package com.abntbuilder.formatter.engine.model.output;

public sealed interface DocxBlock permits DocxParagraph, DocxBookmarkParagraph, DocxIndexEntryParagraph,
        DocxPageBreak, DocxBlankLine, DocxSectionBreak,
        DocxImageBlock, DocxTableBlock, DocxListItemParagraph, DocxFootnoteReferenceBlock, DocxTocBlock {
}
