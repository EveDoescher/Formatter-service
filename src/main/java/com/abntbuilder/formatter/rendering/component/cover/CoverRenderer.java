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

    public CoverRenderer() {
        this(new SinglePageLayoutDocxMapper());
    }

    public CoverRenderer(SinglePageLayoutDocxMapper docxMapper) {
        this.docxMapper = Objects.requireNonNull(docxMapper, "docxMapper must not be null");
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

        List<SinglePageLayoutGroup> groups = createLayoutGroups(
                cover,
                coverRule,
                styleResolver
        );

        List<BigDecimal> gapWeights = createGapWeights(groups, coverRule.layoutRule());

        return docxMapper.mapToDocxBlocksAnchoringLastGroup(
                profile.pageRule(),
                groups,
                gapWeights,
                coverRule.layoutRule().safetyBlankLines()
        );
    }

    private static List<SinglePageLayoutGroup> createLayoutGroups(
            CoverComponent cover,
            CoverComponentRule coverRule,
            StyleResolver styleResolver
    ) {
        List<SinglePageLayoutGroup> groups = new ArrayList<>();

        addGroupIfNotEmpty(
                groups,
                TOP_GROUP_ID,
                cover.topLines(),
                styleResolver.resolve(coverRule.styleMapping().topLinesStyleId())
        );

        addGroupIfNotEmpty(
                groups,
                AUTHORS_GROUP_ID,
                cover.authorLines(),
                styleResolver.resolve(coverRule.styleMapping().authorLinesStyleId())
        );

        groups.add(createTitleGroup(
                cover,
                styleResolver.resolve(coverRule.styleMapping().titleStyleId()),
                styleResolver.resolve(coverRule.styleMapping().subtitleStyleId())
        ));

        addGroupIfNotEmpty(
                groups,
                BOTTOM_GROUP_ID,
                cover.bottomLines(),
                styleResolver.resolve(coverRule.styleMapping().bottomLinesStyleId())
        );

        if (groups.size() < 2) {
            throw new IllegalArgumentException("cover must contain at least two layout groups.");
        }

        return List.copyOf(groups);
    }

    private static void addGroupIfNotEmpty(
            List<SinglePageLayoutGroup> groups,
            String groupId,
            List<String> lines,
            StyleRule styleRule
    ) {
        if (lines.isEmpty()) {
            return;
        }

        List<SinglePageLayoutTextLine> textLines = lines.stream()
                .map(line -> new SinglePageLayoutTextLine(line, styleRule))
                .toList();

        groups.add(new SinglePageLayoutGroup(groupId, textLines));
    }

    private static SinglePageLayoutGroup createTitleGroup(
            CoverComponent cover,
            StyleRule titleStyle,
            StyleRule subtitleStyle
    ) {
        List<SinglePageLayoutTextLine> titleLines = new ArrayList<>();

        titleLines.add(new SinglePageLayoutTextLine(cover.title(), titleStyle));

        cover.subtitle().ifPresent(subtitle ->
                titleLines.add(new SinglePageLayoutTextLine(subtitle, subtitleStyle))
        );

        return new SinglePageLayoutGroup(TITLE_GROUP_ID, titleLines);
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