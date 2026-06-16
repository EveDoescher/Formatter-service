package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.bodycontent.BodyCitationType;
import com.abntbuilder.formatter.document.component.bodycontent.BodyLongQuote;
import com.abntbuilder.formatter.document.component.bodycontent.BodyFigure;
import com.abntbuilder.formatter.document.component.bodycontent.BodyParagraph;
import com.abntbuilder.formatter.document.component.bodycontent.BodySection;
import com.abntbuilder.formatter.document.component.bodycontent.BodyTable;
import com.abntbuilder.formatter.document.component.bodycontent.ImageSourceType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class BodySectionRequestTest {

    @Test
    void shouldConvertSemanticContentBlocksToDomain() {
        BodySectionRequest request = new BodySectionRequest(
                "citacoes",
                1,
                "Citacoes",
                null,
                null,
                List.of(
                        new BodyBlockRequest(
                                BodyBlockType.PARAGRAPH,
                                null,
                                "Paragrafo comum.",
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                        ),
                        new BodyBlockRequest(
                                BodyBlockType.DIRECT_LONG_QUOTE,
                                null,
                                "Citacao direta longa.",
                                null,
                                new CitationSourceRequest(List.of(author("Sobrenome Teste Um")), "2020", "10"),
                                null,
                                null,
                                null,
                                null
                        )
                )
        );

        BodySection section = request.toDomain();

        BodyParagraph paragraph = assertInstanceOf(BodyParagraph.class, section.content().get(0));
        BodyLongQuote longQuote = assertInstanceOf(BodyLongQuote.class, section.content().get(1));

        assertEquals("Paragrafo comum.", paragraph.text());
        assertEquals("Citacao direta longa.", longQuote.text());
    }

    @Test
    void shouldConvertParagraphInlineContentToDomain() {
        BodySectionRequest request = new BodySectionRequest(
                "citacoes-inline",
                1,
                "Citacoes inline",
                null,
                null,
                List.of(new BodyBlockRequest(
                        BodyBlockType.PARAGRAPH,
                        null,
                        null,
                        List.of(
                                new BodyInlineRequest(
                                        BodyInlineType.TEXT,
                                        "Segundo ",
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null
                                ),
                                new BodyInlineRequest(
                                        BodyInlineType.CITATION,
                                        null,
                                        null,
                                        BodyCitationType.DIRECT_SHORT,
                                        com.abntbuilder.formatter.document.component.bodycontent.BodyCitationMode.NARRATIVE,
                                        new CitationSourceRequest(List.of(author("Sobrenome Teste Um")), "2020", "10"),
                                        null,
                                        null
                                ),
                                new BodyInlineRequest(
                                        BodyInlineType.TEXT,
                                        ", ",
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null
                                ),
                                new BodyInlineRequest(
                                        BodyInlineType.QUOTE_TEXT,
                                        "a organizacao documental depende de criterios formais",
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null
                                )
                        ),
                        null,
                        null,
                        null,
                        null,
                        null
                ))
        );

        BodySection section = request.toDomain();

        BodyParagraph paragraph = assertInstanceOf(BodyParagraph.class, section.content().getFirst());

        assertEquals(
                "Segundo Sobrenome Teste Um (2020, p. 10), \"a organizacao documental depende de criterios formais\"",
                paragraph.text()
        );
    }

    @Test
    void shouldConvertLegacyParagraphsToParagraphBlocks() {
        BodySectionRequest request = new BodySectionRequest(
                "introducao",
                1,
                "Introducao",
                List.of("Paragrafo legado."),
                null,
                null
        );

        BodySection section = request.toDomain();

        BodyParagraph paragraph = assertInstanceOf(BodyParagraph.class, section.content().getFirst());

        assertEquals("Paragrafo legado.", paragraph.text());
    }

    @Test
    void shouldConvertFigureBlockToDomain() {
        BodySectionRequest request = new BodySectionRequest(
                "figuras",
                1,
                "Figuras",
                null,
                null,
                List.of(new BodyBlockRequest(
                        BodyBlockType.FIGURE,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        new BodyFigureRequest(
                                "figura-teste",
                                "grupo-figura-teste",
                                "Figura teste",
                                "Elaboração teste",
                                new ImageSourceRequest(
                                        ImageSourceType.DATA_URI,
                                        "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/p9sAAAAASUVORK5CYII=",
                                        "Imagem teste",
                                        null
                                )
                        ),
                        null
                ))
        );

        BodySection section = request.toDomain();

        BodyFigure figure = assertInstanceOf(BodyFigure.class, section.content().getFirst());

        assertEquals("figura-teste", figure.id());
        assertEquals("grupo-figura-teste", figure.continuationGroupId().orElseThrow());
        assertEquals("Figura teste", figure.caption());
    }

    @Test
    void shouldConvertTableBlockToDomain() {
        BodySectionRequest request = new BodySectionRequest(
                "tabelas",
                1,
                "Tabelas",
                null,
                null,
                List.of(new BodyBlockRequest(
                        BodyBlockType.TABLE,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        new BodyTableRequest(
                                "tabela-teste",
                                "grupo-tabela-teste",
                                "Resultados de teste",
                                "Elaboração teste",
                                List.of(
                                        new BodyTableColumnRequest("Cenário"),
                                        new BodyTableColumnRequest("Resultado")
                                ),
                                List.of(new BodyTableRowRequest(List.of("Teste A", "Aprovado")))
                        )
                ))
        );

        BodySection section = request.toDomain();

        BodyTable table = assertInstanceOf(BodyTable.class, section.content().getFirst());

        assertEquals("tabela-teste", table.id());
        assertEquals("grupo-tabela-teste", table.continuationGroupId().orElseThrow());
        assertEquals("Resultados de teste", table.caption());
        assertEquals(2, table.columns().size());
        assertEquals(1, table.rows().size());
    }

    private static CitationAuthorRequest author(String surname) {
        return new CitationAuthorRequest(
                com.abntbuilder.formatter.document.component.bodycontent.CitationAuthorType.PERSON,
                surname,
                null,
                null,
                null
        );
    }
}
