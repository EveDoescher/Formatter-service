package com.abntbuilder.formatter.rendering.component.listoffigures;

import com.abntbuilder.formatter.document.component.listoffigures.ListOfFiguresComponent;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyContentMetadata;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyDisplayObjectMetadata;
import com.abntbuilder.formatter.rendering.component.indexlist.AbstractIndexListRenderer;

import java.util.List;
import java.util.function.Function;

public final class ListOfFiguresRenderer extends AbstractIndexListRenderer<ListOfFiguresComponent> {

    public static final String COMPONENT_ID = "listOfFigures";

    @Override public String componentId() { return COMPONENT_ID; }
    @Override public Class<ListOfFiguresComponent> componentType() { return ListOfFiguresComponent.class; }

    @Override
    protected Function<BodyContentMetadata, List<BodyDisplayObjectMetadata>> metadataExtractor() {
        return BodyContentMetadata::figures;
    }
}
