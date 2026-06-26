# bodyContent Fase 6 — Correções e Cenários Amplos

> **Pré-requisito:** Fases 1–5 concluídas.
>
> **For agentic workers:** Use superpowers:subagent-driven-development. Steps usam `- [ ]`.

**Goal:** Corrigir lacunas estruturais identificadas nas Fases 1–5 que impedem uso real do serviço: modelo de referências insuficiente para ABNT NBR 6023:2018, ausência de suporte a múltiplos abstracts, numeração incorreta de seções em apêndices/anexos, listas aninhadas no corpo textual, e células mescladas em tabelas.

**Tech Stack:** Java 21, Spring Boot, docx4j 11.5.13, JUnit 5

---

## Visão geral das correções

| Task | Problema | Impacto |
|---|---|---|
| Task 1 | `ReferenceEntry` sem campos para periódico, tese e capítulo de livro | Referências ABNT incompletas ou com hacks |
| Task 2 | Múltiplos abstracts em idiomas diferentes | Pós-graduação exige PT + EN; programas internacionais adicionam um terceiro |
| Task 3 | Numeração de seções em apêndice/anexo sem prefixo de letra ("A 1", "A 2") | Apêndices ficam com numeração igual ao corpo |
| Task 4 | `BodyListItem` sem suporte a sublistas | Listas aninhadas impossíveis no domínio |
| Task 5 | `BodyTableRow` sem colspan/rowspan | Tabelas com células mescladas causam erro de validação |

---

## Task 1 — `ReferenceEntry`: modelo de domínio e formatter corrigidos

### Problema

`ReferenceEntry` tem um único conjunto de campos para todos os tipos de referência. Para `BOOK_CHAPTER`, o `ReferencesEntryFormatter` reutiliza `url` para o autor do livro-fonte e `subtitle` para o título do livro. Isso impede que um capítulo de livro tenha URL própria ou subtítulo. `JOURNAL` não tem `volume`, `issue` nem `doi`. `THESIS` não tem `degree` nem `institutionName`. Tipos frequentes em trabalhos acadêmicos — `CONFERENCE_PAPER`, `REPORT`, `STANDARD` — não existem no enum `ReferenceType`.

### Solução

Adicionar campos específicos a `ReferenceEntry`, novos valores ao `ReferenceType`, e corrigir o `ReferencesEntryFormatter` para usar os campos corretos em vez dos hacks.

**Files:**
- Modify: `ReferenceType.java` — adicionar `CONFERENCE_PAPER`, `REPORT`, `STANDARD`
- Modify: `ReferenceEntry.java` — adicionar `volume`, `issue`, `doi`, `degree`, `institutionName`, `bookTitle`, `bookAuthors`
- Modify: `ReferencesEntryFormatter.java` — corrigir `formatBookChapter`, `formatJournal`, `formatThesis`; adicionar `formatConferencePaper`, `formatReport`, `formatStandard`
- Modify: `ReferenceEntryRequest.java` — adicionar campos opcionais novos
- Modify: samples de referências

- [ ] **Step 1: Atualizar `ReferenceType`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/references/ReferenceType.java
public enum ReferenceType {
    BOOK,
    BOOK_CHAPTER,
    JOURNAL,
    WEBSITE,
    LEGISLATION,
    THESIS,
    CONFERENCE_PAPER,   // anais de congresso — ABNT NBR 6023 seção 8.10
    REPORT,             // relatório técnico / nota técnica
    STANDARD            // normas técnicas (ABNT, ISO, etc.)
}
```

- [ ] **Step 2: Atualizar `ReferenceEntry`**

Adicionar campos opcionais. Os campos existentes não são removidos — `subtitle`, `url` e `publisher` continuam tendo seu uso original nos tipos que os usam legitimamente. Os novos campos específicos eliminam os hacks:

```java
// src/main/java/com/abntbuilder/formatter/document/component/references/ReferenceEntry.java
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
        Optional<String> accessDate,
        // Campos para JOURNAL
        Optional<String> volume,
        Optional<String> issue,
        Optional<String> doi,
        // Campos para THESIS
        Optional<String> degree,            // "Dissertação (Mestrado)" / "Tese (Doutorado)"
        Optional<String> institutionName,   // "Universidade Fictícia de Limeira"
        // Campos para BOOK_CHAPTER (substituem os hacks de url/subtitle)
        Optional<String> bookTitle,
        Optional<List<ReferenceAuthor>> bookAuthors
) {
    public ReferenceEntry {
        requireNonBlank(id, "id");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(authors, "authors must not be null");
        requireNonBlank(title, "title");
        // todos os Optional<String> existentes
        Objects.requireNonNull(subtitle, "subtitle must not be null");
        Objects.requireNonNull(edition, "edition must not be null");
        Objects.requireNonNull(city, "city must not be null");
        Objects.requireNonNull(publisher, "publisher must not be null");
        requireNonBlank(year, "year");
        Objects.requireNonNull(pages, "pages must not be null");
        Objects.requireNonNull(url, "url must not be null");
        Objects.requireNonNull(accessDate, "accessDate must not be null");
        // novos opcionais
        Objects.requireNonNull(volume, "volume must not be null");
        Objects.requireNonNull(issue, "issue must not be null");
        Objects.requireNonNull(doi, "doi must not be null");
        Objects.requireNonNull(degree, "degree must not be null");
        Objects.requireNonNull(institutionName, "institutionName must not be null");
        Objects.requireNonNull(bookTitle, "bookTitle must not be null");
        Objects.requireNonNull(bookAuthors, "bookAuthors must not be null");
        authors = List.copyOf(authors);
        bookAuthors = bookAuthors.map(List::copyOf);
    }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
```

> **Retrocompatibilidade:** Os campos `subtitle`, `url` e `publisher` continuam existindo com seus significados originais. O `formatBookChapter` passa a usar `bookTitle` e `bookAuthors` em vez de `subtitle` e `url`. Se `bookTitle` estiver ausente em uma entrada `BOOK_CHAPTER`, o formatter deve lançar exceção com mensagem clara.

- [ ] **Step 3: Corrigir `ReferencesEntryFormatter`**

**`formatBookChapter`** — usar os campos corretos:

```java
private List<ReferenceSegment> formatBookChapter(ReferenceEntry e) {
    // Validar campos obrigatórios para este tipo
    String bookTitleValue = e.bookTitle().orElseThrow(() ->
            new IllegalArgumentException("BOOK_CHAPTER reference '" + e.id() + "' must have bookTitle."));

    List<ReferenceSegment> segments = new ArrayList<>();
    segments.add(new ReferenceSegment(renderAuthors(e.authors()), false));
    segments.addAll(renderTitle(e.title(), Optional.empty()));
    segments.add(new ReferenceSegment(" " + rule.inLabel(), false));

    // Autores do livro (campo próprio, não hack de url)
    e.bookAuthors().ifPresent(bookAuthorList -> {
        if (!bookAuthorList.isEmpty()) {
            segments.add(new ReferenceSegment(renderAuthors(bookAuthorList) + " ", false));
        }
    });

    // Título do livro em negrito (campo próprio, não hack de subtitle)
    segments.add(new ReferenceSegment(bookTitleValue, true));
    segments.add(new ReferenceSegment(". ", false));

    e.edition().ifPresent(ed -> segments.add(new ReferenceSegment(ed + " ed. ", false)));
    e.city().ifPresent(c -> segments.add(new ReferenceSegment(c + ": ", false)));
    e.publisher().ifPresent(p -> segments.add(new ReferenceSegment(p + ", ", false)));
    segments.add(new ReferenceSegment(e.year() + ".", false));
    e.pages().ifPresent(p -> segments.add(new ReferenceSegment(" p. " + p + ".", false)));
    return List.copyOf(segments);
}
```

**`formatJournal`** — usar volume, issue e doi:

```java
private List<ReferenceSegment> formatJournal(ReferenceEntry e) {
    // ABNT NBR 6023: AUTOR. Título do artigo. Nome do Periódico, cidade, v. X, n. Y, p. ZZ-ZZ, ano. DOI ou URL.
    List<ReferenceSegment> segments = new ArrayList<>();
    segments.add(new ReferenceSegment(renderAuthors(e.authors()), false));
    segments.addAll(renderTitle(e.title(), e.subtitle()));
    e.publisher().ifPresent(journal ->
            segments.add(new ReferenceSegment(" " + journal + ",", false)));
    e.volume().ifPresent(v ->
            segments.add(new ReferenceSegment(" v. " + v + ",", false)));
    e.issue().ifPresent(n ->
            segments.add(new ReferenceSegment(" n. " + n + ",", false)));
    e.pages().ifPresent(p ->
            segments.add(new ReferenceSegment(" p. " + p + ",", false)));
    segments.add(new ReferenceSegment(" " + e.year() + ".", false));
    e.doi().ifPresent(d ->
            segments.add(new ReferenceSegment(" DOI: " + d + ".", false)));
    e.url().filter(u -> e.doi().isEmpty()).ifPresent(u ->
            segments.add(new ReferenceSegment(" " + rule.availableAtLabel() + u + ".", false)));
    return List.copyOf(segments);
}
```

**`formatThesis`** — usar degree e institutionName:

```java
private List<ReferenceSegment> formatThesis(ReferenceEntry e) {
    // ABNT NBR 6023: AUTOR. Título. Ano. Dissertação/Tese (Grau) — Instituição, Cidade, Ano.
    List<ReferenceSegment> segments = new ArrayList<>();
    segments.add(new ReferenceSegment(renderAuthors(e.authors()), false));
    segments.addAll(renderTitle(e.title(), e.subtitle()));
    segments.add(new ReferenceSegment(". " + e.year() + ". ", false));
    String typeInfo = e.degree().orElse("Dissertação/Tese");
    String institution = e.institutionName().map(i -> " — " + i).orElse("");
    String city = e.city().map(c -> ", " + c).orElse("");
    segments.add(new ReferenceSegment(typeInfo + institution + city + ", " + e.year() + ".", false));
    return List.copyOf(segments);
}
```

Adicionar `formatConferencePaper`, `formatReport`, `formatStandard` e o switch correspondente:

```java
case CONFERENCE_PAPER -> formatConferencePaper(entry);
case REPORT           -> formatReport(entry);
case STANDARD         -> formatStandard(entry);

private List<ReferenceSegment> formatConferencePaper(ReferenceEntry e) {
    // AUTOR. Título do trabalho. In: NOME DO EVENTO, número., ano, cidade. Anais [...]. Cidade: Editora, ano. p. XX-XX.
    // publisher = nome do evento; subtitle = nome dos anais (ex: "Anais...")
    List<ReferenceSegment> segments = new ArrayList<>();
    segments.add(new ReferenceSegment(renderAuthors(e.authors()), false));
    segments.addAll(renderTitle(e.title(), Optional.empty()));
    segments.add(new ReferenceSegment(" " + rule.inLabel(), false));
    e.publisher().ifPresent(event ->
            segments.add(new ReferenceSegment(event.toUpperCase() + ", ", false)));
    e.subtitle().ifPresent(anais -> {
        segments.add(new ReferenceSegment(anais, true));
        segments.add(new ReferenceSegment(". ", false));
    });
    e.city().ifPresent(c -> segments.add(new ReferenceSegment(c + ": ", false)));
    segments.add(new ReferenceSegment(e.year() + ".", false));
    e.pages().ifPresent(p -> segments.add(new ReferenceSegment(" p. " + p + ".", false)));
    return List.copyOf(segments);
}

private List<ReferenceSegment> formatReport(ReferenceEntry e) {
    // AUTOR/INSTITUIÇÃO. Título: subtítulo. Cidade: Editora/Instituição, ano. (Série/número se houver)
    List<ReferenceSegment> segments = new ArrayList<>();
    segments.add(new ReferenceSegment(renderAuthors(e.authors()), false));
    segments.addAll(renderTitle(e.title(), e.subtitle()));
    e.city().ifPresent(c -> segments.add(new ReferenceSegment(". " + c + ": ", false)));
    e.publisher().ifPresent(p -> segments.add(new ReferenceSegment(p + ", ", false)));
    segments.add(new ReferenceSegment(e.year() + ".", false));
    e.url().ifPresent(u ->
            segments.add(new ReferenceSegment(" " + rule.availableAtLabel() + u + ".", false)));
    return List.copyOf(segments);
}

private List<ReferenceSegment> formatStandard(ReferenceEntry e) {
    // ORGANIZAÇÃO. Título: subtítulo. Edição. Cidade, ano.
    // authors vazios são válidos aqui (norma sem autores pessoais)
    List<ReferenceSegment> segments = new ArrayList<>();
    if (!e.authors().isEmpty()) {
        segments.add(new ReferenceSegment(renderAuthors(e.authors()), false));
    }
    segments.addAll(renderTitle(e.title(), e.subtitle()));
    // P8 fix: edition termina com ". ed." (sem ponto final solto); city usa separador
    // condicional para evitar duplo ponto quando edition está presente (". ed.. Cidade").
    e.edition().ifPresent(ed -> segments.add(new ReferenceSegment(". " + ed + ". ed.", false)));
    String citySeparator = e.edition().isPresent() ? " " : ". ";
    e.city().ifPresent(c -> segments.add(new ReferenceSegment(citySeparator + c + ", ", false)));
    segments.add(new ReferenceSegment(e.year() + ".", false));
    return List.copyOf(segments);
}
```

- [ ] **Step 4: Atualizar `ReferenceEntryRequest`**

Adicionar todos os campos opcionais novos com as mesmas anotações de validação dos outros opcionais:

```java
String volume,
String issue,
String doi,
String degree,
String institutionName,
String bookTitle,
List<ReferenceAuthorRequest> bookAuthors
```

No `toDomain()`:

```java
return new ReferenceEntry(
        id, type.toDomain(),
        authors.stream().map(ReferenceAuthorRequest::toDomain).toList(),
        title,
        Optional.ofNullable(subtitle),
        Optional.ofNullable(edition),
        Optional.ofNullable(city),
        Optional.ofNullable(publisher),
        year,
        Optional.ofNullable(pages),
        Optional.ofNullable(url),
        Optional.ofNullable(accessDate),
        Optional.ofNullable(volume),
        Optional.ofNullable(issue),
        Optional.ofNullable(doi),
        Optional.ofNullable(degree),
        Optional.ofNullable(institutionName),
        Optional.ofNullable(bookTitle),
        Optional.ofNullable(bookAuthors == null ? null
                : bookAuthors.stream().map(ReferenceAuthorRequest::toDomain).toList())
);
```

- [ ] **Step 5: Escrever testes adicionais em `ReferencesEntryFormatterTest`**

```java
@Test void shouldFormatJournalWithVolumeIssueDoi() { ... }
@Test void shouldFormatThesisWithDegreeAndInstitution() { ... }
@Test void shouldFormatBookChapterWithOwnFields() { ... }
@Test void shouldThrowForBookChapterWithoutBookTitle() { ... }
@Test void shouldFormatConferencePaper() { ... }
@Test void shouldFormatStandardWithoutAuthors() { ... }
```

- [ ] **Step 6: Atualizar samples de referências**

```
docs/samples/references/references-book.json        — sem alteração
docs/samples/references/references-mixed.json       — adicionar exemplos com novos campos e tipos
docs/samples/references/references-journal.json     — novo: periódico com volume, issue, doi
docs/samples/references/references-thesis.json      — novo: dissertação com degree e institutionName
docs/samples/references/references-chapter.json     — novo: capítulo com bookTitle e bookAuthors
docs/samples/references/references-conference.json  — novo: anais de congresso
docs/samples/references/references-standard.json    — novo: norma técnica
```

- [ ] **Step 7: Compilar e rodar**

```bash
mvn compile -q && mvn test -q
```

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "fix: expand ReferenceEntry model and formatter for ABNT NBR 6023:2018"
```

---

## Task 2 — Múltiplos abstracts em idiomas diferentes

### Problema

O sistema suporta apenas um campo `abstractEn` em `DocumentContentRequest` e um único valor `ABSTRACT_EN` em `ComponentType`. Programas de pós-graduação exigem resumo em português (já implementado como `resumo`) e abstract em inglês. Programas internacionais adicionam um terceiro idioma (espanhol, francês). A arquitetura atual impossibilita representar mais de um abstract no documento.

### Solução

Substituir o componente `AbstractComponent` (instância única) por um componente `AbstractsComponent` que contém uma lista de entradas, cada uma com idioma e conteúdo. O perfil declara os rótulos por idioma via `headingText` — o componente não sabe o idioma, apenas renderiza a sequência.

**Files:**
- Create: `AbstractEntry.java` em pacote `abstracten`
- Modify: `AbstractComponent.java` — passa a conter `List<AbstractEntry>`
- Modify: `AbstractComponentRule.java` — passa a suportar heading por idioma via lista, ou heading genérico único
- Modify: `AbstractRenderer.java` — itera as entradas
- Modify: `AbstractRequest.java` — passa a conter lista de entradas
- Modify: `DocumentContentRequest.java` — sem alteração (o campo já se chama `abstractEn`)
- Modify: `ComponentType.java` — `ABSTRACT_EN` permanece (nome é workaround de keyword)

- [ ] **Step 1: Criar `AbstractEntry`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/abstracten/AbstractEntry.java
package com.abntbuilder.formatter.document.component.abstracten;

import java.util.List;
import java.util.Objects;

public record AbstractEntry(
        String headingText,   // "ABSTRACT", "RÉSUMÉ", "RESUMEN" — declarado no request
        String text,
        List<String> keywords,
        String keywordsLabel  // "Keywords:", "Mots-clés :", "Palabras clave:" — declarado no request
) {
    public AbstractEntry {
        requireNonBlank(headingText, "headingText");
        requireNonBlank(text, "text");
        Objects.requireNonNull(keywords, "keywords must not be null");
        if (keywords.isEmpty()) throw new IllegalArgumentException("keywords must not be empty.");
        requireNonBlank(keywordsLabel, "keywordsLabel");
        keywords = List.copyOf(keywords);
    }
    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
```

> **Decisão de design:** `headingText` e `keywordsLabel` ficam no `AbstractEntry` (conteúdo), não na rule do perfil. O motivo: cada entrada pode ser de um idioma diferente, com um heading diferente. A rule do perfil controlaria apenas estilos tipográficos — que são iguais para todas as entradas.

- [ ] **Step 2: Atualizar `AbstractComponent`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/abstracten/AbstractComponent.java
package com.abntbuilder.formatter.document.component.abstracten;

import com.abntbuilder.formatter.document.component.ComponentType;
import com.abntbuilder.formatter.document.component.DocumentComponent;
import java.util.List;
import java.util.Objects;

public record AbstractComponent(List<AbstractEntry> entries) implements DocumentComponent {
    public AbstractComponent {
        Objects.requireNonNull(entries, "entries must not be null");
        if (entries.isEmpty()) throw new IllegalArgumentException("entries must not be empty.");
        entries = List.copyOf(entries);
    }
    @Override public ComponentType type() { return ComponentType.ABSTRACT_EN; }
}
```

- [ ] **Step 3: Atualizar `AbstractComponentRule`**

O perfil precisa apenas de estilos tipográficos — o heading de cada entrada vem do próprio `AbstractEntry`:

```java
// src/main/java/com/abntbuilder/formatter/profile/model/component/abstracten/AbstractComponentRule.java
public record AbstractComponentRule(
        String componentId,
        String headingStyleId,    // estilo do heading (igual para todos os idiomas)
        String textStyleId,
        String keywordsStyleId,
        String keywordsSeparator, // "; "
        String keywordsTerminator // "." — ponto final obrigatório pela ABNT (P5)
) implements ComponentRule {
    public AbstractComponentRule {
        requireNonBlank(componentId, "componentId");
        requireNonBlank(headingStyleId, "headingStyleId");
        requireNonBlank(textStyleId, "textStyleId");
        requireNonBlank(keywordsStyleId, "keywordsStyleId");
        requireNonBlank(keywordsSeparator, "keywordsSeparator");
        requireNonBlank(keywordsTerminator, "keywordsTerminator");
    }
    @Override public Map<String, String> contentBindings() { return Map.of(); }
    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
```

> Remover os campos `headingText` e `keywordsLabel` da rule — eles ficaram obsoletos com a mudança. O campo `keywordsTerminator` (P5) substitui a ausência de ponto final nas keywords.

- [ ] **Step 4: Atualizar `AbstractRenderer`**

```java
// src/main/java/com/abntbuilder/formatter/rendering/component/abstracten/AbstractRenderer.java
@Override
public List<DocxBlock> render(AbstractComponent component, DocumentProfile profile) {
    AbstractComponentRule rule = new ComponentRuleResolver(profile)
            .resolve(COMPONENT_ID, AbstractComponentRule.class);
    StyleResolver styleResolver = new StyleResolver(profile);
    StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());
    StyleRule textStyle = styleResolver.resolve(rule.textStyleId());
    StyleRule keywordsStyle = styleResolver.resolve(rule.keywordsStyleId());

    List<DocxBlock> blocks = new ArrayList<>();
    boolean first = true;
    for (AbstractEntry entry : component.entries()) {
        if (!first) {
            blocks.add(new DocxPageBreak());  // cada abstract em página própria
        }
        // P3 fix: linha em branco após o heading, antes do texto
        blocks.add(new DocxParagraph(
                List.of(DocxRun.of(entry.headingText(), headingStyle)), headingStyle));
        blocks.add(new DocxBlankLine(headingStyle));
        blocks.add(new DocxParagraph(
                List.of(DocxRun.of(entry.text(), textStyle)), textStyle));
        // P4 fix: label em negrito (run separado) + P5 fix: terminador ao final
        String keywordsBody = String.join(rule.keywordsSeparator(), entry.keywords())
                + rule.keywordsTerminator();
        InlineFormatting boldLabel = new InlineFormatting(
                Optional.of(true), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty());
        blocks.add(new DocxParagraph(
                List.of(
                        new DocxRun(entry.keywordsLabel() + " ", keywordsStyle, boldLabel),
                        DocxRun.of(keywordsBody, keywordsStyle)
                ),
                keywordsStyle));
        first = false;
    }
    return List.copyOf(blocks);
}
```

- [ ] **Step 5: Atualizar `AbstractRequest`**

```java
// src/main/java/com/abntbuilder/formatter/api/export/dto/request/AbstractRequest.java
public record AbstractEntryRequest(
        @NotBlank String headingText,
        @NotBlank String text,
        @NotEmpty List<@NotBlank String> keywords,
        @NotBlank String keywordsLabel
) {
    public AbstractEntry toDomain() {
        return new AbstractEntry(headingText, text, keywords, keywordsLabel);
    }
}

public record AbstractRequest(
        @NotEmpty @Valid List<AbstractEntryRequest> entries
) {
    public AbstractComponent toDomain() {
        return new AbstractComponent(entries.stream().map(AbstractEntryRequest::toDomain).toList());
    }
}
```

- [ ] **Step 6: Atualizar `AbstractComponentRuleRequest`**

Remover `headingText` e `keywordsLabel`, adicionar `keywordsSeparator` e `keywordsTerminator` (P5):

```java
public record AbstractComponentRuleRequest(
        @NotBlank String componentId,
        @NotBlank String headingStyleId,
        @NotBlank String textStyleId,
        @NotBlank String keywordsStyleId,
        @NotBlank String keywordsSeparator,
        @NotBlank String keywordsTerminator
) {
    public AbstractComponentRule toDomain() {
        return new AbstractComponentRule(componentId, headingStyleId,
                textStyleId, keywordsStyleId, keywordsSeparator, keywordsTerminator);
    }
}
```

- [ ] **Step 7: Atualizar `abnt-unip-profile.json`**

```json
"abstract": {
  "componentId": "abstract",
  "headingStyleId": "abstract.heading",
  "textStyleId": "abstract.text",
  "keywordsStyleId": "abstract.keywords",
  "keywordsSeparator": "; ",
  "keywordsTerminator": "."
}
```

- [ ] **Step 8: Atualizar sample `resumo/resumo-simple.json`**

```json
"abstract": {
  "entries": [
    {
      "headingText": "ABSTRACT",
      "text": "This work investigates...",
      "keywords": ["distributed systems", "parallel processing"],
      "keywordsLabel": "Keywords:"
    },
    {
      "headingText": "RESUMEN",
      "text": "Este trabajo investiga...",
      "keywords": ["sistemas distribuidos", "procesamiento paralelo"],
      "keywordsLabel": "Palabras clave:"
    }
  ]
}
```

- [ ] **Step 9: Compilar e rodar**

```bash
mvn compile -q && mvn test -q
```

- [ ] **Step 10: Commit**

```bash
git add -A
git commit -m "fix: AbstractComponent now supports multiple entries for multilingual abstracts"
```

---

## Task 3 — Numeração de seções em apêndice/anexo com prefixo de letra

### Problema

`AppendixRenderer` e `AnnexRenderer` instanciam `BodyContentRenderer` diretamente. O `BodyContentRenderer` usa `SectionNumberingState`, que gera "1", "1.1", "2" etc. — sem nenhum prefixo. O perfil (`BodyContentNumberingRule`) não tem campo `sectionPrefix`. O apêndice A gera seções numeradas "1", "2" — igual ao corpo. ABNT NBR 14724 exige "A 1", "A 2", "B 1", "B 2".

### Solução

Adicionar `sectionPrefix` opcional a `BodyContentNumberingRule`. `AppendixRenderer` e `AnnexRenderer` precisam criar uma rule derivada com o prefixo preenchido antes de instanciar o `BodyContentRenderer`.

**Files:**
- Modify: `BodyContentNumberingRule.java` — adicionar `Optional<String> sectionPrefix`
- Modify: `SectionNumberingState` (classe interna de `BodyContentRenderer`) — usar o prefixo na formatação
- Modify: `AppendixRenderer.java` — injetar prefixo "A", "B", "C"... na rule antes de renderizar
- Modify: `AnnexRenderer.java` — idem
- Modify: `abnt-unip-profile.json` — sem alteração necessária (prefixo vem do renderer, não do perfil)

- [ ] **Step 1: Atualizar `BodyContentNumberingRule`**

```java
// src/main/java/com/abntbuilder/formatter/profile/model/component/bodycontent/BodyContentNumberingRule.java
public record BodyContentNumberingRule(
        boolean enabled,
        String separator,
        String primarySuffix,
        Optional<String> sectionPrefix  // ex: "A " para apêndice A — ausente no corpo normal
) {
    public BodyContentNumberingRule {
        if (enabled) {
            requireNonBlank(separator, "separator");
            requireNonNull(primarySuffix, "primarySuffix");
        }
        Objects.requireNonNull(sectionPrefix, "sectionPrefix must not be null");
    }

    // Factory para criar uma cópia com prefixo — usado pelo AppendixRenderer/AnnexRenderer
    public BodyContentNumberingRule withPrefix(String prefix) {
        return new BodyContentNumberingRule(enabled, separator, primarySuffix, Optional.of(prefix));
    }

    private static void requireNonBlank(String v, String f) { ... }
    private static void requireNonNull(String v, String f) { ... }
}
```

> **⚠️ Retrocompatibilidade:** o campo `sectionPrefix` é novo. Adicionar `Optional.empty()` como valor padrão na desserialização JSON. No `BodyContentNumberingRuleRequest`, adicionar campo `String sectionPrefix` (nullable) e mapear para `Optional.ofNullable(sectionPrefix)`.

- [ ] **Step 2: Atualizar `SectionNumberingState` em `BodyContentRenderer`**

Em `sectionNumber(int level)`, prefixar com o valor de `sectionPrefix` quando presente:

```java
private String sectionNumber(int level) {
    String prefix = numberingRule.sectionPrefix().orElse("");
    if (level == 1) {
        return prefix + counters[0] + numberingRule.primarySuffix();
    }
    List<String> parts = new ArrayList<>();
    for (int index = 0; index < level; index++) {
        parts.add(String.valueOf(counters[index]));
    }
    return prefix + String.join(numberingRule.separator(), parts);
}
```

- [ ] **Step 3: Atualizar `AppendixRenderer`**

```java
@Override
public List<DocxBlock> render(AppendixComponent component, DocumentProfile profile) {
    AppendixComponentRule rule = new ComponentRuleResolver(profile)
            .resolve(COMPONENT_ID, AppendixComponentRule.class);
    StyleResolver styleResolver = new StyleResolver(profile);
    StyleRule headingStyle = styleResolver.resolve(rule.headingStyleId());

    // Resolver a bodyContentRule base para derivar versões com prefixo
    BodyContentComponentRule bodyRule = new ComponentRuleResolver(profile)
            .resolve(BodyContentRenderer.COMPONENT_ID, BodyContentComponentRule.class);

    List<DocxBlock> blocks = new ArrayList<>();
    char letter = 'A';
    for (AppendixItem item : component.items()) {
        String heading = rule.headingTemplate()
                .replace("{letter}", String.valueOf(letter))
                .replace("{title}", item.title());
        blocks.add(new DocxParagraph(List.of(DocxRun.of(heading, headingStyle)), headingStyle));

        if (!item.sections().isEmpty()) {
            // Criar rule derivada com prefixo "A " para este apêndice
            String prefix = letter + " ";
            BodyContentNumberingRule numbering = bodyRule.numbering().withPrefix(prefix);
            BodyContentComponentRule derivedRule = bodyRule.withNumbering(numbering);
            BodyContentComponent appendixContent = new BodyContentComponent(item.sections());
            blocks.addAll(new BodyContentRenderer().renderWith(appendixContent, profile, derivedRule));
        }
        letter++;
    }
    return List.copyOf(blocks);
}
```

> **Nota:** `BodyContentComponentRule.withNumbering(BodyContentNumberingRule)` e `BodyContentRenderer.renderWith(component, profile, rule)` são dois métodos auxiliares que precisam ser criados:
>
> - `BodyContentComponentRule.withNumbering(BodyContentNumberingRule numbering)` — factory que retorna uma cópia da rule com o campo `numbering` substituído.
> - `BodyContentRenderer.renderWith(BodyContentComponent, DocumentProfile, BodyContentComponentRule)` — overload de `render` que aceita uma rule já resolvida externamente, em vez de resolvê-la do perfil. Isso evita que o renderer busque a rule errada do perfil.

- [ ] **Step 4: Adicionar `BodyContentComponentRule.withNumbering` e `BodyContentRenderer.renderWith`**

```java
// Em BodyContentComponentRule:
public BodyContentComponentRule withNumbering(BodyContentNumberingRule newNumbering) {
    return new BodyContentComponentRule(
            componentId, headingStyleId, paragraphStyleId, longQuoteStyleId,
            layout, newNumbering, styleMapping, figureRule, tableRule, frameRule,
            chartRule, codeListingRule, citationFormatting, crossReferenceLabels
    );
}

// Em BodyContentRenderer — novo método package-private:
List<DocxBlock> renderWith(BodyContentComponent component, DocumentProfile profile,
                           BodyContentComponentRule rule) {
    // mesma lógica do render() atual, mas usando a rule passada em vez de resolvê-la do perfil
    // Extrair o corpo do render() atual para um método privado que aceite a rule como parâmetro,
    // e chamar esse método tanto do render() normal quanto deste renderWith()
}
```

- [ ] **Step 5: Atualizar `AnnexRenderer` com o mesmo padrão do `AppendixRenderer`**

- [ ] **Step 6: Escrever `AppendixSectionNumberingTest`**

```java
@Test
void shouldNumberAppendixSectionsWithLetterPrefix() {
    // montar AppendixComponent com item "A", sections com level 1 e 2
    // renderizar com perfil com numbering habilitado
    // verificar que os títulos de seção começam com "A 1", "A 1.1", "A 2"
}
```

- [ ] **Step 7: Compilar e rodar**

```bash
mvn compile -q && mvn test -q
```

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "fix: appendix/annex section numbering now uses letter prefix (A 1, A 2, B 1)"
```

---

## Task 4 — Listas aninhadas no corpo textual

### Problema

`BodyListItem` aceita apenas `List<BodyInline>` — conteúdo inline puro. Não é possível representar um item de lista que contenha uma sublista. A estrutura atual é plana e não permite aninhamento.

### Solução

Alterar `BodyListItem` para aceitar conteúdo misto: texto inline e sublista opcional. O sealed interface `BodyBlock` já contém `BodyList` — `BodyListItem` precisa aceitar um `Optional<BodyList>` como conteúdo filho.

**Files:**
- Modify: `BodyListItem.java` — adicionar `Optional<BodyList> subList`
- Modify: `BodyListItemRequest.java` — adicionar campo `subList` opcional
- Modify: `BodyContentRenderer` — renderizar a sublista recursivamente após o item pai

- [ ] **Step 1: Atualizar `BodyListItem`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyListItem.java
public record BodyListItem(
        List<BodyInline> content,
        Optional<BodyList> subList
) {
    public BodyListItem {
        Objects.requireNonNull(content, "content must not be null");
        if (content.isEmpty()) throw new IllegalArgumentException("content must not be empty.");
        content = List.copyOf(content);
        Objects.requireNonNull(subList, "subList must not be null");
    }

    // Construtor de conveniência para itens sem sublista (retrocompatibilidade com código existente)
    public BodyListItem(List<BodyInline> content) {
        this(content, Optional.empty());
    }
}
```

- [ ] **Step 2: Atualizar renderização de lista em `BodyContentRenderer`**

No método que itera `BodyList`, após emitir o `DocxListItemParagraph` de cada item, verificar se há sublista e renderizá-la recursivamente com nível incrementado:

```java
private List<DocxBlock> renderList(BodyList list, BodyContentComponentRule rule,
                                   StyleResolver styleResolver, int nestingLevel, ...) {
    List<DocxBlock> blocks = new ArrayList<>();
    StyleRule itemStyle = styleResolver.resolve(
            rule.styleMapping().listItemStyleIdForType(list.type()));

    for (BodyListItem item : list.items()) {
        List<DocxRun> runs = item.content().stream()
                .flatMap(inline -> toDocxRun(inline, itemStyle, ...).stream())
                .toList();
        blocks.add(new DocxListItemParagraph(runs, itemStyle, list.type(), nestingLevel));

        // Sublista aninhada — nível incrementado
        item.subList().ifPresent(sub ->
                blocks.addAll(renderList(sub, rule, styleResolver, nestingLevel + 1, ...)));
    }
    return blocks;
}
```

> **Nota:** `DocxListItemParagraph` provavelmente já tem um campo de nível. Verificar a assinatura atual e adicionar o parâmetro `nestingLevel` se necessário. O `Docx4jWriter` usa esse nível para calcular o `numId` e o `ilvl` do parágrafo de lista no OOXML.

- [ ] **Step 3: Atualizar `BodyListItemRequest`**

```java
public record BodyListItemRequest(
        @NotEmpty List<BodyInlineRequest> content,
        @Valid BodyListRequest subList  // nullable
) {
    public BodyListItem toDomain(CitationFormattingRule citationFormatting) {
        return new BodyListItem(
                content.stream().map(i -> i.toDomain(citationFormatting)).toList(),
                Optional.ofNullable(subList == null ? null : subList.toDomain(citationFormatting))
        );
    }
}
```

- [ ] **Step 4: Escrever `NestedListRenderingTest`**

```java
@Test
void shouldRenderNestedListWithTwoLevels() {
    // montar BodyList UNORDERED com um item que tem subList ORDERED com dois subitens
    // verificar que o DOCX contém 3 parágrafos de lista com ilvl 0, 1, 1
}
```

- [ ] **Step 5: Compilar e rodar**

```bash
mvn compile -q && mvn test -q
```

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "fix: BodyListItem now supports nested sublists"
```

---

## Task 5 — Tabelas com colspan e rowspan

### Problema

`BodyTableRow` contém `List<String>` — células como strings simples sem metadado de mesclagem. A validação `row.cells().size() == columnCount` rejeita qualquer linha com célula mesclada. `BodyTableColumn` não tem informação de span. O `Docx4jWriter` escreve `<w:tc>` sem `<w:gridSpan>` nem `<w:vMerge>`.

### Solução

Substituir `List<String>` em `BodyTableRow` por `List<BodyTableCell>`, onde `BodyTableCell` carrega o texto e os spans opcionais. Atualizar a validação de `BodyTable` para contar células considerando os colspans. Atualizar o `Docx4jWriter` para emitir `<w:gridSpan>` e `<w:vMerge>`.

**Files:**
- Create: `BodyTableCell.java`
- Modify: `BodyTableRow.java` — `List<String>` → `List<BodyTableCell>`
- Modify: `BodyTable.java` — validação de contagem de colunas considerando colspan
- Modify: `BodyTableCellRequest.java` (novo) e `BodyTableRowRequest.java`
- Modify: `Docx4jWriter.java` — emitir gridSpan e vMerge
- Modify: `BodyContentRenderer` — passar `BodyTableCell` ao writer

- [ ] **Step 1: Criar `BodyTableCell`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyTableCell.java
public record BodyTableCell(
        String text,
        int colspan,    // 1 = normal, >1 = mescla horizontal
        boolean rowspanStart,  // true = início de mesclagem vertical
        boolean rowspanContinuation  // true = célula mesclada verticalmente (não exibe conteúdo)
) {
    public BodyTableCell {
        if (text == null) text = "";  // células mescladas verticalmente podem ter texto vazio
        if (colspan < 1) throw new IllegalArgumentException("colspan must be >= 1.");
        if (rowspanStart && rowspanContinuation)
            throw new IllegalArgumentException("cell cannot be both rowspanStart and rowspanContinuation.");
    }

    // Construtor de conveniência para célula normal
    public BodyTableCell(String text) {
        this(text, 1, false, false);
    }
}
```

- [ ] **Step 2: Atualizar `BodyTableRow`**

```java
// src/main/java/com/abntbuilder/formatter/document/component/bodycontent/BodyTableRow.java
public record BodyTableRow(List<BodyTableCell> cells) {
    public BodyTableRow {
        Objects.requireNonNull(cells, "cells must not be null");
        if (cells.isEmpty()) throw new IllegalArgumentException("cells must not be empty.");
        cells = List.copyOf(cells);
    }

    // Contar colunas efetivas somando colspans
    public int effectiveColumnCount() {
        return cells.stream().mapToInt(BodyTableCell::colspan).sum();
    }
}
```

- [ ] **Step 3: Atualizar validação em `BodyTable`**

```java
// Em BodyTable compact constructor — substituir a validação de tamanho:
int columnCount = columns.size();
for (BodyTableRow row : rows) {
    int effectiveCount = row.effectiveColumnCount();
    if (effectiveCount != columnCount) {
        throw new IllegalArgumentException(
                "table row effective column count (" + effectiveCount +
                ") must match declared column count (" + columnCount + ").");
    }
}
```

- [ ] **Step 4: Atualizar `Docx4jWriter` para emitir gridSpan e vMerge**

No método que escreve células de tabela (`<w:tc>`), adicionar:

```java
// Para cada BodyTableCell cell:
TcPr tcPr = objectFactory.createTcPr();

if (cell.colspan() > 1) {
    TcPrInner.GridSpan gridSpan = objectFactory.createTcPrInnerGridSpan();
    gridSpan.setVal(BigInteger.valueOf(cell.colspan()));
    tcPr.setGridSpan(gridSpan);
}

if (cell.rowspanStart()) {
    TcPrInner.VMerge vMerge = objectFactory.createTcPrInnerVMerge();
    vMerge.setVal(STMerge.RESTART);
    tcPr.setVMerge(vMerge);
}

if (cell.rowspanContinuation()) {
    TcPrInner.VMerge vMerge = objectFactory.createTcPrInnerVMerge();
    // Sem setVal — continuação de merge vertical usa elemento vazio
    tcPr.setVMerge(vMerge);
}

tc.setTcPr(tcPr);
```

- [ ] **Step 5: Criar `BodyTableCellRequest` e atualizar `BodyTableRowRequest`**

```java
// BodyTableCellRequest
public record BodyTableCellRequest(
        String text,
        int colspan,
        boolean rowspanStart,
        boolean rowspanContinuation
) {
    public BodyTableCell toDomain() {
        return new BodyTableCell(
                text == null ? "" : text,
                colspan <= 0 ? 1 : colspan,
                rowspanStart,
                rowspanContinuation
        );
    }
}

// BodyTableRowRequest — trocar List<String> por List<BodyTableCellRequest>
public record BodyTableRowRequest(@NotEmpty @Valid List<BodyTableCellRequest> cells) {
    public BodyTableRow toDomain() {
        return new BodyTableRow(cells.stream().map(BodyTableCellRequest::toDomain).toList());
    }
}
```

> **Retrocompatibilidade de JSON:** Se o campo `cells` do request atual for `List<String>`, será necessário manter um deserializador customizado ou migrar os samples existentes. A abordagem mais simples é migrar os samples — são poucos e todos são de teste.

- [ ] **Step 6: Migrar samples de tabelas existentes**

Atualizar as células em todos os samples que usam `BodyTable` para o novo formato `{ "text": "...", "colspan": 1, "rowspanStart": false, "rowspanContinuation": false }`. Adicionar um sample com célula mesclada:

```
docs/samples/body-content/body-content-table-merged.json
```

- [ ] **Step 7: Escrever `MergedTableRenderingTest`**

```java
@Test
void shouldRenderTableWithColspan() {
    // montar BodyTable com linha de cabeçalho onde primeira célula tem colspan=2
    // verificar que o DOCX emite <w:gridSpan w:val="2"/>
}

@Test
void shouldRenderTableWithRowspan() {
    // montar BodyTable com duas linhas onde célula da primeira tem rowspanStart=true
    // e célula correspondente na segunda tem rowspanContinuation=true
    // verificar que o DOCX emite <w:vMerge w:val="restart"/> e <w:vMerge/>
}
```

- [ ] **Step 8: Compilar e rodar**

```bash
mvn compile -q && mvn test -q
```

- [ ] **Step 9: Commit**

```bash
git add -A
git commit -m "fix: BodyTableCell supports colspan and rowspan (gridSpan, vMerge in OOXML)"
```

---

## Task 6 — Suite final e validação visual

- [ ] **Step 1: Rodar suite completa**

```bash
mvn test -q
```

Esperado: todos os testes passam.

- [ ] **Step 2: Validação visual no Word para cada correção**

- Referências: periódico com volume/issue/doi, dissertação com grau e instituição, capítulo com campos próprios, anais de congresso, norma técnica
- Abstract: dois idiomas (EN + ES) renderizados em páginas separadas dentro do mesmo componente
- Apêndice: seções numeradas "A 1", "A 1.1", "B 1" — não "1", "1.1", "1"
- Lista aninhada: item de lista com sublista indentada
- Tabela mesclada: células com colspan e rowspan abrindo corretamente no Word

- [ ] **Step 3: Commit final**

```bash
git add -A
git commit -m "feat: complete Fase 6 — reference model, multilingual abstract, appendix numbering, nested lists, table merge"
```

---

## Checklist de conclusão da Fase 6

| Correção | Task | Arquivo(s) principal(is) |
|---|---|---|
| `ReferenceEntry` com campos para periódico, tese e capítulo | Task 1 | `ReferenceEntry.java`, `ReferencesEntryFormatter.java` |
| Novos tipos de referência: `CONFERENCE_PAPER`, `REPORT`, `STANDARD` | Task 1 | `ReferenceType.java` |
| `AbstractComponent` com lista de entradas multilíngue | Task 2 | `AbstractComponent.java`, `AbstractEntry.java`, `AbstractRenderer.java` |
| Numeração de seções em apêndice/anexo com prefixo "A 1", "B 1" | Task 3 | `BodyContentNumberingRule.java`, `AppendixRenderer.java`, `AnnexRenderer.java` |
| `BodyListItem` com sublista opcional aninhada | Task 4 | `BodyListItem.java`, `BodyContentRenderer.java` |
| `BodyTableCell` com colspan/rowspan e OOXML correto | Task 5 | `BodyTableCell.java`, `BodyTableRow.java`, `Docx4jWriter.java` |
| Testes para todas as correções | Tasks 1–5 | — |
| Suite completa verde | Task 6 | — |
