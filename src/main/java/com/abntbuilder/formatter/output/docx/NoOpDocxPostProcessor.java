package com.abntbuilder.formatter.output.docx;

import com.abntbuilder.formatter.engine.contract.DocxPostProcessor;
import com.abntbuilder.formatter.engine.contract.PostProcessorResult;
import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;

public final class NoOpDocxPostProcessor implements DocxPostProcessor {
    @Override
    public PostProcessorResult process(byte[] docxBytes, DocumentProfile profile) {
        return PostProcessorResult.of(docxBytes);
    }
}
