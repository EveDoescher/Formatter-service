package com.abntbuilder.formatter.rendering.component.cover.layout;

public sealed interface CoverLayoutElement permits CoverTextLines, CoverSpacerLines {

    int lineCount();
}
