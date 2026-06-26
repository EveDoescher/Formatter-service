# Design: bodyContent Fase 1 — Correções + Fundação Inline

## Contexto

O `bodyContent` já renderiza seções, parágrafos, citações, figuras e tabelas.
Esta fase resolve todos os desvios documentados na análise proposta vs. implementação
e adiciona formatação inline (negrito, itálico, sublinhado), que é a fundação
necessária para as fases seguintes.

A abordagem escolhida é **inline-model-first**: o modelo de runs é redesenhado
primeiro e os bugs de citação são corrigidos em cima do modelo novo, evitando
retrabalho.

## Escopo

### O que entra nesta fase

- Redesenho do modelo inline com suporte a multi-run
- Correções dos desvios GAP-1, GAP-2, GAP-3, GAP-4, GAP-5, GAP-8, GAP-9, GAP-10
- Correções dos desvios DESVIO-1, DESVIO-2, DESVIO-3, DESVIO-5, DESVIO-6, DESVIO-9, DESVIO-10
- Formatação inline: negrito, itálico, sublinhado
- `InvalidBodyContentException` dedicada
- Heading styles com `<w:basedOn>` no writer
- `BodyContentRendererDocxSanityTest`

### O que não entra nesta fase

Listas, blocos de código, equações, notas de rodapé, referências cruzadas,
marcadores de sigla, coleta de metadados. Esses elementos pertencem às fases 2 a 5.

---

## Seção 1 — Modelo inline com multi-run

### Problema atual

`BodyParagraph.text()` concatena todos os itens de `content` em uma única
`String`, que vira um único `<w:r>` no DOCX. Isso impede formatação diferenciada
por trecho e é a raiz do GAP-1.

### Novo record `InlineFormatting`

```text
InlineFormatting
  bold:      Optional<Boolean>   — empty = herda do estilo do parágrafo
  italic:    Optional<Boolean>   — empty = herda do estilo do parágrafo
  underline: Optional<Boolean>   — empty = herda do estilo do parágrafo
```

`Optional.empty()` em qualquer campo significa herança do estilo base, não
ausência de opinião. O writer aplica o override somente quando o campo está
presente.

### Mudanças em `BodyText` e `BodyQuoteText`

Ambos ganham um campo `InlineFormatting formatting` com default
`InlineFormatting.none()` (todos `Optional.empty()`). A API de construção
permite omitir o campo para compatibilidade com código existente.

`BodyCitationCall` **não ganha** `InlineFormatting` — sua formatação vem
exclusivamente do estilo mapeado no perfil.

### Novo `DocxRun`

`DocxParagraph` deixa de receber `String text` e passa a receber
`List<DocxRun>`. Cada `DocxRun` carrega:

```text
DocxRun
  text:         String
  baseStyle:    StyleRule          — estilo base do parágrafo
  formatting:   InlineFormatting   — overrides de caractere
```

O `Docx4jWriter` emite um `<w:r>` separado para cada `DocxRun`. Runs com
`InlineFormatting.none()` emitem apenas `<w:rPr>` derivado do estilo base.
Runs com override emitem os elementos `<w:b>`, `<w:i>`, `<w:u>` adicionais.

### Mudança no renderer

O renderer para de chamar `paragraph.text()` e passa a iterar
`paragraph.content()`, construindo um `DocxRun` por item inline:

- `BodyText` → `DocxRun` com seu texto e seu `InlineFormatting`
- `BodyQuoteText` → `DocxRun` com aspas adicionadas conforme tipo, e seu `InlineFormatting`
- `BodyCitationCall` → `DocxRun` com o texto renderizado e estilo do perfil,
  sem `InlineFormatting` de caractere

---

## Seção 2 — Correções de citação

### GAP-2 / DESVIO-1 — Citações curtas e indiretas como blocos autônomos

`BodyCitation` sai do `sealed interface BodyBlock`. Somente citação direta
longa permanece como bloco, renomeada para `BodyLongQuote` para deixar o tipo
explícito.

```text
BodyBlock permite: BodyParagraph, BodyLongQuote, NumberedDisplayObject
```

`BodyBlockRequest` passa a rejeitar `DIRECT_SHORT_QUOTE`, `INDIRECT_CITATION`
e `CITATION_OF_CITATION` no nível de bloco com `InvalidBodyContentException`
e mensagem clara indicando que esses tipos devem ser usados como inline dentro
de `PARAGRAPH`.

`BodyLongQuote` representa exclusivamente `DIRECT_LONG` e é validada como
bloco próprio com exigência de `page`.

### DESVIO-2 — Tokens hardcoded no domínio

Os tokens `"p. "`, `"et al."`, `"; "` e `" apud "` saem de `CitationSource`
e `BodyCitationCall`.

Novo record no perfil:

```text
CitationFormattingRule
  pagePrefix:        String   — ex: "p. "
  multiAuthorJoiner: String   — ex: "; "
  etAl:              String   — ex: "et al."
  apudConnector:     String   — ex: " apud "
```

`CitationFormattingRule` entra em `BodyContentComponentRule`. O perfil UNIP
declara os valores ABNT atuais. `CitationSource` e `BodyCitationCall` recebem
`CitationFormattingRule` nos métodos de formatação em vez de usar literais.

`CitationFormattingRuleRequest` é criado para o perfil inline, com
`@NotBlank` em todos os campos.

### GAP-1 — Citation call como run diferenciado

Com o modelo de runs da Seção 1, cada `BodyCitationCall` dentro de um
parágrafo vira um `DocxRun` cujo estilo base vem do mapeamento do perfil
(`directShortQuoteStyleId`, `indirectCitationStyleId`, etc.). O marcador
autor-data aparece no DOCX como run separado com estilo próprio, não
concatenado na string do parágrafo.

---

## Seção 3 — Correções estruturais restantes

### Infraestrutura de erro e validação

**`InvalidBodyContentException`** (GAP-3)

Nova exceção em `shared/exception/`, seguindo o padrão de
`InvalidCoverContentException` e `InvalidApprovalSheetContentException`.
Todos os `IllegalArgumentException` lançados no domínio e nos requests do
`bodyContent` migram para ela. O handler HTTP consegue diferenciar erros de
bodyContent de outros contextos.

**`@NotNull` / `@NotBlank` em `FigureRuleRequest` e `TableRuleRequest`** (DESVIO-5)

Todos os campos obrigatórios de `FigureRuleRequest` e `TableRuleRequest`
recebem anotações de validação. Perfil inline incompleto falha na validação
do request com identificação do campo JSON de origem.

**`BodyContentRequest.toDomain()` sem null-coerce silencioso** (DESVIO-6)

O trecho `sections == null ? List.of() : sections` é removido. A anotação
`@NotEmpty` já cobre o caso. O código passa a confiar na validação declarativa.

### Anti-padrões no domínio

**`BodyCitation` sem side-effect de construção** (DESVIO-9)

`BodyCitation.java` é removido inteiramente nesta fase — sua responsabilidade
como bloco autônomo deixa de existir, e a validação de `DIRECT_LONG` passa
para `BodyLongQuote`. O desvio é resolvido pela remoção, não por refatoração.

**`BodyContentComponentRule` documenta ausência de `contentBindings`** (DESVIO-10)

O record implementa o método retornando `Map.of()` com um comentário de
intenção: bodyContent não usa dados de `work` porque seu conteúdo é totalmente
próprio do componente textual.

### Renderer

**Flag `previousRenderedTextWasBodyParagraph` corrigida** (DESVIO-3 / GAP-10)

Renomeada para `previousBlockWasTextualContent`. Passa a ser ativada apenas
por `BodyParagraph` e `BodyLongQuote`. Figuras e tabelas não ativam a flag,
portanto títulos após display objects não recebem blank line extra.

**`SectionNumberingState` com comportamento documentado** (GAP-8)

Quando uma seção de nível 2 aparece sem que o nível 1 tenha sido declarado,
o comportamento atual (forçar `counters[0] = 1`) é mantido mas documentado
explicitamente como política deliberada. A validação de hierarquia em
`BodySection` mitiga a maioria dos casos.

**Heading styles com `<w:basedOn>`** (GAP-5)

`Docx4jWriter.createHeadingStyle()` passa a declarar
`<w:basedOn w:val="Normal"/>` em todos os estilos de heading. Isso garante
que o Word não herde cor azul, espaçamento ou outros defaults do tema do
documento em ambientes diferentes.

**Compilação e ajustes finos de tabelas no `Docx4jWriter`**

Validar e corrigir se necessário: `tblHeader`, `TblBorders`, `CTBorder`,
`TblWidth`, `TrPr`, `TcPr`. O contrato de `DocxTableBlock` não muda —
apenas a materialização no writer é ajustada à API real do docx4j disponível.

**Sample `body-content-figures-url-visual.json` incluído nos testes** (GAP-9)

`BodyContentSampleValidationTest` passa a incluir o sample de figura com URL
na lista de validação automática.

---

## Seção 4 — Testes

### Modelo inline e runs

- `InlineFormattingTest` — combinações de bold/italic/underline, herança via `Optional.empty()`
- `BodyParagraphTest` — atualizado para content com múltiplos runs heterogêneos
- `BodyContentRendererTest` — cada inline vira `DocxRun` separado; figura/tabela
  antes de título não insere blank line extra
- `BodyContentRendererDocxSanityTest` (novo, resolve GAP-4) — abre o `.docx`
  gerado e inspeciona `<w:r>` individuais, `<w:pStyle>`, heading styles com
  `<w:basedOn>`

### Citações

- `CitationFormattingRuleTest` — tokens vêm do perfil, não hardcoded
- `BodyCitationCallTest` — usa `CitationFormattingRule` do perfil; formata
  corretamente parenthetical, narrative e apud
- `BodyBlockRequestTest` — rejeita `DIRECT_SHORT_QUOTE`, `INDIRECT_CITATION`,
  `CITATION_OF_CITATION` como bloco com `InvalidBodyContentException`
- `BodyLongQuoteTest` — valida exigência de page e comportamento como bloco

### Perfil e validação

- `FigureRuleRequestTest` / `TableRuleRequestTest` — campos obrigatórios
  rejeitam null com identificação do campo
- `CitationFormattingRuleRequestTest` — campos `@NotBlank` rejeitam blank

### Renderer e samples

- `BodyContentSampleValidationTest` — inclui `body-content-figures-url-visual.json`
- Sample válido `body-content-inline-formatting.json` — parágrafo com negrito,
  itálico, sublinhado e citação inline no mesmo parágrafo
- Sample inválido `body-content-inline-formatting-invalid.json` — `DIRECT_SHORT`
  enviado como bloco, deve falhar com `InvalidBodyContentException`

### Integração

- `DocxExportControllerIntegrationTest` atualizado para cobrir bodyContent com
  os novos samples

---

## Arquivos afetados (estimativa)

### Domínio
- `BodyBlock.java` — remove `BodyCitation`, adiciona `BodyLongQuote`
- `BodyLongQuote.java` — novo record (substitui `BodyCitation` como bloco)
- `BodyCitation.java` — removido (funcionalidade migra para `BodyCitationCall` inline)
- `BodyText.java` — adiciona `InlineFormatting`
- `BodyQuoteText.java` — adiciona `InlineFormatting`
- `CitationSource.java` — recebe `CitationFormattingRule` nos métodos de formatação
- `BodyCitationCall.java` — usa `CitationFormattingRule`; remove hardcode
- `InlineFormatting.java` — novo record
- `BodyContentComponent.java` — lança `InvalidBodyContentException`

### Perfil
- `BodyContentComponentRule.java` — adiciona `CitationFormattingRule`; documenta ausência de contentBindings
- `CitationFormattingRule.java` — novo record
- `CitationFormattingRuleRequest.java` — novo DTO com validação
- `FigureRuleRequest.java` — adiciona `@NotNull`/`@NotBlank`
- `TableRuleRequest.java` — adiciona `@NotNull`/`@NotBlank`
- `abnt-unip-profile.json` — adiciona `citationFormatting` com valores ABNT

### Request
- `BodyBlockRequest.java` — rejeita tipos inline como bloco
- `BodyContentRequest.java` — remove null-coerce silencioso
- `BodyBlockType.java` — remove `DIRECT_SHORT_QUOTE`, `INDIRECT_CITATION`, `CITATION_OF_CITATION` do enum; `BodyBlockType` passa a conter apenas `PARAGRAPH`, `DIRECT_LONG_QUOTE`, `FIGURE`, `TABLE`
- `BodyInlineRequest.java` — adiciona `InlineFormatting` para `TEXT` e `QUOTE_TEXT`

### Output
- `DocxParagraph.java` — muda de `String text` para `List<DocxRun>`
- `DocxRun.java` — novo record
- `Docx4jWriter.java` — emite `<w:r>` por run; adiciona `<w:basedOn>` em headings; ajustes de tabela

### Shared
- `InvalidBodyContentException.java` — nova exceção

### Rendering
- `BodyContentRenderer.java` — itera inlines como runs; renomeia e corrige flag; usa `BodyLongQuote`

---

## Regras que não devem ser quebradas

- Nenhum token acadêmico (`"p. "`, `"et al."`, `"apud"`) permanece hardcoded no domínio ou renderer.
- `BodyBlock` só aceita tipos que genuinamente ocupam nível de bloco.
- Perfil incompleto falha cedo com mensagem clara identificando o campo.
- O writer materializa contratos — não toma decisões de layout ou estilo.
- Samples inválidos devem falhar antes da geração do DOCX.
