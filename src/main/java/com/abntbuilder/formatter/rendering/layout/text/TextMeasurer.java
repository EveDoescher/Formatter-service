package com.abntbuilder.formatter.rendering.layout.text;

import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;

public interface TextMeasurer {

    MeasuredText measure(
            String text,
            PageRule pageRule,
            StyleRule styleRule
    );
}
