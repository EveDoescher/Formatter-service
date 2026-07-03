package com.abntbuilder.formatter.rendering.component.bodycontent;

import java.util.Optional;

public record DisplayObjectContinuationPart(
        int number,
        int index,
        int count,
        Optional<String> continuationLabel
) {

    public boolean last() {
        return index == count;
    }
}
