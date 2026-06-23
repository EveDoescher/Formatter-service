package com.abntbuilder.formatter.document.component.bodycontent;

public sealed interface BodyInline permits BodyText, BodyCitationCall, BodyQuoteText, BodyAbbreviation, BodyFootnote, BodyCrossReference {

    String renderedText();
}
