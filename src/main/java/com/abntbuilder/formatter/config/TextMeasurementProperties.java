package com.abntbuilder.formatter.config;

import com.abntbuilder.formatter.rendering.text.MissingFontPolicy;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "formatter.text-measurement")
public class TextMeasurementProperties {

    private MissingFontPolicy missingFontPolicy = MissingFontPolicy.FAIL;

    public MissingFontPolicy getMissingFontPolicy() {
        return missingFontPolicy;
    }

    public void setMissingFontPolicy(MissingFontPolicy missingFontPolicy) {
        this.missingFontPolicy = missingFontPolicy == null ? MissingFontPolicy.FAIL : missingFontPolicy;
    }
}
