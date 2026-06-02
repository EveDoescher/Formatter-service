package com.abntbuilder.formatter.profile.resolution;

import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.PageOrientation;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.StyleType;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.profile.model.component.cover.CoverComponentRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverLayoutRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverStyleMapping;
import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageComponentRule;
import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageStyleMapping;
import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageTextTemplateRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.HorizontalPlacementRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.HorizontalPlacementStrategy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.LayoutGapRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageGroupRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageItemRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutPolicy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutRule;
import com.abntbuilder.formatter.shared.exception.MissingProfileException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class InMemoryProfileProvider implements ProfileProvider {

    private final Map<String, DocumentProfile> profiles;

    public InMemoryProfileProvider() {
        this(Map.of("abnt-unip-profile", abntUnipProfile()));
    }

    public InMemoryProfileProvider(Map<String, DocumentProfile> profiles) {
        this.profiles = Map.copyOf(profiles);
    }

    @Override
    public DocumentProfile findById(String profileId) {
        DocumentProfile profile = profiles.get(profileId);

        if (profile == null) {
            throw new MissingProfileException(profileId);
        }

        return profile;
    }

    private static DocumentProfile abntUnipProfile() {
        return new DocumentProfile(
                "abnt-unip-profile",
                "ABNT UNIP Profile",
                new PageRule(
                        BigDecimal.valueOf(21),
                        BigDecimal.valueOf(29.7),
                        BigDecimal.valueOf(3),
                        BigDecimal.valueOf(2),
                        BigDecimal.valueOf(2),
                        BigDecimal.valueOf(3),
                        PageOrientation.PORTRAIT
                ),
                List.of(
                        style("cover.top", true, true),
                        style("cover.author", false, true),
                        style("cover.title", true, true),
                        style("cover.subtitle", false, false),
                        style("cover.bottom", false, true),
                        style("titlePage.author", false, true),
                        style("titlePage.title", true, true),
                        style("titlePage.subtitle", false, false),
                        style("titlePage.nature", TextAlignment.JUSTIFIED, BigDecimal.ONE, false, false),
                        style("titlePage.advisor", TextAlignment.LEFT, BigDecimal.ONE, false, false),
                        style("titlePage.coadvisor", TextAlignment.LEFT, BigDecimal.ONE, false, false),
                        style("titlePage.bottom", false, false)
                ),
                List.of(
                        new CoverComponentRule(
                                "cover",
                                new CoverStyleMapping(
                                        "cover.top",
                                        "cover.author",
                                        "cover.title",
                                        "cover.subtitle",
                                        "cover.bottom",
                                        "cover.bottom"
                                ),
                                defaultCoverLayoutRule()
                        ),
                        new TitlePageComponentRule(
                                "titlePage",
                                new TitlePageStyleMapping(
                                        "titlePage.author",
                                        "titlePage.title",
                                        "titlePage.subtitle",
                                        "titlePage.nature",
                                        "titlePage.advisor",
                                        "titlePage.coadvisor",
                                        "titlePage.bottom",
                                        "titlePage.bottom"
                                ),
                                new TitlePageTextTemplateRule(
                                        "{workType} para {degreeObjective} em {courseName} apresentado \u00e0 {institutionName}.",
                                        "Orientador(a): {academicTitle} {name}.",
                                        "Coorientador(a): {academicTitle} {name}."
                                ),
                                defaultTitlePageLayoutRule()
                        )
                )
        );
    }

    private static CoverLayoutRule defaultCoverLayoutRule() {
        return new CoverLayoutRule(
                List.of(
                        new SinglePageGroupRule(
                                CoverLayoutRule.INSTITUTION_GROUP_ID,
                                true,
                                List.of(new SinglePageItemRule("institutionalLines", true, Optional.empty()))
                        ),
                        new SinglePageGroupRule(
                                CoverLayoutRule.AUTHORS_GROUP_ID,
                                false,
                                List.of(new SinglePageItemRule("authors", false, Optional.empty()))
                        ),
                        new SinglePageGroupRule(
                                CoverLayoutRule.TITLE_GROUP_ID,
                                true,
                                List.of(
                                        new SinglePageItemRule("title", true, Optional.empty()),
                                        new SinglePageItemRule("subtitle", false, Optional.empty())
                                )
                        ),
                        new SinglePageGroupRule(
                                CoverLayoutRule.BOTTOM_GROUP_ID,
                                true,
                                List.of(
                                        new SinglePageItemRule("city", true, Optional.of(1)),
                                        new SinglePageItemRule("year", true, Optional.of(1))
                                )
                        )
                ),
                List.of(
                        new LayoutGapRule(
                                CoverLayoutRule.INSTITUTION_GROUP_ID,
                                CoverLayoutRule.AUTHORS_GROUP_ID,
                                BigDecimal.valueOf(30)
                        ),
                        new LayoutGapRule(
                                CoverLayoutRule.AUTHORS_GROUP_ID,
                                CoverLayoutRule.TITLE_GROUP_ID,
                                BigDecimal.valueOf(10)
                        ),
                        new LayoutGapRule(
                                CoverLayoutRule.TITLE_GROUP_ID,
                                CoverLayoutRule.BOTTOM_GROUP_ID,
                                BigDecimal.valueOf(60)
                        )
                ),
                SinglePageLayoutPolicy.defaultSinglePagePolicy()
        );
    }

    private static SinglePageLayoutRule defaultTitlePageLayoutRule() {
        HorizontalPlacementRule fullWidth = HorizontalPlacementRule.fullContentWidth();
        HorizontalPlacementRule rightHalf = new HorizontalPlacementRule(
                HorizontalPlacementStrategy.FROM_PAGE_CENTER_TO_RIGHT_MARGIN
        );

        return new SinglePageLayoutRule(
                List.of(
                        new SinglePageGroupRule(
                                "titlePage.authors",
                                true,
                                List.of(new SinglePageItemRule("authors", true, Optional.empty(), fullWidth))
                        ),
                        new SinglePageGroupRule(
                                "titlePage.titleBlock",
                                true,
                                List.of(
                                        new SinglePageItemRule("title", true, Optional.empty(), fullWidth),
                                        new SinglePageItemRule("subtitle", false, Optional.empty(), fullWidth)
                                )
                        ),
                        new SinglePageGroupRule(
                                "titlePage.natureBlock",
                                true,
                                List.of(
                                        new SinglePageItemRule("nature", true, Optional.empty(), rightHalf),
                                        new SinglePageItemRule("advisor", false, Optional.empty(), rightHalf),
                                        new SinglePageItemRule("coadvisor", false, Optional.empty(), rightHalf)
                                )
                        ),
                        new SinglePageGroupRule(
                                "titlePage.bottom",
                                true,
                                List.of(
                                        new SinglePageItemRule("city", true, Optional.of(1), fullWidth),
                                        new SinglePageItemRule("year", true, Optional.of(1), fullWidth)
                                )
                        )
                ),
                List.of(
                        new LayoutGapRule(
                                "titlePage.authors",
                                "titlePage.titleBlock",
                                BigDecimal.valueOf(20)
                        ),
                        new LayoutGapRule(
                                "titlePage.titleBlock",
                                "titlePage.natureBlock",
                                BigDecimal.valueOf(35)
                        ),
                        new LayoutGapRule(
                                "titlePage.natureBlock",
                                "titlePage.bottom",
                                BigDecimal.valueOf(45)
                        )
                ),
                SinglePageLayoutPolicy.defaultSinglePagePolicy()
        );
    }

    private static StyleRule style(String id, boolean bold, boolean uppercase) {
        return style(id, TextAlignment.CENTER, BigDecimal.valueOf(1.5), bold, uppercase);
    }

    private static StyleRule style(
            String id,
            TextAlignment alignment,
            BigDecimal lineSpacing,
            boolean bold,
            boolean uppercase
    ) {
        return new StyleRule(
                id,
                StyleType.PARAGRAPH,
                "Times New Roman",
                BigDecimal.valueOf(12),
                alignment,
                lineSpacing,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                bold,
                false,
                uppercase
        );
    }
}
