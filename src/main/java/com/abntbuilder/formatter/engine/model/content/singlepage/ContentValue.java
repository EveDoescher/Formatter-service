package com.abntbuilder.formatter.engine.model.content.singlepage;

public sealed interface ContentValue
        permits TextValue, TextListValue, ComposedTextValue, SignatureBlockListValue, TableValue, EntryListValue {}
