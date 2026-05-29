package com.abntbuilder.formatter.config;

import com.abntbuilder.formatter.rendering.component.cover.CoverRenderer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RenderingConfig {

    @Bean
    public CoverRenderer coverRenderer() {
        return new CoverRenderer();
    }
}
