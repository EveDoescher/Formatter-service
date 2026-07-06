package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.flowtextual.FlowItem;
import com.abntbuilder.formatter.profile.model.component.flowtextual.FlowTextualComponentRule;
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
        return new FlowTextualComponentRule(componentId, domainItems);
    }

    public sealed interface FlowItemRequest
            permits HeadingItemRequest, BlankLinesItemRequest, PlainTextItemRequest,
                    TemplatedTextItemRequest, BoldLabeledKeywordsItemRequest,
                    PairListItemRequest, TableBlockItemRequest {
        FlowItem toDomain();
    }

    public record HeadingItemRequest(String styleId, String text) implements FlowItemRequest {
        public FlowItem toDomain() { return new FlowItem.HeadingItem(styleId, text); }
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
}
