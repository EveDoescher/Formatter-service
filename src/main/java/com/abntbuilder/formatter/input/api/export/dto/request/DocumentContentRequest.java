package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.content.DocumentComponent;
import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.BodyContentComponentRule;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.CitationFormattingRule;
import com.abntbuilder.formatter.input.profile.ComponentRuleResolver;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DocumentContentRequest {

    private final Map<String, ComponentContentRequest> components = new LinkedHashMap<>();

    @JsonAnySetter
    public void addComponent(String key, ComponentContentRequest value) {
        if (value != null) components.put(key, value);
    }

    public List<DocumentComponent> toComponents() {
        return toComponents(null);
    }

    public List<DocumentComponent> toComponents(DocumentProfile profile) {
        ComponentRuleResolver ruleResolver = profile == null ? null : new ComponentRuleResolver(profile);
        List<DocumentComponent> result = new ArrayList<>();

        for (Map.Entry<String, ComponentContentRequest> entry : components.entrySet()) {
            String componentId = entry.getKey();
            ComponentContentRequest request = entry.getValue();

            CitationFormattingRule citationFormatting = null;
            if (ruleResolver != null && request instanceof BodyContentRequest) {
                citationFormatting = ruleResolver
                        .resolve(componentId, BodyContentComponentRule.class)
                        .citationFormatting();
            }
            if (ruleResolver != null && request instanceof SectionedContentRequest) {
                try {
                    citationFormatting = ruleResolver
                            .resolve("bodyContent", BodyContentComponentRule.class)
                            .citationFormatting();
                } catch (Exception ignored) {
                    // bodyContent may not be in this profile
                }
            }

            if (request instanceof SinglePageContentRequest sp && !sp.hasSlots()) {
                continue;
            }

            result.add(request.toDomain(componentId, citationFormatting));
        }

        return List.copyOf(result);
    }
}
