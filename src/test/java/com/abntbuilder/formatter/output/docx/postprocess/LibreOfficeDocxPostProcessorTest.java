package com.abntbuilder.formatter.output.docx.postprocess;

import com.abntbuilder.formatter.config.LibreOfficeProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LibreOfficeDocxPostProcessorTest {

    @Test
    void shouldReturnOriginalBytesWhenLibreOfficeNotFound() {
        LibreOfficeProperties props = new LibreOfficeProperties();
        props.setExecutablePath("nonexistent-soffice-binary");
        props.setTimeoutSeconds(5);

        LibreOfficeDocxPostProcessor processor = new LibreOfficeDocxPostProcessor(props);
        byte[] original = new byte[]{1, 2, 3, 4};

        byte[] result = processor.process(original);

        assertThat(result).isEqualTo(original);
    }

    @Test
    void shouldReturnOriginalBytesWhenInputIsEmpty() {
        LibreOfficeProperties props = new LibreOfficeProperties();
        props.setExecutablePath("nonexistent-soffice-binary");
        props.setTimeoutSeconds(5);

        LibreOfficeDocxPostProcessor processor = new LibreOfficeDocxPostProcessor(props);
        byte[] original = new byte[0];

        byte[] result = processor.process(original);

        assertThat(result).isEqualTo(original);
    }
}
