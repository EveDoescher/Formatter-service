package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.DisplayObjectContinuationLabels;

public record DisplayObjectContinuationLabelsRequest(
        String first,
        String middle,
        String last
) {

    DisplayObjectContinuationLabels toDomain() {
        return new DisplayObjectContinuationLabels(first, middle, last);
    }
}
