package com.abntbuilder.formatter.api.export.dto.request;

import jakarta.validation.Valid;

public record DocumentContentRequest(
        @Valid CoverRequest cover,
        @Valid TitlePageRequest titlePage,
        @Valid ApprovalSheetRequest approvalSheet
) {
}
