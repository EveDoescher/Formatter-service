package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.CitationFormattingRule;
import jakarta.validation.constraints.NotBlank;

public record CitationFormattingRuleRequest(
        @NotBlank String pagePrefix,
        @NotBlank String multiAuthorJoiner,
        @NotBlank String etAl,
        @NotBlank String apudConnector,
        @NotBlank String suppressionMarker,
        @NotBlank String emphasisOursLabel,
        @NotBlank String emphasisAuthorLabel,
        @NotBlank String verbalCitationLabel,
        String authorYearSeparator,
        String pageReferenceSeparator,
        String parenOpen,
        String parenClose,
        Boolean etAlItalic,
        String numericPrefix,
        String numericSuffix,
        String numericRangeSeparator,
        String numericListSeparator,
        Boolean numericRangeCollapse,
        String multiSourceSeparator,
        String ibidLabel,
        String noteStyleId,
        CitationFormattingRule.FootnoteRestartPolicy footnoteRestartPolicy
) {

    public CitationFormattingRule toDomain() {
        return new CitationFormattingRule(
                pagePrefix, multiAuthorJoiner, etAl, apudConnector, suppressionMarker,
                emphasisOursLabel, emphasisAuthorLabel, verbalCitationLabel,
                authorYearSeparator, pageReferenceSeparator, parenOpen, parenClose,
                etAlItalic != null && etAlItalic,
                java.util.Optional.ofNullable(numericPrefix),
                java.util.Optional.ofNullable(numericSuffix),
                java.util.Optional.ofNullable(numericRangeSeparator),
                java.util.Optional.ofNullable(numericListSeparator),
                numericRangeCollapse != null && numericRangeCollapse,
                multiSourceSeparator,
                java.util.Optional.ofNullable(ibidLabel),
                java.util.Optional.ofNullable(noteStyleId),
                footnoteRestartPolicy
        );
    }
}
