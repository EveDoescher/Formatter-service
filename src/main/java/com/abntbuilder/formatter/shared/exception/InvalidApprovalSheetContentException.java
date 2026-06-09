package com.abntbuilder.formatter.shared.exception;

public class InvalidApprovalSheetContentException extends IllegalArgumentException {

    public InvalidApprovalSheetContentException(String message) {
        super(message);
    }

    public static InvalidApprovalSheetContentException missingRequiredGroup(String groupId) {
        return new InvalidApprovalSheetContentException(
                "approvalSheet required group has no content: " + groupId + "."
        );
    }

    public static InvalidApprovalSheetContentException missingRequiredItem(String itemId) {
        return new InvalidApprovalSheetContentException(
                "approvalSheet required item has no content: " + itemId + "."
        );
    }

    public static InvalidApprovalSheetContentException itemExceedsMaxVisualLines(
            String itemId,
            int maxVisualLines
    ) {
        return new InvalidApprovalSheetContentException(
                "approvalSheet item exceeds max visual lines: " + itemId + " must fit in "
                        + maxVisualLines
                        + " visual line(s)."
        );
    }
}
