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
import com.abntbuilder.formatter.shared.exception.MissingProfileException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
                        style("cover.bottom", false, true)
                ),
                List.of(new CoverComponentRule(
                        "cover",
                        new CoverStyleMapping(
                                "cover.top",
                                "cover.author",
                                "cover.title",
                                "cover.subtitle",
                                "cover.bottom"
                        ),
                        new CoverLayoutRule(
                                BigDecimal.valueOf(30),
                                BigDecimal.valueOf(10),
                                BigDecimal.valueOf(60)
                        )
                ))
        );
    }

    private static StyleRule style(String id, boolean bold, boolean uppercase) {
        return new StyleRule(
                id,
                StyleType.PARAGRAPH,
                "Times New Roman",
                BigDecimal.valueOf(12),
                TextAlignment.CENTER,
                BigDecimal.valueOf(1.5),
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
