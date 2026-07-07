package com.abntbuilder.formatter.input.api.export.dto.response;

public record GenerateDocxResponse(
        String exportId,
        String fileName,
        String downloadUrl,
        long sizeBytes
) {
}