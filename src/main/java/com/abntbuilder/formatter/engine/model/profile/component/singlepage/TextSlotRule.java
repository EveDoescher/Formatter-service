package com.abntbuilder.formatter.engine.model.profile.component.singlepage;

public record TextSlotRule(boolean required, String description, String placeholder) implements SlotRule {}
