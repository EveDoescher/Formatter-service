package com.abntbuilder.formatter.document.component.titlepage;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AcademicPersonTest {

    @Test
    void shouldCreateAcademicPersonWithTitle() {
        AcademicPerson person = new AcademicPerson("Jose da Silva", Optional.of("Prof. Dr."));

        assertEquals("Jose da Silva", person.name());
        assertEquals(Optional.of("Prof. Dr."), person.academicTitle());
    }

    @Test
    void shouldRejectBlankName() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new AcademicPerson(" ", Optional.empty())
        );

        assertEquals("name must not be blank.", exception.getMessage());
    }

    @Test
    void shouldRejectBlankAcademicTitleWhenPresent() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new AcademicPerson("Jose da Silva", Optional.of(" "))
        );

        assertEquals("academicTitle must not be blank.", exception.getMessage());
    }
}
