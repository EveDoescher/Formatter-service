package com.abntbuilder.formatter.rendering.singlepage;

public sealed interface SinglePageLayoutElement permits SinglePageTextLines, SinglePageSpacerLines {

    int lineCount();

    int heightTwips();
}
