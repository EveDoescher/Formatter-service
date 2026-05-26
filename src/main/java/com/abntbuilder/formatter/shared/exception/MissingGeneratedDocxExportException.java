package com.abntbuilder.formatter.shared.exception;

public class MissingGeneratedDocxExportException extends RuntimeException {

    public MissingGeneratedDocxExportException(String exportId) {
        super("Generated DOCX export not found for id: " + exportId);
    }
}