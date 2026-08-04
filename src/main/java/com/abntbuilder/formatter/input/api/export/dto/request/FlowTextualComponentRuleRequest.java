package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.profile.component.flowtextual.FlowItem;
import com.abntbuilder.formatter.engine.model.profile.component.flowtextual.FlowTextualComponentRule;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record FlowTextualComponentRuleRequest(
        @NotBlank String componentId,
        @NotEmpty List<FlowItemRequest> items
) {
    public FlowTextualComponentRule toDomain() {
        List<FlowItem> domainItems = items.stream()
                .map(FlowItemRequest::toDomain)
                .toList();
        return new FlowTextualComponentRule(componentId, true, null, domainItems);
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = HeadingItemRequest.class,            name = "HEADING"),
            @JsonSubTypes.Type(value = InlineHeadingItemRequest.class,      name = "INLINE_HEADING"),
            @JsonSubTypes.Type(value = BlankLinesItemRequest.class,         name = "BLANK_LINES"),
            @JsonSubTypes.Type(value = PlainTextItemRequest.class,          name = "PLAIN_TEXT"),
            @JsonSubTypes.Type(value = TemplatedTextItemRequest.class,      name = "TEMPLATED_TEXT"),
            @JsonSubTypes.Type(value = BoldLabeledKeywordsItemRequest.class, name = "BOLD_LABELED_KEYWORDS"),
            @JsonSubTypes.Type(value = PairListItemRequest.class,           name = "PAIR_LIST"),
            @JsonSubTypes.Type(value = TableBlockItemRequest.class,         name = "TABLE_BLOCK"),
            @JsonSubTypes.Type(value = RepeatGroupItemRequest.class,        name = "REPEAT_GROUP"),
    })
    public sealed interface FlowItemRequest
            permits HeadingItemRequest, InlineHeadingItemRequest, BlankLinesItemRequest,
                    PlainTextItemRequest, TemplatedTextItemRequest,
                    BoldLabeledKeywordsItemRequest, PairListItemRequest,
                    TableBlockItemRequest, RepeatGroupItemRequest {
        FlowItem toDomain();
    }

    public record HeadingItemRequest(String styleId, String text) implements FlowItemRequest {
        public FlowItem toDomain() { return new FlowItem.HeadingItem(styleId, text); }
    }

    public record InlineHeadingItemRequest(
            @NotBlank String headingStyleId,
            @NotBlank String headingText,
            @NotBlank String bodyStyleId,
            @NotBlank String bodySlotName) implements FlowItemRequest {
        public FlowItem toDomain() {
            return new FlowItem.InlineHeadingItem(headingStyleId, headingText, bodyStyleId, bodySlotName);
        }
    }

    public record BlankLinesItemRequest(String styleId, int count) implements FlowItemRequest {
        public FlowItem toDomain() { return new FlowItem.BlankLinesItem(styleId, count); }
    }

    public record PlainTextItemRequest(String styleId, String slotName) implements FlowItemRequest {
        public FlowItem toDomain() { return new FlowItem.PlainTextItem(styleId, slotName); }
    }

    public record TemplatedTextItemRequest(
            String styleId, String template, List<String> fieldNames) implements FlowItemRequest {
        public FlowItem toDomain() {
            return new FlowItem.TemplatedTextItem(styleId, template, fieldNames);
        }
    }

    public record BoldLabeledKeywordsItemRequest(
            String styleId, String labelSlotName, String keywordsSlotName,
            String separator, String terminator) implements FlowItemRequest {
        public FlowItem toDomain() {
            return new FlowItem.BoldLabeledKeywordsItem(
                    styleId, labelSlotName, keywordsSlotName, separator, terminator);
        }
    }

    public record PairListItemRequest(
            String styleId, String termsSlotName, String definitionsSlotName,
            String separator) implements FlowItemRequest {
        public FlowItem toDomain() {
            return new FlowItem.PairListItem(styleId, termsSlotName, definitionsSlotName, separator);
        }
    }

    public record TableBlockItemRequest(
            String headerStyleId, String cellStyleId,
            List<String> headers, String rowsSlotName) implements FlowItemRequest {
        public FlowItem toDomain() {
            return new FlowItem.TableBlockItem(headerStyleId, cellStyleId, headers, rowsSlotName);
        }
    }

    public record RepeatGroupItemRequest(
            @NotBlank String entriesSlotName,
            boolean pageBreakBetweenEntries,
            @NotEmpty List<FlowItemRequest> group) implements FlowItemRequest {
        public FlowItem toDomain() {
            List<FlowItem> domainGroup = group.stream().map(FlowItemRequest::toDomain).toList();
            return new FlowItem.RepeatGroupItem(entriesSlotName, pageBreakBetweenEntries, domainGroup);
        }
    }
}
