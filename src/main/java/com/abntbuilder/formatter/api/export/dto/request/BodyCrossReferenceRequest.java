package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.bodycontent.BodyCrossReference;
import com.abntbuilder.formatter.document.component.bodycontent.CrossReferenceDisplayMode;
import com.abntbuilder.formatter.document.component.bodycontent.CrossReferenceTargetType;
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
