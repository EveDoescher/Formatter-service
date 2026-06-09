package com.abntbuilder.formatter.rendering.component.approvalsheet;

import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalCommitteeMember;
import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalEvent;
import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalSheetComponent;
import com.abntbuilder.formatter.profile.model.component.approvalsheet.ApprovalSheetComponentRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageGroupRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageItemRule;
import com.abntbuilder.formatter.shared.exception.InvalidApprovalSheetContentException;
import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class ApprovalSheetProfileContentValidator {

    private final ApprovalSheetTextTemplateResolver templateResolver;

    public ApprovalSheetProfileContentValidator() {
        this(new ApprovalSheetTextTemplateResolver());
    }

    public ApprovalSheetProfileContentValidator(ApprovalSheetTextTemplateResolver templateResolver) {
        this.templateResolver = Objects.requireNonNull(templateResolver, "templateResolver must not be null");
    }

    public void validate(ApprovalSheetComponent component, ApprovalSheetComponentRule rule) {
        Objects.requireNonNull(component, "component must not be null");
        Objects.requireNonNull(rule, "rule must not be null");

        if (!ApprovalSheetRenderer.COMPONENT_ID.equals(rule.componentId())) {
            throw new InvalidProfileStructureException("approvalSheet componentId must be approvalSheet.");
        }

        Set<String> groupIds = new HashSet<>();

        for (SinglePageGroupRule groupRule : rule.layoutRule().groups()) {
            if (!groupIds.add(groupRule.id())) {
                throw new InvalidProfileStructureException("Duplicate approvalSheet group id: " + groupRule.id());
            }

            boolean groupHasContent = false;
            Set<String> itemIds = new HashSet<>();

            for (SinglePageItemRule itemRule : groupRule.items()) {
                if (!itemIds.add(itemRule.id())) {
                    throw new InvalidProfileStructureException(
                            "Duplicate approvalSheet item id in group " + groupRule.id() + ": " + itemRule.id()
                    );
                }

                boolean itemHasContent = hasContent(component, rule, itemRule.id());

                if (itemRule.required() && !itemHasContent) {
                    throw InvalidApprovalSheetContentException.missingRequiredItem(itemRule.id());
                }

                groupHasContent = groupHasContent || itemHasContent;
                rule.styleMapping().styleIdForItem(itemRule.id());
            }

            if (groupRule.required() && !groupHasContent) {
                throw InvalidApprovalSheetContentException.missingRequiredGroup(groupRule.id());
            }
        }
    }

    private boolean hasContent(ApprovalSheetComponent component, ApprovalSheetComponentRule rule, String itemId) {
        return switch (itemId) {
            case "authors" -> !component.authors().isEmpty();
            case "title" -> !component.title().isBlank();
            case "subtitle" -> component.subtitle().isPresent();
            case "nature" -> true;
            case "approvalText" -> hasApprovalTextContent(component, rule);
            case "committeeHeading" -> !component.committeeMembers().isEmpty();
            case "committeeMembers" -> hasCommitteeMembersContent(component, rule);
            default -> throw new InvalidProfileStructureException("Unknown approvalSheet item id: " + itemId);
        };
    }

    private boolean hasApprovalTextContent(ApprovalSheetComponent component, ApprovalSheetComponentRule rule) {
        Set<String> requiredFields = templateResolver.approvalTextRequiredFields(rule.textTemplates());

        if (requiredFields.isEmpty()) {
            return true;
        }

        return component.approvalEvent()
                .filter(approvalEvent -> hasRequiredApprovalEventFields(approvalEvent, requiredFields))
                .isPresent();
    }

    private static boolean hasRequiredApprovalEventFields(ApprovalEvent approvalEvent, Set<String> requiredFields) {
        for (String requiredField : requiredFields) {
            boolean present = switch (requiredField) {
                case "location" -> approvalEvent.location().isPresent();
                case "date" -> approvalEvent.date().isPresent();
                case "approvalTextData" -> approvalEvent.approvalTextData().isPresent();
                default -> throw new InvalidProfileStructureException(
                        "Unknown approvalSheet approval event field: " + requiredField
                );
            };

            if (!present) {
                return false;
            }
        }

        return true;
    }

    private boolean hasCommitteeMembersContent(ApprovalSheetComponent component, ApprovalSheetComponentRule rule) {
        if (component.committeeMembers().isEmpty()) {
            return false;
        }

        Set<String> requiredFields = templateResolver.committeeMemberRequiredFields(rule.textTemplates());

        for (ApprovalCommitteeMember member : component.committeeMembers()) {
            validateRequiredCommitteeMemberFields(member, requiredFields);
        }

        return true;
    }

    private static void validateRequiredCommitteeMemberFields(
            ApprovalCommitteeMember member,
            Set<String> requiredFields
    ) {
        for (String requiredField : requiredFields) {
            boolean present = switch (requiredField) {
                case "name" -> !member.name().isBlank();
                case "title" -> member.title().isPresent();
                case "institutionName" -> member.institutionName().isPresent();
                case "role" -> member.role().isPresent();
                default -> throw new InvalidProfileStructureException(
                        "Unknown approvalSheet committee member field: " + requiredField
                );
            };

            if (!present) {
                throw InvalidApprovalSheetContentException.missingRequiredItem(
                        "committeeMembers." + requiredField
                );
            }
        }
    }
}
