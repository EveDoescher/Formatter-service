package com.abntbuilder.formatter.rendering.layout.singlepage;

public sealed interface SinglePageLayoutElement permits SinglePageTextLines, SinglePageSpacerLines {

    int lineCount();

    int heightTwips();
}
