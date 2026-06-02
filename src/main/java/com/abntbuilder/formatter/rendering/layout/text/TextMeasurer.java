package com.abntbuilder.formatter.rendering.layout.text;

import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;

public interface TextMeasurer {

    default MeasuredText measure(
            String text,
            PageRule pageRule,
            StyleRule styleRule
    ) {
        return measure(
                text,
                pageRule,
                styleRule,
                TextMeasurementArea.fromStyle(pageRule, styleRule)
        );
    }

    MeasuredText measure(
            String text,
            PageRule pageRule,
            StyleRule styleRule,
            TextMeasurementArea area
    );
}
