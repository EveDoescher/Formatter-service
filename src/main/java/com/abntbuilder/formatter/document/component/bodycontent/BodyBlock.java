package com.abntbuilder.formatter.document.component.bodycontent;

public sealed interface BodyBlock permits BodyParagraph, BodyLongQuote, BodyList, NumberedDisplayObject, BodyEquation {
}
