# bodyContent Fase 3 — Metadados e Referências Cruzadas

> **Pré-requisito:** Fase 2 concluída — todos os tipos de bloco e inline existem, incluindo `BodyFrame`, `BodyCodeListing`, `BodyChart`, `BodyEquation`, `BodyAbbreviation`.
>
> **For agentic workers:** Use superpowers:subagent-driven-development. Steps usam `- [ ]`.

**Goal:** Instrumentar o renderer para emitir metadados de todas as séries numeradas (seções, figuras, tabelas, quadros, gráficos, listagens, siglas). Usar esses metadados para suportar referências cruzadas inline (`CROSS_REFERENCE`).

**Tech Stack:** Java 21, Spring Boot, JUnit 5

---

## Mapa de arquivos

### Novos
- `BodyContentMetadata.java`
- `BodySectionMetadata.java`
- `BodyDisplayObjectMetadata.java` (record genérico para figuras/tabelas/quadros/gráficos/listagens)
- `BodyAbbreviationMetadata.java`
- `BodyContentRenderResult.java`
- `MetadataEmittingRenderer.java` (interface)
- `BodyCrossReference.java`
- `CrossReferenceTargetType.java`
- `CrossReferenceDisplayMode.java`
- `BodyCrossReferenceRequest.java`
- `CrossReferenceLabelsRule.java` (regra de perfil)

### Modificados
- `BodyInline.java` — adiciona `BodyCrossReference`
- `BodyInlineType.java` — adiciona `CROSS_REFERENCE`
- `BodyInlineRequest.java` — adiciona caso CROSS_REFERENCE
- `BodyContentComponentRule.java` — adiciona `crossReferenceLabels`
- `BodyContentComponentRuleRequest.java` — campo correspondente
- `BodyContentRenderer.java` — implementa `MetadataEmittingRenderer`, emite `BodyContentRenderResult`, resolve referências cruzadas
- `DocumentRenderer.java` — detecta `MetadataEmittingRenderer`, armazena metadados em `DocumentRenderResult`
- `abnt-unip-profile.json` — adiciona `crossReferenceLabels`

---

## Task 1 — Tipos de metadados

**Files:**
- Create: `BodySectionMetadata.java`, `BodyDisplayObjectMetadata.java`, `BodyAbbreviationMetadata.java`, `BodyContentMetadata.java`, `BodyContentRenderResult.java`

- [ ] **Step 1: Criar `BodySectionMetadata`**

```java
// src/main/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodySectionMetadata.java
package com.abntbuilder.formatter.rendering.component.bodycontent;

public record BodySectionMetadata(
        String id,
        int level,
        String renderedTitle  // com prefixo numérico já aplicado, ex: "1 Introdução"
) {
    public BodySectionMetadata {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id must not be blank.");
        if (level < 1 || level > 6) throw new IllegalArgumentException("level must be between 1 and 6.");
        if (renderedTitle == null || renderedTitle.isBlank()) throw new IllegalArgumentException("renderedTitle must not be blank.");
    }
}
```

- [ ] **Step 2: Criar `BodyDisplayObjectMetadata`**

```java
// src/main/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyDisplayObjectMetadata.java
package com.abntbuilder.formatter.rendering.component.bodycontent;

public record BodyDisplayObjectMetadata(
        String id,
        int number,
        String caption
) {
    public BodyDisplayObjectMetadata {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id must not be blank.");
        if (number < 1) throw new IllegalArgumentException("number must be >= 1.");
        if (caption == null || caption.isBlank()) throw new IllegalArgumentException("caption must not be blank.");
    }
}
```

- [ ] **Step 3: Criar `BodyAbbreviationMetadata`**

```java
// src/main/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyAbbreviationMetadata.java
package com.abntbuilder.formatter.rendering.component.bodycontent;

public record BodyAbbreviationMetadata(
        String abbreviation,
        String expansion
) {
    public BodyAbbreviationMetadata {
        if (abbreviation == null || abbreviation.isBlank()) throw new IllegalArgumentException("abbreviation must not be blank.");
        if (expansion == null || expansion.isBlank()) throw new IllegalArgumentException("expansion must not be blank.");
    }
}
```

- [ ] **Step 4: Criar `BodyContentMetadata`**

```java
// src/main/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyContentMetadata.java
package com.abntbuilder.formatter.rendering.component.bodycontent;

import java.util.List;
import java.util.Objects;

public record BodyContentMetadata(
        List<BodySectionMetadata> sections,
        List<BodyDisplayObjectMetadata> figures,
        List<BodyDisplayObjectMetadata> tables,
        List<BodyDisplayObjectMetadata> frames,
        List<BodyDisplayObjectMetadata> charts,
        List<BodyDisplayObjectMetadata> codeListings,
        List<BodyAbbreviationMetadata> abbreviations
) {
    public BodyContentMetadata {
        Objects.requireNonNull(sections, "sections must not be null");
        Objects.requireNonNull(figures, "figures must not be null");
        Objects.requireNonNull(tables, "tables must not be null");
        Objects.requireNonNull(frames, "frames must not be null");
        Objects.requireNonNull(charts, "charts must not be null");
        Objects.requireNonNull(codeListings, "codeListings must not be null");
        Objects.requireNonNull(abbreviations, "abbreviations must not be null");
        sections = List.copyOf(sections);
        figures = List.copyOf(figures);
        tables = List.copyOf(tables);
        frames = List.copyOf(frames);
        charts = List.copyOf(charts);
        codeListings = List.copyOf(codeListings);
        abbreviations = List.copyOf(abbreviations);
    }

    public static BodyContentMetadata empty() {
        return new BodyContentMetadata(
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of()
        );
    }
}
```

- [ ] **Step 5: Criar `BodyContentRenderResult`**

```java
// src/main/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyContentRenderResult.java
package com.abntbuilder.formatter.rendering.component.bodycontent;

import com.abntbuilder.formatter.output.docx.api.DocxBlock;

import java.util.List;
import java.util.Objects;

public record BodyContentRenderResult(
        List<DocxBlock> blocks,
        BodyContentMetadata metadata
) {
    public BodyContentRenderResult {
        Objects.requireNonNull(blocks, "blocks must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");
        blocks = List.copyOf(blocks);
    }
}
```

- [ ] **Step 6: Compilar**

```bash
cd /mnt/c/Users/evelynnd/Documents/Projetos/Formatter-service
mvn compile -q
```

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodySectionMetadata.java \
        src/main/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyDisplayObjectMetadata.java \
        src/main/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyAbbreviationMetadata.java \
        src/main/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyContentMetadata.java \
        src/main/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyContentRenderResult.java
git commit -m "feat: add BodyContentMetadata and related types"
```

---

## Task 2 — `MetadataEmittingRenderer` interface

**Files:**
- Create: `MetadataEmittingRenderer.java`
- Modify: `BodyContentRenderer.java` — implementa a interface

- [ ] **Step 1: Criar `MetadataEmittingRenderer`**

```java
// src/main/java/com/abntbuilder/formatter/rendering/component/MetadataEmittingRenderer.java
package com.abntbuilder.formatter.rendering.component;

import com.abntbuilder.formatter.document.component.DocumentComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyContentRenderResult;

import java.util.List;

public interface MetadataEmittingRenderer<T extends DocumentComponent>
        extends ComponentRenderer<T> {

    BodyContentRenderResult renderWithMetadata(T component, DocumentProfile profile);

    @Override
    default List<DocxBlock> render(T component, DocumentProfile profile) {
        return renderWithMetadata(component, profile).blocks();
    }
}
```

- [ ] **Step 2: Atualizar a assinatura de `BodyContentRenderer`**

Mudar a declaração da classe:

```java
public final class BodyContentRenderer
        implements MetadataEmittingRenderer<BodyContentComponent> {
```

**Importante:** A interface `MetadataEmittingRenderer` fornece um `default render()` que delega para `renderWithMetadata`. Portanto **não deve existir** um método `render` na classe `BodyContentRenderer` — o método a implementar é `renderWithMetadata`. Se o método `render` existir na classe, ele vai sobrescrever o default da interface e não haverá delegação automática.

Renomear o método existente `render(BodyContentComponent, DocumentProfile)` para `renderWithMetadata`, ajustando o retorno de `List<DocxBlock>` para `BodyContentRenderResult`.

O esqueleto completo do método após esta Task (antes de a Task 3 preencher os acumuladores) é:

```java
@Override
public BodyContentRenderResult renderWithMetadata(
        BodyContentComponent component, DocumentProfile profile) {

    BodyContentComponentRule rule = profile.ruleFor(component);
    StyleResolver styleResolver = StyleResolver.from(profile, rule.styleMapping());

    // Estado de numeração — inalterado em relação ao render() anterior
    SectionNumberingState numberingState = new SectionNumberingState(rule.numbering());
    DisplayObjectRenderingState<BodyFigure> figureRenderingState =
            new DisplayObjectRenderingState<>(figuresFrom(component.sections()));
    DisplayObjectRenderingState<BodyTable> tableRenderingState =
            new DisplayObjectRenderingState<>(tablesFrom(component.sections()));
    // (demais DisplayObjectRenderingState para Fase 2: frames, charts, codeListings)

    // Acumuladores de metadados — inicializados aqui, preenchidos na Task 3
    List<BodySectionMetadata> sectionMetas = new ArrayList<>();
    List<BodyDisplayObjectMetadata> figureMetas = new ArrayList<>();
    List<BodyDisplayObjectMetadata> tableMetas = new ArrayList<>();
    List<BodyDisplayObjectMetadata> frameMetas = new ArrayList<>();
    List<BodyDisplayObjectMetadata> chartMetas = new ArrayList<>();
    List<BodyDisplayObjectMetadata> codeListingMetas = new ArrayList<>();
    List<BodyAbbreviationMetadata> abbreviationMetas = new ArrayList<>();

    // Acumulador de blocks de saída — inalterado
    List<DocxBlock> blocks = new ArrayList<>();

    // Loop principal sobre seções e blocos — detalhado na Task 3, mantido sem alteração aqui
    for (BodySection section : component.sections()) {
        // ... renderização de seções e blocos (ver Task 3)
    }

    // Retorno com metadados — substituir o antigo return List.copyOf(blocks)
    BodyContentMetadata metadata = new BodyContentMetadata(
            List.copyOf(sectionMetas),
            List.copyOf(figureMetas),
            List.copyOf(tableMetas),
            List.copyOf(frameMetas),
            List.copyOf(chartMetas),
            List.copyOf(codeListingMetas),
            List.copyOf(abbreviationMetas)
    );
    return new BodyContentRenderResult(List.copyOf(blocks), metadata);
    // Nesta Task, os acumuladores ainda estão vazios — preenchidos na Task 3
}
```

> **Atenção:** O retorno com `BodyContentMetadata.empty()` usado como passo temporário na revisão anterior deve ser substituído pelo retorno acima com os acumuladores já declarados (mesmo que ainda vazios). Isso garante que a Task 3 apenas preencha os acumuladores sem alterar a estrutura do método.

Verificar que nenhum `@Override public List<DocxBlock> render(...)` permanece na classe.

- [ ] **Step 3: Compilar e rodar suite para garantir que nada quebrou**

```bash
mvn compile -q && mvn test -q
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/abntbuilder/formatter/rendering/component/MetadataEmittingRenderer.java \
        src/main/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyContentRenderer.java
git commit -m "feat: extract MetadataEmittingRenderer interface and migrate BodyContentRenderer"
```

---

## Task 3 — Emissão de metadados no renderer

**Files:**
- Modify: `BodyContentRenderer.java`

O renderer já acumula estado via `SectionNumberingState` e `DisplayObjectRenderingState`. Agora precisa também acumular metadados.

- [ ] **Step 1: Adicionar acumuladores de metadados no método `renderWithMetadata`**

```java
List<BodySectionMetadata> sectionMetas = new ArrayList<>();
List<BodyDisplayObjectMetadata> figureMetas = new ArrayList<>();
List<BodyDisplayObjectMetadata> tableMetas = new ArrayList<>();
List<BodyDisplayObjectMetadata> frameMetas = new ArrayList<>();
List<BodyDisplayObjectMetadata> chartMetas = new ArrayList<>();
List<BodyDisplayObjectMetadata> codeListingMetas = new ArrayList<>();
List<BodyAbbreviationMetadata> abbreviationMetas = new ArrayList<>();
```

- [ ] **Step 2: Capturar metadados de seções**

Após calcular o título renderizado da seção:

```java
String renderedTitle = numberingState.resolveTitle(section.level(), section.title().orElseThrow());
// ...
sectionMetas.add(new BodySectionMetadata(section.id(), section.level(), renderedTitle));
```

- [ ] **Step 3: Capturar metadados de display objects no caller (não nos métodos privados estáticos)**

Os métodos `renderFigure`, `renderTable` etc. são `private static` — não têm acesso às listas de metadados. A coleta ocorre no caller, em `renderWithMetadata`, logo após a chamada que retorna os blocks.

O padrão é: chamar `nextPart()` **antes** de delegar para `renderFigure`, capturar o número, e acumular o metadado. Para isso, extrair a chamada a `nextPart()` do interior de `renderFigure` para o caller:

**Mudar a assinatura de `renderFigure` para receber `part` como parâmetro:**

```java
private static List<DocxBlock> renderFigure(
        BodyFigure figure,
        FigureRule rule,
        StyleResolver styleResolver,
        DisplayObjectRenderingState<BodyFigure> figureRenderingState,
        DisplayObjectContinuationPart part  // receber de fora, não calcular internamente
) { ... }
```

**No caller em `renderWithMetadata`, ao processar cada bloco:**

```java
case BodyFigure figure -> {
    DisplayObjectContinuationPart part = figureRenderingState.nextPart(figure, rule.figure().continuationLabels());
    // Acumular metadado usando o número já calculado
    if (part.number() > 0) {  // apenas na primeira aparição do id (part.number() é o número sequencial global)
        figureMetas.add(new BodyDisplayObjectMetadata(figure.id(), part.number(), figure.caption()));
    }
    yield renderFigure(figure, rule.figure(), styleResolver, figureRenderingState, part);
}
```

Fazer o mesmo para `renderTable`, `renderFrame`, `renderChart`, `renderCodeListing` — cada um recebe `part` como parâmetro e o caller acumula o metadado antes de chamar o método.

**Atenção:** `figureMetas` deve registrar apenas a primeira aparição de cada `id` (primeira parte do grupo de continuação), não todas as partes. Adicionar filtro:

```java
boolean isFirstPart = part.continuationLabel().isEmpty();  // primeira parte não tem label de continuação
if (isFirstPart) {
    figureMetas.add(new BodyDisplayObjectMetadata(figure.id(), part.number(), figure.caption()));
}
```

- [ ] **Step 4: Capturar metadados de siglas**

No `toDocxRun`, quando o inline for `BodyAbbreviation`, registrar a primeira ocorrência. Para que `toDocxRun` tenha acesso a `abbreviationMetas`, passar a lista como parâmetro (preferível a transformar em método de instância, pois evita estado mutável implícito).

**Assinatura atualizada de `toDocxRun`:**

```java
private static DocxRun toDocxRun(
        BodyInline inline,
        StyleRule baseStyle,
        BodyContentComponentRule rule,
        StyleResolver styleResolver,
        List<BodyAbbreviationMetadata> abbreviationMetas  // novo parâmetro
) { ... }
```

**Case de `BodyAbbreviation` dentro de `toDocxRun`:**

```java
case BodyAbbreviation abbr -> {
    // registrar apenas a primeira ocorrência de cada abreviatura
    if (abbreviationMetas.stream().noneMatch(m -> m.abbreviation().equals(abbr.abbreviation()))) {
        abbreviationMetas.add(new BodyAbbreviationMetadata(abbr.abbreviation(), abbr.expansion()));
    }
    yield new DocxRun(abbr.renderedText(), baseStyle, InlineFormatting.none());
}
```

**Como é chamado em `renderContentBlock`** (o método que itera os inlines de um parágrafo/bloco):

```java
private static List<DocxRun> renderContentBlock(
        List<BodyInline> inlines,
        StyleRule baseStyle,
        BodyContentComponentRule rule,
        StyleResolver styleResolver,
        List<BodyAbbreviationMetadata> abbreviationMetas  // propagado do caller
) {
    return inlines.stream()
            .map(inline -> toDocxRun(inline, baseStyle, rule, styleResolver, abbreviationMetas))
            .toList();
}
```

`renderContentBlock` é chamado a partir do loop em `renderWithMetadata`, que possui `abbreviationMetas` declarado no topo — portanto a lista mutável é passada por referência e acumulada ao longo de toda a renderização.

- [ ] **Step 5: Construir e retornar `BodyContentMetadata`**

Ao final do método `renderWithMetadata`:

```java
BodyContentMetadata metadata = new BodyContentMetadata(
        List.copyOf(sectionMetas),
        List.copyOf(figureMetas),
        List.copyOf(tableMetas),
        List.copyOf(frameMetas),
        List.copyOf(chartMetas),
        List.copyOf(codeListingMetas),
        List.copyOf(abbreviationMetas)
);
return new BodyContentRenderResult(List.copyOf(blocks), metadata);
```

- [ ] **Step 6: Escrever `BodyContentRendererMetadataTest`**

Estes são testes **unitários diretos** sobre `BodyContentRenderer.renderWithMetadata()` — não usam MockMvc nem `@SpringBootTest`. O `DocumentProfile` é obtido pelo `DocumentProfileRepository` via `@Autowired` (apenas o Spring context mínimo) ou construído manualmente se o perfil de teste permitir.

```java
// src/test/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyContentRendererMetadataTest.java
package com.abntbuilder.formatter.rendering.component.bodycontent;

import com.abntbuilder.formatter.document.component.bodycontent.*;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.repository.DocumentProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BodyContentRendererMetadataTest {

    @Autowired
    private DocumentProfileRepository profileRepository;

    @Autowired
    private BodyContentRenderer renderer;

    private DocumentProfile profile;

    @BeforeEach
    void setUp() {
        profile = profileRepository.findById("abnt-unip-profile").orElseThrow();
    }

    @Test
    void shouldEmitSectionMetadata() {
        BodySection s1 = new BodySection("sec-intro", 1, "Introdução", List.of());
        BodySection s2 = new BodySection("sec-dev", 1, "Desenvolvimento", List.of());
        BodyContentComponent component = new BodyContentComponent("bodyContent", List.of(s1, s2));

        BodyContentRenderResult result = renderer.renderWithMetadata(component, profile);

        assertThat(result.metadata().sections()).hasSize(2);
        assertThat(result.metadata().sections().get(0).id()).isEqualTo("sec-intro");
        assertThat(result.metadata().sections().get(0).level()).isEqualTo(1);
        assertThat(result.metadata().sections().get(0).renderedTitle()).startsWith("1");
        assertThat(result.metadata().sections().get(1).id()).isEqualTo("sec-dev");
        assertThat(result.metadata().sections().get(1).renderedTitle()).startsWith("2");
    }

    @Test
    void shouldEmitFigureMetadata() {
        BodyFigure figure = new BodyFigure(
                "fig-1",
                "Diagrama de componentes",
                new BodyFigure.Image(BodyFigure.Image.SourceType.DATA_URI,
                        "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/p9sAAAAASUVORK5CYII=",
                        "Diagrama")
        );
        BodySection section = new BodySection("sec-1", 1, "Seção", List.of(figure));
        BodyContentComponent component = new BodyContentComponent("bodyContent", List.of(section));

        BodyContentRenderResult result = renderer.renderWithMetadata(component, profile);

        assertThat(result.metadata().figures()).hasSize(1);
        assertThat(result.metadata().figures().get(0).id()).isEqualTo("fig-1");
        assertThat(result.metadata().figures().get(0).number()).isEqualTo(1);
        assertThat(result.metadata().figures().get(0).caption()).isEqualTo("Diagrama de componentes");
    }

    @Test
    void shouldEmitAbbreviationMetadata() {
        BodyAbbreviation abbr = new BodyAbbreviation("ABNT", "Associação Brasileira de Normas Técnicas");
        BodyParagraph paragraph = new BodyParagraph(List.of(abbr));
        BodySection section = new BodySection("sec-1", 1, "Seção", List.of(paragraph));
        BodyContentComponent component = new BodyContentComponent("bodyContent", List.of(section));

        BodyContentRenderResult result = renderer.renderWithMetadata(component, profile);

        assertThat(result.metadata().abbreviations()).hasSize(1);
        assertThat(result.metadata().abbreviations().get(0).abbreviation()).isEqualTo("ABNT");
        assertThat(result.metadata().abbreviations().get(0).expansion())
                .isEqualTo("Associação Brasileira de Normas Técnicas");
    }

    @Test
    void shouldEmitAbbreviationOnlyOnce() {
        BodyAbbreviation abbr = new BodyAbbreviation("ABNT", "Associação Brasileira de Normas Técnicas");
        BodyParagraph p1 = new BodyParagraph(List.of(abbr));
        BodyParagraph p2 = new BodyParagraph(List.of(abbr));
        BodySection section = new BodySection("sec-1", 1, "Seção", List.of(p1, p2));
        BodyContentComponent component = new BodyContentComponent("bodyContent", List.of(section));

        BodyContentRenderResult result = renderer.renderWithMetadata(component, profile);

        assertThat(result.metadata().abbreviations()).hasSize(1);
        assertThat(result.metadata().abbreviations().get(0).abbreviation()).isEqualTo("ABNT");
    }
}
```

> **Nota:** Os construtores usados acima (`new BodySection(...)`, `new BodyFigure(...)` etc.) devem ser conferidos contra as assinaturas reais das classes de domínio — ajustar se necessário. O ponto central é que os testes constroem `BodyContentComponent` programaticamente e chamam `renderer.renderWithMetadata(component, profile)` diretamente, assertando sobre `result.metadata()`.

- [ ] **Step 7: Rodar testes**

```bash
mvn test -pl . -Dtest=BodyContentRendererMetadataTest -q
```

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyContentRenderer.java \
        src/test/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyContentRendererMetadataTest.java
git commit -m "feat: emit BodyContentMetadata from BodyContentRenderer"
```

---

## Task 4 — `DocumentRenderer` armazena metadados

**Files:**
- Modify: `DocumentRenderer.java`

- [ ] **Step 1: Adicionar método auxiliar de cast em `MetadataEmittingRenderer`**

```java
// Em MetadataEmittingRenderer — adicionar default method:
@SuppressWarnings("unchecked")
default BodyContentRenderResult renderComponentWithMetadata(
        DocumentComponent component, DocumentProfile profile) {
    return renderWithMetadata((T) component, profile);
}
```

Imports necessários em `MetadataEmittingRenderer.java`:
```java
import com.abntbuilder.formatter.document.component.DocumentComponent;
```

- [ ] **Step 2: Atualizar o bloco `if (component != null)` em `DocumentRenderer.render()`**

Adicionar `BodyContentMetadata bodyContentMetadata = BodyContentMetadata.empty();` como **variável local** antes do loop `for (String componentId : componentOrder)`.

Substituir o bloco `if (component != null)` atual (que contém `addBlocks(blocks, pageNumbering, rendererRegistry.get(...).renderComponent(...))`) por:

```java
if (component != null) {
    Optional<DocxPageNumbering> pageNumbering = pageNumberingState.beforeRendering(componentId);
    if (blocks.isEmpty() && pageNumbering.isPresent()) {
        initialPageNumbering = pageNumbering;
    }

    ComponentRenderer<?> renderer = rendererRegistry.get(componentId);
    List<DocxBlock> componentBlocks;

    if (renderer instanceof MetadataEmittingRenderer<?> emitting) {
        BodyContentRenderResult result = emitting.renderComponentWithMetadata(component, command.profile());
        componentBlocks = result.blocks();
        bodyContentMetadata = result.metadata();  // variável local — sem race condition
    } else {
        componentBlocks = renderer.renderComponent(component, command.profile());
    }

    addBlocks(blocks, pageNumbering, componentBlocks);
    pageNumberingState.afterRendering();
}
```

Adicionar imports em `DocumentRenderer.java`:
```java
import com.abntbuilder.formatter.rendering.component.MetadataEmittingRenderer;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyContentMetadata;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyContentRenderResult;
```

> `bodyContentMetadata` é variável **local ao método** `render()` — uma instância por invocação, sem estado compartilhado entre requests. A Fase 5 Task 1 acrescenta o case `MetadataConsumingRenderer` neste mesmo bloco.

- [ ] **Step 3: Compilar e rodar suite**

```bash
mvn compile -q && mvn test -q
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/abntbuilder/formatter/rendering/document/DocumentRenderer.java
git commit -m "feat: DocumentRenderer stores BodyContentMetadata from MetadataEmittingRenderer"
```

---

## Task 5 — Domínio de referências cruzadas

**Files:**
- Create: `CrossReferenceTargetType.java`, `CrossReferenceDisplayMode.java`, `BodyCrossReference.java`
- Modify: `BodyInline.java`

- [ ] **Step 1: Criar `CrossReferenceTargetType`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/bodycontent/CrossReferenceTargetType.java
package com.abntbuilder.formatter.document.component.bodycontent;

public enum CrossReferenceTargetType {
    SECTION,
    FIGURE,
    TABLE,
    FRAME,
    CHART,
    CODE_LISTING,
    EQUATION
}
```

- [ ] **Step 2: Criar `CrossReferenceDisplayMode`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/bodycontent/CrossReferenceDisplayMode.java
package com.abntbuilder.formatter.document.component.bodycontent;

public enum CrossReferenceDisplayMode {
    NUMBER_ONLY,      // "1", "2.1"
    LABEL_AND_NUMBER, // "Figura 1", "Quadro 2", "Seção 1.1"
    CAPTION           // texto completo da legenda/título
}
```

- [ ] **Step 3: Criar `BodyCrossReference`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyCrossReference.java
package com.abntbuilder.formatter.document.component.bodycontent;

import java.util.Objects;

public record BodyCrossReference(
        String targetId,
        CrossReferenceTargetType targetType,
        CrossReferenceDisplayMode displayMode
) implements BodyInline {

    public BodyCrossReference {
        if (targetId == null || targetId.isBlank()) {
            throw new IllegalArgumentException("targetId must not be blank.");
        }
        Objects.requireNonNull(targetType, "targetType must not be null");
        Objects.requireNonNull(displayMode, "displayMode must not be null");
    }

    @Override
    public String renderedText() {
        // Placeholder — o texto real é resolvido pelo renderer na segunda passagem
        return "[ref:" + targetId + "]";
    }
}
```

- [ ] **Step 4: Atualizar `BodyInline`**

```java
public sealed interface BodyInline
    permits BodyText, BodyCitationCall, BodyQuoteText,
            BodyAbbreviation, BodyFootnote, BodyCrossReference {
}
```

- [ ] **Step 5: Escrever e rodar teste**

```java
class BodyCrossReferenceTest {
    @Test
    void shouldCreateWithValidData() {
        BodyCrossReference ref = new BodyCrossReference("fig-1", CrossReferenceTargetType.FIGURE, CrossReferenceDisplayMode.LABEL_AND_NUMBER);
        assertThat(ref.targetId()).isEqualTo("fig-1");
    }

    @Test
    void shouldRejectBlankTargetId() {
        assertThatThrownBy(() -> new BodyCrossReference("", CrossReferenceTargetType.FIGURE, CrossReferenceDisplayMode.NUMBER_ONLY))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
```

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: add BodyCrossReference domain type"
```

---

## Task 6 — Regra de labels de referência cruzada no perfil

**Files:**
- Create: `CrossReferenceLabelsRule.java`, `CrossReferenceLabelsRuleRequest.java`
- Modify: `BodyContentComponentRule.java`, `BodyContentComponentRuleRequest.java`
- Modify: `abnt-unip-profile.json`

- [ ] **Step 1: Criar `CrossReferenceLabelsRule`**

```java
// src/main/java/com/abntbuilder/formatter/profile/model/component/bodycontent/CrossReferenceLabelsRule.java
package com.abntbuilder.formatter.profile.model.component.bodycontent;

public record CrossReferenceLabelsRule(
        String sectionLabel,
        String figureLabel,
        String tableLabel,
        String frameLabel,
        String chartLabel,
        String codeListingLabel,
        String equationLabel
) {
    public CrossReferenceLabelsRule {
        requireNonBlank(sectionLabel, "sectionLabel");
        requireNonBlank(figureLabel, "figureLabel");
        requireNonBlank(tableLabel, "tableLabel");
        requireNonBlank(frameLabel, "frameLabel");
        requireNonBlank(chartLabel, "chartLabel");
        requireNonBlank(codeListingLabel, "codeListingLabel");
        requireNonBlank(equationLabel, "equationLabel");
    }

    public String labelFor(CrossReferenceTargetType type) {
        return switch (type) {
            case SECTION -> sectionLabel;
            case FIGURE -> figureLabel;
            case TABLE -> tableLabel;
            case FRAME -> frameLabel;
            case CHART -> chartLabel;
            case CODE_LISTING -> codeListingLabel;
            case EQUATION -> equationLabel;
        };
    }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
```

- [ ] **Step 2: Adicionar `crossReferenceLabels` em `BodyContentComponentRule`**

```java
public record BodyContentComponentRule(
        String componentId,
        BodyContentStyleMapping styleMapping,
        BodyContentNumberingRule numbering,
        BodyContentLayoutRule layout,
        FigureRule figure,
        TableRule table,
        FrameRule frame,
        CodeListingRule codeListing,
        ChartRule chart,
        CitationFormattingRule citationFormatting,
        CrossReferenceLabelsRule crossReferenceLabels   // NOVO
) implements ComponentRule { ... }
```

- [ ] **Step 3: Adicionar no perfil JSON**

```json
"crossReferenceLabels": {
  "sectionLabel": "Seção",
  "figureLabel": "Figura",
  "tableLabel": "Tabela",
  "frameLabel": "Quadro",
  "chartLabel": "Gráfico",
  "codeListingLabel": "Listagem",
  "equationLabel": "Equação"
}
```

- [ ] **Step 4: Compilar**

```bash
mvn compile -q
```

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: add CrossReferenceLabelsRule to profile and BodyContentComponentRule"
```

---

## Task 7 — Request de referência cruzada

**Files:**
- Create: `BodyCrossReferenceRequest.java`
- Modify: `BodyInlineType.java`, `BodyInlineRequest.java`

- [ ] **Step 1: Adicionar `CROSS_REFERENCE` em `BodyInlineType`**

```java
public enum BodyInlineType {
    TEXT,
    CITATION,
    QUOTE_TEXT,
    ABBREVIATION,
    FOOTNOTE,
    CROSS_REFERENCE
}
```

- [ ] **Step 2: Criar `BodyCrossReferenceRequest`**

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/BodyCrossReferenceRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.bodycontent.BodyCrossReference;
import com.abntbuilder.formatter.document.component.bodycontent.CrossReferenceDisplayMode;
import com.abntbuilder.formatter.document.component.bodycontent.CrossReferenceTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BodyCrossReferenceRequest(
        @NotBlank String targetId,
        @NotNull CrossReferenceTargetType targetType,
        @NotNull CrossReferenceDisplayMode displayMode
) {
    public BodyCrossReference toDomain() {
        return new BodyCrossReference(targetId, targetType, displayMode);
    }
}
```

- [ ] **Step 3: Adicionar campo e caso em `BodyInlineRequest`**

Adicionar campo:

```java
@Valid BodyCrossReferenceRequest crossReference
```

Adicionar caso no `toDomain(CitationFormattingRule)`:

```java
case CROSS_REFERENCE -> {
    if (crossReference == null) {
        throw new IllegalArgumentException("crossReference must be provided for CROSS_REFERENCE inline.");
    }
    yield crossReference.toDomain();
}
```

- [ ] **Step 4: Compilar**

```bash
mvn compile -q
```

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: add CROSS_REFERENCE inline type and BodyCrossReferenceRequest"
```

---

## Task 8 — Resolução de referências cruzadas no renderer

**Files:**
- Modify: `BodyContentRenderer.java`

A resolução de referências cruzadas exige que o renderer saiba os metadados antes de renderizar os inlines. A abordagem é **duas passagens**:

1. **Passagem 1** — renderização completa, acumulando metadados. `BodyCrossReference` gera um run com texto placeholder `"[ref:id]"`.
2. **Passagem 2** — substituir todos os runs placeholder pelo texto resolvido usando os metadados coletados.

Alternativa mais simples (e suficiente para esta fase): **pré-coletar** todos os metadados antes da renderização principal, varrendo `component.sections()` uma única vez.

Adotar a abordagem de pré-coleta:

- [ ] **Step 1: Criar record `CrossReferenceIndex` em `BodyContentRenderer`**

```java
// Record privado dentro de BodyContentRenderer (ou package-private separado se preferir)
private record CrossReferenceIndex(
        Map<String, BodySectionMetadata> sections,
        Map<String, BodyDisplayObjectMetadata> figures,
        Map<String, BodyDisplayObjectMetadata> tables,
        Map<String, BodyDisplayObjectMetadata> frames,
        Map<String, BodyDisplayObjectMetadata> charts,
        Map<String, BodyDisplayObjectMetadata> codeListings
) {
    String resolve(String targetId, CrossReferenceTargetType targetType,
                   CrossReferenceDisplayMode displayMode, CrossReferenceLabelsRule labels) {
        return switch (displayMode) {
            case NUMBER_ONLY -> resolveNumber(targetId, targetType);
            case LABEL_AND_NUMBER -> labels.labelFor(targetType) + " " + resolveNumber(targetId, targetType);
            case CAPTION -> resolveCaption(targetId, targetType);
        };
    }

    private String resolveNumber(String targetId, CrossReferenceTargetType targetType) {
        return switch (targetType) {
            case SECTION -> {
                BodySectionMetadata m = sections.get(targetId);
                if (m == null) throw new InvalidBodyContentException(
                        "CROSS_REFERENCE targetId not found: '" + targetId + "' (type: SECTION).");
                yield m.renderedTitle().split(" ")[0];  // pega só o número "1", "1.2" etc.
            }
            case FIGURE -> resolveDisplayObjectNumber(targetId, figures, "FIGURE");
            case TABLE -> resolveDisplayObjectNumber(targetId, tables, "TABLE");
            case FRAME -> resolveDisplayObjectNumber(targetId, frames, "FRAME");
            case CHART -> resolveDisplayObjectNumber(targetId, charts, "CHART");
            case CODE_LISTING -> resolveDisplayObjectNumber(targetId, codeListings, "CODE_LISTING");
            case EQUATION -> throw new InvalidBodyContentException(
                    "CROSS_REFERENCE to EQUATION requires targetId matching a BodyEquation — not yet indexed.");
        };
    }

    private String resolveDisplayObjectNumber(String targetId,
            Map<String, BodyDisplayObjectMetadata> index, String typeName) {
        BodyDisplayObjectMetadata m = index.get(targetId);
        if (m == null) throw new InvalidBodyContentException(
                "CROSS_REFERENCE targetId not found: '" + targetId + "' (type: " + typeName + ").");
        return String.valueOf(m.number());
    }

    private String resolveCaption(String targetId, CrossReferenceTargetType targetType) {
        return switch (targetType) {
            case SECTION -> {
                BodySectionMetadata m = sections.get(targetId);
                if (m == null) throw new InvalidBodyContentException(
                        "CROSS_REFERENCE targetId not found: '" + targetId + "' (type: SECTION).");
                yield m.renderedTitle();
            }
            case FIGURE -> figures.containsKey(targetId) ? figures.get(targetId).caption()
                    : throwNotFound(targetId, "FIGURE");
            case TABLE -> tables.containsKey(targetId) ? tables.get(targetId).caption()
                    : throwNotFound(targetId, "TABLE");
            case FRAME -> frames.containsKey(targetId) ? frames.get(targetId).caption()
                    : throwNotFound(targetId, "FRAME");
            case CHART -> charts.containsKey(targetId) ? charts.get(targetId).caption()
                    : throwNotFound(targetId, "CHART");
            case CODE_LISTING -> codeListings.containsKey(targetId) ? codeListings.get(targetId).caption()
                    : throwNotFound(targetId, "CODE_LISTING");
            case EQUATION -> throwNotFound(targetId, "EQUATION");
        };
    }

    private String throwNotFound(String targetId, String typeName) {
        throw new InvalidBodyContentException(
                "CROSS_REFERENCE targetId not found: '" + targetId + "' (type: " + typeName + ").");
    }
}
```

Adicionar import: `import java.util.Map;`

- [ ] **Step 2: Implementar `buildCrossReferenceIndex`**

Este método percorre as seções, usando instâncias **separadas** de `SectionNumberingState` e `DisplayObjectRenderingState` (não as mesmas da renderização principal), para pré-calcular os números sem efeito colateral:

```java
private static CrossReferenceIndex buildCrossReferenceIndex(
        BodyContentComponent component, BodyContentComponentRule rule) {

    SectionNumberingState prepassNumbering = new SectionNumberingState(rule.numbering());
    DisplayObjectRenderingState<BodyFigure> preFigures = new DisplayObjectRenderingState<>(
            figuresFrom(component.sections()));
    DisplayObjectRenderingState<BodyTable> preTables = new DisplayObjectRenderingState<>(
            tablesFrom(component.sections()));
    // Adicionar preFrames, preCharts, preCodeListings analogamente quando Fase 2 concluída

    Map<String, BodySectionMetadata> sectionIndex = new LinkedHashMap<>();
    Map<String, BodyDisplayObjectMetadata> figureIndex = new LinkedHashMap<>();
    Map<String, BodyDisplayObjectMetadata> tableIndex = new LinkedHashMap<>();
    Map<String, BodyDisplayObjectMetadata> frameIndex = new LinkedHashMap<>();
    Map<String, BodyDisplayObjectMetadata> chartIndex = new LinkedHashMap<>();
    Map<String, BodyDisplayObjectMetadata> codeListingIndex = new LinkedHashMap<>();

    for (BodySection section : component.sections()) {
        if (section.title().isPresent()) {
            String renderedTitle = prepassNumbering.resolveTitle(section.level(), section.title().orElseThrow());
            sectionIndex.put(section.id(), new BodySectionMetadata(section.id(), section.level(), renderedTitle));
        }
        for (BodyBlock block : section.blocks()) {
            if (block instanceof BodyFigure figure) {
                DisplayObjectContinuationPart part = preFigures.nextPart(figure, rule.figure().continuationLabels());
                if (part.continuationLabel().isEmpty()) {
                    figureIndex.put(figure.id(), new BodyDisplayObjectMetadata(figure.id(), part.number(), figure.caption()));
                }
            }
            if (block instanceof BodyTable table) {
                DisplayObjectContinuationPart part = preTables.nextPart(table, rule.table().continuationLabels());
                if (part.continuationLabel().isEmpty()) {
                    tableIndex.put(table.id(), new BodyDisplayObjectMetadata(table.id(), part.number(), table.caption()));
                }
            }
            // Adicionar cases para BodyFrame, BodyChart, BodyCodeListing após Fase 2
        }
    }

    return new CrossReferenceIndex(
            Map.copyOf(sectionIndex),
            Map.copyOf(figureIndex),
            Map.copyOf(tableIndex),
            Map.copyOf(frameIndex),
            Map.copyOf(chartIndex),
            Map.copyOf(codeListingIndex)
    );
}
```

Adicionar imports: `import java.util.LinkedHashMap;`

- [ ] **Step 3: Chamar `buildCrossReferenceIndex` no início de `renderWithMetadata`**

```java
CrossReferenceIndex crossRefIndex = buildCrossReferenceIndex(component, rule);
```

Passar `crossRefIndex` como parâmetro adicional em `renderContentBlock` e, por propagação, em `toDocxRun`.

**Assinatura atualizada de `renderContentBlock`:**

```java
private static List<DocxRun> renderContentBlock(
        List<BodyInline> inlines,
        StyleRule baseStyle,
        BodyContentComponentRule rule,
        StyleResolver styleResolver,
        List<BodyAbbreviationMetadata> abbreviationMetas,
        CrossReferenceIndex crossRefIndex  // novo parâmetro
) {
    return inlines.stream()
            .map(inline -> toDocxRun(inline, baseStyle, rule, styleResolver, abbreviationMetas, crossRefIndex))
            .toList();
}
```

**Assinatura atualizada de `toDocxRun`:**

```java
private static DocxRun toDocxRun(
        BodyInline inline,
        StyleRule baseStyle,
        BodyContentComponentRule rule,
        StyleResolver styleResolver,
        List<BodyAbbreviationMetadata> abbreviationMetas,
        CrossReferenceIndex crossRefIndex  // novo parâmetro
) { ... }
```

Atualizar todas as chamadas a `renderContentBlock` no loop de `renderWithMetadata` para passar tanto `abbreviationMetas` (adicionado na Task 3) quanto `crossRefIndex`.

- [ ] **Step 4: Resolver `BodyCrossReference` no `toDocxRun`**

```java
case BodyCrossReference ref -> {
    String resolved = crossRefIndex.resolve(
            ref.targetId(),
            ref.targetType(),
            ref.displayMode(),
            rule.crossReferenceLabels()
    );
    yield new DocxRun(resolved, baseStyle, InlineFormatting.none());
}
```

- [ ] **Step 3: Rejeitar `targetId` desconhecido**

Em `crossRefIndex.resolve`, se o `targetId` não existir no índice:

```java
throw new InvalidBodyContentException(
    "CROSS_REFERENCE targetId not found: '" + ref.targetId() + "' (type: " + ref.targetType() + ")."
);
```

- [ ] **Step 4: Implementar `CrossReferenceIndex.resolve`**

```java
public String resolve(String targetId, CrossReferenceTargetType targetType,
                      CrossReferenceDisplayMode displayMode, CrossReferenceLabelsRule labels) {
    return switch (displayMode) {
        case NUMBER_ONLY -> resolveNumber(targetId, targetType);
        case LABEL_AND_NUMBER -> labels.labelFor(targetType) + " " + resolveNumber(targetId, targetType);
        case CAPTION -> resolveCaption(targetId, targetType);
    };
}
```

- [ ] **Step 5: Escrever `BodyCrossReferenceTest` de integração**

```java
@Test
void shouldResolveCrossReferenceToFigureNumber() throws Exception {
    String json = Files.readString(Path.of("docs/samples/body-content/body-content-cross-references.json"));
    mockMvc.perform(post("/api/v1/exports/docx")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isOk());
    // verificar que o DOCX não contém "[ref:" — significa que todos foram resolvidos
}

@Test
void shouldRejectUnknownCrossReferenceId() throws Exception {
    String json = Files.readString(Path.of("docs/samples/body-content/body-content-cross-reference-unknown-id-invalid.json"));
    mockMvc.perform(post("/api/v1/exports/docx")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isBadRequest());
}
```

- [ ] **Step 6: Compilar e rodar**

```bash
mvn compile -q && mvn test -q
```

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "feat: resolve CROSS_REFERENCE inlines using pre-collected metadata index"
```

---

## Task 9 — Samples e suite completa

**Files:**
- Create: `docs/samples/body-content/body-content-cross-references.json`
- Create: `docs/samples/body-content/body-content-cross-reference-unknown-id-invalid.json`
- Modify: `BodyContentSampleValidationTest.java`

- [ ] **Step 1: Criar sample de referências cruzadas**

```json
{
  "profileId": "abnt-unip-profile",
  "document": {
    "components": {
      "bodyContent": {
        "sections": [
          {
            "id": "s1", "level": 1, "title": "Contextualização",
            "blocks": [
              {
                "type": "PARAGRAPH",
                "content": [
                  { "type": "TEXT", "text": "Conforme apresentado na " },
                  { "type": "CROSS_REFERENCE",
                    "crossReference": {
                      "targetId": "fig-modelo",
                      "targetType": "FIGURE",
                      "displayMode": "LABEL_AND_NUMBER"
                    }
                  },
                  { "type": "TEXT", "text": ", o modelo semântico organiza os blocos." }
                ]
              },
              {
                "type": "FIGURE",
                "figure": {
                  "id": "fig-modelo",
                  "caption": "Modelo semântico de blocos",
                  "image": {
                    "sourceType": "DATA_URI",
                    "dataUri": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/p9sAAAAASUVORK5CYII=",
                    "altText": "Modelo"
                  }
                }
              }
            ]
          }
        ]
      }
    }
  }
}
```

- [ ] **Step 2: Criar sample inválido (id desconhecido)**

```json
{
  "profileId": "abnt-unip-profile",
  "document": {
    "components": {
      "bodyContent": {
        "sections": [{
          "id": "s1", "level": 1, "title": "Seção",
          "blocks": [{
            "type": "PARAGRAPH",
            "content": [{
              "type": "CROSS_REFERENCE",
              "crossReference": {
                "targetId": "id-que-nao-existe",
                "targetType": "FIGURE",
                "displayMode": "LABEL_AND_NUMBER"
              }
            }]
          }]
        }]
      }
    }
  }
}
```

- [ ] **Step 3: Adicionar ao `BodyContentSampleValidationTest`**

- [ ] **Step 4: Suite completa**

```bash
mvn test -q
```

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: add cross-reference samples and complete Fase 3"
```

---

## Checklist de conclusão da Fase 3

| Requisito | Task |
|---|---|
| `BodyContentMetadata` com seções, figuras, tabelas, quadros, gráficos, listagens, siglas | Task 1 |
| `BodyContentRenderResult` encapsula blocks + metadata | Task 1 |
| `MetadataEmittingRenderer` interface | Task 2 |
| `BodyContentRenderer` implementa `MetadataEmittingRenderer` | Task 2 |
| Metadados emitidos corretamente (seções com título numerado, display objects com número) | Task 3 |
| Siglas coletadas uma única vez por abreviatura | Task 3 |
| `DocumentRenderer` armazena `BodyContentMetadata` | Task 4 |
| `BodyCrossReference` domínio | Task 5 |
| `CrossReferenceLabelsRule` no perfil | Task 6 |
| `CROSS_REFERENCE` em `BodyInlineType` e request | Task 7 |
| Resolução de referências cruzadas por pré-coleta de metadados | Task 8 |
| Rejeição de `targetId` desconhecido antes de gerar DOCX | Task 8 |
| Samples válidos e inválidos | Task 9 |
| Suite completa verde | Task 9 |
