package com.abntbuilder.formatter.engine.contract;

import com.abntbuilder.formatter.engine.model.content.DocumentComponent;
import com.abntbuilder.formatter.engine.model.output.DocxBlock;
import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;

import java.util.List;

public interface ComponentRenderer<T extends DocumentComponent> {

    String componentId();

    Class<T> componentType();

    List<DocxBlock> render(T component, DocumentProfile profile);

    default List<DocxBlock> renderComponent(DocumentComponent component, DocumentProfile profile) {
        return render(componentType().cast(component), profile);
    }
}
