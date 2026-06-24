package com.abntbuilder.formatter.output.docx.api;

public interface DocxPostProcessor {
    byte[] process(byte[] docxBytes);
}
