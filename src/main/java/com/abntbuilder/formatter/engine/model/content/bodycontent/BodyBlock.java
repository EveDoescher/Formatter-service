package com.abntbuilder.formatter.engine.model.content.bodycontent;

public sealed interface BodyBlock permits BodyParagraph, BodyLongQuote, BodyList, NumberedDisplayObject, BodyEquation {
}
