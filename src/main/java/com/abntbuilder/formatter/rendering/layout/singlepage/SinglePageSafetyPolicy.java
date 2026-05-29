package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.profile.model.PageRule;

public interface SinglePageSafetyPolicy {

    SinglePageRenderableArea calculate(PageRule pageRule, int lineHeightTwips);
}
