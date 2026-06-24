package com.abntbuilder.formatter.application.export;

import com.abntbuilder.formatter.output.docx.api.DocxDocument;
import com.abntbuilder.formatter.output.docx.api.DocxPostProcessor;
import com.abntbuilder.formatter.output.docx.api.DocxWriter;
import com.abntbuilder.formatter.rendering.orchestration.DocumentRenderer;
import com.abntbuilder.formatter.shared.exception.MissingGeneratedDocxExportException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DocxExportService {

    private final DocxWriter docxWriter;
    private final GeneratedDocxExportStore generatedDocxExportStore;
    private final DocumentRenderer documentRenderer;
    private final DocxPostProcessor docxPostProcessor;

    public DocxExportService(
            DocxWriter docxWriter,
            GeneratedDocxExportStore generatedDocxExportStore,
            DocumentRenderer documentRenderer,
            DocxPostProcessor docxPostProcessor
    ) {
        this.docxWriter = Objects.requireNonNull(docxWriter, "docxWriter must not be null");
        this.generatedDocxExportStore = Objects.requireNonNull(
                generatedDocxExportStore,
                "generatedDocxExportStore must not be null"
        );
        this.documentRenderer = Objects.requireNonNull(documentRenderer, "documentRenderer must not be null");
        this.docxPostProcessor = Objects.requireNonNull(docxPostProcessor, "docxPostProcessor must not be null");
    }

    public byte[] export(ExportDocxCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        DocxDocument document = documentRenderer.render(command);
        byte[] docxBytes = docxWriter.write(document);

        return docxPostProcessor.process(docxBytes);
    }

    public GeneratedDocxExport generate(ExportDocxCommand command) {
        byte[] bytes = export(command);

        return generatedDocxExportStore.save(command.fileName(), bytes);
    }

    public GeneratedDocxExport findGeneratedExport(String exportId) {
        if (exportId == null || exportId.isBlank()) {
            throw new IllegalArgumentException("exportId must not be blank.");
        }

        return generatedDocxExportStore.findById(exportId)
                .orElseThrow(() -> new MissingGeneratedDocxExportException(exportId));
    }
}
