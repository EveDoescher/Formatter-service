package com.abntbuilder.formatter.profile.model.component;

import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ComponentContentBindingsTest {

    @Test
    void shouldRejectUnknownWorkBindingSource() {
        InvalidProfileStructureException exception = assertThrows(
                InvalidProfileStructureException.class,
                () -> new ComponentContentBindings(Map.of("title", "work.titel"))
        );

        assertEquals("Unsupported content binding source: work.titel", exception.getMessage());
    }
}
