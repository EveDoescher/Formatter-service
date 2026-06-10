package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalSheetComponent;
import com.abntbuilder.formatter.profile.model.component.ComponentContentBindings;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

public record ApprovalSheetRequest(
        List<String> authors,
        String title,
        String subtitle,
        @Valid ApprovalSheetNatureRequest nature,
        @Valid ApprovalEventRequest approvalEvent,
        @Valid List<ApprovalCommitteeMemberRequest> committeeMembers
) {

    public ApprovalSheetComponent toDomain() {
        return toDomain(null, new ComponentContentBindings(java.util.Map.of()));
    }

    public ApprovalSheetComponent toDomain(AcademicWorkRequest work, ComponentContentBindings bindings) {
        WorkContentBindingResolver resolver = new WorkContentBindingResolver("approvalSheet", work, bindings);
        ApprovalSheetNatureRequest resolvedNature = resolver.resolveApprovalSheetNature("nature", nature);

        return new ApprovalSheetComponent(
                resolvedList(resolver.resolveStringList("authors", authors)),
                resolver.resolveString("title", title),
                Optional.ofNullable(resolver.resolveString("subtitle", subtitle)),
                requireNature(resolvedNature).toDomain(),
                approvalEvent == null || !approvalEvent.hasContent()
                        ? Optional.empty()
                        : Optional.of(approvalEvent.toDomain()),
                committeeMembers == null
                        ? List.of()
                        : committeeMembers.stream()
                        .map(ApprovalCommitteeMemberRequest::toDomain)
                        .toList()
        );
    }

    private static List<String> resolvedList(List<String> value) {
        return value == null ? List.of() : value;
    }

    private static ApprovalSheetNatureRequest requireNature(ApprovalSheetNatureRequest value) {
        if (value == null) {
            throw new IllegalArgumentException(
                    "approvalSheet.nature must be provided explicitly or through work bindings."
            );
        }

        return value;
    }
}
