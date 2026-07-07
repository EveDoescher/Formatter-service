package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.content.DocumentComponent;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.CitationFormattingRule;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "contentType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SinglePageContentRequest.class, name = "SINGLE_PAGE"),
        @JsonSubTypes.Type(value = FlowTextualContentRequest.class, name = "FLOW_TEXTUAL"),
        @JsonSubTypes.Type(value = BodyContentRequest.class, name = "BODY_CONTENT"),
        @JsonSubTypes.Type(value = ReferencesRequest.class, name = "REFERENCES"),
        @JsonSubTypes.Type(value = SectionedContentRequest.class, name = "SECTIONED"),
        @JsonSubTypes.Type(value = SectionIndexContentRequest.class, name = "SECTION_INDEX"),
        @JsonSubTypes.Type(value = ElementIndexContentRequest.class, name = "ELEMENT_INDEX"),
})
public interface ComponentContentRequest {

    DocumentComponent toDomain(String componentId, CitationFormattingRule citationFormatting);
}
