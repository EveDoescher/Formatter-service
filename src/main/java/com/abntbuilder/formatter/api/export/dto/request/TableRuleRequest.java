package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.profile.model.component.bodycontent.DisplayObjectSourcePlacement;
import com.abntbuilder.formatter.profile.model.component.bodycontent.TableRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TableRuleRequest(
        String captionStyleId,
        String sourceStyleId,
        String headerStyleId,
        String cellStyleId,
        String captionTemplate,
        String sourceTemplate,
        @Valid @NotNull DisplayObjectContinuationLabelsRequest continuationLabels,
        DisplayObjectSourcePlacement sourcePlacement,
        TextAlignment tableAlignment,
        BigDecimal widthPercent,
        Boolean repeatHeaderOnPageBreak
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
                repeatHeaderOnPageBreak
        );
    }
}
