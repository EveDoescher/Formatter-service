package com.abntbuilder.formatter.engine.model.content.bodycontent;

public sealed interface BodyInline permits BodyText, BodyCitationCall, BodyQuoteText, BodyAbbreviation, BodyFootnote, BodyCrossReference {

    String renderedText();
}
