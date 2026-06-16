package com.abntbuilder.formatter.shared.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class InvalidBodyContentExceptionTest {

    @Test
    void shouldExtendIllegalArgumentException() {
        InvalidBodyContentException ex = new InvalidBodyContentException("test message");
        assertThat(ex).isInstanceOf(IllegalArgumentException.class);
        assertThat(ex.getMessage()).isEqualTo("test message");
    }
}
