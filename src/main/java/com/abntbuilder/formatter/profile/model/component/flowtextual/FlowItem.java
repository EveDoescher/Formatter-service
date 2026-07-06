package com.abntbuilder.formatter.profile.model.component.flowtextual;

import java.util.List;
import java.util.Objects;

/**
 * Declarative rendering instruction for one visual unit within a FlowTextualComponent.
 * The renderer reads the item type and executes the corresponding output.
 */
public sealed interface FlowItem
        permits FlowItem.HeadingItem,
                FlowItem.BlankLinesItem,
                FlowItem.PlainTextItem,
                FlowItem.TemplatedTextItem,
                FlowItem.BoldLabeledKeywordsItem,
                FlowItem.PairListItem,
                FlowItem.TableBlockItem,
                FlowItem.RepeatGroupItem {

    /** Fixed heading paragraph with text from the profile. */
    record HeadingItem(String styleId, String text) implements FlowItem {
        public HeadingItem {
            requireNonBlank(styleId, "HeadingItem.styleId");
            requireNonBlank(text, "HeadingItem.text");
        }
    }

    /** A fixed number of blank lines (semantic spacing). */
    record BlankLinesItem(String styleId, int count) implements FlowItem {
        public BlankLinesItem {
            requireNonBlank(styleId, "BlankLinesItem.styleId");
            if (count <= 0) throw new IllegalArgumentException("BlankLinesItem.count must be > 0.");
        }
    }

    /** A paragraph whose text comes from a TextValue slot. */
    record PlainTextItem(String styleId, String slotName) implements FlowItem {
        public PlainTextItem {
            requireNonBlank(styleId, "PlainTextItem.styleId");
            requireNonBlank(slotName, "PlainTextItem.slotName");
        }
    }

    /**
     * A paragraph built from a template with named placeholders ({field}).
     * Each fieldName maps to a TextValue slot. Unknown placeholders are left as-is.
     */
    record TemplatedTextItem(String styleId, String template, List<String> fieldNames) implements FlowItem {
        public TemplatedTextItem {
            requireNonBlank(styleId, "TemplatedTextItem.styleId");
            requireNonBlank(template, "TemplatedTextItem.template");
            Objects.requireNonNull(fieldNames, "TemplatedTextItem.fieldNames must not be null.");
            fieldNames = List.copyOf(fieldNames);
        }
    }

    /**
     * A paragraph with a bold label prefix followed by a list of keywords.
     * The label comes from a TextValue slot; the keyword list from a TextListValue slot.
     */
    record BoldLabeledKeywordsItem(
            String styleId,
            String labelSlotName,
            String keywordsSlotName,
            String separator,
            String terminator
    ) implements FlowItem {
        public BoldLabeledKeywordsItem {
            requireNonBlank(styleId, "BoldLabeledKeywordsItem.styleId");
            requireNonBlank(labelSlotName, "BoldLabeledKeywordsItem.labelSlotName");
            requireNonBlank(keywordsSlotName, "BoldLabeledKeywordsItem.keywordsSlotName");
            Objects.requireNonNull(separator, "BoldLabeledKeywordsItem.separator must not be null.");
            Objects.requireNonNull(terminator, "BoldLabeledKeywordsItem.terminator must not be null.");
        }
    }

    /**
     * A list of term → definition entries rendered as "term + separator + definition".
     * Entries come from a TextListValue slot where each item is "termseparatordefinition"
     * encoded, OR from two parallel TextListValue slots (termsSlotName + definitionsSlotName).
     */
    record PairListItem(
            String styleId,
            String termsSlotName,
            String definitionsSlotName,
            String separator
    ) implements FlowItem {
        public PairListItem {
            requireNonBlank(styleId, "PairListItem.styleId");
            requireNonBlank(termsSlotName, "PairListItem.termsSlotName");
            requireNonBlank(definitionsSlotName, "PairListItem.definitionsSlotName");
            Objects.requireNonNull(separator, "PairListItem.separator must not be null.");
        }
    }

    /**
     * A table block. Column headers come from the profile.
     * Row data comes from a TableValue slot.
     */
    record TableBlockItem(
            String headerStyleId,
            String cellStyleId,
            List<String> headers,
            String rowsSlotName
    ) implements FlowItem {
        public TableBlockItem {
            requireNonBlank(headerStyleId, "TableBlockItem.headerStyleId");
            requireNonBlank(cellStyleId, "TableBlockItem.cellStyleId");
            Objects.requireNonNull(headers, "TableBlockItem.headers must not be null.");
            if (headers.isEmpty()) throw new IllegalArgumentException("TableBlockItem.headers must not be empty.");
            headers = List.copyOf(headers);
            requireNonBlank(rowsSlotName, "TableBlockItem.rowsSlotName");
        }
    }

    /**
     * Repeats a group of FlowItems once per entry in an EntryListValue slot.
     * Each iteration's slots override the component-level slots for the duration of that group.
     */
    record RepeatGroupItem(
            String entriesSlotName,
            boolean pageBreakBetweenEntries,
            List<FlowItem> group
    ) implements FlowItem {
        public RepeatGroupItem {
            requireNonBlank(entriesSlotName, "RepeatGroupItem.entriesSlotName");
            Objects.requireNonNull(group, "RepeatGroupItem.group must not be null.");
            if (group.isEmpty()) throw new IllegalArgumentException("RepeatGroupItem.group must not be empty.");
            group = List.copyOf(group);
        }
    }

    private static void requireNonBlank(String v, String field) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(field + " must not be blank.");
    }
}
