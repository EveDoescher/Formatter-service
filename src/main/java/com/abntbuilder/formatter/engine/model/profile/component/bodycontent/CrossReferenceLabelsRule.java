package com.abntbuilder.formatter.engine.model.profile.component.bodycontent;

import com.abntbuilder.formatter.engine.model.content.bodycontent.CrossReferenceTargetType;

public record CrossReferenceLabelsRule(
        String sectionLabel,
        String figureLabel,
        String tableLabel,
        String frameLabel,
        String chartLabel,
        String codeListingLabel,
        String equationLabel
) {
    public CrossReferenceLabelsRule {
        requireNonBlank(sectionLabel, "sectionLabel");
        requireNonBlank(figureLabel, "figureLabel");
        requireNonBlank(tableLabel, "tableLabel");
        requireNonBlank(frameLabel, "frameLabel");
        requireNonBlank(chartLabel, "chartLabel");
        requireNonBlank(codeListingLabel, "codeListingLabel");
        requireNonBlank(equationLabel, "equationLabel");
    }

    public String labelFor(CrossReferenceTargetType type) {
        return switch (type) {
            case SECTION -> sectionLabel;
            case FIGURE -> figureLabel;
            case TABLE -> tableLabel;
            case FRAME -> frameLabel;
            case CHART -> chartLabel;
            case CODE_LISTING -> codeListingLabel;
            case EQUATION -> equationLabel;
        };
    }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
