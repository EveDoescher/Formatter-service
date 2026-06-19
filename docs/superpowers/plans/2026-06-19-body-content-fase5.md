# bodyContent Fase 5 — Componentes Gerados (Índices Derivados)

> **Pré-requisito:** Fase 3 concluída — `BodyContentMetadata` disponível em `DocumentRenderer`.
>
> **For agentic workers:** Use superpowers:subagent-driven-development. Steps usam `- [ ]`.

**Goal:** Implementar os componentes que se geram automaticamente a partir dos metadados coletados na Fase 3: Sumário, Lista de Ilustrações, Lista de Tabelas, Lista de Quadros, Lista de Gráficos, Lista de Listagens, Lista de Abreviaturas e Siglas, Lista de Símbolos.

**Tech Stack:** Java 21, Spring Boot, docx4j 11.5.13, JUnit 5

**Característica arquitetural desta fase:** Os renderers desta fase consomem `BodyContentMetadata` produzido por `BodyContentRenderer`. O `DocumentRenderer` precisa expor esses metadados para que os renderers de índice possam acessá-los durante a renderização do documento.

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
- `ListOfFiguresRequest.java`, `ListOfTablesRequest.java`, etc.
- `IndexListComponentRuleRequest.java`
- `ListOfAbbreviationsRequest.java`, `ListOfAbbreviationsComponentRuleRequest.java`
- `ListOfSymbolsRequest.java`, `SymbolEntryRequest.java`, `ListOfSymbolsComponentRuleRequest.java`

### Modificados
- `DocumentRenderer.java` — disponibiliza `BodyContentMetadata` para renderers de índice
- `abnt-unip-profile.json` — regras e estilos de todos os novos componentes
- `ExportDocxRequest.java` (ou equivalente) — aceita os novos componentes no request

---

## Task 1 — Disponibilizar metadados para renderers de índice

O `DocumentRenderer` já armazena `BodyContentMetadata` (Task 4 da Fase 3). Agora os renderers de índice precisam acessá-la.

**Estratégia:** Interface `MetadataConsumingRenderer` com método `setMetadata(BodyContentMetadata)`. O `DocumentRenderer` chama `setMetadata` antes de renderizar cada componente de índice.

**⚠️ Thread-safety:** Os renderers de índice são beans Spring singleton. `setMetadata` como método mutador de instância criaria race condition em requests concorrentes. A solução é **não usar `setMetadata` como mutador de estado de instância**, mas sim como parâmetro adicional em `render`. Duas abordagens viáveis:

1. **Abordagem A (recomendada):** Alterar `MetadataConsumingRenderer` para ter `render(T component, DocumentProfile profile, BodyContentMetadata metadata)` em vez de `setMetadata + render`. O `DocumentRenderer` chama esse método diretamente quando detecta a interface.

2. **Abordagem B:** Criar um wrapper não-singleton por request que encapsula o renderer singleton e injeta os metadados. Mais complexo.

**Adotar Abordagem A** — é mais simples e alinhada com o padrão de "parâmetros explícitos":

```java
// MetadataConsumingRenderer
public interface MetadataConsumingRenderer<T extends DocumentComponent>
        extends ComponentRenderer<T> {

    List<DocxBlock> renderWithMetadata(T component, DocumentProfile profile, BodyContentMetadata metadata);

    // render() sem metadados retorna lista vazia ou lança — não deve ser chamado diretamente
    @Override
    default List<DocxBlock> render(T component, DocumentProfile profile) {
        return renderWithMetadata(component, profile, BodyContentMetadata.empty());
    }
}
```

O `DocumentRenderer` usa `renderComponentWithMetadata` (que aceita `DocumentComponent` e faz o cast) quando o renderer implementa `MetadataConsumingRenderer`.

**Files:**
- Create: `MetadataConsumingRenderer.java`
- Modify: `DocumentRenderer.java`

- [ ] **Step 1: Criar `MetadataConsumingRenderer`**

**Abordagem A (thread-safe, sem estado mutável de instância):** o método de render recebe os metadados como parâmetro explícito. `DocumentRenderer` detecta a interface e passa os metadados coletados do `BodyContentRenderer`. O método `render(T, DocumentProfile)` herdado de `ComponentRenderer` delega para `renderWithMetadata` com metadados vazios — nunca deve ser chamado diretamente pelo `DocumentRenderer` quando a interface é presente.

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

- [ ] **Step 2: Atualizar `DocumentRenderer` para passar metadados como parâmetro**

No loop `for (String componentId : componentOrder)`, substituir o bloco `if (component != null)` existente:

```java
if (component != null) {
    Optional<DocxPageNumbering> pageNumbering = pageNumberingState.beforeRendering(componentId);
    if (blocks.isEmpty() && pageNumbering.isPresent()) {
        initialPageNumbering = pageNumbering;
    }

    ComponentRenderer<?> renderer = rendererRegistry.get(componentId);
    List<DocxBlock> componentBlocks;

    if (renderer instanceof MetadataEmittingRenderer<?> emitting) {
        // Fase 3: BodyContentRenderer — coleta metadados e retorna blocks
        BodyContentRenderResult result = emitting.renderComponentWithMetadata(component, command.profile());
        componentBlocks = result.blocks();
        bodyContentMetadata = result.metadata();  // atualizar variável local
    } else if (renderer instanceof MetadataConsumingRenderer<?> consuming) {
        // Fase 5: renderers de índice — recebem metadados como parâmetro (thread-safe)
        componentBlocks = consuming.renderComponentWithMetadata(component, command.profile(), bodyContentMetadata);
    } else {
        componentBlocks = renderer.renderComponent(component, command.profile());
    }

    addBlocks(blocks, pageNumbering, componentBlocks);
    pageNumberingState.afterRendering();
}
```

Adicionar no topo do método `render(ExportDocxCommand command)`, antes do loop:

```java
BodyContentMetadata bodyContentMetadata = BodyContentMetadata.empty();  // variável local — sem race condition
```

Adicionar imports em `DocumentRenderer.java`:
```java
import com.abntbuilder.formatter.rendering.component.MetadataConsumingRenderer;
import com.abntbuilder.formatter.rendering.component.MetadataEmittingRenderer;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyContentMetadata;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyContentRenderResult;
```

**⚠️ Atenção:** O `bodyContentMetadata` é variável local ao método `render()` — uma instância por request, sem campo de instância em `DocumentRenderer`. Isso garante thread-safety no singleton Spring.

- [ ] **Step 3: Compilar**

```bash
cd /mnt/c/Users/evelynnd/Documents/Projetos/Formatter-service
mvn compile -q
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/abntbuilder/formatter/rendering/component/MetadataConsumingRenderer.java \
        src/main/java/com/abntbuilder/formatter/rendering/document/DocumentRenderer.java
git commit -m "feat: add MetadataConsumingRenderer interface and inject metadata in DocumentRenderer"
```

---

## Task 2 — Sumário

O sumário usa o campo TOC do Word para geração automática de números de página. O Word atualiza os números quando o usuário abre o documento e pressiona Ctrl+A → F9. Isso é documentado como comportamento esperado — o formatter não promete números de página sem que o usuário atualize o campo.

**Files:**
- Create: `SummaryComponent.java`, `SummaryComponentRule.java`, `SummaryRenderer.java`
- Create: `SummaryRequest.java`, `SummaryComponentRuleRequest.java`
- Create: `DocxTocBlock.java` (novo DocxBlock para campo TOC)

- [ ] **Step 1: Criar `SummaryComponent`**

Adicionar `SUMMARY` ao enum `ComponentType` em `com.abntbuilder.formatter.document.component.ComponentType`:

```java
// Linha existente (exemplo): GLOSSARY
// Adicionar após o último valor:
SUMMARY
```

```java
// src/main/java/com/abntbuilder/formatter/document/component/summary/SummaryComponent.java
package com.abntbuilder.formatter.document.component.summary;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

// Sem campos de conteúdo — o conteúdo vem dos metadados.
public record SummaryComponent() implements DocumentComponent {
    @Override public ComponentType type() { return ComponentType.SUMMARY; }
}
```

- [ ] **Step 2: Criar `SummaryComponentRule`**

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
        List<String> entryStyleIdsByLevel,  // estilo por nível (level 1, 2, ...)
        boolean useTocField                 // true = campo TOC do Word; false = entradas estáticas
) implements ComponentRule {
    public SummaryComponentRule {
        if (componentId == null || componentId.isBlank()) throw new IllegalArgumentException("componentId must not be blank.");
        if (headingStyleId == null || headingStyleId.isBlank()) throw new IllegalArgumentException("headingStyleId must not be blank.");
        if (headingText == null || headingText.isBlank()) throw new IllegalArgumentException("headingText must not be blank.");
        if (entryStyleIdsByLevel == null || entryStyleIdsByLevel.isEmpty())
            throw new IllegalArgumentException("entryStyleIdsByLevel must not be empty.");
        entryStyleIdsByLevel = List.copyOf(entryStyleIdsByLevel);
    }
    @Override public String componentId() { return componentId; }
    @Override public Map<String, String> contentBindings() { return Map.of(); }
}
```

- [ ] **Step 3: Criar `DocxTocBlock`**

O campo TOC do Word é emitido como três runs encadeados (`fldChar BEGIN`, `instrText`, `fldChar END`) dentro de um parágrafo. Criar um `DocxBlock` especializado para que o writer o materialize corretamente:

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

Atualizar `DocxBlock`:

```java
public sealed interface DocxBlock
    permits DocxParagraph, DocxPageBreak, DocxBlankLine, DocxSectionBreak,
            DocxImageBlock, DocxTableBlock, DocxListItemParagraph, DocxTocBlock {
}
```

- [ ] **Step 4: Implementar `DocxTocBlock` no `Docx4jWriter`**

```java
case DocxTocBlock tocBlock -> {
    P p = objectFactory.createP();
    PPr pPr = createParagraphProperties(tocBlock.styleRule(), Optional.empty(), Optional.empty(), Optional.empty());
    p.setPPr(pPr);

    // Run 1: BEGIN
    R beginRun = objectFactory.createR();
    FldChar beginFldChar = objectFactory.createFldChar();
    beginFldChar.setFldCharType(STFldCharType.BEGIN);
    beginFldChar.setDirty(true);  // forçar atualização ao abrir
    beginRun.getContent().add(objectFactory.createRFldChar(beginFldChar));
    p.getContent().add(beginRun);

    // Run 2: instrução
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

Imports: `org.docx4j.wml.FldChar`, `org.docx4j.wml.STFldCharType`

- [ ] **Step 5: Criar `SummaryRenderer`**

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
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;
import com.abntbuilder.formatter.rendering.component.MetadataConsumingRenderer;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyContentMetadata;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodySectionMetadata;

import java.util.ArrayList;
import java.util.List;

public final class SummaryRenderer
        implements MetadataConsumingRenderer<SummaryComponent> {

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
            String tocInstruction = "TOC \\o \"1-" + rule.entryStyleIdsByLevel().size() + "\" \\h \\z \\u";
            blocks.add(new DocxTocBlock(headingStyle, tocInstruction));
        } else {
            for (BodySectionMetadata section : metadata.sections()) {
                int level = section.level();
                String styleId = level <= rule.entryStyleIdsByLevel().size()
                        ? rule.entryStyleIdsByLevel().get(level - 1)
                        : rule.entryStyleIdsByLevel().get(rule.entryStyleIdsByLevel().size() - 1);
                StyleRule entryStyle = styleResolver.resolve(styleId);
                blocks.add(new DocxParagraph(
                        List.of(DocxRun.of(section.renderedTitle(), entryStyle)), entryStyle
                ));
            }
        }

        return List.copyOf(blocks);
    }
}
```

- [ ] **Step 6: Criar requests, registrar renderer, adicionar ao perfil e componentOrder**

`SummaryRequest` — record vazio (sem campos de conteúdo):

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/SummaryRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.summary.SummaryComponent;

public record SummaryRequest() {
    public SummaryComponent toDomain() { return new SummaryComponent(); }
}
```

`SummaryComponentRuleRequest`:

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/SummaryComponentRuleRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.summary.SummaryComponentRule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SummaryComponentRuleRequest(
        @NotBlank String headingStyleId,
        @NotBlank String headingText,
        @NotEmpty List<String> entryStyleIdsByLevel,
        boolean useTocField
) {
    public SummaryComponentRule toDomain(String componentId) {
        return new SummaryComponentRule(componentId, headingStyleId, headingText, entryStyleIdsByLevel, useTocField);
    }
}
```

Em `DocumentContentRequest` — adicionar campo e mapeamento:

```java
// campo no record:
@Valid SummaryRequest summary,

// em toComponents():
if (summary != null) {
    components.add(summary.toDomain());
}
```

Em `RenderingConfig` — adicionar `@Bean`:

```java
@Bean
public SummaryRenderer summaryRenderer() {
    return new SummaryRenderer();
}
```

Perfil JSON:

```json
"summary": {
  "componentId": "summary",
  "headingStyleId": "summary.heading",
  "headingText": "SUMÁRIO",
  "entryStyleIdsByLevel": [
    "summary.entry.level1",
    "summary.entry.level2",
    "summary.entry.level3"
  ],
  "useTocField": true
}
```

- [ ] **Step 7: Escrever `SummaryRendererTest`**

```java
@SpringBootTest
@AutoConfigureMockMvc
class SummaryRendererTest {

    @Test
    void shouldGenerateDocxWithTocField() throws Exception {
        // Gerar DOCX com bodyContent + summary
        // Verificar que document.xml contém instrução TOC
        String json = Files.readString(Path.of("docs/samples/summary/summary-with-toc.json"));
        byte[] docxBytes = /* perform request */;
        Document wordDoc = extractWordDocument(docxBytes);
        NodeList instrNodes = wordDoc.getElementsByTagNameNS(
                "http://schemas.openxmlformats.org/wordprocessingml/2006/main", "instrText"
        );
        boolean hasToc = false;
        for (int i = 0; i < instrNodes.getLength(); i++) {
            if (instrNodes.item(i).getTextContent().contains("TOC")) {
                hasToc = true;
                break;
            }
        }
        assertThat(hasToc).isTrue();
    }
}
```

- [ ] **Step 8: Criar sample**

```
docs/samples/summary/summary-with-toc.json   — bodyContent com 3 seções + summary
```

- [ ] **Step 9: Compilar e rodar**

```bash
mvn compile -q && mvn test -pl . -Dtest=SummaryRendererTest -q
```

- [ ] **Step 10: Commit**

```bash
git add -A
git commit -m "feat: add Summary component with TOC field generation"
```

---

## Task 3 — Listas de Display Objects (Figuras, Tabelas, Quadros, Gráficos, Listagens)

Todos os cinco seguem o mesmo padrão — diferem apenas no tipo de metadado consumido e no texto do heading.

**Padrão compartilhado:** `IndexListComponentRule` e `IndexListComponentRuleRequest`.

**Files:**
- Create: `IndexListComponentRule.java`, `IndexListComponentRuleRequest.java`
- Create: `ListOfFiguresComponent.java`, `ListOfTablesComponent.java`, `ListOfFramesComponent.java`, `ListOfChartsComponent.java`, `ListOfCodeListingsComponent.java`
- Create: (uma renderer para cada, todos implementam `MetadataConsumingRenderer`)

- [ ] **Step 1: Criar `IndexListComponentRule`**

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
        String entryTemplate  // ex: "{number} — {caption}" — declarado no perfil
) implements ComponentRule {
    public IndexListComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(headingStyleId, "headingStyleId");
        requireNonBlank(headingText, "headingText");
        requireNonBlank(entryStyleId, "entryStyleId");
        requireNonBlank(entryTemplate, "entryTemplate");
        if (!entryTemplate.contains("{number}") || !entryTemplate.contains("{caption}")) {
            throw new InvalidProfileStructureException(
                    "indexList.entryTemplate must contain {number} and {caption}."
            );
        }
    }
    @Override public Map<String, String> contentBindings() { return Map.of(); }
    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
```

- [ ] **Step 2: Criar os cinco componentes (sem campos de conteúdo)**

Adicionar os cinco valores ao enum `ComponentType`:

```java
// Adicionar após SUMMARY (ou ao final dos valores existentes):
LIST_OF_FIGURES,
LIST_OF_TABLES,
LIST_OF_FRAMES,
LIST_OF_CHARTS,
LIST_OF_CODE_LISTINGS
```

```java
// src/main/java/com/abntbuilder/formatter/document/component/listoffigures/ListOfFiguresComponent.java
package com.abntbuilder.formatter.document.component.listoffigures;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

public record ListOfFiguresComponent() implements DocumentComponent {
    @Override public ComponentType type() { return ComponentType.LIST_OF_FIGURES; }
}
```

```java
// src/main/java/com/abntbuilder/formatter/document/component/listoftables/ListOfTablesComponent.java
package com.abntbuilder.formatter.document.component.listoftables;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

public record ListOfTablesComponent() implements DocumentComponent {
    @Override public ComponentType type() { return ComponentType.LIST_OF_TABLES; }
}
```

```java
// src/main/java/com/abntbuilder/formatter/document/component/listofframes/ListOfFramesComponent.java
package com.abntbuilder.formatter.document.component.listofframes;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

public record ListOfFramesComponent() implements DocumentComponent {
    @Override public ComponentType type() { return ComponentType.LIST_OF_FRAMES; }
}
```

```java
// src/main/java/com/abntbuilder/formatter/document/component/listofcharts/ListOfChartsComponent.java
package com.abntbuilder.formatter.document.component.listofcharts;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

public record ListOfChartsComponent() implements DocumentComponent {
    @Override public ComponentType type() { return ComponentType.LIST_OF_CHARTS; }
}
```

```java
// src/main/java/com/abntbuilder/formatter/document/component/listofcodelistings/ListOfCodeListingsComponent.java
package com.abntbuilder.formatter.document.component.listofcodelistings;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

public record ListOfCodeListingsComponent() implements DocumentComponent {
    @Override public ComponentType type() { return ComponentType.LIST_OF_CODE_LISTINGS; }
}
```

- [ ] **Step 3: Criar um renderer base por composição**

Para evitar duplicação, criar `AbstractIndexListRenderer` (classe abstrata ou helper):

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
                List.of(DocxRun.of(rule.headingText(), headingStyle)), headingStyle
        ));

        List<BodyDisplayObjectMetadata> items = metadataExtractor().apply(metadata);
        for (BodyDisplayObjectMetadata item : items) {
            // Formato declarado no perfil via entryTemplate, ex: "{number} — {caption}"
            String text = rule.entryTemplate()
                    .replace("{number}", String.valueOf(item.number()))
                    .replace("{caption}", item.caption());
            blocks.add(new DocxParagraph(List.of(DocxRun.of(text, entryStyle)), entryStyle));
        }

        return List.copyOf(blocks);
    }
}
```

- [ ] **Step 4: Criar os cinco renderers concretos**

```java
// ListOfFiguresRenderer.java
public final class ListOfFiguresRenderer
        extends AbstractIndexListRenderer<ListOfFiguresComponent> {
    public static final String COMPONENT_ID = "listOfFigures";
    @Override public String componentId() { return COMPONENT_ID; }
    @Override public Class<ListOfFiguresComponent> componentType() { return ListOfFiguresComponent.class; }
    @Override protected Function<BodyContentMetadata, List<BodyDisplayObjectMetadata>> metadataExtractor() {
        return BodyContentMetadata::figures;
    }
}

// ListOfTablesRenderer — uses ::tables
// ListOfFramesRenderer — uses ::frames
// ListOfChartsRenderer — uses ::charts
// ListOfCodeListingsRenderer — uses ::codeListings
```

- [ ] **Step 5: Criar requests e rule requests**

Todos os cinco são records vazios com `toDomain()`:

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/ListOfFiguresRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.listoffigures.ListOfFiguresComponent;

public record ListOfFiguresRequest() {
    public ListOfFiguresComponent toDomain() { return new ListOfFiguresComponent(); }
}
```

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/ListOfTablesRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.listoftables.ListOfTablesComponent;

public record ListOfTablesRequest() {
    public ListOfTablesComponent toDomain() { return new ListOfTablesComponent(); }
}
```

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/ListOfFramesRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.listofframes.ListOfFramesComponent;

public record ListOfFramesRequest() {
    public ListOfFramesComponent toDomain() { return new ListOfFramesComponent(); }
}
```

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/ListOfChartsRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.listofcharts.ListOfChartsComponent;

public record ListOfChartsRequest() {
    public ListOfChartsComponent toDomain() { return new ListOfChartsComponent(); }
}
```

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/ListOfCodeListingsRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.listofcodelistings.ListOfCodeListingsComponent;

public record ListOfCodeListingsRequest() {
    public ListOfCodeListingsComponent toDomain() { return new ListOfCodeListingsComponent(); }
}
```

Em `DocumentContentRequest` — adicionar campos e mapeamentos para todos os cinco:

```java
// campos no record:
@Valid ListOfFiguresRequest listOfFigures,
@Valid ListOfTablesRequest listOfTables,
@Valid ListOfFramesRequest listOfFrames,
@Valid ListOfChartsRequest listOfCharts,
@Valid ListOfCodeListingsRequest listOfCodeListings,

// em toComponents():
if (listOfFigures != null) components.add(listOfFigures.toDomain());
if (listOfTables != null) components.add(listOfTables.toDomain());
if (listOfFrames != null) components.add(listOfFrames.toDomain());
if (listOfCharts != null) components.add(listOfCharts.toDomain());
if (listOfCodeListings != null) components.add(listOfCodeListings.toDomain());
```

Em `RenderingConfig` — adicionar um `@Bean` para cada renderer:

```java
@Bean
public ListOfFiguresRenderer listOfFiguresRenderer() {
    return new ListOfFiguresRenderer();
}

@Bean
public ListOfTablesRenderer listOfTablesRenderer() {
    return new ListOfTablesRenderer();
}

@Bean
public ListOfFramesRenderer listOfFramesRenderer() {
    return new ListOfFramesRenderer();
}

@Bean
public ListOfChartsRenderer listOfChartsRenderer() {
    return new ListOfChartsRenderer();
}

@Bean
public ListOfCodeListingsRenderer listOfCodeListingsRenderer() {
    return new ListOfCodeListingsRenderer();
}
```

`IndexListComponentRuleRequest`:

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/IndexListComponentRuleRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.indexlist.IndexListComponentRule;
import jakarta.validation.constraints.NotBlank;

public record IndexListComponentRuleRequest(
        @NotBlank String headingStyleId,
        @NotBlank String headingText,
        @NotBlank String entryStyleId,
        @NotBlank String entryTemplate  // ex: "{number} — {caption}"
) {
    public IndexListComponentRule toDomain(String componentId) {
        return new IndexListComponentRule(componentId, headingStyleId, headingText, entryStyleId, entryTemplate);
    }
}
```

- [ ] **Step 6: Registrar os cinco renderers**

- [ ] **Step 7: Adicionar ao perfil JSON**

```json
"listOfFigures": {
  "componentId": "listOfFigures",
  "headingStyleId": "list.heading",
  "headingText": "LISTA DE ILUSTRAÇÕES",
  "entryStyleId": "list.entry",
  "entryTemplate": "{number} — {caption}"
},
"listOfTables": {
  "componentId": "listOfTables",
  "headingText": "LISTA DE TABELAS",
  "entryTemplate": "{number} — {caption}",
  ...
},
"listOfFrames": {
  "componentId": "listOfFrames",
  "headingText": "LISTA DE QUADROS",
  "entryTemplate": "{number} — {caption}",
  ...
},
"listOfCharts": {
  "componentId": "listOfCharts",
  "headingText": "LISTA DE GRÁFICOS",
  "entryTemplate": "{number} — {caption}",
  ...
},
"listOfCodeListings": {
  "componentId": "listOfCodeListings",
  "headingText": "LISTA DE LISTAGENS",
  "entryTemplate": "{number} — {caption}",
  ...
}
```

- [ ] **Step 8: Criar samples**

```
docs/samples/list-of-figures/list-of-figures-with-figures.json
docs/samples/list-of-tables/list-of-tables-with-tables.json
```

(Cada sample inclui `bodyContent` com figuras/tabelas E o componente de lista — ambos no `selectedComponents`.)

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

- [ ] **Step 1: Criar `ListOfAbbreviationsComponent`**

Adicionar `LIST_OF_ABBREVIATIONS` ao enum `ComponentType`:

```java
// Adicionar após LIST_OF_CODE_LISTINGS:
LIST_OF_ABBREVIATIONS
```

```java
// src/main/java/com/abntbuilder/formatter/document/component/listofabbreviations/ListOfAbbreviationsComponent.java
package com.abntbuilder.formatter.document.component.listofabbreviations;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

public record ListOfAbbreviationsComponent() implements DocumentComponent {
    @Override public ComponentType type() { return ComponentType.LIST_OF_ABBREVIATIONS; }
}
```

- [ ] **Step 2: Criar `ListOfAbbreviationsComponentRule`**

```java
public record ListOfAbbreviationsComponentRule(
        String componentId,
        String headingStyleId,
        String headingText,
        String entryStyleId,
        String termSeparator,          // " — "
        boolean sortAlphabetically
) implements ComponentRule { ... }
```

- [ ] **Step 3: Criar `ListOfAbbreviationsRenderer`**

```java
public final class ListOfAbbreviationsRenderer
        implements MetadataConsumingRenderer<ListOfAbbreviationsComponent> {

    public static final String COMPONENT_ID = "listOfAbbreviations";

    @Override public String componentId() { return COMPONENT_ID; }
    @Override public Class<ListOfAbbreviationsComponent> componentType() { return ListOfAbbreviationsComponent.class; }

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
            abbreviations.sort(java.util.Comparator.comparing(BodyAbbreviationMetadata::abbreviation));
        }

        for (BodyAbbreviationMetadata abbr : abbreviations) {
            String text = abbr.abbreviation() + rule.termSeparator() + abbr.expansion();
            blocks.add(new DocxParagraph(List.of(DocxRun.of(text, entryStyle)), entryStyle));
        }

        return List.copyOf(blocks);
    }
}
```

- [ ] **Step 4: Request, registro, perfil, componentOrder, sample, teste, compilar, commit**

`ListOfAbbreviationsRequest` — record vazio:

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/ListOfAbbreviationsRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.listofabbreviations.ListOfAbbreviationsComponent;

public record ListOfAbbreviationsRequest() {
    public ListOfAbbreviationsComponent toDomain() { return new ListOfAbbreviationsComponent(); }
}
```

`ListOfAbbreviationsComponentRuleRequest`:

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/ListOfAbbreviationsComponentRuleRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.listofabbreviations.ListOfAbbreviationsComponentRule;
import jakarta.validation.constraints.NotBlank;

public record ListOfAbbreviationsComponentRuleRequest(
        @NotBlank String headingStyleId,
        @NotBlank String headingText,
        @NotBlank String entryStyleId,
        @NotBlank String termSeparator,
        boolean sortAlphabetically
) {
    public ListOfAbbreviationsComponentRule toDomain(String componentId) {
        return new ListOfAbbreviationsComponentRule(
                componentId, headingStyleId, headingText, entryStyleId, termSeparator, sortAlphabetically);
    }
}
```

Em `DocumentContentRequest` — adicionar campo e mapeamento:

```java
// campo no record:
@Valid ListOfAbbreviationsRequest listOfAbbreviations,

// em toComponents():
if (listOfAbbreviations != null) components.add(listOfAbbreviations.toDomain());
```

Em `RenderingConfig` — adicionar `@Bean`:

```java
@Bean
public ListOfAbbreviationsRenderer listOfAbbreviationsRenderer() {
    return new ListOfAbbreviationsRenderer();
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

Diferente das listas de abreviaturas, a lista de símbolos é fornecida explicitamente pelo usuário (não coletada automaticamente do texto), pois símbolos matemáticos e científicos não têm marcação inline padronizável.

**Files:**
- Create: `SymbolEntry.java`, `ListOfSymbolsComponent.java`
- Create: `ListOfSymbolsComponentRule.java`, `ListOfSymbolsComponentRuleRequest.java`
- Create: `ListOfSymbolsRenderer.java`
- Create: `ListOfSymbolsRequest.java`, `SymbolEntryRequest.java`

- [ ] **Step 1: Criar `SymbolEntry`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/listofsymbols/SymbolEntry.java
package com.abntbuilder.formatter.document.component.listofsymbols;

public record SymbolEntry(String symbol, String meaning) {
    public SymbolEntry {
        if (symbol == null || symbol.isBlank()) throw new IllegalArgumentException("symbol must not be blank.");
        if (meaning == null || meaning.isBlank()) throw new IllegalArgumentException("meaning must not be blank.");
    }
}
```

- [ ] **Step 2: Criar `ListOfSymbolsComponent`**

Adicionar `LIST_OF_SYMBOLS` ao enum `ComponentType`:

```java
// Adicionar após LIST_OF_ABBREVIATIONS:
LIST_OF_SYMBOLS
```

```java
// src/main/java/com/abntbuilder/formatter/document/component/listofsymbols/ListOfSymbolsComponent.java
package com.abntbuilder.formatter.document.component.listofsymbols;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;
import java.util.List;
import java.util.Objects;

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
        String headingText,
        String entryStyleId,
        String termSeparator  // " — "
) implements ComponentRule { ... }
```

- [ ] **Step 4: Criar `ListOfSymbolsRenderer`**

**Não** implementa `MetadataConsumingRenderer` — o conteúdo vem diretamente do componente.

```java
// src/main/java/com/abntbuilder/formatter/rendering/component/listofsymbols/ListOfSymbolsRenderer.java
package com.abntbuilder.formatter.rendering.component.listofsymbols;

import com.abntbuilder.formatter.document.component.listofsymbols.ListOfSymbolsComponent;
import com.abntbuilder.formatter.document.component.listofsymbols.SymbolEntry;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.listofsymbols.ListOfSymbolsComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;

import java.util.ArrayList;
import java.util.List;

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

- [ ] **Step 5: Criar `SymbolEntryRequest`**

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/SymbolEntryRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.listofsymbols.SymbolEntry;
import jakarta.validation.constraints.NotBlank;

public record SymbolEntryRequest(
        @NotBlank String symbol,
        @NotBlank String meaning
) {
    public SymbolEntry toDomain() {
        return new SymbolEntry(symbol, meaning);
    }
}
```

- [ ] **Step 6: Criar `ListOfSymbolsRequest`**

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/ListOfSymbolsRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.listofsymbols.ListOfSymbolsComponent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ListOfSymbolsRequest(
        @Valid @NotEmpty List<SymbolEntryRequest> entries
) {
    public ListOfSymbolsComponent toDomain() {
        return new ListOfSymbolsComponent(
                entries.stream().map(SymbolEntryRequest::toDomain).toList()
        );
    }
}
```

- [ ] **Step 7: Adicionar campo em `DocumentContentRequest`**

```java
// record DocumentContentRequest — adicionar campo:
@Valid ListOfSymbolsRequest listOfSymbols,

// método toComponents() — adicionar:
if (listOfSymbols != null) {
    components.add(listOfSymbols.toDomain());
}
```

- [ ] **Step 8: Registrar `ListOfSymbolsRenderer` em `RenderingConfig`**

```java
@Bean
public ListOfSymbolsRenderer listOfSymbolsRenderer() {
    return new ListOfSymbolsRenderer();
}
```

- [ ] **Step 9: Criar `ListOfSymbolsComponentRuleRequest`**

```java
public record ListOfSymbolsComponentRuleRequest(
        @NotBlank String headingStyleId,
        @NotBlank String headingText,
        @NotBlank String entryStyleId,
        @NotBlank String termSeparator
) {
    public ListOfSymbolsComponentRule toDomain(String componentId) {
        return new ListOfSymbolsComponentRule(componentId, headingStyleId, headingText, entryStyleId, termSeparator);
    }
}
```

- [ ] **Step 10: Adicionar ao perfil JSON e componentOrder**

```json
"listOfSymbols": {
  "componentId": "listOfSymbols",
  "headingStyleId": "list.heading",
  "headingText": "LISTA DE SÍMBOLOS",
  "entryStyleId": "list.entry",
  "termSeparator": " — "
}
```

- [ ] **Step 11: Criar sample**

```json
{
  "profileId": "abnt-unip-profile",
  "document": {
    "components": {
      "listOfSymbols": {
        "entries": [
          { "symbol": "α", "meaning": "Coeficiente de significância" },
          { "symbol": "σ", "meaning": "Desvio padrão" },
          { "symbol": "μ", "meaning": "Média populacional" }
        ]
      }
    }
  }
}
```

- [ ] **Step 12: Compilar e rodar**

```bash
mvn compile -q && mvn test -q
```

- [ ] **Step 13: Commit**

```bash
git add -A
git commit -m "feat: add ListOfSymbols component with explicit user-provided entries"
```

---

## Task 6 — `componentOrder` final e integração completa

- [ ] **Step 1: Atualizar `componentOrder` em `abnt-unip-profile.json`**

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

- [ ] **Step 2: Criar sample composto completo**

```
docs/samples/composed/documento-completo.json
```

Este sample inclui todos os componentes (cover, titlePage, approvalSheet, errata, dedication, acknowledgments, epigraph, resumo, abstract, listOfAbbreviations, listOfSymbols, summary, listOfFigures, listOfTables, bodyContent com figuras e tabelas, references, appendix, annex, glossary) com dados realistas de teste.

- [ ] **Step 3: Criar `DocumentoCompletoIntegrationTest`**

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        // Verificar presença do campo TOC
        Document wordDoc = extractWordDocument(docxBytes);
        NodeList instrNodes = wordDoc.getElementsByTagNameNS(
                "http://schemas.openxmlformats.org/wordprocessingml/2006/main", "instrText"
        );
        boolean hasToc = false;
        for (int i = 0; i < instrNodes.getLength(); i++) {
            if (instrNodes.item(i).getTextContent().contains("TOC")) { hasToc = true; break; }
        }
        assertThat(hasToc).as("document should contain TOC field").isTrue();
    }

    /**
     * Abre o arquivo DOCX (que é um ZIP) e faz o parse do XML de word/document.xml,
     * retornando o Document DOM para inspeção nos testes.
     */
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

- [ ] **Step 4: Suite completa**

```bash
mvn test -q
```

Esperado: todos os testes passam.

- [ ] **Step 5: Validação visual no Word**

Abrir o DOCX gerado pelo sample `documento-completo.json` e verificar:

- Sumário: campo TOC visível (mostra "Erro! Indicador não definido." até atualizar — comportamento esperado)
- Pressionar Ctrl+A → F9: sumário atualiza com os títulos e números de página corretos
- Lista de Ilustrações: entradas com "Figura N - Caption"
- Lista de Tabelas: entradas com "Tabela N - Caption"
- Lista de Abreviaturas: entradas em ordem alfabética
- Lista de Símbolos: entradas com símbolo e significado
- Pré-textuais (errata, dedicatória, epígrafe, agradecimentos, resumo, abstract) no local correto
- Corpo textual com numeração de páginas visível apenas a partir do bodyContent
- Referências formatadas por tipo ABNT
- Apêndices e Anexos com letras automáticas
- Glossário ao final

- [ ] **Step 6: Commit final**

```bash
git add -A
git commit -m "feat: complete Fase 5 — all index components and full academic document generation"
```

---

## Nota sobre números de página no TOC

O Word processa o campo TOC e insere números de página apenas quando o documento é aberto e o campo é atualizado (Ctrl+A → F9 ou ao abrir se o Word perguntar). O formatter não tem como calcular números de página sem abrir o documento — isso é uma limitação intrínseca do formato DOCX em geração offline.

Este comportamento deve ser documentado no README do projeto e nos samples:

```
// Nota no sample README:
// Após gerar o DOCX, abra no Word e pressione Ctrl+A → F9
// para atualizar o sumário e os números de página.
```

---

## Checklist de conclusão da Fase 5

| Componente | Fonte de dados | Task |
|---|---|---|
| Sumário com campo TOC do Word | `BodyContentMetadata.sections` | Task 2 |
| `DocxTocBlock` e suporte no writer | — | Task 2 |
| Lista de Ilustrações | `BodyContentMetadata.figures` | Task 3 |
| Lista de Tabelas | `BodyContentMetadata.tables` | Task 3 |
| Lista de Quadros | `BodyContentMetadata.frames` | Task 3 |
| Lista de Gráficos | `BodyContentMetadata.charts` | Task 3 |
| Lista de Listagens | `BodyContentMetadata.codeListings` | Task 3 |
| Lista de Abreviaturas e Siglas (ordem alfabética) | `BodyContentMetadata.abbreviations` | Task 4 |
| Lista de Símbolos (conteúdo explícito) | `ListOfSymbolsComponent.entries` | Task 5 |
| `MetadataConsumingRenderer` + injeção em `DocumentRenderer` | — | Task 1 |
| `componentOrder` final com todos os componentes | — | Task 6 |
| Sample `documento-completo.json` | — | Task 6 |
| Suite completa verde | — | Task 6 |
| TOC atualiza corretamente ao pressionar F9 no Word | — | Task 6 (validação visual) |
