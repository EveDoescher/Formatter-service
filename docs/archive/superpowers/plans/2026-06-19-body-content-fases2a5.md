# bodyContent — Fases 2 a 5

> **Pré-requisito:** Fase 1 concluída e pushada (branch `feature/elementos-textuais`, 289 testes verdes).

Este documento define o escopo e a estratégia das quatro fases que completam o `bodyContent` e os componentes que dependem dele. Baseado no `docs/manual_de_normalizacao_abnt.pdf` (UNIP/ABNT), no design doc da Fase 1 e no roadmap arquitetural.

## Contexto: o que Fase 1 entregou

- `InlineFormatting` (bold/italic/underline) em `BodyText`, `BodyQuoteText` e `DocxRun`
- `DocxParagraph` com `List<DocxRun>` — writer emite `<w:r>` por run
- `CitationFormattingRule` no perfil — tokens de citação saíram do domínio
- `BodyLongQuote` como bloco autônomo (`DIRECT_LONG_QUOTE`)
- `BodyFigure` e `BodyTable` como objetos numerados com continuação
- `BodyBlockType` reduzido a `PARAGRAPH`, `DIRECT_LONG_QUOTE`, `FIGURE`, `TABLE`
- Headings reais do Word com `<w:basedOn Normal>`
- Numeração de seções profile-driven

## O que ficou para as fases seguintes

O design doc `2026-06-16-body-content-fase1-design.md` registrou explicitamente:

> Listas, blocos de código, equações, notas de rodapé, referências cruzadas, marcadores de sigla, coleta de metadados. Esses elementos pertencem às fases 2 a 5.

O manual ABNT/UNIP e a ABNT NBR 10520/14724 confirmam elementos adicionais não cobertos:

- **Quadro** — distinto de Tabela: bordas fechadas, conteúdo qualitativo/textual, numerado separadamente ("Quadro 1")
- **Superscript e Subscript** — faltam em `InlineFormatting` (H₂O, CO₂, notação matemática inline)
- **Supressões, interpolações e ênfase** dentro de `QUOTE_TEXT` — seção própria na ABNT NBR 10520
- **Citação de informação verbal** — tipo semântico próprio (palestra, aula, comunicação pessoal), vai em nota de rodapé
- **Lista de símbolos** e **Lista de quadros** — faltam nos pré-textuais da Fase 5
- **Errata, Dedicatória, Agradecimentos, Epígrafe** — pré-textuais listados no manual, faltam na Fase 4

---

## Dependências entre fases

```
Fase 1 (concluída)
  └── Fase 2 (listas, quadro, code listing, chart, equações, siglas, footnotes,
              superscript/subscript, supressões/interpolações, citação verbal)
        └── Fase 3 (metadados + referências cruzadas)  ← depende de Fase 2
              └── Fase 5 (sumário, listas derivadas)   ← depende de Fase 3
  └── Fase 4 (references, appendix, annex, glossary,
              resumo, abstract, errata, dedicatória,
              agradecimentos, epígrafe)                ← independente
```

---

## Fase 2 — Blocos e Inline Especializados

**Meta:** Completar todos os tipos de bloco e de conteúdo inline que o `bodyContent` suporta. Esta é a fase de maior volume.

### 2.1 Listas

**Novos block types:** `ORDERED_LIST`, `UNORDERED_LIST`

Cada lista tem `items[]`. Cada item tem `content[]` — mesma estrutura de inline content de `BodyParagraph`. Listas aninhadas não são suportadas nesta fase — item com outra lista no `content` é rejeitado com `InvalidBodyContentException`.

**Domínio:**

```
BodyList (implements BodyBlock)
  type: BodyListType (ORDERED | UNORDERED)
  items: List<BodyListItem>

BodyListItem
  content: List<BodyInline>
```

`BodyBlock`:

```java
public sealed interface BodyBlock
    permits BodyParagraph, BodyLongQuote, BodyList, NumberedDisplayObject {
}
```

**Perfil:**

```
bodyContent.styleMapping.listOrderedStyleId
bodyContent.styleMapping.listUnorderedStyleId
```

**Output DOCX:** Novo record `DocxListItemParagraph` implementando `DocxBlock`:

```
DocxListItemParagraph
  runs: List<DocxRun>
  styleRule: StyleRule
  listType: BodyListType
  listLevel: int  (sempre 0 nesta fase)
```

O writer cria dois `<w:abstractNum>` (um para ordered, um para unordered) uma única vez por documento e os reutiliza. Cada item recebe `<w:numPr>` apontando para o `<w:num>` correspondente.

**Samples:**

```
docs/samples/body-content/body-content-lists.json
docs/samples/body-content/body-content-lists-mixed.json
docs/samples/body-content/body-content-lists-nested-invalid.json
```

---

### 2.2 Quadro

**Novo block type:** `FRAME` (Quadro em ABNT)

Quadro é **distinto de Tabela** segundo a ABNT:

| | Tabela | Quadro |
|---|---|---|
| Conteúdo | Dados quantitativos | Dados qualitativos/textuais |
| Bordas | Abertas (sem bordas verticais externas, sem borda superior/inferior) | Fechadas (todas as bordas) |
| Numeração | "Tabela 1" | "Quadro 1" |
| Lista pré-textual | Lista de tabelas | Lista de quadros |

Estruturalmente igual a `BodyTable` (caption + grid + source), mas com regras de borda e template diferentes. `BodyFrame` implementa `NumberedDisplayObject` separadamente.

**Domínio:**

```
BodyFrame (implements NumberedDisplayObject)
  id: String
  continuationGroupId: Optional<String>
  caption: String
  columns: List<BodyTableColumn>
  rows: List<BodyTableRow>
  source: Optional<String>
```

**Perfil:**

```
bodyContent.frame.captionStyleId
bodyContent.frame.sourceStyleId
bodyContent.frame.headerStyleId
bodyContent.frame.cellStyleId
bodyContent.frame.captionTemplate        = "Quadro {number} - {caption}"
bodyContent.frame.sourceTemplate         = "Fonte: {source}"
bodyContent.frame.continuationLabels
bodyContent.frame.sourcePlacement
bodyContent.frame.tableAlignment
bodyContent.frame.widthPercent
bodyContent.frame.repeatHeaderOnPageBreak
```

A diferença visual do Quadro (bordas fechadas em todos os lados) é declarada via `DocxTableBlock` com um campo `borderStyle: TableBorderStyle (OPEN | CLOSED)`. O writer aplica `<w:tblBorders>` de acordo. Tabela ABNT usa `OPEN`; Quadro ABNT usa `CLOSED`.

**Samples:**

```
docs/samples/body-content/body-content-frames.json
docs/samples/body-content/body-content-frame-continuation.json
```

---

### 2.3 Code Listing (Listagem de Código)

**Novo block type:** `CODE_LISTING`

Pertence à família de display objects numerados. Composta por:

```
caption
code block (texto literal, fonte monoespaçada)
source (opcional)
```

**Domínio:**

```
BodyCodeListing (implements NumberedDisplayObject)
  id: String
  continuationGroupId: Optional<String>
  caption: String
  language: Optional<String>   — informativo
  code: String                 — texto literal com quebras de linha internas
  source: Optional<String>
```

**Perfil:**

```
bodyContent.codeListing.captionStyleId
bodyContent.codeListing.codeStyleId
bodyContent.codeListing.sourceStyleId
bodyContent.codeListing.captionTemplate     = "Listagem {number} - {caption}"
bodyContent.codeListing.sourceTemplate      = "Fonte: {source}"
bodyContent.codeListing.continuationLabels
bodyContent.codeListing.sourcePlacement
```

Estilo `bodyContent.code` no perfil: `Courier New`, tamanho 10, alinhamento LEFT.

**Output DOCX:** Um `DocxParagraph` por linha de código. As quebras `\n` no campo `code` geram parágrafos individuais com o estilo de código. `DisplayObjectRenderingState` reutilizado sem alteração.

**Samples:**

```
docs/samples/body-content/body-content-code-listings.json
docs/samples/body-content/body-content-code-listing-continuation.json
```

---

### 2.4 Chart (Gráfico)

**Novo block type:** `CHART`

Pertence à família de display objects numerados. Estruturalmente idêntico a `BodyFigure` — renderiza imagem com caption e source, mas numerado separadamente ("Gráfico 1").

**Domínio:**

```
BodyChart (implements NumberedDisplayObject)
  id: String
  continuationGroupId: Optional<String>
  caption: String
  image: BodyImageSource
  source: Optional<String>
```

**Perfil:**

```
bodyContent.chart.captionStyleId
bodyContent.chart.sourceStyleId
bodyContent.chart.captionTemplate         = "Gráfico {number} - {caption}"
bodyContent.chart.sourceTemplate          = "Fonte: {source}"
bodyContent.chart.continuationLabels
bodyContent.chart.sourcePlacement
bodyContent.chart.imageAlignment
bodyContent.chart.maxWidthCm
bodyContent.chart.maxHeightCm
bodyContent.chart.defaultDpi
bodyContent.chart.maxImageBytes
bodyContent.chart.urlFetchTimeoutSeconds
bodyContent.chart.fitPolicy
```

O renderer extrai um método genérico `renderImageDisplayObject` compartilhado por `BodyFigure` e `BodyChart`.

**Samples:**

```
docs/samples/body-content/body-content-charts.json
```

---

### 2.5 Equações

**Novo block type:** `EQUATION`

Equações são blocos centralizados, não numerados no perfil UNIP atual (podem ganhar numeração futura via perfil).

**Domínio:**

```
BodyEquation (implements BodyBlock)
  text: String             — representação textual (LaTeX ou texto plano)
  label: Optional<String>  — identificador para referência cruzada futura
```

Nesta fase, `text` é renderizado como parágrafo com estilo de equação. Suporte a LaTeX/MathML fica para fase futura.

**Perfil:**

```
bodyContent.styleMapping.equationStyleId
```

Estilo `bodyContent.equation`: Times New Roman, tamanho 12, alinhamento CENTER.

**Samples:**

```
docs/samples/body-content/body-content-equations.json
```

---

### 2.6 Superscript e Subscript em InlineFormatting

`InlineFormatting` ganha dois campos adicionais:

```java
public record InlineFormatting(
    Optional<Boolean> bold,
    Optional<Boolean> italic,
    Optional<Boolean> underline,
    Optional<Boolean> superscript,   // NOVO
    Optional<Boolean> subscript      // NOVO
) { ... }
```

`InlineFormattingRequest` ganha campos correspondentes.

No `Docx4jWriter`, ao encontrar `superscript = true`, emite `<w:vertAlign w:val="superscript"/>` no `<w:rPr>`. Para `subscript = true`, emite `<w:vertAlign w:val="subscript"/>`.

Superscript e subscript são mutuamente exclusivos — se ambos forem `true`, lançar `InvalidBodyContentException`.

**Casos de uso imediatos:**
- Notas de rodapé (superscript automático via `BodyFootnote`)
- Fórmulas químicas inline: H₂O, CO₂ (subscript manual via `BodyText`)
- Potências matemáticas: x² (superscript manual)

**Samples:** Incluir casos de superscript e subscript no sample de inline formatting existente.

---

### 2.7 Marcadores de Sigla (Inline)

**Novo inline type:** `ABBREVIATION`

**Domínio:**

```
BodyAbbreviation (implements BodyInline)
  abbreviation: String   — ex: "ABNT"
  expansion: String      — ex: "Associação Brasileira de Normas Técnicas"
```

`renderedText()` retorna apenas `abbreviation`. A expansion é coletada pelo renderer para a lista de abreviaturas (Fase 5). Renderizado com o estilo base do parágrafo ou com `abbreviationStyleId` opcional do perfil.

**Samples:**

```
docs/samples/body-content/body-content-abbreviations.json
```

---

### 2.8 Notas de Rodapé (Inline)

**Novo inline type:** `FOOTNOTE`

**Domínio:**

```
BodyFootnote (implements BodyInline)
  content: List<BodyInline>   — conteúdo da nota (TEXT, CITATION, QUOTE_TEXT)
```

`renderedText()` retorna string vazia — apenas a chamada superscript aparece no fluxo do parágrafo.

**Perfil:**

```
bodyContent.styleMapping.footnoteCallStyleId
bodyContent.styleMapping.footnoteTextStyleId
```

**Output DOCX:** O writer acumula notas em um mapa `footnoteId → conteúdo` e injeta `<w:footnotes>` no pacote após processar todos os blocos.

**Nota:** A **citação de informação verbal** (palestra, aula, comunicação pessoal) é modelada como `FOOTNOTE` com um `content` contendo `TEXT` + um `BodyCitationCall` de tipo `VERBAL`. Novo `BodyCitationType.VERBAL` é adicionado:

```
BodyCitationType:
  DIRECT_SHORT
  DIRECT_LONG
  INDIRECT
  CITATION_OF_CITATION
  VERBAL                 ← NOVO — informação verbal (palestra, aula, entrevista)
```

`VERBAL` não exige `page`. O perfil decide como formatar a chamada (tipicamente "informação verbal" entre parênteses).

**Samples:**

```
docs/samples/body-content/body-content-footnotes.json
docs/samples/body-content/body-content-footnotes-empty-content-invalid.json
```

---

### 2.9 Supressões, Interpolações e Ênfase em Citações

A ABNT NBR 10520 define regras para modificações dentro do texto citado:

- **Supressão** `[...]`: trecho omitido da citação original
- **Interpolação** `[texto]`: texto inserido pelo citante para clareza
- **Ênfase do citante** — grifo adicionado pelo autor do trabalho: requer nota `(grifo nosso)` emitida automaticamente após a chamada de citação
- **Ênfase do autor original** — grifo que já existia no original: requer nota `(grifo do autor)` emitida automaticamente

Esses elementos são modelados dentro de `QUOTE_TEXT`:

**Domínio:** `BodyQuoteText` ganha campos:

```java
public record BodyQuoteText(
    BodyQuoteType type,
    String text,
    InlineFormatting formatting,
    List<BodyQuoteMarker> markers   // NOVO
) implements BodyInline { ... }
```

```
BodyQuoteMarker (record)
  markerType: BodyQuoteMarkerType
  position: int   — índice de caractere no text onde o marcador é inserido

BodyQuoteMarkerType:
  SUPPRESSION      — insere "[...]" na posição
  INTERPOLATION    — o trecho entre position e endPosition é interpolação "[texto]"
  EMPHASIS_OURS    — formata trecho em itálico e acrescenta "(grifo nosso)" após a citação
  EMPHASIS_AUTHOR  — acrescenta "(grifo do autor)" após a citação
```

O `renderedText()` de `BodyQuoteText` aplica os marcadores antes de retornar o texto. O renderer gera a nota de ênfase (`grifo nosso`/`grifo do autor`) como texto no run seguinte ao `QUOTE_TEXT`, usando o estilo base do parágrafo.

**Perfil:**

```
bodyContent.citationFormatting.suppressionMarker    = "[...]"
bodyContent.citationFormatting.emphasisOursLabel    = "grifo nosso"
bodyContent.citationFormatting.emphasisAuthorLabel  = "grifo do autor"
```

**Samples:**

```
docs/samples/body-content/body-content-quote-markers.json
docs/samples/body-content/body-content-quote-markers-invalid.json
```

---

### Checklist Fase 2

**InlineFormatting:**
- [ ] Adicionar `superscript` e `subscript` a `InlineFormatting` e `InlineFormattingRequest`
- [ ] Writer emite `<w:vertAlign>` para superscript/subscript
- [ ] Rejeitar superscript + subscript simultâneos
- [ ] Atualizar samples existentes e testes

**Listas:**
- [ ] `BodyList`, `BodyListItem`, `BodyListType`
- [ ] `BodyBlockType` com `ORDERED_LIST` e `UNORDERED_LIST`
- [ ] `BodyListRequest`, `BodyListItemRequest`
- [ ] `DocxListItemParagraph` record
- [ ] Writer cria `<w:abstractNum>` e aplica `<w:numPr>` por item
- [ ] Perfil com `listOrderedStyleId` e `listUnorderedStyleId`
- [ ] Samples e testes

**Quadro:**
- [ ] `BodyFrame` implementando `NumberedDisplayObject`
- [ ] `BodyBlockType.FRAME`
- [ ] `BodyFrameRequest`
- [ ] Perfil com regras de `frame`
- [ ] `DocxTableBlock` com campo `TableBorderStyle (OPEN | CLOSED)`
- [ ] Writer aplica bordas fechadas para `CLOSED` e bordas abertas (ABNT) para `OPEN`
- [ ] `DisplayObjectRenderingState` reutilizado para quadros
- [ ] Samples e testes

**Code Listing:**
- [ ] `BodyCodeListing` implementando `NumberedDisplayObject`
- [ ] `BodyBlockType.CODE_LISTING`
- [ ] `BodyCodeListingRequest`
- [ ] Perfil com regras de `codeListing`
- [ ] Renderer divide `code` em linhas e emite um `DocxParagraph` por linha
- [ ] Samples e testes

**Chart:**
- [ ] `BodyChart` implementando `NumberedDisplayObject`
- [ ] `BodyBlockType.CHART`
- [ ] `BodyChartRequest`
- [ ] Perfil com regras de `chart`
- [ ] Renderer extrai `renderImageDisplayObject` genérico compartilhado com `BodyFigure`
- [ ] Samples e testes

**Equações:**
- [ ] `BodyEquation` implementando `BodyBlock`
- [ ] `BodyBlockType.EQUATION`
- [ ] `BodyEquationRequest`
- [ ] Perfil com `equationStyleId`
- [ ] Samples e testes

**Siglas:**
- [ ] `BodyAbbreviation` implementando `BodyInline`
- [ ] `BodyInlineType.ABBREVIATION`
- [ ] `BodyAbbreviationRequest`
- [ ] Coleta de siglas pelo renderer (lista ordenada por ordem de aparição)
- [ ] Samples e testes

**Notas de Rodapé + Citação Verbal:**
- [ ] `BodyFootnote` implementando `BodyInline`
- [ ] `BodyInlineType.FOOTNOTE`
- [ ] `BodyFootnoteRequest`
- [ ] `BodyCitationType.VERBAL` adicionado
- [ ] Perfil com `footnoteCallStyleId` e `footnoteTextStyleId`
- [ ] Writer emite `<w:footnotes>` após processar todos os blocos
- [ ] Samples e testes

**Supressões, Interpolações e Ênfase:**
- [ ] `BodyQuoteMarker` e `BodyQuoteMarkerType`
- [ ] `BodyQuoteText` ganha `List<BodyQuoteMarker>`
- [ ] `BodyQuoteMarkerRequest`
- [ ] `CitationFormattingRule` ganha `suppressionMarker`, `emphasisOursLabel`, `emphasisAuthorLabel`
- [ ] Renderer emite run extra com "(grifo nosso)"/"(grifo do autor)" quando necessário
- [ ] Samples e testes

**Suite completa:**
- [ ] Todos os novos block types em `BodyBlockType` (ORDERED_LIST, UNORDERED_LIST, FRAME, CODE_LISTING, CHART, EQUATION)
- [ ] Todos os novos inline types em `BodyInlineType` (ABBREVIATION, FOOTNOTE)
- [ ] 289+ testes passando
- [ ] Validação visual no Word para cada tipo de bloco novo

---

## Fase 3 — Metadados e Referências Cruzadas

**Meta:** Instrumentar o renderer para emitir metadados de todas as séries de objetos numerados. Usar esses metadados para referências cruzadas inline.

**Pré-requisito:** Fase 2 concluída.

### 3.1 Metadados de Renderização

`BodyContentRenderer.render()` passa a retornar um objeto composto:

```java
public record BodyContentRenderResult(
    List<DocxBlock> blocks,
    BodyContentMetadata metadata
)

public record BodyContentMetadata(
    List<BodySectionMetadata> sections,
    List<BodyFigureMetadata> figures,
    List<BodyTableMetadata> tables,
    List<BodyFrameMetadata> frames,
    List<BodyChartMetadata> charts,
    List<BodyCodeListingMetadata> codeListings,
    List<BodyAbbreviationMetadata> abbreviations
)

// Todos com campos: id, number, caption (ou renderedTitle para seções)
```

Interface para não quebrar `ComponentRenderer<T>`:

```java
public interface MetadataEmittingRenderer<T extends DocumentComponent>
    extends ComponentRenderer<T> {

    BodyContentRenderResult renderWithMetadata(T component, DocumentProfile profile);

    @Override
    default List<DocxBlock> render(T component, DocumentProfile profile) {
        return renderWithMetadata(component, profile).blocks();
    }
}
```

`DocumentRenderer` detecta `MetadataEmittingRenderer` e armazena metadados em `DocumentRenderResult`.

### 3.2 Referências Cruzadas (Inline)

**Novo inline type:** `CROSS_REFERENCE`

**Domínio:**

```
BodyCrossReference (implements BodyInline)
  targetId: String
  targetType: CrossReferenceTargetType
  displayMode: CrossReferenceDisplayMode
```

```
CrossReferenceTargetType:
  SECTION | FIGURE | TABLE | FRAME | CHART | CODE_LISTING | EQUATION

CrossReferenceDisplayMode:
  NUMBER_ONLY      — "1", "2.1"
  LABEL_AND_NUMBER — "Figura 1", "Quadro 2", "Seção 1.1"
  CAPTION          — texto completo da legenda/título
```

O renderer resolve referências cruzadas após a primeira passagem (quando todos os metadados estão disponíveis). `targetId` desconhecido lança `InvalidBodyContentException` antes de gerar DOCX.

**Perfil:**

```
bodyContent.crossReference.sectionLabel      = "Seção"
bodyContent.crossReference.figureLabel       = "Figura"
bodyContent.crossReference.tableLabel        = "Tabela"
bodyContent.crossReference.frameLabel        = "Quadro"
bodyContent.crossReference.chartLabel        = "Gráfico"
bodyContent.crossReference.codeListingLabel  = "Listagem"
bodyContent.crossReference.equationLabel     = "Equação"
```

**Samples:**

```
docs/samples/body-content/body-content-cross-references.json
docs/samples/body-content/body-content-cross-reference-unknown-id-invalid.json
```

### Checklist Fase 3

- [ ] `BodyContentMetadata` e `BodyContentRenderResult`
- [ ] `MetadataEmittingRenderer` interface
- [ ] `BodyContentRenderer` implementa `MetadataEmittingRenderer`
- [ ] Metadados emitidos para seções, figuras, tabelas, quadros, gráficos, listagens
- [ ] `BodyAbbreviationMetadata` coletada para Fase 5
- [ ] `DocumentRenderer` armazena metadados em `DocumentRenderResult`
- [ ] `BodyCrossReference`, `CrossReferenceTargetType`, `CrossReferenceDisplayMode`
- [ ] `BodyInlineType.CROSS_REFERENCE`
- [ ] `BodyCrossReferenceRequest`
- [ ] Perfil com labels de referência cruzada para todos os tipos
- [ ] Resolução de referências cruzadas na segunda passagem
- [ ] Rejeição de `targetId` desconhecido
- [ ] Samples e testes

---

## Fase 4 — Pré-textuais Especializados e Pós-textuais

**Meta:** Implementar os componentes que completam o trabalho acadêmico — tanto os pré-textuais que o manual ABNT/UNIP lista quanto os pós-textuais.

**Independente das Fases 2 e 3.**

### 4.1 Pré-textuais single-page: Errata, Dedicatória, Epígrafe

Esses três componentes são single-page — reutilizam a infra existente de `SinglePageLayoutRule`.

**Errata:**

```
ErrataComponent (implements DocumentComponent)
  entries: List<ErrataEntry>

ErrataEntry
  page: String
  line: String
  incorrectText: String
  correctText: String
```

Perfil: `errata.heading.text`, estilos de entrada, template de linha de errata.

**Dedicatória:**

```
DedicationComponent (implements DocumentComponent)
  text: String
```

Texto alinhado à direita na metade inferior da página, conforme ABNT. Perfil controla posição e estilo.

**Epígrafe:**

```
EpigraphComponent (implements DocumentComponent)
  text: String
  author: String
  source: Optional<String>
```

Texto recuado à direita, itálico, seguido de autoria. Perfil controla recuo, estilo e template de autoria.

### 4.2 Pré-textuais flow: Agradecimentos, Resumo, Abstract

**Agradecimentos:**

```
AcknowledgmentsComponent (implements DocumentComponent)
  text: String
```

Usa base `flow-content` quando for longo. Perfil: `acknowledgments.heading.text`, estilo do texto.

**Resumo e Abstract:**

```
ResumoComponent (implements DocumentComponent)
  text: String
  keywords: List<String>

AbstractComponent (implements DocumentComponent)
  text: String
  keywords: List<String>
```

Perfil:

```
resumo.heading.text        = "RESUMO"
resumo.heading.styleId
resumo.styleMapping.textStyleId
resumo.styleMapping.keywordsStyleId
resumo.keywordsLabel       = "Palavras-chave:"
resumo.keywordsSeparator   = "; "

abstract.heading.text      = "ABSTRACT"
...
```

### 4.3 Pós-textual: references

**Domínio:**

```
ReferencesComponent (implements DocumentComponent)
  entries: List<ReferenceEntry>

ReferenceEntry
  id: String
  type: ReferenceType (BOOK | BOOK_CHAPTER | JOURNAL | WEBSITE | LEGISLATION | THESIS)
  authors: List<ReferenceAuthor>
  title: String
  subtitle: Optional<String>
  edition: Optional<String>
  city: Optional<String>
  publisher: Optional<String>
  year: String
  pages: Optional<String>
  url: Optional<String>
  accessDate: Optional<String>
```

`ReferencesEntryFormatter` formata cada entrada por tipo ABNT — lógica textual, não visual.

**Perfil:**

```
references.heading.text                = "REFERÊNCIAS"
references.heading.styleId
references.styleMapping.entryStyleId
references.layout.blankLinesBetweenEntries
```

### 4.4 Pós-textuais: appendix e annex

**Domínio:**

```
AppendixComponent (implements DocumentComponent)
  items: List<AppendixItem>

AppendixItem
  title: String
  sections: List<BodySection>
```

Renderer calcula A, B, C... automaticamente. Reutiliza `BodyContentRenderer.renderContentBlock()` internamente. Display objects dentro de apêndice são numerados independentemente do bodyContent.

**Perfil:**

```
appendix.heading.template  = "APÊNDICE {letter} — {title}"
appendix.heading.styleId
appendix.styleMapping.paragraphStyleId
appendix.styleMapping.sectionTitleStyleIdsByLevel[]

annex.heading.template     = "ANEXO {letter} — {title}"
...
```

### 4.5 Pós-textual: glossary

**Domínio:**

```
GlossaryComponent (implements DocumentComponent)
  entries: List<GlossaryEntry>

GlossaryEntry
  term: String
  definition: String
```

**Perfil:**

```
glossary.heading.text      = "GLOSSÁRIO"
glossary.heading.styleId
glossary.styleMapping.entryStyleId
glossary.termSeparator     = " — "
```

### Ordem no componentOrder

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

### Checklist Fase 4

- [ ] `ErrataComponent` + `ErrataRenderer` (single-page)
- [ ] `DedicationComponent` + `DedicationRenderer` (single-page, texto à direita na metade inferior)
- [ ] `EpigraphComponent` + `EpigraphRenderer` (single-page, texto recuado à direita)
- [ ] `AcknowledgmentsComponent` + `AcknowledgmentsRenderer` (flow)
- [ ] `ResumoComponent` + `ResumoRenderer` (flow)
- [ ] `AbstractComponent` + `AbstractRenderer` (flow)
- [ ] `ReferencesComponent`, `ReferenceEntry`, `ReferenceType`, `ReferencesEntryFormatter`
- [ ] `ReferencesRenderer` registrado
- [ ] `AppendixComponent` + `AppendixRenderer` com letras automáticas
- [ ] `AnnexComponent` + `AnnexRenderer`
- [ ] `GlossaryComponent` + `GlossaryRenderer`
- [ ] Todos os componentes em `componentOrder` do perfil
- [ ] Samples válidos e inválidos para cada componente
- [ ] Testes unitários e de integração
- [ ] Validado visualmente no Word

---

## Fase 5 — Componentes Gerados (Índices Derivados)

**Meta:** Componentes que se geram automaticamente a partir dos metadados da Fase 3.

**Pré-requisito:** Fase 3 concluída — `BodyContentMetadata` disponível.

### 5.1 summary (Sumário)

`SummaryRenderer` usa `BodyContentMetadata.sections`. Estratégia: campo TOC do Word (`<w:fldChar>` com instrução `TOC`). O Word atualiza os números de página ao abrir (Ctrl+A → F9). Isso é documentado como comportamento esperado.

**Perfil:**

```
summary.heading.text                   = "SUMÁRIO"
summary.heading.styleId
summary.styleMapping.entryStyleIdsByLevel[]
summary.useTocField                    = true
```

### 5.2 Listas de ilustrações

Usam metadados de cada série de objetos numerados:

```
ListOfFiguresComponent     → BodyContentMetadata.figures
ListOfTablesComponent      → BodyContentMetadata.tables
ListOfFramesComponent      → BodyContentMetadata.frames    (Quadros)
ListOfChartsComponent      → BodyContentMetadata.charts
ListOfCodeListingsComponent → BodyContentMetadata.codeListings
```

Perfil de cada lista:

```
listOfFigures.heading.text       = "LISTA DE ILUSTRAÇÕES"
listOfTables.heading.text        = "LISTA DE TABELAS"
listOfFrames.heading.text        = "LISTA DE QUADROS"
listOfCharts.heading.text        = "LISTA DE GRÁFICOS"
listOfCodeListings.heading.text  = "LISTA DE LISTAGENS"
```

### 5.3 listOfAbbreviations e listOfSymbols

**Lista de Abreviaturas e Siglas:**

Usa `BodyContentMetadata.abbreviations`. Renderiza em ordem alfabética com `abbreviation — expansion`.

**Lista de Símbolos:**

```
ListOfSymbolsComponent (implements DocumentComponent)
  entries: List<SymbolEntry>

SymbolEntry
  symbol: String
  meaning: String
```

Diferente da lista de abreviaturas, a lista de símbolos é fornecida explicitamente pelo usuário (não coletada automaticamente do texto), pois símbolos matemáticos e científicos não têm marcação inline padronizável.

**Perfil:**

```
listOfAbbreviations.heading.text   = "LISTA DE ABREVIATURAS E SIGLAS"
listOfAbbreviations.heading.styleId
listOfAbbreviations.styleMapping.entryStyleId
listOfAbbreviations.termSeparator  = " — "
listOfAbbreviations.sortAlphabetically = true

listOfSymbols.heading.text         = "LISTA DE SÍMBOLOS"
listOfSymbols.heading.styleId
listOfSymbols.styleMapping.entryStyleId
listOfSymbols.termSeparator        = " — "
```

### Ordem final no componentOrder

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

### Nota sobre numeração de páginas

Os componentes gerados ficam antes do `bodyContent`. O perfil UNIP declara `pageNumbering.visibleFromComponentId = bodyContent`, portanto as listas não exibem numeração no cabeçalho, mas o campo TOC do Word linka corretamente para as seções do bodyContent.

### Checklist Fase 5

- [ ] `SummaryComponent` + `SummaryRenderer` com campo TOC
- [ ] `ListOfFiguresComponent`, `ListOfTablesComponent`, `ListOfFramesComponent`, `ListOfChartsComponent`, `ListOfCodeListingsComponent` + renderers
- [ ] `ListOfAbbreviationsComponent` + renderer (usa metadados de siglas, ordem alfabética)
- [ ] `ListOfSymbolsComponent` + renderer (conteúdo explícito do usuário)
- [ ] Todos os componentes em `componentOrder` do perfil
- [ ] Samples para cada componente
- [ ] Testes unitários e de integração
- [ ] Validado no Word: TOC mostra seções, Ctrl+A → F9 atualiza páginas

---

## Regra de ouro

```
Conteúdo semântico pertence ao request.
Decisão visual/estrutural pertence ao perfil.
Cálculo derivado pertence ao código.
```
