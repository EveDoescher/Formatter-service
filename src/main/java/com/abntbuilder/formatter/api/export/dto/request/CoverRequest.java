package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.cover.CoverComponent;
import com.abntbuilder.formatter.profile.model.component.ComponentContentBindings;

import java.util.List;
import java.util.Optional;

public record CoverRequest(
        List<String> institutionalLines,
        List<String> authors,
        String title,
        String subtitle,
        String city,
        String year
) {

    public CoverComponent toDomain() {
        return toDomain(null, new ComponentContentBindings(java.util.Map.of()));
    }

    public CoverComponent toDomain(AcademicWorkRequest work, ComponentContentBindings bindings) {
        WorkContentBindingResolver resolver = new WorkContentBindingResolver("cover", work, bindings);

        return new CoverComponent(
                resolvedList(resolver.resolveStringList("institutionalLines", institutionalLines)),
                resolvedList(resolver.resolveStringList("authors", authors)),
                resolver.resolveString("title", title),
                Optional.ofNullable(resolver.resolveString("subtitle", subtitle)),
                resolver.resolveString("city", city),
                resolver.resolveString("year", year)
        );
    }

    private static List<String> resolvedList(List<String> value) {
        return value == null ? List.of() : value;
    }
}
