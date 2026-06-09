package com.abntbuilder.formatter.document.component.approvalsheet;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record ApprovalSheetComponent(
        List<String> authors,
        String title,
        Optional<String> subtitle,
        ApprovalSheetNature nature,
        Optional<ApprovalEvent> approvalEvent,
        List<ApprovalCommitteeMember> committeeMembers
) implements DocumentComponent {

    public ApprovalSheetComponent {
        authors = validateAuthors(authors);
        requireNonBlank(title, "title");
        Objects.requireNonNull(subtitle, "subtitle must not be null");
        subtitle.ifPresent(value -> requireNonBlank(value, "subtitle"));
        Objects.requireNonNull(nature, "nature must not be null");
        Objects.requireNonNull(approvalEvent, "approvalEvent must not be null");
        committeeMembers = validateCommitteeMembers(committeeMembers);
    }

    @Override
    public ComponentType type() {
        return ComponentType.APPROVAL_SHEET;
    }

    private static List<String> validateAuthors(List<String> authors) {
        Objects.requireNonNull(authors, "authors must not be null");

        if (authors.isEmpty()) {
            throw new IllegalArgumentException("authors must not be empty.");
        }

        for (String author : authors) {
            requireNonBlank(author, "authors item");
        }

        return List.copyOf(authors);
    }

    private static List<ApprovalCommitteeMember> validateCommitteeMembers(
            List<ApprovalCommitteeMember> committeeMembers
    ) {
        Objects.requireNonNull(committeeMembers, "committeeMembers must not be null");

        for (ApprovalCommitteeMember member : committeeMembers) {
            Objects.requireNonNull(member, "committeeMembers must not contain null values.");
        }

        return List.copyOf(committeeMembers);
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
