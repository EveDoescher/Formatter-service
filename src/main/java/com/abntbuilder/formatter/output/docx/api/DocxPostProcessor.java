package com.abntbuilder.formatter.output.docx.api;

import com.abntbuilder.formatter.profile.model.DocumentProfile;

public interface DocxPostProcessor {
    PostProcessorResult process(byte[] docxBytes, DocumentProfile profile);
}
