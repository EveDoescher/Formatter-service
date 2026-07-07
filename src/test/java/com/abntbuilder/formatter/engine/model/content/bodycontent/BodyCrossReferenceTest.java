package com.abntbuilder.formatter.engine.model.content.bodycontent;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BodyCrossReferenceTest {

    @Test
    void shouldCreateWithValidData() {
        BodyCrossReference ref = new BodyCrossReference("fig-1", CrossReferenceTargetType.FIGURE, CrossReferenceDisplayMode.LABEL_AND_NUMBER);
        assertThat(ref.targetId()).isEqualTo("fig-1");
        assertThat(ref.targetType()).isEqualTo(CrossReferenceTargetType.FIGURE);
        assertThat(ref.displayMode()).isEqualTo(CrossReferenceDisplayMode.LABEL_AND_NUMBER);
    }

    @Test
    void shouldRejectBlankTargetId() {
        assertThatThrownBy(() -> new BodyCrossReference("", CrossReferenceTargetType.FIGURE, CrossReferenceDisplayMode.NUMBER_ONLY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("targetId");
    }

    @Test
    void shouldRejectNullTargetId() {
        assertThatThrownBy(() -> new BodyCrossReference(null, CrossReferenceTargetType.FIGURE, CrossReferenceDisplayMode.NUMBER_ONLY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNullTargetType() {
        assertThatThrownBy(() -> new BodyCrossReference("sec-1", null, CrossReferenceDisplayMode.NUMBER_ONLY))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNullDisplayMode() {
        assertThatThrownBy(() -> new BodyCrossReference("sec-1", CrossReferenceTargetType.SECTION, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void renderedTextShouldContainTargetId() {
        BodyCrossReference ref = new BodyCrossReference("table-3", CrossReferenceTargetType.TABLE, CrossReferenceDisplayMode.CAPTION);
        assertThat(ref.renderedText()).contains("table-3");
    }
}
