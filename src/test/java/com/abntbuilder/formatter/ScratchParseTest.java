package com.abntbuilder.formatter;

import com.abntbuilder.formatter.api.export.dto.request.ExportDocxRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.File;

public class ScratchParseTest {

    @Test
    void testParse() throws Exception {
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();
        File file = new File("docs/samples/body-content/body-content-fase2-visual-test.json");
        try {
            ExportDocxRequest request = mapper.readValue(file, ExportDocxRequest.class);
            System.out.println("Parsed successfully: " + request.fileName());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
