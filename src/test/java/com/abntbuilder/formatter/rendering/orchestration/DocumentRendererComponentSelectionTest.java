package com.abntbuilder.formatter.rendering.orchestration;

import com.abntbuilder.formatter.application.export.ExportDocxCommand;
import com.abntbuilder.formatter.engine.model.content.singlepage.SinglePageContent;
import com.abntbuilder.formatter.engine.model.content.singlepage.TextValue;
import com.abntbuilder.formatter.engine.model.output.DocxBlock;
import com.abntbuilder.formatter.engine.model.output.DocxDocument;
import com.abntbuilder.formatter.engine.model.output.DocxPageBreak;
import com.abntbuilder.formatter.engine.model.output.DocxParagraph;
import com.abntbuilder.formatter.engine.model.output.DocxSectionBreak;
import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;
import com.abntbuilder.formatter.engine.model.profile.PageNumberingPlacement;
import com.abntbuilder.formatter.engine.model.profile.PageNumberingRule;
import com.abntbuilder.formatter.engine.model.profile.PageOrientation;
import com.abntbuilder.formatter.engine.model.profile.PageRule;
import com.abntbuilder.formatter.engine.model.profile.StyleRule;
import com.abntbuilder.formatter.engine.model.profile.StyleType;
import com.abntbuilder.formatter.engine.model.profile.TextAlignment;
import com.abntbuilder.formatter.engine.model.profile.component.singlepage.SinglePageComponentRule;
import com.abntbuilder.formatter.engine.model.profile.component.singlepage.TextSlotRule;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.SinglePageGroupRule;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.SinglePageItemRule;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.SinglePageLayoutPolicy;
import com.abntbuilder.formatter.engine.model.profile.layout.singlepage.SinglePageLayoutRule;
import com.abntbuilder.formatter.rendering.singlepage.MarginBasedSinglePageSafetyPolicy;
import com.abntbuilder.formatter.rendering.singlepage.OrderedLayoutGapResolver;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageContentValidator;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageGapDistributor;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageLayoutAssembler;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageLayoutCalculator;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageLayoutEngine;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageLayoutLineMetrics;
import com.abntbuilder.formatter.rendering.singlepage.SinglePageLayoutRenderer;
import com.abntbuilder.formatter.rendering.text.FontMetricsTextMeasurer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentRendererComponentSelectionTest {

    private final DocumentRenderer renderer = new DocumentRenderer(
            new ComponentSelectionResolver(),
            singlePageLayoutCalculator(),
            new SinglePageLayoutRenderer()
    );

    @Test
    void shouldRenderAllPresentContentWhenSelectionIsAbsent() {
        DocxDocument document = renderer.render(command(List.of()));

        assertEquals(List.of("COVER", "TITLE_PAGE", "Paragraph"), paragraphTexts(document));
        assertEquals(2, document.blocks().stream()
                .filter(b -> b instanceof DocxPageBreak || b instanceof DocxSectionBreak)
                .count());
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

        assertEquals(List.of("COVER"), paragraphTexts(document));
    }

    @Test
    void shouldRenderOnlySelectedParagraphs() {
        DocxDocument document = renderer.render(command(List.of("paragraphs")));

        assertEquals(List.of("Paragraph"), paragraphTexts(document));
    }

    @Test
    void shouldRenderOnlySelectedTitlePage() {
        DocxDocument document = renderer.render(command(List.of("titlePage")));

        assertEquals(List.of("TITLE_PAGE"), paragraphTexts(document));
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
                () -> renderer.render(command(List.of("unknownComponent")))
        );

        assertEquals("Unsupported selected component: unknownComponent", exception.getMessage());
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

    // --- Fixtures ---

    private static SinglePageLayoutCalculator singlePageLayoutCalculator() {
        return new SinglePageLayoutCalculator(
                new SinglePageContentValidator(),
                new SinglePageLayoutAssembler(
                        new FontMetricsTextMeasurer(),
                        new OrderedLayoutGapResolver()
                ),
                new SinglePageLayoutEngine(
                        new SinglePageLayoutLineMetrics(),
                        new MarginBasedSinglePageSafetyPolicy(),
                        new SinglePageGapDistributor()
                )
        );
    }

    private static SinglePageComponentRule componentRule(String componentId) {
        return new SinglePageComponentRule(
                componentId,
                true,
                null,
                Map.of("label", new TextSlotRule(true, null, null)),
                Map.of("label", "sp.body"),
                new SinglePageLayoutRule(
                        List.of(new SinglePageGroupRule("top", true, List.of(
                                new SinglePageItemRule("label", true, Optional.empty())
                        ))),
                        List.of(),
                        SinglePageLayoutPolicy.defaultSinglePagePolicy()
                )
        );
    }

    private static SinglePageContent cover() {
        return new SinglePageContent("cover", Map.of("label", new TextValue("COVER")));
    }

    private static SinglePageContent titlePage() {
        return new SinglePageContent("titlePage", Map.of("label", new TextValue("TITLE_PAGE")));
    }

    private static SinglePageContent approvalSheet() {
        return new SinglePageContent("approvalSheet", Map.of("label", new TextValue("APPROVAL_SHEET")));
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

    private static DocumentProfile profile() {
        return new DocumentProfile(
                "test-profile",
                "Test Profile",
                pageRule(),
                List.of(style("sp.body"), style("body")),
                List.of(componentRule("cover"), componentRule("titlePage"), componentRule("approvalSheet")),
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
                List.of(style("sp.body"), style("body")),
                List.of(componentRule("cover"), componentRule("titlePage"), componentRule("approvalSheet")),
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
                List.of(style("sp.body"), style("body")),
                List.of(componentRule("cover"), componentRule("titlePage"), componentRule("approvalSheet")),
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
                List.of(style("sp.body"), style("body")),
                List.of(componentRule("cover"), componentRule("titlePage"), componentRule("approvalSheet")),
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
}
