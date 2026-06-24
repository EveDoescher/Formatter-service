package com.abntbuilder.formatter.rendering.component.listoftables;

import com.abntbuilder.formatter.document.component.listoftables.ListOfTablesComponent;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyContentMetadata;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyDisplayObjectMetadata;
import com.abntbuilder.formatter.rendering.component.indexlist.AbstractIndexListRenderer;

import java.util.List;
import java.util.function.Function;

public final class ListOfTablesRenderer extends AbstractIndexListRenderer<ListOfTablesComponent> {

    public static final String COMPONENT_ID = "listOfTables";

    @Override public String componentId() { return COMPONENT_ID; }
    @Override public Class<ListOfTablesComponent> componentType() { return ListOfTablesComponent.class; }

    @Override
    protected Function<BodyContentMetadata, List<BodyDisplayObjectMetadata>> metadataExtractor() {
        return BodyContentMetadata::tables;
    }
}
