package com.abntbuilder.formatter.rendering.component.bodycontent;

import java.util.List;
import java.util.Objects;

public record BodyContentMetadata(
        List<BodySectionMetadata> sections,
        List<BodyDisplayObjectMetadata> figures,
        List<BodyDisplayObjectMetadata> tables,
        List<BodyDisplayObjectMetadata> frames,
        List<BodyDisplayObjectMetadata> charts,
        List<BodyDisplayObjectMetadata> codeListings,
        List<BodyAbbreviationMetadata> abbreviations
) {
    public BodyContentMetadata {
        Objects.requireNonNull(sections, "sections must not be null");
        Objects.requireNonNull(figures, "figures must not be null");
        Objects.requireNonNull(tables, "tables must not be null");
        Objects.requireNonNull(frames, "frames must not be null");
        Objects.requireNonNull(charts, "charts must not be null");
        Objects.requireNonNull(codeListings, "codeListings must not be null");
        Objects.requireNonNull(abbreviations, "abbreviations must not be null");
        sections = List.copyOf(sections);
        figures = List.copyOf(figures);
        tables = List.copyOf(tables);
        frames = List.copyOf(frames);
        charts = List.copyOf(charts);
        codeListings = List.copyOf(codeListings);
        abbreviations = List.copyOf(abbreviations);
    }

    public static BodyContentMetadata empty() {
        return new BodyContentMetadata(
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of()
        );
    }
}
