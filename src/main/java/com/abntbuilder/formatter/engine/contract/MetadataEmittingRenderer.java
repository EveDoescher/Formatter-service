package com.abntbuilder.formatter.engine.contract;

import com.abntbuilder.formatter.engine.model.content.DocumentComponent;
import com.abntbuilder.formatter.engine.model.output.DocxBlock;
import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;

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
