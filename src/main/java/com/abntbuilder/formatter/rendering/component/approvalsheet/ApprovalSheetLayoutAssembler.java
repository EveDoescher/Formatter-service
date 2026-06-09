package com.abntbuilder.formatter.rendering.component.approvalsheet;

import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalCommitteeMember;
import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalEvent;
import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalSheetComponent;
import com.abntbuilder.formatter.output.docx.api.ParagraphLayoutOverride;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.approvalsheet.ApprovalSheetComponentRule;
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
import com.abntbuilder.formatter.shared.exception.InvalidApprovalSheetContentException;
import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class ApprovalSheetLayoutAssembler {

    private final TextMeasurer textMeasurer;
    private final OrderedLayoutGapResolver gapResolver;
    private final ApprovalSheetProfileContentValidator validator;
    private final ApprovalSheetTextTemplateResolver templateResolver;
    private final HorizontalPlacementResolver horizontalPlacementResolver;

    public ApprovalSheetLayoutAssembler(
            TextMeasurer textMeasurer,
            OrderedLayoutGapResolver gapResolver,
            ApprovalSheetProfileContentValidator validator,
            ApprovalSheetTextTemplateResolver templateResolver,
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
            ApprovalSheetComponent component,
            DocumentProfile profile,
            ApprovalSheetComponentRule rule
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
            ApprovalSheetComponent component,
            StyleResolver styleResolver,
            PageRule pageRule,
            ApprovalSheetComponentRule rule,
            SinglePageGroupRule groupRule
    ) {
        List<SinglePageLayoutItem> items = new ArrayList<>();

        for (int itemRuleIndex = 0; itemRuleIndex < groupRule.items().size(); itemRuleIndex++) {
            SinglePageItemRule itemRule = groupRule.items().get(itemRuleIndex);
            List<ApprovalSheetItemValue> values = valuesForItem(component, rule, itemRule.id());
            boolean hasFollowingContent = hasFollowingContent(component, rule, groupRule.items(), itemRuleIndex);

            for (int valueIndex = 0; valueIndex < values.size(); valueIndex++) {
                ApprovalSheetItemValue value = values.get(valueIndex);
                StyleRule styleRule = styleResolver.resolve(rule.styleMapping().styleIdForItem(itemRule.id()));
                TextMeasurementArea measurementArea = horizontalPlacementResolver.resolve(
                        pageRule,
                        styleRule,
                        itemRule.horizontalPlacement()
                );
                MeasuredText measuredText = textMeasurer.measure(value.text(), pageRule, styleRule, measurementArea);

                itemRule.maxVisualLinesPerValue().ifPresent(maxLines -> {
                    if (measuredText.lineCount() > maxLines) {
                        throw InvalidApprovalSheetContentException.itemExceedsMaxVisualLines(
                                itemRule.id(),
                                maxLines
                        );
                    }
                });

                items.add(new SinglePageLayoutItem(
                        itemInstanceId(itemRule.id(), valueIndex, values.size()),
                        styleRule,
                        String.join(" ", measuredText.visualLines()),
                        measuredText.visualLines(),
                        Optional.of(measurementArea),
                        layoutOverrideFor(itemRule, measurementArea),
                        blankLinesAfter(itemRule, value, valueIndex, values.size(), hasFollowingContent)
                ));
            }
        }

        return List.copyOf(items);
    }

    private boolean hasFollowingContent(
            ApprovalSheetComponent component,
            ApprovalSheetComponentRule rule,
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

    private List<ApprovalSheetItemValue> valuesForItem(
            ApprovalSheetComponent component,
            ApprovalSheetComponentRule rule,
            String itemId
    ) {
        return switch (itemId) {
            case "authors" -> component.authors().stream()
                    .map(ApprovalSheetItemValue::regular)
                    .toList();
            case "title" -> List.of(ApprovalSheetItemValue.regular(component.title()));
            case "subtitle" -> component.subtitle()
                    .map(subtitle -> List.of(ApprovalSheetItemValue.regular(subtitle)))
                    .orElseGet(List::of);
            case "nature" -> List.of(ApprovalSheetItemValue.regular(
                    templateResolver.resolveNature(rule.textTemplates(), component.nature())
            ));
            case "approvalText" -> approvalTextValues(component, rule);
            case "committeeHeading" -> component.committeeMembers().isEmpty()
                    ? List.of()
                    : List.of(ApprovalSheetItemValue.regular(
                            templateResolver.resolveCommitteeHeading(rule.textTemplates())
                    ));
            case "committeeMembers" -> committeeMemberValues(component, rule);
            default -> throw new InvalidProfileStructureException("Unknown approvalSheet item id: " + itemId);
        };
    }

    private List<ApprovalSheetItemValue> approvalTextValues(
            ApprovalSheetComponent component,
            ApprovalSheetComponentRule rule
    ) {
        Set<String> requiredFields = templateResolver.approvalTextRequiredFields(rule.textTemplates());

        if (requiredFields.isEmpty()) {
            return List.of(ApprovalSheetItemValue.regular(
                    templateResolver.resolveApprovalText(
                            rule.textTemplates(),
                            new ApprovalEvent(Optional.empty(), Optional.empty(), Optional.empty())
                    )
            ));
        }

        return component.approvalEvent()
                .filter(approvalEvent -> approvalEvent.hasContent())
                .map(event -> List.of(ApprovalSheetItemValue.regular(
                        templateResolver.resolveApprovalText(rule.textTemplates(), event)
                )))
                .orElseGet(List::of);
    }

    private List<ApprovalSheetItemValue> committeeMemberValues(
            ApprovalSheetComponent component,
            ApprovalSheetComponentRule rule
    ) {
        List<ApprovalSheetItemValue> values = new ArrayList<>();

        for (ApprovalCommitteeMember member : component.committeeMembers()) {
            List<String> memberLines = templateResolver.resolveCommitteeMemberLines(rule.textTemplates(), member);

            for (int lineIndex = 0; lineIndex < memberLines.size(); lineIndex++) {
                values.add(new ApprovalSheetItemValue(
                        memberLines.get(lineIndex),
                        lineIndex == memberLines.size() - 1
                ));
            }
        }

        return List.copyOf(values);
    }

    private static String itemInstanceId(String itemId, int valueIndex, int valueCount) {
        if (valueCount == 1) {
            return itemId;
        }

        return itemId + "[" + valueIndex + "]";
    }

    private static int blankLinesAfter(
            SinglePageItemRule itemRule,
            ApprovalSheetItemValue value,
            int valueIndex,
            int valueCount,
            boolean hasFollowingContent
    ) {
        if (!value.appliesConfiguredBlankLinesAfter()) {
            return 0;
        }

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

    private record ApprovalSheetItemValue(
            String text,
            boolean appliesConfiguredBlankLinesAfter
    ) {

        private static ApprovalSheetItemValue regular(String text) {
            return new ApprovalSheetItemValue(text, true);
        }
    }
}
