package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.titlepage.AcademicPerson;
import jakarta.validation.constraints.NotBlank;

import java.util.Optional;

public record AcademicPersonRequest(
        String academicTitle,
        @NotBlank String name
) {

    public AcademicPerson toDomain() {
        return new AcademicPerson(
                name,
                academicTitle == null ? Optional.empty() : Optional.of(academicTitle)
        );
    }
}
