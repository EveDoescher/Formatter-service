package com.abntbuilder.formatter.rendering.component.titlepage;

import com.abntbuilder.formatter.document.component.titlepage.TitlePageComponent;
import com.abntbuilder.formatter.output.docx.api.ParagraphLayoutOverride;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.titlepage.TitlePageComponentRule;
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
import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;
import com.abntbuilder.formatter.shared.exception.InvalidTitlePageContentException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class TitlePageLayoutAssembler {

    private final TextMeasurer textMeasurer;
    private final OrderedLayoutGapResolver gapResolver;
    private final TitlePageProfileContentValidator validator;
    private final TitlePageTextTemplateResolver templateResolver;
    private final HorizontalPlacementResolver horizontalPlacementResolver;

    public TitlePageLayoutAssembler(
            TextMeasurer textMeasurer,
            OrderedLayoutGapResolver gapResolver,
            TitlePageProfileContentValidator validator,
            TitlePageTextTemplateResolver templateResolver,
            HorizontalPlacementResolver horizontalPlacementResolver
    ) {
        this.textMeasurer = Objects.requireNonNull(textMeasurer, "textMeasurer must not be null");
        this.gapResolver = Objects.requireNonNull(gapResolver, "gapResolver must not be null");
        this.validator = Objects.requireNonNull(validator, "validator must not be null");
        this.templateResolver = Objects.requireNonNull(templateResolver, "templateResolver must not be null");
        this.horizontalPlacementResolver = Objects.requireNonNull(
                horizontalPlacementResolver,
                "horizontalPlacementResolver must not be null"
        );
    }

    public SinglePageLayoutInput assemble(
            TitlePageComponent component,
            DocumentProfile profile,
            TitlePageComponentRule rule
    ) {
        Objects.requireNonNull(component, "component must not be null");
        Objects.requireNonNull(profile, "profile must not be null");
        Objects.requireNonNull(rule, "rule must not be null");

        validator.validate(component, rule);

        StyleResolver styleResolver = new StyleResolver(profile);
        PageRule pageRule = profile.pageRule();
        List<SinglePageLayoutGroup> groups = new ArrayList<>();

        for (SinglePageGroupRule groupRule : rule.layoutRule().groups()) {
            List<SinglePageLayoutItem> items = assembleGroupItems(
                    component,
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
            TitlePageComponent component,
            StyleResolver styleResolver,
            PageRule pageRule,
            TitlePageComponentRule rule,
            SinglePageGroupRule groupRule
    ) {
        List<SinglePageLayoutItem> items = new ArrayList<>();

        for (int itemRuleIndex = 0; itemRuleIndex < groupRule.items().size(); itemRuleIndex++) {
            SinglePageItemRule itemRule = groupRule.items().get(itemRuleIndex);
            List<String> values = valuesForItem(component, rule, itemRule.id());
            boolean hasFollowingContent = hasFollowingContent(component, rule, groupRule.items(), itemRuleIndex);

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
                        throw InvalidTitlePageContentException.itemExceedsMaxVisualLines(itemRule.id(), maxLines);
                    }
                });

                items.add(new SinglePageLayoutItem(
                        itemInstanceId(itemRule.id(), valueIndex, values.size()),
                        styleRule,
                        String.join(" ", measuredText.visualLines()),
                        measuredText.visualLines(),
                        Optional.of(measurementArea),
                        layoutOverrideFor(itemRule, measurementArea),
                        blankLinesAfter(itemRule, valueIndex, values.size(), hasFollowingContent)
                ));
            }
        }

        return List.copyOf(items);
    }

    private boolean hasFollowingContent(
            TitlePageComponent component,
            TitlePageComponentRule rule,
            List<SinglePageItemRule> itemRules,
            int currentItemRuleIndex
    ) {
        for (int index = currentItemRuleIndex + 1; index < itemRules.size(); index++) {
            if (!valuesForItem(component, rule, itemRules.get(index).id()).isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private List<String> valuesForItem(
            TitlePageComponent component,
            TitlePageComponentRule rule,
            String itemId
    ) {
        return switch (itemId) {
            case "authors" -> component.authors();
            case "title" -> List.of(component.title());
            case "subtitle" -> component.subtitle().map(List::of).orElseGet(List::of);
            case "nature" -> List.of(templateResolver.resolveNature(rule.textTemplates(), component.nature()));
            case "advisor" -> component.advisor()
                    .map(advisor -> List.of(templateResolver.resolveAdvisor(rule.textTemplates(), advisor)))
                    .orElseGet(List::of);
            case "coadvisor" -> component.coadvisor()
                    .map(coadvisor -> List.of(templateResolver.resolveCoadvisor(rule.textTemplates(), coadvisor)))
                    .orElseGet(List::of);
            case "city" -> List.of(component.city());
            case "year" -> List.of(component.year());
            default -> throw new InvalidProfileStructureException("Unknown titlePage item id: " + itemId);
        };
    }

    private static String itemInstanceId(String itemId, int valueIndex, int valueCount) {
        if (valueCount == 1) {
            return itemId;
        }

        return itemId + "[" + valueIndex + "]";
    }

    private static int blankLinesAfter(
            SinglePageItemRule itemRule,
            int valueIndex,
            int valueCount,
            boolean hasFollowingContent
    ) {
        if (valueIndex < valueCount - 1 || hasFollowingContent) {
            return itemRule.blankLinesAfter();
        }

        return 0;
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
