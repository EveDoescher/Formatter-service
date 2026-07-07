package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.content.bodycontent.BodyCrossReference;
import com.abntbuilder.formatter.engine.model.content.bodycontent.CrossReferenceDisplayMode;
import com.abntbuilder.formatter.engine.model.content.bodycontent.CrossReferenceTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BodyCrossReferenceRequest(
        @NotBlank String targetId,
        @NotNull CrossReferenceTargetType targetType,
        @NotNull CrossReferenceDisplayMode displayMode
) {
    public BodyCrossReference toDomain() {
        return new BodyCrossReference(targetId, targetType, displayMode);
    }
}
