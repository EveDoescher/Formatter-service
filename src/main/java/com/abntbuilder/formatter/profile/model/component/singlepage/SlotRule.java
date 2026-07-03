package com.abntbuilder.formatter.profile.model.component.singlepage;

public sealed interface SlotRule
        permits TextSlotRule, TextListSlotRule, ComposedTextSlotRule, SignatureBlockListSlotRule {

    boolean required();
}
