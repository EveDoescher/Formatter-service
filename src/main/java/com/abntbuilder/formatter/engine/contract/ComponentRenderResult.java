package com.abntbuilder.formatter.engine.contract;

import com.abntbuilder.formatter.engine.model.output.DocxBlock;
import java.util.List;

public interface ComponentRenderResult {
    List<DocxBlock> blocks();
}
