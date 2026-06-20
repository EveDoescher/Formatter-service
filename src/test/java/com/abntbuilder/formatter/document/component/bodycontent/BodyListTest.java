package com.abntbuilder.formatter.document.component.bodycontent;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class BodyListTest {

    private static BodyListItem item(String text) {
        return new BodyListItem(List.of(new BodyText(text)));
    }

    @Test
    void shouldCreateOrderedList() {
        BodyList list = new BodyList(BodyListType.ORDERED, List.of(item("a"), item("b")));
        assertThat(list.type()).isEqualTo(BodyListType.ORDERED);
        assertThat(list.items()).hasSize(2);
    }

    @Test
    void shouldRejectNullType() {
        assertThatThrownBy(() -> new BodyList(null, List.of(item("a"))))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectEmptyItems() {
        assertThatThrownBy(() -> new BodyList(BodyListType.ORDERED, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
