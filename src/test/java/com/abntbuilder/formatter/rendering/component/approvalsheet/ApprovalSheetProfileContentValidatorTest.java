package com.abntbuilder.formatter.rendering.component.approvalsheet;

import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalCommitteeMember;
import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalEvent;
import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalSheetComponent;
import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalSheetNature;
import com.abntbuilder.formatter.profile.model.component.approvalsheet.ApprovalSheetCommitteeMemberRule;
import com.abntbuilder.formatter.profile.model.component.approvalsheet.ApprovalSheetComponentRule;
import com.abntbuilder.formatter.profile.model.component.approvalsheet.ApprovalSheetSignatureLineRule;
import com.abntbuilder.formatter.profile.model.component.approvalsheet.ApprovalSheetStyleMapping;
import com.abntbuilder.formatter.profile.model.component.approvalsheet.ApprovalSheetTextTemplateRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.HorizontalPlacementRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.HorizontalPlacementStrategy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.LayoutGapRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageGroupRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageItemRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutPolicy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutRule;
import com.abntbuilder.formatter.shared.exception.InvalidApprovalSheetContentException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApprovalSheetProfileContentValidatorTest {

    private final ApprovalSheetProfileContentValidator validator = new ApprovalSheetProfileContentValidator();

    @Test
    void shouldAllowMissingApprovalEventWhenTemplateDoesNotUseApprovalEventFields() {
        assertDoesNotThrow(() -> validator.validate(
                component(Optional.empty(), committeeMember(Optional.of("Orientador"))),
                rule(
                        "Aprovado(a) em: ______/______/______",
                        List.of("{title} {name}", "{institutionName}", "{role}")
                )
        ));
    }

    @Test
    void shouldRequireApprovalEventFieldsUsedByTemplate() {
        InvalidApprovalSheetContentException exception = assertThrows(
                InvalidApprovalSheetContentException.class,
                () -> validator.validate(
                        component(
                                Optional.of(new ApprovalEvent(
                                        Optional.of("Limeira"),
                                        Optional.empty(),
                                        Optional.empty()
                                )),
                                committeeMember(Optional.of("Orientador"))
                        ),
                        rule(
                                "Aprovado(a) em {location}, {date}.",
                                List.of("{title} {name}", "{institutionName}", "{role}")
                        )
                )
        );

        assertEquals("approvalSheet required item has no content: approvalText.", exception.getMessage());
    }

    @Test
    void shouldRequireCommitteeMemberFieldUsedByTemplate() {
        InvalidApprovalSheetContentException exception = assertThrows(
                InvalidApprovalSheetContentException.class,
                () -> validator.validate(
                        component(Optional.empty(), committeeMember(Optional.empty())),
                        rule(
                                "Aprovado(a) em: ______/______/______",
                                List.of("{title} {name}", "{institutionName}", "{role}")
                        )
                )
        );

        assertEquals("approvalSheet required item has no content: committeeMembers.role.", exception.getMessage());
    }

    @Test
    void shouldAllowMissingCommitteeMemberRoleWhenTemplateDoesNotUseRole() {
        assertDoesNotThrow(() -> validator.validate(
                component(Optional.empty(), committeeMember(Optional.empty())),
                rule(
                        "Aprovado(a) em: ______/______/______",
                        List.of("{title} {name}", "{institutionName}")
                )
        ));
    }

    private static ApprovalSheetComponent component(
            Optional<ApprovalEvent> approvalEvent,
            ApprovalCommitteeMember committeeMember
    ) {
        return new ApprovalSheetComponent(
                List.of("Autor"),
                "Titulo",
                Optional.empty(),
                new ApprovalSheetNature(
                        "Trabalho academico",
                        "avaliacao parcial",
                        "Curso",
                        "Universidade"
                ),
                approvalEvent,
                List.of(committeeMember)
        );
    }

    private static ApprovalCommitteeMember committeeMember(Optional<String> role) {
        return new ApprovalCommitteeMember(
                "Jose da Silva",
                Optional.of("Prof. Dr."),
                Optional.of("Universidade"),
                role
        );
    }

    private static ApprovalSheetComponentRule rule(
            String approvalTextTemplate,
            List<String> committeeMemberLines
    ) {
        return new ApprovalSheetComponentRule(
                ApprovalSheetRenderer.COMPONENT_ID,
                new ApprovalSheetStyleMapping(
                        "approvalSheet.author",
                        "approvalSheet.title",
                        "approvalSheet.subtitle",
                        "approvalSheet.nature",
                        "approvalSheet.approval",
                        "approvalSheet.committee.heading",
                        "approvalSheet.committee"
                ),
                new ApprovalSheetTextTemplateRule(
                        "{workType} para {degreeObjective} em {courseName} apresentado a {institutionName}.",
                        approvalTextTemplate,
                        "BANCA EXAMINADORA",
                        new ApprovalSheetCommitteeMemberRule(
                                new ApprovalSheetSignatureLineRule(true, "________________________________________"),
                                committeeMemberLines
                        )
                ),
                new SinglePageLayoutRule(
                        List.of(
                                group("approvalSheet.authors", "authors"),
                                group("approvalSheet.titleBlock", "title"),
                                group("approvalSheet.natureBlock", "nature"),
                                group("approvalSheet.approvalBlock", "approvalText"),
                                group("approvalSheet.committeeHeading", "committeeHeading"),
                                group("approvalSheet.committeeBlock", "committeeMembers")
                        ),
                        List.of(
                                gap("approvalSheet.authors", "approvalSheet.titleBlock"),
                                gap("approvalSheet.titleBlock", "approvalSheet.natureBlock"),
                                gap("approvalSheet.natureBlock", "approvalSheet.approvalBlock"),
                                gap("approvalSheet.approvalBlock", "approvalSheet.committeeHeading"),
                                gap("approvalSheet.committeeHeading", "approvalSheet.committeeBlock")
                        ),
                        SinglePageLayoutPolicy.defaultSinglePagePolicy()
                )
        );
    }

    private static SinglePageGroupRule group(String groupId, String itemId) {
        return new SinglePageGroupRule(
                groupId,
                true,
                List.of(new SinglePageItemRule(
                        itemId,
                        true,
                        Optional.empty(),
                        new HorizontalPlacementRule(HorizontalPlacementStrategy.FULL_CONTENT_WIDTH),
                        0
                ))
        );
    }

    private static LayoutGapRule gap(String fromGroupId, String toGroupId) {
        return new LayoutGapRule(fromGroupId, toGroupId, BigDecimal.ONE);
    }
}
