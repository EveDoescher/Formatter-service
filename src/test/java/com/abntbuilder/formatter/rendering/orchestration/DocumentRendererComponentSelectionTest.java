package com.abntbuilder.formatter.rendering.orchestration;

import com.abntbuilder.formatter.application.export.ExportDocxCommand;
import com.abntbuilder.formatter.document.component.singlepage.SinglePageContent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxDocument;
import com.abntbuilder.formatter.output.docx.api.DocxPageBreak;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.output.docx.api.DocxSectionBreak;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.PageNumberingPlacement;
import com.abntbuilder.formatter.profile.model.PageNumberingRule;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void shouldStartPageNumberingSectionBeforeConfiguredComponent() {
        DocxDocument document = renderer.render(new ExportDocxCommand(
                "test.docx",
                profileWithPageNumberingFromParagraphs(),
                List.of(cover()),
                List.of("cover", "paragraphs"),
                List.of(new ExportDocxCommand.ParagraphCommand("Paragraph", "body"))
        ));

        assertEquals(List.of("COVER", "Paragraph"), paragraphTexts(document));
        assertEquals(1, document.blocks().stream().filter(DocxSectionBreak.class::isInstance).count());
        assertEquals(0, document.blocks().stream().filter(DocxPageBreak.class::isInstance).count());
        assertTrue(document.initialPageNumbering().orElseThrow().countingStarts());
        assertFalse(document.initialPageNumbering().orElseThrow().visible());
        DocxSectionBreak sectionBreak = document.blocks()
                .stream()
                .filter(DocxSectionBreak.class::isInstance)
                .map(DocxSectionBreak.class::cast)
                .findFirst()
                .orElseThrow();
        assertFalse(sectionBreak.pageNumbering().countingStarts());
        assertTrue(sectionBreak.pageNumbering().visible());
    }

    @Test
    void shouldUseInitialPageNumberingWhenConfiguredComponentIsFirst() {
        DocxDocument document = renderer.render(new ExportDocxCommand(
                "test.docx",
                profileWithPageNumberingAtParagraphs(),
                List.of(),
                List.of("paragraphs"),
                List.of(new ExportDocxCommand.ParagraphCommand("Paragraph", "body"))
        ));

        assertEquals(List.of("Paragraph"), paragraphTexts(document));
        assertEquals("body", document.initialPageNumbering().orElseThrow().styleRule().id());
        assertTrue(document.initialPageNumbering().orElseThrow().countingStarts());
        assertTrue(document.initialPageNumbering().orElseThrow().visible());
    }

    @Test
    void shouldRejectSelectedComponentsWhenPageCountingAnchorIsMissing() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> renderer.render(new ExportDocxCommand(
                        "test.docx",
                        profileWithPageNumberingFromTitlePageToParagraphs(),
                        List.of(),
                        List.of("paragraphs"),
                        List.of(new ExportDocxCommand.ParagraphCommand("Paragraph", "body"))
                ))
        );

        assertEquals(
                "selectedComponents must include pageNumbering.countFromComponentId: titlePage",
                exception.getMessage()
        );
    }

    @Test
    void shouldStartCountingAndOnlyShowNumberingAtConfiguredVisibilityStart() {
        DocxDocument document = renderer.render(new ExportDocxCommand(
                "test.docx",
                profileWithPageNumberingFromTitlePageToParagraphs(),
                List.of(cover(), titlePage()),
                List.of("cover", "titlePage", "paragraphs"),
                List.of(new ExportDocxCommand.ParagraphCommand("Paragraph", "body"))
        ));

        List<DocxSectionBreak> sectionBreaks = document.blocks()
                .stream()
                .filter(DocxSectionBreak.class::isInstance)
                .map(DocxSectionBreak.class::cast)
                .toList();

        assertEquals(2, sectionBreaks.size());
        assertTrue(sectionBreaks.get(0).pageNumbering().countingStarts());
        assertFalse(sectionBreaks.get(0).pageNumbering().visible());
        assertFalse(sectionBreaks.get(1).pageNumbering().countingStarts());
        assertTrue(sectionBreaks.get(1).pageNumbering().visible());
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
                .map(p -> p.runs().get(0).text())
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

    private static SinglePageContent cover() {
        return new SinglePageContent("cover", java.util.Map.of());
    }

    private static SinglePageContent titlePage() {
        return new SinglePageContent("titlePage", java.util.Map.of());
    }

    private static SinglePageContent approvalSheet() {
        return new SinglePageContent("approvalSheet", java.util.Map.of());
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

    private static DocumentProfile profileWithPageNumberingFromParagraphs() {
        return new DocumentProfile(
                "test-profile",
                "Test Profile",
                pageRule(),
                Optional.of(new PageNumberingRule(
                        true,
                        "cover",
                        "paragraphs",
                        "body",
                        PageNumberingPlacement.HEADER_RIGHT,
                        BigDecimal.valueOf(2),
                        BigDecimal.valueOf(2)
                )),
                List.of(style("body")),
                List.of(new FakeComponentRule("cover"), new FakeComponentRule("titlePage"), new FakeComponentRule("approvalSheet")),
                List.of("cover", "titlePage", "approvalSheet", "paragraphs")
        );
    }

    private static DocumentProfile profileWithPageNumberingFromTitlePageToParagraphs() {
        return new DocumentProfile(
                "test-profile",
                "Test Profile",
                pageRule(),
                Optional.of(new PageNumberingRule(
                        true,
                        "titlePage",
                        "paragraphs",
                        "body",
                        PageNumberingPlacement.HEADER_RIGHT,
                        BigDecimal.valueOf(2),
                        BigDecimal.valueOf(2)
                )),
                List.of(style("body")),
                List.of(new FakeComponentRule("cover"), new FakeComponentRule("titlePage"), new FakeComponentRule("approvalSheet")),
                List.of("cover", "titlePage", "approvalSheet", "paragraphs")
        );
    }

    private static DocumentProfile profileWithPageNumberingAtParagraphs() {
        return new DocumentProfile(
                "test-profile",
                "Test Profile",
                pageRule(),
                Optional.of(new PageNumberingRule(
                        true,
                        "paragraphs",
                        "paragraphs",
                        "body",
                        PageNumberingPlacement.HEADER_RIGHT,
                        BigDecimal.valueOf(2),
                        BigDecimal.valueOf(2)
                )),
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

    private static final class FakeCoverRenderer implements ComponentRenderer<SinglePageContent> {

        @Override
        public String componentId() {
            return "cover";
        }

        @Override
        public Class<SinglePageContent> componentType() {
            return SinglePageContent.class;
        }

        @Override
        public List<DocxBlock> render(SinglePageContent component, DocumentProfile profile) {
            StyleRule s = style("body"); return List.of(new DocxParagraph(List.of(DocxRun.of("COVER", s)), s));
        }
    }

    private static final class FakeTitlePageRenderer implements ComponentRenderer<SinglePageContent> {

        @Override
        public String componentId() {
            return "titlePage";
        }

        @Override
        public Class<SinglePageContent> componentType() {
            return SinglePageContent.class;
        }

        @Override
        public List<DocxBlock> render(SinglePageContent component, DocumentProfile profile) {
            StyleRule s = style("body"); return List.of(new DocxParagraph(List.of(DocxRun.of("TITLE_PAGE", s)), s));
        }
    }

    private static final class FakeApprovalSheetRenderer implements ComponentRenderer<SinglePageContent> {

        @Override
        public String componentId() {
            return "approvalSheet";
        }

        @Override
        public Class<SinglePageContent> componentType() {
            return SinglePageContent.class;
        }

        @Override
        public List<DocxBlock> render(SinglePageContent component, DocumentProfile profile) {
            StyleRule s = style("body"); return List.of(new DocxParagraph(List.of(DocxRun.of("APPROVAL_SHEET", s)), s));
        }
    }

    private record FakeComponentRule(String componentId) implements ComponentRule {
    }
}
