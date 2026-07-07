package com.abntbuilder.formatter.engine.contract;

import com.abntbuilder.formatter.engine.model.content.DocumentComponent;
import com.abntbuilder.formatter.engine.model.output.DocxBlock;
import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;
import com.abntbuilder.formatter.rendering.phase0.Phase0Index;

import java.util.List;

public interface MetadataConsumingRenderer<T extends DocumentComponent>
        extends ComponentRenderer<T> {

    List<DocxBlock> renderWithMetadata(T component, DocumentProfile profile, Phase0Index phase0Index);

    @Override
    default List<DocxBlock> render(T component, DocumentProfile profile) {
        return renderWithMetadata(component, profile, Phase0Index.empty());
    }

    @SuppressWarnings("unchecked")
    default List<DocxBlock> renderComponentWithMetadata(
            DocumentComponent component, DocumentProfile profile, Phase0Index phase0Index) {
        return renderWithMetadata((T) component, profile, phase0Index);
    }
}
