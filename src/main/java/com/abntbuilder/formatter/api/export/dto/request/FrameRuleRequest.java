package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.profile.model.component.bodycontent.DisplayObjectSourcePlacement;
import com.abntbuilder.formatter.profile.model.component.bodycontent.FrameRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FrameRuleRequest(
        @NotBlank String captionStyleId,
        @NotBlank String sourceStyleId,
        @NotBlank String headerStyleId,
        @NotBlank String cellStyleId,
        @NotBlank String captionTemplate,
        @NotBlank String sourceTemplate,
        @Valid @NotNull DisplayObjectContinuationLabelsRequest continuationLabels,
        @NotNull DisplayObjectSourcePlacement sourcePlacement,
        @NotNull TextAlignment tableAlignment,
        @NotNull BigDecimal widthPercent,
        @NotNull Boolean repeatHeaderOnPageBreak
) {

    public FrameRule toDomain() {
        return new FrameRule(
                captionStyleId,
                sourceStyleId,
                headerStyleId,
                cellStyleId,
                captionTemplate,
                sourceTemplate,
                continuationLabels.toDomain(),
                sourcePlacement,
                tableAlignment,
                widthPercent,
                repeatHeaderOnPageBreak
        );
    }
}
