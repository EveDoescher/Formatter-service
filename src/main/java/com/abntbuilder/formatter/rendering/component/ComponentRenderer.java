package com.abntbuilder.formatter.rendering.component;

import com.abntbuilder.formatter.document.component.DocumentComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.profile.model.DocumentProfile;

import java.util.List;

public interface ComponentRenderer<T extends DocumentComponent> {

    String componentId();

    Class<T> componentType();

    List<DocxBlock> render(T component, DocumentProfile profile);

    default List<DocxBlock> renderComponent(DocumentComponent component, DocumentProfile profile) {
        return render(componentType().cast(component), profile);
    }
}
