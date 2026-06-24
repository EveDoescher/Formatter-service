# bodyContent Fase 5 — Componentes Gerados (Índices Derivados) ✅ CONCLUÍDA 2026-06-24

> **Pré-requisito:** Fase 3 concluída — `BodyContentMetadata` disponível em `DocumentRenderer`.
>
> **For agentic workers:** Use superpowers:subagent-driven-development. Steps usam `- [ ]`.

**Goal:** Implementar os componentes que se geram automaticamente a partir dos metadados coletados na Fase 3: Sumário, Lista de Ilustrações, Lista de Tabelas, Lista de Quadros, Lista de Gráficos, Lista de Listagens, Lista de Abreviaturas e Siglas, Lista de Símbolos.

**Tech Stack:** Java 21, Spring Boot, docx4j 11.5.13, JUnit 5

**Característica arquitetural desta fase:** Os renderers desta fase consomem `BodyContentMetadata` produzido por `BodyContentRenderer`. O `DocumentRenderer` já possui a variável local `bodyContentMetadata` e o branch `MetadataEmittingRenderer` implementados na Fase 3. A Task 1 desta fase adiciona apenas o branch `MetadataConsumingRenderer` que estava pendente.

---

## Mapa de arquivos

### Novos (domínio)
- `SummaryComponent.java`
- `ListOfFiguresComponent.java`, `ListOfTablesComponent.java`
- `ListOfFramesComponent.java`, `ListOfChartsComponent.java`, `ListOfCodeListingsComponent.java`
- `ListOfAbbreviationsComponent.java`
- `ListOfSymbolsComponent.java`, `SymbolEntry.java`

### Novos (perfil)
- `SummaryComponentRule.java`
- `IndexListComponentRule.java` (reutilizado por todas as listas de display objects)
- `ListOfAbbreviationsComponentRule.java`
- `ListOfSymbolsComponentRule.java`

### Novos (rendering)
- `SummaryRenderer.java`
- `ListOfFiguresRenderer.java`, `ListOfTablesRenderer.java`
- `ListOfFramesRenderer.java`, `ListOfChartsRenderer.java`, `ListOfCodeListingsRenderer.java`
- `ListOfAbbreviationsRenderer.java`
- `ListOfSymbolsRenderer.java`
- `MetadataConsumingRenderer.java` (interface auxiliar)

### Novos (requests)
- `SummaryRequest.java`, `SummaryComponentRuleRequest.java`
- `ListOfFiguresRequest.java`, `ListOfTablesRequest.java`, `ListOfFramesRequest.java`, `ListOfChartsRequest.java`, `ListOfCodeListingsRequest.java`
- `IndexListComponentRuleRequest.java`
- `ListOfAbbreviationsRequest.java`, `ListOfAbbreviationsComponentRuleRequest.java`
- `ListOfSymbolsRequest.java`, `SymbolEntryRequest.java`, `ListOfSymbolsComponentRuleRequest.java`

### Modificados
- `DocumentRenderer.java` — adiciona branch `MetadataConsumingRenderer` no loop existente
- `ComponentRulesRequest.java` — adiciona campos para todas as novas rules
- `DocumentContentRequest.java` — adiciona campos para todos os novos componentes
- `RenderingConfig.java` — registra todos os novos renderers como `@Bean`
- `abnt-unip-profile.json` — regras e estilos de todos os novos componentes
- `ComponentType.java` — 8 novos valores

---

## Task 1 — `MetadataConsumingRenderer` e atualização do `DocumentRenderer`

O `DocumentRenderer` já tem (desde a Fase 3):
- Variável local `BodyContentMetadata bodyContentMetadata = BodyContentMetadata.empty()` no início do método `render()`
- Branch `if (renderer instanceof MetadataEmittingRenderer<?,?> emitting)` que coleta os metadados

O que está faltando é o branch `else if` para os renderers de índice.

**Files:**
- Create: `MetadataConsumingRenderer.java`
- Modify: `DocumentRenderer.java`

- [ ] **Step 1: Criar `MetadataConsumingRenderer`**

```java
// src/main/java/com/abntbuilder/formatter/rendering/component/MetadataConsumingRenderer.java
package com.abntbuilder.formatter.rendering.component;

import com.abntbuilder.formatter.document.component.DocumentComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyContentMetadata;

import java.util.List;

public interface MetadataConsumingRenderer<T extends DocumentComponent>
        extends ComponentRenderer<T> {

    List<DocxBlock> renderWithMetadata(T component, DocumentProfile profile, BodyContentMetadata metadata);

    @Override
    default List<DocxBlock> render(T component, DocumentProfile profile) {
        return renderWithMetadata(component, profile, BodyContentMetadata.empty());
    }

    @SuppressWarnings("unchecked")
    default List<DocxBlock> renderComponentWithMetadata(
            DocumentComponent component, DocumentProfile profile, BodyContentMetadata metadata) {
        return renderWithMetadata((T) component, profile, metadata);
    }
}
```

- [ ] **Step 2: Atualizar `DocumentRenderer` — adicionar o branch `MetadataConsumingRenderer`**

No loop `for (String componentId : componentOrder)`, dentro do bloco `if (component != null)`, o código atual é:

```java
if (renderer instanceof MetadataEmittingRenderer<?,?> emitting) {
    ComponentRenderResult result = emitting.renderComponentWithMetadata(component, command.profile());
    componentBlocks = result.blocks();
    if (result instanceof BodyContentRenderResult bcr) {
        bodyContentMetadata = bcr.metadata();
    }
} else {
    componentBlocks = renderer.renderComponent(component, command.profile());
}
```

Substituir pelo seguinte (adicionar somente o `else if` entre os dois branches existentes):

```java
if (renderer instanceof MetadataEmittingRenderer<?,?> emitting) {
    ComponentRenderResult result = emitting.renderComponentWithMetadata(component, command.profile());
    componentBlocks = result.blocks();
    if (result instanceof BodyContentRenderResult bcr) {
        bodyContentMetadata = bcr.metadata();
    }
} else if (renderer instanceof MetadataConsumingRenderer<?> consuming) {
    componentBlocks = consuming.renderComponentWithMetadata(component, command.profile(), bodyContentMetadata);
} else {
    componentBlocks = renderer.renderComponent(component, command.profile());
}
```

Adicionar import em `DocumentRenderer.java`:
```java
import com.abntbuilder.formatter.rendering.component.MetadataConsumingRenderer;
```

- [ ] **Step 3: Compilar**

```bash
mvn compile -q
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/abntbuilder/formatter/rendering/component/MetadataConsumingRenderer.java \
        src/main/java/com/abntbuilder/formatter/rendering/orchestration/DocumentRenderer.java
git commit -m "feat: add MetadataConsumingRenderer interface and inject metadata in DocumentRenderer"
```

---

## Task 2 — Sumário

O sumário usa o campo TOC do Word para geração automática de números de página. O Word atualiza os números quando o usuário abre o documento e pressiona Ctrl+A → F9. Este comportamento é documentado como esperado — o formatter não promete números de página sem que o usuário atualize o campo. Esta é a solução correta: calcular páginas offline é impossível sem renderizar o layout tipográfico completo.

**Files:**
- Create: `SummaryComponent.java`, `SummaryComponentRule.java`, `SummaryRenderer.java`
- Create: `SummaryRequest.java`, `SummaryComponentRuleRequest.java`
- Create: `DocxTocBlock.java`

- [ ] **Step 1: Adicionar `SUMMARY` ao enum `ComponentType`**

```java
// Adicionar ao enum ComponentType em ComponentType.java:
SUMMARY
```

- [ ] **Step 2: Criar `SummaryComponent`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/summary/SummaryComponent.java
package com.abntbuilder.formatter.document.component.summary;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

public record SummaryComponent() implements DocumentComponent {
    @Override public ComponentType type() { return ComponentType.SUMMARY; }
}
```

- [ ] **Step 3: Criar `SummaryComponentRule`**

```java
// src/main/java/com/abntbuilder/formatter/profile/model/component/summary/SummaryComponentRule.java
package com.abntbuilder.formatter.profile.model.component.summary;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import java.util.List;
import java.util.Map;

public record SummaryComponentRule(
        String componentId,
        String headingStyleId,
        String headingText,
        List<String> entryStyleIdsByLevel,
        boolean useTocField
) implements ComponentRule {
    public SummaryComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(headingStyleId, "headingStyleId");
        requireNonBlank(headingText, "headingText");
        if (entryStyleIdsByLevel == null || entryStyleIdsByLevel.isEmpty())
            throw new IllegalArgumentException("entryStyleIdsByLevel must not be empty.");
        entryStyleIdsByLevel = List.copyOf(entryStyleIdsByLevel);
    }
    @Override public Map<String, String> contentBindings() { return Map.of(); }
    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
```

- [ ] **Step 4: Criar `DocxTocBlock`**

```java
// src/main/java/com/abntbuilder/formatter/output/docx/api/DocxTocBlock.java
package com.abntbuilder.formatter.output.docx.api;

import com.abntbuilder.formatter.profile.model.StyleRule;
import java.util.Objects;

public record DocxTocBlock(
        StyleRule styleRule,
        String tocInstruction  // ex: "TOC \\o \"1-6\" \\h \\z \\u"
) implements DocxBlock {
    public DocxTocBlock {
        Objects.requireNonNull(styleRule, "styleRule must not be null");
        if (tocInstruction == null || tocInstruction.isBlank())
            throw new IllegalArgumentException("tocInstruction must not be blank.");
    }
}
```

Atualizar `DocxBlock` (sealed interface) para incluir `DocxTocBlock` nos `permits`:

```java
public sealed interface DocxBlock
    permits DocxParagraph, DocxPageBreak, DocxBlankLine, DocxSectionBreak,
            DocxImageBlock, DocxTableBlock, DocxListItemParagraph,
            DocxFootnoteReferenceBlock, DocxTocBlock {
}
```

- [ ] **Step 5: Implementar `DocxTocBlock` no `Docx4jWriter`**

No switch de `writeBlock`, adicionar:

```java
case DocxTocBlock tocBlock -> {
    P p = objectFactory.createP();
    PPr pPr = createParagraphProperties(tocBlock.styleRule(), Optional.empty(), Optional.empty(), Optional.empty());
    p.setPPr(pPr);

    // Run 1: BEGIN
    R beginRun = objectFactory.createR();
    FldChar beginFldChar = objectFactory.createFldChar();
    beginFldChar.setFldCharType(STFldCharType.BEGIN);
    beginFldChar.setDirty(true);
    beginRun.getContent().add(objectFactory.createRFldChar(beginFldChar));
    p.getContent().add(beginRun);

    // Run 2: instrução TOC
    R instrRun = objectFactory.createR();
    Text instrText = objectFactory.createText();
    instrText.setValue(tocBlock.tocInstruction());
    instrText.setSpace("preserve");
    instrRun.getContent().add(objectFactory.createRInstrText(instrText));
    p.getContent().add(instrRun);

    // Run 3: END
    R endRun = objectFactory.createR();
    FldChar endFldChar = objectFactory.createFldChar();
    endFldChar.setFldCharType(STFldCharType.END);
    endRun.getContent().add(objectFactory.createRFldChar(endFldChar));
    p.getContent().add(endRun);

    wordPackage.getMainDocumentPart().addObject(p);
}
```

Imports necessários: `org.docx4j.wml.FldChar`, `org.docx4j.wml.STFldCharType`

- [ ] **Step 6: Criar `SummaryRenderer`**

```java
// src/main/java/com/abntbuilder/formatter/rendering/component/summary/SummaryRenderer.java
package com.abntbuilder.formatter.rendering.component.summary;

import com.abntbuilder.formatter.document.component.summary.SummaryComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.output.docx.api.DocxTocBlock;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.summary.SummaryComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.MetadataConsumingRenderer;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyContentMetadata;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodySectionMetadata;

import java.util.ArrayList;
import java.util.List;

public final class SummaryRenderer implements MetadataConsumingRenderer<SummaryComponent> {

    public static final String COMPONENT_ID = "summary";

    @Override public String componentId() { return COMPONENT_ID; }
    @Override public Class<SummaryComponent> componentType() { return SummaryComponent.class; }

    @Override
    public List<DocxBlock> renderWithMetadata(
            SummaryComponent component, DocumentProfile profile, BodyContentMetadata metadata) {
        SummaryComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(COMPONENT_ID, SummaryComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        blocks.add(new DocxParagraph(
                List.of(DocxRun.of(rule.headingText(), headingStyle)), headingStyle
        ));

        if (rule.useTocField()) {
            int maxLevel = rule.entryStyleIdsByLevel().size();
            String tocInstruction = "TOC \\o \"1-" + maxLevel + "\" \\h \\z \\u";
            blocks.add(new DocxTocBlock(headingStyle, tocInstruction));
        } else {
            for (BodySectionMetadata section : metadata.sections()) {
                int level = section.level();
                int styleIndex = Math.min(level - 1, rule.entryStyleIdsByLevel().size() - 1);
                StyleRule entryStyle = styleResolver.resolve(rule.entryStyleIdsByLevel().get(styleIndex));
                blocks.add(new DocxParagraph(
                        List.of(DocxRun.of(section.renderedTitle(), entryStyle)), entryStyle
                ));
            }
        }

        return List.copyOf(blocks);
    }
}
```

- [ ] **Step 7: Criar `SummaryRequest` e `SummaryComponentRuleRequest`**

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/SummaryRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.summary.SummaryComponent;

public record SummaryRequest() {
    public SummaryComponent toDomain() { return new SummaryComponent(); }
}
```

```java
// Colocar no mesmo pacote dos outros *ComponentRuleRequest existentes
public record SummaryComponentRuleRequest(
        @NotBlank String headingStyleId,
        @NotBlank String headingText,
        @NotEmpty List<String> entryStyleIdsByLevel,
        boolean useTocField
) {
    public SummaryComponentRule toDomain(String componentId) {
        return new SummaryComponentRule(componentId, headingStyleId, headingText,
                entryStyleIdsByLevel, useTocField);
    }
}
```

- [ ] **Step 8: Criar sample**

```
docs/samples/summary/summary-with-toc.json   — bodyContent com 3+ seções + summary
```

- [ ] **Step 9: Compilar e rodar**

```bash
mvn compile -q && mvn test -q
```

- [ ] **Step 10: Commit**

```bash
git add -A
git commit -m "feat: add Summary component with Word TOC field"
```

---

## Task 3 — Listas de Display Objects (Figuras, Tabelas, Quadros, Gráficos, Listagens)

Todos os cinco componentes seguem o mesmo padrão arquitetural e são implementados juntos.

**Files:**
- Create: `IndexListComponentRule.java`, `IndexListComponentRuleRequest.java`
- Create: 5 componentes, 5 renderers (via `AbstractIndexListRenderer`)
- Create: 5 requests (records vazios)

- [ ] **Step 1: Adicionar 5 valores ao enum `ComponentType`**

```java
LIST_OF_FIGURES,
LIST_OF_TABLES,
LIST_OF_FRAMES,
LIST_OF_CHARTS,
LIST_OF_CODE_LISTINGS
```

- [ ] **Step 2: Criar `IndexListComponentRule`**

```java
// src/main/java/com/abntbuilder/formatter/profile/model/component/indexlist/IndexListComponentRule.java
package com.abntbuilder.formatter.profile.model.component.indexlist;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;
import java.util.Map;

public record IndexListComponentRule(
        String componentId,
        String headingStyleId,
        String headingText,
        String entryStyleId,
        String entryTemplate  // ex: "{number} — {caption}"
) implements ComponentRule {
    public IndexListComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(headingStyleId, "headingStyleId");
        requireNonBlank(headingText, "headingText");
        requireNonBlank(entryStyleId, "entryStyleId");
        requireNonBlank(entryTemplate, "entryTemplate");
        if (!entryTemplate.contains("{number}") || !entryTemplate.contains("{caption}")) {
            throw new InvalidProfileStructureException(
                    "indexList.entryTemplate must contain {number} and {caption}.");
        }
    }
    @Override public Map<String, String> contentBindings() { return Map.of(); }
    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
```

- [ ] **Step 3: Criar os 5 componentes (records vazios)**

```java
// src/main/java/com/abntbuilder/formatter/document/component/listoffigures/ListOfFiguresComponent.java
public record ListOfFiguresComponent() implements DocumentComponent {
    @Override public ComponentType type() { return ComponentType.LIST_OF_FIGURES; }
}
// Idem para ListOfTablesComponent, ListOfFramesComponent, ListOfChartsComponent, ListOfCodeListingsComponent
// pacotes: listoftables, listofframes, listofcharts, listofcodelistings
```

- [ ] **Step 4: Criar `AbstractIndexListRenderer`**

```java
// src/main/java/com/abntbuilder/formatter/rendering/component/indexlist/AbstractIndexListRenderer.java
package com.abntbuilder.formatter.rendering.component.indexlist;

import com.abntbuilder.formatter.document.component.DocumentComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.indexlist.IndexListComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.MetadataConsumingRenderer;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyContentMetadata;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyDisplayObjectMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractIndexListRenderer<T extends DocumentComponent>
        implements MetadataConsumingRenderer<T> {

    protected abstract Function<BodyContentMetadata, List<BodyDisplayObjectMetadata>> metadataExtractor();

    @Override
    public List<DocxBlock> renderWithMetadata(
            T component, DocumentProfile profile, BodyContentMetadata metadata) {
        IndexListComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(componentId(), IndexListComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());
        StyleRule entryStyle = styleResolver.resolve(rule.entryStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        blocks.add(new DocxParagraph(
                List.of(DocxRun.of(rule.headingText(), headingStyle)), headingStyle));

        for (BodyDisplayObjectMetadata item : metadataExtractor().apply(metadata)) {
            String text = rule.entryTemplate()
                    .replace("{number}", String.valueOf(item.number()))
                    .replace("{caption}", item.caption());
            blocks.add(new DocxParagraph(List.of(DocxRun.of(text, entryStyle)), entryStyle));
        }

        return List.copyOf(blocks);
    }
}
```

- [ ] **Step 5: Criar os 5 renderers concretos**

```java
// src/main/java/com/abntbuilder/formatter/rendering/component/listoffigures/ListOfFiguresRenderer.java
package com.abntbuilder.formatter.rendering.component.listoffigures;

import com.abntbuilder.formatter.document.component.listoffigures.ListOfFiguresComponent;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyContentMetadata;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyDisplayObjectMetadata;
import com.abntbuilder.formatter.rendering.component.indexlist.AbstractIndexListRenderer;

import java.util.List;
import java.util.function.Function;

public final class ListOfFiguresRenderer extends AbstractIndexListRenderer<ListOfFiguresComponent> {
    public static final String COMPONENT_ID = "listOfFigures";
    @Override public String componentId() { return COMPONENT_ID; }
    @Override public Class<ListOfFiguresComponent> componentType() { return ListOfFiguresComponent.class; }
    @Override protected Function<BodyContentMetadata, List<BodyDisplayObjectMetadata>> metadataExtractor() {
        return BodyContentMetadata::figures;
    }
}
// Idem para os outros 4:
// ListOfTablesRenderer   → componentId="listOfTables"   → ::tables
// ListOfFramesRenderer   → componentId="listOfFrames"   → ::frames
// ListOfChartsRenderer   → componentId="listOfCharts"   → ::charts
// ListOfCodeListingsRenderer → componentId="listOfCodeListings" → ::codeListings
```

- [ ] **Step 6: Criar 5 requests (records vazios) e `IndexListComponentRuleRequest`**

```java
// Padrão idêntico para todos os 5:
public record ListOfFiguresRequest() {
    public ListOfFiguresComponent toDomain() { return new ListOfFiguresComponent(); }
}
// Idem: ListOfTablesRequest, ListOfFramesRequest, ListOfChartsRequest, ListOfCodeListingsRequest

public record IndexListComponentRuleRequest(
        @NotBlank String headingStyleId,
        @NotBlank String headingText,
        @NotBlank String entryStyleId,
        @NotBlank String entryTemplate
) {
    public IndexListComponentRule toDomain(String componentId) {
        return new IndexListComponentRule(componentId, headingStyleId, headingText, entryStyleId, entryTemplate);
    }
}
```

- [ ] **Step 7: Adicionar ao perfil JSON**

```json
"listOfFigures": {
  "componentId": "listOfFigures",
  "headingStyleId": "list.heading",
  "headingText": "LISTA DE ILUSTRAÇÕES",
  "entryStyleId": "list.entry",
  "entryTemplate": "{number} — {caption}"
},
"listOfTables":      { "headingText": "LISTA DE TABELAS",   ... },
"listOfFrames":      { "headingText": "LISTA DE QUADROS",   ... },
"listOfCharts":      { "headingText": "LISTA DE GRÁFICOS",  ... },
"listOfCodeListings":{ "headingText": "LISTA DE LISTAGENS", ... }
```

Estilos em `styleRules`:
```json
{ "id": "list.heading", "fontFamily": "Times New Roman", "fontSizePt": 12, "bold": true, "uppercase": true, "alignment": "CENTER", "lineHeightRule": "EXACT", "lineHeightPt": 18 },
{ "id": "list.entry",   "fontFamily": "Times New Roman", "fontSizePt": 12, "bold": false, "uppercase": false, "alignment": "JUSTIFY", "lineHeightRule": "EXACT", "lineHeightPt": 18 }
```

- [ ] **Step 8: Criar samples**

```
docs/samples/list-of-figures/list-of-figures-with-figures.json
docs/samples/list-of-tables/list-of-tables-with-tables.json
```

- [ ] **Step 9: Compilar e rodar**

```bash
mvn compile -q && mvn test -q
```

- [ ] **Step 10: Commit**

```bash
git add -A
git commit -m "feat: add list-of-figures, tables, frames, charts, code-listings index renderers"
```

---

## Task 4 — Lista de Abreviaturas e Siglas

**Files:**
- Create: `ListOfAbbreviationsComponent.java`
- Create: `ListOfAbbreviationsComponentRule.java`, `ListOfAbbreviationsComponentRuleRequest.java`
- Create: `ListOfAbbreviationsRenderer.java`
- Create: `ListOfAbbreviationsRequest.java`

- [ ] **Step 1: Adicionar `LIST_OF_ABBREVIATIONS` ao enum `ComponentType`**

- [ ] **Step 2: Criar `ListOfAbbreviationsComponent`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/listofabbreviations/ListOfAbbreviationsComponent.java
public record ListOfAbbreviationsComponent() implements DocumentComponent {
    @Override public ComponentType type() { return ComponentType.LIST_OF_ABBREVIATIONS; }
}
```

- [ ] **Step 3: Criar `ListOfAbbreviationsComponentRule`**

```java
// src/main/java/com/abntbuilder/formatter/profile/model/component/listofabbreviations/ListOfAbbreviationsComponentRule.java
public record ListOfAbbreviationsComponentRule(
        String componentId,
        String headingStyleId,
        String headingText,      // "LISTA DE ABREVIATURAS E SIGLAS"
        String entryStyleId,
        String termSeparator,    // " — "
        boolean sortAlphabetically
) implements ComponentRule {
    public ListOfAbbreviationsComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(headingStyleId, "headingStyleId");
        requireNonBlank(headingText, "headingText");
        requireNonBlank(entryStyleId, "entryStyleId");
        requireNonBlank(termSeparator, "termSeparator");
    }
    @Override public Map<String, String> contentBindings() { return Map.of(); }
    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
```

- [ ] **Step 4: Criar `ListOfAbbreviationsRenderer`**

```java
// src/main/java/com/abntbuilder/formatter/rendering/component/listofabbreviations/ListOfAbbreviationsRenderer.java
public final class ListOfAbbreviationsRenderer
        implements MetadataConsumingRenderer<ListOfAbbreviationsComponent> {

    public static final String COMPONENT_ID = "listOfAbbreviations";

    @Override public String componentId() { return COMPONENT_ID; }
    @Override public Class<ListOfAbbreviationsComponent> componentType() {
        return ListOfAbbreviationsComponent.class;
    }

    @Override
    public List<DocxBlock> renderWithMetadata(
            ListOfAbbreviationsComponent component, DocumentProfile profile, BodyContentMetadata metadata) {
        ListOfAbbreviationsComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(COMPONENT_ID, ListOfAbbreviationsComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());
        StyleRule entryStyle = styleResolver.resolve(rule.entryStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        blocks.add(new DocxParagraph(List.of(DocxRun.of(rule.headingText(), headingStyle)), headingStyle));

        List<BodyAbbreviationMetadata> abbreviations = new ArrayList<>(metadata.abbreviations());
        if (rule.sortAlphabetically()) {
            abbreviations.sort(Comparator.comparing(BodyAbbreviationMetadata::abbreviation));
        }

        for (BodyAbbreviationMetadata abbr : abbreviations) {
            String text = abbr.abbreviation() + rule.termSeparator() + abbr.expansion();
            blocks.add(new DocxParagraph(List.of(DocxRun.of(text, entryStyle)), entryStyle));
        }

        return List.copyOf(blocks);
    }
}
```

- [ ] **Step 5: Request, rule request, `DocumentContentRequest`, `RenderingConfig`, perfil, sample, compilar, commit**

```java
// ListOfAbbreviationsRequest — record vazio
public record ListOfAbbreviationsRequest() {
    public ListOfAbbreviationsComponent toDomain() { return new ListOfAbbreviationsComponent(); }
}

// ListOfAbbreviationsComponentRuleRequest
public record ListOfAbbreviationsComponentRuleRequest(
        @NotBlank String headingStyleId,
        @NotBlank String headingText,
        @NotBlank String entryStyleId,
        @NotBlank String termSeparator,
        boolean sortAlphabetically
) {
    public ListOfAbbreviationsComponentRule toDomain(String componentId) {
        return new ListOfAbbreviationsComponentRule(componentId, headingStyleId, headingText,
                entryStyleId, termSeparator, sortAlphabetically);
    }
}
```

Perfil JSON:
```json
"listOfAbbreviations": {
  "componentId": "listOfAbbreviations",
  "headingStyleId": "list.heading",
  "headingText": "LISTA DE ABREVIATURAS E SIGLAS",
  "entryStyleId": "list.entry",
  "termSeparator": " — ",
  "sortAlphabetically": true
}
```

```bash
git commit -m "feat: add ListOfAbbreviations component using collected abbreviation metadata"
```

---

## Task 5 — Lista de Símbolos

Diferente das listas de abreviaturas, a lista de símbolos é fornecida explicitamente pelo usuário — símbolos matemáticos e científicos não têm marcação inline padronizável.

**Files:**
- Create: `SymbolEntry.java`, `ListOfSymbolsComponent.java`
- Create: `ListOfSymbolsComponentRule.java`, `ListOfSymbolsComponentRuleRequest.java`
- Create: `ListOfSymbolsRenderer.java`
- Create: `ListOfSymbolsRequest.java`, `SymbolEntryRequest.java`

- [ ] **Step 1: Adicionar `LIST_OF_SYMBOLS` ao enum `ComponentType`**

- [ ] **Step 2: Criar `SymbolEntry` e `ListOfSymbolsComponent`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/listofsymbols/SymbolEntry.java
public record SymbolEntry(String symbol, String meaning) {
    public SymbolEntry {
        requireNonBlank(symbol, "symbol");
        requireNonBlank(meaning, "meaning");
    }
    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}

// src/main/java/com/abntbuilder/formatter/document/component/listofsymbols/ListOfSymbolsComponent.java
public record ListOfSymbolsComponent(List<SymbolEntry> entries) implements DocumentComponent {
    public ListOfSymbolsComponent {
        Objects.requireNonNull(entries, "entries must not be null");
        if (entries.isEmpty()) throw new IllegalArgumentException("entries must not be empty.");
        entries = List.copyOf(entries);
    }
    @Override public ComponentType type() { return ComponentType.LIST_OF_SYMBOLS; }
}
```

- [ ] **Step 3: Criar `ListOfSymbolsComponentRule`**

```java
public record ListOfSymbolsComponentRule(
        String componentId,
        String headingStyleId,
        String headingText,   // "LISTA DE SÍMBOLOS"
        String entryStyleId,
        String termSeparator  // " — "
) implements ComponentRule { ... }
```

- [ ] **Step 4: Criar `ListOfSymbolsRenderer`**

`ListOfSymbolsRenderer` **não** implementa `MetadataConsumingRenderer` — o conteúdo vem diretamente do componente, não dos metadados.

```java
// src/main/java/com/abntbuilder/formatter/rendering/component/listofsymbols/ListOfSymbolsRenderer.java
public final class ListOfSymbolsRenderer implements ComponentRenderer<ListOfSymbolsComponent> {

    public static final String COMPONENT_ID = "listOfSymbols";

    @Override public String componentId() { return COMPONENT_ID; }
    @Override public Class<ListOfSymbolsComponent> componentType() { return ListOfSymbolsComponent.class; }

    @Override
    public List<DocxBlock> render(ListOfSymbolsComponent component, DocumentProfile profile) {
        ListOfSymbolsComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(COMPONENT_ID, ListOfSymbolsComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());
        StyleRule entryStyle = styleResolver.resolve(rule.entryStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        blocks.add(new DocxParagraph(List.of(DocxRun.of(rule.headingText(), headingStyle)), headingStyle));
        for (SymbolEntry entry : component.entries()) {
            String text = entry.symbol() + rule.termSeparator() + entry.meaning();
            blocks.add(new DocxParagraph(List.of(DocxRun.of(text, entryStyle)), entryStyle));
        }
        return List.copyOf(blocks);
    }
}
```

- [ ] **Step 5: Criar requests**

```java
// SymbolEntryRequest
public record SymbolEntryRequest(@NotBlank String symbol, @NotBlank String meaning) {
    public SymbolEntry toDomain() { return new SymbolEntry(symbol, meaning); }
}

// ListOfSymbolsRequest
public record ListOfSymbolsRequest(@NotEmpty @Valid List<SymbolEntryRequest> entries) {
    public ListOfSymbolsComponent toDomain() {
        return new ListOfSymbolsComponent(entries.stream().map(SymbolEntryRequest::toDomain).toList());
    }
}

// ListOfSymbolsComponentRuleRequest
public record ListOfSymbolsComponentRuleRequest(
        @NotBlank String headingStyleId,
        @NotBlank String headingText,
        @NotBlank String entryStyleId,
        @NotBlank String termSeparator
) {
    public ListOfSymbolsComponentRule toDomain(String componentId) {
        return new ListOfSymbolsComponentRule(componentId, headingStyleId, headingText,
                entryStyleId, termSeparator);
    }
}
```

- [ ] **Step 6: `DocumentContentRequest`, `RenderingConfig`, perfil, sample, compilar, commit**

```json
"listOfSymbols": {
  "componentId": "listOfSymbols",
  "headingStyleId": "list.heading",
  "headingText": "LISTA DE SÍMBOLOS",
  "entryStyleId": "list.entry",
  "termSeparator": " — "
}
```

```bash
git commit -m "feat: add ListOfSymbols component with explicit user-provided entries"
```

---

## Task 6 — `ComponentRulesRequest`, `componentOrder` final e integração completa

> Esta task deve ser executada antes da Task 7 — o sample composto criado aqui é usado no teste de integração do pós-processador LibreOffice.

- [ ] **Step 1: Atualizar `ComponentRulesRequest`**

Adicionar os novos campos e conversões no `toDomain()`:

```java
// Novos campos a adicionar em ComponentRulesRequest:
@Valid SummaryComponentRuleRequest summary,
@Valid IndexListComponentRuleRequest listOfFigures,
@Valid IndexListComponentRuleRequest listOfTables,
@Valid IndexListComponentRuleRequest listOfFrames,
@Valid IndexListComponentRuleRequest listOfCharts,
@Valid IndexListComponentRuleRequest listOfCodeListings,
@Valid ListOfAbbreviationsComponentRuleRequest listOfAbbreviations,
@Valid ListOfSymbolsComponentRuleRequest listOfSymbols

// Em toDomain() — adicionar:
if (summary != null)            rules.add(summary.toDomain("summary"));
if (listOfFigures != null)      rules.add(listOfFigures.toDomain("listOfFigures"));
if (listOfTables != null)       rules.add(listOfTables.toDomain("listOfTables"));
if (listOfFrames != null)       rules.add(listOfFrames.toDomain("listOfFrames"));
if (listOfCharts != null)       rules.add(listOfCharts.toDomain("listOfCharts"));
if (listOfCodeListings != null) rules.add(listOfCodeListings.toDomain("listOfCodeListings"));
if (listOfAbbreviations != null) rules.add(listOfAbbreviations.toDomain("listOfAbbreviations"));
if (listOfSymbols != null)      rules.add(listOfSymbols.toDomain("listOfSymbols"));
```

- [ ] **Step 2: Atualizar `componentOrder` em `abnt-unip-profile.json`**

```json
"componentOrder": [
  "cover",
  "titlePage",
  "errata",
  "approvalSheet",
  "dedication",
  "acknowledgments",
  "epigraph",
  "resumo",
  "abstract",
  "listOfAbbreviations",
  "listOfSymbols",
  "summary",
  "listOfFigures",
  "listOfTables",
  "listOfFrames",
  "listOfCharts",
  "listOfCodeListings",
  "bodyContent",
  "references",
  "appendix",
  "annex",
  "glossary"
]
```

- [ ] **Step 3: Criar sample composto completo**

```
docs/samples/composed/documento-completo.json
```

Inclui todos os componentes com dados fictícios realistas: cover, titlePage, approvalSheet, errata, dedication, acknowledgments, epigraph, resumo, abstract, listOfAbbreviations, listOfSymbols, summary, listOfFigures, listOfTables, bodyContent com figuras e tabelas, references, appendix, annex, glossary.

- [ ] **Step 4: Criar `DocumentoCompletoIntegrationTest`**

```java
@SpringBootTest
@AutoConfigureMockMvc
class DocumentoCompletoIntegrationTest {

    @Autowired MockMvc mockMvc;

    @Test
    void shouldGenerateCompleteAcademicDocument() throws Exception {
        String json = Files.readString(Path.of("docs/samples/composed/documento-completo.json"));
        byte[] docxBytes = mockMvc.perform(post("/api/v1/exports/docx")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(docxBytes).isNotEmpty();

        Document wordDoc = extractWordDocument(docxBytes);
        NodeList instrNodes = wordDoc.getElementsByTagNameNS(
                "http://schemas.openxmlformats.org/wordprocessingml/2006/main", "instrText");
        boolean hasToc = false;
        for (int i = 0; i < instrNodes.getLength(); i++) {
            if (instrNodes.item(i).getTextContent().contains("TOC")) { hasToc = true; break; }
        }
        assertThat(hasToc).as("document should contain TOC field").isTrue();
    }

    private Document extractWordDocument(byte[] docxBytes) throws Exception {
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(docxBytes))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if ("word/document.xml".equals(entry.getName())) {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setNamespaceAware(true);
                    return factory.newDocumentBuilder().parse(zip);
                }
                zip.closeEntry();
            }
        }
        throw new IllegalStateException("word/document.xml not found in DOCX output");
    }
}
```

- [ ] **Step 5: Suite completa**

```bash
mvn test -q
```

Esperado: todos os testes passam.

- [ ] **Step 6: Validação visual no Word**

Abrir o DOCX gerado e verificar:
- Sumário: campo TOC visível; Ctrl+A → F9 atualiza com títulos e números de página
- Lista de Ilustrações / Tabelas: entradas com "{número} — {caption}"
- Lista de Abreviaturas: em ordem alfabética
- Lista de Símbolos: símbolo + significado
- Todos os pré-textuais na sequência correta
- Numeração de páginas visível apenas a partir de `bodyContent`

- [ ] **Step 7: Commit final**

```bash
git add -A
git commit -m "feat: complete Fase 5 — all index components and full academic document generation"
```

---

---

## Task 7 — Pós-processamento LibreOffice: atualização automática de campos dinâmicos

### Contexto

O `DocxTocBlock` gerado na Task 2 emite um campo `TOC` do Word com `dirty=true`. Esse campo só é resolvido (com números de página reais) quando um processador de texto abre o documento e atualiza os campos. Sem esse passo, o sumário exibe "Erro! Indicador não definido." até o usuário pressionar F9 manualmente.

Esta task implementa um pós-processador que invoca o LibreOffice em modo headless após a geração do DOCX, fazendo com que os campos dinâmicos (sumário, listas de figuras, referências cruzadas) sejam resolvidos automaticamente antes de entregar o arquivo ao usuário.

**Por que LibreOffice e não docx4j:** o docx4j tem um `FieldUpdater`, mas ele não computa números de página — não faz layout tipográfico. Para resolver o campo `TOC` com números de página corretos é necessário um processador que faça o layout completo do documento. LibreOffice headless faz exatamente isso quando converte DOCX → DOCX.

**Estratégia:** LibreOffice converte o arquivo de entrada para DOCX novamente. Durante o carregamento, ele avalia todos os campos com `dirty=true` e grava os valores resolvidos no arquivo de saída. O arquivo de saída é um DOCX válido com o sumário já preenchido.

**Comando:**
```bash
soffice --headless --norestore --convert-to "docx:MS Word 2007 XML" --outdir /tmp/output /tmp/input.docx
```

**Configuração:** o pós-processador é opcional. Se o LibreOffice não estiver instalado ou estiver desabilitado, o serviço continua funcionando — entrega o DOCX com campos por atualizar (comportamento atual). O usuário configura via `application.properties`.

**Ponto de injeção:** `DocxExportService.export()`, após `docxWriter.write(document)`. O serviço recebe os bytes do DOCX, os passa pelo pós-processador e retorna os bytes processados.

**Files:**
- Create: `DocxPostProcessor.java` (interface em `output/docx/api`)
- Create: `LibreOfficeDocxPostProcessor.java` (em `output/docx/postprocess`)
- Create: `NoOpDocxPostProcessor.java` (em `output/docx/postprocess`)
- Create: `LibreOfficeProperties.java` (properties em `config`)
- Create: `PostProcessingConfig.java` (em `config`)
- Modify: `DocxExportService.java` — injetar e chamar `DocxPostProcessor`

---

- [ ] **Step 1: Criar `DocxPostProcessor`**

```java
// src/main/java/com/abntbuilder/formatter/output/docx/api/DocxPostProcessor.java
package com.abntbuilder.formatter.output.docx.api;

public interface DocxPostProcessor {
    byte[] process(byte[] docxBytes);
}
```

- [ ] **Step 2: Criar `NoOpDocxPostProcessor`**

Usado quando o LibreOffice está desabilitado. Retorna os bytes sem modificação.

```java
// src/main/java/com/abntbuilder/formatter/output/docx/postprocess/NoOpDocxPostProcessor.java
package com.abntbuilder.formatter.output.docx.postprocess;

import com.abntbuilder.formatter.output.docx.api.DocxPostProcessor;

public final class NoOpDocxPostProcessor implements DocxPostProcessor {
    @Override
    public byte[] process(byte[] docxBytes) {
        return docxBytes;
    }
}
```

- [ ] **Step 3: Criar `LibreOfficeProperties`**

```java
// src/main/java/com/abntbuilder/formatter/config/LibreOfficeProperties.java
package com.abntbuilder.formatter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "formatter.libreoffice")
public class LibreOfficeProperties {

    private boolean enabled = false;
    private String executablePath = "soffice";  // padrão: no PATH
    private int timeoutSeconds = 60;

    // getters e setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getExecutablePath() { return executablePath; }
    public void setExecutablePath(String executablePath) { this.executablePath = executablePath; }
    public int getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
}
```

Em `src/main/resources/application.properties`, documentar as propriedades:

```properties
# Pós-processamento LibreOffice — desabilitado por padrão
# formatter.libreoffice.enabled=true
# formatter.libreoffice.executable-path=/usr/bin/soffice
# formatter.libreoffice.timeout-seconds=60
```

- [ ] **Step 4: Criar `LibreOfficeDocxPostProcessor`**

```java
// src/main/java/com/abntbuilder/formatter/output/docx/postprocess/LibreOfficeDocxPostProcessor.java
package com.abntbuilder.formatter.output.docx.postprocess;

import com.abntbuilder.formatter.config.LibreOfficeProperties;
import com.abntbuilder.formatter.output.docx.api.DocxPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public final class LibreOfficeDocxPostProcessor implements DocxPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(LibreOfficeDocxPostProcessor.class);

    private final LibreOfficeProperties properties;

    public LibreOfficeDocxPostProcessor(LibreOfficeProperties properties) {
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
    }

    @Override
    public byte[] process(byte[] docxBytes) {
        Path tempInput = null;
        Path tempOutputDir = null;
        try {
            tempInput = Files.createTempFile("formatter-", ".docx");
            tempOutputDir = Files.createTempDirectory("formatter-lo-out-");

            Files.write(tempInput, docxBytes);

            boolean success = runLibreOffice(tempInput, tempOutputDir);
            if (!success) {
                log.warn("LibreOffice post-processing failed or timed out — returning original DOCX bytes.");
                return docxBytes;
            }

            // LibreOffice gera o arquivo com o mesmo nome na pasta de saída
            Path outputFile = tempOutputDir.resolve(tempInput.getFileName());
            if (!Files.exists(outputFile)) {
                log.warn("LibreOffice output file not found — returning original DOCX bytes.");
                return docxBytes;
            }

            return Files.readAllBytes(outputFile);

        } catch (IOException | InterruptedException e) {
            log.warn("LibreOffice post-processing error — returning original DOCX bytes.", e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return docxBytes;
        } finally {
            deleteSilently(tempInput);
            deleteSilently(tempOutputDir);
        }
    }

    private boolean runLibreOffice(Path inputFile, Path outputDir)
            throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                properties.getExecutablePath(),
                "--headless",
                "--norestore",
                "--convert-to", "docx:MS Word 2007 XML",
                "--outdir", outputDir.toAbsolutePath().toString(),
                inputFile.toAbsolutePath().toString()
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // Consumir stdout para evitar bloqueio de buffer
        process.getInputStream().transferTo(java.io.OutputStream.nullOutputStream());

        boolean finished = process.waitFor(properties.getTimeoutSeconds(), TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            return false;
        }
        return process.exitValue() == 0;
    }

    private static void deleteSilently(Path path) {
        if (path == null) return;
        try {
            if (Files.isDirectory(path)) {
                try (Stream<Path> walk = Files.walk(path)) {
                    walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                    });
                }
            } else {
                Files.deleteIfExists(path);
            }
        } catch (IOException ignored) {}
    }
}
```

> **Segurança de temp files:** os arquivos são criados em diretório temporário do sistema (`java.io.tmpdir`). O bloco `finally` garante limpeza mesmo em caso de exceção. O nome do arquivo usa `formatter-` como prefixo para facilitar identificação em caso de limpeza manual.
>
> **Thread safety:** cada chamada cria seu próprio diretório temporário. O bean pode ser singleton sem problemas de concorrência.
>
> **Falha silenciosa:** se o LibreOffice falhar por qualquer motivo (não instalado, timeout, erro de conversão), o método retorna os bytes originais. O serviço nunca falha por causa do pós-processador — apenas o sumário ficará sem números de página.

- [ ] **Step 5: Criar `PostProcessingConfig`**

```java
// src/main/java/com/abntbuilder/formatter/config/PostProcessingConfig.java
package com.abntbuilder.formatter.config;

import com.abntbuilder.formatter.output.docx.api.DocxPostProcessor;
import com.abntbuilder.formatter.output.docx.postprocess.LibreOfficeDocxPostProcessor;
import com.abntbuilder.formatter.output.docx.postprocess.NoOpDocxPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LibreOfficeProperties.class)
public class PostProcessingConfig {

    @Bean
    @ConditionalOnProperty(name = "formatter.libreoffice.enabled", havingValue = "true")
    public DocxPostProcessor libreOfficeDocxPostProcessor(LibreOfficeProperties properties) {
        return new LibreOfficeDocxPostProcessor(properties);
    }

    @Bean
    @ConditionalOnProperty(
            name = "formatter.libreoffice.enabled",
            havingValue = "true",
            matchIfMissing = true  // default: NoOp quando propriedade ausente ou false
    )
    public DocxPostProcessor noOpDocxPostProcessor() {
        return new NoOpDocxPostProcessor();
    }
}
```

> **⚠️ Dois beans com o mesmo tipo:** Spring injeta o bean ativo pela condição. Quando `formatter.libreoffice.enabled=true`, apenas `libreOfficeDocxPostProcessor` é criado. Quando ausente ou `false`, apenas `noOpDocxPostProcessor`. Não há conflito porque as condições são mutuamente exclusivas.

- [ ] **Step 6: Atualizar `DocxExportService`**

```java
// Adicionar campo e injeção via construtor:
private final DocxPostProcessor docxPostProcessor;

public DocxExportService(
        DocxWriter docxWriter,
        GeneratedDocxExportStore generatedDocxExportStore,
        DocumentRenderer documentRenderer,
        DocxPostProcessor docxPostProcessor  // novo
) {
    this.docxWriter = Objects.requireNonNull(docxWriter, "docxWriter must not be null");
    this.generatedDocxExportStore = Objects.requireNonNull(generatedDocxExportStore, "...");
    this.documentRenderer = Objects.requireNonNull(documentRenderer, "documentRenderer must not be null");
    this.docxPostProcessor = Objects.requireNonNull(docxPostProcessor, "docxPostProcessor must not be null");
}

// Atualizar export():
public byte[] export(ExportDocxCommand command) {
    Objects.requireNonNull(command, "command must not be null");

    DocxDocument document = documentRenderer.render(command);
    byte[] docxBytes = docxWriter.write(document);

    return docxPostProcessor.process(docxBytes);  // novo — NoOp quando LibreOffice desabilitado
}
```

- [ ] **Step 7: Escrever `LibreOfficeDocxPostProcessorTest`**

Este teste verifica o comportamento de fallback — não requer LibreOffice instalado para passar no CI:

```java
// src/test/java/com/abntbuilder/formatter/output/docx/postprocess/LibreOfficeDocxPostProcessorTest.java
class LibreOfficeDocxPostProcessorTest {

    @Test
    void shouldReturnOriginalBytesWhenLibreOfficeNotFound() {
        LibreOfficeProperties props = new LibreOfficeProperties();
        props.setExecutablePath("nonexistent-soffice-binary");
        props.setTimeoutSeconds(5);

        LibreOfficeDocxPostProcessor processor = new LibreOfficeDocxPostProcessor(props);
        byte[] original = new byte[]{1, 2, 3, 4};

        byte[] result = processor.process(original);

        assertThat(result).isEqualTo(original);
    }

    @Test
    void shouldReturnOriginalBytesWhenInputIsEmpty() {
        LibreOfficeProperties props = new LibreOfficeProperties();
        props.setExecutablePath("nonexistent-soffice-binary");
        props.setTimeoutSeconds(5);

        LibreOfficeDocxPostProcessor processor = new LibreOfficeDocxPostProcessor(props);
        byte[] original = new byte[0];

        byte[] result = processor.process(original);

        assertThat(result).isEqualTo(original);
    }
}
```

Teste de integração real (rodado apenas quando LibreOffice disponível, via tag `@Tag("libreoffice")`):

```java
@Tag("libreoffice")
@SpringBootTest
@AutoConfigureMockMvc
class LibreOfficeTocIntegrationTest {

    @Autowired MockMvc mockMvc;

    @Test
    void tocShouldHaveResolvedPageNumbersAfterLibreOfficeProcessing() throws Exception {
        // Este teste só faz sentido com formatter.libreoffice.enabled=true e LibreOffice instalado.
        // Verificar que o DOCX resultante não contém o atributo dirty=true no campo TOC.
        String json = Files.readString(Path.of("docs/samples/summary/summary-with-toc.json"));
        byte[] docxBytes = mockMvc.perform(post("/api/v1/exports/docx")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        Document wordDoc = extractWordDocument(docxBytes);
        // Se o LibreOffice processou corretamente, os campos dirty=true foram removidos
        // e o TOC contém entradas com números de página reais (não placeholders)
        NodeList instrNodes = wordDoc.getElementsByTagNameNS(
                "http://schemas.openxmlformats.org/wordprocessingml/2006/main", "instrText");
        boolean hasToc = false;
        for (int i = 0; i < instrNodes.getLength(); i++) {
            if (instrNodes.item(i).getTextContent().contains("TOC")) { hasToc = true; break; }
        }
        assertThat(hasToc).as("TOC field should still be present after processing").isTrue();
        // Verificação visual complementar: abrir o arquivo gerado e conferir que o sumário
        // exibe números de página sem precisar pressionar F9
    }
}
```

> **CI:** O teste `@Tag("libreoffice")` é excluído do build padrão. Para rodar localmente com LibreOffice instalado:
> ```bash
> mvn test -Dgroups=libreoffice -Dformatter.libreoffice.enabled=true
> ```

- [ ] **Step 8: Compilar e rodar (sem LibreOffice)**

```bash
mvn compile -q && mvn test -q
```

Esperado: todos os testes passam, incluindo `LibreOfficeDocxPostProcessorTest` (que testa apenas o fallback, sem LibreOffice real).

- [ ] **Step 9: Commit**

```bash
git add -A
git commit -m "feat: add LibreOffice headless post-processor for TOC field resolution"
```

---

## Nota sobre números de página no Sumário

Com o pós-processador habilitado (`formatter.libreoffice.enabled=true` e LibreOffice instalado no servidor), o sumário e todas as listas de display objects são entregues com números de página já resolvidos. Sem ele, o comportamento de fallback é o DOCX com campos por atualizar — o usuário pressiona Ctrl+A → F9 no Word ou LibreOffice para resolver manualmente.

O LibreOffice é disponível gratuitamente para Linux/macOS/Windows e pode ser instalado no servidor via `apt install libreoffice` (Debian/Ubuntu). Em ambientes Docker, incluir `libreoffice-writer` na imagem.

---

## Checklist de conclusão da Fase 5

| Componente | Fonte de dados | Task |
|---|---|---|
| `MetadataConsumingRenderer` + branch no `DocumentRenderer` | — | Task 1 |
| `DocxTocBlock` + case no `Docx4jWriter` | — | Task 2 |
| Sumário com campo TOC do Word | `BodyContentMetadata.sections` | Task 2 |
| Lista de Ilustrações | `BodyContentMetadata.figures` | Task 3 |
| Lista de Tabelas | `BodyContentMetadata.tables` | Task 3 |
| Lista de Quadros | `BodyContentMetadata.frames` | Task 3 |
| Lista de Gráficos | `BodyContentMetadata.charts` | Task 3 |
| Lista de Listagens | `BodyContentMetadata.codeListings` | Task 3 |
| Lista de Abreviaturas e Siglas (ordem alfabética) | `BodyContentMetadata.abbreviations` | Task 4 |
| Lista de Símbolos (conteúdo explícito) | `ListOfSymbolsComponent.entries` | Task 5 |
| `ComponentRulesRequest` atualizado | — | Task 6 |
| `componentOrder` final com todos os componentes | — | Task 6 |
| Sample `documento-completo.json` | — | Task 6 |
| `DocxPostProcessor` interface + `NoOpDocxPostProcessor` | — | Task 7 |
| `LibreOfficeDocxPostProcessor` com fallback silencioso | — | Task 7 |
| `PostProcessingConfig` com `@ConditionalOnProperty` | — | Task 7 |
| `DocxExportService` injetando e chamando `DocxPostProcessor` | — | Task 7 |
| `LibreOfficeDocxPostProcessorTest` (testa fallback, sem LibreOffice real) | — | Task 7 |
| Suite completa verde | — | Task 6 |
| Com LibreOffice habilitado: TOC entregue com páginas resolvidas | — | Task 7 (validação local) |
| Sem LibreOffice: TOC entregue com campos por atualizar (fallback) | — | Task 7 (comportamento padrão) |
