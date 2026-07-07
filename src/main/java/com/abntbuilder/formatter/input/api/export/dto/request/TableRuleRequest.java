package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.profile.TextAlignment;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.DisplayObjectSourcePlacement;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.NumberingStrategy;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.TableRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TableRuleRequest(
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
        @NotNull Boolean repeatHeaderOnPageBreak,
        @NotNull NumberingStrategy numberingStrategy,
        @NotBlank String label,
        String separator
) {

    TableRule toDomain() {
        return new TableRule(
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
                repeatHeaderOnPageBreak,
                numberingStrategy,
                label,
                separator
        );
    }
}
