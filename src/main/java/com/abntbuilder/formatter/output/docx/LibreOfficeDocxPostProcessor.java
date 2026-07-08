package com.abntbuilder.formatter.output.docx;

import com.abntbuilder.formatter.config.LibreOfficeProperties;
import com.abntbuilder.formatter.engine.contract.DocxPostProcessor;
import com.abntbuilder.formatter.engine.contract.PostProcessorResult;
import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;
import com.abntbuilder.formatter.engine.model.profile.PostProcessingRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
    public PostProcessorResult process(byte[] docxBytes, DocumentProfile profile) {
        Objects.requireNonNull(profile, "profile must not be null");

        PostProcessingRule postProcessing = profile.postProcessingRule().orElse(null);
        List<String> warnings = new ArrayList<>();

        if (!isLoAvailable()) {
            return PostProcessorResult.of(docxBytes, warnings);
        }

        Path workDir = null;
        try {
            workDir = Files.createTempDirectory("formatter-lo-");
            byte[] resolvedBytes = resolveFields(docxBytes, workDir, warnings);

            if (postProcessing != null && postProcessing.pdfOutput()
                    .map(PostProcessingRule.PdfOutputRule::enabled).orElse(false)) {
                generatePdf(resolvedBytes, workDir, warnings);
            }

            return PostProcessorResult.of(resolvedBytes, warnings);
        } catch (IOException | InterruptedException e) {
            log.warn("LibreOffice post-processing error.", e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return PostProcessorResult.of(docxBytes, warnings);
        } finally {
            deleteSilently(workDir);
        }
    }

    private boolean isLoAvailable() {
        return new java.io.File(properties.getExecutablePath()).canExecute()
                || findOnPath(properties.getExecutablePath());
    }

    private static boolean findOnPath(String executable) {
        String path = System.getenv("PATH");
        if (path == null) return false;
        for (String dir : path.split(java.io.File.pathSeparator)) {
            if (new java.io.File(dir, executable).canExecute()) return true;
        }
        return false;
    }

    // Opens the DOCX in LibreOffice via a Python-UNO script, updates all indexes
    // and fields, then saves back to DOCX. Unlike --convert-to docx (which does a
    // full format round-trip and discards unresolved fields), this path only calls
    // refresh()/update() and storeToURL() — fields are replaced with resolved values
    // while the rest of the document structure is preserved.
    private byte[] resolveFields(byte[] docxBytes, Path workDir, List<String> warnings)
            throws IOException, InterruptedException {

        Path inputFile = workDir.resolve("input.docx");
        Path outputFile = workDir.resolve("output.docx");
        Path scriptFile = workDir.resolve("resolve_fields.py");

        Files.write(inputFile, docxBytes);

        String inputUrl = inputFile.toUri().toString();
        String outputUrl = outputFile.toUri().toString();

        // The script starts soffice as a UNO listener on a named pipe, connects to it,
        // opens the document, refreshes all indexes and fields, saves, and shuts down.
        String sofficeBin = properties.getExecutablePath();
        String script = "import subprocess, time, os, uno\n"
            + "from com.sun.star.beans import PropertyValue\n"
            + "\n"
            + "def prop(name, value):\n"
            + "    p = PropertyValue()\n"
            + "    p.Name = name\n"
            + "    p.Value = value\n"
            + "    return p\n"
            + "\n"
            + "def connect(pipe_name, retries=20, delay=0.5):\n"
            + "    localCtx = uno.getComponentContext()\n"
            + "    resolver = localCtx.ServiceManager.createInstanceWithContext(\n"
            + "        'com.sun.star.bridge.UnoUrlResolver', localCtx)\n"
            + "    url = 'uno:pipe,name=' + pipe_name + ';urp;StarOffice.ComponentContext'\n"
            + "    for _ in range(retries):\n"
            + "        try:\n"
            + "            return resolver.resolve(url)\n"
            + "        except Exception:\n"
            + "            time.sleep(delay)\n"
            + "    raise RuntimeError('Could not connect to LibreOffice on pipe: ' + pipe_name)\n"
            + "\n"
            + "pipe_name = 'formatter-lo-pipe-' + str(os.getpid())\n"
            + "proc = subprocess.Popen([\n"
            + "    '" + sofficeBin + "', '--headless', '--norestore', '--nofirststartwizard',\n"
            + "    '--accept=pipe,name=' + pipe_name + ';urp;StarOffice.ServiceManager'\n"
            + "])\n"
            + "try:\n"
            + "    remoteCtx = connect(pipe_name)\n"
            + "    smgr = remoteCtx.ServiceManager\n"
            + "    desktop = smgr.createInstanceWithContext('com.sun.star.frame.Desktop', remoteCtx)\n"
            + "    doc = desktop.loadComponentFromURL(\n"
            + "        '" + inputUrl + "', '_blank', 0,\n"
            + "        (prop('Hidden', True), prop('MacroExecutionMode', 4)))\n"
            + "    try:\n"
            + "        doc.getTextFields().refresh()\n"
            + "    except Exception:\n"
            + "        pass\n"
            + "    try:\n"
            + "        for idx in doc.getDocumentIndexes():\n"
            + "            idx.update()\n"
            + "    except Exception:\n"
            + "        pass\n"
            + "    try:\n"
            + "        doc.getTextFields().refresh()\n"
            + "    except Exception:\n"
            + "        pass\n"
            + "    doc.storeToURL(\n"
            + "        '" + outputUrl + "',\n"
            + "        (prop('FilterName', 'MS Word 2007 XML'), prop('Overwrite', True)))\n"
            + "    doc.close(True)\n"
            + "    desktop.terminate()\n"
            + "finally:\n"
            + "    proc.wait(timeout=10)\n";

        Files.writeString(scriptFile, script);

        // python3 provided by LibreOffice itself (python-uno bridge)
        String pythonExe = resolveLoPython();

        ProcessBuilder pb = new ProcessBuilder(pythonExe, scriptFile.toAbsolutePath().toString());
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String output = new String(process.getInputStream().readAllBytes());

        boolean finished = process.waitFor(properties.getTimeoutSeconds(), TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            warnings.add("Field resolution timed out — delivering unresolved DOCX.");
            return docxBytes;
        }
        if (process.exitValue() != 0) {
            log.warn("Field resolution script failed (exit {}): {}", process.exitValue(), output);
            warnings.add("Field resolution failed — delivering unresolved DOCX.");
            return docxBytes;
        }
        if (!Files.exists(outputFile)) {
            warnings.add("Field resolution produced no output — delivering unresolved DOCX.");
            return docxBytes;
        }

        return Files.readAllBytes(outputFile);
    }

    private String resolveLoPython() {
        // LibreOffice ships its own Python interpreter with UNO bindings.
        // Common locations across distros and macOS.
        String loBase = properties.getExecutablePath()
                .replace("soffice", "")
                .replace("/bin/", "/");
        String[] candidates = {
            "/usr/lib/libreoffice/program/python3",
            "/usr/lib/libreoffice/program/python",
            "/opt/libreoffice/program/python3",
            "/Applications/LibreOffice.app/Contents/MacOS/python3",
            loBase + "program/python3",
        };
        for (String candidate : candidates) {
            if (new java.io.File(candidate).canExecute()) {
                return candidate;
            }
        }
        return "python3";
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
