package com.abntbuilder.formatter.input.profile;

import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CompiledTypeScriptContractIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldAcceptSectionIndexContractSerializedByTheTypeScriptCompiler() throws IOException {
        DocumentProfile profile = read("/compiled-profiles/section-index-from-typescript.json");

        assertEquals("profile", profile.id());
        assertEquals(1, profile.componentRules().size());
    }

    @Test
    void shouldAcceptElementIndexContractSerializedByTheTypeScriptCompiler() throws IOException {
        DocumentProfile profile = read("/compiled-profiles/element-index-from-typescript.json");

        assertEquals("element-profile", profile.id());
        assertEquals(1, profile.componentRules().size());
    }

    private DocumentProfile read(String resource) throws IOException {
        try (InputStream input = getClass().getResourceAsStream(resource)) {
            assertNotNull(input, "Fixture de contrato compilado não encontrado.");
            return objectMapper.readValue(input, ProfileDefinition.class).toDomain();
        }
    }
}
