package com.abntbuilder.formatter.api.export.controller;

import com.abntbuilder.formatter.api.export.dto.request.ExportDocxRequest;
import com.abntbuilder.formatter.api.export.dto.response.GenerateDocxResponse;
import com.abntbuilder.formatter.application.export.DocxExportService;
import com.abntbuilder.formatter.application.export.GeneratedDocxExport;
import com.abntbuilder.formatter.profile.resolution.ProfileProvider;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/exports")
public class DocxExportController {

    private static final String DOCX_MEDIA_TYPE_VALUE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    private static final MediaType DOCX_MEDIA_TYPE = MediaType.parseMediaType(DOCX_MEDIA_TYPE_VALUE);

    private final DocxExportService docxExportService;
    private final ProfileProvider profileProvider;

    public DocxExportController(
            DocxExportService docxExportService,
            ProfileProvider profileProvider
    ) {
        this.docxExportService = docxExportService;
        this.profileProvider = profileProvider;
    }

    @PostMapping(
            value = "/docx",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = DOCX_MEDIA_TYPE_VALUE
    )
    public ResponseEntity<byte[]> exportDocx(@Valid @RequestBody ExportDocxRequest request) {
        byte[] docxBytes = docxExportService.export(request.toCommand(profileProvider));

        return ResponseEntity.ok()
                .contentType(DOCX_MEDIA_TYPE)
                .contentLength(docxBytes.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition(request.fileName()))
                .body(docxBytes);
    }

    @PostMapping(
            value = "/docx/generated",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<GenerateDocxResponse> generateDocx(@Valid @RequestBody ExportDocxRequest request) {
        GeneratedDocxExport generatedExport = docxExportService.generate(request.toCommand(profileProvider));

        GenerateDocxResponse response = new GenerateDocxResponse(
                generatedExport.id(),
                generatedExport.fileName(),
                downloadUrl(generatedExport.id()),
                generatedExport.sizeBytes()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping(
            value = "/docx/generated/{exportId}/download",
            produces = DOCX_MEDIA_TYPE_VALUE
    )
    public ResponseEntity<byte[]> downloadGeneratedDocx(@PathVariable String exportId) {
        GeneratedDocxExport generatedExport = docxExportService.findGeneratedExport(exportId);
        byte[] docxBytes = generatedExport.bytes();

        return ResponseEntity.ok()
                .contentType(DOCX_MEDIA_TYPE)
                .contentLength(docxBytes.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition(generatedExport.fileName()))
                .body(docxBytes);
    }

    private static String contentDisposition(String fileName) {
        String safeFileName = fileName
                .replace("\r", "")
                .replace("\n", "")
                .replace("\"", "");

        return ContentDisposition.attachment()
                .filename(safeFileName)
                .build()
                .toString();
    }

    private static String downloadUrl(String exportId) {
        return UriComponentsBuilder
                .fromPath("/api/v1/exports/docx/generated/{exportId}/download")
                .buildAndExpand(exportId)
                .toUriString();
    }
}
