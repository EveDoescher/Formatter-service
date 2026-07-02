package com.abntbuilder.formatter.rendering.component.bodycontent;

import com.abntbuilder.formatter.profile.model.component.bodycontent.BodyContentNumberingRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SectionNumberingState {

    private static final int MAX_LEVEL = 6;

    private final BodyContentNumberingRule numberingRule;
    private final int[] counters = new int[MAX_LEVEL];

    public SectionNumberingState(BodyContentNumberingRule numberingRule) {
        this.numberingRule = Objects.requireNonNull(numberingRule, "numberingRule must not be null");
    }

    public String resolveTitle(int level, String title) {
        if (!numberingRule.enabled()) {
            return title;
        }
        increment(level);
        return sectionNumber(level) + " " + title;
    }

    public String resolveNumber(int level) {
        if (!numberingRule.enabled()) {
            return String.valueOf(level);
        }
        return sectionNumber(level);
    }

    private void increment(int level) {
        int index = level - 1;
        for (int currentIndex = 0; currentIndex < index; currentIndex++) {
            if (counters[currentIndex] == 0) {
                counters[currentIndex] = 1;
            }
        }
        counters[index]++;
        for (int currentIndex = index + 1; currentIndex < counters.length; currentIndex++) {
            counters[currentIndex] = 0;
        }
    }

    private String sectionNumber(int level) {
        if (level == 1) {
            return counters[0] + numberingRule.primarySuffix();
        }
        List<String> parts = new ArrayList<>();
        for (int index = 0; index < level; index++) {
            parts.add(String.valueOf(counters[index]));
        }
        return String.join(numberingRule.separator(), parts);
    }
}
