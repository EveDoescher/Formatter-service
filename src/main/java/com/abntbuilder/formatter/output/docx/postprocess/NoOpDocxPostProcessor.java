package com.abntbuilder.formatter.output.docx.postprocess;

import com.abntbuilder.formatter.output.docx.api.DocxPostProcessor;

public final class NoOpDocxPostProcessor implements DocxPostProcessor {
    @Override
    public byte[] process(byte[] docxBytes) {
        return docxBytes;
    }
}
