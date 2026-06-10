package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.titlepage.TitlePageComponent;
import com.abntbuilder.formatter.profile.model.component.ComponentContentBindings;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

public record TitlePageRequest(
        List<String> authors,
        String title,
        String subtitle,
        @Valid TitlePageNatureRequest nature,
        @Valid AcademicPersonRequest advisor,
        @Valid AcademicPersonRequest coadvisor,
        String city,
        String year
) {

    public TitlePageComponent toDomain() {
        return toDomain(null, new ComponentContentBindings(java.util.Map.of()));
    }

    public TitlePageComponent toDomain(AcademicWorkRequest work, ComponentContentBindings bindings) {
        WorkContentBindingResolver resolver = new WorkContentBindingResolver("titlePage", work, bindings);
        TitlePageNatureRequest resolvedNature = resolver.resolveTitlePageNature("nature", nature);

        return new TitlePageComponent(
                resolvedList(resolver.resolveStringList("authors", authors)),
                resolver.resolveString("title", title),
                Optional.ofNullable(resolver.resolveString("subtitle", subtitle)),
                requireNature(resolvedNature).toDomain(),
                Optional.ofNullable(resolver.resolveAcademicPerson("advisor", advisor))
                        .map(AcademicPersonRequest::toDomain),
                Optional.ofNullable(resolver.resolveAcademicPerson("coadvisor", coadvisor))
                        .map(AcademicPersonRequest::toDomain),
                resolver.resolveString("city", city),
                resolver.resolveString("year", year)
        );
    }

    private static List<String> resolvedList(List<String> value) {
        return value == null ? List.of() : value;
    }

    private static TitlePageNatureRequest requireNature(TitlePageNatureRequest value) {
        if (value == null) {
            throw new IllegalArgumentException("titlePage.nature must be provided explicitly or through work bindings.");
        }

        return value;
    }
}
