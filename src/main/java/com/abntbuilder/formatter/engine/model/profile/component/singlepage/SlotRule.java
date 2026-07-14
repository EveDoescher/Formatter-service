package com.abntbuilder.formatter.engine.model.profile.component.singlepage;

public sealed interface SlotRule
        permits TextSlotRule, TextListSlotRule, ComposedTextSlotRule, SignatureBlockListSlotRule {

    boolean required();

    String description();

    String placeholder();
}
