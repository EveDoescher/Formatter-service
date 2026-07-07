package com.abntbuilder.formatter.output.docx;

import com.abntbuilder.formatter.input.profile.ClasspathJsonProfileProvider;
import com.abntbuilder.formatter.config.LibreOfficeProperties;
import com.abntbuilder.formatter.engine.contract.PostProcessorResult;
import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LibreOfficeDocxPostProcessorTest {

    private static final DocumentProfile PROFILE =
            new ClasspathJsonProfileProvider().findById("abnt-unip-profile");

    @Test
    void shouldReturnOriginalBytesWhenLibreOfficeNotFound() {
        LibreOfficeProperties props = new LibreOfficeProperties();
        props.setExecutablePath("nonexistent-soffice-binary");
        props.setTimeoutSeconds(5);

        LibreOfficeDocxPostProcessor processor = new LibreOfficeDocxPostProcessor(props);
        byte[] original = new byte[]{1, 2, 3, 4};

        PostProcessorResult result = processor.process(original, PROFILE);

        assertThat(result.docxBytes()).isEqualTo(original);
        assertThat(result.warnings()).isEmpty();
    }

    @Test
    void shouldReturnOriginalBytesWhenInputIsEmpty() {
        LibreOfficeProperties props = new LibreOfficeProperties();
        props.setExecutablePath("nonexistent-soffice-binary");
        props.setTimeoutSeconds(5);

        LibreOfficeDocxPostProcessor processor = new LibreOfficeDocxPostProcessor(props);
        byte[] original = new byte[0];

        PostProcessorResult result = processor.process(original, PROFILE);

        assertThat(result.docxBytes()).isEqualTo(original);
    }
}
