package com.abntbuilder.formatter.rendering.singlepage;

import com.abntbuilder.formatter.engine.model.profile.StyleRule;
import com.abntbuilder.formatter.shared.measurement.MeasurementConverter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public final class SinglePageLayoutLineMetrics {

    public int layoutLineHeightTwips(List<SinglePageLayoutGroup> groups) {
        Objects.requireNonNull(groups, "groups must not be null");

        if (groups.isEmpty()) {
            throw new IllegalArgumentException("groups must not be empty.");
        }

        int maxLineHeightTwips = 0;

        for (SinglePageLayoutGroup group : groups) {
            Objects.requireNonNull(group, "groups must not contain null values.");

            for (SinglePageLayoutItem item : group.items()) {
                maxLineHeightTwips = Math.max(
                        maxLineHeightTwips,
                        exactLineHeightTwips(item.styleRule())
                );
            }
        }

        if (maxLineHeightTwips <= 0) {
            throw new IllegalArgumentException("layout line height must be greater than zero.");
        }

        return maxLineHeightTwips;
    }

    public int contentLineCount(List<SinglePageLayoutGroup> groups) {
        Objects.requireNonNull(groups, "groups must not be null");

        int count = 0;

        for (SinglePageLayoutGroup group : groups) {
            Objects.requireNonNull(group, "groups must not contain null values.");
            count += group.lineCount();
        }

        return count;
    }

    public int contentHeightTwips(List<SinglePageLayoutGroup> groups, int layoutLineHeightTwips) {
        if (layoutLineHeightTwips <= 0) {
            throw new IllegalArgumentException("layoutLineHeightTwips must be greater than zero.");
        }

        return contentHeightTwips(groups);
    }

    public int contentHeightTwips(List<SinglePageLayoutGroup> groups) {
        Objects.requireNonNull(groups, "groups must not be null");

        int heightTwips = 0;

        for (SinglePageLayoutGroup group : groups) {
            Objects.requireNonNull(group, "groups must not contain null values.");

            for (SinglePageLayoutItem item : group.items()) {
                heightTwips += itemHeightTwips(item);
            }
        }

        return heightTwips;
    }

    public int itemHeightTwips(SinglePageLayoutItem item) {
        Objects.requireNonNull(item, "item must not be null");

        return item.lineCount() * exactLineHeightTwips(item.styleRule());
    }

    public int exactLineHeightTwips(StyleRule styleRule) {
        return MeasurementConverter.pointsToTwips(exactLineHeightPt(styleRule));
    }

    public BigDecimal exactLineHeightPt(StyleRule styleRule) {
        Objects.requireNonNull(styleRule, "styleRule must not be null");

        return styleRule.fontSizePt().multiply(styleRule.lineSpacing());
    }
}
