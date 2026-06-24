package com.abntbuilder.formatter.config;

import com.abntbuilder.formatter.output.docx.api.DocxPostProcessor;
import com.abntbuilder.formatter.output.docx.postprocess.LibreOfficeDocxPostProcessor;
import com.abntbuilder.formatter.output.docx.postprocess.NoOpDocxPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LibreOfficeProperties.class)
public class PostProcessingConfig {

    @Bean
    @ConditionalOnProperty(name = "formatter.libreoffice.enabled", havingValue = "true")
    public DocxPostProcessor libreOfficeDocxPostProcessor(LibreOfficeProperties properties) {
        return new LibreOfficeDocxPostProcessor(properties);
    }

    @Bean
    @ConditionalOnProperty(
            name = "formatter.libreoffice.enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public DocxPostProcessor noOpDocxPostProcessor() {
        return new NoOpDocxPostProcessor();
    }
}
