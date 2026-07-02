package com.abntbuilder.formatter.rendering.component.listoffigures;

import com.abntbuilder.formatter.document.component.listoffigures.ListOfFiguresComponent;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyDisplayObjectMetadata;
import com.abntbuilder.formatter.rendering.component.indexlist.AbstractIndexListRenderer;
import com.abntbuilder.formatter.rendering.phase0.Phase0Index;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class ListOfFiguresRenderer extends AbstractIndexListRenderer<ListOfFiguresComponent> {

    public static final String COMPONENT_ID = "listOfFigures";

    @Override public String componentId() { return COMPONENT_ID; }
    @Override public Class<ListOfFiguresComponent> componentType() { return ListOfFiguresComponent.class; }

    @Override
    protected Function<Phase0Index, List<BodyDisplayObjectMetadata>> metadataExtractor() {
        return p -> new ArrayList<>(p.figures().values());
    }
}
