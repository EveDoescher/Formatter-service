package com.abntbuilder.formatter.rendering.component.cover;

import com.abntbuilder.formatter.application.export.DocxExportService;
import com.abntbuilder.formatter.application.export.ExportDocxCommand;
import com.abntbuilder.formatter.application.export.InMemoryGeneratedDocxExportStore;
import com.abntbuilder.formatter.document.component.cover.CoverComponent;
import com.abntbuilder.formatter.output.docx.docx4j.Docx4jWriter;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.PageOrientation;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.StyleType;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.profile.model.component.cover.CoverComponentRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverLayoutRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverStyleMapping;
import com.abntbuilder.formatter.shared.exception.InvalidCoverContentException;
import com.abntbuilder.formatter.shared.exception.SinglePageLayoutOverflowException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CoverSampleValidationTest {

    private static final Path COVER_SAMPLES_DIR = Path.of("docs", "samples", "cover");

    private final DocxExportService exportService = new DocxExportService(
            new Docx4jWriter(),
            new InMemoryGeneratedDocxExportStore()
    );

    @Test
    void shouldGenerateAllSuccessfulCoverSamples() {
        List<CoverSample> samples = List.of(
                new CoverSample("cover-short.json", shortCover()),
                new CoverSample("cover-no-subtitle.json", noSubtitleCover()),
                new CoverSample("cover-long-title.json", longTitleCover()),
                new CoverSample("cover-many-authors.json", manyAuthorsCover()),
                new CoverSample("cover-long-title-many-authors.json", longTitleManyAuthorsCover()),
                new CoverSample("cover-limit.json", limitCover())
        );

        for (CoverSample sample : samples) {
            assertSampleFileExists(sample.sampleName());

            byte[] bytes = exportService.export(toCommand(sample));

            assertTrue(bytes.length > 0, sample.sampleName() + " should generate a DOCX.");
            assertTrue(startsWithZipHeader(bytes), sample.sampleName() + " should generate ZIP/DOCX bytes.");
        }
    }

    @Test
    void shouldFailOverflowCoverSampleBeforeGeneratingDocx() {
        CoverSample sample = new CoverSample("cover-overflow.json", overflowCover());

        assertSampleFileExists(sample.sampleName());

        assertThrows(
                SinglePageLayoutOverflowException.class,
                () -> exportService.export(toCommand(sample))
        );
    }

    @Test
    void shouldFailInvalidBottomWrapCoverSampleBeforeGeneratingDocx() {
        CoverSample sample = new CoverSample("cover-bottom-wrap-invalid.json", bottomWrapInvalidCover());

        assertSampleFileExists(sample.sampleName());

        InvalidCoverContentException exception = assertThrows(
                InvalidCoverContentException.class,
                () -> exportService.export(toCommand(sample))
        );

        assertEquals("cover bottomLines must contain exactly city and year.", exception.getMessage());
    }

    private static ExportDocxCommand toCommand(CoverSample sample) {
        return new ExportDocxCommand(
                sample.sampleName().replace(".json", ".docx"),
                validProfile(),
                Optional.of(sample.cover()),
                List.of()
        );
    }

    private static void assertSampleFileExists(String sampleName) {
        Path samplePath = COVER_SAMPLES_DIR.resolve(sampleName);

        assertTrue(Files.isRegularFile(samplePath), "Missing cover sample: " + sampleName);
    }

    private static boolean startsWithZipHeader(byte[] bytes) {
        return bytes.length >= 4
                && bytes[0] == 'P'
                && bytes[1] == 'K'
                && bytes[2] == 3
                && bytes[3] == 4;
    }

    private static CoverComponent shortCover() {
        return new CoverComponent(
                List.of("Universidade Paulista"),
                List.of("Nome Completo do Aluno"),
                "Titulo do Trabalho",
                Optional.of("Subtitulo do trabalho"),
                List.of("Limeira", "2026")
        );
    }

    private static CoverComponent noSubtitleCover() {
        return new CoverComponent(
                List.of("Universidade Paulista"),
                List.of("Nome Completo do Aluno"),
                "Titulo do Trabalho sem Subtitulo",
                Optional.empty(),
                List.of("Limeira", "2026")
        );
    }

    private static CoverComponent longTitleCover() {
        return new CoverComponent(
                List.of(
                        "Universidade da Integracao Internacional da Lusofonia Afro-Brasileira",
                        "Curso de Engenharia Ambiental Sanitaria e Ocupacional"
                ),
                List.of("Ana Clara de Albuquerque Vasconcelos"),
                "Pneumoultramicroscopicossilicovulcanoconiotico: uma analise epidemiologica e socioambiental da exposicao ocupacional prolongada a particulas silicovulcanicas",
                Optional.of("Estudo interdisciplinar sobre saude coletiva, responsabilidade empresarial e prevencao em ambientes industriais"),
                List.of("Sao Jose do Vale do Rio Preto", "2026")
        );
    }

    private static CoverComponent manyAuthorsCover() {
        return new CoverComponent(
                List.of(
                        "Universidade Paulista",
                        "Curso de Sistemas de Informacao"
                ),
                List.of(
                        "Ana Clara de Albuquerque",
                        "Bruno Henrique Cavalcanti",
                        "Camila Aparecida Rodrigues",
                        "Diego Fernando Nascimento",
                        "Eduarda Cristina Menezes",
                        "Felipe Augusto Pereira",
                        "Gabriela Martins de Souza",
                        "Henrique Rafael Oliveira"
                ),
                "Titulo do Trabalho em Grupo",
                Optional.of("Relatorio academico desenvolvido por multiplos autores"),
                List.of("Limeira", "2026")
        );
    }

    private static CoverComponent longTitleManyAuthorsCover() {
        return new CoverComponent(
                List.of(
                        "Universidade da Integracao Internacional da Lusofonia Afro-Brasileira",
                        "Centro de Ciencias Exatas Ambientais Biologicas e Tecnologicas",
                        "Curso de Engenharia Ambiental Sanitaria Ocupacional e Previdenciaria"
                ),
                List.of(
                        "Ana Carolina de Albuquerque",
                        "Bruno Henrique Cavalcanti",
                        "Camila Aparecida Rodrigues",
                        "Fernando Augusto Pereira",
                        "Mariana Cristina Figueiredo"
                ),
                "Pneumoultramicroscopicossilicovulcanoconiotico: uma analise epidemiologica ocupacional e socioambiental",
                Optional.of("Estudo sobre exposicao prolongada a particulas silicovulcanicas e impactos em politicas de prevencao industrial"),
                List.of("Sao Jose do Vale do Rio Preto", "2026")
        );
    }

    private static CoverComponent limitCover() {
        return new CoverComponent(
                List.of(
                        "Universidade Paulista",
                        "Instituto de Ciencias Exatas Humanas Tecnologicas e Sociais",
                        "Curso de Engenharia Gestao Educacao e Tecnologias Aplicadas"
                ),
                List.of(
                        "Autor Teste Carga Linha 01",
                        "Autor Teste Carga Linha 02",
                        "Autor Teste Carga Linha 03",
                        "Autor Teste Carga Linha 04",
                        "Autor Teste Carga Linha 05",
                        "Autor Teste Carga Linha 06",
                        "Autor Teste Carga Linha 07",
                        "Autor Teste Carga Linha 08",
                        "Autor Teste Carga Linha 09",
                        "Autor Teste Carga Linha 10"
                ),
                "Pneumoultramicroscopicossilicovulcanoconiotico",
                Optional.of("Texto de carga para subtitulo extenso com varias palavras controladas para aproximar a capa do limite maximo geravel antes do overflow vertical"),
                List.of("Sao Jose do Vale do Rio Preto", "2026")
        );
    }

    private static CoverComponent overflowCover() {
        return new CoverComponent(
                List.of(
                        "Universidade Paulista",
                        "Instituto de Ciencias Exatas Humanas Tecnologicas e Sociais",
                        "Curso de Engenharia Gestao Educacao e Tecnologias Aplicadas"
                ),
                List.of(
                        "Autor Teste Overflow Vertical Linha 01",
                        "Autor Teste Overflow Vertical Linha 02",
                        "Autor Teste Overflow Vertical Linha 03",
                        "Autor Teste Overflow Vertical Linha 04",
                        "Autor Teste Overflow Vertical Linha 05",
                        "Autor Teste Overflow Vertical Linha 06",
                        "Autor Teste Overflow Vertical Linha 07",
                        "Autor Teste Overflow Vertical Linha 08",
                        "Autor Teste Overflow Vertical Linha 09",
                        "Autor Teste Overflow Vertical Linha 10",
                        "Autor Teste Overflow Vertical Linha 11",
                        "Autor Teste Overflow Vertical Linha 12",
                        "Autor Teste Overflow Vertical Linha 13",
                        "Autor Teste Overflow Vertical Linha 14",
                        "Autor Teste Overflow Vertical Linha 15",
                        "Autor Teste Overflow Vertical Linha 16",
                        "Autor Teste Overflow Vertical Linha 17",
                        "Autor Teste Overflow Vertical Linha 18",
                        "Autor Teste Overflow Vertical Linha 19",
                        "Autor Teste Overflow Vertical Linha 20",
                        "Autor Teste Overflow Vertical Linha 21",
                        "Autor Teste Overflow Vertical Linha 22",
                        "Autor Teste Overflow Vertical Linha 23",
                        "Autor Teste Overflow Vertical Linha 24",
                        "Autor Teste Overflow Vertical Linha 25",
                        "Autor Teste Overflow Vertical Linha 26",
                        "Autor Teste Overflow Vertical Linha 27",
                        "Autor Teste Overflow Vertical Linha 28"
                ),
                "Pneumoultramicroscopicossilicovulcanoconiotico: uma analise epidemiologica e socioambiental",
                Optional.of("Estudo sobre exposicao ocupacional prolongada a particulas silicovulcanicas em ambientes industriais de alta complexidade"),
                List.of("Sao Jose do Vale do Rio Preto", "2026")
        );
    }

    private static CoverComponent bottomWrapInvalidCover() {
        return new CoverComponent(
                List.of("Universidade Paulista"),
                List.of("Nome Completo do Aluno"),
                "Titulo do Trabalho",
                Optional.of("Subtitulo do trabalho"),
                List.of(
                        "Cidade Brasileira Com Nome Propositalmente Extenso Para Forcar Quebra Em Mais De Uma Linha",
                        "2026"
                )
        );
    }

    private static DocumentProfile validProfile() {
        return new DocumentProfile(
                "abnt-unip-profile",
                "ABNT UNIP Profile",
                validPageRule(),
                List.of(
                        style("cover.top", true, true),
                        style("cover.author", false, true),
                        style("cover.title", true, true),
                        style("cover.subtitle", false, false),
                        style("cover.bottom", false, true)
                ),
                List.of(new CoverComponentRule(
                        "cover",
                        new CoverStyleMapping(
                                "cover.top",
                                "cover.author",
                                "cover.title",
                                "cover.subtitle",
                                "cover.bottom"
                        ),
                        new CoverLayoutRule(
                                BigDecimal.valueOf(30),
                                BigDecimal.valueOf(10),
                                BigDecimal.valueOf(60)
                        )
                ))
        );
    }

    private static PageRule validPageRule() {
        return new PageRule(
                BigDecimal.valueOf(21),
                BigDecimal.valueOf(29.7),
                BigDecimal.valueOf(3),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(3),
                PageOrientation.PORTRAIT
        );
    }

    private static StyleRule style(String id, boolean bold, boolean uppercase) {
        return new StyleRule(
                id,
                StyleType.PARAGRAPH,
                "Times New Roman",
                BigDecimal.valueOf(12),
                TextAlignment.CENTER,
                BigDecimal.valueOf(1.5),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                bold,
                false,
                uppercase
        );
    }

    private record CoverSample(
            String sampleName,
            CoverComponent cover
    ) {
    }
}
