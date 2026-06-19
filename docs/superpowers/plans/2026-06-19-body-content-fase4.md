# bodyContent Fase 4 — Pré-textuais Especializados e Pós-textuais

> **Pré-requisito:** Fase 1 concluída. **Independente das Fases 2 e 3** — pode ser desenvolvida em paralelo.
>
> **For agentic workers:** Use superpowers:subagent-driven-development. Steps usam `- [ ]`.

**Goal:** Implementar os componentes pré-textuais (Errata, Dedicatória, Epígrafe, Agradecimentos, Resumo, Abstract) e pós-textuais (Referências, Apêndice, Anexo, Glossário) que completam um trabalho acadêmico ABNT.

**Tech Stack:** Java 21, Spring Boot, docx4j 11.5.13, JUnit 5

**Família arquitetural dos componentes desta fase:**
- **Errata**: `flow-content` simples — pode ocupar mais de uma página, não precisa de `SinglePageLayoutEngine`.
- **Dedicatória e Epígrafe**: `flow-content` com `DocxPageBreak` emitido antes do primeiro bloco, para garantir página própria — não precisam de `SinglePageLayoutEngine`.
- **Agradecimentos, Resumo, Abstract, Referências, Apêndice, Anexo, Glossário**: `flow-content`.

> A classificação "single-page" do roadmap era uma sugestão arquitetural para os componentes pré-textuais mais simples, mas dado que `SinglePageLayoutEngine` é complexo de reutilizar (requer `SinglePageLayoutInput`, `SinglePageGroupRule`, etc.), todos os componentes desta fase usam o padrão `flow-content` direto. Componentes que precisam de página própria emitem `DocxPageBreak` como primeiro bloco.

**⚠️ Ponto crítico para todos os componentes desta fase:** `DocumentContentRequest` é o objeto que recebe o conteúdo do documento no request JSON. Ele tem campos explícitos para cada componente (`cover`, `titlePage`, `approvalSheet`, `bodyContent`). **Para cada novo componente, é obrigatório**:
1. Adicionar campo em `DocumentContentRequest`
2. Adicionar conversão no método `toComponents(AcademicWorkRequest work, DocumentProfile profile)`
3. Adicionar campo em `ExportDocxRequest` **não** — o ponto de entrada é `DocumentContentRequest` via campo `document` no request raiz.

Esse passo é omitido em alguns componentes abaixo por brevidade, mas **nunca pode ser pulado**.

**⚠️ `DocumentComponent.type()` retorna `ComponentType` (enum), não `String`** — a interface real é:
```java
public interface DocumentComponent {
    ComponentType type();
}
```
Para cada novo componente desta fase, **antes de criar o domínio**, adicionar o valor correspondente ao enum `ComponentType` em `src/main/java/com/abntbuilder/formatter/document/component/ComponentType.java`. Os valores a adicionar ao longo desta fase: `ERRATA, DEDICATION, EPIGRAPH, ACKNOWLEDGMENTS, RESUMO, ABSTRACT_EN, REFERENCES, APPENDIX, ANNEX, GLOSSARY`.

**Padrão de implementação (igual a `cover`, `titlePage`, `approvalSheet`):**
1. Criar domínio (`XxxComponent`)
2. Criar request DTO (`XxxRequest`)
3. Criar rule de perfil (`XxxComponentRule`)
4. Criar renderer (`XxxRenderer`) registrado em `ComponentRendererRegistry`
5. Adicionar no `componentOrder` do perfil
6. Criar samples e testes

---

## Ordem de implementação recomendada

```
1. Errata, Dedicatória, Epígrafe (single-page — padrão já consolidado)
2. Agradecimentos, Resumo, Abstract (flow simples)
3. References (lógica de formatação por tipo)
4. Appendix e Annex (reutilizam estrutura de BodySection)
5. Glossary
```

---

## Task 1 — Errata

A errata é um componente pré-textual opcional que lista correções a erros de edições anteriores.

**Files:**
- Create: `ErrataComponent.java`, `ErrataEntry.java`
- Create: `ErrataRequest.java`, `ErrataEntryRequest.java`
- Create: `ErrataComponentRule.java`
- Create: `ErrataComponentRuleRequest.java`
- Create: `ErrataRenderer.java`
- Modify: `abnt-unip-profile.json`

- [ ] **Step 0: Adicionar `ERRATA` ao enum `ComponentType`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/ComponentType.java
public enum ComponentType {
    COVER,
    TITLE_PAGE,
    APPROVAL_SHEET,
    BODY_CONTENT,
    ERRATA           // adicionar
}
```

- [ ] **Step 1: Criar domínio**

```java
// src/main/java/com/abntbuilder/formatter/document/component/errata/ErrataEntry.java
package com.abntbuilder.formatter.document.component.errata;

public record ErrataEntry(
        String page,
        String line,
        String incorrectText,
        String correctText
) {
    public ErrataEntry {
        requireNonBlank(page, "page");
        requireNonBlank(line, "line");
        requireNonBlank(incorrectText, "incorrectText");
        requireNonBlank(correctText, "correctText");
    }
    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
```

```java
// src/main/java/com/abntbuilder/formatter/document/component/errata/ErrataComponent.java
package com.abntbuilder.formatter.document.component.errata;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;
import java.util.List;
import java.util.Objects;

public record ErrataComponent(List<ErrataEntry> entries) implements DocumentComponent {
    public ErrataComponent {
        Objects.requireNonNull(entries, "entries must not be null");
        if (entries.isEmpty()) throw new IllegalArgumentException("entries must not be empty.");
        entries = List.copyOf(entries);
    }
    @Override public ComponentType type() { return ComponentType.ERRATA; }
}
```

- [ ] **Step 2: Criar `ErrataComponentRule`**

```java
// src/main/java/com/abntbuilder/formatter/profile/model/component/errata/ErrataComponentRule.java
package com.abntbuilder.formatter.profile.model.component.errata;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import java.util.Map;
import java.util.Objects;

public record ErrataComponentRule(
        String componentId,
        String headingStyleId,
        String headingText,
        String entryStyleId,
        String entryTemplate  // ex: "Página {page}, linha {line}: onde se lê {incorrect}, leia-se {correct}."
) implements ComponentRule {
    public ErrataComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(headingStyleId, "headingStyleId");
        requireNonBlank(headingText, "headingText");
        requireNonBlank(entryStyleId, "entryStyleId");
        requireNonBlank(entryTemplate, "entryTemplate");
    }
    @Override public Map<String, String> contentBindings() { return Map.of(); }
    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
```

- [ ] **Step 3: Criar `ErrataRenderer`**

```java
// src/main/java/com/abntbuilder/formatter/rendering/component/errata/ErrataRenderer.java
package com.abntbuilder.formatter.rendering.component.errata;

import com.abntbuilder.formatter.document.component.errata.ErrataComponent;
import com.abntbuilder.formatter.document.component.errata.ErrataEntry;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.errata.ErrataComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ErrataRenderer implements ComponentRenderer<ErrataComponent> {

    public static final String COMPONENT_ID = "errata";

    @Override public String componentId() { return COMPONENT_ID; }
    @Override public Class<ErrataComponent> componentType() { return ErrataComponent.class; }

    @Override
    public List<DocxBlock> render(ErrataComponent component, DocumentProfile profile) {
        ErrataComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(COMPONENT_ID, ErrataComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());
        StyleRule entryStyle = styleResolver.resolve(rule.entryStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        blocks.add(new DocxParagraph(
                List.of(DocxRun.of(rule.headingText(), headingStyle)),
                headingStyle
        ));

        for (ErrataEntry entry : component.entries()) {
            String text = rule.entryTemplate()
                    .replace("{page}", entry.page())
                    .replace("{line}", entry.line())
                    .replace("{incorrect}", entry.incorrectText())
                    .replace("{correct}", entry.correctText());
            blocks.add(new DocxParagraph(List.of(DocxRun.of(text, entryStyle)), entryStyle));
        }

        return List.copyOf(blocks);
    }
}
```

- [ ] **Step 4: Criar requests** (`ErrataRequest`, `ErrataEntryRequest`, `ErrataComponentRuleRequest`)

Padrão: campos com `@Valid`/`@NotBlank`/`@NotNull`, método `toDomain()`.

- [ ] **Step 5: Adicionar campo em `DocumentContentRequest`**

```java
// record DocumentContentRequest — adicionar:
@Valid ErrataRequest errata,

// método toComponents() — adicionar:
if (errata != null) {
    components.add(errata.toDomain());
}
```

- [ ] **Step 6: Registrar `ErrataRenderer` em `RenderingConfig`**

Os renderers são beans Spring registrados em `src/main/java/com/abntbuilder/formatter/config/RenderingConfig.java`. Adicionar:

```java
@Bean
public ErrataRenderer errataRenderer() {
    return new ErrataRenderer();
}
```

O `ComponentRendererRegistry` é construído com `List<ComponentRenderer<?>>` injetada pelo Spring — qualquer bean que implemente `ComponentRenderer<?>` é automaticamente incluído.

- [ ] **Step 6: Adicionar rule e estilos em `abnt-unip-profile.json`**

Em `styleRules`:
```json
{ "id": "errata.heading", "fontFamily": "Times New Roman", "fontSizePt": 12, "bold": true, "italic": false, "uppercase": true, "alignment": "CENTER", "lineHeightRule": "EXACT", "lineHeightPt": 18 },
{ "id": "errata.entry",   "fontFamily": "Times New Roman", "fontSizePt": 12, "bold": false, "italic": false, "uppercase": false, "alignment": "JUSTIFY", "lineHeightRule": "EXACT", "lineHeightPt": 18 }
```

Em `componentRules`:
```json
"errata": {
  "componentId": "errata",
  "headingStyleId": "errata.heading",
  "headingText": "ERRATA",
  "entryStyleId": "errata.entry",
  "entryTemplate": "Folha {page}, linha {line}: onde se lê {incorrect}, leia-se {correct}."
}
```

Em `componentOrder` (antes de `approvalSheet`):
```json
"errata"
```

- [ ] **Step 7: Criar sample e teste**

```
docs/samples/errata/errata-simple.json
docs/samples/errata/errata-empty-invalid.json
```

- [ ] **Step 8: Compilar e rodar**

```bash
mvn compile -q && mvn test -q
```

- [ ] **Step 9: Commit**

```bash
git add -A
git commit -m "feat: add Errata component (renderer, rule, request, sample)"
```

---

## Task 2 — Dedicatória

Componente `flow-content` com página própria. O renderer emite `DocxPageBreak` como primeiro bloco para garantir que a dedicatória sempre comece em nova página. O texto é alinhado à direita na metade inferior — simulado com `indentLeftCm` de aproximadamente metade da largura útil (≈9cm).

**Files:**
- Create: `DedicationComponent.java`, `DedicationComponentRule.java`, `DedicationRenderer.java`
- Create: `DedicationRequest.java`, `DedicationComponentRuleRequest.java`

- [ ] **Step 0: Adicionar `DEDICATION` ao enum `ComponentType`**

```java
// Acrescentar ao enum ComponentType em ComponentType.java:
DEDICATION
```

- [ ] **Step 1: Criar domínio**

```java
// src/main/java/com/abntbuilder/formatter/document/component/dedication/DedicationComponent.java
package com.abntbuilder.formatter.document.component.dedication;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

public record DedicationComponent(String text) implements DocumentComponent {
    public DedicationComponent {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("text must not be blank.");
    }
    @Override public ComponentType type() { return ComponentType.DEDICATION; }
}
```

- [ ] **Step 2: Criar `DedicationComponentRule`**

```java
// src/main/java/com/abntbuilder/formatter/profile/model/component/dedication/DedicationComponentRule.java
package com.abntbuilder.formatter.profile.model.component.dedication;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import java.util.Map;

public record DedicationComponentRule(
        String componentId,
        String textStyleId
        // A posição (metade inferior direita) é declarada no perfil via SinglePageLayoutRule
        // Nesta fase, renderizar como parágrafo alinhado à direita sem engine single-page
) implements ComponentRule {
    public DedicationComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(textStyleId, "textStyleId");
    }
    @Override public Map<String, String> contentBindings() { return Map.of(); }
    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
```

> Nota: a dedicatória ABNT deve ficar na metade inferior direita. Nesta fase, renderizar como parágrafo alinhado à direita com `indentLeftCm: 9` (aprox. metade da largura útil de 16cm em A4 com margens 3/2). O posicionamento exato via `SinglePageLayoutEngine` pode ser refinado em fase futura.

- [ ] **Step 3: Criar `DedicationRenderer`**

```java
// src/main/java/com/abntbuilder/formatter/rendering/component/dedication/DedicationRenderer.java
package com.abntbuilder.formatter.rendering.component.dedication;

import com.abntbuilder.formatter.document.component.dedication.DedicationComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxPageBreak;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.dedication.DedicationComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;

import java.util.List;

public final class DedicationRenderer implements ComponentRenderer<DedicationComponent> {

    public static final String COMPONENT_ID = "dedication";

    @Override public String componentId() { return COMPONENT_ID; }
    @Override public Class<DedicationComponent> componentType() { return DedicationComponent.class; }

    @Override
    public List<DocxBlock> render(DedicationComponent component, DocumentProfile profile) {
        DedicationComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(COMPONENT_ID, DedicationComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule textStyle = styleResolver.resolve(rule.textStyleId());
        return List.of(
                new DocxPageBreak(),  // garante página nova
                new DocxParagraph(List.of(DocxRun.of(component.text(), textStyle)), textStyle)
        );
    }
}
```

- [ ] **Step 5: Adicionar campo em `DocumentContentRequest` e bean em `RenderingConfig`**

Seguir o mesmo padrão da Errata (Steps 5 e 6 da Task 1).

- [ ] **Step 6: Criar request, adicionar ao perfil, ao `componentOrder`, criar sample, compilar, commit**

Estilo `dedication.text`: Times New Roman 12pt, alinhamento RIGHT, `indentLeftCm: 9`, espaçamento 1.5.

```bash
git commit -m "feat: add Dedication component"
```

---

## Task 3 — Epígrafe

Texto literário recuado à direita, seguido de autoria.

**Files:**
- Create: `EpigraphComponent.java`, `EpigraphComponentRule.java`, `EpigraphRenderer.java`
- Create: `EpigraphRequest.java`, `EpigraphComponentRuleRequest.java`

- [ ] **Step 0: Adicionar `EPIGRAPH` ao enum `ComponentType`**

```java
// Acrescentar ao enum ComponentType em ComponentType.java:
EPIGRAPH
```

- [ ] **Step 1: Criar domínio**

```java
// src/main/java/com/abntbuilder/formatter/document/component/epigraph/EpigraphComponent.java
package com.abntbuilder.formatter.document.component.epigraph;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;
import java.util.Optional;

public record EpigraphComponent(
        String text,
        String author,
        Optional<String> source
) implements DocumentComponent {
    public EpigraphComponent {
        requireNonBlank(text, "text");
        requireNonBlank(author, "author");
        java.util.Objects.requireNonNull(source, "source must not be null");
    }
    @Override public ComponentType type() { return ComponentType.EPIGRAPH; }
    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
```

- [ ] **Step 2: Criar `EpigraphComponentRule`**

```java
// src/main/java/com/abntbuilder/formatter/profile/model/component/epigraph/EpigraphComponentRule.java
package com.abntbuilder.formatter.profile.model.component.epigraph;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import java.util.Map;

public record EpigraphComponentRule(
        String componentId,
        String textStyleId,
        String authorStyleId,
        String authorTemplate  // ex: "— {author}" ou "— {author}, {source}"
) implements ComponentRule {
    public EpigraphComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(textStyleId, "textStyleId");
        requireNonBlank(authorStyleId, "authorStyleId");
        requireNonBlank(authorTemplate, "authorTemplate");
    }
    @Override public Map<String, String> contentBindings() { return Map.of(); }
    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
```

- [ ] **Step 3: Criar `EpigraphRenderer`**

Emite: `DocxPageBreak` + parágrafo com texto epigráfico (estilo itálico recuado à direita) + parágrafo de autoria.

```java
// src/main/java/com/abntbuilder/formatter/rendering/component/epigraph/EpigraphRenderer.java
package com.abntbuilder.formatter.rendering.component.epigraph;

import com.abntbuilder.formatter.document.component.epigraph.EpigraphComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxPageBreak;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.epigraph.EpigraphComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;

import java.util.ArrayList;
import java.util.List;

public final class EpigraphRenderer implements ComponentRenderer<EpigraphComponent> {

    public static final String COMPONENT_ID = "epigraph";

    @Override public String componentId() { return COMPONENT_ID; }
    @Override public Class<EpigraphComponent> componentType() { return EpigraphComponent.class; }

    @Override
    public List<DocxBlock> render(EpigraphComponent component, DocumentProfile profile) {
        EpigraphComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(COMPONENT_ID, EpigraphComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule textStyle = styleResolver.resolve(rule.textStyleId());
        StyleRule authorStyle = styleResolver.resolve(rule.authorStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        blocks.add(new DocxPageBreak());  // garante página nova
        blocks.add(new DocxParagraph(List.of(DocxRun.of(component.text(), textStyle)), textStyle));

        String authorText = component.source()
                .map(src -> rule.authorTemplate()
                        .replace("{author}", component.author())
                        .replace("{source}", src))
                .orElse(rule.authorTemplate()
                        .replace("{author}", component.author())
                        .replace(", {source}", "").replace("{source}", ""));
        blocks.add(new DocxParagraph(List.of(DocxRun.of(authorText, authorStyle)), authorStyle));

        return List.copyOf(blocks);
    }
}
```

- [ ] **Step 4: Criar `EpigraphRequest`**

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/EpigraphRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.epigraph.EpigraphComponent;
import jakarta.validation.constraints.NotBlank;
import java.util.Optional;

public record EpigraphRequest(
        @NotBlank String text,
        @NotBlank String author,
        String source
) {
    public EpigraphComponent toDomain() {
        return new EpigraphComponent(text, author, Optional.ofNullable(source));
    }
}
```

- [ ] **Step 5: Criar `EpigraphComponentRuleRequest`**

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/profile/EpigraphComponentRuleRequest.java
package com.abntbuilder.formatter.api.export.dto.request.profile;

import com.abntbuilder.formatter.profile.model.component.epigraph.EpigraphComponentRule;
import jakarta.validation.constraints.NotBlank;

public record EpigraphComponentRuleRequest(
        @NotBlank String componentId,
        @NotBlank String textStyleId,
        @NotBlank String authorStyleId,
        @NotBlank String authorTemplate
) {
    public EpigraphComponentRule toDomain() {
        return new EpigraphComponentRule(componentId, textStyleId, authorStyleId, authorTemplate);
    }
}
```

- [ ] **Step 6: Adicionar campo em `DocumentContentRequest`**

```java
// record DocumentContentRequest — adicionar campo:
@Valid EpigraphRequest epigraph,

// método toComponents() — adicionar:
if (epigraph != null) {
    components.add(epigraph.toDomain());
}
```

- [ ] **Step 7: Registrar `EpigraphRenderer` em `RenderingConfig`**

```java
@Bean
public EpigraphRenderer epigraphRenderer() {
    return new EpigraphRenderer();
}
```

- [ ] **Step 8: Adicionar ao perfil e componentOrder, criar sample, compilar, commit**

Estilos: `epigraph.text` — itálico, recuo esquerdo 9cm, alinhamento LEFT; `epigraph.author` — recuo esquerdo 9cm, alinhamento LEFT.

```bash
git commit -m "feat: add Epigraph component"
```

---

## Task 4 — Agradecimentos

Flow-content simples (texto livre).

**Files:**
- Create: `AcknowledgmentsComponent.java`, `AcknowledgmentsComponentRule.java`, `AcknowledgmentsRenderer.java`
- Create: `AcknowledgmentsRequest.java`, `AcknowledgmentsComponentRuleRequest.java`

- [ ] **Step 0: Adicionar `ACKNOWLEDGMENTS` ao enum `ComponentType`**

```java
// Acrescentar ao enum ComponentType em ComponentType.java:
ACKNOWLEDGMENTS
```

- [ ] **Step 1: Criar domínio**

```java
// src/main/java/com/abntbuilder/formatter/document/component/acknowledgments/AcknowledgmentsComponent.java
package com.abntbuilder.formatter.document.component.acknowledgments;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;

public record AcknowledgmentsComponent(String text) implements DocumentComponent {
    public AcknowledgmentsComponent {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("text must not be blank.");
    }
    @Override public ComponentType type() { return ComponentType.ACKNOWLEDGMENTS; }
}
```

- [ ] **Step 2: `AcknowledgmentsComponentRule`**

```java
// src/main/java/com/abntbuilder/formatter/profile/model/component/acknowledgments/AcknowledgmentsComponentRule.java
package com.abntbuilder.formatter.profile.model.component.acknowledgments;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import java.util.Map;

public record AcknowledgmentsComponentRule(
        String componentId,
        String headingStyleId,
        String headingText,    // "AGRADECIMENTOS"
        String textStyleId
) implements ComponentRule {
    public AcknowledgmentsComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(headingStyleId, "headingStyleId");
        requireNonBlank(headingText, "headingText");
        requireNonBlank(textStyleId, "textStyleId");
    }
    @Override public Map<String, String> contentBindings() { return Map.of(); }
    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
```

- [ ] **Step 3: Criar `AcknowledgmentsRenderer`** — emite heading + parágrafo de texto.

```java
// src/main/java/com/abntbuilder/formatter/rendering/component/acknowledgments/AcknowledgmentsRenderer.java
package com.abntbuilder.formatter.rendering.component.acknowledgments;

import com.abntbuilder.formatter.document.component.acknowledgments.AcknowledgmentsComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.acknowledgments.AcknowledgmentsComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;

import java.util.ArrayList;
import java.util.List;

public final class AcknowledgmentsRenderer implements ComponentRenderer<AcknowledgmentsComponent> {

    public static final String COMPONENT_ID = "acknowledgments";

    @Override public String componentId() { return COMPONENT_ID; }
    @Override public Class<AcknowledgmentsComponent> componentType() { return AcknowledgmentsComponent.class; }

    @Override
    public List<DocxBlock> render(AcknowledgmentsComponent component, DocumentProfile profile) {
        AcknowledgmentsComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(COMPONENT_ID, AcknowledgmentsComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());
        StyleRule textStyle = styleResolver.resolve(rule.textStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        blocks.add(new DocxParagraph(List.of(DocxRun.of(rule.headingText(), headingStyle)), headingStyle));
        blocks.add(new DocxParagraph(List.of(DocxRun.of(component.text(), textStyle)), textStyle));
        return List.copyOf(blocks);
    }
}
```

- [ ] **Step 4: Criar `AcknowledgmentsRequest`**

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/AcknowledgmentsRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.acknowledgments.AcknowledgmentsComponent;
import jakarta.validation.constraints.NotBlank;

public record AcknowledgmentsRequest(@NotBlank String text) {
    public AcknowledgmentsComponent toDomain() {
        return new AcknowledgmentsComponent(text);
    }
}
```

- [ ] **Step 5: Criar `AcknowledgmentsComponentRuleRequest`**

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/profile/AcknowledgmentsComponentRuleRequest.java
package com.abntbuilder.formatter.api.export.dto.request.profile;

import com.abntbuilder.formatter.profile.model.component.acknowledgments.AcknowledgmentsComponentRule;
import jakarta.validation.constraints.NotBlank;

public record AcknowledgmentsComponentRuleRequest(
        @NotBlank String componentId,
        @NotBlank String headingStyleId,
        @NotBlank String headingText,
        @NotBlank String textStyleId
) {
    public AcknowledgmentsComponentRule toDomain() {
        return new AcknowledgmentsComponentRule(componentId, headingStyleId, headingText, textStyleId);
    }
}
```

- [ ] **Step 6: Adicionar campo em `DocumentContentRequest`**

```java
// record DocumentContentRequest — adicionar campo:
@Valid AcknowledgmentsRequest acknowledgments,

// método toComponents() — adicionar:
if (acknowledgments != null) {
    components.add(acknowledgments.toDomain());
}
```

- [ ] **Step 7: Registrar `AcknowledgmentsRenderer` em `RenderingConfig`**

```java
@Bean
public AcknowledgmentsRenderer acknowledgmentsRenderer() {
    return new AcknowledgmentsRenderer();
}
```

- [ ] **Step 8: Adicionar ao perfil e componentOrder, criar sample, compilar, commit**

```bash
git commit -m "feat: add Acknowledgments component"
```

---

## Task 5 — Resumo e Abstract

**Files:**
- Create: `ResumoComponent.java`, `AbstractComponent.java`
- Create: `ResumoComponentRule.java`, `AbstractComponentRule.java`
- Create: `ResumoRenderer.java`, `AbstractRenderer.java`
- Create: `ResumoRequest.java`, `AbstractRequest.java`
- Create: `ResumoComponentRuleRequest.java`, `AbstractComponentRuleRequest.java`

- [ ] **Step 0: Adicionar `RESUMO` e `ABSTRACT_EN` ao enum `ComponentType`**

```java
// Acrescentar ao enum ComponentType em ComponentType.java:
RESUMO,
ABSTRACT_EN
```

> `ABSTRACT_EN` e não `ABSTRACT` porque `abstract` é palavra reservada em Java e enum values com esse nome causam erro de compilação.

- [ ] **Step 1: Criar `ResumoComponent` e `AbstractComponent`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/resumo/ResumoComponent.java
package com.abntbuilder.formatter.document.component.resumo;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;
import java.util.List;
import java.util.Objects;

public record ResumoComponent(
        String text,
        List<String> keywords
) implements DocumentComponent {
    public ResumoComponent {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("text must not be blank.");
        Objects.requireNonNull(keywords, "keywords must not be null");
        if (keywords.isEmpty()) throw new IllegalArgumentException("keywords must not be empty.");
        keywords = List.copyOf(keywords);
    }
    @Override public ComponentType type() { return ComponentType.RESUMO; }
}
```

```java
// src/main/java/com/abntbuilder/formatter/document/component/abstracten/AbstractComponent.java
package com.abntbuilder.formatter.document.component.abstracten;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;
import java.util.List;
import java.util.Objects;

public record AbstractComponent(
        String text,
        List<String> keywords
) implements DocumentComponent {
    public AbstractComponent {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("text must not be blank.");
        Objects.requireNonNull(keywords, "keywords must not be null");
        if (keywords.isEmpty()) throw new IllegalArgumentException("keywords must not be empty.");
        keywords = List.copyOf(keywords);
    }
    @Override public ComponentType type() { return ComponentType.ABSTRACT_EN; }
}
```

> Pacote `abstracten` porque `abstract` é palavra reservada. O nome do componente no perfil continua sendo `"abstract"`.

- [ ] **Step 2: Criar `ResumoComponentRule` e `AbstractComponentRule`**

```java
// src/main/java/com/abntbuilder/formatter/profile/model/component/resumo/ResumoComponentRule.java
package com.abntbuilder.formatter.profile.model.component.resumo;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import java.util.Map;

public record ResumoComponentRule(
        String componentId,
        String headingStyleId,
        String headingText,       // "RESUMO"
        String textStyleId,
        String keywordsStyleId,
        String keywordsLabel,     // "Palavras-chave:"
        String keywordsSeparator  // "; "
) implements ComponentRule {
    public ResumoComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(headingStyleId, "headingStyleId");
        requireNonBlank(headingText, "headingText");
        requireNonBlank(textStyleId, "textStyleId");
        requireNonBlank(keywordsStyleId, "keywordsStyleId");
        requireNonBlank(keywordsLabel, "keywordsLabel");
        requireNonBlank(keywordsSeparator, "keywordsSeparator");
    }
    @Override public Map<String, String> contentBindings() { return Map.of(); }
    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
```

```java
// src/main/java/com/abntbuilder/formatter/profile/model/component/abstracten/AbstractComponentRule.java
package com.abntbuilder.formatter.profile.model.component.abstracten;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import java.util.Map;

public record AbstractComponentRule(
        String componentId,
        String headingStyleId,
        String headingText,       // "ABSTRACT"
        String textStyleId,
        String keywordsStyleId,
        String keywordsLabel,     // "Keywords:"
        String keywordsSeparator  // "; "
) implements ComponentRule {
    public AbstractComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(headingStyleId, "headingStyleId");
        requireNonBlank(headingText, "headingText");
        requireNonBlank(textStyleId, "textStyleId");
        requireNonBlank(keywordsStyleId, "keywordsStyleId");
        requireNonBlank(keywordsLabel, "keywordsLabel");
        requireNonBlank(keywordsSeparator, "keywordsSeparator");
    }
    @Override public Map<String, String> contentBindings() { return Map.of(); }
    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
```

- [ ] **Step 3: Criar `ResumoRenderer` e `AbstractRenderer`**

```java
// src/main/java/com/abntbuilder/formatter/rendering/component/resumo/ResumoRenderer.java
package com.abntbuilder.formatter.rendering.component.resumo;

import com.abntbuilder.formatter.document.component.resumo.ResumoComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.resumo.ResumoComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;

import java.util.ArrayList;
import java.util.List;

public final class ResumoRenderer implements ComponentRenderer<ResumoComponent> {

    public static final String COMPONENT_ID = "resumo";

    @Override public String componentId() { return COMPONENT_ID; }
    @Override public Class<ResumoComponent> componentType() { return ResumoComponent.class; }

    @Override
    public List<DocxBlock> render(ResumoComponent component, DocumentProfile profile) {
        ResumoComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(COMPONENT_ID, ResumoComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());
        StyleRule textStyle = styleResolver.resolve(rule.textStyleId());
        StyleRule keywordsStyle = styleResolver.resolve(rule.keywordsStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        blocks.add(new DocxParagraph(List.of(DocxRun.of(rule.headingText(), headingStyle)), headingStyle));
        blocks.add(new DocxParagraph(List.of(DocxRun.of(component.text(), textStyle)), textStyle));

        String keywordsText = rule.keywordsLabel() + " " +
                String.join(rule.keywordsSeparator(), component.keywords());
        blocks.add(new DocxParagraph(List.of(DocxRun.of(keywordsText, keywordsStyle)), keywordsStyle));

        return List.copyOf(blocks);
    }
}
```

```java
// src/main/java/com/abntbuilder/formatter/rendering/component/abstracten/AbstractRenderer.java
package com.abntbuilder.formatter.rendering.component.abstracten;

import com.abntbuilder.formatter.document.component.abstracten.AbstractComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.abstracten.AbstractComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;

import java.util.ArrayList;
import java.util.List;

public final class AbstractRenderer implements ComponentRenderer<AbstractComponent> {

    public static final String COMPONENT_ID = "abstract";

    @Override public String componentId() { return COMPONENT_ID; }
    @Override public Class<AbstractComponent> componentType() { return AbstractComponent.class; }

    @Override
    public List<DocxBlock> render(AbstractComponent component, DocumentProfile profile) {
        AbstractComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(COMPONENT_ID, AbstractComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());
        StyleRule textStyle = styleResolver.resolve(rule.textStyleId());
        StyleRule keywordsStyle = styleResolver.resolve(rule.keywordsStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        blocks.add(new DocxParagraph(List.of(DocxRun.of(rule.headingText(), headingStyle)), headingStyle));
        blocks.add(new DocxParagraph(List.of(DocxRun.of(component.text(), textStyle)), textStyle));

        String keywordsText = rule.keywordsLabel() + " " +
                String.join(rule.keywordsSeparator(), component.keywords());
        blocks.add(new DocxParagraph(List.of(DocxRun.of(keywordsText, keywordsStyle)), keywordsStyle));

        return List.copyOf(blocks);
    }
}
```

- [ ] **Step 4: Requests, registro, perfil, componentOrder, samples, testes, compilar, commit**

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/ResumoRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.resumo.ResumoComponent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ResumoRequest(
        @NotBlank String text,
        @NotEmpty List<@NotBlank String> keywords
) {
    public ResumoComponent toDomain() {
        return new ResumoComponent(text, keywords);
    }
}
```

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/AbstractRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.abstracten.AbstractComponent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AbstractRequest(
        @NotBlank String text,
        @NotEmpty List<@NotBlank String> keywords
) {
    public AbstractComponent toDomain() {
        return new AbstractComponent(text, keywords);
    }
}
```

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/profile/ResumoComponentRuleRequest.java
package com.abntbuilder.formatter.api.export.dto.request.profile;

import com.abntbuilder.formatter.profile.model.component.resumo.ResumoComponentRule;
import jakarta.validation.constraints.NotBlank;

public record ResumoComponentRuleRequest(
        @NotBlank String componentId,
        @NotBlank String headingStyleId,
        @NotBlank String headingText,
        @NotBlank String textStyleId,
        @NotBlank String keywordsStyleId,
        @NotBlank String keywordsLabel,
        @NotBlank String keywordsSeparator
) {
    public ResumoComponentRule toDomain() {
        return new ResumoComponentRule(componentId, headingStyleId, headingText,
                textStyleId, keywordsStyleId, keywordsLabel, keywordsSeparator);
    }
}
```

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/profile/AbstractComponentRuleRequest.java
package com.abntbuilder.formatter.api.export.dto.request.profile;

import com.abntbuilder.formatter.profile.model.component.abstracten.AbstractComponentRule;
import jakarta.validation.constraints.NotBlank;

public record AbstractComponentRuleRequest(
        @NotBlank String componentId,
        @NotBlank String headingStyleId,
        @NotBlank String headingText,
        @NotBlank String textStyleId,
        @NotBlank String keywordsStyleId,
        @NotBlank String keywordsLabel,
        @NotBlank String keywordsSeparator
) {
    public AbstractComponentRule toDomain() {
        return new AbstractComponentRule(componentId, headingStyleId, headingText,
                textStyleId, keywordsStyleId, keywordsLabel, keywordsSeparator);
    }
}
```

Em `DocumentContentRequest` adicionar:
```java
@Valid ResumoRequest resumo,
@Valid AbstractRequest abstractEn,
```

No método `toComponents()`:
```java
if (resumo != null) components.add(resumo.toDomain());
if (abstractEn != null) components.add(abstractEn.toDomain());
```

Em `RenderingConfig` adicionar:
```java
@Bean
public ResumoRenderer resumoRenderer() {
    return new ResumoRenderer();
}

@Bean
public AbstractRenderer abstractRenderer() {
    return new AbstractRenderer();
}
```

```json
"componentOrder": [
  "cover", "titlePage", "errata", "approvalSheet",
  "dedication", "acknowledgments", "epigraph",
  "resumo", "abstract",
  "bodyContent", ...
]
```

```bash
git commit -m "feat: add Resumo and Abstract pre-textual components"
```

---

## Task 6 — References (Referências)

**Files:**
- Create: `ReferenceEntry.java`, `ReferenceType.java`, `ReferenceAuthor.java`
- Create: `ReferencesComponent.java`
- Create: `ReferencesComponentRule.java`
- Create: `ReferencesEntryFormatter.java`
- Create: `ReferencesRenderer.java`
- Create: `ReferencesRequest.java`, `ReferenceEntryRequest.java`, `ReferenceAuthorRequest.java`
- Create: `ReferencesComponentRuleRequest.java`

- [ ] **Step 0: Adicionar `REFERENCES` ao enum `ComponentType`**

```java
// Acrescentar ao enum ComponentType em ComponentType.java:
REFERENCES
```

- [ ] **Step 1: Criar `ReferenceType`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/references/ReferenceType.java
package com.abntbuilder.formatter.document.component.references;

public enum ReferenceType {
    BOOK,
    BOOK_CHAPTER,
    JOURNAL,
    WEBSITE,
    LEGISLATION,
    THESIS
}
```

- [ ] **Step 2: Criar `ReferenceAuthor`**

```java
public record ReferenceAuthor(
        String surname,
        Optional<String> givenNames
) {
    public ReferenceAuthor {
        if (surname == null || surname.isBlank()) throw new IllegalArgumentException("surname must not be blank.");
        Objects.requireNonNull(givenNames, "givenNames must not be null");
    }

    // Renderização ABNT: SOBRENOME, Nome. ou SOBRENOME.
    public String renderedAbnt() {
        return givenNames
                .map(given -> surname.toUpperCase() + ", " + given + ".")
                .orElse(surname.toUpperCase() + ".");
    }
}
```

- [ ] **Step 3: Criar `ReferenceEntry`**

```java
public record ReferenceEntry(
        String id,
        ReferenceType type,
        List<ReferenceAuthor> authors,
        String title,
        Optional<String> subtitle,
        Optional<String> edition,
        Optional<String> city,
        Optional<String> publisher,
        String year,
        Optional<String> pages,
        Optional<String> url,
        Optional<String> accessDate
) {
    public ReferenceEntry { /* validações */ }
}
```

- [ ] **Step 4: Criar `ReferencesComponent`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/references/ReferencesComponent.java
package com.abntbuilder.formatter.document.component.references;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;
import java.util.List;
import java.util.Objects;

public record ReferencesComponent(List<ReferenceEntry> entries) implements DocumentComponent {
    public ReferencesComponent {
        Objects.requireNonNull(entries, "entries must not be null");
        if (entries.isEmpty()) throw new IllegalArgumentException("entries must not be empty.");
        entries = List.copyOf(entries);
    }
    @Override public ComponentType type() { return ComponentType.REFERENCES; }
}
```

- [ ] **Step 5: Criar `ReferencesEntryFormatter`**

Formata o texto de cada entrada por tipo, seguindo as regras da ABNT NBR 6023:

```java
// src/main/java/com/abntbuilder/formatter/rendering/component/references/ReferencesEntryFormatter.java
public final class ReferencesEntryFormatter {

    public String format(ReferenceEntry entry) {
        return switch (entry.type()) {
            case BOOK -> formatBook(entry);
            case BOOK_CHAPTER -> formatBookChapter(entry);
            case JOURNAL -> formatJournal(entry);
            case WEBSITE -> formatWebsite(entry);
            case LEGISLATION -> formatLegislation(entry);
            case THESIS -> formatThesis(entry);
        };
    }

    private String formatBook(ReferenceEntry e) {
        // AUTOR(ES). Título: subtítulo. Edição. Cidade: Editora, Ano.
        StringBuilder sb = new StringBuilder();
        sb.append(renderAuthors(e.authors()));
        sb.append(renderTitle(e.title(), e.subtitle()));
        e.edition().ifPresent(ed -> sb.append(" ").append(ed).append(" ed."));
        sb.append(". ");
        e.city().ifPresent(c -> sb.append(c).append(": "));
        e.publisher().ifPresent(p -> sb.append(p).append(", "));
        sb.append(e.year()).append(".");
        return sb.toString();
    }

    private String formatWebsite(ReferenceEntry e) {
        // AUTOR(ES). Título. Disponível em: URL. Acesso em: DATA.
        StringBuilder sb = new StringBuilder();
        sb.append(renderAuthors(e.authors()));
        sb.append(renderTitle(e.title(), e.subtitle()));
        e.url().ifPresent(u -> sb.append(" Disponível em: ").append(u).append("."));
        e.accessDate().ifPresent(d -> sb.append(" Acesso em: ").append(d).append("."));
        return sb.toString();
    }

    private String formatThesis(ReferenceEntry e) {
        // AUTOR. Título. Ano. Dissertação/Tese (grau) — Instituição, Cidade, Ano.
        StringBuilder sb = new StringBuilder();
        sb.append(renderAuthors(e.authors()));
        sb.append(renderTitle(e.title(), e.subtitle()));
        sb.append(". ").append(e.year()).append(".");
        // institution, degree — podem vir de campos adicionais (subtitle reutilizado aqui por ora)
        return sb.toString();
    }

    private String formatJournal(ReferenceEntry e) {
        // AUTOR. Título do artigo. Nome do periódico, v., n., p., ano.
        // (publisher = nome do periódico, pages = p.)
        StringBuilder sb = new StringBuilder();
        sb.append(renderAuthors(e.authors()));
        sb.append(renderTitle(e.title(), e.subtitle()));
        e.publisher().ifPresent(journal -> sb.append(" ").append(journal));
        e.pages().ifPresent(p -> sb.append(", p. ").append(p));
        sb.append(", ").append(e.year()).append(".");
        return sb.toString();
    }

    private String formatBookChapter(ReferenceEntry e) {
        // ABNT NBR 6023: AUTOR DO CAPÍTULO. Título do capítulo. In: AUTOR DO LIVRO. Título do livro. ed. Cidade: Editora, Ano. p. XX-XX.
        // ReferenceEntry reutiliza campos: title = título do capítulo, subtitle = título do livro, publisher = editora, pages = páginas
        // Para capítulo: o campo `url` é reutilizado para armazenar o autor do livro (ou adicionar campo específico em ReferenceEntry)
        StringBuilder sb = new StringBuilder();
        sb.append(renderAuthors(e.authors()));
        // título do capítulo em negrito — marcador para o renderer
        sb.append(renderTitle(e.title(), Optional.empty()));
        sb.append(" In: ");
        // autor do livro: reutilizar campo url para armazenar — ou adicionar campo bookAuthor em ReferenceEntry
        e.url().ifPresent(bookAuthor -> sb.append(bookAuthor.toUpperCase()).append(". "));
        // título do livro: reutilizar subtitle para armazenar título do livro
        e.subtitle().ifPresent(bookTitle -> sb.append("**").append(bookTitle).append("**. "));
        e.edition().ifPresent(ed -> sb.append(ed).append(" ed. "));
        e.city().ifPresent(c -> sb.append(c).append(": "));
        e.publisher().ifPresent(p -> sb.append(p).append(", "));
        sb.append(e.year()).append(".");
        e.pages().ifPresent(p -> sb.append(" p. ").append(p).append("."));
        return sb.toString();
    }

    // Nota para quem implementar: ReferenceEntry pode precisar de campos adicionais
    // (bookTitle, bookAuthors) para BOOK_CHAPTER em vez de reutilizar subtitle/url.
    // A alternativa mais limpa é adicionar Optional<String> bookTitle e
    // Optional<List<ReferenceAuthor>> bookAuthors em ReferenceEntry antes da Task 6.

    private String formatLegislation(ReferenceEntry e) {
        // título, ementa, data. url.
        StringBuilder sb = new StringBuilder();
        sb.append(e.title());
        e.subtitle().ifPresent(s -> sb.append(". ").append(s));
        sb.append(". ").append(e.year()).append(".");
        e.url().ifPresent(u -> sb.append(" Disponível em: ").append(u).append("."));
        return sb.toString();
    }

    private String renderAuthors(List<ReferenceAuthor> authors) {
        if (authors.isEmpty()) return "";
        if (authors.size() == 1) return authors.get(0).renderedAbnt() + " ";
        if (authors.size() <= 3) {
            return authors.stream()
                    .map(ReferenceAuthor::renderedAbnt)
                    .collect(java.util.stream.Collectors.joining("; ")) + " ";
        }
        return authors.get(0).renderedAbnt() + " et al. ";
    }

    private String renderTitle(String title, Optional<String> subtitle) {
        // Retorna o título entre marcadores "**" para que o renderer identifique o trecho em negrito
        return subtitle.map(s -> "**" + title + "**" + ": " + s)
                       .orElse("**" + title + "**");
    }
}
```

> **Negrito do título em referências — implementação concreta:**
>
> O `ReferencesEntryFormatter.format()` retorna strings com marcadores `**título**`. O `ReferencesRenderer` deve dividir cada string por esses marcadores e emitir múltiplos `DocxRun`:
>
> ```java
> // Em ReferencesRenderer.render(), substituir:
> //   blocks.add(new DocxParagraph(List.of(DocxRun.of(text, entryStyle)), entryStyle));
> // por:
> blocks.add(new DocxParagraph(buildReferenceRuns(text, entryStyle), entryStyle));
>
> private static List<DocxRun> buildReferenceRuns(String text, StyleRule baseStyle) {
>     List<DocxRun> runs = new ArrayList<>();
>     // Dividir em segmentos: texto normal e trechos entre **...**
>     String[] parts = text.split("\\*\\*", -1);
>     boolean bold = false;
>     for (String part : parts) {
>         if (!part.isEmpty()) {
>             InlineFormatting formatting = bold
>                     ? new InlineFormatting(Optional.of(true), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())
>                     : InlineFormatting.none();
>             runs.add(new DocxRun(part, baseStyle, formatting));
>         }
>         bold = !bold;
>     }
>     return runs;
> }
> ```

- [ ] **Step 6: Criar `ReferencesComponentRule`**

```java
// src/main/java/com/abntbuilder/formatter/profile/model/component/references/ReferencesComponentRule.java
package com.abntbuilder.formatter.profile.model.component.references;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import java.util.Map;

public record ReferencesComponentRule(
        String componentId,
        String headingStyleId,
        String headingText,
        String entryStyleId,
        int blankLinesBetweenEntries
) implements ComponentRule {
    public ReferencesComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(headingStyleId, "headingStyleId");
        requireNonBlank(headingText, "headingText");
        requireNonBlank(entryStyleId, "entryStyleId");
        if (blankLinesBetweenEntries < 0) throw new IllegalArgumentException("blankLinesBetweenEntries must be >= 0.");
    }
    @Override public Map<String, String> contentBindings() { return Map.of(); }
    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
```

- [ ] **Step 7: Criar `ReferencesRenderer`**

```java
// src/main/java/com/abntbuilder/formatter/rendering/component/references/ReferencesRenderer.java
package com.abntbuilder.formatter.rendering.component.references;

import com.abntbuilder.formatter.document.component.references.ReferenceEntry;
import com.abntbuilder.formatter.document.component.references.ReferencesComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlankLine;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.references.ReferencesComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;

import java.util.ArrayList;
import java.util.List;

public final class ReferencesRenderer implements ComponentRenderer<ReferencesComponent> {

    public static final String COMPONENT_ID = "references";
    private final ReferencesEntryFormatter formatter = new ReferencesEntryFormatter();

    @Override public String componentId() { return COMPONENT_ID; }
    @Override public Class<ReferencesComponent> componentType() { return ReferencesComponent.class; }

    @Override
    public List<DocxBlock> render(ReferencesComponent component, DocumentProfile profile) {
        ReferencesComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(COMPONENT_ID, ReferencesComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());
        StyleRule entryStyle = styleResolver.resolve(rule.entryStyleId());
        StyleRule blankStyle = entryStyle; // usar mesmo estilo para linhas em branco

        List<DocxBlock> blocks = new ArrayList<>();
        blocks.add(new DocxParagraph(
                List.of(DocxRun.of(rule.headingText(), headingStyle)), headingStyle
        ));

        boolean first = true;
        for (ReferenceEntry entry : component.entries()) {
            if (!first && rule.blankLinesBetweenEntries() > 0) {
                for (int i = 0; i < rule.blankLinesBetweenEntries(); i++) {
                    blocks.add(new DocxBlankLine(blankStyle));
                }
            }
            String text = formatter.format(entry);
            blocks.add(new DocxParagraph(buildReferenceRuns(text, entryStyle), entryStyle));
            first = false;
        }

        return List.copyOf(blocks);
    }
}
```

- [ ] **Step 8: Criar requests, registrar renderer, adicionar ao perfil e componentOrder**

```json
"references": {
  "componentId": "references",
  "headingStyleId": "references.heading",
  "headingText": "REFERÊNCIAS",
  "entryStyleId": "references.entry",
  "blankLinesBetweenEntries": 1
}
```

- [ ] **Step 9: Criar samples**

```
docs/samples/references/references-book.json        — livros com 1, 2 e 3+ autores
docs/samples/references/references-mixed.json       — mistura de todos os tipos
docs/samples/references/references-empty-invalid.json
```

- [ ] **Step 10: Escrever `ReferencesEntryFormatterTest`**

```java
class ReferencesEntryFormatterTest {
    private final ReferencesEntryFormatter formatter = new ReferencesEntryFormatter();

    @Test void shouldFormatBookWithOneAuthor() { ... }
    @Test void shouldFormatBookWithThreeAuthors() { ... }
    @Test void shouldFormatBookWithMoreThanThreeAuthors() { ... }
    @Test void shouldFormatWebsiteWithAccessDate() { ... }
    @Test void shouldFormatJournalArticle() { ... }
}
```

- [ ] **Step 11: Compilar e rodar**

```bash
mvn compile -q && mvn test -q
```

- [ ] **Step 12: Commit**

```bash
git add -A
git commit -m "feat: add References component with ABNT NBR 6023 entry formatting"
```

---

## Task 7 — Appendix e Annex

**Files:**
- Create: `AppendixItem.java`, `AppendixComponent.java`, `AnnexItem.java`, `AnnexComponent.java`
- Create: `AppendixComponentRule.java`, `AnnexComponentRule.java`
- Create: `AppendixRenderer.java`, `AnnexRenderer.java`
- Create: `AppendixRequest.java`, `AppendixItemRequest.java`, `AnnexRequest.java`, `AnnexItemRequest.java`
- Create: `AppendixComponentRuleRequest.java`, `AnnexComponentRuleRequest.java`

- [ ] **Step 0: Adicionar `APPENDIX` e `ANNEX` ao enum `ComponentType`**

```java
// Acrescentar ao enum ComponentType em ComponentType.java:
APPENDIX,
ANNEX
```

- [ ] **Step 1: Criar `AppendixItem`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/appendix/AppendixItem.java
package com.abntbuilder.formatter.document.component.appendix;

import com.abntbuilder.formatter.document.component.bodycontent.BodySection;
import java.util.List;
import java.util.Objects;

public record AppendixItem(
        String title,
        List<BodySection> sections
) {
    public AppendixItem {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title must not be blank.");
        Objects.requireNonNull(sections, "sections must not be null");
        sections = List.copyOf(sections);
    }
}
```

- [ ] **Step 2: Criar `AppendixComponent`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/appendix/AppendixComponent.java
package com.abntbuilder.formatter.document.component.appendix;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;
import java.util.List;
import java.util.Objects;

public record AppendixComponent(List<AppendixItem> items) implements DocumentComponent {
    public AppendixComponent {
        Objects.requireNonNull(items, "items must not be null");
        if (items.isEmpty()) throw new IllegalArgumentException("items must not be empty.");
        items = List.copyOf(items);
    }
    @Override public ComponentType type() { return ComponentType.APPENDIX; }
}
```

`AnnexComponent` e `AnnexItem` são idênticos, com pacote `annex` e `type()` retornando `ComponentType.ANNEX`:

```java
// src/main/java/com/abntbuilder/formatter/document/component/annex/AnnexComponent.java
package com.abntbuilder.formatter.document.component.annex;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;
import java.util.List;
import java.util.Objects;

public record AnnexComponent(List<AnnexItem> items) implements DocumentComponent {
    public AnnexComponent {
        Objects.requireNonNull(items, "items must not be null");
        if (items.isEmpty()) throw new IllegalArgumentException("items must not be empty.");
        items = List.copyOf(items);
    }
    @Override public ComponentType type() { return ComponentType.ANNEX; }
}
```

- [ ] **Step 3: Criar `AppendixComponentRule`**

```java
// src/main/java/com/abntbuilder/formatter/profile/model/component/appendix/AppendixComponentRule.java
package com.abntbuilder.formatter.profile.model.component.appendix;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record AppendixComponentRule(
        String componentId,
        String headingTemplate,    // "APÊNDICE {letter} — {title}"
        String headingStyleId,
        String paragraphStyleId,
        List<String> sectionTitleStyleIdsByLevel
) implements ComponentRule {
    public AppendixComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(headingTemplate, "headingTemplate");
        requireNonBlank(headingStyleId, "headingStyleId");
        requireNonBlank(paragraphStyleId, "paragraphStyleId");
        Objects.requireNonNull(sectionTitleStyleIdsByLevel, "sectionTitleStyleIdsByLevel must not be null");
        sectionTitleStyleIdsByLevel = List.copyOf(sectionTitleStyleIdsByLevel);
    }
    @Override public Map<String, String> contentBindings() { return Map.of(); }
    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
```

`AnnexComponentRule` — código idêntico a `AppendixComponentRule`, apenas com pacote `annex` e mensagem de erro diferente:

```java
// src/main/java/com/abntbuilder/formatter/profile/model/component/annex/AnnexComponentRule.java
package com.abntbuilder.formatter.profile.model.component.annex;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record AnnexComponentRule(
        String componentId,
        String headingTemplate,
        String headingStyleId,
        String paragraphStyleId,
        List<String> sectionTitleStyleIdsByLevel
) implements ComponentRule {
    public AnnexComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(headingTemplate, "headingTemplate");
        requireNonBlank(headingStyleId, "headingStyleId");
        requireNonBlank(paragraphStyleId, "paragraphStyleId");
        Objects.requireNonNull(sectionTitleStyleIdsByLevel, "sectionTitleStyleIdsByLevel must not be null");
        sectionTitleStyleIdsByLevel = List.copyOf(sectionTitleStyleIdsByLevel);
    }
    @Override public String componentId() { return componentId; }
    @Override public Map<String, String> contentBindings() { return Map.of(); }
    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
```

- [ ] **Step 4: Criar `AppendixRenderer`**

O renderer calcula a letra (A, B, C...) com base no índice. Para cada item:
1. Emite o heading: template com `{letter}` e `{title}` preenchidos
2. Reutiliza `BodyContentRenderer.renderContentBlock()` (método estático) para os blocos das seções

```java
// src/main/java/com/abntbuilder/formatter/rendering/component/appendix/AppendixRenderer.java
package com.abntbuilder.formatter.rendering.component.appendix;

import com.abntbuilder.formatter.document.component.appendix.AppendixComponent;
import com.abntbuilder.formatter.document.component.appendix.AppendixItem;
import com.abntbuilder.formatter.document.component.bodycontent.BodyContentComponent;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.appendix.AppendixComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyContentRenderer;

import java.util.ArrayList;
import java.util.List;

public final class AppendixRenderer implements ComponentRenderer<AppendixComponent> {

    public static final String COMPONENT_ID = "appendix";

    @Override public String componentId() { return COMPONENT_ID; }
    @Override public Class<AppendixComponent> componentType() { return AppendixComponent.class; }

    @Override
    public List<DocxBlock> render(AppendixComponent component, DocumentProfile profile) {
        AppendixComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(COMPONENT_ID, AppendixComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        char letter = 'A';
        for (AppendixItem item : component.items()) {
            String heading = rule.headingTemplate()
                    .replace("{letter}", String.valueOf(letter))
                    .replace("{title}", item.title());
            blocks.add(new DocxParagraph(List.of(DocxRun.of(heading, headingStyle)), headingStyle));

            // Reutilizar BodyContentRenderer para renderizar as seções internas.
            // As seções em AppendixItem devem começar em level 1.
            if (!item.sections().isEmpty()) {
                BodyContentComponent appendixContent = new BodyContentComponent(item.sections());
                BodyContentRenderer contentRenderer = new BodyContentRenderer();
                List<DocxBlock> sectionBlocks = contentRenderer.render(appendixContent, profile);
                blocks.addAll(sectionBlocks);
            }
            letter++;
        }
        return List.copyOf(blocks);
    }
}
```

> **⚠️ `renderContentBlock` é `private static` em `BodyContentRenderer`** — não é acessível externamente. O `AppendixRenderer` instancia um `BodyContentRenderer` e chama `render()` com um `BodyContentComponent` construído a partir das seções do item de apêndice.
>
> **Problema de hierarquia:** `BodyContentComponent` valida que as seções obedecem à hierarquia (seção filha tem level = parent.level + 1). As seções internas de um apêndice podem começar em level 2, o que falharia na validação do `BodyContentComponent`.
>
> **Solução concreta:** as `sections` em `AppendixItem` devem começar em level 1 (o título do apêndice já é renderizado separadamente pelo heading template). O request JSON deve refletir isso. No `AppendixRenderer`, montar o `BodyContentComponent` normalmente:
>
> ```java
> // Em AppendixRenderer.render(), dentro do loop sobre component.items():
> // Montar um BodyContentComponent com as seções do item
> BodyContentComponent appendixContent = new BodyContentComponent(item.sections());
> // Instanciar um novo BodyContentRenderer (não singleton — instância local)
> BodyContentRenderer contentRenderer = new BodyContentRenderer();
> List<DocxBlock> sectionBlocks = contentRenderer.render(appendixContent, profile);
> blocks.addAll(sectionBlocks);
> ```
>
> **Atenção:** `BodyContentRenderer.render()` usa `bodyContentRule(profile)` para buscar a rule — isso significa que os estilos e numeração do apêndice seguem a mesma rule do `bodyContent`. Isso é aceitável para esta fase (seções do apêndice usam os mesmos estilos de parágrafo e seção que o corpo). Se o apêndice precisar de estilos diferentes, criar uma `AppendixBodyContentRule` no perfil — mas isso é refinamento futuro.

- [ ] **Step 5: Criar requests, registrar renderers, adicionar ao perfil e componentOrder**

```json
"appendix": {
  "componentId": "appendix",
  "headingTemplate": "APÊNDICE {letter} — {title}",
  "headingStyleId": "appendix.heading",
  "paragraphStyleId": "bodyContent.paragraph",
  "sectionTitleStyleIdsByLevel": ["bodyContent.section.level1", "bodyContent.section.level2"]
}
```

- [ ] **Step 6: Criar samples**

```
docs/samples/appendix/appendix-single.json
docs/samples/appendix/appendix-multiple.json
docs/samples/annex/annex-simple.json
```

- [ ] **Step 7: Compilar e rodar**

```bash
mvn compile -q && mvn test -q
```

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "feat: add Appendix and Annex components with automatic letter numbering"
```

---

## Task 8 — Glossário

**Files:**
- Create: `GlossaryEntry.java`, `GlossaryComponent.java`
- Create: `GlossaryComponentRule.java`
- Create: `GlossaryRenderer.java`
- Create: `GlossaryRequest.java`, `GlossaryEntryRequest.java`
- Create: `GlossaryComponentRuleRequest.java`

- [ ] **Step 0: Adicionar `GLOSSARY` ao enum `ComponentType`**

```java
// Acrescentar ao enum ComponentType em ComponentType.java:
GLOSSARY
```

- [ ] **Step 1: Criar domínio**

```java
// src/main/java/com/abntbuilder/formatter/document/component/glossary/GlossaryEntry.java
package com.abntbuilder.formatter.document.component.glossary;

public record GlossaryEntry(String term, String definition) {
    public GlossaryEntry {
        requireNonBlank(term, "term");
        requireNonBlank(definition, "definition");
    }
    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
```

```java
// src/main/java/com/abntbuilder/formatter/document/component/glossary/GlossaryComponent.java
package com.abntbuilder.formatter.document.component.glossary;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;
import java.util.List;
import java.util.Objects;

public record GlossaryComponent(List<GlossaryEntry> entries) implements DocumentComponent {
    public GlossaryComponent {
        Objects.requireNonNull(entries, "entries must not be null");
        if (entries.isEmpty()) throw new IllegalArgumentException("entries must not be empty.");
        entries = List.copyOf(entries);
    }
    @Override public ComponentType type() { return ComponentType.GLOSSARY; }
}
```

- [ ] **Step 2: Criar `GlossaryComponentRule`**

```java
// src/main/java/com/abntbuilder/formatter/profile/model/component/glossary/GlossaryComponentRule.java
package com.abntbuilder.formatter.profile.model.component.glossary;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;
import java.util.Map;

public record GlossaryComponentRule(
        String componentId,
        String headingStyleId,
        String headingText,     // "GLOSSÁRIO"
        String entryStyleId,
        String termSeparator    // " — "
) implements ComponentRule {
    public GlossaryComponentRule {
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

- [ ] **Step 3: Criar `GlossaryRenderer`**

```java
// src/main/java/com/abntbuilder/formatter/rendering/component/glossary/GlossaryRenderer.java
package com.abntbuilder.formatter.rendering.component.glossary;

import com.abntbuilder.formatter.document.component.glossary.GlossaryComponent;
import com.abntbuilder.formatter.document.component.glossary.GlossaryEntry;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.glossary.GlossaryComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;

import java.util.ArrayList;
import java.util.List;

public final class GlossaryRenderer implements ComponentRenderer<GlossaryComponent> {

    public static final String COMPONENT_ID = "glossary";

    @Override public String componentId() { return COMPONENT_ID; }
    @Override public Class<GlossaryComponent> componentType() { return GlossaryComponent.class; }

    @Override
    public List<DocxBlock> render(GlossaryComponent component, DocumentProfile profile) {
        GlossaryComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(COMPONENT_ID, GlossaryComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);
        StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());
        StyleRule entryStyle = styleResolver.resolve(rule.entryStyleId());

        List<DocxBlock> blocks = new ArrayList<>();
        blocks.add(new DocxParagraph(List.of(DocxRun.of(rule.headingText(), headingStyle)), headingStyle));
        for (GlossaryEntry entry : component.entries()) {
            String text = entry.term() + rule.termSeparator() + entry.definition();
            blocks.add(new DocxParagraph(List.of(DocxRun.of(text, entryStyle)), entryStyle));
        }
        return List.copyOf(blocks);
    }
}
```

- [ ] **Step 4: Requests, registro, perfil, componentOrder, sample, testes, compilar, commit**

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/GlossaryEntryRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.glossary.GlossaryEntry;
import jakarta.validation.constraints.NotBlank;

public record GlossaryEntryRequest(
        @NotBlank String term,
        @NotBlank String definition
) {
    public GlossaryEntry toDomain() {
        return new GlossaryEntry(term, definition);
    }
}
```

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/GlossaryRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.glossary.GlossaryComponent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record GlossaryRequest(
        @NotEmpty @Valid List<GlossaryEntryRequest> entries
) {
    public GlossaryComponent toDomain() {
        return new GlossaryComponent(entries.stream().map(GlossaryEntryRequest::toDomain).toList());
    }
}
```

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/profile/GlossaryComponentRuleRequest.java
package com.abntbuilder.formatter.api.export.dto.request.profile;

import com.abntbuilder.formatter.profile.model.component.glossary.GlossaryComponentRule;
import jakarta.validation.constraints.NotBlank;

public record GlossaryComponentRuleRequest(
        @NotBlank String componentId,
        @NotBlank String headingStyleId,
        @NotBlank String headingText,
        @NotBlank String entryStyleId,
        @NotBlank String termSeparator
) {
    public GlossaryComponentRule toDomain() {
        return new GlossaryComponentRule(componentId, headingStyleId, headingText, entryStyleId, termSeparator);
    }
}
```

Em `DocumentContentRequest` adicionar:
```java
@Valid GlossaryRequest glossary,
```

No método `toComponents()`:
```java
if (glossary != null) components.add(glossary.toDomain());
```

Em `RenderingConfig` adicionar:
```java
@Bean
public GlossaryRenderer glossaryRenderer() {
    return new GlossaryRenderer();
}
```

Em `abnt-unip-profile.json`, em `componentRules`:
```json
"glossary": {
  "componentId": "glossary",
  "headingStyleId": "glossary.heading",
  "headingText": "GLOSSÁRIO",
  "entryStyleId": "glossary.entry",
  "termSeparator": " — "
}
```

Em `styleRules`:
```json
{ "id": "glossary.heading", "fontFamily": "Times New Roman", "fontSizePt": 12, "bold": true, "uppercase": true, "alignment": "CENTER", "lineHeightRule": "EXACT", "lineHeightPt": 18 },
{ "id": "glossary.entry",   "fontFamily": "Times New Roman", "fontSizePt": 12, "bold": false, "uppercase": false, "alignment": "JUSTIFY", "lineHeightRule": "EXACT", "lineHeightPt": 18 }
```

Sample JSON mínimo (`docs/samples/glossary/glossary-simple.json`):
```json
{
  "document": {
    "glossary": {
      "entries": [
        { "term": "ABNT", "definition": "Associação Brasileira de Normas Técnicas." },
        { "term": "NBR", "definition": "Norma Brasileira Regulamentadora." }
      ]
    }
  }
}
```

```bash
git commit -m "feat: add Glossary post-textual component"
```

---

## Task 9 — Suite final e validação visual

- [ ] **Step 1: Rodar suite completa**

```bash
mvn test -q
```

Esperado: todos os testes passam.

- [ ] **Step 2: Gerar e verificar no Word para cada componente novo**

Para cada renderer novo, gerar DOCX do sample correspondente e verificar:

- Errata: tabela de erros formatada com template
- Dedicatória: texto na metade inferior direita
- Epígrafe: texto recuado à direita com autoria
- Agradecimentos: heading + texto
- Resumo: "RESUMO" + texto + "Palavras-chave: ..."
- Abstract: "ABSTRACT" + texto + "Keywords: ..."
- Referências: "REFERÊNCIAS" + entradas formatadas por tipo ABNT
- Apêndice: "APÊNDICE A — Título" + conteúdo
- Anexo: "ANEXO A — Título" + conteúdo
- Glossário: "GLOSSÁRIO" + entradas com separador

- [ ] **Step 3: Atualizar `componentOrder` final em `abnt-unip-profile.json`**

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
  "bodyContent",
  "references",
  "appendix",
  "annex",
  "glossary"
]
```

- [ ] **Step 4: Commit final**

```bash
git add -A
git commit -m "feat: complete Fase 4 — all pre and post-textual components implemented"
```

---

## Checklist de conclusão da Fase 4

| Componente | Família | Task |
|---|---|---|
| Errata | flow | Task 1 |
| Dedicatória | flow (posição direita) | Task 2 |
| Epígrafe | flow (recuado) | Task 3 |
| Agradecimentos | flow | Task 4 |
| Resumo | flow | Task 5 |
| Abstract | flow | Task 5 |
| Referências (`ReferencesEntryFormatter` por tipo ABNT) | flow | Task 6 |
| Apêndice (letras automáticas A, B, C...) | flow | Task 7 |
| Anexo | flow | Task 7 |
| Glossário | flow | Task 8 |
| Todos registrados em `ComponentRendererRegistry` | Tasks 1–8 |
| Todos em `componentOrder` do perfil | Task 9 |
| Samples válidos e inválidos para cada um | Tasks 1–8 |
| Suite completa verde | Task 9 |
| Validação visual no Word | Task 9 |
