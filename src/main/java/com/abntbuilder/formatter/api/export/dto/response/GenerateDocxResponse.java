package com.abntbuilder.formatter.api.export.dto.response;

public record GenerateDocxResponse(
        String exportId,
        String fileName,
        String downloadUrl,
        long sizeBytes
) {
}