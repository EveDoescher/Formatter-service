package com.abntbuilder.formatter.config;

import com.abntbuilder.formatter.engine.contract.DocxWriter;
import com.abntbuilder.formatter.output.docx.Docx4jWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocxWriterConfig {

    @Bean
    public DocxWriter docxWriter() {
        return new Docx4jWriter();
    }
}
