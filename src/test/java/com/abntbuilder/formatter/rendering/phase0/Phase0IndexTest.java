package com.abntbuilder.formatter.rendering.phase0;

import com.abntbuilder.formatter.engine.model.content.bodycontent.CrossReferenceDisplayMode;
import com.abntbuilder.formatter.engine.model.content.bodycontent.CrossReferenceTargetType;
import com.abntbuilder.formatter.engine.model.profile.component.bodycontent.CrossReferenceLabelsRule;
import com.abntbuilder.formatter.engine.model.profile.component.elementindex.ElementType;
import com.abntbuilder.formatter.rendering.bodycontent.BodyDisplayObjectMetadata;
import com.abntbuilder.formatter.rendering.bodycontent.BodySectionMetadata;
import com.abntbuilder.formatter.shared.exception.InvalidBodyContentException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Phase0IndexTest {

    private static final CrossReferenceLabelsRule LABELS =
            new CrossReferenceLabelsRule("Seção", "Figura", "Tabela", "Quadro", "Gráfico", "Listagem", "Equação");

    @Test
    void shouldResolveFigureNumberOnly() {
        Phase0Index index = indexWithFigure("fig-1", 1, "Diagrama de casos de uso");

        String result = index.resolveCrossReference(
                "fig-1", CrossReferenceTargetType.FIGURE,
                CrossReferenceDisplayMode.NUMBER_ONLY, LABELS);

        assertThat(result).isEqualTo("1");
    }

    @Test
    void shouldResolveFigureLabelAndNumber() {
        Phase0Index index = indexWithFigure("fig-1", 1, "Diagrama de casos de uso");

        String result = index.resolveCrossReference(
                "fig-1", CrossReferenceTargetType.FIGURE,
                CrossReferenceDisplayMode.LABEL_AND_NUMBER, LABELS);

        assertThat(result).isEqualTo("Figura 1");
    }

    @Test
    void shouldResolveFigureCaption() {
        Phase0Index index = indexWithFigure("fig-1", 1, "Diagrama de casos de uso");

        String result = index.resolveCrossReference(
                "fig-1", CrossReferenceTargetType.FIGURE,
                CrossReferenceDisplayMode.CAPTION, LABELS);

        assertThat(result).isEqualTo("Diagrama de casos de uso");
    }

    @Test
    void shouldResolveSectionNumber() {
        Phase0Index index = indexWithSection("sec-1", 1, "1 Introdução", "1");

        String result = index.resolveCrossReference(
                "sec-1", CrossReferenceTargetType.SECTION,
                CrossReferenceDisplayMode.NUMBER_ONLY, LABELS);

        assertThat(result).isEqualTo("1");
    }

    @Test
    void shouldResolveSectionCaption() {
        Phase0Index index = indexWithSection("sec-1", 1, "1 Introdução", "1");

        String result = index.resolveCrossReference(
                "sec-1", CrossReferenceTargetType.SECTION,
                CrossReferenceDisplayMode.CAPTION, LABELS);

        assertThat(result).isEqualTo("1 Introdução");
    }

    @Test
    void shouldThrowForUnknownFigureTargetId() {
        Phase0Index index = Phase0Index.empty();

        assertThatThrownBy(() -> index.resolveCrossReference(
                "fig-inexistente", CrossReferenceTargetType.FIGURE,
                CrossReferenceDisplayMode.NUMBER_ONLY, LABELS))
                .isInstanceOf(InvalidBodyContentException.class)
                .hasMessageContaining("fig-inexistente");
    }

    @Test
    void shouldThrowForUnknownSectionTargetId() {
        Phase0Index index = Phase0Index.empty();

        assertThatThrownBy(() -> index.resolveCrossReference(
                "sec-inexistente", CrossReferenceTargetType.SECTION,
                CrossReferenceDisplayMode.NUMBER_ONLY, LABELS))
                .isInstanceOf(InvalidBodyContentException.class)
                .hasMessageContaining("sec-inexistente");
    }

    @Test
    void shouldResolveTableLabelAndNumber() {
        Phase0Index index = indexWithTable("tbl-1", 3, "Comparação de resultados");

        String result = index.resolveCrossReference(
                "tbl-1", CrossReferenceTargetType.TABLE,
                CrossReferenceDisplayMode.LABEL_AND_NUMBER, LABELS);

        assertThat(result).isEqualTo("Tabela 3");
    }

    private static Phase0Index indexWithFigure(String id, int number, String caption) {
        return new Phase0Index(
                Map.of(),
                Map.of(ElementType.FIGURE, Map.of(id, new BodyDisplayObjectMetadata(id, number, caption))),
                List.of()
        );
    }

    private static Phase0Index indexWithTable(String id, int number, String caption) {
        return new Phase0Index(
                Map.of(),
                Map.of(ElementType.TABLE, Map.of(id, new BodyDisplayObjectMetadata(id, number, caption))),
                List.of()
        );
    }

    private static Phase0Index indexWithSection(String id, int level, String renderedTitle, String renderedNumber) {
        return new Phase0Index(
                Map.of(id, new BodySectionMetadata(id, level, renderedTitle, renderedNumber)),
                Map.of(),
                List.of()
        );
    }
}
