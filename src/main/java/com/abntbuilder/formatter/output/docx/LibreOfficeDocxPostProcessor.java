package com.abntbuilder.formatter.output.docx;

import com.abntbuilder.formatter.config.LibreOfficeProperties;
import com.abntbuilder.formatter.engine.contract.DocxPostProcessor;
import com.abntbuilder.formatter.engine.contract.PostProcessorResult;
import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;
import com.abntbuilder.formatter.engine.model.profile.PostProcessingRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class LibreOfficeDocxPostProcessor implements DocxPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(LibreOfficeDocxPostProcessor.class);

    private final LibreOfficeProperties properties;

    public LibreOfficeDocxPostProcessor(LibreOfficeProperties properties) {
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
    }

    @Override
    public PostProcessorResult process(byte[] docxBytes, DocumentProfile profile) {
        Objects.requireNonNull(profile, "profile must not be null");

        Path workDir = null;
        try {
            workDir = Files.createTempDirectory("formatter-lo-");
            Path inputFile = workDir.resolve("input.docx");
            Files.write(inputFile, docxBytes);

            PostProcessingRule postProcessing = profile.postProcessingRule().orElse(null);

            byte[] cleanedBytes = cleanDocxForLibreOffice(docxBytes);
            Files.write(inputFile, cleanedBytes);

            byte[] current = resolveFieldsViaLibreOffice(inputFile, workDir, cleanedBytes);

            List<String> warnings = new ArrayList<>();

            if (postProcessing != null) {
                current = applyUnoScript(current, workDir, profile, postProcessing, warnings);
            }

            if (postProcessing != null && postProcessing.pdfOutput()
                    .map(PostProcessingRule.PdfOutputRule::enabled).orElse(false)) {
                generatePdf(current, workDir, warnings);
            }

            return PostProcessorResult.of(current, warnings);

        } catch (IOException | InterruptedException e) {
            log.warn("LibreOffice post-processing error — returning original DOCX bytes.", e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return PostProcessorResult.of(docxBytes);
        } finally {
            deleteSilently(workDir);
        }
    }

    private byte[] resolveFieldsViaLibreOffice(Path inputFile, Path workDir, byte[] fallback)
            throws IOException, InterruptedException {
        Path outDir = workDir.resolve("fields-out");
        Files.createDirectories(outDir);

        ProcessBuilder pb = new ProcessBuilder(
                properties.getExecutablePath(),
                "--headless",
                "--norestore",
                "--convert-to", "docx:MS Word 2007 XML",
                "--outdir", outDir.toAbsolutePath().toString(),
                inputFile.toAbsolutePath().toString()
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String loOutput = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        boolean finished = process.waitFor(properties.getTimeoutSeconds(), TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            log.warn("LibreOffice field resolution timed out — returning original DOCX bytes. Output: {}", loOutput);
            return fallback;
        }
        if (process.exitValue() != 0) {
            log.warn("LibreOffice field resolution exited with {} — returning original DOCX bytes. Output: {}",
                    process.exitValue(), loOutput);
            return fallback;
        }

        Path output = outDir.resolve("input.docx");
        if (!Files.exists(output)) {
            log.warn("LibreOffice field resolution produced no output — returning original DOCX bytes. Output: {}", loOutput);
            return fallback;
        }
        return Files.readAllBytes(output);
    }

    private byte[] applyUnoScript(
            byte[] docxBytes,
            Path workDir,
            DocumentProfile profile,
            PostProcessingRule rule,
            List<String> warnings
    ) throws IOException, InterruptedException {
        Path scriptInput = workDir.resolve("uno-input.docx");
        Path scriptOutput = workDir.resolve("uno-output.docx");
        Path warningsFile = workDir.resolve("warnings.txt");
        Path scriptFile = workDir.resolve("formatter_uno.py");

        Files.write(scriptInput, docxBytes);
        writeUnoScript(scriptFile, profile, rule);

        ProcessBuilder pb = new ProcessBuilder(
                "python3",
                scriptFile.toAbsolutePath().toString(),
                scriptInput.toAbsolutePath().toString(),
                scriptOutput.toAbsolutePath().toString(),
                warningsFile.toAbsolutePath().toString()
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        String scriptOutput2 = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        boolean finished = process.waitFor(properties.getTimeoutSeconds(), TimeUnit.SECONDS);

        if (!finished) {
            process.destroyForcibly();
            log.warn("UNO script timed out — returning DOCX without UNO transformations.");
            return docxBytes;
        }
        if (process.exitValue() != 0) {
            log.warn("UNO script exited with {} — returning DOCX without UNO transformations. Output: {}",
                    process.exitValue(), scriptOutput2);
            return docxBytes;
        }

        if (Files.exists(warningsFile)) {
            List<String> lines = Files.readAllLines(warningsFile, StandardCharsets.UTF_8);
            warnings.addAll(lines.stream().filter(l -> !l.isBlank()).toList());
        }

        if (!Files.exists(scriptOutput)) {
            log.warn("UNO script produced no output file — returning DOCX without UNO transformations.");
            return docxBytes;
        }
        return Files.readAllBytes(scriptOutput);
    }

    private void generatePdf(byte[] docxBytes, Path workDir, List<String> warnings)
            throws IOException, InterruptedException {
        Path pdfInput = workDir.resolve("pdf-input.docx");
        Path pdfOutDir = workDir.resolve("pdf-out");
        Files.createDirectories(pdfOutDir);
        Files.write(pdfInput, docxBytes);

        ProcessBuilder pb = new ProcessBuilder(
                properties.getExecutablePath(),
                "--headless",
                "--norestore",
                "--convert-to", "pdf",
                "--outdir", pdfOutDir.toAbsolutePath().toString(),
                pdfInput.toAbsolutePath().toString()
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();
        process.getInputStream().transferTo(java.io.OutputStream.nullOutputStream());

        boolean finished = process.waitFor(properties.getTimeoutSeconds(), TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            warnings.add("PDF generation timed out.");
        } else if (process.exitValue() != 0) {
            warnings.add("PDF generation failed with exit code " + process.exitValue() + ".");
        }
    }

    private static void writeUnoScript(
            Path scriptFile,
            DocumentProfile profile,
            PostProcessingRule rule
    ) throws IOException {
        boolean tableContinuation = rule.tableContinuationLabels()
                .map(PostProcessingRule.TableContinuationLabelsRule::enabled).orElse(false);
        boolean orphanCorrection = rule.orphanTitleCorrection()
                .map(PostProcessingRule.OrphanTitleCorrectionRule::enabled).orElse(false);
        boolean integrityCheck = rule.integrityCheck()
                .map(PostProcessingRule.IntegrityCheckRule::enabled).orElse(false);
        boolean checkMarginOverflow = rule.integrityCheck()
                .map(PostProcessingRule.IntegrityCheckRule::checkMarginOverflow).orElse(false);
        boolean checkFontSubstitution = rule.integrityCheck()
                .map(PostProcessingRule.IntegrityCheckRule::checkFontSubstitution).orElse(false);
        Optional<Integer> maxPages = rule.integrityCheck()
                .flatMap(PostProcessingRule.IntegrityCheckRule::maxPages);

        String continuesLabel = rule.tableContinuationLabels()
                .map(PostProcessingRule.TableContinuationLabelsRule::continuesLabel).orElse("");
        String continuationLabel = rule.tableContinuationLabels()
                .map(PostProcessingRule.TableContinuationLabelsRule::continuationLabel).orElse("");
        String conclusionLabel = rule.tableContinuationLabels()
                .map(PostProcessingRule.TableContinuationLabelsRule::conclusionLabel).orElse("");
        String labelStyleId = rule.tableContinuationLabels()
                .map(PostProcessingRule.TableContinuationLabelsRule::labelStyleId).orElse("");

        String maxPagesStr = maxPages.map(Object::toString).orElse("None");

        String script = """
                import sys
                import os
                import subprocess
                import time

                input_path = sys.argv[1]
                output_path = sys.argv[2]
                warnings_path = sys.argv[3]

                TABLE_CONTINUATION = %s
                ORPHAN_CORRECTION = %s
                INTEGRITY_CHECK = %s
                CHECK_MARGIN_OVERFLOW = %s
                CHECK_FONT_SUBSTITUTION = %s
                MAX_PAGES = %s
                CONTINUES_LABEL = %s
                CONTINUATION_LABEL = %s
                CONCLUSION_LABEL = %s
                LABEL_STYLE_ID = %s

                warnings = []

                try:
                    import uno
                    from com.sun.star.beans import PropertyValue

                    localContext = uno.getComponentContext()
                    resolver = localContext.ServiceManager.createInstanceWithContext(
                        "com.sun.star.bridge.UnoUrlResolver", localContext)

                    # Start LibreOffice in listening mode
                    lo_proc = subprocess.Popen([
                        "soffice",
                        "--headless",
                        "--norestore",
                        "--accept=socket,host=localhost,port=2002;urp;StarOffice.ServiceManager"
                    ])
                    time.sleep(3)

                    try:
                        ctx = resolver.resolve(
                            "uno:socket,host=localhost,port=2002;urp;StarOffice.ComponentContext")
                        smgr = ctx.ServiceManager
                        desktop = smgr.createInstanceWithContext("com.sun.star.frame.Desktop", ctx)

                        url = uno.systemPathToFileUrl(os.path.abspath(input_path))
                        props = []
                        p = PropertyValue()
                        p.Name = "MacroExecutionMode"
                        p.Value = 4
                        props.append(p)

                        doc = desktop.loadComponentFromURL(url, "_blank", 0, tuple(props))

                        if TABLE_CONTINUATION:
                            apply_table_continuation(doc, CONTINUES_LABEL, CONTINUATION_LABEL, CONCLUSION_LABEL, LABEL_STYLE_ID, warnings)

                        if ORPHAN_CORRECTION:
                            apply_orphan_correction(doc, warnings)

                        if INTEGRITY_CHECK:
                            run_integrity_check(doc, CHECK_MARGIN_OVERFLOW, CHECK_FONT_SUBSTITUTION, MAX_PAGES, warnings)

                        try:
                            indexes = doc.getTextFields()
                            if hasattr(doc, 'getIndexes'):
                                idx_access = doc.getIndexes()
                                for i in range(idx_access.getCount()):
                                    idx = idx_access.getByIndex(i)
                                    if hasattr(idx, 'update'):
                                        idx.update()
                        except Exception as e:
                            warnings.append("TOC update failed: " + str(e))

                        save_props = []
                        sp = PropertyValue()
                        sp.Name = "FilterName"
                        sp.Value = "MS Word 2007 XML"
                        save_props.append(sp)
                        out_url = uno.systemPathToFileUrl(os.path.abspath(output_path))
                        doc.storeToURL(out_url, tuple(save_props))
                        doc.close(True)
                    finally:
                        lo_proc.terminate()
                        lo_proc.wait(timeout=10)

                except Exception as e:
                    warnings.append("UNO post-processing failed: " + str(e))
                    import shutil
                    shutil.copy2(input_path, output_path)

                with open(warnings_path, "w", encoding="utf-8") as wf:
                    wf.write("\\n".join(warnings))


                def apply_table_continuation(doc, continues_label, continuation_label, conclusion_label, style_id, warnings):
                    try:
                        from com.sun.star.text import TextContentAnchorType
                        enum_tables = doc.getTextTables()
                        for i in range(enum_tables.getCount()):
                            table = enum_tables.getByIndex(i)
                            # Detect page breaks by checking row anchor pages
                            pages_with_rows = {}
                            row_count = table.getRows().getCount()
                            for r in range(row_count):
                                row = table.getRows().getByIndex(r)
                                cell = table.getCellByPosition(0, r)
                                anchor_page = cell.createEnumeration().nextElement().PageNumberOffset
                                page = _get_text_page(cell)
                                if page not in pages_with_rows:
                                    pages_with_rows[page] = []
                                pages_with_rows[page].append(r)

                            sorted_pages = sorted(pages_with_rows.keys())
                            if len(sorted_pages) <= 1:
                                continue

                            # Insert continuation rows (simplified — marks only, no actual insertion in MVP)
                            warnings.append(
                                f"Table on multiple pages detected (table {i+1}): "
                                f"{len(sorted_pages)} pages. Manual review recommended."
                            )
                    except Exception as e:
                        warnings.append("Table continuation detection failed: " + str(e))


                def _get_text_page(cell):
                    try:
                        enum = cell.createEnumeration()
                        if enum.hasMoreElements():
                            para = enum.nextElement()
                            vp = para.createEnumeration()
                            if vp.hasMoreElements():
                                portion = vp.nextElement()
                                return portion.TextPortionType
                        return 0
                    except Exception:
                        return 0


                def apply_orphan_correction(doc, warnings):
                    try:
                        enum = doc.getText().createEnumeration()
                        prev_heading = None
                        prev_heading_page = -1
                        while enum.hasMoreElements():
                            element = enum.nextElement()
                            if not element.supportsService("com.sun.star.text.Paragraph"):
                                prev_heading = None
                                continue
                            style = element.ParaStyleName.lower()
                            if "heading 1" in style or style.endswith("1") and "head" in style:
                                prev_heading = element
                                try:
                                    prev_heading_page = element.PageNumberOffset
                                except Exception:
                                    prev_heading_page = -1
                            elif prev_heading is not None and prev_heading_page >= 0:
                                try:
                                    current_page = element.PageNumberOffset
                                    if current_page > prev_heading_page:
                                        # First content paragraph is on a different page — heading is orphan
                                        prev_heading.BreakType = 4  # com.sun.star.style.BreakType.PAGE_BEFORE
                                        warnings.append(
                                            f"Orphan heading corrected: page break inserted before heading on page {prev_heading_page}."
                                        )
                                except Exception:
                                    pass
                                prev_heading = None
                                prev_heading_page = -1
                    except Exception as e:
                        warnings.append("Orphan correction failed: " + str(e))


                def run_integrity_check(doc, check_margin, check_font, max_pages, warnings):
                    try:
                        page_count = doc.getDrawPages().getCount() if hasattr(doc, "getDrawPages") else -1
                        if page_count < 0:
                            # Writer documents use a different API
                            enum = doc.getText().createEnumeration()
                            page_count = 1
                            while enum.hasMoreElements():
                                el = enum.nextElement()
                                if el.supportsService("com.sun.star.text.Paragraph"):
                                    try:
                                        p = el.PageNumberOffset
                                        if p > page_count:
                                            page_count = p
                                    except Exception:
                                        pass

                        if max_pages is not None and page_count > max_pages:
                            warnings.append(
                                f"Document has {page_count} pages but profile declares max {max_pages}."
                            )

                        if check_font:
                            # Font substitution detection via document properties is limited in UNO
                            # — emit advisory warning only
                            warnings.append(
                                "Font substitution check requested. Manual verification recommended: "
                                "ensure all declared fonts are installed in the LibreOffice environment."
                            )
                    except Exception as e:
                        warnings.append("Integrity check failed: " + str(e))
                """.formatted(
                pyBool(tableContinuation), pyBool(orphanCorrection), pyBool(integrityCheck),
                pyBool(checkMarginOverflow), pyBool(checkFontSubstitution), maxPagesStr,
                pyStr(continuesLabel), pyStr(continuationLabel), pyStr(conclusionLabel), pyStr(labelStyleId)
        );

        Files.writeString(scriptFile, script, StandardCharsets.UTF_8);
    }

    private static String pyBool(boolean value) {
        return value ? "True" : "False";
    }

    private static String pyStr(String value) {
        if (value == null || value.isEmpty()) return "\"\"";
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static final Pattern LATENT_STYLES = Pattern.compile(
            "<w:latentStyles\\b[^>]*>.*?</w:latentStyles>", Pattern.DOTALL);

    private static byte[] cleanDocxForLibreOffice(byte[] docxBytes) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(docxBytes));
                 ZipOutputStream zout = new ZipOutputStream(out)) {
                ZipEntry entry;
                while ((entry = zin.getNextEntry()) != null) {
                    byte[] entryBytes = zin.readAllBytes();
                    if (entry.getName().equals("word/styles.xml")) {
                        String xml = new String(entryBytes, StandardCharsets.UTF_8);
                        xml = LATENT_STYLES.matcher(xml).replaceAll("");
                        entryBytes = xml.getBytes(StandardCharsets.UTF_8);
                    }
                    ZipEntry newEntry = new ZipEntry(entry.getName());
                    zout.putNextEntry(newEntry);
                    zout.write(entryBytes);
                    zout.closeEntry();
                }
            }
            return out.toByteArray();
        } catch (Exception e) {
            log.warn("DOCX cleanup for LibreOffice failed — using original bytes. Cause: {}", e.getMessage());
            return docxBytes;
        }
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
