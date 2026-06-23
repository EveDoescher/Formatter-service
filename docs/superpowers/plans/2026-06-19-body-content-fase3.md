# bodyContent Fase 3 — Metadados e Referências Cruzadas

> **Pré-requisito:** Fase 2 concluída — todos os tipos de bloco e inline existem, incluindo `BodyFrame`, `BodyCodeListing`, `BodyChart`, `BodyEquation`, `BodyAbbreviation`.
>
> **For agentic workers:** Use superpowers:subagent-driven-development. Steps usam `- [x]`.

**Goal:** Instrumentar o renderer para emitir metadados de todas as séries numeradas (seções, figuras, tabelas, quadros, gráficos, listagens, siglas). Usar esses metadados para suportar referências cruzadas inline (`CROSS_REFERENCE`).

**Tech Stack:** Java 21, Spring Boot, JUnit 5

---

## Mapa de arquivos

### Novos
- `BodyContentMetadata.java`
- `BodySectionMetadata.java`
- `BodyDisplayObjectMetadata.java` (record genérico para figuras/tabelas/quadros/gráficos/listagens)
- `BodyAbbreviationMetadata.java`
- `ComponentRenderResult.java` (interface genérica em `rendering.component`)
- `BodyContentRenderResult.java` (implementa `ComponentRenderResult`)
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
- `DocumentRenderer.java` — detecta `MetadataEmittingRenderer`, armazena `BodyContentMetadata` em variável local
- `abnt-unip-profile.json` — adiciona `crossReferenceLabels`

---

## Task 1 — Tipos de metadados

**Files:**
- Create: `ComponentRenderResult.java`, `BodySectionMetadata.java`, `BodyDisplayObjectMetadata.java`, `BodyAbbreviationMetadata.java`, `BodyContentMetadata.java`, `BodyContentRenderResult.java`

- [x] **Step 1: Criar `BodySectionMetadata`**

```java
// src/main/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodySectionMetadata.java
package com.abntbuilder.formatter.rendering.component.bodycontent;

public record BodySectionMetadata(
        String id,
        int level,
        String renderedTitle,   // com prefixo numérico já aplicado, ex: "1 Introdução"
        String renderedNumber   // apenas o número, ex: "1", "1.2" — armazenado separado para evitar split frágil
) {
    public BodySectionMetadata {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id must not be blank.");
        if (level < 1 || level > 6) throw new IllegalArgumentException("level must be between 1 and 6.");
        if (renderedTitle == null || renderedTitle.isBlank()) throw new IllegalArgumentException("renderedTitle must not be blank.");
        if (renderedNumber == null || renderedNumber.isBlank()) throw new IllegalArgumentException("renderedNumber must not be blank.");
    }
}
```

- [x] **Step 2: Criar `BodyDisplayObjectMetadata`**

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

- [x] **Step 3: Criar `BodyAbbreviationMetadata`**

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

- [x] **Step 4: Criar `BodyContentMetadata`**

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

- [x] **Step 5: Criar `ComponentRenderResult`**

Interface marcadora no pacote `rendering.component` — desacopla `MetadataEmittingRenderer` de tipos específicos do bodyContent:

```java
// src/main/java/com/abntbuilder/formatter/rendering/component/ComponentRenderResult.java
package com.abntbuilder.formatter.rendering.component;

import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import java.util.List;

public interface ComponentRenderResult {
    List<DocxBlock> blocks();
}
```

- [x] **Step 6: Criar `BodyContentRenderResult`**

```java
// src/main/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyContentRenderResult.java
package com.abntbuilder.formatter.rendering.component.bodycontent;

import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.rendering.component.ComponentRenderResult;

import java.util.List;
import java.util.Objects;

public record BodyContentRenderResult(
        List<DocxBlock> blocks,
        BodyContentMetadata metadata
) implements ComponentRenderResult {
    public BodyContentRenderResult {
        Objects.requireNonNull(blocks, "blocks must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");
        blocks = List.copyOf(blocks);
    }
}
```

- [x] **Step 7: Compilar**

```bash
cd /mnt/c/Users/evelynnd/Documents/Projetos/Formatter-service
mvn compile -q
```

- [x] **Step 8: Commit**

```bash
git add src/main/java/com/abntbuilder/formatter/rendering/component/ComponentRenderResult.java \
        src/main/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodySectionMetadata.java \
        src/main/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyDisplayObjectMetadata.java \
        src/main/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyAbbreviationMetadata.java \
        src/main/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyContentMetadata.java \
        src/main/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyContentRenderResult.java
git commit -m "feat: add ComponentRenderResult, BodyContentMetadata and related types"
```

---

## Task 2 — `MetadataEmittingRenderer` interface

**Files:**
- Create: `MetadataEmittingRenderer.java`
- Modify: `BodyContentRenderer.java` — implementa a interface

- [x] **Step 1: Criar `MetadataEmittingRenderer`**

`MetadataEmittingRenderer` usa `ComponentRenderResult` (não `BodyContentRenderResult`) para não acoplar a interface genérica a um tipo específico do bodyContent. O `default render()` delega para `renderWithMetadata` e chama `blocks()`, que é o único método declarado em `ComponentRenderResult`.

```java
// src/main/java/com/abntbuilder/formatter/rendering/component/MetadataEmittingRenderer.java
package com.abntbuilder.formatter.rendering.component;

import com.abntbuilder.formatter.document.component.DocumentComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.profile.model.DocumentProfile;

import java.util.List;

public interface MetadataEmittingRenderer<T extends DocumentComponent, R extends ComponentRenderResult>
        extends ComponentRenderer<T> {

    R renderWithMetadata(T component, DocumentProfile profile);

    @Override
    default List<DocxBlock> render(T component, DocumentProfile profile) {
        return renderWithMetadata(component, profile).blocks();
    }
}
```

- [x] **Step 2: Atualizar a assinatura de `BodyContentRenderer`**

Mudar a declaração da classe para usar os dois parâmetros de tipo:

```java
public final class BodyContentRenderer
        implements MetadataEmittingRenderer<BodyContentComponent, BodyContentRenderResult> {
```

**Importante:** A interface `MetadataEmittingRenderer` fornece um `default render()` que delega para `renderWithMetadata`. Portanto **não deve existir** um método `render` na classe `BodyContentRenderer` — o método a implementar é `renderWithMetadata`. Se o método `render` existir na classe, ele vai sobrescrever o default da interface e não haverá delegação automática.

Renomear o método existente `render(BodyContentComponent, DocumentProfile)` para `renderWithMetadata`, ajustando o retorno de `List<DocxBlock>` para `BodyContentRenderResult`.

O esqueleto completo do método após esta Task (antes de a Task 3 preencher os acumuladores) é:

O esqueleto usa os mesmos construtores que existem no código atual — `new ComponentRuleResolver(profile).resolve(...)` e `new StyleResolver(profile)`:

```java
@Override
public BodyContentRenderResult renderWithMetadata(
        BodyContentComponent component, DocumentProfile profile) {

    BodyContentComponentRule rule = new ComponentRuleResolver(profile)
            .resolve(COMPONENT_ID, BodyContentComponentRule.class);
    StyleResolver styleResolver = new StyleResolver(profile);

    // Estado de numeração — inalterado em relação ao render() anterior
    SectionNumberingState numberingState = new SectionNumberingState(rule.numbering());
    StyleRule blankLineStyle = styleResolver.resolve(rule.layout().blankLineStyleId());
    boolean previousBlockWasTextualContent = false;
    DisplayObjectRenderingState<BodyFigure> figureRenderingState =
            new DisplayObjectRenderingState<>(figuresFrom(component.sections()));
    DisplayObjectRenderingState<BodyTable> tableRenderingState =
            new DisplayObjectRenderingState<>(tablesFrom(component.sections()));
    DisplayObjectRenderingState<BodyFrame> frameRenderingState =
            new DisplayObjectRenderingState<>(framesFrom(component.sections()));
    DisplayObjectRenderingState<BodyCodeListing> codeListingRenderingState =
            new DisplayObjectRenderingState<>(codeListingsFrom(component.sections()));
    DisplayObjectRenderingState<BodyChart> chartRenderingState =
            new DisplayObjectRenderingState<>(chartsFrom(component.sections()));
    int[] footnoteCounter = new int[1];

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

Verificar que nenhum `@Override public List<DocxBlock> render(...)` permanece na classe.

- [x] **Step 3: Compilar e rodar suite para garantir que nada quebrou**

```bash
mvn compile -q && mvn test -q
```

- [x] **Step 4: Commit**

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

- [x] **Step 1: Adicionar acumuladores de metadados no método `renderWithMetadata`**

```java
List<BodySectionMetadata> sectionMetas = new ArrayList<>();
List<BodyDisplayObjectMetadata> figureMetas = new ArrayList<>();
List<BodyDisplayObjectMetadata> tableMetas = new ArrayList<>();
List<BodyDisplayObjectMetadata> frameMetas = new ArrayList<>();
List<BodyDisplayObjectMetadata> chartMetas = new ArrayList<>();
List<BodyDisplayObjectMetadata> codeListingMetas = new ArrayList<>();
List<BodyAbbreviationMetadata> abbreviationMetas = new ArrayList<>();
```

- [x] **Step 2: Expor `resolveNumber` em `SectionNumberingState`**

`SectionNumberingState` só tem `resolveTitle(int level, String title)` — o número é privado em `sectionNumber()`. Adicionar um método público antes de usar na Task 3 e na Task 8:

```java
// dentro de SectionNumberingState (classe interna de BodyContentRenderer)
String resolveNumber(int level) {
    // Não incrementa — apenas retorna o número atual para o nível dado.
    // Deve ser chamado APÓS resolveTitle (que já incrementou os contadores).
    return sectionNumber(level);
}
```

**Atenção:** `resolveNumber` deve ser chamado **depois** de `resolveTitle` para o mesmo nível, pois `resolveTitle` é quem faz o `increment()`. Se chamado antes, o número ainda não foi incrementado.

- [x] **Step 3: Capturar metadados de seções**

Dentro do bloco `if (section.title().isPresent())`, após a chamada existente a `numberingState.resolveTitle`:

```java
String renderedTitle = numberingState.resolveTitle(section.level(), section.title().orElseThrow());
String renderedNumber = numberingState.resolveNumber(section.level()); // chamado APÓS resolveTitle
// ...
sectionMetas.add(new BodySectionMetadata(section.id(), section.level(), renderedTitle, renderedNumber));
```

- [x] **Step 3: Capturar metadados de display objects no caller (não nos métodos privados estáticos)**

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

- [x] **Step 5: Capturar metadados de siglas**

`toDocxRun` já existe e retorna `List<DocxRun>` (desde a Fase 2, por causa dos marcadores de ênfase). Sua assinatura atual é:

```java
private static List<DocxRun> toDocxRun(
        BodyInline inline,
        StyleRule baseStyle,
        BodyContentComponentRule rule,
        StyleResolver styleResolver,
        List<DocxFootnoteContent> footnoteAccumulator,
        int[] footnoteCounter
)
```

Adicionar `abbreviationMetas` como novo parâmetro após `styleResolver`:

```java
private static List<DocxRun> toDocxRun(
        BodyInline inline,
        StyleRule baseStyle,
        BodyContentComponentRule rule,
        StyleResolver styleResolver,
        List<BodyAbbreviationMetadata> abbreviationMetas,  // NOVO
        List<DocxFootnoteContent> footnoteAccumulator,
        int[] footnoteCounter
)
```

Atualizar **todas** as chamadas a `toDocxRun` no corpo do renderer para passar `abbreviationMetas`. Há três sítios: no case `BodyParagraph`, no case `BodyList` (items), e no case `BodyFootnote` (runs recursivos dentro de `toDocxRun` si mesmo).

**Case de `BodyAbbreviation` dentro de `toDocxRun`** — já existe, só adicionar o registro:

```java
case BodyAbbreviation abbr -> {
    if (abbreviationMetas.stream().noneMatch(m -> m.abbreviation().equals(abbr.abbreviation()))) {
        abbreviationMetas.add(new BodyAbbreviationMetadata(abbr.abbreviation(), abbr.expansion()));
    }
    yield List.of(new DocxRun(abbr.renderedText(), baseStyle, InlineFormatting.none()));
}
```

Não existe um método separado `renderContentBlock(List<BodyInline>...)` — os inlines são processados diretamente com `.flatMap(inline -> toDocxRun(...).stream())` no switch de `renderContentBlock(BodyBlock, ...)`. O parâmetro `abbreviationMetas` é declarado em `renderWithMetadata` e propagado via `toDocxRun`.

- [x] **Step 6: Construir e retornar `BodyContentMetadata`**

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

- [x] **Step 7: Escrever `BodyContentRendererMetadataTest`**

Testes unitários diretos sem `@SpringBootTest` — seguem o padrão de `BodyContentRendererTest`, que instancia `new BodyContentRenderer()` e constrói o `DocumentProfile` via `profile()` helper. Conferir as assinaturas reais antes de implementar: `BodySection` recebe `(String id, int level, Optional<String> title, List<BodyBlock> blocks)`; `BodyContentComponent` recebe `(List<BodySection> sections)`; `BodyFigure` recebe `(String id, Optional<String> continuationGroupId, String caption, Optional<String> source, BodyImageSource image)`.

```java
// src/test/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyContentRendererMetadataTest.java
package com.abntbuilder.formatter.rendering.component.bodycontent;

import com.abntbuilder.formatter.document.component.bodycontent.*;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class BodyContentRendererMetadataTest {

    private static final String ONE_PIXEL_PNG =
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/p9sAAAAASUVORK5CYII=";

    private final BodyContentRenderer renderer = new BodyContentRenderer();

    @Test
    void shouldEmitSectionMetadata() {
        BodySection s1 = new BodySection("sec-intro", 1, Optional.of("Introdução"), List.of());
        BodySection s2 = new BodySection("sec-dev", 1, Optional.of("Desenvolvimento"), List.of());
        BodyContentComponent component = new BodyContentComponent(List.of(s1, s2));

        BodyContentRenderResult result = renderer.renderWithMetadata(component, profile());

        assertThat(result.metadata().sections()).hasSize(2);
        assertThat(result.metadata().sections().get(0).id()).isEqualTo("sec-intro");
        assertThat(result.metadata().sections().get(0).level()).isEqualTo(1);
        assertThat(result.metadata().sections().get(0).renderedTitle()).startsWith("1");
        assertThat(result.metadata().sections().get(0).renderedNumber()).isEqualTo("1");
        assertThat(result.metadata().sections().get(1).renderedTitle()).startsWith("2");
    }

    @Test
    void shouldEmitFigureMetadata() {
        BodyFigure figure = new BodyFigure(
                "fig-1",
                Optional.empty(),
                "Diagrama de componentes",
                Optional.empty(),
                new BodyImageSource(ImageSourceType.DATA_URI, ONE_PIXEL_PNG, null, "Diagrama")
        );
        BodySection section = new BodySection("sec-1", 1, Optional.of("Seção"), List.of(figure));
        BodyContentComponent component = new BodyContentComponent(List.of(section));

        BodyContentRenderResult result = renderer.renderWithMetadata(component, profile());

        assertThat(result.metadata().figures()).hasSize(1);
        assertThat(result.metadata().figures().get(0).id()).isEqualTo("fig-1");
        assertThat(result.metadata().figures().get(0).number()).isEqualTo(1);
        assertThat(result.metadata().figures().get(0).caption()).isEqualTo("Diagrama de componentes");
    }

    @Test
    void shouldEmitAbbreviationMetadataOnlyOnce() {
        BodyAbbreviation abbr = new BodyAbbreviation("ABNT", "Associação Brasileira de Normas Técnicas");
        BodyParagraph p1 = new BodyParagraph(List.of(abbr));
        BodyParagraph p2 = new BodyParagraph(List.of(abbr));
        BodySection section = new BodySection("sec-1", 1, Optional.of("Seção"), List.of(p1, p2));
        BodyContentComponent component = new BodyContentComponent(List.of(section));

        BodyContentRenderResult result = renderer.renderWithMetadata(component, profile());

        assertThat(result.metadata().abbreviations()).hasSize(1);
        assertThat(result.metadata().abbreviations().get(0).abbreviation()).isEqualTo("ABNT");
        assertThat(result.metadata().abbreviations().get(0).expansion())
                .isEqualTo("Associação Brasileira de Normas Técnicas");
    }

    private static DocumentProfile profile() {
        // Copiar o helper profile() de BodyContentRendererTest — mesmo perfil mínimo
        // com styleRules, BodyContentComponentRule, etc.
        throw new UnsupportedOperationException("copiar implementação de BodyContentRendererTest.profile()");
    }
}
```

> **Atenção ao implementar:** Copiar o método `profile()` de `BodyContentRendererTest` (que constrói um `DocumentProfile` mínimo com todas as styleRules necessárias). Os construtores de domínio acima são baseados nas assinaturas reais — verificar antes de compilar.

- [x] **Step 7: Rodar testes**

```bash
mvn test -pl . -Dtest=BodyContentRendererMetadataTest -q
```

- [x] **Step 8: Commit**

```bash
git add src/main/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyContentRenderer.java \
        src/test/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyContentRendererMetadataTest.java
git commit -m "feat: emit BodyContentMetadata from BodyContentRenderer"
```

---

## Task 4 — `DocumentRenderer` armazena metadados

**Files:**
- Modify: `DocumentRenderer.java`

- [x] **Step 1: Adicionar método auxiliar de cast em `MetadataEmittingRenderer`**

`DocumentRenderer` usa `instanceof MetadataEmittingRenderer<?,?>` com wildcard e precisa de um helper que apague os tipos para chamar `renderWithMetadata` sem cast não verificado espalhado pelo caller. Adicionar em `MetadataEmittingRenderer.java`:

```java
// Em MetadataEmittingRenderer — adicionar default method e import:
import com.abntbuilder.formatter.document.component.DocumentComponent;

@SuppressWarnings("unchecked")
default ComponentRenderResult renderComponentWithMetadata(
        DocumentComponent component, DocumentProfile profile) {
    return renderWithMetadata((T) component, profile);
}
```

O retorno é `ComponentRenderResult` (não `BodyContentRenderResult`) para que o `DocumentRenderer` não dependa de um tipo específico do bodyContent.

- [x] **Step 2: Atualizar o bloco `if (component != null)` em `DocumentRenderer.render()`**

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

    if (renderer instanceof MetadataEmittingRenderer<?,?> emitting) {
        ComponentRenderResult result = emitting.renderComponentWithMetadata(component, command.profile());
        componentBlocks = result.blocks();
        if (result instanceof BodyContentRenderResult bcr) {
            bodyContentMetadata = bcr.metadata();
        }
    } else {
        componentBlocks = renderer.renderComponent(component, command.profile());
    }

    addBlocks(blocks, pageNumbering, componentBlocks);
    pageNumberingState.afterRendering();
}
```

Adicionar imports em `DocumentRenderer.java`:
```java
import com.abntbuilder.formatter.rendering.component.ComponentRenderResult;
import com.abntbuilder.formatter.rendering.component.MetadataEmittingRenderer;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyContentMetadata;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyContentRenderResult;
```

> `bodyContentMetadata` é variável **local ao método** `render()` — uma instância por invocação, sem estado compartilhado entre requests. A Fase 5 Task 1 acrescenta o case `MetadataConsumingRenderer` neste mesmo bloco.

- [x] **Step 3: Compilar e rodar suite**

```bash
mvn compile -q && mvn test -q
```

- [x] **Step 4: Commit**

```bash
git add src/main/java/com/abntbuilder/formatter/rendering/document/DocumentRenderer.java
git commit -m "feat: DocumentRenderer stores BodyContentMetadata from MetadataEmittingRenderer"
```

---

## Task 5 — Domínio de referências cruzadas

**Files:**
- Create: `CrossReferenceTargetType.java`, `CrossReferenceDisplayMode.java`, `BodyCrossReference.java`
- Modify: `BodyInline.java`

- [x] **Step 1: Criar `CrossReferenceTargetType`**

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

- [x] **Step 2: Criar `CrossReferenceDisplayMode`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/bodycontent/CrossReferenceDisplayMode.java
package com.abntbuilder.formatter.document.component.bodycontent;

public enum CrossReferenceDisplayMode {
    NUMBER_ONLY,      // "1", "2.1"
    LABEL_AND_NUMBER, // "Figura 1", "Quadro 2", "Seção 1.1"
    CAPTION           // texto completo da legenda/título
}
```

- [x] **Step 3: Criar `BodyCrossReference`**

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

- [x] **Step 4: Atualizar `BodyInline`**

```java
public sealed interface BodyInline
    permits BodyText, BodyCitationCall, BodyQuoteText,
            BodyAbbreviation, BodyFootnote, BodyCrossReference {
}
```

- [x] **Step 5: Escrever e rodar teste**

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

- [x] **Step 6: Commit**

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

- [x] **Step 1: Criar `CrossReferenceLabelsRule`**

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

- [x] **Step 2: Adicionar `crossReferenceLabels` em `BodyContentComponentRule`**

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

- [x] **Step 3: Adicionar no perfil JSON**

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

- [x] **Step 4: Compilar**

```bash
mvn compile -q
```

- [x] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: add CrossReferenceLabelsRule to profile and BodyContentComponentRule"
```

---

## Task 7 — Request de referência cruzada

**Files:**
- Create: `BodyCrossReferenceRequest.java`
- Modify: `BodyInlineType.java`, `BodyInlineRequest.java`

- [x] **Step 1: Adicionar `CROSS_REFERENCE` em `BodyInlineType`**

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

- [x] **Step 2: Criar `BodyCrossReferenceRequest`**

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

- [x] **Step 3: Adicionar campo e caso em `BodyInlineRequest`**

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

- [x] **Step 4: Compilar**

```bash
mvn compile -q
```

- [x] **Step 5: Commit**

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

- [x] **Step 1: Criar record `CrossReferenceIndex` em `BodyContentRenderer`**

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
                yield m.renderedNumber();  // número da seção, ex: "1", "1.2" — armazenado separado em BodySectionMetadata
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

- [x] **Step 2: Implementar `buildCrossReferenceIndex`**

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
            // resolveTitle incrementa os contadores; resolveNumber deve ser chamado APÓS
            String renderedTitle = prepassNumbering.resolveTitle(section.level(), section.title().orElseThrow());
            String renderedNumber = prepassNumbering.resolveNumber(section.level());  // ex: "1", "1.2"
            sectionIndex.put(section.id(), new BodySectionMetadata(section.id(), section.level(), renderedTitle, renderedNumber));
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

- [x] **Step 3: Chamar `buildCrossReferenceIndex` no início de `renderWithMetadata`**

```java
CrossReferenceIndex crossRefIndex = buildCrossReferenceIndex(component, rule);
```

Passar `crossRefIndex` como parâmetro adicional em `toDocxRun`. Não existe um método `renderContentBlock(List<BodyInline>...)` separado — os inlines são processados com `.flatMap(inline -> toDocxRun(...).stream())` diretamente nos cases do switch de `renderContentBlock(BodyBlock, ...)`.

**Assinatura atualizada de `toDocxRun`** (adicionar `crossRefIndex` após `abbreviationMetas`):

```java
private static List<DocxRun> toDocxRun(
        BodyInline inline,
        StyleRule baseStyle,
        BodyContentComponentRule rule,
        StyleResolver styleResolver,
        List<BodyAbbreviationMetadata> abbreviationMetas,
        List<DocxFootnoteContent> footnoteAccumulator,
        int[] footnoteCounter,
        CrossReferenceIndex crossRefIndex  // novo parâmetro
) { ... }
```

Atualizar todas as chamadas a `toDocxRun` no renderer para passar `crossRefIndex`. Há três sítios: no case `BodyParagraph`, no case `BodyList` (items) e na recursão interna de `BodyFootnote`.

- [x] **Step 4: Resolver `BodyCrossReference` no `toDocxRun`**

`toDocxRun` retorna `List<DocxRun>` (desde a Fase 2):

```java
case BodyCrossReference ref -> {
    String resolved = crossRefIndex.resolve(
            ref.targetId(),
            ref.targetType(),
            ref.displayMode(),
            rule.crossReferenceLabels()
    );
    yield List.of(new DocxRun(resolved, baseStyle, InlineFormatting.none()));
}
```

- [x] **Step 3: Rejeitar `targetId` desconhecido**

Em `crossRefIndex.resolve`, se o `targetId` não existir no índice:

```java
throw new InvalidBodyContentException(
    "CROSS_REFERENCE targetId not found: '" + ref.targetId() + "' (type: " + ref.targetType() + ")."
);
```

- [x] **Step 4: Implementar `CrossReferenceIndex.resolve`**

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

- [x] **Step 5: Escrever `BodyCrossReferenceTest` de integração**

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

- [x] **Step 6: Compilar e rodar**

```bash
mvn compile -q && mvn test -q
```

- [x] **Step 7: Commit**

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

- [x] **Step 1: Criar sample de referências cruzadas**

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

- [x] **Step 2: Criar sample inválido (id desconhecido)**

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

- [x] **Step 3: Adicionar ao `BodyContentSampleValidationTest`**

- [x] **Step 4: Suite completa**

```bash
mvn test -q
```

- [x] **Step 5: Commit**

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
