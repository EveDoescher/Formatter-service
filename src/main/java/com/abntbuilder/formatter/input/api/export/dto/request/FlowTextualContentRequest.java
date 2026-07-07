package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.content.DocumentComponent;
import com.abntbuilder.formatter.engine.model.content.flowtextual.FlowTextualContent;
import com.abntbuilder.formatter.engine.model.content.singlepage.ContentValue;
import com.abntbuilder.formatter.engine.model.content.singlepage.EntryListValue;
import com.abntbuilder.formatter.engine.model.content.singlepage.TableValue;
import com.abntbuilder.formatter.engine.model.content.singlepage.TextListValue;
import com.abntbuilder.formatter.engine.model.content.singlepage.TextValue;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.CitationFormattingRule;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FlowTextualContentRequest implements ComponentContentRequest {

    private final Map<String, Object> rawSlots = new HashMap<>();

    @JsonAnySetter
    public void setSlot(String key, Object value) {
        rawSlots.put(key, value);
    }

    @Override
    public DocumentComponent toDomain(String componentId, CitationFormattingRule citationFormatting) {
        return toDomain(componentId);
    }

    public FlowTextualContent toDomain(String componentId) {
        Map<String, ContentValue> slots = new HashMap<>();

        for (Map.Entry<String, Object> entry : rawSlots.entrySet()) {
            String slotId = entry.getKey();
            Object raw = entry.getValue();

            if (raw == null) {
                continue;
            }

            slots.put(slotId, toContentValue(componentId, slotId, raw));
        }

        return new FlowTextualContent(componentId, slots);
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
                return new TextListValue((List<String>) list);
            }

            if (first instanceof List) {
                List<List<String>> rows = (List<List<String>>) list;
                return new TableValue(rows);
            }

            if (first instanceof Map) {
                List<Map<String, ContentValue>> entries = ((List<Map<String, Object>>) list).stream()
                        .map(entryMap -> {
                            Map<String, ContentValue> converted = new java.util.HashMap<>();
                            for (Map.Entry<String, Object> e : entryMap.entrySet()) {
                                if (e.getValue() != null) {
                                    converted.put(e.getKey(),
                                            toContentValue(componentId, e.getKey(), e.getValue()));
                                }
                            }
                            return converted;
                        })
                        .toList();
                return new EntryListValue(entries);
            }

            throw new IllegalArgumentException(
                    "Slot '" + slotId + "' in component '" + componentId
                            + "' contains a list with unsupported element type: "
                            + first.getClass().getSimpleName() + ".");
        }

        throw new IllegalArgumentException(
                "Slot '" + slotId + "' in component '" + componentId
                        + "' has unsupported type: " + raw.getClass().getSimpleName() + ".");
    }
}
