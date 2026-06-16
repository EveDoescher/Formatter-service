package com.abntbuilder.formatter.document.component.bodycontent;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

class InlineFormattingTest {

    @Test
    void noneShouldHaveAllEmpty() {
        InlineFormatting f = InlineFormatting.none();
        assertThat(f.bold()).isEmpty();
        assertThat(f.italic()).isEmpty();
        assertThat(f.underline()).isEmpty();
    }

    @Test
    void shouldPreserveExplicitValues() {
        InlineFormatting f = new InlineFormatting(
                Optional.of(true), Optional.of(false), Optional.of(true)
        );
        assertThat(f.bold()).contains(true);
        assertThat(f.italic()).contains(false);
        assertThat(f.underline()).contains(true);
    }
}
