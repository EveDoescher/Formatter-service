package com.abntbuilder.formatter.rendering.component.flowtextual;

import com.abntbuilder.formatter.document.component.flowtextual.FlowTextualContent;
import com.abntbuilder.formatter.document.component.singlepage.TableValue;
import com.abntbuilder.formatter.document.component.singlepage.TextListValue;
import com.abntbuilder.formatter.document.component.singlepage.TextValue;
import com.abntbuilder.formatter.output.docx.api.DocxBlankLine;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxTableBlock;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.PageOrientation;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.StyleType;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.profile.model.component.flowtextual.FlowItem;
import com.abntbuilder.formatter.profile.model.component.flowtextual.FlowTextualComponentRule;
import com.abntbuilder.formatter.rendering.phase0.Phase0Index;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FlowTextualRendererTest {

    @Test
    void shouldRenderHeadingAndPlainText() {
        FlowTextualRenderer renderer = new FlowTextualRenderer("dedication");
        FlowTextualComponentRule rule = new FlowTextualComponentRule("dedication", List.of(
                new FlowItem.HeadingItem("s", "DEDICATÓRIA"),
                new FlowItem.PlainTextItem("s", "text")
        ));
        DocumentProfile profile = profileWith("dedication", rule);
        FlowTextualContent content = new FlowTextualContent("dedication",
                Map.of("text", new TextValue("Aos meus pais.")));

        List<DocxBlock> blocks = renderer.renderWithMetadata(content, profile, Phase0Index.empty());

        assertEquals(2, blocks.size());
        assertInstanceOf(DocxParagraph.class, blocks.get(0));
        assertInstanceOf(DocxParagraph.class, blocks.get(1));
        DocxParagraph heading = (DocxParagraph) blocks.get(0);
        assertEquals("DEDICATÓRIA", heading.runs().getFirst().text());
    }

    @Test
    void shouldRenderBlankLines() {
        FlowTextualRenderer renderer = new FlowTextualRenderer("dedication");
        FlowTextualComponentRule rule = new FlowTextualComponentRule("dedication", List.of(
                new FlowItem.BlankLinesItem("s", 3),
                new FlowItem.PlainTextItem("s", "text")
        ));
        DocumentProfile profile = profileWith("dedication", rule);
        FlowTextualContent content = new FlowTextualContent("dedication",
                Map.of("text", new TextValue("abc")));

        List<DocxBlock> blocks = renderer.renderWithMetadata(content, profile, Phase0Index.empty());

        assertEquals(4, blocks.size());
        assertInstanceOf(DocxBlankLine.class, blocks.get(0));
        assertInstanceOf(DocxBlankLine.class, blocks.get(1));
        assertInstanceOf(DocxBlankLine.class, blocks.get(2));
        assertInstanceOf(DocxParagraph.class, blocks.get(3));
    }

    @Test
    void shouldRenderTemplatedText() {
        FlowTextualRenderer renderer = new FlowTextualRenderer("epigraph");
        FlowTextualComponentRule rule = new FlowTextualComponentRule("epigraph", List.of(
                new FlowItem.PlainTextItem("s", "text"),
                new FlowItem.TemplatedTextItem("s", "— {author}, {source}", List.of("author", "source"))
        ));
        DocumentProfile profile = profileWith("epigraph", rule);
        FlowTextualContent content = new FlowTextualContent("epigraph", Map.of(
                "text", new TextValue("A jornada é a recompensa."),
                "author", new TextValue("Provérbio chinês"),
                "source", new TextValue("Atribuição popular")
        ));

        List<DocxBlock> blocks = renderer.renderWithMetadata(content, profile, Phase0Index.empty());

        assertEquals(2, blocks.size());
        DocxParagraph authorLine = (DocxParagraph) blocks.get(1);
        assertEquals("— Provérbio chinês, Atribuição popular", authorLine.runs().getFirst().text());
    }

    @Test
    void shouldRenderBoldLabeledKeywords() {
        FlowTextualRenderer renderer = new FlowTextualRenderer("resumo");
        FlowTextualComponentRule rule = new FlowTextualComponentRule("resumo", List.of(
                new FlowItem.HeadingItem("s", "RESUMO"),
                new FlowItem.PlainTextItem("s", "text"),
                new FlowItem.BoldLabeledKeywordsItem("s", "keywordsLabel", "keywords", "; ", ".")
        ));
        DocumentProfile profile = profileWith("resumo", rule);
        FlowTextualContent content = new FlowTextualContent("resumo", Map.of(
                "text", new TextValue("Este trabalho apresenta..."),
                "keywordsLabel", new TextValue("Palavras-chave:"),
                "keywords", new TextListValue(List.of("educação", "tecnologia", "inovação"))
        ));

        List<DocxBlock> blocks = renderer.renderWithMetadata(content, profile, Phase0Index.empty());

        assertEquals(3, blocks.size());
        DocxParagraph keywordsBlock = (DocxParagraph) blocks.get(2);
        assertEquals(2, keywordsBlock.runs().size());
        assertEquals("Palavras-chave:", keywordsBlock.runs().get(0).text());
        assertEquals(" educação; tecnologia; inovação.", keywordsBlock.runs().get(1).text());
    }

    @Test
    void shouldRenderPairList() {
        FlowTextualRenderer renderer = new FlowTextualRenderer("glossary");
        FlowTextualComponentRule rule = new FlowTextualComponentRule("glossary", List.of(
                new FlowItem.HeadingItem("s", "GLOSSÁRIO"),
                new FlowItem.PairListItem("s", "terms", "definitions", " — ")
        ));
        DocumentProfile profile = profileWith("glossary", rule);
        FlowTextualContent content = new FlowTextualContent("glossary", Map.of(
                "terms", new TextListValue(List.of("API", "SDK")),
                "definitions", new TextListValue(List.of("Interface de programação", "Kit de desenvolvimento"))
        ));

        List<DocxBlock> blocks = renderer.renderWithMetadata(content, profile, Phase0Index.empty());

        assertEquals(3, blocks.size());
        DocxParagraph entry1 = (DocxParagraph) blocks.get(1);
        assertEquals("API — Interface de programação", entry1.runs().getFirst().text());
    }

    @Test
    void shouldRejectMismatchedPairListSizes() {
        FlowTextualRenderer renderer = new FlowTextualRenderer("glossary");
        FlowTextualComponentRule rule = new FlowTextualComponentRule("glossary", List.of(
                new FlowItem.PairListItem("s", "terms", "definitions", " — ")
        ));
        DocumentProfile profile = profileWith("glossary", rule);
        FlowTextualContent content = new FlowTextualContent("glossary", Map.of(
                "terms", new TextListValue(List.of("A", "B")),
                "definitions", new TextListValue(List.of("only one"))
        ));

        assertThrows(IllegalArgumentException.class, () ->
                renderer.renderWithMetadata(content, profile, Phase0Index.empty()));
    }

    @Test
    void shouldRenderTableBlock() {
        FlowTextualRenderer renderer = new FlowTextualRenderer("errata");
        FlowTextualComponentRule rule = new FlowTextualComponentRule("errata", List.of(
                new FlowItem.HeadingItem("s", "ERRATA"),
                new FlowItem.TableBlockItem("s", "s",
                        List.of("Folha", "Linha", "Onde se lê", "Leia-se"), "rows")
        ));
        DocumentProfile profile = profileWith("errata", rule);
        FlowTextualContent content = new FlowTextualContent("errata", Map.of(
                "rows", new TableValue(List.of(
                        List.of("10", "3", "texto errado", "texto correto")
                ))
        ));

        List<DocxBlock> blocks = renderer.renderWithMetadata(content, profile, Phase0Index.empty());

        assertEquals(2, blocks.size());
        assertInstanceOf(DocxParagraph.class, blocks.get(0));
        assertInstanceOf(DocxTableBlock.class, blocks.get(1));
    }

    @Test
    void shouldRejectMissingTextSlot() {
        FlowTextualRenderer renderer = new FlowTextualRenderer("dedication");
        FlowTextualComponentRule rule = new FlowTextualComponentRule("dedication", List.of(
                new FlowItem.PlainTextItem("s", "text")
        ));
        DocumentProfile profile = profileWith("dedication", rule);
        FlowTextualContent content = new FlowTextualContent("dedication", Map.of());

        assertThrows(IllegalArgumentException.class, () ->
                renderer.renderWithMetadata(content, profile, Phase0Index.empty()));
    }

    @Test
    void componentIdIsExposed() {
        FlowTextualRenderer renderer = new FlowTextualRenderer("errata");
        assertEquals("errata", renderer.componentId());
        assertEquals(FlowTextualContent.class, renderer.componentType());
    }

    private static DocumentProfile profileWith(String componentId, FlowTextualComponentRule rule) {
        return new DocumentProfile(
                "test", "Test",
                pageRule(),
                List.of(style("s")),
                List.of(rule),
                List.of(componentId)
        );
    }

    private static StyleRule style(String id) {
        return new StyleRule(id, StyleType.PARAGRAPH, "Arial", BigDecimal.valueOf(12),
                TextAlignment.LEFT, BigDecimal.valueOf(1.5),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO,
                false, false, false);
    }

    private static PageRule pageRule() {
        return new PageRule(
                BigDecimal.valueOf(21), BigDecimal.valueOf(29.7),
                BigDecimal.valueOf(3), BigDecimal.valueOf(2),
                BigDecimal.valueOf(2), BigDecimal.valueOf(3),
                PageOrientation.PORTRAIT);
    }
}
