package com.abntbuilder.formatter.engine.model.content.bodycontent;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class BodyListItemTest {

    @Test
    void shouldCreateWithContent() {
        BodyListItem item = new BodyListItem(List.of(new BodyText("texto")));
        assertThat(item.content()).hasSize(1);
    }

    @Test
    void shouldRejectEmptyContent() {
        assertThatThrownBy(() -> new BodyListItem(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNullContent() {
        assertThatThrownBy(() -> new BodyListItem(null))
                .isInstanceOf(NullPointerException.class);
    }
}
