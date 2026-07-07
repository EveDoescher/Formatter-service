package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.profile.TextAlignment;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.DisplayObjectSourcePlacement;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.FigureRule;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.ImageFitPolicy;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.NumberingStrategy;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FigureRuleRequest(
        @NotBlank String captionStyleId,
        @NotBlank String sourceStyleId,
        @NotBlank String captionTemplate,
        @NotBlank String sourceTemplate,
        @Valid @NotNull DisplayObjectContinuationLabelsRequest continuationLabels,
        @NotNull DisplayObjectSourcePlacement sourcePlacement,
        @NotNull TextAlignment imageAlignment,
        @NotNull BigDecimal maxWidthCm,
        @NotNull BigDecimal maxHeightCm,
        @NotNull BigDecimal defaultDpi,
        @NotNull Integer maxImageBytes,
        @NotNull Integer urlFetchTimeoutSeconds,
        @NotNull ImageFitPolicy fitPolicy,
        @NotNull NumberingStrategy numberingStrategy,
        @NotBlank String label,
        String separator
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
                fitPolicy,
                numberingStrategy,
                label,
                separator
        );
    }
}
