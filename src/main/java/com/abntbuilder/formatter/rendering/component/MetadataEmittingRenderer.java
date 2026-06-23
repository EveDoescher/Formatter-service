package com.abntbuilder.formatter.rendering.component;

import com.abntbuilder.formatter.document.component.DocumentComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.profile.model.DocumentProfile;

import java.util.List;

public interface MetadataEmittingRenderer<T extends DocumentComponent, R extends ComponentRenderResult>
        extends ComponentRenderer<T> {

    R renderWithMetadata(T component, DocumentProfile profile);

    @Override
    default List<DocxBlock> render(T component, DocumentProfile profile) {
        return renderWithMetadata(component, profile).blocks();
    }

    @SuppressWarnings("unchecked")
    default ComponentRenderResult renderComponentWithMetadata(
            DocumentComponent component, DocumentProfile profile) {
        return renderWithMetadata((T) component, profile);
    }
}
