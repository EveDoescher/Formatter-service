package com.abntbuilder.formatter.rendering.component;

import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import java.util.List;

public interface ComponentRenderResult {
    List<DocxBlock> blocks();
}
