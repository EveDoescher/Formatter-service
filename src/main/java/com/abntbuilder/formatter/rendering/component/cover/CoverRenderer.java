package com.abntbuilder.formatter.rendering.component.cover;

import com.abntbuilder.formatter.document.component.cover.CoverComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverComponentRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverLayoutRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutDocxMapper;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutGroup;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutTextLine;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageTextLineBreaker;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CoverRenderer {

    private static final String COVER_COMPONENT_ID = "cover";

    private static final String TOP_GROUP_ID = "cover.top";
    private static final String AUTHORS_GROUP_ID = "cover.authors";
    private static final String TITLE_GROUP_ID = "cover.title";
    private static final String BOTTOM_GROUP_ID = "cover.bottom";

    private final SinglePageLayoutDocxMapper docxMapper;
    private final SinglePageTextLineBreaker lineBreaker;

    public CoverRenderer() {
        this(new SinglePageLayoutDocxMapper(), new SinglePageTextLineBreaker());
    }

    public CoverRenderer(
            SinglePageLayoutDocxMapper docxMapper,
            SinglePageTextLineBreaker lineBreaker
    ) {
        this.docxMapper = Objects.requireNonNull(docxMapper, "docxMapper must not be null");
        this.lineBreaker = Objects.requireNonNull(lineBreaker, "lineBreaker must not be null");
    }

    public List<DocxBlock> render(CoverComponent cover, DocumentProfile profile) {
        Objects.requireNonNull(cover, "cover must not be null");
        Objects.requireNonNull(profile, "profile must not be null");

        StyleResolver styleResolver = new StyleResolver(profile);
        ComponentRuleResolver componentRuleResolver = new ComponentRuleResolver(profile);

        CoverComponentRule coverRule = componentRuleResolver.resolve(
                COVER_COMPONENT_ID,
                CoverComponentRule.class
        );

        CoverLayoutRule layoutRule = coverRule.layoutRule();

        List<SinglePageLayoutGroup> groups = createLayoutGroups(
                cover,
                coverRule,
                styleResolver,
                layoutRule.maxCharactersPerLine()
        );

        validateAnchoredBottomGroup(groups);

        List<BigDecimal> gapWeights = createGapWeights(groups, layoutRule);

        return docxMapper.mapToDocxBlocksAnchoringLastGroup(
                profile.pageRule(),
                groups,
                gapWeights,
                layoutRule.bottomPaddingLineSlots()
        );
    }

    private List<SinglePageLayoutGroup> createLayoutGroups(
            CoverComponent cover,
            CoverComponentRule coverRule,
            StyleResolver styleResolver,
            int maxCharactersPerLine
    ) {
        List<SinglePageLayoutGroup> groups = new ArrayList<>();

        addGroupIfNotEmpty(
                groups,
                TOP_GROUP_ID,
                cover.topLines(),
                styleResolver.resolve(coverRule.styleMapping().topLinesStyleId()),
                maxCharactersPerLine
        );

        addGroupIfNotEmpty(
                groups,
                AUTHORS_GROUP_ID,
                cover.authorLines(),
                styleResolver.resolve(coverRule.styleMapping().authorLinesStyleId()),
                maxCharactersPerLine
        );

        groups.add(createTitleGroup(
                cover,
                styleResolver.resolve(coverRule.styleMapping().titleStyleId()),
                styleResolver.resolve(coverRule.styleMapping().subtitleStyleId()),
                maxCharactersPerLine
        ));

        addGroupIfNotEmpty(
                groups,
                BOTTOM_GROUP_ID,
                cover.bottomLines(),
                styleResolver.resolve(coverRule.styleMapping().bottomLinesStyleId()),
                maxCharactersPerLine
        );

        return List.copyOf(groups);
    }

    private void addGroupIfNotEmpty(
            List<SinglePageLayoutGroup> groups,
            String groupId,
            List<String> lines,
            StyleRule styleRule,
            int maxCharactersPerLine
    ) {
        if (lines.isEmpty()) {
            return;
        }

        List<SinglePageLayoutTextLine> textLines = new ArrayList<>();

        for (String line : lines) {
            for (String brokenLine : lineBreaker.breakText(line, maxCharactersPerLine)) {
                textLines.add(new SinglePageLayoutTextLine(brokenLine, styleRule));
            }
        }

        groups.add(new SinglePageLayoutGroup(groupId, textLines));
    }

    private SinglePageLayoutGroup createTitleGroup(
            CoverComponent cover,
            StyleRule titleStyle,
            StyleRule subtitleStyle,
            int maxCharactersPerLine
    ) {
        List<SinglePageLayoutTextLine> titleLines = new ArrayList<>();

        for (String line : lineBreaker.breakText(cover.title(), maxCharactersPerLine)) {
            titleLines.add(new SinglePageLayoutTextLine(line, titleStyle));
        }

        cover.subtitle().ifPresent(subtitle -> {
            for (String line : lineBreaker.breakText(subtitle, maxCharactersPerLine)) {
                titleLines.add(new SinglePageLayoutTextLine(line, subtitleStyle));
            }
        });

        return new SinglePageLayoutGroup(TITLE_GROUP_ID, titleLines);
    }

    private static void validateAnchoredBottomGroup(List<SinglePageLayoutGroup> groups) {
        if (groups.isEmpty()) {
            throw new IllegalArgumentException("cover must contain layout groups.");
        }

        SinglePageLayoutGroup lastGroup = groups.getLast();

        if (!BOTTOM_GROUP_ID.equals(lastGroup.id())) {
            throw new IllegalArgumentException("cover must contain bottomLines for anchored bottom layout.");
        }

        if (lastGroup.lines().size() != 2) {
            throw new IllegalArgumentException("cover bottomLines must contain exactly city and year.");
        }
    }

    private static List<BigDecimal> createGapWeights(
            List<SinglePageLayoutGroup> groups,
            CoverLayoutRule layoutRule
    ) {
        List<BigDecimal> weights = new ArrayList<>();

        for (int index = 0; index < groups.size() - 1; index++) {
            String current = groups.get(index).id();
            String next = groups.get(index + 1).id();

            weights.add(resolveGapWeight(current, next, layoutRule));
        }

        return List.copyOf(weights);
    }

    private static BigDecimal resolveGapWeight(
            String currentGroupId,
            String nextGroupId,
            CoverLayoutRule layoutRule
    ) {
        if (TOP_GROUP_ID.equals(currentGroupId) && AUTHORS_GROUP_ID.equals(nextGroupId)) {
            return layoutRule.topToAuthorWeight();
        }

        if (AUTHORS_GROUP_ID.equals(currentGroupId) && TITLE_GROUP_ID.equals(nextGroupId)) {
            return layoutRule.authorToTitleWeight();
        }

        if (TITLE_GROUP_ID.equals(currentGroupId) && BOTTOM_GROUP_ID.equals(nextGroupId)) {
            return layoutRule.titleToBottomWeight();
        }

        return BigDecimal.ONE;
    }
}