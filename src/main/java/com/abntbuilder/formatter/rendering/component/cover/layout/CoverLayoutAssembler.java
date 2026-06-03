package com.abntbuilder.formatter.rendering.component.cover.layout;

import com.abntbuilder.formatter.document.component.cover.CoverComponent;
import com.abntbuilder.formatter.output.docx.api.ParagraphLayoutOverride;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverComponentRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.HorizontalPlacementStrategy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageGroupRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageItemRule;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.layout.singlepage.HorizontalPlacementResolver;
import com.abntbuilder.formatter.rendering.layout.singlepage.OrderedLayoutGapResolver;
import com.abntbuilder.formatter.rendering.layout.singlepage.ResolvedLayoutGap;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutGroup;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutInput;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutItem;
import com.abntbuilder.formatter.rendering.layout.text.MeasuredText;
import com.abntbuilder.formatter.rendering.layout.text.TextMeasurer;
import com.abntbuilder.formatter.rendering.layout.text.TextMeasurementArea;
import com.abntbuilder.formatter.shared.exception.InvalidCoverContentException;
import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CoverLayoutAssembler {

    private final TextMeasurer textMeasurer;
    private final OrderedLayoutGapResolver gapResolver;
    private final CoverProfileContentValidator validator;
    private final HorizontalPlacementResolver horizontalPlacementResolver;

    public CoverLayoutAssembler(
            TextMeasurer textMeasurer,
            OrderedLayoutGapResolver gapResolver,
            CoverProfileContentValidator validator
    ) {
        this(
                textMeasurer,
                gapResolver,
                validator,
                new HorizontalPlacementResolver()
        );
    }

    public CoverLayoutAssembler(
            TextMeasurer textMeasurer,
            OrderedLayoutGapResolver gapResolver,
            CoverProfileContentValidator validator,
            HorizontalPlacementResolver horizontalPlacementResolver
    ) {
        this.textMeasurer = Objects.requireNonNull(textMeasurer, "textMeasurer must not be null");
        this.gapResolver = Objects.requireNonNull(gapResolver, "gapResolver must not be null");
        this.validator = Objects.requireNonNull(validator, "validator must not be null");
        this.horizontalPlacementResolver = Objects.requireNonNull(
                horizontalPlacementResolver,
                "horizontalPlacementResolver must not be null"
        );
    }

    public SinglePageLayoutInput assemble(
            CoverComponent cover,
            DocumentProfile profile,
            CoverComponentRule rule
    ) {
        Objects.requireNonNull(cover, "cover must not be null");
        Objects.requireNonNull(profile, "profile must not be null");
        Objects.requireNonNull(rule, "rule must not be null");

        validator.validate(cover, rule);

        StyleResolver styleResolver = new StyleResolver(profile);
        PageRule pageRule = profile.pageRule();
        List<SinglePageLayoutGroup> groups = new ArrayList<>();

        for (SinglePageGroupRule groupRule : rule.layoutRule().groups()) {
            List<SinglePageLayoutItem> items = assembleGroupItems(
                    cover,
                    styleResolver,
                    pageRule,
                    rule,
                    groupRule
            );

            if (!items.isEmpty()) {
                groups.add(new SinglePageLayoutGroup(groupRule.id(), items));
            }
        }

        List<String> presentGroupOrder = groups.stream()
                .map(SinglePageLayoutGroup::id)
                .toList();
        List<ResolvedLayoutGap> gaps = gapResolver.resolve(
                rule.layoutRule().declaredGroupOrder(),
                presentGroupOrder,
                rule.layoutRule().gapRules()
        );

        return new SinglePageLayoutInput(
                pageRule,
                groups,
                gaps,
                rule.layoutRule().policy()
        );
    }

    private List<SinglePageLayoutItem> assembleGroupItems(
            CoverComponent cover,
            StyleResolver styleResolver,
            PageRule pageRule,
            CoverComponentRule rule,
            SinglePageGroupRule groupRule
    ) {
        List<SinglePageLayoutItem> items = new ArrayList<>();

        for (SinglePageItemRule itemRule : groupRule.items()) {
            List<String> values = valuesForItem(cover, itemRule.id());

            for (int valueIndex = 0; valueIndex < values.size(); valueIndex++) {
                String value = values.get(valueIndex);
                StyleRule styleRule = styleResolver.resolve(rule.styleMapping().styleIdForItem(itemRule.id()));
                TextMeasurementArea measurementArea = horizontalPlacementResolver.resolve(
                        pageRule,
                        styleRule,
                        itemRule.horizontalPlacement()
                );
                MeasuredText measuredText = textMeasurer.measure(value, pageRule, styleRule, measurementArea);

                itemRule.maxVisualLinesPerValue().ifPresent(maxLines -> {
                    if (measuredText.lineCount() > maxLines) {
                        throw InvalidCoverContentException.itemExceedsMaxVisualLines(itemRule.id(), maxLines);
                    }
                });

                items.add(new SinglePageLayoutItem(
                        itemInstanceId(itemRule.id(), valueIndex, values.size()),
                        styleRule,
                        String.join(" ", measuredText.visualLines()),
                        measuredText.visualLines(),
                        Optional.of(measurementArea),
                        layoutOverrideFor(itemRule, measurementArea)
                ));
            }
        }

        return List.copyOf(items);
    }

    private static List<String> valuesForItem(CoverComponent cover, String itemId) {
        return switch (itemId) {
            case "institutionalLines" -> cover.institutionalLines();
            case "authors" -> cover.authors();
            case "title" -> List.of(cover.title());
            case "subtitle" -> cover.subtitle().map(List::of).orElseGet(List::of);
            case "city" -> List.of(cover.city());
            case "year" -> List.of(cover.year());
            default -> throw new InvalidProfileStructureException("Unknown cover item id: " + itemId);
        };
    }

    private static String itemInstanceId(String itemId, int valueIndex, int valueCount) {
        if (valueCount == 1) {
            return itemId;
        }

        return itemId + "[" + valueIndex + "]";
    }

    private static ParagraphLayoutOverride layoutOverrideFor(
            SinglePageItemRule itemRule,
            TextMeasurementArea measurementArea
    ) {
        if (itemRule.horizontalPlacement().strategy() == HorizontalPlacementStrategy.FULL_CONTENT_WIDTH) {
            return ParagraphLayoutOverride.none();
        }

        return new ParagraphLayoutOverride(
                Optional.of(measurementArea.leftIndentCm()),
                Optional.of(measurementArea.rightIndentCm()),
                Optional.empty()
        );
    }
}
