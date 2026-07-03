package com.abntbuilder.formatter.document.component.singlepage;

public sealed interface ContentValue
        permits TextValue, TextListValue, ComposedTextValue, SignatureBlockListValue {}
