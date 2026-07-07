package com.abntbuilder.formatter.engine.model.content.bodycontent;

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
        assertThat(f.superscript()).isEmpty();
        assertThat(f.subscript()).isEmpty();
    }

    @Test
    void shouldPreserveExplicitValues() {
        InlineFormatting f = new InlineFormatting(
                Optional.of(true), Optional.of(false), Optional.of(true),
                Optional.of(true), Optional.of(false)
        );
        assertThat(f.bold()).contains(true);
        assertThat(f.italic()).contains(false);
        assertThat(f.underline()).contains(true);
        assertThat(f.superscript()).contains(true);
        assertThat(f.subscript()).contains(false);
    }

    @Test
    void shouldRejectSuperscriptAndSubscriptTogether() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> new InlineFormatting(
                Optional.of(false), Optional.of(false), Optional.of(false),
                Optional.of(true), Optional.of(true)
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("superscript and subscript cannot both be true.");
    }
}
