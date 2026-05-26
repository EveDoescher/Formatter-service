package com.abntbuilder.formatter.application.export;

import java.util.Optional;

public interface GeneratedDocxExportStore {

    GeneratedDocxExport save(String fileName, byte[] bytes);

    Optional<GeneratedDocxExport> findById(String id);
}