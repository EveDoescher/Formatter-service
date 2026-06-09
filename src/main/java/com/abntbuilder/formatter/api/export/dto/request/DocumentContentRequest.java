package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.DocumentComponent;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;

public record DocumentContentRequest(
        @Valid CoverRequest cover,
        @Valid TitlePageRequest titlePage,
        @Valid ApprovalSheetRequest approvalSheet
) {
    public List<DocumentComponent> toComponents() {
        List<DocumentComponent> components = new ArrayList<>();

        if (cover != null) {
            components.add(cover.toDomain());
        }

        if (titlePage != null) {
            components.add(titlePage.toDomain());
        }

        if (approvalSheet != null) {
            components.add(approvalSheet.toDomain());
        }

        return List.copyOf(components);
    }
}
