package com.abntbuilder.formatter.engine.contract;

import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;

public interface DocxPostProcessor {
    PostProcessorResult process(byte[] docxBytes, DocumentProfile profile);
}
