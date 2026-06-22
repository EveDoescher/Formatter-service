package com.abntbuilder.formatter.document.component.bodycontent;

import java.util.Optional;

public sealed interface NumberedDisplayObject extends BodyBlock permits BodyFigure, BodyTable, BodyFrame, BodyCodeListing, BodyChart {

    String id();

    Optional<String> continuationGroupId();

    String caption();

    Optional<String> source();

    default String displayGroupKey() {
        return continuationGroupId().orElse(id());
    }
}
