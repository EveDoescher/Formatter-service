package com.abntbuilder.formatter.rendering.component.singlepage;

import com.abntbuilder.formatter.document.component.singlepage.ComposedTextValue;
import com.abntbuilder.formatter.document.component.singlepage.ContentValue;
import com.abntbuilder.formatter.document.component.singlepage.SignatureBlockListValue;
import com.abntbuilder.formatter.document.component.singlepage.SinglePageContent;
import com.abntbuilder.formatter.document.component.singlepage.TextListValue;
import com.abntbuilder.formatter.document.component.singlepage.TextValue;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.component.singlepage.ComposedTextSlotRule;
import com.abntbuilder.formatter.profile.model.component.singlepage.SignatureBlockListSlotRule;
import com.abntbuilder.formatter.profile.model.component.singlepage.SinglePageComponentRule;
import com.abntbuilder.formatter.profile.model.component.singlepage.SlotRule;
import com.abntbuilder.formatter.profile.model.component.singlepage.TextListSlotRule;
import com.abntbuilder.formatter.profile.model.component.singlepage.TextSlotRule;
import com.abntbuilder.formatter.shared.exception.InvalidSinglePageContentException;

import java.util.Map;
import java.util.Objects;

public final class SinglePageContentValidator {

    public void validate(
            SinglePageContent content,
            SinglePageComponentRule rule,
            DocumentProfile profile
    ) {
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(rule, "rule must not be null");
        Objects.requireNonNull(profile, "profile must not be null");

        validateRequiredSlots(content, rule);
        validateSlotTypeCompatibility(content, rule);
    }

    private static void validateRequiredSlots(SinglePageContent content, SinglePageComponentRule rule) {
        for (Map.Entry<String, SlotRule> entry : rule.slots().entrySet()) {
            String slotId = entry.getKey();
            SlotRule slotRule = entry.getValue();

            if (slotRule.required() && !content.slots().containsKey(slotId)) {
                throw InvalidSinglePageContentException.missingRequiredSlot(content.componentId(), slotId);
            }
        }
    }

    private static void validateSlotTypeCompatibility(SinglePageContent content, SinglePageComponentRule rule) {
        for (Map.Entry<String, ContentValue> entry : content.slots().entrySet()) {
            String slotId = entry.getKey();
            ContentValue value = entry.getValue();
            SlotRule slotRule = rule.slots().get(slotId);

            if (slotRule == null) {
                continue;
            }

            String expected = expectedTypeName(slotRule);
            String actual = value.getClass().getSimpleName();

            boolean compatible = switch (slotRule) {
                case TextSlotRule ignored -> value instanceof TextValue;
                case TextListSlotRule ignored -> value instanceof TextListValue;
                case ComposedTextSlotRule ignored -> value instanceof ComposedTextValue;
                case SignatureBlockListSlotRule ignored -> value instanceof SignatureBlockListValue;
            };

            if (!compatible) {
                throw InvalidSinglePageContentException.slotTypeMismatch(
                        content.componentId(), slotId, expected, actual);
            }
        }
    }

    private static String expectedTypeName(SlotRule slotRule) {
        return switch (slotRule) {
            case TextSlotRule ignored -> "TextValue";
            case TextListSlotRule ignored -> "TextListValue";
            case ComposedTextSlotRule ignored -> "ComposedTextValue";
            case SignatureBlockListSlotRule ignored -> "SignatureBlockListValue";
        };
    }
}
