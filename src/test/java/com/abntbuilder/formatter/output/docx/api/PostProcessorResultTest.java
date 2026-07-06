package com.abntbuilder.formatter.output.docx.api;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class PostProcessorResultTest {

    @Test
    void shouldCreateResultWithNoWarnings() {
        byte[] bytes = new byte[]{1, 2, 3};
        PostProcessorResult result = PostProcessorResult.of(bytes);

        assertThat(result.docxBytes()).isEqualTo(bytes);
        assertThat(result.warnings()).isEmpty();
        assertThat(result.hasWarnings()).isFalse();
    }

    @Test
    void shouldCreateResultWithWarnings() {
        byte[] bytes = new byte[]{1, 2, 3};
        PostProcessorResult result = PostProcessorResult.of(bytes, List.of("warn1", "warn2"));

        assertThat(result.warnings()).containsExactly("warn1", "warn2");
        assertThat(result.hasWarnings()).isTrue();
    }

    @Test
    void shouldReturnImmutableWarningsList() {
        PostProcessorResult result = PostProcessorResult.of(new byte[0], List.of("w"));

        assertThat(result.warnings()).isUnmodifiable();
    }

    @Test
    void shouldRejectNullBytes() {
        assertThatNullPointerException().isThrownBy(() -> PostProcessorResult.of(null));
    }
}
