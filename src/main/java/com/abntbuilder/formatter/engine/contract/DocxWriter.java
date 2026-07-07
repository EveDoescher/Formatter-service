package com.abntbuilder.formatter.engine.contract;

import com.abntbuilder.formatter.engine.model.output.DocxDocument;

public interface DocxWriter {

    byte[] write(DocxDocument document);
}