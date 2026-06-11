package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.profile.model.component.bodycontent.DisplayObjectSourcePlacement;
import com.abntbuilder.formatter.profile.model.component.bodycontent.FigureRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.ImageFitPolicy;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FigureRuleRequest(
        String captionStyleId,
        String sourceStyleId,
        String captionTemplate,
        String sourceTemplate,
        @Valid @NotNull DisplayObjectContinuationLabelsRequest continuationLabels,
        DisplayObjectSourcePlacement sourcePlacement,
        TextAlignment imageAlignment,
        BigDecimal maxWidthCm,
        BigDecimal maxHeightCm,
        BigDecimal defaultDpi,
        Integer maxImageBytes,
        Integer urlFetchTimeoutSeconds,
        ImageFitPolicy fitPolicy
) {

    FigureRule toDomain() {
        return new FigureRule(
                captionStyleId,
                sourceStyleId,
                captionTemplate,
                sourceTemplate,
                continuationLabels.toDomain(),
                sourcePlacement,
                imageAlignment,
                maxWidthCm,
                maxHeightCm,
                defaultDpi,
                maxImageBytes,
                urlFetchTimeoutSeconds,
                fitPolicy
        );
    }
}
