package com.abntbuilder.formatter.output.docx.postprocess;

import com.abntbuilder.formatter.config.LibreOfficeProperties;
import com.abntbuilder.formatter.output.docx.api.DocxPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public final class LibreOfficeDocxPostProcessor implements DocxPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(LibreOfficeDocxPostProcessor.class);

    private final LibreOfficeProperties properties;

    public LibreOfficeDocxPostProcessor(LibreOfficeProperties properties) {
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
    }

    @Override
    public byte[] process(byte[] docxBytes) {
        Path tempInput = null;
        Path tempOutputDir = null;
        try {
            tempInput = Files.createTempFile("formatter-", ".docx");
            tempOutputDir = Files.createTempDirectory("formatter-lo-out-");

            Files.write(tempInput, docxBytes);

            boolean success = runLibreOffice(tempInput, tempOutputDir);
            if (!success) {
                log.warn("LibreOffice post-processing failed or timed out — returning original DOCX bytes.");
                return docxBytes;
            }

            Path outputFile = tempOutputDir.resolve(tempInput.getFileName());
            if (!Files.exists(outputFile)) {
                log.warn("LibreOffice output file not found — returning original DOCX bytes.");
                return docxBytes;
            }

            return Files.readAllBytes(outputFile);

        } catch (IOException | InterruptedException e) {
            log.warn("LibreOffice post-processing error — returning original DOCX bytes.", e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return docxBytes;
        } finally {
            deleteSilently(tempInput);
            deleteSilently(tempOutputDir);
        }
    }

    private boolean runLibreOffice(Path inputFile, Path outputDir)
            throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                properties.getExecutablePath(),
                "--headless",
                "--norestore",
                "--convert-to", "docx:MS Word 2007 XML",
                "--outdir", outputDir.toAbsolutePath().toString(),
                inputFile.toAbsolutePath().toString()
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        process.getInputStream().transferTo(java.io.OutputStream.nullOutputStream());

        boolean finished = process.waitFor(properties.getTimeoutSeconds(), TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            return false;
        }
        return process.exitValue() == 0;
    }

    private static void deleteSilently(Path path) {
        if (path == null) return;
        try {
            if (Files.isDirectory(path)) {
                try (Stream<Path> walk = Files.walk(path)) {
                    walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                    });
                }
            } else {
                Files.deleteIfExists(path);
            }
        } catch (IOException ignored) {}
    }
}
