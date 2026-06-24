package com.abntbuilder.formatter.rendering.component.listofcharts;

import com.abntbuilder.formatter.document.component.listofcharts.ListOfChartsComponent;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyContentMetadata;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyDisplayObjectMetadata;
import com.abntbuilder.formatter.rendering.component.indexlist.AbstractIndexListRenderer;

import java.util.List;
import java.util.function.Function;

public final class ListOfChartsRenderer extends AbstractIndexListRenderer<ListOfChartsComponent> {

    public static final String COMPONENT_ID = "listOfCharts";

    @Override public String componentId() { return COMPONENT_ID; }
    @Override public Class<ListOfChartsComponent> componentType() { return ListOfChartsComponent.class; }

    @Override
    protected Function<BodyContentMetadata, List<BodyDisplayObjectMetadata>> metadataExtractor() {
        return BodyContentMetadata::charts;
    }
}
