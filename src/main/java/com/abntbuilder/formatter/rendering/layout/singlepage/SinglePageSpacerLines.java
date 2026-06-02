package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.shared.measurement.MeasurementConverter;

import java.math.BigDecimal;
import java.util.Objects;

public record SinglePageSpacerLines(
        String gapId,
        String fromGroupId,
        String toGroupId,
        int lineCount,
        StyleRule styleRule,
        int lineHeightTwips
) implements SinglePageLayoutElement {

    public SinglePageSpacerLines(
            String gapId,
            String fromGroupId,
            String toGroupId,
            int lineCount,
            StyleRule styleRule
    ) {
        this(
                gapId,
                fromGroupId,
                toGroupId,
                lineCount,
                styleRule,
                MeasurementConverter.pointsToTwips(styleRule.fontSizePt().multiply(styleRule.lineSpacing()))
        );
    }

    public SinglePageSpacerLines {
        requireNonBlank(gapId, "gapId");
        requireNonBlank(fromGroupId, "fromGroupId");
        requireNonBlank(toGroupId, "toGroupId");
        Objects.requireNonNull(styleRule, "styleRule must not be null");

        if (lineCount <= 0) {
            throw new IllegalArgumentException("lineCount must be greater than zero.");
        }

        if (lineHeightTwips <= 0) {
            throw new IllegalArgumentException("lineHeightTwips must be greater than zero.");
        }
    }

    @Override
    public int heightTwips() {
        return lineCount * lineHeightTwips;
    }

    public BigDecimal exactLineHeightPt() {
        return MeasurementConverter.twipsToPoints(lineHeightTwips);
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
