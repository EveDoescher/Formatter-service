package com.abntbuilder.formatter.rendering.singlepage;

import com.abntbuilder.formatter.engine.model.profile.PageRule;

public interface SinglePageSafetyPolicy {

    SinglePageRenderableArea calculate(PageRule pageRule, int lineHeightTwips);
}
