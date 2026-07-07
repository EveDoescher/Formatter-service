package com.abntbuilder.formatter.rendering.singlepage;

import com.abntbuilder.formatter.engine.model.profile.StyleRule;
import com.abntbuilder.formatter.shared.measurement.MeasurementConverter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record SinglePageSpacerLines(
        String gapId,
        String fromGroupId,
        String toGroupId,
        int lineCount,
        StyleRule styleRule,
        int lineHeightTwips,
        int heightTwips
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

    public SinglePageSpacerLines(
            String gapId,
            String fromGroupId,
            String toGroupId,
            int lineCount,
            StyleRule styleRule,
            int lineHeightTwips
    ) {
        this(
                gapId,
                fromGroupId,
                toGroupId,
                lineCount,
                styleRule,
                lineHeightTwips,
                lineCount * lineHeightTwips
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

        if (heightTwips < lineCount) {
            throw new IllegalArgumentException("heightTwips must be greater than or equal to lineCount.");
        }
    }

    @Override
    public int heightTwips() {
        return heightTwips;
    }

    public BigDecimal exactLineHeightPt() {
        return MeasurementConverter.twipsToPoints(lineHeightTwips);
    }

    public List<Integer> distributedLineHeightTwips() {
        int baseLineHeightTwips = heightTwips / lineCount;
        int remainingTwips = heightTwips % lineCount;
        List<Integer> lineHeights = new ArrayList<>();

        for (int index = 0; index < lineCount; index++) {
            lineHeights.add(baseLineHeightTwips + (index < remainingTwips ? 1 : 0));
        }

        return List.copyOf(lineHeights);
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
