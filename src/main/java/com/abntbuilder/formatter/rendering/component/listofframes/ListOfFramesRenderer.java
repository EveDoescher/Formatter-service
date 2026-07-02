package com.abntbuilder.formatter.rendering.component.listofframes;

import com.abntbuilder.formatter.document.component.listofframes.ListOfFramesComponent;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyDisplayObjectMetadata;
import com.abntbuilder.formatter.rendering.component.indexlist.AbstractIndexListRenderer;
import com.abntbuilder.formatter.rendering.phase0.Phase0Index;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class ListOfFramesRenderer extends AbstractIndexListRenderer<ListOfFramesComponent> {

    public static final String COMPONENT_ID = "listOfFrames";

    @Override public String componentId() { return COMPONENT_ID; }
    @Override public Class<ListOfFramesComponent> componentType() { return ListOfFramesComponent.class; }

    @Override
    protected Function<Phase0Index, List<BodyDisplayObjectMetadata>> metadataExtractor() {
        return p -> new ArrayList<>(p.frames().values());
    }
}
