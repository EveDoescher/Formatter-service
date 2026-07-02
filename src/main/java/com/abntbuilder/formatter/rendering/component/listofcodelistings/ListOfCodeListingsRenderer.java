package com.abntbuilder.formatter.rendering.component.listofcodelistings;

import com.abntbuilder.formatter.document.component.listofcodelistings.ListOfCodeListingsComponent;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyDisplayObjectMetadata;
import com.abntbuilder.formatter.rendering.component.indexlist.AbstractIndexListRenderer;
import com.abntbuilder.formatter.rendering.phase0.Phase0Index;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class ListOfCodeListingsRenderer extends AbstractIndexListRenderer<ListOfCodeListingsComponent> {

    public static final String COMPONENT_ID = "listOfCodeListings";

    @Override public String componentId() { return COMPONENT_ID; }
    @Override public Class<ListOfCodeListingsComponent> componentType() { return ListOfCodeListingsComponent.class; }

    @Override
    protected Function<Phase0Index, List<BodyDisplayObjectMetadata>> metadataExtractor() {
        return p -> new ArrayList<>(p.codeListings().values());
    }
}
