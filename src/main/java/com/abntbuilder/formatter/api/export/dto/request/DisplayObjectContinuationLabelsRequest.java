package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.bodycontent.DisplayObjectContinuationLabels;

public record DisplayObjectContinuationLabelsRequest(
        String first,
        String middle,
        String last
) {

    DisplayObjectContinuationLabels toDomain() {
        return new DisplayObjectContinuationLabels(first, middle, last);
    }
}
