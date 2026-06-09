package com.abntbuilder.formatter.rendering.component.approvalsheet;

import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalCommitteeMember;
import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalEvent;
import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalSheetNature;
import com.abntbuilder.formatter.profile.model.component.approvalsheet.ApprovalSheetCommitteeMemberRule;
import com.abntbuilder.formatter.profile.model.component.approvalsheet.ApprovalSheetSignatureLineRule;
import com.abntbuilder.formatter.profile.model.component.approvalsheet.ApprovalSheetTextTemplateRule;
import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApprovalSheetTextTemplateResolverTest {

    private final ApprovalSheetTextTemplateResolver resolver = new ApprovalSheetTextTemplateResolver();

    @Test
    void shouldResolveApprovalSheetTemplatesFromProfile() {
        assertEquals(
                "Trabalho academico para avaliacao parcial em Curso apresentado a Universidade.",
                resolver.resolveNature(templates(), nature())
        );
        assertEquals(
                "Aprovado em Limeira, 10 de junho de 2026.",
                resolver.resolveApprovalText(
                        templates(),
                        new ApprovalEvent(
                                Optional.of("Limeira"),
                                Optional.of("10 de junho de 2026"),
                                Optional.empty()
                        )
                )
        );
        assertEquals(
                "BANCA EXAMINADORA",
                resolver.resolveCommitteeHeading(templates())
        );
        assertEquals(
                List.of(
                        "________________________________________",
                        "Prof. Dr. Jose da Silva",
                        "Universidade",
                        "Orientador"
                ),
                resolver.resolveCommitteeMemberLines(
                        templates(),
                        new ApprovalCommitteeMember(
                                "Jose da Silva",
                                Optional.of("Prof. Dr."),
                                Optional.of("Universidade"),
                                Optional.of("Orientador")
                        )
                )
        );
    }

    @Test
    void shouldRejectUnknownPlaceholder() {
        ApprovalSheetTextTemplateRule templates = new ApprovalSheetTextTemplateRule(
                "{workType} {unknownPlaceholder}",
                "Aprovado em {location}, {date}.",
                "BANCA EXAMINADORA",
                new ApprovalSheetCommitteeMemberRule(
                        signatureLine(),
                        List.of("{title} {name}")
                )
        );

        InvalidProfileStructureException exception = assertThrows(
                InvalidProfileStructureException.class,
                () -> resolver.resolveNature(templates, nature())
        );

        assertEquals("Unknown approvalSheet template placeholder: unknownPlaceholder", exception.getMessage());
    }

    private static ApprovalSheetTextTemplateRule templates() {
        return new ApprovalSheetTextTemplateRule(
                "{workType} para {degreeObjective} em {courseName} apresentado a {institutionName}.",
                "Aprovado em {location}, {date}.",
                "BANCA EXAMINADORA",
                new ApprovalSheetCommitteeMemberRule(
                        signatureLine(),
                        List.of(
                                "{title} {name}",
                                "{institutionName}",
                                "{role}"
                        )
                )
        );
    }

    private static ApprovalSheetSignatureLineRule signatureLine() {
        return new ApprovalSheetSignatureLineRule(true, "________________________________________");
    }

    private static ApprovalSheetNature nature() {
        return new ApprovalSheetNature(
                "Trabalho academico",
                "avaliacao parcial",
                "Curso",
                "Universidade"
        );
    }
}
