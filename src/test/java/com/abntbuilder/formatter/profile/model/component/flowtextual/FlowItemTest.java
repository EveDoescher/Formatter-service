package com.abntbuilder.formatter.profile.model.component.flowtextual;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlowItemTest {

    @Test
    void headingItemRejectsBlankFields() {
        assertThrows(IllegalArgumentException.class, () -> new FlowItem.HeadingItem("", "text"));
        assertThrows(IllegalArgumentException.class, () -> new FlowItem.HeadingItem("style", ""));
    }

    @Test
    void blankLinesItemRejectsZeroCount() {
        assertThrows(IllegalArgumentException.class, () -> new FlowItem.BlankLinesItem("style", 0));
        assertThrows(IllegalArgumentException.class, () -> new FlowItem.BlankLinesItem("style", -1));
    }

    @Test
    void plainTextItemRejectsBlankSlotName() {
        assertThrows(IllegalArgumentException.class, () -> new FlowItem.PlainTextItem("style", ""));
    }

    @Test
    void templatedTextItemPreservesFieldNames() {
        FlowItem.TemplatedTextItem item = new FlowItem.TemplatedTextItem(
                "style", "{author}, {source}", List.of("author", "source"));

        assertEquals(List.of("author", "source"), item.fieldNames());
    }

    @Test
    void templatedTextItemReturnsImmutableFieldNames() {
        FlowItem.TemplatedTextItem item = new FlowItem.TemplatedTextItem(
                "style", "{x}", List.of("x"));

        assertThrows(UnsupportedOperationException.class, () -> item.fieldNames().add("y"));
    }

    @Test
    void pairListItemAllowsPhase0SourcePrefix() {
        FlowItem.PairListItem item = new FlowItem.PairListItem(
                "style", "$abbreviations", "$sort", " — ");

        assertEquals("$abbreviations", item.termsSlotName());
        assertEquals("$sort", item.definitionsSlotName());
    }

    @Test
    void tableBlockItemRejectsEmptyHeaders() {
        assertThrows(IllegalArgumentException.class, () ->
                new FlowItem.TableBlockItem("hStyle", "cStyle", List.of(), "rows"));
    }

    @Test
    void tableBlockItemPreservesHeaders() {
        FlowItem.TableBlockItem item = new FlowItem.TableBlockItem(
                "hStyle", "cStyle", List.of("Folha", "Linha", "Onde", "Leia-se"), "rows");

        assertEquals(4, item.headers().size());
        assertThrows(UnsupportedOperationException.class, () -> item.headers().add("extra"));
    }

    @Test
    void repeatGroupItemRejectsEmptyGroup() {
        assertThrows(IllegalArgumentException.class,
                () -> new FlowItem.RepeatGroupItem("entries", true, List.of()));
    }

    @Test
    void repeatGroupItemRejectsNullGroup() {
        assertThrows(NullPointerException.class,
                () -> new FlowItem.RepeatGroupItem("entries", true, null));
    }

    @Test
    void repeatGroupItemRejectsBlankSlotName() {
        assertThrows(IllegalArgumentException.class,
                () -> new FlowItem.RepeatGroupItem("", true,
                        List.of(new FlowItem.PlainTextItem("s", "text"))));
    }

    @Test
    void repeatGroupItemGroupIsImmutable() {
        FlowItem.RepeatGroupItem item = new FlowItem.RepeatGroupItem(
                "entries", true,
                List.of(new FlowItem.PlainTextItem("s", "text")));

        assertEquals(1, item.group().size());
        assertThrows(UnsupportedOperationException.class,
                () -> item.group().add(new FlowItem.PlainTextItem("s", "x")));
    }
}
