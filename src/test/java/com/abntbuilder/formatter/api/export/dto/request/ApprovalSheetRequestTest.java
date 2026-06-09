package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalSheetComponent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApprovalSheetRequestTest {

    @Test
    void shouldConvertSemanticApprovalSheetRequestToDomain() {
        ApprovalSheetRequest request = new ApprovalSheetRequest(
                List.of("Autor"),
                "Titulo",
                "Subtitulo",
                new ApprovalSheetNatureRequest(
                        "Trabalho academico",
                        "avaliacao parcial",
                        "Curso",
                        "Universidade"
                ),
                new ApprovalEventRequest("Limeira", "10 de junho de 2026", null),
                List.of(new ApprovalCommitteeMemberRequest(
                        "Jose da Silva",
                        "Prof. Dr.",
                        "Universidade Paulista",
                        "Orientador"
                ))
        );

        ApprovalSheetComponent approvalSheet = request.toDomain();

        assertEquals(List.of("Autor"), approvalSheet.authors());
        assertEquals("Titulo", approvalSheet.title());
        assertEquals("Subtitulo", approvalSheet.subtitle().orElseThrow());
        assertEquals("Trabalho academico", approvalSheet.nature().workType());
        assertEquals("Limeira", approvalSheet.approvalEvent().orElseThrow().location().orElseThrow());
        assertEquals("Jose da Silva", approvalSheet.committeeMembers().getFirst().name());
        assertEquals("Orientador", approvalSheet.committeeMembers().getFirst().role().orElseThrow());
    }

    @Test
    void shouldConvertMissingOptionalApprovalSheetFieldsToEmptyOptionals() {
        ApprovalSheetRequest request = new ApprovalSheetRequest(
                List.of("Autor"),
                "Titulo",
                null,
                new ApprovalSheetNatureRequest(
                        "Trabalho academico",
                        "avaliacao parcial",
                        "Curso",
                        "Universidade"
                ),
                null,
                null
        );

        ApprovalSheetComponent approvalSheet = request.toDomain();

        assertTrue(approvalSheet.subtitle().isEmpty());
        assertTrue(approvalSheet.approvalEvent().isEmpty());
        assertTrue(approvalSheet.committeeMembers().isEmpty());
    }

    @Test
    void shouldIgnoreEmptyApprovalEventRequest() {
        ApprovalSheetRequest request = new ApprovalSheetRequest(
                List.of("Autor"),
                "Titulo",
                null,
                new ApprovalSheetNatureRequest(
                        "Trabalho academico",
                        "avaliacao parcial",
                        "Curso",
                        "Universidade"
                ),
                new ApprovalEventRequest(null, null, null),
                null
        );

        ApprovalSheetComponent approvalSheet = request.toDomain();

        assertTrue(approvalSheet.approvalEvent().isEmpty());
    }
}
