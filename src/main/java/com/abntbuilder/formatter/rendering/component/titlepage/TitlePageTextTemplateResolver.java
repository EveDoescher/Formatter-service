package com.abntbuilder.formatter.rendering.component.titlepage;

import com.abntbuilder.formatter.document.component.titlepage.AcademicPerson;
import com.abntbuilder.formatter.document.component.titlepage.TitlePageNature;
import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageTextTemplateRule;
import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TitlePageTextTemplateResolver {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([A-Za-z][A-Za-z0-9]*)}");

    public String resolveNature(
            TitlePageTextTemplateRule templates,
            TitlePageNature nature
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

    public String resolveAdvisor(
            TitlePageTextTemplateRule templates,
            AcademicPerson advisor
    ) {
        Objects.requireNonNull(templates, "templates must not be null");
        Objects.requireNonNull(advisor, "advisor must not be null");

        return resolvePerson(templates.advisorTemplate(), advisor);
    }

    public String resolveCoadvisor(
            TitlePageTextTemplateRule templates,
            AcademicPerson coadvisor
    ) {
        Objects.requireNonNull(templates, "templates must not be null");
        Objects.requireNonNull(coadvisor, "coadvisor must not be null");

        return resolvePerson(templates.coadvisorTemplate(), coadvisor);
    }

    private static String resolvePerson(String template, AcademicPerson person) {
        return resolve(
                template,
                Map.of(
                        "academicTitle", person.academicTitle().orElse(""),
                        "name", person.name()
                )
        );
    }

    private static String resolve(String template, Map<String, String> valuesByPlaceholder) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuilder resolved = new StringBuilder();

        while (matcher.find()) {
            String placeholder = matcher.group(1);

            if (!valuesByPlaceholder.containsKey(placeholder)) {
                throw new InvalidProfileStructureException("Unknown titlePage template placeholder: " + placeholder);
            }

            matcher.appendReplacement(
                    resolved,
                    Matcher.quoteReplacement(valuesByPlaceholder.get(placeholder))
            );
        }

        matcher.appendTail(resolved);

        return normalize(resolved.toString());
    }

    private static String normalize(String value) {
        return value
                .replaceAll("\\s+", " ")
                .replace(" .", ".")
                .replace(" ,", ",")
                .trim();
    }
}
