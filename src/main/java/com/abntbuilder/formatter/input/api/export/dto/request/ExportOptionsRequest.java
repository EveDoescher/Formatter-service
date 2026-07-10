package com.abntbuilder.formatter.input.api.export.dto.request;

import com.abntbuilder.formatter.engine.model.FontPreferences;

import java.util.List;
import java.util.Map;

public record ExportOptionsRequest(
        List<String> selectedComponents,
        Map<String, String> fonts
) {
    public FontPreferences toFontPreferences() {
        return fonts == null ? FontPreferences.NONE : new FontPreferences(fonts);
    }
}
