package com.abntbuilder.formatter.engine.contract;

import com.abntbuilder.formatter.engine.model.content.DocumentComponent;
import com.abntbuilder.formatter.rendering.phase0.Phase0Index;

public interface Phase0ConsumingRenderer<T extends DocumentComponent, R extends ComponentRenderResult>
        extends MetadataEmittingRenderer<T, R> {

    R renderWithPhase0(T component, com.abntbuilder.formatter.engine.model.profile.DocumentProfile profile, Phase0Index phase0Index);

    @Override
    default R renderWithMetadata(T component, com.abntbuilder.formatter.engine.model.profile.DocumentProfile profile) {
        return renderWithPhase0(component, profile, Phase0Index.empty());
    }

    @SuppressWarnings("unchecked")
    default R renderComponentWithPhase0(
            DocumentComponent component,
            com.abntbuilder.formatter.engine.model.profile.DocumentProfile profile,
            Phase0Index phase0Index) {
        return renderWithPhase0((T) component, profile, phase0Index);
    }
}
