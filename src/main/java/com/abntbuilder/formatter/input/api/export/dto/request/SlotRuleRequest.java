package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.profile.component.singlepage.ComposedTextSlotRule;
import com.abntbuilder.formatter.engine.model.profile.component.singlepage.SignatureBlockListSlotRule;
import com.abntbuilder.formatter.engine.model.profile.component.singlepage.SlotRule;
import com.abntbuilder.formatter.engine.model.profile.component.singlepage.TextListSlotRule;
import com.abntbuilder.formatter.engine.model.profile.component.singlepage.TextSlotRule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SlotRuleRequest(
        @NotBlank String type,
        @NotNull Boolean required,
        // ComposedTextSlotRule fields
        String template,
        List<String> fieldNames,
        // SignatureBlockListSlotRule fields
        Boolean signatureLineEnabled,
        String signatureLineText,
        List<String> lineTemplates,
        List<String> knownFieldNames
) {

    public SlotRule toDomain() {
        return switch (type) {
            case "TEXT" -> new TextSlotRule(required);
            case "TEXT_LIST" -> new TextListSlotRule(required);
            case "COMPOSED_TEXT" -> new ComposedTextSlotRule(required, template, fieldNames);
            case "SIGNATURE_BLOCK_LIST" -> new SignatureBlockListSlotRule(
                    required,
                    signatureLineEnabled != null && signatureLineEnabled,
                    signatureLineText,
                    lineTemplates,
                    knownFieldNames
            );
            default -> throw new IllegalArgumentException("Unknown slot rule type: " + type);
        };
    }
}
