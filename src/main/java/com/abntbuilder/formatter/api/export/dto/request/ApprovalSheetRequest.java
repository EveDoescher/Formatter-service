package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalSheetComponent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Optional;

public record ApprovalSheetRequest(
        @NotEmpty List<@NotBlank String> authors,
        @NotBlank String title,
        String subtitle,
        @Valid @NotNull ApprovalSheetNatureRequest nature,
        @Valid ApprovalEventRequest approvalEvent,
        @Valid List<ApprovalCommitteeMemberRequest> committeeMembers
) {

    public ApprovalSheetComponent toDomain() {
        return new ApprovalSheetComponent(
                authors == null ? List.of() : authors,
                title,
                subtitle == null ? Optional.empty() : Optional.of(subtitle),
                nature.toDomain(),
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
}
