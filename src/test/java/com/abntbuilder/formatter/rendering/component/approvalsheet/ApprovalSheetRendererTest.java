package com.abntbuilder.formatter.rendering.component.approvalsheet;

import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalCommitteeMember;
import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalEvent;
import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalSheetComponent;
import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalSheetNature;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.resolution.ClasspathJsonProfileProvider;
import com.abntbuilder.formatter.rendering.layout.singlepage.HorizontalPlacementResolver;
import com.abntbuilder.formatter.rendering.layout.singlepage.MarginBasedSinglePageSafetyPolicy;
import com.abntbuilder.formatter.rendering.layout.singlepage.OrderedLayoutGapResolver;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageGapDistributor;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutEngine;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutLineMetrics;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutRenderer;
import com.abntbuilder.formatter.rendering.layout.text.FontMetricsTextMeasurer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApprovalSheetRendererTest {

    private final DocumentProfile profile = new ClasspathJsonProfileProvider().findById("abnt-unip-profile");
    private final ApprovalSheetRenderer renderer = approvalSheetRenderer();

    @Test
    void shouldRenderApprovalSheetComponentAsDocxBlocks() {
        List<DocxBlock> blocks = renderer.render(validApprovalSheet(), profile);

        List<String> paragraphTexts = paragraphs(blocks).stream()
                .map(DocxParagraph::text)
                .toList();
        String fullText = String.join(" ", paragraphTexts);

        assertTrue(paragraphTexts.contains("PESSOA AUTORA TESTE 01"));
        assertTrue(paragraphTexts.contains("TITULO DO TRABALHO"));
        assertTrue(fullText.contains("Trabalho de conclusao de curso"));
        assertTrue(paragraphTexts.contains("Aprovado(a) em: ______/______/______"));
        assertTrue(paragraphTexts.contains("BANCA EXAMINADORA"));
        assertTrue(paragraphTexts.contains("________________________________________"));
        assertTrue(paragraphTexts.contains("Prof. Dr. Pessoa Orientadora Teste"));
        assertTrue(paragraphTexts.contains("Universidade Paulista - UNIP"));
        assertTrue(paragraphTexts.contains("Orientador"));
        assertFalse(paragraphTexts.contains("________________________________________ Prof. Dr. Pessoa Orientadora Teste"));
        assertFalse(paragraphTexts.contains("Limeira"));
        assertFalse(fullText.contains("10 de junho de 2026"));
    }

    private static List<DocxParagraph> paragraphs(List<DocxBlock> blocks) {
        return blocks.stream()
                .filter(DocxParagraph.class::isInstance)
                .map(DocxParagraph.class::cast)
                .toList();
    }

    private static ApprovalSheetComponent validApprovalSheet() {
        return new ApprovalSheetComponent(
                List.of("Pessoa Autora Teste 01"),
                "Titulo do Trabalho",
                Optional.empty(),
                new ApprovalSheetNature(
                        "Trabalho de conclusao de curso",
                        "obtencao do titulo de graduacao",
                        "Analise e Desenvolvimento de Sistemas",
                        "Universidade Paulista - UNIP"
                ),
                Optional.of(new ApprovalEvent(
                        Optional.of("Limeira"),
                        Optional.of("10 de junho de 2026"),
                        Optional.empty()
                )),
                List.of(new ApprovalCommitteeMember(
                        "Pessoa Orientadora Teste",
                        Optional.of("Prof. Dr."),
                        Optional.of("Universidade Paulista - UNIP"),
                        Optional.of("Orientador")
                ))
        );
    }

    private static ApprovalSheetRenderer approvalSheetRenderer() {
        return new ApprovalSheetRenderer(
                new ApprovalSheetLayoutCalculator(
                        new ApprovalSheetLayoutAssembler(
                                new FontMetricsTextMeasurer(),
                                new OrderedLayoutGapResolver(),
                                new ApprovalSheetProfileContentValidator(),
                                new ApprovalSheetTextTemplateResolver(),
                                new HorizontalPlacementResolver()
                        ),
                        new SinglePageLayoutEngine(
                                new SinglePageLayoutLineMetrics(),
                                new MarginBasedSinglePageSafetyPolicy(),
                                new SinglePageGapDistributor()
                        )
                ),
                new SinglePageLayoutRenderer()
        );
    }
}
