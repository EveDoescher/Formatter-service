# bodyContent Fase 2 — Blocos e Inline Especializados

> **Pré-requisito:** Fase 1 concluída (289 testes verdes, branch `feature/elementos-textuais`).
>
> **For agentic workers:** Use superpowers:subagent-driven-development ou superpowers:executing-plans. Steps usam `- [ ]` para tracking.

**Goal:** Completar todos os tipos de bloco e inline do `bodyContent`: superscript/subscript, listas, quadro, code listing, chart, equações, siglas, notas de rodapé, citação verbal e marcadores de citação (supressão, interpolação, ênfase).

**Tech Stack:** Java 21, Spring Boot, docx4j 11.5.13, JUnit 5

---

## Mapa de arquivos

### Novos (domínio)
- `BodyList.java`, `BodyListItem.java`, `BodyListType.java`
- `BodyFrame.java`
- `BodyCodeListing.java`
- `BodyChart.java`
- `BodyEquation.java`
- `BodyAbbreviation.java`
- `BodyFootnote.java`
- `BodyQuoteMarker.java`, `BodyQuoteMarkerType.java`

### Novos (perfil)
- `FrameRule.java`, `CodeListingRule.java`, `ChartRule.java`

### Novos (output)
- `DocxListItemParagraph.java`

### Modificados (domínio)
- `BodyBlock.java` — adiciona `BodyList`, `BodyEquation`
- `BodyInline.java` — adiciona `BodyAbbreviation`, `BodyFootnote`
- `BodyCitationType.java` — adiciona `VERBAL`
- `NumberedDisplayObject.java` — adiciona `BodyFrame`, `BodyCodeListing`, `BodyChart`
- `InlineFormatting.java` — adiciona `superscript`, `subscript`
- `BodyQuoteText.java` — adiciona `List<BodyQuoteMarker> markers`
- `BodyContentComponent.java` — valida IDs únicos dos novos display objects
- `CitationFormattingRule.java` — adiciona `suppressionMarker`, `emphasisOursLabel`, `emphasisAuthorLabel`

### Modificados (perfil)
- `BodyContentComponentRule.java` — adiciona `frameRule`, `codeListingRule`, `chartRule`
- `BodyContentStyleMapping.java` — adiciona `listOrderedStyleId`, `listUnorderedStyleId`, `equationStyleId`, `abbreviationStyleId`, `footnoteCallStyleId`, `footnoteTextStyleId`

### Modificados (requests)
- `BodyBlockType.java` — adiciona `ORDERED_LIST`, `UNORDERED_LIST`, `FRAME`, `CODE_LISTING`, `CHART`, `EQUATION`
- `BodyBlockRequest.java` — novos casos
- `BodyInlineType.java` — adiciona `ABBREVIATION`, `FOOTNOTE`
- `BodyInlineRequest.java` — novos casos
- `InlineFormattingRequest.java` — adiciona `superscript`, `subscript`
- `BodyContentComponentRuleRequest.java` — novos campos
- `DocxBlock.java` — adiciona `DocxListItemParagraph`
- `Docx4jWriter.java` — suporte a listas, superscript/subscript, footnotes, bordas fechadas
- `abnt-unip-profile.json` — estilos e regras dos novos elementos

---

## Task 1 — Superscript e Subscript em `InlineFormatting`

**Files:**
- Modify: `src/main/java/com/abntbuilder/formatter/document/component/bodycontent/InlineFormatting.java`
- Modify: `src/main/java/com/abntbuilder/formatter/api/export/dto/request/InlineFormattingRequest.java`
- Modify: `src/main/java/com/abntbuilder/formatter/output/docx/docx4j/Docx4jWriter.java`

- [ ] **Step 1: Atualizar `InlineFormatting`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/bodycontent/InlineFormatting.java
public record InlineFormatting(
        Optional<Boolean> bold,
        Optional<Boolean> italic,
        Optional<Boolean> underline,
        Optional<Boolean> superscript,
        Optional<Boolean> subscript
) {

    public InlineFormatting {
        Objects.requireNonNull(bold, "bold must not be null");
        Objects.requireNonNull(italic, "italic must not be null");
        Objects.requireNonNull(underline, "underline must not be null");
        Objects.requireNonNull(superscript, "superscript must not be null");
        Objects.requireNonNull(subscript, "subscript must not be null");
        if (superscript.orElse(false) && subscript.orElse(false)) {
            throw new IllegalArgumentException("superscript and subscript cannot both be true.");
        }
    }

    public static InlineFormatting none() {
        return new InlineFormatting(
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty()
        );
    }
}
```

- [ ] **Step 2: Atualizar `InlineFormattingRequest`**

```java
public record InlineFormattingRequest(
        Boolean bold,
        Boolean italic,
        Boolean underline,
        Boolean superscript,
        Boolean subscript
) {
    public InlineFormatting toDomain() {
        return new InlineFormatting(
                Optional.ofNullable(bold),
                Optional.ofNullable(italic),
                Optional.ofNullable(underline),
                Optional.ofNullable(superscript),
                Optional.ofNullable(subscript)
        );
    }
}
```

- [ ] **Step 3: Atualizar `Docx4jWriter.buildRunProperties` para emitir `<w:vertAlign>`**

Localizar `buildRunProperties` e adicionar após os blocos de bold/italic/underline:

```java
formatting.superscript().ifPresent(sup -> {
    if (sup) {
        CTVerticalAlignRun vertAlign = objectFactory.createCTVerticalAlignRun();
        vertAlign.setVal(STVerticalAlignRun.SUPERSCRIPT);
        rPr.setVertAlign(vertAlign);
    }
});
formatting.subscript().ifPresent(sub -> {
    if (sub) {
        CTVerticalAlignRun vertAlign = objectFactory.createCTVerticalAlignRun();
        vertAlign.setVal(STVerticalAlignRun.SUBSCRIPT);
        rPr.setVertAlign(vertAlign);
    }
});
```

Imports: `org.docx4j.wml.CTVerticalAlignRun`, `org.docx4j.wml.STVerticalAlignRun`

- [ ] **Step 4: Compilar e rodar testes existentes**

```bash
cd /mnt/c/Users/evelynnd/Documents/Projetos/Formatter-service
mvn test -pl . -Dtest=InlineFormattingTest -q
mvn compile -q
```

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/abntbuilder/formatter/document/component/bodycontent/InlineFormatting.java \
        src/main/java/com/abntbuilder/formatter/api/export/dto/request/InlineFormattingRequest.java \
        src/main/java/com/abntbuilder/formatter/output/docx/docx4j/Docx4jWriter.java
git commit -m "feat: add superscript and subscript to InlineFormatting"
```

---

## Task 2 — Listas: domínio

**Files:**
- Create: `BodyListType.java`, `BodyListItem.java`, `BodyList.java`
- Modify: `BodyBlock.java`, `BodyInline.java` (BodyListItem.content usa BodyInline)

- [ ] **Step 1: Criar `BodyListType`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyListType.java
package com.abntbuilder.formatter.document.component.bodycontent;

public enum BodyListType {
    ORDERED,
    UNORDERED
}
```

- [ ] **Step 2: Escrever teste para `BodyListItem`**

```java
// src/test/java/com/abntbuilder/formatter/document/component/bodycontent/BodyListItemTest.java
package com.abntbuilder.formatter.document.component.bodycontent;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class BodyListItemTest {

    @Test
    void shouldCreateWithContent() {
        BodyListItem item = new BodyListItem(List.of(new BodyText("texto")));
        assertThat(item.content()).hasSize(1);
    }

    @Test
    void shouldRejectEmptyContent() {
        assertThatThrownBy(() -> new BodyListItem(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNullContent() {
        assertThatThrownBy(() -> new BodyListItem(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
```

- [ ] **Step 3: Criar `BodyListItem`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyListItem.java
package com.abntbuilder.formatter.document.component.bodycontent;

import java.util.List;
import java.util.Objects;

public record BodyListItem(List<BodyInline> content) {

    public BodyListItem {
        Objects.requireNonNull(content, "content must not be null");
        if (content.isEmpty()) {
            throw new IllegalArgumentException("content must not be empty.");
        }
        content = List.copyOf(content);
    }
}
```

- [ ] **Step 4: Escrever teste para `BodyList`**

```java
// src/test/java/com/abntbuilder/formatter/document/component/bodycontent/BodyListTest.java
package com.abntbuilder.formatter.document.component.bodycontent;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class BodyListTest {

    private static BodyListItem item(String text) {
        return new BodyListItem(List.of(new BodyText(text)));
    }

    @Test
    void shouldCreateOrderedList() {
        BodyList list = new BodyList(BodyListType.ORDERED, List.of(item("a"), item("b")));
        assertThat(list.type()).isEqualTo(BodyListType.ORDERED);
        assertThat(list.items()).hasSize(2);
    }

    @Test
    void shouldRejectNullType() {
        assertThatThrownBy(() -> new BodyList(null, List.of(item("a"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectEmptyItems() {
        assertThatThrownBy(() -> new BodyList(BodyListType.ORDERED, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
```

- [ ] **Step 5: Criar `BodyList`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyList.java
package com.abntbuilder.formatter.document.component.bodycontent;

import java.util.List;
import java.util.Objects;

public record BodyList(
        BodyListType type,
        List<BodyListItem> items
) implements BodyBlock {

    public BodyList {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(items, "items must not be null");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty.");
        }
        items = List.copyOf(items);
    }
}
```

- [ ] **Step 6: Atualizar `BodyBlock`**

```java
public sealed interface BodyBlock
    permits BodyParagraph, BodyLongQuote, BodyList, BodyEquation, NumberedDisplayObject {
}
```

> Nota: `BodyEquation` é adicionado aqui também para compilar depois da Task 6. Se preferir compilação incremental, adicionar só `BodyList` agora e `BodyEquation` na Task 6.

- [ ] **Step 7: Rodar testes e compilar**

```bash
mvn test -pl . -Dtest="BodyListTest,BodyListItemTest" -q
mvn compile -q
```

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyListType.java \
        src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyListItem.java \
        src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyList.java \
        src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyBlock.java \
        src/test/java/com/abntbuilder/formatter/document/component/bodycontent/BodyListTest.java \
        src/test/java/com/abntbuilder/formatter/document/component/bodycontent/BodyListItemTest.java
git commit -m "feat: add BodyList and BodyListItem domain types"
```

---

## Task 3 — Listas: output DOCX e writer

**⚠️ Nota arquitetural importante antes de implementar:**
`Docx4jWriter` é um `@Bean` singleton (ver `RenderingConfig`). Ele **não pode ter campos de instância mutáveis** — seria race condition em requests concorrentes. Além disso, o writer não usa variável `body` — usa `wordPackage.getMainDocumentPart().addObject(...)`. O padrão correto é: criar os `numId`s **dentro de `write()`**, detectar antes do loop se algum `DocxListItemParagraph` existe, criar os ids uma vez, e passá-los por parâmetro para `writeBlock`.

**Files:**
- Create: `DocxListItemParagraph.java`
- Modify: `DocxBlock.java`
- Modify: `Docx4jWriter.java`

- [ ] **Step 1: Criar `DocxListItemParagraph`**

```java
// src/main/java/com/abntbuilder/formatter/output/docx/api/DocxListItemParagraph.java
package com.abntbuilder.formatter.output.docx.api;

import com.abntbuilder.formatter.document.component.bodycontent.BodyListType;
import com.abntbuilder.formatter.profile.model.StyleRule;

import java.util.List;
import java.util.Objects;

public record DocxListItemParagraph(
        List<DocxRun> runs,
        StyleRule styleRule,
        BodyListType listType,
        int listLevel
) implements DocxBlock {

    public DocxListItemParagraph {
        Objects.requireNonNull(runs, "runs must not be null");
        if (runs.isEmpty()) {
            throw new IllegalArgumentException("runs must not be empty.");
        }
        runs = List.copyOf(runs);
        Objects.requireNonNull(styleRule, "styleRule must not be null");
        Objects.requireNonNull(listType, "listType must not be null");
        if (listLevel < 0) {
            throw new IllegalArgumentException("listLevel must be >= 0.");
        }
    }
}
```

- [ ] **Step 2: Atualizar `DocxBlock`**

```java
public sealed interface DocxBlock
    permits DocxParagraph, DocxPageBreak, DocxBlankLine, DocxSectionBreak,
            DocxImageBlock, DocxTableBlock, DocxListItemParagraph {
}
```

- [ ] **Step 3: Adicionar suporte a listas no `Docx4jWriter`**

**Não adicionar campos de instância.** Em vez disso:

1. Criar um record local `ListNumIds(int orderedNumId, int unorderedNumId)` privado no writer.

2. No método `write(DocxDocument document)`, antes do loop, verificar se há algum `DocxListItemParagraph` nos blocos:

```java
// dentro de write(), antes do for (DocxBlock block : document.blocks())
boolean hasLists = document.blocks().stream()
        .anyMatch(b -> b instanceof DocxListItemParagraph);
ListNumIds listNumIds = hasLists ? createListNumIds(wordPackage) : null;
```

3. Criar o método `createListNumIds` que constrói os `abstractNum` e `num` e retorna o record:

```java
private ListNumIds createListNumIds(WordprocessingMLPackage wordPackage) {
    NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
    try {
        wordPackage.getMainDocumentPart().addTargetPart(ndp);
    } catch (Exception e) {
        throw new RuntimeException("Failed to create NumberingDefinitionsPart", e);
    }
    Numbering numbering = objectFactory.createNumbering();
    ndp.setJaxbElement(numbering);

    // AbstractNum ordered (decimal)
    Numbering.AbstractNum abstractOrdered = objectFactory.createNumberingAbstractNum();
    abstractOrdered.setAbstractNumId(BigInteger.ZERO);
    Lvl lvlOrdered = objectFactory.createLvl();
    lvlOrdered.setIlvl(BigInteger.ZERO);
    Lvl.NumFmt fmtOrdered = objectFactory.createLvlNumFmt();
    fmtOrdered.setVal(NumberFormat.DECIMAL);
    lvlOrdered.setNumFmt(fmtOrdered);
    Lvl.LvlText lvlTextOrdered = objectFactory.createLvlLvlText();
    lvlTextOrdered.setValue("%1.");
    lvlOrdered.setLvlText(lvlTextOrdered);
    Lvl.Start startOrdered = objectFactory.createLvlStart();
    startOrdered.setVal(BigInteger.ONE);
    lvlOrdered.setStart(startOrdered);
    abstractOrdered.getLvl().add(lvlOrdered);
    numbering.getAbstractNum().add(abstractOrdered);

    // AbstractNum unordered (bullet)
    Numbering.AbstractNum abstractUnordered = objectFactory.createNumberingAbstractNum();
    abstractUnordered.setAbstractNumId(BigInteger.ONE);
    Lvl lvlUnordered = objectFactory.createLvl();
    lvlUnordered.setIlvl(BigInteger.ZERO);
    Lvl.NumFmt fmtUnordered = objectFactory.createLvlNumFmt();
    fmtUnordered.setVal(NumberFormat.BULLET);
    lvlUnordered.setNumFmt(fmtUnordered);
    Lvl.LvlText lvlTextUnordered = objectFactory.createLvlLvlText();
    lvlTextUnordered.setValue("•");
    lvlUnordered.setLvlText(lvlTextUnordered);
    abstractUnordered.getLvl().add(lvlUnordered);
    numbering.getAbstractNum().add(abstractUnordered);

    // Num ordered — numId 1
    Numbering.Num numOrdered = objectFactory.createNumberingNum();
    numOrdered.setNumId(BigInteger.ONE);
    Numbering.Num.AbstractNumId aoidOrdered = objectFactory.createNumberingNumAbstractNumId();
    aoidOrdered.setVal(BigInteger.ZERO);
    numOrdered.setAbstractNumId(aoidOrdered);
    numbering.getNum().add(numOrdered);

    // Num unordered — numId 2
    Numbering.Num numUnordered = objectFactory.createNumberingNum();
    numUnordered.setNumId(BigInteger.TWO);
    Numbering.Num.AbstractNumId aoidUnordered = objectFactory.createNumberingNumAbstractNumId();
    aoidUnordered.setVal(BigInteger.ONE);
    numUnordered.setAbstractNumId(aoidUnordered);
    numbering.getNum().add(numUnordered);

    return new ListNumIds(1, 2);
}

private record ListNumIds(int orderedNumId, int unorderedNumId) {}
```

4. Atualizar o loop em `write()` para passar `listNumIds`. Localizar o loop `for (DocxBlock block : document.blocks())` e substituir:

```java
// Antes (linha ~88):
writeBlock(wordPackage, block);

// Depois:
writeBlock(wordPackage, block, listNumIds);
```

5. Atualizar `writeBlock` para aceitar `ListNumIds` como parâmetro adicional:

```java
private void writeBlock(WordprocessingMLPackage wordPackage, DocxBlock block, ListNumIds listNumIds) {
    switch (block) {
        case DocxParagraph paragraph -> writeParagraph(wordPackage, paragraph);
        case DocxPageBreak ignored -> writePageBreak(wordPackage);
        case DocxBlankLine blankLine -> writeBlankLine(wordPackage, blankLine);
        case DocxImageBlock imageBlock -> writeImage(wordPackage, imageBlock);
        case DocxTableBlock tableBlock -> writeTable(wordPackage, tableBlock);
        case DocxListItemParagraph listItem -> writeListItem(wordPackage, listItem, listNumIds);
        case DocxSectionBreak ignored -> throw new IllegalArgumentException(
                "Section breaks must be handled by the document section state."
        );
    }
}
```

5. Implementar `writeListItem`:

```java
private void writeListItem(
        WordprocessingMLPackage wordPackage,
        DocxListItemParagraph listItem,
        ListNumIds listNumIds
) {
    P p = objectFactory.createP();
    PPr pPr = createParagraphProperties(
            listItem.styleRule(), Optional.empty(), Optional.empty(), Optional.empty()
    );
    PPrBase.NumPr numPr = objectFactory.createPPrBaseNumPr();
    PPrBase.NumPr.Ilvl ilvl = objectFactory.createPPrBaseNumPrIlvl();
    ilvl.setVal(BigInteger.valueOf(listItem.listLevel()));
    numPr.setIlvl(ilvl);
    PPrBase.NumPr.NumId numId = objectFactory.createPPrBaseNumPrNumId();
    int id = listItem.listType() == BodyListType.ORDERED
            ? listNumIds.orderedNumId()
            : listNumIds.unorderedNumId();
    numId.setVal(BigInteger.valueOf(id));
    numPr.setNumId(numId);
    pPr.setNumPr(numPr);
    p.setPPr(pPr);
    for (DocxRun run : listItem.runs()) {
        R r = objectFactory.createR();
        r.setRPr(buildRunProperties(run.baseStyle(), run.formatting()));
        Text t = objectFactory.createText();
        t.setValue(resolveText(run.text(), run.baseStyle()));
        t.setSpace("preserve");
        r.getContent().add(t);
        p.getContent().add(r);
    }
    wordPackage.getMainDocumentPart().addObject(p);
}
```

- [ ] **Step 4: Compilar**

```bash
mvn compile -q
```

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/abntbuilder/formatter/output/docx/api/DocxListItemParagraph.java \
        src/main/java/com/abntbuilder/formatter/output/docx/api/DocxBlock.java \
        src/main/java/com/abntbuilder/formatter/output/docx/docx4j/Docx4jWriter.java
git commit -m "feat: add DocxListItemParagraph and list numbering in Docx4jWriter"
```

---

## Task 4 — Listas: perfil, requests e renderer

**Files:**
- Modify: `BodyContentStyleMapping.java`, `BodyContentStyleMappingRequest.java`
- Create: `BodyListRequest.java`, `BodyListItemRequest.java`
- Modify: `BodyBlockType.java`, `BodyBlockRequest.java`
- Modify: `BodyContentRenderer.java`
- Modify: `abnt-unip-profile.json`

- [ ] **Step 1: Adicionar `listOrderedStyleId` e `listUnorderedStyleId` em `BodyContentStyleMapping`**

```java
public record BodyContentStyleMapping(
        List<String> sectionTitleStyleIdsByLevel,
        String paragraphStyleId,
        String directShortQuoteStyleId,
        String directLongQuoteStyleId,
        String indirectCitationStyleId,
        String citationOfCitationStyleId,
        String listOrderedStyleId,      // NOVO
        String listUnorderedStyleId     // NOVO
) { ... }
```

Atualizar construtor e validações para exigir os novos campos. Atualizar `BodyContentStyleMappingRequest` analogamente.

- [ ] **Step 2: Criar `BodyListItemRequest`**

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/BodyListItemRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.bodycontent.BodyListItem;
import com.abntbuilder.formatter.profile.model.component.bodycontent.CitationFormattingRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BodyListItemRequest(@Valid @NotEmpty List<BodyInlineRequest> content) {

    public BodyListItem toDomain(CitationFormattingRule citationFormatting) {
        return new BodyListItem(
                content.stream()
                        .map(inline -> inline.toDomain(citationFormatting))
                        .toList()
        );
    }
}
```

- [ ] **Step 3: Criar `BodyListRequest`**

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/BodyListRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.bodycontent.BodyList;
import com.abntbuilder.formatter.document.component.bodycontent.BodyListType;
import com.abntbuilder.formatter.profile.model.component.bodycontent.CitationFormattingRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record BodyListRequest(
        @NotNull BodyListType type,
        @Valid @NotEmpty List<BodyListItemRequest> items
) {
    public BodyList toDomain(CitationFormattingRule citationFormatting) {
        return new BodyList(type,
                items.stream()
                        .map(item -> item.toDomain(citationFormatting))
                        .toList());
    }
}
```

- [ ] **Step 4: Atualizar `BodyBlockType`**

```java
public enum BodyBlockType {
    PARAGRAPH,
    DIRECT_LONG_QUOTE,
    FIGURE,
    TABLE,
    ORDERED_LIST,
    UNORDERED_LIST,
    FRAME,
    CODE_LISTING,
    CHART,
    EQUATION
}
```

- [ ] **Step 5: Atualizar `BodyBlockRequest` com novos campos**

Adicionar campos:

```java
@Valid BodyListRequest list,
@Valid BodyFrameRequest frame,
@Valid BodyCodeListingRequest codeListing,
@Valid BodyChartRequest chart,
@Valid BodyEquationRequest equation
```

Adicionar casos no `toDomain(CitationFormattingRule)`:

```java
case ORDERED_LIST, UNORDERED_LIST -> {
    if (list == null) throw new InvalidBodyContentException(type + " block requires list.");
    yield list.toDomain(citationFormatting);
}
case FRAME -> {
    if (frame == null) throw new InvalidBodyContentException("frame must be provided for FRAME block.");
    yield frame.toDomain();
}
case CODE_LISTING -> {
    if (codeListing == null) throw new InvalidBodyContentException("codeListing must be provided for CODE_LISTING block.");
    yield codeListing.toDomain();
}
case CHART -> {
    if (chart == null) throw new InvalidBodyContentException("chart must be provided for CHART block.");
    yield chart.toDomain();
}
case EQUATION -> {
    if (equation == null) throw new InvalidBodyContentException("equation must be provided for EQUATION block.");
    yield equation.toDomain();
}
```

- [ ] **Step 6: Adicionar caso `BodyList` em `BodyContentRenderer.renderContentBlock`**

```java
case BodyList list -> {
    StyleRule itemStyle = styleResolver.resolve(
            list.type() == BodyListType.ORDERED
                    ? rule.styleMapping().listOrderedStyleId()
                    : rule.styleMapping().listUnorderedStyleId()
    );
    yield list.items().stream()
            .map(item -> {
                List<DocxRun> runs = item.content().stream()
                        .map(inline -> toDocxRun(inline, itemStyle, rule, styleResolver))
                        .toList();
                return (DocxBlock) new DocxListItemParagraph(runs, itemStyle, list.type(), 0);
            })
            .toList();
}
```

- [ ] **Step 7: Adicionar estilos de lista em `abnt-unip-profile.json`**

Em `styleRules`:

```json
{
  "id": "bodyContent.list.ordered",
  "fontFamily": "Times New Roman",
  "fontSizePt": 12,
  "bold": false,
  "italic": false,
  "uppercase": false,
  "alignment": "JUSTIFY",
  "lineHeightRule": "EXACT",
  "lineHeightPt": 18,
  "firstLineIndentCm": 0,
  "indentLeftCm": 1.25
},
{
  "id": "bodyContent.list.unordered",
  "fontFamily": "Times New Roman",
  "fontSizePt": 12,
  "bold": false,
  "italic": false,
  "uppercase": false,
  "alignment": "JUSTIFY",
  "lineHeightRule": "EXACT",
  "lineHeightPt": 18,
  "firstLineIndentCm": 0,
  "indentLeftCm": 1.25
}
```

Em `componentRules.bodyContent.styleMapping`:

```json
"listOrderedStyleId": "bodyContent.list.ordered",
"listUnorderedStyleId": "bodyContent.list.unordered"
```

- [ ] **Step 8: Compilar e rodar**

```bash
mvn compile -q && mvn test -pl . -Dtest="BodyListTest,BodyContentSampleValidationTest" -q
```

- [ ] **Step 9: Commit**

```bash
git add -A
git commit -m "feat: add ORDERED_LIST and UNORDERED_LIST block types with DOCX rendering"
```

---

## Task 5 — Quadro (FRAME): domínio e regra de perfil

**Files:**
- Create: `BodyFrame.java`, `FrameRule.java`, `FrameRuleRequest.java`
- Modify: `NumberedDisplayObject.java`, `BodyContentComponentRule.java`, `BodyContentComponentRuleRequest.java`

- [ ] **Step 1: Criar `BodyFrame`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyFrame.java
package com.abntbuilder.formatter.document.component.bodycontent;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record BodyFrame(
        String id,
        Optional<String> continuationGroupId,
        String caption,
        Optional<String> source,
        List<BodyTableColumn> columns,
        List<BodyTableRow> rows
) implements NumberedDisplayObject {

    public BodyFrame {
        requireNonBlank(id, "id");
        Objects.requireNonNull(continuationGroupId, "continuationGroupId must not be null");
        requireNonBlank(caption, "caption");
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(columns, "columns must not be null");
        if (columns.isEmpty()) throw new IllegalArgumentException("columns must not be empty.");
        Objects.requireNonNull(rows, "rows must not be null");
        if (rows.isEmpty()) throw new IllegalArgumentException("rows must not be empty.");
        int colCount = columns.size();
        for (BodyTableRow row : rows) {
            if (row.cells().size() != colCount) {
                throw new IllegalArgumentException(
                        "frame row cell count must match column count (" + colCount + ")."
                );
            }
        }
        columns = List.copyOf(columns);
        rows = List.copyOf(rows);
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank.");
        }
    }
}
```

- [ ] **Step 2: Atualizar `NumberedDisplayObject`**

```java
public sealed interface NumberedDisplayObject extends BodyBlock
    permits BodyFigure, BodyTable, BodyFrame, BodyCodeListing, BodyChart {
}
```

- [ ] **Step 3: Criar `FrameRule`**

```java
// src/main/java/com/abntbuilder/formatter/profile/model/component/bodycontent/FrameRule.java
package com.abntbuilder.formatter.profile.model.component.bodycontent;

import com.abntbuilder.formatter.profile.model.TextAlignment;

import java.math.BigDecimal;
import java.util.Objects;

public record FrameRule(
        String captionStyleId,
        String sourceStyleId,
        String headerStyleId,
        String cellStyleId,
        String captionTemplate,
        String sourceTemplate,
        DisplayObjectContinuationLabels continuationLabels,
        DisplayObjectSourcePlacement sourcePlacement,
        TextAlignment tableAlignment,
        BigDecimal widthPercent,
        Boolean repeatHeaderOnPageBreak
) {
    public FrameRule {
        requireNonBlank(captionStyleId, "captionStyleId");
        requireNonBlank(sourceStyleId, "sourceStyleId");
        requireNonBlank(headerStyleId, "headerStyleId");
        requireNonBlank(cellStyleId, "cellStyleId");
        requireNonBlank(captionTemplate, "captionTemplate");
        requireNonBlank(sourceTemplate, "sourceTemplate");
        Objects.requireNonNull(continuationLabels, "continuationLabels must not be null");
        Objects.requireNonNull(sourcePlacement, "sourcePlacement must not be null");
        Objects.requireNonNull(tableAlignment, "tableAlignment must not be null");
        Objects.requireNonNull(widthPercent, "widthPercent must not be null");
        if (widthPercent.compareTo(BigDecimal.ZERO) <= 0 || widthPercent.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("widthPercent must be between 0 (exclusive) and 100 (inclusive).");
        }
        Objects.requireNonNull(repeatHeaderOnPageBreak, "repeatHeaderOnPageBreak must not be null");
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank.");
        }
    }
}
```

- [ ] **Step 4: Adicionar `frame` em `BodyContentComponentRule`**

```java
public record BodyContentComponentRule(
        String componentId,
        BodyContentStyleMapping styleMapping,
        BodyContentNumberingRule numbering,
        BodyContentLayoutRule layout,
        FigureRule figure,
        TableRule table,
        FrameRule frame,           // NOVO
        CitationFormattingRule citationFormatting
) implements ComponentRule { ... }
```

Atualizar construtor e `BodyContentComponentRuleRequest` analogamente.

- [ ] **Step 5: Criar `FrameRuleRequest`**

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/FrameRuleRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.profile.model.component.bodycontent.DisplayObjectSourcePlacement;
import com.abntbuilder.formatter.profile.model.component.bodycontent.FrameRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FrameRuleRequest(
        @NotBlank String captionStyleId,
        @NotBlank String sourceStyleId,
        @NotBlank String headerStyleId,
        @NotBlank String cellStyleId,
        @NotBlank String captionTemplate,
        @NotBlank String sourceTemplate,
        @Valid @NotNull DisplayObjectContinuationLabelsRequest continuationLabels,
        @NotNull DisplayObjectSourcePlacement sourcePlacement,
        @NotNull TextAlignment tableAlignment,
        @NotNull BigDecimal widthPercent,
        @NotNull Boolean repeatHeaderOnPageBreak
) {

    FrameRule toDomain() {
        return new FrameRule(
                captionStyleId,
                sourceStyleId,
                headerStyleId,
                cellStyleId,
                captionTemplate,
                sourceTemplate,
                continuationLabels.toDomain(),
                sourcePlacement,
                tableAlignment,
                widthPercent,
                repeatHeaderOnPageBreak
        );
    }
}
```

- [ ] **Step 6: Adicionar regra de frame e estilos em `abnt-unip-profile.json`**

```json
"frame": {
  "captionStyleId": "bodyContent.frame.caption",
  "sourceStyleId": "bodyContent.frame.source",
  "headerStyleId": "bodyContent.frame.header",
  "cellStyleId": "bodyContent.frame.cell",
  "captionTemplate": "Quadro {number} - {caption}",
  "sourceTemplate": "Fonte: {source}",
  "continuationLabels": { "first": "continua", "middle": "continuação", "last": "conclusão" },
  "sourcePlacement": "LAST_PART_ONLY",
  "tableAlignment": "CENTER",
  "widthPercent": 100,
  "repeatHeaderOnPageBreak": true
}
```

- [ ] **Step 7: Compilar**

```bash
mvn compile -q
```

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "feat: add BodyFrame domain type and FrameRule profile rule"
```

---

## Task 6 — Quadro: bordas fechadas no writer e renderer

**Files:**
- Create: `TableBorderStyle.java` (enum)
- Modify: `DocxTableBlock.java` — campo `borderStyle`
- Modify: `Docx4jWriter.java` — bordas CLOSED vs OPEN
- Modify: `BodyContentRenderer.java` — renderiza `BodyFrame`

- [ ] **Step 1: Criar `TableBorderStyle`**

```java
// src/main/java/com/abntbuilder/formatter/output/docx/api/TableBorderStyle.java
package com.abntbuilder.formatter.output.docx.api;

public enum TableBorderStyle {
    OPEN,   // ABNT tabela: sem bordas verticais externas, sem borda sup/inf
    CLOSED  // ABNT quadro: todas as bordas fechadas
}
```

- [ ] **Step 2: Atualizar `DocxTableBlock` com `borderStyle`**

Adicionar campo `TableBorderStyle borderStyle` ao record. Valor padrão para código existente: `OPEN`. Atualizar construtor e todos os callers em `BodyContentRenderer.renderTable`.

- [ ] **Step 3: Atualizar `Docx4jWriter` para aplicar bordas por `borderStyle`**

No método que processa `DocxTableBlock`, ao criar `TblBorders`:

**⚠️ O writer já possui `createTableBorder()` (linha ~356). Não criar um novo método `createBorder()` — isso geraria código duplicado. Reutilizar `createTableBorder()` diretamente.**

Localizar no writer o bloco de configuração de bordas da tabela (dentro de `writeTable`). Atualizar para usar `tableBlock.borderStyle()`:

```java
TblBorders tblBorders = objectFactory.createTblBorders();
if (tableBlock.borderStyle() == TableBorderStyle.CLOSED) {
    // ABNT quadro: todas as bordas fechadas
    tblBorders.setTop(createTableBorder());
    tblBorders.setBottom(createTableBorder());
    tblBorders.setLeft(createTableBorder());
    tblBorders.setRight(createTableBorder());
    tblBorders.setInsideH(createTableBorder());
    tblBorders.setInsideV(createTableBorder());
} else {
    // ABNT tabela: apenas linhas horizontais
    tblBorders.setTop(createTableBorder());
    tblBorders.setBottom(createTableBorder());
    tblBorders.setInsideH(createTableBorder());
    // Sem left, right, insideV — padrão ABNT tabela
}
tableProperties.setTblBorders(tblBorders);
```

O método `createTableBorder()` já existe no writer e não deve ser modificado.

- [ ] **Step 4: Adicionar `BodyFrame` em `BodyContentRenderer`**

Em `render()`, inicializar o estado de rendering de frames logo após o de tabelas:

```java
DisplayObjectRenderingState<BodyFrame> frameRenderingState = new DisplayObjectRenderingState<>(
        framesFrom(component.sections())
);
```

Adicionar o helper `framesFrom` análogo a `tablesFrom`:

```java
private static List<BodyFrame> framesFrom(List<BodySection> sections) {
    List<BodyFrame> frames = new ArrayList<>();
    for (BodySection section : sections) {
        for (BodyBlock block : section.blocks()) {
            if (block instanceof BodyFrame frame) {
                frames.add(frame);
            }
        }
    }
    return List.copyOf(frames);
}
```

Adicionar `frameRenderingState` como parâmetro de `renderContentBlock` (ao lado de `figureRenderingState` e `tableRenderingState`) e adicionar o case:

```java
case BodyFrame frame -> renderFrame(frame, rule.frame(), styleResolver, frameRenderingState);
```

Implementar `renderFrame` — idêntico a `renderTable` mas usa `rule.frame()` (do tipo `FrameRule`) e passa `TableBorderStyle.CLOSED` para `DocxTableBlock`:

```java
private static List<DocxBlock> renderFrame(
        BodyFrame frame,
        FrameRule rule,
        StyleResolver styleResolver,
        DisplayObjectRenderingState<BodyFrame> frameRenderingState
) {
    DisplayObjectContinuationPart part = frameRenderingState.nextPart(frame, rule.continuationLabels());
    boolean renderSource = shouldRenderSource(frame, rule, part, frameRenderingState);
    List<DocxBlock> blocks = new ArrayList<>();

    StyleRule captionStyle = styleResolver.resolve(rule.captionStyleId());
    blocks.add(new DocxParagraph(
            List.of(DocxRun.of(resolveCaptionText(frame.caption(), rule.captionTemplate(), part), captionStyle)),
            captionStyle,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            true,
            true
    ));
    blocks.add(new DocxTableBlock(
            frame.columns().stream().map(column -> column.header()).toList(),
            frame.rows().stream().map(row -> row.cells()).toList(),
            styleResolver.resolve(rule.headerStyleId()),
            styleResolver.resolve(rule.cellStyleId()),
            rule.widthPercent(),
            rule.tableAlignment(),
            rule.repeatHeaderOnPageBreak(),
            renderSource,
            true,
            TableBorderStyle.CLOSED   // quadro ABNT usa bordas fechadas
    ));

    if (renderSource) {
        String source = frameRenderingState.sourceFor(frame).orElseThrow();
        StyleRule sourceStyle = styleResolver.resolve(rule.sourceStyleId());
        blocks.add(new DocxParagraph(
                List.of(DocxRun.of(rule.sourceTemplate().replace("{source}", source), sourceStyle)),
                sourceStyle,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                false,
                true
        ));
    }

    return List.copyOf(blocks);
}
```

Adicionar `shouldRenderSource` para `BodyFrame`/`FrameRule` análogo ao de tabela:

```java
private static boolean shouldRenderSource(
        BodyFrame frame,
        FrameRule rule,
        DisplayObjectContinuationPart part,
        DisplayObjectRenderingState<BodyFrame> frameRenderingState
) {
    if (frameRenderingState.sourceFor(frame).isEmpty()) {
        return false;
    }
    return switch (rule.sourcePlacement()) {
        case EVERY_PART -> true;
        case LAST_PART_ONLY -> part.last();
    };
}
```

- [ ] **Step 5: Validar IDs únicos de frames em `BodyContentComponent`**

Em `validateDisplayObjectIds`, adicionar um terceiro `Set<String>` para frames, exatamente como feito para figuras e tabelas:

```java
private static void validateDisplayObjectIds(List<BodySection> sections) {
    Set<String> figureIds = new HashSet<>();
    Set<String> tableIds = new HashSet<>();
    Set<String> frameIds = new HashSet<>();

    for (BodySection section : sections) {
        for (BodyBlock block : section.blocks()) {
            if (block instanceof BodyFigure figure && !figureIds.add(figure.id())) {
                throw new IllegalArgumentException("bodyContent figure id must be unique: " + figure.id());
            }
            if (block instanceof BodyTable table && !tableIds.add(table.id())) {
                throw new IllegalArgumentException("bodyContent table id must be unique: " + table.id());
            }
            if (block instanceof BodyFrame frame && !frameIds.add(frame.id())) {
                throw new IllegalArgumentException("bodyContent frame id must be unique: " + frame.id());
            }
        }
    }
}
```

- [ ] **Step 6: Compilar e rodar suite**

```bash
mvn compile -q && mvn test -q
```

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "feat: add BodyFrame rendering with CLOSED borders and FRAME block type"
```

---

## Task 7 — Code Listing, Chart e Equação: domínio, perfil e renderer

**Files:**
- Create: `BodyCodeListing.java`, `BodyChart.java`, `BodyEquation.java`
- Create: `CodeListingRule.java`, `ChartRule.java`
- Modify: `BodyContentComponentRule.java` — adiciona `codeListingRule`, `chartRule`
- Modify: `BodyContentRenderer.java`
- Modify: `BodyContentStyleMapping.java` — adiciona `equationStyleId`

- [ ] **Step 1: Criar `BodyCodeListing`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyCodeListing.java
package com.abntbuilder.formatter.document.component.bodycontent;

import java.util.Objects;
import java.util.Optional;

public record BodyCodeListing(
        String id,
        Optional<String> continuationGroupId,
        String caption,
        Optional<String> language,
        String code,
        Optional<String> source
) implements NumberedDisplayObject {

    public BodyCodeListing {
        requireNonBlank(id, "id");
        Objects.requireNonNull(continuationGroupId, "continuationGroupId must not be null");
        requireNonBlank(caption, "caption");
        Objects.requireNonNull(language, "language must not be null");
        requireNonBlank(code, "code");
        Objects.requireNonNull(source, "source must not be null");
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank.");
        }
    }
}
```

- [ ] **Step 2: Criar `BodyChart`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyChart.java
package com.abntbuilder.formatter.document.component.bodycontent;

import java.util.Objects;
import java.util.Optional;

public record BodyChart(
        String id,
        Optional<String> continuationGroupId,
        String caption,
        Optional<String> source,
        BodyImageSource image
) implements NumberedDisplayObject {

    public BodyChart {
        requireNonBlank(id, "id");
        Objects.requireNonNull(continuationGroupId, "continuationGroupId must not be null");
        requireNonBlank(caption, "caption");
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(image, "image must not be null");
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank.");
        }
    }
}
```

- [ ] **Step 3: Criar `BodyEquation`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyEquation.java
package com.abntbuilder.formatter.document.component.bodycontent;

import java.util.Objects;
import java.util.Optional;

public record BodyEquation(
        String text,
        Optional<String> label
) implements BodyBlock {

    public BodyEquation {
        requireNonBlank(text, "text");
        Objects.requireNonNull(label, "label must not be null");
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank.");
        }
    }
}
```

- [ ] **Step 4: Criar `CodeListingRule` e `ChartRule`**

```java
// src/main/java/com/abntbuilder/formatter/profile/model/component/bodycontent/CodeListingRule.java
package com.abntbuilder.formatter.profile.model.component.bodycontent;

import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

import java.util.Objects;

public record CodeListingRule(
        String captionStyleId,
        String sourceStyleId,
        String codeStyleId,
        String captionTemplate,
        String sourceTemplate,
        DisplayObjectContinuationLabels continuationLabels,
        DisplayObjectSourcePlacement sourcePlacement
) {

    public CodeListingRule {
        requireNonBlank(captionStyleId, "captionStyleId");
        requireNonBlank(sourceStyleId, "sourceStyleId");
        requireNonBlank(codeStyleId, "codeStyleId");
        requireNonBlank(captionTemplate, "captionTemplate");
        requireNonBlank(sourceTemplate, "sourceTemplate");
        Objects.requireNonNull(continuationLabels, "continuationLabels must not be null");
        Objects.requireNonNull(sourcePlacement, "sourcePlacement must not be null");

        if (!captionTemplate.contains("{number}") || !captionTemplate.contains("{caption}")) {
            throw new InvalidProfileStructureException(
                    "codeListing.captionTemplate must contain {number} and {caption}."
            );
        }
        if (!sourceTemplate.contains("{source}")) {
            throw new InvalidProfileStructureException(
                    "codeListing.sourceTemplate must contain {source}."
            );
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidProfileStructureException("codeListing." + fieldName + " must not be blank.");
        }
    }
}
```

```java
// src/main/java/com/abntbuilder/formatter/profile/model/component/bodycontent/ChartRule.java
package com.abntbuilder.formatter.profile.model.component.bodycontent;

import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;

import java.util.Objects;

/**
 * Regra de perfil para gráficos.
 * imageRule encapsula todos os campos de renderização de imagem (alignment, maxWidthCm, maxHeightCm,
 * defaultDpi, maxImageBytes, urlFetchTimeoutSeconds, fitPolicy) reutilizando FigureRule.
 */
public record ChartRule(
        String captionStyleId,
        String sourceStyleId,
        String captionTemplate,
        String sourceTemplate,
        DisplayObjectContinuationLabels continuationLabels,
        DisplayObjectSourcePlacement sourcePlacement,
        FigureRule imageRule
) {

    public ChartRule {
        requireNonBlank(captionStyleId, "captionStyleId");
        requireNonBlank(sourceStyleId, "sourceStyleId");
        requireNonBlank(captionTemplate, "captionTemplate");
        requireNonBlank(sourceTemplate, "sourceTemplate");
        Objects.requireNonNull(continuationLabels, "continuationLabels must not be null");
        Objects.requireNonNull(sourcePlacement, "sourcePlacement must not be null");
        Objects.requireNonNull(imageRule, "imageRule must not be null");

        if (!captionTemplate.contains("{number}") || !captionTemplate.contains("{caption}")) {
            throw new InvalidProfileStructureException(
                    "chart.captionTemplate must contain {number} and {caption}."
            );
        }
        if (!sourceTemplate.contains("{source}")) {
            throw new InvalidProfileStructureException(
                    "chart.sourceTemplate must contain {source}."
            );
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidProfileStructureException("chart." + fieldName + " must not be blank.");
        }
    }
}
```

- [ ] **Step 5: Adicionar `equationStyleId` em `BodyContentStyleMapping`**

- [ ] **Step 6: Adicionar casos no renderer**

`BodyCodeListing`: dividir `code` em linhas por `\n`, emitir um `DocxParagraph` por linha com o estilo de código.

```java
case BodyCodeListing codeListing -> {
    DisplayObjectContinuationPart part = codeListingRenderingState.nextPart(codeListing, rule.codeListing().continuationLabels());
    List<DocxBlock> blocks = new ArrayList<>();
    StyleRule captionStyle = styleResolver.resolve(rule.codeListing().captionStyleId());
    blocks.add(new DocxParagraph(
            List.of(DocxRun.of(resolveCaptionText(codeListing.caption(), rule.codeListing().captionTemplate(), part), captionStyle)),
            captionStyle, Optional.empty(), Optional.empty(), Optional.empty(), true, true
    ));
    StyleRule codeStyle = styleResolver.resolve(rule.codeListing().codeStyleId());
    String[] lines = codeListing.code().split("\n", -1);
    for (String line : lines) {
        String rendered = line.isEmpty() ? " " : line;
        blocks.add(new DocxParagraph(List.of(DocxRun.of(rendered, codeStyle)), codeStyle));
    }
    if (shouldRenderSource(codeListing, rule.codeListing(), part, codeListingRenderingState)) {
        String source = codeListingRenderingState.sourceFor(codeListing).orElseThrow();
        StyleRule sourceStyle = styleResolver.resolve(rule.codeListing().sourceStyleId());
        blocks.add(new DocxParagraph(
                List.of(DocxRun.of(rule.codeListing().sourceTemplate().replace("{source}", source), sourceStyle)),
                sourceStyle
        ));
    }
    yield List.copyOf(blocks);
}
```

`BodyChart`: reutilizar `renderImageDisplayObject` genérico extraído de `renderFigure`. Para isso, extrair de `renderFigure` o bloco que gera `DocxImageBlock` para um método separado com a assinatura:

```java
private static DocxImageBlock renderImageDisplayObject(
        BodyImageSource imageSource,
        FigureRule figureRule,
        boolean shouldRenderSource
) {
    ResolvedImage resolved = resolveImage(imageSource, figureRule);
    return new DocxImageBlock(
            resolved.bytes(),
            resolved.mimeType(),
            imageSource.altText(),
            resolved.widthCm(),
            resolved.heightCm(),
            figureRule.imageAlignment(),
            shouldRenderSource,
            true
    );
}
```

`BodyChart.renderChart` chama este método passando `chart.image()` e a `FigureRule` do perfil (ou uma `ChartRule` com campos de image compatíveis — se `ChartRule` tiver `imageAlignment`, `maxWidthCm`, `maxHeightCm`, `defaultDpi`, `maxImageBytes` e `urlFetchTimeoutSeconds`, pode ser usada diretamente no lugar de `FigureRule`).

**Decisão de design:** Se `ChartRule` tiver os mesmos campos de imagem que `FigureRule`, criar interface comum `ImageDisplayRule` implementada por ambas — assim `resolveImage` e `renderImageDisplayObject` aceitam a interface. Caso contrário, adicionar um campo `FigureRule imageRule` dentro de `ChartRule` delegando para ele.

`BodyEquation`: parágrafo simples com `equationStyleId`.

```java
case BodyEquation equation -> {
    StyleRule equationStyle = styleResolver.resolve(rule.styleMapping().equationStyleId());
    yield List.of(new DocxParagraph(
            List.of(DocxRun.of(equation.text(), equationStyle)),
            equationStyle
    ));
}
```

- [ ] **Step 7: Criar requests**

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/BodyCodeListingRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.bodycontent.BodyCodeListing;
import jakarta.validation.constraints.NotBlank;

import java.util.Optional;

public record BodyCodeListingRequest(
        @NotBlank String id,
        String continuationGroupId,
        @NotBlank String caption,
        String language,
        @NotBlank String code,
        String source
) {
    public BodyCodeListing toDomain() {
        return new BodyCodeListing(
                id,
                Optional.ofNullable(continuationGroupId),
                caption,
                Optional.ofNullable(language),
                code,
                Optional.ofNullable(source)
        );
    }
}
```

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/BodyChartRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.bodycontent.BodyChart;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Optional;

public record BodyChartRequest(
        @NotBlank String id,
        String continuationGroupId,
        @NotBlank String caption,
        String source,
        @Valid @NotNull BodyImageSourceRequest image
) {
    public BodyChart toDomain() {
        return new BodyChart(
                id,
                Optional.ofNullable(continuationGroupId),
                caption,
                Optional.ofNullable(source),
                image.toDomain()
        );
    }
}
```

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/BodyEquationRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.bodycontent.BodyEquation;
import jakarta.validation.constraints.NotBlank;

import java.util.Optional;

public record BodyEquationRequest(
        @NotBlank String text,
        String label
) {
    public BodyEquation toDomain() {
        return new BodyEquation(text, Optional.ofNullable(label));
    }
}
```

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/CodeListingRuleRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.bodycontent.CodeListingRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.DisplayObjectSourcePlacement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CodeListingRuleRequest(
        @NotBlank String captionStyleId,
        @NotBlank String sourceStyleId,
        @NotBlank String codeStyleId,
        @NotBlank String captionTemplate,
        @NotBlank String sourceTemplate,
        @Valid @NotNull DisplayObjectContinuationLabelsRequest continuationLabels,
        @NotNull DisplayObjectSourcePlacement sourcePlacement
) {
    CodeListingRule toDomain() {
        return new CodeListingRule(
                captionStyleId,
                sourceStyleId,
                codeStyleId,
                captionTemplate,
                sourceTemplate,
                continuationLabels.toDomain(),
                sourcePlacement
        );
    }
}
```

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/ChartRuleRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.bodycontent.ChartRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.DisplayObjectSourcePlacement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChartRuleRequest(
        @NotBlank String captionStyleId,
        @NotBlank String sourceStyleId,
        @NotBlank String captionTemplate,
        @NotBlank String sourceTemplate,
        @Valid @NotNull DisplayObjectContinuationLabelsRequest continuationLabels,
        @NotNull DisplayObjectSourcePlacement sourcePlacement,
        @Valid @NotNull FigureRuleRequest imageRule
) {
    ChartRule toDomain() {
        return new ChartRule(
                captionStyleId,
                sourceStyleId,
                captionTemplate,
                sourceTemplate,
                continuationLabels.toDomain(),
                sourcePlacement,
                imageRule.toDomain()
        );
    }
}
```

> **Nota sobre `BodyImageSourceRequest`:** verificar se já existe em `src/main/java/com/abntbuilder/formatter/api/export/dto/request/`. Se existir, reutilizar. Se não existir, criar com campos `sourceType (ImageSourceType)`, `dataUri (String)`, `url (String)`, `altText (String)` e `toDomain()` que retorna `new BodyImageSource(sourceType, dataUri, url, altText)`. O mesmo vale para `FigureRuleRequest` — verificar se já existe antes de criar.

- [ ] **Step 8: Adicionar regras e estilos no perfil JSON**

- [ ] **Step 9: Compilar e rodar suite**

```bash
mvn compile -q && mvn test -q
```

- [ ] **Step 10: Commit**

```bash
git add -A
git commit -m "feat: add CODE_LISTING, CHART, EQUATION block types"
```

---

## Task 8 — Siglas e Notas de Rodapé: inline

**Files:**
- Create: `BodyAbbreviation.java`, `BodyFootnote.java`
- Modify: `BodyInline.java`, `BodyCitationType.java`
- Modify: `BodyInlineType.java`, `BodyInlineRequest.java`
- Modify: `BodyContentStyleMapping.java` — `footnoteCallStyleId`, `footnoteTextStyleId`
- Modify: `Docx4jWriter.java` — suporte a footnotes

- [ ] **Step 1: Criar `BodyAbbreviation`**

```java
public record BodyAbbreviation(
        String abbreviation,
        String expansion
) implements BodyInline {
    public BodyAbbreviation {
        requireNonBlank(abbreviation, "abbreviation");
        requireNonBlank(expansion, "expansion");
    }

    @Override
    public String renderedText() {
        return abbreviation;
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank.");
        }
    }
}
```

- [ ] **Step 2: Criar `BodyFootnote`**

```java
public record BodyFootnote(
        List<BodyInline> content
) implements BodyInline {
    public BodyFootnote {
        Objects.requireNonNull(content, "content must not be null");
        if (content.isEmpty()) throw new IllegalArgumentException("content must not be empty.");
        content = List.copyOf(content);
    }

    @Override
    public String renderedText() {
        return ""; // rendered as superscript reference, not inline text
    }
}
```

- [ ] **Step 3: Atualizar `BodyInline`**

```java
public sealed interface BodyInline
    permits BodyText, BodyCitationCall, BodyQuoteText, BodyAbbreviation, BodyFootnote {
}
```

- [ ] **Step 4: Adicionar `VERBAL` em `BodyCitationType`**

```java
public enum BodyCitationType {
    DIRECT_SHORT,
    DIRECT_LONG,
    INDIRECT,
    CITATION_OF_CITATION,
    VERBAL
}
```

Atualizar `CitationFormattingRule` para incluir o campo `verbalCitationLabel` (ex: `"informação verbal"`). O label NÃO pode estar hardcoded no domínio ou no renderer — vem do perfil. O renderer acessa `rule.citationFormatting().verbalCitationLabel()` para montar o texto `"(informação verbal)"` após a chamada de citação.

No perfil JSON, adicionar em `citationFormatting`:
```json
"verbalCitationLabel": "informação verbal"
```

- [ ] **Step 5: Atualizar `BodyInlineType` e `BodyInlineRequest`**

Adicionar `ABBREVIATION` e `FOOTNOTE` ao enum `BodyInlineType`.

Adicionar os campos `expansion` e `content` ao record `BodyInlineRequest` (os outros campos já existem):

```java
public record BodyInlineRequest(
        @NotNull BodyInlineType type,
        String text,
        String expansion,                          // NOVO — para ABBREVIATION
        BodyQuoteType quoteType,
        BodyCitationType citationType,
        BodyCitationMode mode,
        @Valid CitationSourceRequest source,
        @Valid CitationSourceRequest originalSource,
        @Valid CitationSourceRequest consultedSource,
        InlineFormattingRequest formatting,
        @Valid List<BodyInlineRequest> content      // NOVO — para FOOTNOTE
) { ... }
```

No `toDomain(CitationFormattingRule)`, adicionar os novos cases:

```java
case ABBREVIATION -> {
    if (text == null || text.isBlank()) {
        throw new IllegalArgumentException("ABBREVIATION.text must not be blank.");
    }
    if (expansion == null || expansion.isBlank()) {
        throw new IllegalArgumentException("ABBREVIATION.expansion must not be blank.");
    }
    yield new BodyAbbreviation(text, expansion);
}
case FOOTNOTE -> {
    if (content == null || content.isEmpty()) {
        throw new IllegalArgumentException("FOOTNOTE.content must not be empty.");
    }
    yield new BodyFootnote(
        content.stream().map(inline -> inline.toDomain(citationFormatting)).toList()
    );
}
```

> **Não criar um método `requireNonBlank` local** — usar a verificação inline como mostrado acima para manter o padrão já presente nos outros cases de `BodyInlineRequest.toDomain()` (que lança `IllegalArgumentException` diretamente).

- [ ] **Step 6: Criar `DocxFootnoteContent` e `DocxFootnoteReferenceBlock`**

Footnotes em DOCX residem dentro de um `<w:p>` como referências inline, não como blocos separados. A abordagem é: o parágrafo que contém chamadas de nota emite um bloco especial que carrega tanto o conteúdo do parágrafo quanto as notas associadas.

```java
// src/main/java/com/abntbuilder/formatter/output/docx/api/DocxFootnoteContent.java
package com.abntbuilder.formatter.output.docx.api;

import java.util.List;
import java.util.Objects;

public record DocxFootnoteContent(int id, List<DocxRun> contentRuns) {
    public DocxFootnoteContent {
        if (id < 1) throw new IllegalArgumentException("id must be >= 1.");
        Objects.requireNonNull(contentRuns, "contentRuns must not be null");
        if (contentRuns.isEmpty()) throw new IllegalArgumentException("contentRuns must not be empty.");
        contentRuns = List.copyOf(contentRuns);
    }
}
```

```java
// src/main/java/com/abntbuilder/formatter/output/docx/api/DocxFootnoteReferenceBlock.java
package com.abntbuilder.formatter.output.docx.api;

import java.util.List;
import java.util.Objects;

public record DocxFootnoteReferenceBlock(
        DocxParagraph hostParagraph,
        List<DocxFootnoteContent> footnotes
) implements DocxBlock {
    public DocxFootnoteReferenceBlock {
        Objects.requireNonNull(hostParagraph, "hostParagraph must not be null");
        Objects.requireNonNull(footnotes, "footnotes must not be null");
        if (footnotes.isEmpty()) throw new IllegalArgumentException("footnotes must not be empty.");
        footnotes = List.copyOf(footnotes);
    }
}
```

Atualizar `DocxBlock`:
```java
public sealed interface DocxBlock
    permits DocxParagraph, DocxPageBreak, DocxBlankLine, DocxSectionBreak,
            DocxImageBlock, DocxTableBlock, DocxListItemParagraph, DocxFootnoteReferenceBlock {
}
```

- [ ] **Step 7: Implementar `DocxFootnoteReferenceBlock` no `Docx4jWriter`**

```java
case DocxFootnoteReferenceBlock fnBlock -> {
    // 1. Criar FootnotesPart se não existir
    FootnotesPart fnPart = wordPackage.getMainDocumentPart().getFootnotesPart();
    if (fnPart == null) {
        fnPart = new FootnotesPart();
        wordPackage.getMainDocumentPart().addTargetPart(fnPart);
        CTFootnotes footnotes = objectFactory.createCTFootnotes();
        fnPart.setJaxbElement(footnotes);
    }
    CTFootnotes ctFootnotes = fnPart.getJaxbElement();

    // 2. Renderizar o parágrafo principal, inserindo referências de nota
    P p = objectFactory.createP();
    p.setPPr(isHeadingStyle(fnBlock.hostParagraph().styleRule())
            ? createHeadingParagraphProperties(fnBlock.hostParagraph().styleRule())
            : createParagraphProperties(fnBlock.hostParagraph().styleRule(),
                    fnBlock.hostParagraph().spacingBeforeOverridePt(),
                    fnBlock.hostParagraph().exactLineHeightPt(),
                    fnBlock.hostParagraph().layoutOverride()));
    // Mapa de footnoteId por posição nos runs (o renderer marca com texto especial "[FN:id]")
    for (DocxRun docxRun : fnBlock.hostParagraph().runs()) {
        String text = docxRun.text();
        if (text.startsWith("[FN:") && text.endsWith("]")) {
            int fnId = Integer.parseInt(text.substring(4, text.length() - 1));
            R refRun = objectFactory.createR();
            CTFtnEdnRef ref = objectFactory.createCTFtnEdnRef();
            ref.setId(BigInteger.valueOf(fnId));
            refRun.getContent().add(objectFactory.createRFootnoteReference(ref));
            p.getContent().add(refRun);
        } else {
            R run = objectFactory.createR();
            run.setRPr(buildRunProperties(docxRun.baseStyle(), docxRun.formatting()));
            Text t = objectFactory.createText();
            t.setValue(resolveText(text, docxRun.baseStyle()));
            t.setSpace("preserve");
            run.getContent().add(t);
            p.getContent().add(run);
        }
    }
    wordPackage.getMainDocumentPart().addObject(p);

    // 3. Injetar cada nota no FootnotesPart
    for (DocxFootnoteContent fn : fnBlock.footnotes()) {
        CTFtnEdn footnote = objectFactory.createCTFtnEdn();
        footnote.setId(BigInteger.valueOf(fn.id()));
        P fnParagraph = objectFactory.createP();
        for (DocxRun docxRun : fn.contentRuns()) {
            R run = objectFactory.createR();
            run.setRPr(buildRunProperties(docxRun.baseStyle(), docxRun.formatting()));
            Text t = objectFactory.createText();
            t.setValue(docxRun.text());
            t.setSpace("preserve");
            run.getContent().add(t);
            fnParagraph.getContent().add(run);
        }
        footnote.getContent().add(fnParagraph);
        ctFootnotes.getFn().add(footnote);
    }
}
```

Imports necessários: `org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart`, `org.docx4j.wml.CTFootnotes`, `org.docx4j.wml.CTFtnEdn`, `org.docx4j.wml.CTFtnEdnRef`

- [ ] **Step 8: Atualizar `BodyContentRenderer` — coletar footnotes ao processar `BodyFootnote`**

Em `toDocxRun`, quando o inline é `BodyFootnote`, registrar a nota em uma lista acumulada local ao método `renderWithMetadata` e retornar um run marcador com texto `"[FN:id]"`:

```java
// Adicionar parâmetro à assinatura de toDocxRun:
private static DocxRun toDocxRun(
        BodyInline inline,
        StyleRule baseStyle,
        BodyContentComponentRule rule,
        StyleResolver styleResolver,
        CrossReferenceIndex crossRefIndex,
        List<DocxFootnoteContent> footnoteAccumulator,  // lista acumulada
        int[] footnoteCounter  // contador de id — array para mutabilidade em lambda
) {
    return switch (inline) {
        // Casos existentes (mantidos da implementação atual de toDocxRun):
        case BodyText text -> new DocxRun(text.text(), baseStyle, text.formatting());
        case BodyQuoteText quote -> {
            // Após adicionar applyMarkers (Task 9), renderedText() já aplica marcadores
            yield new DocxRun(quote.renderedText(), baseStyle, quote.formatting());
        }
        case BodyCitationCall call -> {
            StyleRule citationStyle = styleResolver.resolve(
                    rule.styleMapping().styleIdForCitation(call.citationType())
            );
            yield new DocxRun(call.renderedText(), citationStyle, InlineFormatting.none());
        }
        // Novos cases adicionados na Fase 2:
        case BodyAbbreviation abbr -> {
            if (abbreviationMetas != null &&
                abbreviationMetas.stream().noneMatch(m -> m.abbreviation().equals(abbr.abbreviation()))) {
                abbreviationMetas.add(new BodyAbbreviationMetadata(abbr.abbreviation(), abbr.expansion()));
            }
            yield new DocxRun(abbr.renderedText(), baseStyle, InlineFormatting.none());
        }
        case BodyCrossReference ref -> {
            String resolved = crossRefIndex.resolve(
                    ref.targetId(), ref.targetType(), ref.displayMode(), rule.crossReferenceLabels()
            );
            yield new DocxRun(resolved, baseStyle, InlineFormatting.none());
        }
        case BodyFootnote footnote -> {
            int fnId = ++footnoteCounter[0];
            List<DocxRun> fnRuns = footnote.content().stream()
                    .map(fi -> toDocxRun(fi, baseStyle, rule, styleResolver, crossRefIndex, abbreviationMetas, footnoteAccumulator, footnoteCounter))
                    .toList();
            footnoteAccumulator.add(new DocxFootnoteContent(fnId, fnRuns));
            yield new DocxRun("[FN:" + fnId + "]", baseStyle, InlineFormatting.none());
        }
    };
}
```

Após gerar os runs de um parágrafo, se `footnoteAccumulator` não estiver vazio, embrulhar em `DocxFootnoteReferenceBlock` e limpar o accumulator para o próximo parágrafo.

- [ ] **Step 7: Compilar e rodar**

```bash
mvn compile -q && mvn test -q
```

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "feat: add ABBREVIATION and FOOTNOTE inline types, VERBAL citation type"
```

---

## Task 9 — Marcadores de Citação: supressão, interpolação e ênfase

**Files:**
- Create: `BodyQuoteMarkerType.java`, `BodyQuoteMarker.java`
- Modify: `BodyQuoteText.java`
- Modify: `CitationFormattingRule.java`
- Modify: `BodyInlineRequest.java` (campo `markers`)

- [ ] **Step 1: Criar `BodyQuoteMarkerType`**

```java
public enum BodyQuoteMarkerType {
    SUPPRESSION,     // insere "[...]" em um ponto do texto
    INTERPOLATION,   // trecho entre start e end é uma inserção do citante "[texto]"
    EMPHASIS_OURS,   // acrescenta "(grifo nosso)" após a citação
    EMPHASIS_AUTHOR  // acrescenta "(grifo do autor)" após a citação
}
```

- [ ] **Step 2: Criar `BodyQuoteMarker`**

```java
public record BodyQuoteMarker(
        BodyQuoteMarkerType type,
        int position,                // índice no texto (para SUPPRESSION e INTERPOLATION início)
        Optional<Integer> endPosition // para INTERPOLATION
) {
    public BodyQuoteMarker {
        Objects.requireNonNull(type, "type must not be null");
        if (position < 0) throw new IllegalArgumentException("position must be >= 0.");
        Objects.requireNonNull(endPosition, "endPosition must not be null");
        if (type == BodyQuoteMarkerType.INTERPOLATION && endPosition.isEmpty()) {
            throw new IllegalArgumentException("INTERPOLATION marker requires endPosition.");
        }
    }
    // Convenience constructors for SUPPRESSION and EMPHASIS
    public static BodyQuoteMarker suppression(int position) {
        return new BodyQuoteMarker(BodyQuoteMarkerType.SUPPRESSION, position, Optional.empty());
    }
    public static BodyQuoteMarker emphasisOurs() {
        return new BodyQuoteMarker(BodyQuoteMarkerType.EMPHASIS_OURS, 0, Optional.empty());
    }
    public static BodyQuoteMarker emphasisAuthor() {
        return new BodyQuoteMarker(BodyQuoteMarkerType.EMPHASIS_AUTHOR, 0, Optional.empty());
    }
}
```

- [ ] **Step 3: Atualizar `CitationFormattingRule`**

```java
public record CitationFormattingRule(
        String pagePrefix,
        String multiAuthorJoiner,
        String etAl,
        String apudConnector,
        String suppressionMarker,    // NOVO: padrão "[...]"
        String emphasisOursLabel,    // NOVO: padrão "grifo nosso"
        String emphasisAuthorLabel   // NOVO: padrão "grifo do autor"
) { ... }
```

Atualizar `CitationFormattingRuleRequest` e o perfil JSON (`abnt-unip-profile.json`).

- [ ] **Step 4: Atualizar `BodyQuoteText`**

Adicionar `List<BodyQuoteMarker> markers` com default `List.of()`. Atualizar `renderedText()` para aplicar supressões e interpolações:

```java
@Override
public String renderedText() {
    String processed = applyMarkers(text, markers);
    return switch (type) {
        case SHORT -> "\"" + processed.trim() + "\"";
    };
}

private static String applyMarkers(String text, List<BodyQuoteMarker> markers) {
    // EMPHASIS_OURS e EMPHASIS_AUTHOR não modificam o texto (são sufixos emitidos pelo renderer)
    // Processar SUPPRESSION e INTERPOLATION do fim para o início para não deslocar índices
    List<BodyQuoteMarker> positionMarkers = markers.stream()
            .filter(m -> m.type() == BodyQuoteMarkerType.SUPPRESSION
                    || m.type() == BodyQuoteMarkerType.INTERPOLATION)
            .sorted(java.util.Comparator.comparingInt(BodyQuoteMarker::position).reversed())
            .toList();

    StringBuilder sb = new StringBuilder(text);
    for (BodyQuoteMarker marker : positionMarkers) {
        switch (marker.type()) {
            case SUPPRESSION -> {
                int pos = Math.min(marker.position(), sb.length());
                sb.insert(pos, "[...]");
            }
            case INTERPOLATION -> {
                int start = Math.min(marker.position(), sb.length());
                int end = Math.min(marker.endPosition().orElse(start), sb.length());
                if (end < start) break;
                sb.insert(end, "]");
                sb.insert(start, "[");
            }
            default -> { /* EMPHASIS tratado externamente */ }
        }
    }
    return sb.toString();
}
```

No renderer, `toDocxRun` precisa retornar uma lista de runs (não um único run) quando há marcadores de ênfase. Alterar a assinatura de `toDocxRun` para retornar `List<DocxRun>` e atualizar todos os callers.

O case de `BodyQuoteText` fica:

```java
case BodyQuoteText quote -> {
    List<DocxRun> runs = new ArrayList<>();
    runs.add(new DocxRun(quote.renderedText(), baseStyle, quote.formatting()));

    boolean hasEmphasisOurs = quote.markers().stream()
            .anyMatch(m -> m.type() == BodyQuoteMarkerType.EMPHASIS_OURS);
    boolean hasEmphasisAuthor = quote.markers().stream()
            .anyMatch(m -> m.type() == BodyQuoteMarkerType.EMPHASIS_AUTHOR);

    if (hasEmphasisOurs) {
        // Emitir run adicional " (grifo nosso)" após o texto principal, sem formatação extra
        runs.add(new DocxRun(
                " (" + rule.citationFormatting().emphasisOursLabel() + ")",
                baseStyle,
                InlineFormatting.none()
        ));
    } else if (hasEmphasisAuthor) {
        // Emitir run adicional " (grifo do autor)" após o texto principal
        runs.add(new DocxRun(
                " (" + rule.citationFormatting().emphasisAuthorLabel() + ")",
                baseStyle,
                InlineFormatting.none()
        ));
    }

    yield runs;
}
```

> **Nota:** `EMPHASIS_OURS` e `EMPHASIS_AUTHOR` são mutuamente exclusivos semanticamente — se ambos estiverem presentes, EMPHASIS_OURS tem precedência (if/else if acima). Os outros cases de `toDocxRun` retornam `List.of(run)`. O chamador (ex: `paragraph.content().stream().map(...).flatMap(List::stream).toList()`) faz o flatMap da lista de listas.

- [ ] **Step 5: Compilar e rodar suite**

```bash
mvn compile -q && mvn test -q
```

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: add quote markers for suppression, interpolation and emphasis"
```

---

## Task 10 — Samples e validação final

**Files:**
- Create: `docs/samples/body-content/body-content-lists.json`
- Create: `docs/samples/body-content/body-content-lists-nested-invalid.json`
- Create: `docs/samples/body-content/body-content-frames.json`
- Create: `docs/samples/body-content/body-content-code-listings.json`
- Create: `docs/samples/body-content/body-content-charts.json`
- Create: `docs/samples/body-content/body-content-equations.json`
- Create: `docs/samples/body-content/body-content-abbreviations.json`
- Create: `docs/samples/body-content/body-content-footnotes.json`
- Create: `docs/samples/body-content/body-content-quote-markers.json`
- Modify: `BodyContentSampleValidationTest.java`

- [ ] **Step 1: Criar sample de listas**

```json
{
  "profileId": "abnt-unip-profile",
  "document": {
    "components": {
      "bodyContent": {
        "sections": [{
          "id": "s1", "level": 1, "title": "Listas",
          "blocks": [
            {
              "type": "ORDERED_LIST",
              "list": {
                "type": "ORDERED",
                "items": [
                  { "content": [{ "type": "TEXT", "text": "Primeiro item." }] },
                  { "content": [{ "type": "TEXT", "text": "Segundo item com " },
                    { "type": "TEXT", "text": "negrito", "formatting": { "bold": true } },
                    { "type": "TEXT", "text": "." }] }
                ]
              }
            },
            {
              "type": "UNORDERED_LIST",
              "list": {
                "type": "UNORDERED",
                "items": [
                  { "content": [{ "type": "TEXT", "text": "Item não ordenado A." }] },
                  { "content": [{ "type": "TEXT", "text": "Item não ordenado B." }] }
                ]
              }
            }
          ]
        }]
      }
    }
  }
}
```

- [ ] **Step 2: Criar sample inválido (lista aninhada)**

Sample com item contendo outro `ORDERED_LIST` no `content` — deve falhar com 400.

- [ ] **Step 3: Criar samples para os demais tipos**

**`body-content-frames.json`** — quadro com bordas fechadas:
```json
{
  "profileId": "abnt-unip-profile",
  "document": { "components": { "bodyContent": { "sections": [{
    "id": "s1", "level": 1, "title": "Quadros",
    "blocks": [{
      "type": "FRAME",
      "frame": {
        "id": "qdr-1", "caption": "Comparação de metodologias",
        "columns": [{ "header": "Método" }, { "header": "Vantagem" }],
        "rows": [
          { "cells": ["Quantitativo", "Reprodutibilidade"] },
          { "cells": ["Qualitativo", "Profundidade"] }
        ]
      }
    }]
  }]}}}
}
```

**`body-content-code-listings.json`** — listagem de código:
```json
{
  "profileId": "abnt-unip-profile",
  "document": { "components": { "bodyContent": { "sections": [{
    "id": "s1", "level": 1, "title": "Listagens",
    "blocks": [{
      "type": "CODE_LISTING",
      "codeListing": {
        "id": "lst-1", "caption": "Algoritmo de ordenação",
        "language": "java",
        "code": "public void sort(int[] arr) {\n    Arrays.sort(arr);\n}"
      }
    }]
  }]}}}
}
```

**`body-content-charts.json`** — gráfico:
```json
{
  "profileId": "abnt-unip-profile",
  "document": { "components": { "bodyContent": { "sections": [{
    "id": "s1", "level": 1, "title": "Gráficos",
    "blocks": [{
      "type": "CHART",
      "chart": {
        "id": "grf-1", "caption": "Distribuição por faixa etária",
        "image": {
          "sourceType": "DATA_URI",
          "dataUri": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/p9sAAAAASUVORK5CYII=",
          "altText": "Gráfico de distribuição"
        }
      }
    }]
  }]}}}
}
```

**`body-content-equations.json`** — equação:
```json
{
  "profileId": "abnt-unip-profile",
  "document": { "components": { "bodyContent": { "sections": [{
    "id": "s1", "level": 1, "title": "Equações",
    "blocks": [{
      "type": "EQUATION",
      "equation": { "text": "E = mc²", "label": "1" }
    }]
  }]}}}
}
```

**`body-content-abbreviations.json`** — siglas inline:
```json
{
  "profileId": "abnt-unip-profile",
  "document": { "components": { "bodyContent": { "sections": [{
    "id": "s1", "level": 1, "title": "Siglas",
    "blocks": [{
      "type": "PARAGRAPH",
      "content": [
        { "type": "TEXT", "text": "O uso da " },
        { "type": "ABBREVIATION", "text": "IA", "expansion": "Inteligência Artificial" },
        { "type": "TEXT", "text": " tem crescido rapidamente." }
      ]
    }]
  }]}}}
}
```

**`body-content-footnotes.json`** — notas de rodapé inline:
```json
{
  "profileId": "abnt-unip-profile",
  "document": { "components": { "bodyContent": { "sections": [{
    "id": "s1", "level": 1, "title": "Notas",
    "blocks": [{
      "type": "PARAGRAPH",
      "content": [
        { "type": "TEXT", "text": "Conforme afirma o autor" },
        { "type": "FOOTNOTE", "content": [
          { "type": "TEXT", "text": "SILVA, João. Comunicação pessoal, 2023." }
        ]},
        { "type": "TEXT", "text": ", o método é eficaz." }
      ]
    }]
  }]}}}
}
```

**`body-content-quote-markers.json`** — marcadores de citação:
```json
{
  "profileId": "abnt-unip-profile",
  "document": { "components": { "bodyContent": { "sections": [{
    "id": "s1", "level": 1, "title": "Marcadores",
    "blocks": [{
      "type": "DIRECT_LONG_QUOTE",
      "quoteContent": {
        "type": "QUOTE_TEXT",
        "text": "A metodologia foi aplicada com sucesso em contextos variados.",
        "quoteType": "LONG",
        "markers": [
          { "type": "SUPPRESSION", "position": 15 },
          { "type": "EMPHASIS_OURS" }
        ]
      },
      "citation": { "type": "CITATION", "citationType": "DIRECT_LONG", "authors": [{"surname": "FERREIRA"}], "year": "2020", "page": "45" }
    }]
  }]}}}
}
```

- [ ] **Step 4: Atualizar `BodyContentSampleValidationTest`**

Adicionar todos os novos samples nas listas de válidos/inválidos.

- [ ] **Step 5: Suite completa**

```bash
mvn test -q
```

Esperado: todos passam.

- [ ] **Step 6: Validação visual no Word**

Gerar DOCX para cada sample crítico e verificar:
- Listas: bullets e numeração visíveis
- Quadro: bordas em todos os lados, "Quadro 1 - ..."
- Listagem: fonte monoespaçada, cada linha de código preservada
- Gráfico: imagem com "Gráfico 1 - ..."
- Equação: centralizada

- [ ] **Step 7: Commit final**

```bash
git add -A
git commit -m "feat: add samples and tests for all Fase 2 block and inline types"
```

---

## Checklist de conclusão da Fase 2

| Requisito | Task |
|---|---|
| Superscript e Subscript em `InlineFormatting` | Task 1 |
| `DocxListItemParagraph` e numeração DOCX | Task 3 |
| `ORDERED_LIST` e `UNORDERED_LIST` no renderer | Task 4 |
| `BodyFrame` com bordas fechadas ("Quadro") | Tasks 5-6 |
| `BodyCodeListing` com linhas preservadas | Task 7 |
| `BodyChart` reutilizando renderização de imagem | Task 7 |
| `BodyEquation` centralizado | Task 7 |
| `BodyAbbreviation` inline | Task 8 |
| `BodyFootnote` inline + `VERBAL` em `BodyCitationType` | Task 8 |
| Supressões, interpolações, ênfase em `BodyQuoteText` | Task 9 |
| Samples válidos e inválidos | Task 10 |
| Suite completa verde | Task 10 |
