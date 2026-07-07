package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.ChartRule;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.DisplayObjectSourcePlacement;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.NumberingStrategy;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChartRuleRequest(
        @NotBlank String captionStyleId,
        @NotBlank String sourceStyleId,
        @NotBlank String captionTemplate,
        @NotBlank String sourceTemplate,
        @Valid @NotNull DisplayObjectContinuationLabelsRequest continuationLabels,
        @NotNull DisplayObjectSourcePlacement sourcePlacement,
        @Valid @NotNull FigureRuleRequest imageRule,
        @NotNull NumberingStrategy numberingStrategy,
        @NotBlank String label,
        String separator
) {
    public ChartRule toDomain() {
        return new ChartRule(
                captionStyleId,
                sourceStyleId,
                captionTemplate,
                sourceTemplate,
                continuationLabels.toDomain(),
                sourcePlacement,
                imageRule.toDomain(),
                numberingStrategy,
                label,
                separator
        );
    }
}
