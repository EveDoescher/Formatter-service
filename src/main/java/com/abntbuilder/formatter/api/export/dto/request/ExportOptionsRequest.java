package com.abntbuilder.formatter.api.export.dto.request;

import java.util.List;

public record ExportOptionsRequest(
        List<String> selectedComponents
) {
}
