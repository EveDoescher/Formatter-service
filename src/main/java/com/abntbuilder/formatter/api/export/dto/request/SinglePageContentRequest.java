package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.singlepage.ComposedTextValue;
import com.abntbuilder.formatter.document.component.singlepage.ContentValue;
import com.abntbuilder.formatter.document.component.singlepage.SignatureBlockListValue;
import com.abntbuilder.formatter.document.component.singlepage.SinglePageContent;
import com.abntbuilder.formatter.document.component.singlepage.TextListValue;
import com.abntbuilder.formatter.document.component.singlepage.TextValue;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SinglePageContentRequest {

    private final Map<String, Object> rawSlots = new HashMap<>();

    @JsonAnySetter
    public void setSlot(String key, Object value) {
        rawSlots.put(key, value);
    }

    public boolean hasSlots() {
        return !rawSlots.isEmpty();
    }

    public SinglePageContent toDomain(String componentId) {
        Map<String, ContentValue> slots = new HashMap<>();

        for (Map.Entry<String, Object> entry : rawSlots.entrySet()) {
            String slotId = entry.getKey();
            Object raw = entry.getValue();

            if (raw == null) {
                continue;
            }

            slots.put(slotId, toContentValue(componentId, slotId, raw));
        }

        return new SinglePageContent(componentId, slots);
    }

    @SuppressWarnings("unchecked")
    private static ContentValue toContentValue(String componentId, String slotId, Object raw) {
        if (raw instanceof String s) {
            return new TextValue(s);
        }

        if (raw instanceof List<?> list) {
            if (list.isEmpty()) {
                throw new IllegalArgumentException(
                        "Slot '" + slotId + "' in component '" + componentId
                                + "' must not be an empty list.");
            }

            Object first = list.get(0);

            if (first instanceof String) {
                List<String> strings = (List<String>) list;
                return new TextListValue(strings);
            }

            if (first instanceof Map) {
                List<Map<String, String>> entries = (List<Map<String, String>>) list;
                return new SignatureBlockListValue(entries);
            }

            throw new IllegalArgumentException(
                    "Slot '" + slotId + "' in component '" + componentId
                            + "' contains a list with unsupported element type: "
                            + first.getClass().getSimpleName() + ".");
        }

        if (raw instanceof Map<?, ?> map) {
            Map<String, String> fields = (Map<String, String>) map;
            return new ComposedTextValue(fields);
        }

        throw new IllegalArgumentException(
                "Slot '" + slotId + "' in component '" + componentId
                        + "' has unsupported type: " + raw.getClass().getSimpleName() + ".");
    }
}
