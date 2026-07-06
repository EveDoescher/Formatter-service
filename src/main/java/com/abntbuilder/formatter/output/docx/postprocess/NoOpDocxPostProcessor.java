package com.abntbuilder.formatter.output.docx.postprocess;

import com.abntbuilder.formatter.output.docx.api.DocxPostProcessor;
import com.abntbuilder.formatter.output.docx.api.PostProcessorResult;
import com.abntbuilder.formatter.profile.model.DocumentProfile;

public final class NoOpDocxPostProcessor implements DocxPostProcessor {
    @Override
    public PostProcessorResult process(byte[] docxBytes, DocumentProfile profile) {
        return PostProcessorResult.of(docxBytes);
    }
}
