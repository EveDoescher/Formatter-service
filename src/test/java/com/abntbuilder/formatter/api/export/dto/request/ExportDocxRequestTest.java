package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.application.export.ExportDocxCommand;
import com.abntbuilder.formatter.document.component.titlepage.TitlePageComponent;
import com.abntbuilder.formatter.profile.resolution.ClasspathJsonProfileProvider;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExportDocxRequestTest {

    @Test
    void shouldConvertDocumentTitlePageToCommand() {
        ExportDocxRequest request = new ExportDocxRequest(
                "title-page.docx",
                "abnt-unip-profile",
                null,
                new ExportOptionsRequest(List.of("titlePage")),
                new DocumentContentRequest(null, titlePageRequest(), null),
                List.of()
        );

        ExportDocxCommand command = request.toCommand(new ClasspathJsonProfileProvider());

        assertEquals(1, command.documentComponents().size());
        TitlePageComponent titlePage = (TitlePageComponent) command.documentComponents().getFirst();
        assertEquals(List.of("titlePage"), command.selectedComponents());
        assertEquals("Titulo", titlePage.title());
    }

    private static TitlePageRequest titlePageRequest() {
        return new TitlePageRequest(
                List.of("Autor"),
                "Titulo",
                null,
                new TitlePageNatureRequest(
                        "Trabalho academico",
                        "avaliacao parcial",
                        "Curso",
                        "Universidade"
                ),
                null,
                null,
                "Limeira",
                "2026"
        );
    }
}
