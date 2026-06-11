package com.abntbuilder.formatter.rendering.component.bodycontent;

import java.util.Optional;

record DisplayObjectContinuationPart(
        int number,
        int index,
        int count,
        Optional<String> continuationLabel
) {

    boolean last() {
        return index == count;
    }
}
