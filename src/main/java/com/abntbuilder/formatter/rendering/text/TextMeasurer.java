package com.abntbuilder.formatter.rendering.text;

import com.abntbuilder.formatter.engine.model.profile.PageRule;
import com.abntbuilder.formatter.engine.model.profile.StyleRule;

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
