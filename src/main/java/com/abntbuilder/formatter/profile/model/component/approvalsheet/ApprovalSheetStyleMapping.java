package com.abntbuilder.formatter.profile.model.component.approvalsheet;

import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

public record ApprovalSheetStyleMapping(
        String authorsStyleId,
        String titleStyleId,
        String subtitleStyleId,
        String natureStyleId,
        String approvalTextStyleId,
        String committeeHeadingStyleId,
        String committeeMembersStyleId
) {

    public ApprovalSheetStyleMapping {
        requireNonBlank(authorsStyleId, "authorsStyleId");
        requireNonBlank(titleStyleId, "titleStyleId");
        requireNonBlank(subtitleStyleId, "subtitleStyleId");
        requireNonBlank(natureStyleId, "natureStyleId");
        requireNonBlank(approvalTextStyleId, "approvalTextStyleId");
        requireNonBlank(committeeHeadingStyleId, "committeeHeadingStyleId");
        requireNonBlank(committeeMembersStyleId, "committeeMembersStyleId");
    }

    public String styleIdForItem(String itemId) {
        return switch (itemId) {
            case "authors" -> authorsStyleId;
            case "title" -> titleStyleId;
            case "subtitle" -> subtitleStyleId;
            case "nature" -> natureStyleId;
            case "approvalText" -> approvalTextStyleId;
            case "committeeHeading" -> committeeHeadingStyleId;
            case "committeeMembers" -> committeeMembersStyleId;
            default -> throw new InvalidProfileStructureException(
                    "Unknown approvalSheet style mapping item id: " + itemId
            );
        };
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidProfileStructureException(fieldName + " must not be blank.");
        }
    }
}
