package com.abntbuilder.formatter.rendering.component.approvalsheet;

import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalCommitteeMember;
import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalEvent;
import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalSheetNature;
import com.abntbuilder.formatter.profile.model.component.approvalsheet.ApprovalSheetTextTemplateRule;
import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ApprovalSheetTextTemplateResolver {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([A-Za-z][A-Za-z0-9]*)}");

    public String resolveNature(
            ApprovalSheetTextTemplateRule templates,
            ApprovalSheetNature nature
    ) {
        Objects.requireNonNull(templates, "templates must not be null");
        Objects.requireNonNull(nature, "nature must not be null");

        return resolve(
                templates.natureTemplate(),
                Map.of(
                        "workType", nature.workType(),
                        "degreeObjective", nature.degreeObjective(),
                        "courseName", nature.courseName(),
                        "institutionName", nature.institutionName()
                )
        );
    }

    public String resolveApprovalText(
            ApprovalSheetTextTemplateRule templates,
            ApprovalEvent event
    ) {
        Objects.requireNonNull(templates, "templates must not be null");
        Objects.requireNonNull(event, "event must not be null");

        return resolve(
                templates.approvalTextTemplate(),
                Map.of(
                        "location", event.location().orElse(""),
                        "date", event.date().orElse(""),
                        "approvalTextData", event.approvalTextData().orElse("")
                )
        );
    }

    public Set<String> approvalTextRequiredFields(ApprovalSheetTextTemplateRule templates) {
        Objects.requireNonNull(templates, "templates must not be null");

        return placeholdersIn(templates.approvalTextTemplate(), Set.of(
                "location",
                "date",
                "approvalTextData"
        ));
    }

    public Set<String> committeeMemberRequiredFields(ApprovalSheetTextTemplateRule templates) {
        Objects.requireNonNull(templates, "templates must not be null");

        Set<String> requiredFields = new HashSet<>();
        Set<String> allowedFields = Set.of("name", "title", "institutionName", "role");

        if (templates.committeeMemberTemplate().signatureLine().enabled()) {
            requiredFields.addAll(placeholdersIn(
                    templates.committeeMemberTemplate().signatureLine().text(),
                    allowedFields
            ));
        }

        for (String line : templates.committeeMemberTemplate().lineTemplates()) {
            requiredFields.addAll(placeholdersIn(line, allowedFields));
        }

        return Set.copyOf(requiredFields);
    }

    public String resolveCommitteeHeading(ApprovalSheetTextTemplateRule templates) {
        Objects.requireNonNull(templates, "templates must not be null");

        return resolve(templates.committeeHeadingTemplate(), Map.of());
    }

    public List<String> resolveCommitteeMemberLines(
            ApprovalSheetTextTemplateRule templates,
            ApprovalCommitteeMember member
    ) {
        Objects.requireNonNull(templates, "templates must not be null");
        Objects.requireNonNull(member, "member must not be null");

        Map<String, String> values = Map.of(
                "name", member.name(),
                "title", member.title().orElse(""),
                "institutionName", member.institutionName().orElse(""),
                "role", member.role().orElse("")
        );
        List<String> resolvedLines = new ArrayList<>();

        if (templates.committeeMemberTemplate().signatureLine().enabled()) {
            resolvedLines.add(resolve(templates.committeeMemberTemplate().signatureLine().text(), values));
        }

        for (String lineTemplate : templates.committeeMemberTemplate().lineTemplates()) {
            String resolvedLine = resolve(lineTemplate, values);

            if (!resolvedLine.isBlank()) {
                resolvedLines.add(resolvedLine);
            }
        }

        return List.copyOf(resolvedLines);
    }

    private static String resolve(String template, Map<String, String> valuesByPlaceholder) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuilder resolved = new StringBuilder();

        while (matcher.find()) {
            String placeholder = matcher.group(1);

            if (!valuesByPlaceholder.containsKey(placeholder)) {
                throw new InvalidProfileStructureException(
                        "Unknown approvalSheet template placeholder: " + placeholder
                );
            }

            matcher.appendReplacement(
                    resolved,
                    Matcher.quoteReplacement(valuesByPlaceholder.get(placeholder))
            );
        }

        matcher.appendTail(resolved);

        return normalize(resolved.toString());
    }

    private static Set<String> placeholdersIn(String template, Set<String> allowedPlaceholders) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        Set<String> placeholders = new HashSet<>();

        while (matcher.find()) {
            String placeholder = matcher.group(1);

            if (!allowedPlaceholders.contains(placeholder)) {
                throw new InvalidProfileStructureException(
                        "Unknown approvalSheet template placeholder: " + placeholder
                );
            }

            placeholders.add(placeholder);
        }

        return Set.copyOf(placeholders);
    }

    private static String normalize(String value) {
        return value
                .replaceAll("\\s+", " ")
                .replace(" .", ".")
                .replace(" ,", ",")
                .replace("( )", "")
                .trim();
    }
}
