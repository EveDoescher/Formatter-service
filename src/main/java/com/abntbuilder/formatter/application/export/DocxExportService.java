package com.abntbuilder.formatter.application.export;

import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxDocument;
import com.abntbuilder.formatter.output.docx.api.DocxPageBreak;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxWriter;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.cover.CoverRenderer;
import com.abntbuilder.formatter.shared.exception.MissingGeneratedDocxExportException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class DocxExportService {

    private final DocxWriter docxWriter;
    private final GeneratedDocxExportStore generatedDocxExportStore;
    private final CoverRenderer coverRenderer;

    public DocxExportService(
            DocxWriter docxWriter,
            GeneratedDocxExportStore generatedDocxExportStore,
            CoverRenderer coverRenderer
    ) {
        this.docxWriter = Objects.requireNonNull(docxWriter, "docxWriter must not be null");
        this.generatedDocxExportStore = Objects.requireNonNull(
                generatedDocxExportStore,
                "generatedDocxExportStore must not be null"
        );
        this.coverRenderer = Objects.requireNonNull(coverRenderer, "coverRenderer must not be null");
    }

    public byte[] export(ExportDocxCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        StyleResolver styleResolver = new StyleResolver(command.profile());

        List<DocxBlock> blocks = new ArrayList<>();

        command.cover().ifPresent(cover -> {
            blocks.addAll(coverRenderer.render(cover, command.profile()));

            if (!command.paragraphs().isEmpty()) {
                blocks.add(new DocxPageBreak());
            }
        });

        List<DocxBlock> paragraphBlocks = command.paragraphs()
                .stream()
                .map(paragraph -> new DocxParagraph(
                        paragraph.text(),
                        styleResolver.resolve(paragraph.styleId())
                ))
                .map(DocxBlock.class::cast)
                .toList();

        blocks.addAll(paragraphBlocks);

        DocxDocument document = new DocxDocument(
                command.profile().pageRule(),
                blocks
        );

        return docxWriter.write(document);
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
