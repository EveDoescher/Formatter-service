# bodyContent Fase 1 — Correções + Fundação Inline

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Corrigir todos os desvios documentados no bodyContent e adicionar suporte a formatação inline (negrito, itálico, sublinhado) via modelo de multi-run.

**Architecture:** Abordagem inline-model-first — `DocxParagraph` passa de `String text` para `List<DocxRun>`, cada `BodyInline` gera seu próprio run com `InlineFormatting` opcional. `BodyCitation` (bloco autônomo) é removido; apenas `BodyLongQuote` permanece como bloco. Tokens de citação saem do domínio e vão para `CitationFormattingRule` no perfil.

**Tech Stack:** Java 21, Spring Boot, docx4j 11.5.13, JUnit 5, MockMvc

---

## Mapa de arquivos

### Novos
- `src/main/java/com/abntbuilder/formatter/document/component/bodycontent/InlineFormatting.java`
- `src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyLongQuote.java`
- `src/main/java/com/abntbuilder/formatter/output/docx/api/DocxRun.java`
- `src/main/java/com/abntbuilder/formatter/profile/model/component/bodycontent/CitationFormattingRule.java`
- `src/main/java/com/abntbuilder/formatter/api/export/dto/request/CitationFormattingRuleRequest.java`
- `src/main/java/com/abntbuilder/formatter/api/export/dto/request/InlineFormattingRequest.java`
- `src/main/java/com/abntbuilder/formatter/shared/exception/InvalidBodyContentException.java`
- `src/test/java/com/abntbuilder/formatter/document/component/bodycontent/InlineFormattingTest.java`
- `src/test/java/com/abntbuilder/formatter/document/component/bodycontent/BodyLongQuoteTest.java`
- `src/test/java/com/abntbuilder/formatter/profile/model/component/bodycontent/CitationFormattingRuleTest.java`
- `src/test/java/com/abntbuilder/formatter/api/export/dto/request/CitationFormattingRuleRequestTest.java`
- `src/test/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyContentRendererDocxSanityTest.java`
- `docs/samples/body-content/body-content-inline-formatting.json`
- `docs/samples/body-content/body-content-inline-formatting-invalid.json`

### Modificados
- `src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyBlock.java`
- `src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyText.java`
- `src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyQuoteText.java`
- `src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyCitationCall.java`
- `src/main/java/com/abntbuilder/formatter/document/component/bodycontent/CitationSource.java`
- `src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyContentComponent.java`
- `src/main/java/com/abntbuilder/formatter/profile/model/component/bodycontent/BodyContentComponentRule.java`
- `src/main/java/com/abntbuilder/formatter/api/export/dto/request/BodyBlockType.java`
- `src/main/java/com/abntbuilder/formatter/api/export/dto/request/BodyBlockRequest.java`
- `src/main/java/com/abntbuilder/formatter/api/export/dto/request/BodyInlineRequest.java`
- `src/main/java/com/abntbuilder/formatter/api/export/dto/request/BodyContentRequest.java`
- `src/main/java/com/abntbuilder/formatter/api/export/dto/request/BodyContentComponentRuleRequest.java`
- `src/main/java/com/abntbuilder/formatter/api/export/dto/request/FigureRuleRequest.java`
- `src/main/java/com/abntbuilder/formatter/api/export/dto/request/TableRuleRequest.java`
- `src/main/java/com/abntbuilder/formatter/output/docx/api/DocxParagraph.java`
- `src/main/java/com/abntbuilder/formatter/output/docx/docx4j/Docx4jWriter.java`
- `src/main/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyContentRenderer.java`
- `src/main/resources/profiles/abnt-unip-profile.json`
- `src/test/java/com/abntbuilder/formatter/document/component/bodycontent/BodyCitationTest.java` (deletar)
- `src/test/java/com/abntbuilder/formatter/document/component/bodycontent/BodyCitationCallTest.java`
- `src/test/java/com/abntbuilder/formatter/document/component/bodycontent/BodyParagraphTest.java`
- `src/test/java/com/abntbuilder/formatter/api/export/dto/request/BodySectionRequestTest.java`
- `src/test/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyContentRendererTest.java`
- `src/test/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyContentSampleValidationTest.java`

### Removidos
- `src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyCitation.java`

---

## Task 1: `InvalidBodyContentException`

**Files:**
- Create: `src/main/java/com/abntbuilder/formatter/shared/exception/InvalidBodyContentException.java`

- [ ] **Step 1: Escrever o teste**

```java
// src/test/java/com/abntbuilder/formatter/shared/exception/InvalidBodyContentExceptionTest.java
package com.abntbuilder.formatter.shared.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class InvalidBodyContentExceptionTest {

    @Test
    void shouldExtendIllegalArgumentException() {
        InvalidBodyContentException ex = new InvalidBodyContentException("test message");
        assertThat(ex).isInstanceOf(IllegalArgumentException.class);
        assertThat(ex.getMessage()).isEqualTo("test message");
    }
}
```

- [ ] **Step 2: Rodar e confirmar falha**

```bash
cd /mnt/c/Users/evelynnd/Documents/Projetos/Formatter-service
mvn test -pl . -Dtest=InvalidBodyContentExceptionTest -q
```
Esperado: `FAIL` — class not found.

- [ ] **Step 3: Implementar**

```java
// src/main/java/com/abntbuilder/formatter/shared/exception/InvalidBodyContentException.java
package com.abntbuilder.formatter.shared.exception;

public class InvalidBodyContentException extends IllegalArgumentException {

    public InvalidBodyContentException(String message) {
        super(message);
    }
}
```

- [ ] **Step 4: Rodar e confirmar verde**

```bash
mvn test -pl . -Dtest=InvalidBodyContentExceptionTest -q
```

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/abntbuilder/formatter/shared/exception/InvalidBodyContentException.java \
        src/test/java/com/abntbuilder/formatter/shared/exception/InvalidBodyContentExceptionTest.java
git commit -m "feat: add InvalidBodyContentException"
```

---

## Task 2: `InlineFormatting`

**Files:**
- Create: `src/main/java/com/abntbuilder/formatter/document/component/bodycontent/InlineFormatting.java`
- Create: `src/test/java/com/abntbuilder/formatter/document/component/bodycontent/InlineFormattingTest.java`

- [ ] **Step 1: Escrever o teste**

```java
// src/test/java/com/abntbuilder/formatter/document/component/bodycontent/InlineFormattingTest.java
package com.abntbuilder.formatter.document.component.bodycontent;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

class InlineFormattingTest {

    @Test
    void noneShouldHaveAllEmpty() {
        InlineFormatting f = InlineFormatting.none();
        assertThat(f.bold()).isEmpty();
        assertThat(f.italic()).isEmpty();
        assertThat(f.underline()).isEmpty();
    }

    @Test
    void shouldPreserveExplicitValues() {
        InlineFormatting f = new InlineFormatting(
                Optional.of(true), Optional.of(false), Optional.of(true)
        );
        assertThat(f.bold()).contains(true);
        assertThat(f.italic()).contains(false);
        assertThat(f.underline()).contains(true);
    }
}
```

- [ ] **Step 2: Rodar e confirmar falha**

```bash
mvn test -pl . -Dtest=InlineFormattingTest -q
```

- [ ] **Step 3: Implementar**

```java
// src/main/java/com/abntbuilder/formatter/document/component/bodycontent/InlineFormatting.java
package com.abntbuilder.formatter.document.component.bodycontent;

import java.util.Objects;
import java.util.Optional;

public record InlineFormatting(
        Optional<Boolean> bold,
        Optional<Boolean> italic,
        Optional<Boolean> underline
) {

    public InlineFormatting {
        Objects.requireNonNull(bold, "bold must not be null");
        Objects.requireNonNull(italic, "italic must not be null");
        Objects.requireNonNull(underline, "underline must not be null");
    }

    public static InlineFormatting none() {
        return new InlineFormatting(Optional.empty(), Optional.empty(), Optional.empty());
    }
}
```

- [ ] **Step 4: Rodar e confirmar verde**

```bash
mvn test -pl . -Dtest=InlineFormattingTest -q
```

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/abntbuilder/formatter/document/component/bodycontent/InlineFormatting.java \
        src/test/java/com/abntbuilder/formatter/document/component/bodycontent/InlineFormattingTest.java
git commit -m "feat: add InlineFormatting record"
```

---

## Task 3: `DocxRun` e mudança em `DocxParagraph`

**Files:**
- Create: `src/main/java/com/abntbuilder/formatter/output/docx/api/DocxRun.java`
- Modify: `src/main/java/com/abntbuilder/formatter/output/docx/api/DocxParagraph.java`

> **Atenção:** `DocxParagraph` é usado em `SinglePageLayoutRenderer` e `DocumentRenderer` (legado de `paragraphs`). Esses callers precisam ser atualizados nesta task também.

- [ ] **Step 1: Criar `DocxRun`**

```java
// src/main/java/com/abntbuilder/formatter/output/docx/api/DocxRun.java
package com.abntbuilder.formatter.output.docx.api;

import com.abntbuilder.formatter.document.component.bodycontent.InlineFormatting;
import com.abntbuilder.formatter.profile.model.StyleRule;

import java.util.Objects;

public record DocxRun(
        String text,
        StyleRule baseStyle,
        InlineFormatting formatting
) {

    public DocxRun {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank.");
        }
        Objects.requireNonNull(baseStyle, "baseStyle must not be null");
        Objects.requireNonNull(formatting, "formatting must not be null");
    }

    public static DocxRun of(String text, StyleRule baseStyle) {
        return new DocxRun(text, baseStyle, InlineFormatting.none());
    }
}
```

- [ ] **Step 2: Atualizar `DocxParagraph` para aceitar `List<DocxRun>`**

Substituir o conteúdo de `DocxParagraph.java` por:

```java
// src/main/java/com/abntbuilder/formatter/output/docx/api/DocxParagraph.java
package com.abntbuilder.formatter.output.docx.api;

import com.abntbuilder.formatter.profile.model.StyleRule;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record DocxParagraph(
        List<DocxRun> runs,
        StyleRule styleRule,
        Optional<BigDecimal> spacingBeforeOverridePt,
        Optional<BigDecimal> exactLineHeightPt,
        Optional<ParagraphLayoutOverride> layoutOverride,
        boolean keepWithNext,
        boolean keepLines
) implements DocxBlock {

    public DocxParagraph(List<DocxRun> runs, StyleRule styleRule) {
        this(runs, styleRule, Optional.empty(), Optional.empty(), Optional.empty(), false, false);
    }

    public DocxParagraph(List<DocxRun> runs, StyleRule styleRule, Optional<BigDecimal> spacingBeforeOverridePt) {
        this(runs, styleRule, spacingBeforeOverridePt, Optional.empty(), Optional.empty(), false, false);
    }

    public DocxParagraph(
            List<DocxRun> runs,
            StyleRule styleRule,
            Optional<BigDecimal> spacingBeforeOverridePt,
            Optional<BigDecimal> exactLineHeightPt
    ) {
        this(runs, styleRule, spacingBeforeOverridePt, exactLineHeightPt, Optional.empty(), false, false);
    }

    public DocxParagraph(
            List<DocxRun> runs,
            StyleRule styleRule,
            Optional<BigDecimal> spacingBeforeOverridePt,
            Optional<BigDecimal> exactLineHeightPt,
            Optional<ParagraphLayoutOverride> layoutOverride
    ) {
        this(runs, styleRule, spacingBeforeOverridePt, exactLineHeightPt, layoutOverride, false, false);
    }

    public DocxParagraph {
        Objects.requireNonNull(runs, "runs must not be null");
        if (runs.isEmpty()) {
            throw new IllegalArgumentException("runs must not be empty.");
        }
        runs = List.copyOf(runs);
        Objects.requireNonNull(styleRule, "styleRule must not be null");
        Objects.requireNonNull(spacingBeforeOverridePt, "spacingBeforeOverridePt must not be null");
        Objects.requireNonNull(exactLineHeightPt, "exactLineHeightPt must not be null");
        Objects.requireNonNull(layoutOverride, "layoutOverride must not be null");

        spacingBeforeOverridePt.ifPresent(value -> {
            if (value.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("spacingBeforeOverridePt must not be negative.");
            }
        });

        exactLineHeightPt.ifPresent(value -> {
            if (value.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("exactLineHeightPt must be greater than zero.");
            }
        });
    }
}
```

- [ ] **Step 3: Corrigir `SinglePageLayoutRenderer` para usar `DocxRun.of`**

Em `src/main/java/com/abntbuilder/formatter/rendering/layout/singlepage/SinglePageLayoutRenderer.java`, linha 34, trocar:

```java
blocks.add(new DocxParagraph(
        textLines.paragraphText(),
        textLines.styleRule(),
        Optional.empty(),
        Optional.of(textLines.exactLineHeightPt()),
        Optional.of(textLines.layoutOverride())
));
```

Por:

```java
blocks.add(new DocxParagraph(
        List.of(DocxRun.of(textLines.paragraphText(), textLines.styleRule())),
        textLines.styleRule(),
        Optional.empty(),
        Optional.of(textLines.exactLineHeightPt()),
        Optional.of(textLines.layoutOverride())
));
```

Adicionar import: `import com.abntbuilder.formatter.output.docx.api.DocxRun;` e `import java.util.List;`

- [ ] **Step 4: Corrigir `DocumentRenderer` (legado paragraphs)**

Em `src/main/java/com/abntbuilder/formatter/rendering/document/DocumentRenderer.java`, trocar:

```java
.map(paragraph -> new DocxParagraph(
        paragraph.text(),
        styleResolver.resolve(paragraph.styleId())
))
```

Por:

```java
.map(paragraph -> new DocxParagraph(
        List.of(DocxRun.of(paragraph.text(), styleResolver.resolve(paragraph.styleId()))),
        styleResolver.resolve(paragraph.styleId())
))
```

Adicionar imports necessários: `DocxRun`, `java.util.List`.

- [ ] **Step 5: Compilar**

```bash
mvn compile -q
```
Esperado: BUILD SUCCESS. Corrigir qualquer erro de compilação antes de continuar.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/abntbuilder/formatter/output/docx/api/DocxRun.java \
        src/main/java/com/abntbuilder/formatter/output/docx/api/DocxParagraph.java \
        src/main/java/com/abntbuilder/formatter/rendering/layout/singlepage/SinglePageLayoutRenderer.java \
        src/main/java/com/abntbuilder/formatter/rendering/document/DocumentRenderer.java
git commit -m "feat: replace DocxParagraph string text with list of DocxRun"
```

---

## Task 4: Atualizar `Docx4jWriter` para emitir runs individuais

**Files:**
- Modify: `src/main/java/com/abntbuilder/formatter/output/docx/docx4j/Docx4jWriter.java`

- [ ] **Step 1: Localizar o método que renderiza parágrafos**

```bash
grep -n "DocxParagraph\|createRun\|addRun\|P paragraph\|paragraph.getText" \
  src/main/java/com/abntbuilder/formatter/output/docx/docx4j/Docx4jWriter.java | head -20
```

- [ ] **Step 2: Substituir criação de parágrafo para iterar runs**

Localizar o bloco que cria um `<w:p>` a partir de `DocxParagraph`. Ele hoje chama algo como `createRun(paragraph.text(), styleRule)`. Substituir por um loop sobre `paragraph.runs()`:

```java
// Dentro do método que processa DocxParagraph — localizar pelo grep acima
P p = objectFactory.createP();
p.setPPr(createParagraphProperties(
        docxParagraph.styleRule(),
        docxParagraph.spacingBeforeOverridePt(),
        docxParagraph.exactLineHeightPt(),
        docxParagraph.layoutOverride()
));

for (DocxRun run : docxParagraph.runs()) {
    R r = objectFactory.createR();
    RPr rPr = buildRunProperties(run.baseStyle(), run.formatting());
    r.setRPr(rPr);

    Text t = objectFactory.createText();
    t.setValue(resolveText(run.text(), run.baseStyle()));
    t.setSpace("preserve");
    r.getContent().add(t);
    p.getContent().add(r);
}
```

- [ ] **Step 3: Criar `buildRunProperties` que aplica overrides de `InlineFormatting`**

Adicionar método privado no `Docx4jWriter`:

```java
private RPr buildRunProperties(StyleRule baseStyle, InlineFormatting formatting) {
    RPr rPr = createRunProperties(baseStyle);

    formatting.bold().ifPresent(bold -> {
        if (bold) {
            rPr.setB(objectFactory.createBooleanDefaultTrue());
        } else {
            rPr.setB(null);
        }
    });

    formatting.italic().ifPresent(italic -> {
        if (italic) {
            rPr.setI(objectFactory.createBooleanDefaultTrue());
        } else {
            rPr.setI(null);
        }
    });

    formatting.underline().ifPresent(underline -> {
        if (underline) {
            U u = objectFactory.createU();
            u.setVal(STUnderline.SINGLE);
            rPr.setU(u);
        }
    });

    return rPr;
}
```

Adicionar import para `U`, `STUnderline`, `InlineFormatting`.

- [ ] **Step 4: Adicionar `<w:basedOn>` em heading styles**

Localizar `createHeadingStyle` (linha ~429) e adicionar após `style.setType("paragraph")`:

```java
Style.BasedOn basedOn = objectFactory.createStyleBasedOn();
basedOn.setVal("Normal");
style.setBasedOn(basedOn);
```

- [ ] **Step 5: Compilar**

```bash
mvn compile -q
```

- [ ] **Step 6: Rodar testes existentes**

```bash
mvn test -pl . -Dtest=Docx4jWriterTest -q
```
Esperado: todos passam.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/abntbuilder/formatter/output/docx/docx4j/Docx4jWriter.java
git commit -m "feat: emit individual w:r per DocxRun and add basedOn to heading styles"
```

---

## Task 5: `BodyText` e `BodyQuoteText` com `InlineFormatting`

**Files:**
- Modify: `src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyText.java`
- Modify: `src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyQuoteText.java`

- [ ] **Step 1: Atualizar `BodyText`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyText.java
package com.abntbuilder.formatter.document.component.bodycontent;

import java.util.Objects;

public record BodyText(
        String text,
        InlineFormatting formatting
) implements BodyInline {

    public BodyText(String text) {
        this(text, InlineFormatting.none());
    }

    public BodyText {
        requireNonBlank(text, "text");
        Objects.requireNonNull(formatting, "formatting must not be null");
    }

    @Override
    public String renderedText() {
        return text;
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
```

- [ ] **Step 2: Atualizar `BodyQuoteText`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyQuoteText.java
package com.abntbuilder.formatter.document.component.bodycontent;

import java.util.Objects;

public record BodyQuoteText(
        BodyQuoteType type,
        String text,
        InlineFormatting formatting
) implements BodyInline {

    public BodyQuoteText(BodyQuoteType type, String text) {
        this(type, text, InlineFormatting.none());
    }

    public BodyQuoteText {
        Objects.requireNonNull(type, "type must not be null");
        requireNonBlank(text, "text");
        Objects.requireNonNull(formatting, "formatting must not be null");
    }

    @Override
    public String renderedText() {
        return switch (type) {
            case SHORT -> "\"" + text.trim() + "\"";
        };
    }

    private static void requireNoBoundaryQuotes(String value) {
        String trimmed = value.trim();
        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            throw new IllegalArgumentException(
                    "manual boundary quotation marks must not be provided for SHORT quote text."
            );
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
        requireNoBoundaryQuotes(value);
    }
}
```

- [ ] **Step 3: Compilar**

```bash
mvn compile -q
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyText.java \
        src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyQuoteText.java
git commit -m "feat: add InlineFormatting to BodyText and BodyQuoteText"
```

---

## Task 6: `CitationFormattingRule` e atualizar `CitationSource` / `BodyCitationCall`

**Files:**
- Create: `src/main/java/com/abntbuilder/formatter/profile/model/component/bodycontent/CitationFormattingRule.java`
- Modify: `src/main/java/com/abntbuilder/formatter/document/component/bodycontent/CitationSource.java`
- Modify: `src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyCitationCall.java`

- [ ] **Step 1: Escrever teste para `CitationFormattingRule`**

```java
// src/test/java/com/abntbuilder/formatter/profile/model/component/bodycontent/CitationFormattingRuleTest.java
package com.abntbuilder.formatter.profile.model.component.bodycontent;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

class CitationFormattingRuleTest {

    @Test
    void shouldCreateWithValidValues() {
        CitationFormattingRule rule = new CitationFormattingRule("p. ", "; ", "et al.", " apud ");
        assertThat(rule.pagePrefix()).isEqualTo("p. ");
        assertThat(rule.multiAuthorJoiner()).isEqualTo("; ");
        assertThat(rule.etAl()).isEqualTo("et al.");
        assertThat(rule.apudConnector()).isEqualTo(" apud ");
    }

    @Test
    void shouldRejectBlankPagePrefix() {
        assertThatThrownBy(() -> new CitationFormattingRule("", "; ", "et al.", " apud "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
```

- [ ] **Step 2: Rodar e confirmar falha**

```bash
mvn test -pl . -Dtest=CitationFormattingRuleTest -q
```

- [ ] **Step 3: Criar `CitationFormattingRule`**

```java
// src/main/java/com/abntbuilder/formatter/profile/model/component/bodycontent/CitationFormattingRule.java
package com.abntbuilder.formatter.profile.model.component.bodycontent;

public record CitationFormattingRule(
        String pagePrefix,
        String multiAuthorJoiner,
        String etAl,
        String apudConnector
) {

    public CitationFormattingRule {
        requireNonBlank(pagePrefix, "pagePrefix");
        requireNonBlank(multiAuthorJoiner, "multiAuthorJoiner");
        requireNonBlank(etAl, "etAl");
        requireNonBlank(apudConnector, "apudConnector");
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
```

- [ ] **Step 4: Atualizar `CitationSource` para receber `CitationFormattingRule`**

Substituir os métodos `authorText()`, `yearAndPageText()`, `parentheticalText()` e `narrativeReferenceText()` para receber `CitationFormattingRule` como parâmetro (não armazenar no record):

```java
// Métodos a substituir em CitationSource.java
public String authorText(CitationFormattingRule formatting) {
    if (!authors.isEmpty()) {
        return switch (authors.size()) {
            case 1 -> authors.getFirst().renderedName();
            case 2 -> authors.get(0).renderedName() + formatting.multiAuthorJoiner() + authors.get(1).renderedName();
            default -> authors.getFirst().renderedName() + " " + formatting.etAl();
        };
    }
    throw new IllegalStateException("authors must not be empty.");
}

public String yearAndPageText(CitationFormattingRule formatting) {
    return page
            .map(value -> year + ", " + formatting.pagePrefix() + value)
            .orElse(year);
}

public String parentheticalText(CitationFormattingRule formatting) {
    return authorText(formatting) + ", " + yearAndPageText(formatting);
}

public String narrativeReferenceText(CitationFormattingRule formatting) {
    return authorText(formatting) + " (" + yearAndPageText(formatting) + ")";
}
```

Remover os métodos sem parâmetro `CitationFormattingRule`.

- [ ] **Step 5: Atualizar `BodyCitationCall` para receber `CitationFormattingRule`**

Adicionar `CitationFormattingRule formatting` ao record e passar para `CitationSource`:

```java
public record BodyCitationCall(
        BodyCitationType citationType,
        BodyCitationMode mode,
        CitationFormattingRule formatting,
        Optional<CitationSource> source,
        Optional<CitationSource> originalSource,
        Optional<CitationSource> consultedSource
) implements BodyInline {

    // renderedText() usa formatting ao chamar CitationSource:
    private String renderRegularCitation() {
        CitationSource citationSource = source.orElseThrow();
        return switch (mode) {
            case PARENTHETICAL -> "(" + citationSource.parentheticalText(formatting) + ")";
            case NARRATIVE -> citationSource.narrativeReferenceText(formatting);
        };
    }

    private String renderCitationOfCitation() {
        CitationSource original = originalSource.orElseThrow();
        CitationSource consulted = consultedSource.orElseThrow();
        String apudReference = original.authorText(formatting)
                + ", "
                + original.year()
                + formatting.apudConnector()
                + consulted.parentheticalText(formatting);
        return switch (mode) {
            case PARENTHETICAL -> "(" + apudReference + ")";
            case NARRATIVE -> original.authorText(formatting)
                    + " ("
                    + original.year()
                    + formatting.apudConnector()
                    + consulted.parentheticalText(formatting)
                    + ")";
        };
    }
}
```

- [ ] **Step 6: Compilar**

```bash
mvn compile -q
```
Corrigir erros — `BodyInlineRequest` e `BodyContentRenderer` chamam `BodyCitationCall` e precisarão passar `CitationFormattingRule`. Por agora use um placeholder temporário `CitationFormattingRule("p. ", "; ", "et al.", " apud ")` nos callers que ainda não têm acesso ao perfil. Será corrigido na Task 9.

- [ ] **Step 7: Rodar testes**

```bash
mvn test -pl . -Dtest=CitationFormattingRuleTest -q
```

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/abntbuilder/formatter/profile/model/component/bodycontent/CitationFormattingRule.java \
        src/main/java/com/abntbuilder/formatter/document/component/bodycontent/CitationSource.java \
        src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyCitationCall.java \
        src/test/java/com/abntbuilder/formatter/profile/model/component/bodycontent/CitationFormattingRuleTest.java
git commit -m "feat: add CitationFormattingRule and remove hardcoded citation tokens"
```

---

## Task 7: `BodyLongQuote` e remoção de `BodyCitation`

**Files:**
- Create: `src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyLongQuote.java`
- Create: `src/test/java/com/abntbuilder/formatter/document/component/bodycontent/BodyLongQuoteTest.java`
- Modify: `src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyBlock.java`
- Delete: `src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyCitation.java`

- [ ] **Step 1: Escrever teste para `BodyLongQuote`**

```java
// src/test/java/com/abntbuilder/formatter/document/component/bodycontent/BodyLongQuoteTest.java
package com.abntbuilder.formatter.document.component.bodycontent;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BodyLongQuoteTest {

    private static final CitationFormattingRule FORMATTING =
            new CitationFormattingRule("p. ", "; ", "et al.", " apud ");

    private static final CitationSource SOURCE = new CitationSource(
            List.of(new CitationAuthor(CitationAuthorType.PERSON, "Sobrenome", null, null)),
            "2020",
            Optional.of("42")
    );

    @Test
    void shouldCreateWithValidData() {
        BodyLongQuote quote = new BodyLongQuote(
                "texto longo da citação",
                BodyCitationMode.PARENTHETICAL,
                Optional.of(SOURCE),
                Optional.empty(),
                Optional.empty()
        );
        assertThat(quote.text()).isEqualTo("texto longo da citação");
    }

    @Test
    void shouldRejectBlankText() {
        assertThatThrownBy(() -> new BodyLongQuote(
                "  ",
                BodyCitationMode.PARENTHETICAL,
                Optional.of(SOURCE),
                Optional.empty(),
                Optional.empty()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectMissingSource() {
        assertThatThrownBy(() -> new BodyLongQuote(
                "texto",
                BodyCitationMode.PARENTHETICAL,
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectSourceWithoutPage() {
        CitationSource noPage = new CitationSource(
                List.of(new CitationAuthor(CitationAuthorType.PERSON, "Sobrenome", null, null)),
                "2020",
                Optional.empty()
        );
        assertThatThrownBy(() -> new BodyLongQuote(
                "texto",
                BodyCitationMode.PARENTHETICAL,
                Optional.of(noPage),
                Optional.empty(),
                Optional.empty()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRenderParentheticalText() {
        BodyLongQuote quote = new BodyLongQuote(
                "texto da citação.",
                BodyCitationMode.PARENTHETICAL,
                Optional.of(SOURCE),
                Optional.empty(),
                Optional.empty()
        );
        String rendered = quote.renderedText(FORMATTING);
        assertThat(rendered).contains("Sobrenome").contains("2020").contains("p. 42");
    }
}
```

- [ ] **Step 2: Rodar e confirmar falha**

```bash
mvn test -pl . -Dtest=BodyLongQuoteTest -q
```

- [ ] **Step 3: Criar `BodyLongQuote`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyLongQuote.java
package com.abntbuilder.formatter.document.component.bodycontent;

import com.abntbuilder.formatter.profile.model.component.bodycontent.CitationFormattingRule;

import java.util.Objects;
import java.util.Optional;

public record BodyLongQuote(
        String text,
        BodyCitationMode mode,
        Optional<CitationSource> source,
        Optional<CitationSource> originalSource,
        Optional<CitationSource> consultedSource
) implements BodyBlock {

    public BodyLongQuote {
        requireNonBlank(text, "text");
        Objects.requireNonNull(mode, "mode must not be null");
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(originalSource, "originalSource must not be null");
        Objects.requireNonNull(consultedSource, "consultedSource must not be null");

        CitationSource citationSource = source.orElseThrow(() ->
                new IllegalArgumentException("DIRECT_LONG citation source must be provided.")
        );
        citationSource.requirePage("DIRECT_LONG");
    }

    public String renderedText(CitationFormattingRule formatting) {
        BodyCitationCall call = new BodyCitationCall(
                BodyCitationType.DIRECT_LONG, mode, formatting, source, originalSource, consultedSource
        );
        return switch (mode) {
            case PARENTHETICAL -> ensureNoFinalPeriod(text) + " " + call.renderedText() + ".";
            case NARRATIVE -> call.renderedText() + ": " + ensureFinalPeriod(text);
        };
    }

    private static String ensureNoFinalPeriod(String value) {
        String trimmed = value.trim();
        return trimmed.endsWith(".") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }

    private static String ensureFinalPeriod(String value) {
        String trimmed = value.trim();
        return trimmed.endsWith(".") ? trimmed : trimmed + ".";
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
```

- [ ] **Step 4: Atualizar `BodyBlock` para remover `BodyCitation` e adicionar `BodyLongQuote`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyBlock.java
package com.abntbuilder.formatter.document.component.bodycontent;

public sealed interface BodyBlock permits BodyParagraph, BodyLongQuote, NumberedDisplayObject {
}
```

- [ ] **Step 5: Deletar `BodyCitation.java`**

```bash
rm src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyCitation.java
```

- [ ] **Step 6: Compilar**

```bash
mvn compile -q
```
Corrigir erros de compilação em `BodyBlockRequest` (que referenciava `BodyCitation`) e `BodyContentRenderer`.

- [ ] **Step 7: Rodar testes**

```bash
mvn test -pl . -Dtest=BodyLongQuoteTest -q
```

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyLongQuote.java \
        src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyBlock.java \
        src/test/java/com/abntbuilder/formatter/document/component/bodycontent/BodyLongQuoteTest.java
git rm src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyCitation.java
git commit -m "feat: add BodyLongQuote block and remove BodyCitation"
```

---

## Task 8: Atualizar `BodyBlockType`, `BodyBlockRequest` e `BodyContentRequest`

**Files:**
- Modify: `src/main/java/com/abntbuilder/formatter/api/export/dto/request/BodyBlockType.java`
- Modify: `src/main/java/com/abntbuilder/formatter/api/export/dto/request/BodyBlockRequest.java`
- Modify: `src/main/java/com/abntbuilder/formatter/api/export/dto/request/BodyContentRequest.java`

- [ ] **Step 1: Atualizar `BodyBlockType`**

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/BodyBlockType.java
package com.abntbuilder.formatter.api.export.dto.request;

public enum BodyBlockType {
    PARAGRAPH,
    DIRECT_LONG_QUOTE,
    FIGURE,
    TABLE
}
```

- [ ] **Step 2: Atualizar `BodyBlockRequest`**

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/BodyBlockRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.bodycontent.BodyBlock;
import com.abntbuilder.formatter.document.component.bodycontent.BodyCitationMode;
import com.abntbuilder.formatter.document.component.bodycontent.BodyCitationType;
import com.abntbuilder.formatter.document.component.bodycontent.BodyLongQuote;
import com.abntbuilder.formatter.document.component.bodycontent.BodyParagraph;
import com.abntbuilder.formatter.shared.exception.InvalidBodyContentException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Optional;

public record BodyBlockRequest(
        @NotNull BodyBlockType type,
        BodyCitationMode mode,
        String text,
        @Valid List<BodyInlineRequest> content,
        @Valid CitationSourceRequest source,
        @Valid CitationSourceRequest originalSource,
        @Valid CitationSourceRequest consultedSource,
        @Valid BodyFigureRequest figure,
        @Valid BodyTableRequest table
) {

    public BodyBlock toDomain() {
        return switch (type) {
            case PARAGRAPH -> paragraph();
            case DIRECT_LONG_QUOTE -> longQuote();
            case FIGURE -> figureBlock();
            case TABLE -> tableBlock();
        };
    }

    private BodyParagraph paragraph() {
        if (content != null) {
            return new BodyParagraph(content.stream()
                    .map(BodyInlineRequest::toDomain)
                    .toList());
        }
        if (text != null) {
            return new BodyParagraph(text);
        }
        throw new InvalidBodyContentException("PARAGRAPH block requires either content or text.");
    }

    private BodyLongQuote longQuote() {
        if (text == null || text.isBlank()) {
            throw new InvalidBodyContentException("DIRECT_LONG_QUOTE block requires text.");
        }
        return new BodyLongQuote(
                text,
                mode == null ? BodyCitationMode.PARENTHETICAL : mode,
                source == null ? Optional.empty() : Optional.of(source.toDomain()),
                originalSource == null ? Optional.empty() : Optional.of(originalSource.toDomain()),
                consultedSource == null ? Optional.empty() : Optional.of(consultedSource.toDomain())
        );
    }

    private BodyBlock figureBlock() {
        if (figure == null) {
            throw new InvalidBodyContentException("figure must be provided for FIGURE block.");
        }
        return figure.toDomain();
    }

    private BodyBlock tableBlock() {
        if (table == null) {
            throw new InvalidBodyContentException("table must be provided for TABLE block.");
        }
        return table.toDomain();
    }
}
```

- [ ] **Step 3: Corrigir `BodyContentRequest` — remover null-coerce**

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/BodyContentRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.bodycontent.BodyContentComponent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BodyContentRequest(
        @Valid @NotEmpty List<BodySectionRequest> sections
) {

    public BodyContentComponent toDomain() {
        return new BodyContentComponent(
                sections.stream()
                        .map(BodySectionRequest::toDomain)
                        .toList()
        );
    }
}
```

- [ ] **Step 4: Compilar**

```bash
mvn compile -q
```

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/abntbuilder/formatter/api/export/dto/request/BodyBlockType.java \
        src/main/java/com/abntbuilder/formatter/api/export/dto/request/BodyBlockRequest.java \
        src/main/java/com/abntbuilder/formatter/api/export/dto/request/BodyContentRequest.java
git commit -m "feat: update BodyBlockType to block-only types and fix BodyBlockRequest"
```

---

## Task 9: `BodyContentComponentRule` + `CitationFormattingRule` no perfil e request

**Files:**
- Create: `src/main/java/com/abntbuilder/formatter/api/export/dto/request/CitationFormattingRuleRequest.java`
- Modify: `src/main/java/com/abntbuilder/formatter/profile/model/component/bodycontent/BodyContentComponentRule.java`
- Modify: `src/main/java/com/abntbuilder/formatter/api/export/dto/request/BodyContentComponentRuleRequest.java`
- Modify: `src/main/resources/profiles/abnt-unip-profile.json`

- [ ] **Step 1: Criar `CitationFormattingRuleRequest`**

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/CitationFormattingRuleRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.bodycontent.CitationFormattingRule;
import jakarta.validation.constraints.NotBlank;

public record CitationFormattingRuleRequest(
        @NotBlank String pagePrefix,
        @NotBlank String multiAuthorJoiner,
        @NotBlank String etAl,
        @NotBlank String apudConnector
) {

    public CitationFormattingRule toDomain() {
        return new CitationFormattingRule(pagePrefix, multiAuthorJoiner, etAl, apudConnector);
    }
}
```

- [ ] **Step 2: Atualizar `BodyContentComponentRule`**

Adicionar `CitationFormattingRule citationFormatting` ao record e documentar ausência de `contentBindings`:

```java
// src/main/java/com/abntbuilder/formatter/profile/model/component/bodycontent/BodyContentComponentRule.java
package com.abntbuilder.formatter.profile.model.component.bodycontent;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;

import java.util.Map;
import java.util.Objects;

public record BodyContentComponentRule(
        String componentId,
        BodyContentStyleMapping styleMapping,
        BodyContentNumberingRule numbering,
        BodyContentLayoutRule layout,
        FigureRule figure,
        TableRule table,
        CitationFormattingRule citationFormatting
) implements ComponentRule {

    public BodyContentComponentRule {
        requireNonBlank(componentId, "componentId");
        Objects.requireNonNull(styleMapping, "styleMapping must not be null");
        Objects.requireNonNull(numbering, "numbering must not be null");
        Objects.requireNonNull(layout, "layout must not be null");
        Objects.requireNonNull(figure, "figure must not be null");
        Objects.requireNonNull(table, "table must not be null");
        Objects.requireNonNull(citationFormatting, "citationFormatting must not be null");
    }

    // bodyContent does not consume work data — its content is fully owned by the component.
    public Map<String, String> contentBindings() {
        return Map.of();
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
```

- [ ] **Step 3: Atualizar `BodyContentComponentRuleRequest`**

Adicionar o campo e passar para `toDomain()`:

```java
@Valid
@NotNull
CitationFormattingRuleRequest citationFormatting
```

Em `toDomain()`:
```java
return new BodyContentComponentRule(
        componentId,
        styleMapping.toDomain(),
        numbering.toDomain(),
        layout.toDomain(),
        figure.toDomain(),
        table.toDomain(),
        citationFormatting.toDomain()
);
```

- [ ] **Step 4: Adicionar `citationFormatting` no `abnt-unip-profile.json`**

Localizar a seção `"bodyContent"` em `componentRules` e adicionar dentro dela:

```json
"citationFormatting": {
  "pagePrefix": "p. ",
  "multiAuthorJoiner": "; ",
  "etAl": "et al.",
  "apudConnector": " apud "
}
```

- [ ] **Step 5: Compilar**

```bash
mvn compile -q
```

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/abntbuilder/formatter/api/export/dto/request/CitationFormattingRuleRequest.java \
        src/main/java/com/abntbuilder/formatter/profile/model/component/bodycontent/BodyContentComponentRule.java \
        src/main/java/com/abntbuilder/formatter/api/export/dto/request/BodyContentComponentRuleRequest.java \
        src/main/resources/profiles/abnt-unip-profile.json
git commit -m "feat: add CitationFormattingRule to BodyContentComponentRule and profile"
```

---

## Task 10: Atualizar `BodyInlineRequest` com `InlineFormattingRequest`

**Files:**
- Create: `src/main/java/com/abntbuilder/formatter/api/export/dto/request/InlineFormattingRequest.java`
- Modify: `src/main/java/com/abntbuilder/formatter/api/export/dto/request/BodyInlineRequest.java`

- [ ] **Step 1: Criar `InlineFormattingRequest`**

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/InlineFormattingRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.bodycontent.InlineFormatting;

import java.util.Optional;

public record InlineFormattingRequest(
        Boolean bold,
        Boolean italic,
        Boolean underline
) {

    public InlineFormatting toDomain() {
        return new InlineFormatting(
                Optional.ofNullable(bold),
                Optional.ofNullable(italic),
                Optional.ofNullable(underline)
        );
    }
}
```

- [ ] **Step 2: Atualizar `BodyInlineRequest`**

Adicionar campo `InlineFormattingRequest formatting` e passar para `BodyText` e `BodyQuoteText`:

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/BodyInlineRequest.java
package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.document.component.bodycontent.BodyCitationCall;
import com.abntbuilder.formatter.document.component.bodycontent.BodyCitationMode;
import com.abntbuilder.formatter.document.component.bodycontent.BodyCitationType;
import com.abntbuilder.formatter.document.component.bodycontent.BodyInline;
import com.abntbuilder.formatter.document.component.bodycontent.BodyQuoteText;
import com.abntbuilder.formatter.document.component.bodycontent.BodyQuoteType;
import com.abntbuilder.formatter.document.component.bodycontent.BodyText;
import com.abntbuilder.formatter.document.component.bodycontent.InlineFormatting;
import com.abntbuilder.formatter.profile.model.component.bodycontent.CitationFormattingRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Optional;

public record BodyInlineRequest(
        @NotNull BodyInlineType type,
        String text,
        BodyQuoteType quoteType,
        BodyCitationType citationType,
        BodyCitationMode mode,
        @Valid CitationSourceRequest source,
        @Valid CitationSourceRequest originalSource,
        @Valid CitationSourceRequest consultedSource,
        InlineFormattingRequest formatting
) {

    // citationFormatting is passed from the caller (BodyContentRenderer has access to the rule)
    BodyInline toDomain(CitationFormattingRule citationFormatting) {
        InlineFormatting fmt = formatting != null ? formatting.toDomain() : InlineFormatting.none();
        return switch (type) {
            case TEXT -> new BodyText(text, fmt);
            case QUOTE_TEXT -> new BodyQuoteText(
                    quoteType == null ? BodyQuoteType.SHORT : quoteType,
                    text,
                    fmt
            );
            case CITATION -> new BodyCitationCall(
                    requireCitationType(),
                    mode == null ? BodyCitationMode.PARENTHETICAL : mode,
                    citationFormatting,
                    source == null ? Optional.empty() : Optional.of(source.toDomain()),
                    originalSource == null ? Optional.empty() : Optional.of(originalSource.toDomain()),
                    consultedSource == null ? Optional.empty() : Optional.of(consultedSource.toDomain())
            );
        };
    }

    private BodyCitationType requireCitationType() {
        if (citationType == null) {
            throw new IllegalArgumentException("citationType must be provided for CITATION inline content.");
        }
        return citationType;
    }
}
```

- [ ] **Step 3: Atualizar `BodySectionRequest` para passar `citationFormatting` para `BodyInlineRequest.toDomain`**

`BodySectionRequest` chama `BodyBlockRequest.toDomain()`, que chama `BodyInlineRequest.toDomain()`. O `citationFormatting` chega do renderer. Nesta task, `BodySectionRequest` e `BodyBlockRequest` precisam aceitar `CitationFormattingRule` como parâmetro de `toDomain`.

Atualizar `BodySectionRequest.toDomain(CitationFormattingRule citationFormatting)`, `BodyBlockRequest.toDomain(CitationFormattingRule citationFormatting)`, e em `paragraph()` passar para `BodyInlineRequest.toDomain(citationFormatting)`.

- [ ] **Step 4: Compilar**

```bash
mvn compile -q
```

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/abntbuilder/formatter/api/export/dto/request/InlineFormattingRequest.java \
        src/main/java/com/abntbuilder/formatter/api/export/dto/request/BodyInlineRequest.java \
        src/main/java/com/abntbuilder/formatter/api/export/dto/request/BodyBlockRequest.java \
        src/main/java/com/abntbuilder/formatter/api/export/dto/request/BodySectionRequest.java
git commit -m "feat: add InlineFormattingRequest and thread CitationFormattingRule through requests"
```

---

## Task 11: Atualizar `BodyContentRenderer` para multi-run e `BodyLongQuote`

**Files:**
- Modify: `src/main/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyContentRenderer.java`

- [ ] **Step 1: Substituir `paragraph.text()` por iteração de runs**

Em `renderContentBlock`, trocar o caso `BodyParagraph`:

```java
case BodyParagraph paragraph -> {
    StyleRule paragraphStyle = styleResolver.resolve(rule.styleMapping().paragraphStyleId());
    List<DocxRun> runs = paragraph.content().stream()
            .map(inline -> toDocxRun(inline, paragraphStyle, rule.citationFormatting(), styleResolver, rule.styleMapping()))
            .toList();
    yield List.of(new DocxParagraph(runs, paragraphStyle));
}
```

Adicionar método privado `toDocxRun`:

```java
private static DocxRun toDocxRun(
        BodyInline inline,
        StyleRule baseStyle,
        CitationFormattingRule citationFormatting,
        StyleResolver styleResolver,
        BodyContentStyleMapping styleMapping
) {
    return switch (inline) {
        case BodyText text -> new DocxRun(text.text(), baseStyle, text.formatting());
        case BodyQuoteText quote -> new DocxRun(quote.renderedText(), baseStyle, quote.formatting());
        case BodyCitationCall call -> {
            StyleRule citationStyle = styleResolver.resolve(
                    styleMapping.styleIdForCitationType(call.citationType())
            );
            yield new DocxRun(call.renderedText(), citationStyle, InlineFormatting.none());
        }
    };
}
```

> `styleMapping.styleIdForCitationType` — verificar nome exato do método em `BodyContentStyleMapping`. Atualmente é `styleIdForCitation(BodyCitationType)`.

- [ ] **Step 2: Adicionar caso `BodyLongQuote` em `renderContentBlock`**

```java
case BodyLongQuote longQuote -> {
    StyleRule longQuoteStyle = styleResolver.resolve(rule.styleMapping().directLongQuoteStyleId());
    String text = longQuote.renderedText(rule.citationFormatting());
    yield List.of(new DocxParagraph(
            List.of(DocxRun.of(text, longQuoteStyle)),
            longQuoteStyle
    ));
}
```

- [ ] **Step 3: Corrigir flag `previousRenderedTextWasBodyParagraph`**

Renomear para `previousBlockWasTextualContent`. Ativar apenas após `BodyParagraph` e `BodyLongQuote`:

```java
boolean previousBlockWasTextualContent = false;
// ...
case BodyParagraph paragraph -> { /* ... */ yield ...; }  // ativa flag
case BodyLongQuote longQuote -> { /* ... */ yield ...; }  // ativa flag
case BodyFigure figure -> { /* ... */ yield ...; }        // NÃO ativa flag
case BodyTable table -> { /* ... */ yield ...; }          // NÃO ativa flag
```

Mover a atribuição de `previousBlockWasTextualContent = true` para dentro dos casos `BodyParagraph` e `BodyLongQuote` apenas.

- [ ] **Step 4: Passar `citationFormatting` para requests (se `BodyContentRequest.toDomain` ainda existir no renderer)**

O renderer não deve chamar `toDomain` — ele recebe o domínio já resolvido. Verificar se o renderer ainda tem alguma chamada de `toDomain`. Se sim, remover.

- [ ] **Step 5: Compilar**

```bash
mvn compile -q
```

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyContentRenderer.java
git commit -m "feat: render paragraph as DocxRun list and fix previousBlockWasTextualContent flag"
```

---

## Task 12: `FigureRuleRequest` e `TableRuleRequest` com validações

**Files:**
- Modify: `src/main/java/com/abntbuilder/formatter/api/export/dto/request/FigureRuleRequest.java`
- Modify: `src/main/java/com/abntbuilder/formatter/api/export/dto/request/TableRuleRequest.java`

- [ ] **Step 1: Ler `TableRuleRequest` para identificar campos sem anotação**

```bash
cat src/main/java/com/abntbuilder/formatter/api/export/dto/request/TableRuleRequest.java
```

- [ ] **Step 2: Adicionar `@NotBlank` / `@NotNull` em `FigureRuleRequest`**

Todos os campos String obrigatórios recebem `@NotBlank`. Campos de tipo objeto obrigatórios recebem `@NotNull`. Campos numéricos obrigatórios recebem `@NotNull` (são `BigDecimal` / `Integer` boxed).

```java
public record FigureRuleRequest(
        @NotBlank String captionStyleId,
        @NotBlank String sourceStyleId,
        @NotBlank String captionTemplate,
        @NotBlank String sourceTemplate,
        @Valid @NotNull DisplayObjectContinuationLabelsRequest continuationLabels,
        @NotNull DisplayObjectSourcePlacement sourcePlacement,
        @NotNull TextAlignment imageAlignment,
        @NotNull BigDecimal maxWidthCm,
        @NotNull BigDecimal maxHeightCm,
        @NotNull BigDecimal defaultDpi,
        @NotNull Integer maxImageBytes,
        @NotNull Integer urlFetchTimeoutSeconds,
        @NotNull ImageFitPolicy fitPolicy
) { ... }
```

- [ ] **Step 3: Adicionar anotações equivalentes em `TableRuleRequest`** (mesma lógica).

- [ ] **Step 4: Compilar e rodar testes**

```bash
mvn compile -q && mvn test -q
```

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/abntbuilder/formatter/api/export/dto/request/FigureRuleRequest.java \
        src/main/java/com/abntbuilder/formatter/api/export/dto/request/TableRuleRequest.java
git commit -m "fix: add NotNull/NotBlank validation to FigureRuleRequest and TableRuleRequest"
```

---

## Task 13: Validar e corrigir tabelas no `Docx4jWriter`

**Files:**
- Modify: `src/main/java/com/abntbuilder/formatter/output/docx/docx4j/Docx4jWriter.java`

- [ ] **Step 1: Rodar testes e verificar compilação**

```bash
mvn test -q 2>&1 | grep -E "ERROR|FAIL|tblHeader|TblBorders|CTBorder|TblWidth|TrPr|TcPr" | head -20
```

- [ ] **Step 2: Se houver erro em `tblHeader`**

Localizar a chamada problemática:

```bash
grep -n "tblHeader\|createCTTrPrBase" src/main/java/com/abntbuilder/formatter/output/docx/docx4j/Docx4jWriter.java
```

Substituir pelo equivalente compatível com a versão do docx4j disponível. A semântica é declarar que a linha é header (se repete a cada página). Tentativa segura:

```java
// Em vez de: objectFactory.createCTTrPrBaseTblHeader(...)
BooleanDefaultTrue tblHeader = objectFactory.createBooleanDefaultTrue();
trPr.setTblHeader(tblHeader);
```

- [ ] **Step 3: Rodar testes de tabela**

```bash
mvn test -pl . -Dtest=BodyContentSampleValidationTest -q
```

- [ ] **Step 4: Commit (se houve mudanças)**

```bash
git add src/main/java/com/abntbuilder/formatter/output/docx/docx4j/Docx4jWriter.java
git commit -m "fix: adjust docx4j table API calls for tblHeader and borders"
```

---

## Task 14: Samples inline-formatting + atualizar `BodyContentSampleValidationTest`

**Files:**
- Create: `docs/samples/body-content/body-content-inline-formatting.json`
- Create: `docs/samples/body-content/body-content-inline-formatting-invalid.json`
- Modify: `src/test/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyContentSampleValidationTest.java`

- [ ] **Step 1: Criar sample válido `body-content-inline-formatting.json`**

```json
{
  "profileId": "abnt-unip-profile",
  "document": {
    "components": {
      "bodyContent": {
        "sections": [
          {
            "id": "s1",
            "level": 1,
            "title": "Formatação Inline",
            "blocks": [
              {
                "type": "PARAGRAPH",
                "content": [
                  { "type": "TEXT", "text": "Texto normal seguido de " },
                  { "type": "TEXT", "text": "negrito", "formatting": { "bold": true } },
                  { "type": "TEXT", "text": ", depois " },
                  { "type": "TEXT", "text": "itálico", "formatting": { "italic": true } },
                  { "type": "TEXT", "text": " e " },
                  { "type": "TEXT", "text": "sublinhado", "formatting": { "underline": true } },
                  { "type": "TEXT", "text": ". Em seguida uma citação inline: " },
                  {
                    "type": "CITATION",
                    "citationType": "INDIRECT",
                    "mode": "PARENTHETICAL",
                    "source": {
                      "authors": [{ "type": "PERSON", "surname": "Sobrenome Teste" }],
                      "year": "2024"
                    }
                  },
                  { "type": "TEXT", "text": "." }
                ]
              }
            ]
          }
        ]
      }
    }
  }
}
```

- [ ] **Step 2: Criar sample inválido `body-content-inline-formatting-invalid.json`**

Este sample envia `DIRECT_SHORT_QUOTE` como tipo de bloco — deve falhar com 400:

```json
{
  "profileId": "abnt-unip-profile",
  "document": {
    "components": {
      "bodyContent": {
        "sections": [
          {
            "id": "s1",
            "level": 1,
            "title": "Citação Inválida Como Bloco",
            "blocks": [
              {
                "type": "DIRECT_SHORT_QUOTE",
                "text": "texto que não pode ser bloco"
              }
            ]
          }
        ]
      }
    }
  }
}
```

> Atenção: como `DIRECT_SHORT_QUOTE` foi removido do enum `BodyBlockType`, esse JSON causará erro de desserialização (400 por campo inválido). Isso é o comportamento correto.

- [ ] **Step 3: Adicionar samples ao `BodyContentSampleValidationTest`**

Adicionar `"body-content-inline-formatting.json"` na lista de `sampleNames` do teste `shouldGenerateAllSuccessfulBodyContentSamplesFromOfficialJsonFiles`.

Adicionar `"body-content-inline-formatting-invalid.json"` na lista de `sampleNames` do teste `shouldFailInvalidBodyContentSamplesFromOfficialJsonFilesBeforeGeneratingDocx`.

Adicionar também `"body-content-figures-url-visual.json"` na lista de samples válidos (GAP-9) — apenas se o teste conseguir baixar a URL. Se rodar offline, usar `@Tag("visual")` ou mover para teste separado com `@Disabled("requires network")`.

- [ ] **Step 4: Rodar testes de sample**

```bash
mvn test -pl . -Dtest=BodyContentSampleValidationTest -q
```

- [ ] **Step 5: Commit**

```bash
git add docs/samples/body-content/body-content-inline-formatting.json \
        docs/samples/body-content/body-content-inline-formatting-invalid.json \
        src/test/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyContentSampleValidationTest.java
git commit -m "feat: add inline-formatting samples and update sample validation test"
```

---

## Task 15: `BodyContentRendererDocxSanityTest`

**Files:**
- Create: `src/test/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyContentRendererDocxSanityTest.java`

- [ ] **Step 1: Criar o teste de sanidade do DOCX**

```java
// src/test/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyContentRendererDocxSanityTest.java
package com.abntbuilder.formatter.rendering.component.bodycontent;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
class BodyContentRendererDocxSanityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void inlineFormattingSampleShouldContainMultipleRuns() throws Exception {
        String json = Files.readString(
                Path.of("docs/samples/body-content/body-content-inline-formatting.json")
        );

        byte[] docxBytes = mockMvc.perform(post("/api/v1/exports/docx")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        Document wordDoc = extractWordDocument(docxBytes);
        // body content paragraph should have multiple <w:r> runs
        NodeList runs = wordDoc.getElementsByTagNameNS(
                "http://schemas.openxmlformats.org/wordprocessingml/2006/main", "r"
        );
        assertThat(runs.getLength()).isGreaterThan(1);
    }

    @Test
    void headingStylesShouldHaveBasedOnNormal() throws Exception {
        String json = Files.readString(
                Path.of("docs/samples/body-content/body-content-short.json")
        );

        byte[] docxBytes = mockMvc.perform(post("/api/v1/exports/docx")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        Document stylesDoc = extractStylesDocument(docxBytes);
        NodeList basedOnNodes = stylesDoc.getElementsByTagNameNS(
                "http://schemas.openxmlformats.org/wordprocessingml/2006/main", "basedOn"
        );
        // at least one heading style should have basedOn
        assertThat(basedOnNodes.getLength()).isGreaterThan(0);
    }

    private static Document extractWordDocument(byte[] docxBytes) throws Exception {
        return extractXmlEntry(docxBytes, "word/document.xml");
    }

    private static Document extractStylesDocument(byte[] docxBytes) throws Exception {
        return extractXmlEntry(docxBytes, "word/styles.xml");
    }

    private static Document extractXmlEntry(byte[] docxBytes, String entryName) throws Exception {
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(docxBytes))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entryName.equals(entry.getName())) {
                    return DocumentBuilderFactory.newInstance()
                            .newDocumentBuilder()
                            .parse(zip);
                }
            }
        }
        throw new AssertionError("Entry not found in DOCX: " + entryName);
    }
}
```

- [ ] **Step 2: Rodar**

```bash
mvn test -pl . -Dtest=BodyContentRendererDocxSanityTest -q
```

- [ ] **Step 3: Commit**

```bash
git add src/test/java/com/abntbuilder/formatter/rendering/component/bodycontent/BodyContentRendererDocxSanityTest.java
git commit -m "test: add BodyContentRendererDocxSanityTest for runs and heading basedOn"
```

---

## Task 16: Suite completa + `BodyCitationCallTest` atualizado

**Files:**
- Modify: `src/test/java/com/abntbuilder/formatter/document/component/bodycontent/BodyCitationCallTest.java`
- Modify: `src/test/java/com/abntbuilder/formatter/document/component/bodycontent/BodyParagraphTest.java`

- [ ] **Step 1: Atualizar `BodyCitationCallTest` para usar `CitationFormattingRule`**

`BodyCitationCall` agora exige `CitationFormattingRule`. Todos os testes existentes precisam criar um `CitationFormattingRule("p. ", "; ", "et al.", " apud ")` e passá-lo na construção.

Verificar os testes existentes:

```bash
cat src/test/java/com/abntbuilder/formatter/document/component/bodycontent/BodyCitationCallTest.java
```

Adicionar constante no topo da classe de teste:

```java
private static final CitationFormattingRule FORMATTING =
        new CitationFormattingRule("p. ", "; ", "et al.", " apud ");
```

Atualizar cada `new BodyCitationCall(...)` para incluir `FORMATTING` no terceiro argumento.

- [ ] **Step 2: Atualizar `BodyParagraphTest`**

`BodyParagraph` continua igual, mas `BodyText` agora tem `formatting`. Verificar se há testes que quebram:

```bash
mvn test -pl . -Dtest=BodyParagraphTest -q
```

Se houver erros, corrigir construções de `BodyText` sem segundo argumento (o construtor de arity 1 ainda existe).

- [ ] **Step 3: Rodar suite completa**

```bash
mvn test -q
```
Esperado: todos passam.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/com/abntbuilder/formatter/document/component/bodycontent/BodyCitationCallTest.java \
        src/test/java/com/abntbuilder/formatter/document/component/bodycontent/BodyParagraphTest.java
git commit -m "test: update BodyCitationCallTest and BodyParagraphTest for new signatures"
```

---

## Task 17: Verificação visual final

- [ ] **Step 1: Gerar DOCX do sample de inline formatting**

```bash
curl -s -X POST http://localhost:8080/api/v1/exports/docx \
  -H "Content-Type: application/json" \
  -d @docs/samples/body-content/body-content-inline-formatting.json \
  -o /tmp/inline-formatting.docx
```

Ou usar o endpoint via teste de integração e salvar o arquivo.

- [ ] **Step 2: Abrir no Microsoft Word e verificar**

- Parágrafo com trechos em negrito, itálico e sublinhado visíveis
- Citação inline com estilo próprio (não concatenada ao parágrafo)
- Título com estilo heading real (não parágrafo normal com `outlineLvl`)
- Heading não herda cor azul do tema do Word

- [ ] **Step 3: Suite final**

```bash
mvn test -q
```
Esperado: todos passam sem erros.

- [ ] **Step 4: Commit final se necessário**

```bash
git add -A
git commit -m "fix: final adjustments from visual validation"
```

---

## Checklist de cobertura do spec

| Requisito do spec | Task |
|---|---|
| `InlineFormatting` record com bold/italic/underline | Task 2 |
| `DocxRun` e `DocxParagraph` com `List<DocxRun>` | Task 3 |
| `Docx4jWriter` emite `<w:r>` por run | Task 4 |
| `BodyText` e `BodyQuoteText` com `InlineFormatting` | Task 5 |
| `CitationFormattingRule` e remoção de tokens hardcoded | Task 6 |
| `BodyLongQuote` e remoção de `BodyCitation` | Task 7 |
| `BodyBlockType` com apenas tipos de bloco | Task 8 |
| `BodyContentRequest` sem null-coerce silencioso | Task 8 |
| `CitationFormattingRule` no perfil e no rule | Task 9 |
| `BodyInlineRequest` com `InlineFormattingRequest` | Task 10 |
| Renderer com multi-run e flag corrigida | Task 11 |
| `FigureRuleRequest` / `TableRuleRequest` com `@NotNull` | Task 12 |
| Tabelas no `Docx4jWriter` compilando | Task 13 |
| Samples inline-formatting + sample validation test | Task 14 |
| `BodyContentRendererDocxSanityTest` | Task 15 |
| `InvalidBodyContentException` | Task 1 |
| Heading styles com `<w:basedOn>` | Task 4 |
| `body-content-figures-url-visual.json` no teste | Task 14 |
| `BodyContentComponentRule.contentBindings()` documentado | Task 9 |
| `SectionNumberingState` comportamento documentado | Task 11 (comentário inline) |
