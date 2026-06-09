package com.abntbuilder.formatter.rendering.document;

import com.abntbuilder.formatter.application.export.ExportDocxCommand;
import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalSheetComponent;
import com.abntbuilder.formatter.document.component.approvalsheet.ApprovalSheetNature;
import com.abntbuilder.formatter.document.component.cover.CoverComponent;
import com.abntbuilder.formatter.document.component.titlepage.TitlePageComponent;
import com.abntbuilder.formatter.document.component.titlepage.TitlePageNature;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxDocument;
import com.abntbuilder.formatter.output.docx.api.DocxPageBreak;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.PageOrientation;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.StyleType;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;
import com.abntbuilder.formatter.rendering.component.ComponentRendererRegistry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DocumentRendererComponentSelectionTest {

    private final DocumentRenderer renderer = new DocumentRenderer(
            new ComponentRendererRegistry(List.of(
                    new FakeCoverRenderer(),
                    new FakeTitlePageRenderer(),
                    new FakeApprovalSheetRenderer()
            )),
            new ComponentSelectionResolver()
    );

    @Test
    void shouldRenderAllPresentContentWhenSelectionIsAbsent() {
        DocxDocument document = renderer.render(command(List.of()));

        assertEquals(5, document.blocks().size());
        assertEquals(List.of("COVER", "TITLE_PAGE", "Paragraph"), paragraphTexts(document));
        assertEquals(2, document.blocks().stream().filter(DocxPageBreak.class::isInstance).count());
    }

    @Test
    void shouldRenderSelectedCoverAndTitlePageInDocumentOrder() {
        DocxDocument document = renderer.render(command(List.of("titlePage", "cover")));

        assertEquals(List.of("COVER", "TITLE_PAGE"), paragraphTexts(document));
        assertEquals(1, document.blocks().stream().filter(DocxPageBreak.class::isInstance).count());
    }

    @Test
    void shouldRenderOnlySelectedCover() {
        DocxDocument document = renderer.render(command(List.of("cover")));

        List<String> paragraphTexts = paragraphTexts(document);

        assertEquals(List.of("COVER"), paragraphTexts);
    }

    @Test
    void shouldRenderOnlySelectedParagraphs() {
        DocxDocument document = renderer.render(command(List.of("paragraphs")));

        List<String> paragraphTexts = paragraphTexts(document);

        assertEquals(List.of("Paragraph"), paragraphTexts);
    }

    @Test
    void shouldRenderOnlySelectedTitlePage() {
        DocxDocument document = renderer.render(command(List.of("titlePage")));

        List<String> paragraphTexts = paragraphTexts(document);

        assertEquals(List.of("TITLE_PAGE"), paragraphTexts);
    }

    @Test
    void shouldRenderSelectedApprovalSheetThroughRegistry() {
        DocxDocument document = renderer.render(new ExportDocxCommand(
                "test.docx",
                profile(),
                List.of(approvalSheet()),
                List.of("approvalSheet"),
                List.of()
        ));

        assertEquals(List.of("APPROVAL_SHEET"), paragraphTexts(document));
    }

    @Test
    void shouldRejectUnsupportedSelectedComponent() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> renderer.render(command(List.of("abstract")))
        );

        assertEquals("Unsupported selected component: abstract", exception.getMessage());
    }

    @Test
    void shouldRejectSelectedComponentWithoutContent() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> renderer.render(new ExportDocxCommand(
                        "test.docx",
                        profile(),
                        List.of(cover()),
                        List.of("titlePage"),
                        List.of(new ExportDocxCommand.ParagraphCommand("Paragraph", "body"))
                ))
        );

        assertEquals("selected component has no content: titlePage", exception.getMessage());
    }

    private static List<String> paragraphTexts(DocxDocument document) {
        return document.blocks()
                .stream()
                .filter(DocxParagraph.class::isInstance)
                .map(DocxParagraph.class::cast)
                .map(DocxParagraph::text)
                .toList();
    }

    private static ExportDocxCommand command(List<String> selectedComponents) {
        return new ExportDocxCommand(
                "test.docx",
                profile(),
                List.of(cover(), titlePage()),
                selectedComponents,
                List.of(new ExportDocxCommand.ParagraphCommand("Paragraph", "body"))
        );
    }

    private static CoverComponent cover() {
        return new CoverComponent(
                List.of("Universidade"),
                List.of("Autor"),
                "Titulo",
                Optional.empty(),
                "Limeira",
                "2026"
        );
    }

    private static TitlePageComponent titlePage() {
        return new TitlePageComponent(
                List.of("Autor"),
                "Titulo",
                Optional.empty(),
                new TitlePageNature(
                        "Trabalho de conclusao de curso",
                        "obtencao do titulo de graduacao",
                        "Curso",
                        "Universidade"
                ),
                Optional.empty(),
                Optional.empty(),
                "Limeira",
                "2026"
        );
    }

    private static DocumentProfile profile() {
        return new DocumentProfile(
                "test-profile",
                "Test Profile",
                pageRule(),
                List.of(style("body")),
                List.of(new FakeComponentRule("cover"), new FakeComponentRule("titlePage"), new FakeComponentRule("approvalSheet")),
                List.of("cover", "titlePage", "approvalSheet", "paragraphs")
        );
    }

    private static PageRule pageRule() {
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

    private static StyleRule style(String id) {
        return new StyleRule(
                id,
                StyleType.PARAGRAPH,
                "Times New Roman",
                BigDecimal.valueOf(12),
                TextAlignment.LEFT,
                BigDecimal.valueOf(1.5),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                false,
                false
        );
    }

    private static ApprovalSheetComponent approvalSheet() {
        return new ApprovalSheetComponent(
                List.of("Autor"),
                "Titulo",
                Optional.empty(),
                new ApprovalSheetNature(
                        "Trabalho academico",
                        "avaliacao parcial",
                        "Curso",
                        "Universidade"
                ),
                Optional.empty(),
                List.of()
        );
    }

    private static final class FakeCoverRenderer implements ComponentRenderer<CoverComponent> {

        @Override
        public String componentId() {
            return "cover";
        }

        @Override
        public Class<CoverComponent> componentType() {
            return CoverComponent.class;
        }

        @Override
        public List<DocxBlock> render(CoverComponent component, DocumentProfile profile) {
            return List.of(new DocxParagraph("COVER", style("body")));
        }
    }

    private static final class FakeTitlePageRenderer implements ComponentRenderer<TitlePageComponent> {

        @Override
        public String componentId() {
            return "titlePage";
        }

        @Override
        public Class<TitlePageComponent> componentType() {
            return TitlePageComponent.class;
        }

        @Override
        public List<DocxBlock> render(TitlePageComponent component, DocumentProfile profile) {
            return List.of(new DocxParagraph("TITLE_PAGE", style("body")));
        }
    }

    private static final class FakeApprovalSheetRenderer implements ComponentRenderer<ApprovalSheetComponent> {

        @Override
        public String componentId() {
            return "approvalSheet";
        }

        @Override
        public Class<ApprovalSheetComponent> componentType() {
            return ApprovalSheetComponent.class;
        }

        @Override
        public List<DocxBlock> render(ApprovalSheetComponent component, DocumentProfile profile) {
            return List.of(new DocxParagraph("APPROVAL_SHEET", style("body")));
        }
    }

    private record FakeComponentRule(String componentId) implements ComponentRule {
    }
}
