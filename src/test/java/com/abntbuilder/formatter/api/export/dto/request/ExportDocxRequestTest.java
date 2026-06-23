package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.application.export.ExportDocxCommand;
import com.abntbuilder.formatter.document.component.cover.CoverComponent;
import com.abntbuilder.formatter.document.component.titlepage.TitlePageComponent;
import com.abntbuilder.formatter.config.ClasspathJsonProfileProvider;
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
                null,
                new DocumentContentRequest(null, titlePageRequest(), null, null,
                        null, null, null, null, null, null, null, null, null, null),
                List.of()
        );

        ExportDocxCommand command = request.toCommand(new ClasspathJsonProfileProvider());

        assertEquals(1, command.documentComponents().size());
        TitlePageComponent titlePage = (TitlePageComponent) command.documentComponents().getFirst();
        assertEquals(List.of("titlePage"), command.selectedComponents());
        assertEquals("Titulo", titlePage.title());
    }

    @Test
    void shouldResolveDocumentComponentsFromWorkBindings() {
        ExportDocxRequest request = new ExportDocxRequest(
                "work-bound.docx",
                "abnt-unip-profile",
                null,
                new ExportOptionsRequest(List.of("cover", "titlePage")),
                workRequest(),
                new DocumentContentRequest(
                        new CoverRequest(null, null, null, null, null, null),
                        new TitlePageRequest(null, null, null, null, null, null, null, null),
                        null,
                        null,
                        null, null, null, null, null, null, null, null, null, null
                ),
                List.of()
        );

        ExportDocxCommand command = request.toCommand(new ClasspathJsonProfileProvider());

        CoverComponent cover = (CoverComponent) command.documentComponents().get(0);
        TitlePageComponent titlePage = (TitlePageComponent) command.documentComponents().get(1);

        assertEquals(List.of("UNIVERSIDADE PAULISTA"), cover.institutionalLines());
        assertEquals(List.of("Autora Teste"), cover.authors());
        assertEquals("Titulo comum", cover.title());
        assertEquals("Titulo comum", titlePage.title());
        assertEquals("Trabalho academico", titlePage.nature().workType());
        assertEquals("Professora Teste", titlePage.advisor().orElseThrow().name());
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

    private static AcademicWorkRequest workRequest() {
        return new AcademicWorkRequest(
                List.of("UNIVERSIDADE PAULISTA"),
                List.of("Autora Teste"),
                "Titulo comum",
                "Subtitulo comum",
                new AcademicWorkNatureRequest(
                        "Trabalho academico",
                        "avaliacao parcial",
                        "Curso",
                        "Universidade"
                ),
                new AcademicPersonRequest("Profa. Dra.", "Professora Teste"),
                null,
                "Limeira",
                "2026"
        );
    }
}
