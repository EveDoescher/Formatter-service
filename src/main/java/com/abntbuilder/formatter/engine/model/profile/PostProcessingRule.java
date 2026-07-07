package com.abntbuilder.formatter.engine.model.profile;

import java.util.Objects;
import java.util.Optional;

public record PostProcessingRule(
        Optional<TableContinuationLabelsRule> tableContinuationLabels,
        Optional<OrphanTitleCorrectionRule> orphanTitleCorrection,
        Optional<IntegrityCheckRule> integrityCheck,
        Optional<PdfOutputRule> pdfOutput
) {
    public PostProcessingRule {
        Objects.requireNonNull(tableContinuationLabels, "tableContinuationLabels must not be null");
        Objects.requireNonNull(orphanTitleCorrection, "orphanTitleCorrection must not be null");
        Objects.requireNonNull(integrityCheck, "integrityCheck must not be null");
        Objects.requireNonNull(pdfOutput, "pdfOutput must not be null");
    }

    public record TableContinuationLabelsRule(
            boolean enabled,
            String continuesLabel,
            String continuationLabel,
            String conclusionLabel,
            String labelStyleId
    ) {
        public TableContinuationLabelsRule {
            if (enabled) {
                requireNonBlank(continuesLabel, "tableContinuationLabels.continuesLabel");
                requireNonBlank(continuationLabel, "tableContinuationLabels.continuationLabel");
                requireNonBlank(conclusionLabel, "tableContinuationLabels.conclusionLabel");
                requireNonBlank(labelStyleId, "tableContinuationLabels.labelStyleId");
            }
        }
    }

    public record OrphanTitleCorrectionRule(boolean enabled) {}

    public record IntegrityCheckRule(
            boolean enabled,
            boolean checkMarginOverflow,
            boolean checkFontSubstitution,
            Optional<Integer> maxPages
    ) {
        public IntegrityCheckRule {
            Objects.requireNonNull(maxPages, "integrityCheck.maxPages must not be null");
        }
    }

    public record PdfOutputRule(boolean enabled) {}

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
