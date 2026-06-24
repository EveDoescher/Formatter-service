package com.abntbuilder.formatter.rendering.component;

import com.abntbuilder.formatter.document.component.DocumentComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyContentMetadata;

import java.util.List;

public interface MetadataConsumingRenderer<T extends DocumentComponent>
        extends ComponentRenderer<T> {

    List<DocxBlock> renderWithMetadata(T component, DocumentProfile profile, BodyContentMetadata metadata);

    @Override
    default List<DocxBlock> render(T component, DocumentProfile profile) {
        return renderWithMetadata(component, profile, BodyContentMetadata.empty());
    }

    @SuppressWarnings("unchecked")
    default List<DocxBlock> renderComponentWithMetadata(
            DocumentComponent component, DocumentProfile profile, BodyContentMetadata metadata) {
        return renderWithMetadata((T) component, profile, metadata);
    }
}
