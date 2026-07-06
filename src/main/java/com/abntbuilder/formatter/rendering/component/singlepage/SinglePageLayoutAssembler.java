package com.abntbuilder.formatter.rendering.component.singlepage;

import com.abntbuilder.formatter.document.component.singlepage.ComposedTextValue;
import com.abntbuilder.formatter.document.component.singlepage.ContentValue;
import com.abntbuilder.formatter.document.component.singlepage.SignatureBlockListValue;
import com.abntbuilder.formatter.document.component.singlepage.SinglePageContent;
import com.abntbuilder.formatter.document.component.singlepage.TextListValue;
import com.abntbuilder.formatter.document.component.singlepage.TextValue;
import com.abntbuilder.formatter.output.docx.api.ParagraphLayoutOverride;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.singlepage.ComposedTextSlotRule;
import com.abntbuilder.formatter.profile.model.component.singlepage.SignatureBlockListSlotRule;
import com.abntbuilder.formatter.profile.model.component.singlepage.SinglePageComponentRule;
import com.abntbuilder.formatter.profile.model.component.singlepage.SlotRule;
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
import com.abntbuilder.formatter.shared.exception.InvalidSinglePageContentException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class SinglePageLayoutAssembler {

    private final TextMeasurer textMeasurer;
    private final OrderedLayoutGapResolver gapResolver;
    private final HorizontalPlacementResolver horizontalPlacementResolver;

    public SinglePageLayoutAssembler(
            TextMeasurer textMeasurer,
            OrderedLayoutGapResolver gapResolver
    ) {
        this(textMeasurer, gapResolver, new HorizontalPlacementResolver());
    }

    public SinglePageLayoutAssembler(
            TextMeasurer textMeasurer,
            OrderedLayoutGapResolver gapResolver,
            HorizontalPlacementResolver horizontalPlacementResolver
    ) {
        this.textMeasurer = Objects.requireNonNull(textMeasurer, "textMeasurer must not be null");
        this.gapResolver = Objects.requireNonNull(gapResolver, "gapResolver must not be null");
        this.horizontalPlacementResolver = Objects.requireNonNull(
                horizontalPlacementResolver, "horizontalPlacementResolver must not be null");
    }

    public SinglePageLayoutInput assemble(
            SinglePageContent content,
            DocumentProfile profile,
            SinglePageComponentRule rule
    ) {
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(profile, "profile must not be null");
        Objects.requireNonNull(rule, "rule must not be null");

        StyleResolver styleResolver = new StyleResolver(profile);
        PageRule pageRule = profile.pageRule();
        List<SinglePageLayoutGroup> groups = new ArrayList<>();

        for (SinglePageGroupRule groupRule : rule.layoutRule().groups()) {
            List<SinglePageLayoutItem> items = assembleGroupItems(content, styleResolver, pageRule, rule, groupRule);
            if (!items.isEmpty()) {
                groups.add(new SinglePageLayoutGroup(groupRule.id(), items));
            }
        }

        List<String> presentGroupOrder = groups.stream().map(SinglePageLayoutGroup::id).toList();
        List<ResolvedLayoutGap> gaps = gapResolver.resolve(
                rule.layoutRule().declaredGroupOrder(),
                presentGroupOrder,
                rule.layoutRule().gapRules()
        );

        return new SinglePageLayoutInput(pageRule, groups, gaps, rule.layoutRule().policy());
    }

    private List<SinglePageLayoutItem> assembleGroupItems(
            SinglePageContent content,
            StyleResolver styleResolver,
            PageRule pageRule,
            SinglePageComponentRule rule,
            SinglePageGroupRule groupRule
    ) {
        List<SinglePageLayoutItem> items = new ArrayList<>();

        for (SinglePageItemRule itemRule : groupRule.items()) {
            SlotRule slotRule = rule.slots().get(itemRule.id());
            List<String> values = resolveValues(content, rule, itemRule.id(), slotRule);

            if (values.isEmpty()) {
                if (slotRule != null && slotRule.required()) {
                    throw InvalidSinglePageContentException.missingRequiredSlot(
                            content.componentId(), itemRule.id());
                }
                continue;
            }

            String styleId = rule.styleMapping().get(itemRule.id());
            if (styleId == null) {
                throw new InvalidProfileStructureException(
                        "No style mapping for slot '" + itemRule.id()
                                + "' in component '" + content.componentId() + "'.");
            }
            StyleRule styleRule = styleResolver.resolve(styleId);

            for (int i = 0; i < values.size(); i++) {
                String value = normalize(values.get(i));
                TextMeasurementArea measurementArea = horizontalPlacementResolver.resolve(
                        pageRule, styleRule, itemRule.horizontalPlacement());
                MeasuredText measuredText = textMeasurer.measure(value, pageRule, styleRule, measurementArea);

                itemRule.maxVisualLinesPerValue().ifPresent(maxLines -> {
                    if (measuredText.lineCount() > maxLines) {
                        throw InvalidSinglePageContentException.itemExceedsMaxVisualLines(itemRule.id(), maxLines);
                    }
                });

                int blankLinesAfter = (i < values.size() - 1) ? 0 : itemRule.blankLinesAfter();

                items.add(new SinglePageLayoutItem(
                        instanceId(itemRule.id(), i, values.size()),
                        styleRule,
                        String.join(" ", measuredText.visualLines()),
                        measuredText.visualLines(),
                        Optional.of(measurementArea),
                        layoutOverrideFor(itemRule, measurementArea),
                        blankLinesAfter
                ));
            }
        }

        return List.copyOf(items);
    }

    private static List<String> resolveValues(
            SinglePageContent content,
            SinglePageComponentRule rule,
            String slotId,
            SlotRule slotRule
    ) {
        ContentValue value = content.slots().get(slotId);

        if (value == null) {
            return List.of();
        }

        return switch (value) {
            case TextValue tv -> List.of(tv.text());
            case TextListValue tlv -> tlv.items();
            case ComposedTextValue ctv -> {
                if (!(slotRule instanceof ComposedTextSlotRule composedRule)) {
                    throw InvalidSinglePageContentException.slotTypeMismatch(
                            content.componentId(), slotId, "COMPOSED_TEXT", value.getClass().getSimpleName());
                }
                yield List.of(applyTemplate(content.componentId(), slotId, composedRule, ctv.fields()));
            }
            case SignatureBlockListValue sblv -> {
                if (!(slotRule instanceof SignatureBlockListSlotRule sigRule)) {
                    throw InvalidSinglePageContentException.slotTypeMismatch(
                            content.componentId(), slotId, "SIGNATURE_BLOCK_LIST", value.getClass().getSimpleName());
                }
                yield resolveSignatureBlock(content.componentId(), slotId, sigRule, sblv);
            }
            case com.abntbuilder.formatter.document.component.singlepage.TableValue ignored ->
                throw new com.abntbuilder.formatter.shared.exception.InvalidSinglePageContentException(
                        "TableValue is not supported in single-page layout components (slot '" + slotId
                        + "' in '" + content.componentId() + "').");
        };
    }

    private static String applyTemplate(
            String componentId,
            String slotId,
            ComposedTextSlotRule rule,
            Map<String, String> fields
    ) {
        String result = rule.template();
        for (String fieldName : rule.fieldNames()) {
            String fieldValue = fields.get(fieldName);
            if (fieldValue == null || fieldValue.isBlank()) {
                throw InvalidSinglePageContentException.missingTemplateField(componentId, slotId, fieldName);
            }
            result = result.replace("{" + fieldName + "}", fieldValue);
        }
        return result;
    }

    private static List<String> resolveSignatureBlock(
            String componentId,
            String slotId,
            SignatureBlockListSlotRule rule,
            SignatureBlockListValue value
    ) {
        List<String> lines = new ArrayList<>();
        for (Map<String, String> entry : value.entries()) {
            for (String lineTemplate : rule.lineTemplates()) {
                String line = lineTemplate;
                for (String fieldName : rule.knownFieldNames()) {
                    String fieldValue = entry.getOrDefault(fieldName, "");
                    line = line.replace("{" + fieldName + "}", fieldValue);
                }
                line = normalize(line);
                if (!line.isBlank()) {
                    lines.add(line);
                }
            }
            if (rule.signatureLineEnabled()) {
                lines.add(rule.signatureLineText());
            }
        }
        return List.copyOf(lines);
    }

    private static String normalize(String s) {
        return s.replaceAll("\\s+", " ").replace(" .", ".").replace(" ,", ",").trim();
    }

    private static String instanceId(String slotId, int index, int total) {
        return total == 1 ? slotId : slotId + "[" + index + "]";
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
