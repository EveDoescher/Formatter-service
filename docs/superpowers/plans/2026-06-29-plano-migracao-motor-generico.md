# Plano de Migração — Motor Genérico (Taxonomia)

**Data:** 2026-06-29
**Branch:** feature/elementos-textuais → novo branch por bloco recomendado
**Contexto:** Migração do sistema atual para o modelo de taxonomia definido em `docs/motor-generico-taxonomia.md`. Este plano integra e substitui os planos `plano-correcoes-5.5.md`, `2026-06-24-body-content-fase6.md` e `2026-06-24-body-content-fase7-conceito.md`.

> **For agentic workers:** Use superpowers:subagent-driven-development. Steps usam `- [ ]`.
> **Leitura obrigatória antes dos Blocos B–E:** Ler `docs/motor-generico-taxonomia.md` integralmente antes de iniciar qualquer bloco a partir do B. O documento define o modelo alvo — decisões de design dos Blocos C e D dependem diretamente dele. O Bloco A é autocontido e pode ser executado sem essa leitura.

---

## Adaptações em relação aos planos anteriores

| Item original | Decisão |
|---|---|
| 5.5 P1 — cache de BodyContentRenderer no DocumentRenderer | **Descartado.** Resolve o mesmo problema que o Bloco B — a Phase 0. Implementar agora e desfazer depois seria trabalho duplo. |
| 5.5 P11 — `numberingScope: GLOBAL \| PER_SECTION` | **Renomeado e realinhado.** Entra no Bloco B como `numberingStrategy: GLOBAL_SEQUENTIAL \| BY_CHAPTER` conforme a taxonomia, com campos adicionais `label` e `separator`. |
| F6 Task 3 — prefixo de seção em apêndice via `withPrefix` | **Movido para o Bloco C.** O workaround `withPrefix("A ")` seria descartado na migração para `collection` com `itemHeading` por segmentos. Entra diretamente na forma correta. |

---

## Visão geral dos 5 blocos

```
Bloco A — Qualidade imediata         (bugs + completude do modelo de domínio)
Bloco B — Phase 0                    (DisplayObjectCollector standalone)
Bloco C — FlowLayoutEngine           (engine genérico + migração de componentes)
Bloco D — Engine INDEX + profile     (INDEX standalone + profile declarativo)
Bloco E — Pós-processamento UNO      (Fase 7: tabelas, órfãos, PDF)
```

Cada bloco é independente do anterior no sentido de que o sistema compila e funciona ao final de cada um. Blocos C e D dependem do Bloco B.

---

## Bloco A — Qualidade Imediata

**Goal:** Corrigir bugs visuais e estruturais identificados no teste de documento real, expandir o modelo de referências para ABNT NBR 6023:2018, e completar o modelo de domínio com suporte a abstracts multilíngues, sublistas e células mescladas.

**Pré-requisito:** Nenhum. Pode ser implementado imediatamente.

---

### A-1 — Título duplicado em Apêndice e Anexo

**Causa:** `headingTemplate` do perfil monta `"APÊNDICE {letter} — {title}"`. O campo `title` no JSON de teste incluía o prefixo. Além disso, o perfil usa travessão longo `—` (U+2014) onde o manual ABNT-UNIP usa travessão curto `–` (U+2013).

- [ ] Alterar `title` em todos os samples de apêndice/anexo para conter apenas o título sem prefixo
- [ ] Corrigir separador nos templates do perfil de `—` para `–` em `abnt-unip-profile.json`

**Arquivos:** `docs/samples/`, `src/main/resources/abnt-unip-profile.json`

---

### A-2 — Linha em branco após títulos de seção (8 renderers)

**Causa:** Renderers emitem heading e imediatamente o primeiro conteúdo. O manual prescreve separação de 1,5 cm entrelinhas após o título.

**Solução:** Adicionar campo `blankLinesAfterHeading: 1` em cada `ComponentRule` afetado. Cada renderer lê o campo e emite `DocxBlankLine(headingStyle)` após o heading.

- [ ] Adicionar `blankLinesAfterHeading` em: `AcknowledgmentsComponentRule`, `DedicationComponentRule`, `EpigraphComponentRule`, `ErrataComponentRule`, `GlossaryComponentRule`, `ReferencesComponentRule`, `ListOfAbbreviationsComponentRule`, `ListOfSymbolsComponentRule`, `IndexListComponentRule`, `ResumoComponentRule`, `AbstractComponentRule`
- [ ] Adicionar `blankLinesAfterHeading` em `abnt-unip-profile.json` para todos os componentes acima (valor: `1`)
- [ ] Atualizar os renderers correspondentes: `AcknowledgmentsRenderer`, `DedicationRenderer`, `EpigraphRenderer`, `ErrataRenderer`, `GlossaryRenderer`, `ReferencesRenderer`, `ListOfAbbreviationsRenderer`, `ListOfSymbolsRenderer`, `AbstractIndexListRenderer`, `ResumoRenderer`, `AbstractRenderer`

**Arquivos:** Renderers e ComponentRules listados acima + `abnt-unip-profile.json`

---

### A-3 — Label "Palavras-chave" e "Keywords" em negrito + ponto final

**P4 — negrito:**
`ResumoRenderer` e `AbstractRenderer` concatenam label + palavras em um único `DocxRun`. O estilo `resumo.keywords` tem `bold: false` — impossível negritar apenas o label assim.

**Solução P4:** Emitir dois `DocxRun` separados no mesmo `DocxParagraph`:
- Primeiro run: label com `InlineFormatting` `bold=true`
- Segundo run: palavras sem bold

**P5 — ponto final:**
O manual confirma: `"Palavras-chave: Turismo; Mercado de trabalho; Empreendedorismo."` — ponto final obrigatório.

**Solução P5:** Adicionar campo `keywordsTerminator: "."` em `ResumoComponentRule` e `AbstractComponentRule`. Renderers leem e anexam ao final da string de keywords.

- [ ] Adicionar `keywordsTerminator` em `ResumoComponentRule` e `AbstractComponentRule`
- [ ] Adicionar `"keywordsTerminator": "."` em `abnt-unip-profile.json` para resumo e abstract
- [ ] Atualizar `ResumoRenderer`: dois runs separados (label bold + keywords normal) + terminator
- [ ] Atualizar `AbstractRenderer`: mesmo padrão

**Arquivos:** `ResumoRenderer.java`, `AbstractRenderer.java`, `ResumoComponentRule.java`, `AbstractComponentRule.java`, `abnt-unip-profile.json`

---

### A-4 — Página em branco antes da Dedicatória

**Causa:** `DedicationRenderer` emite `new DocxPageBreak()` como primeiro bloco. O `DocumentRenderer` já insere quebras entre componentes — resultado: duas quebras consecutivas.

- [ ] Remover `DocxPageBreak` do `DedicationRenderer`
- [ ] Verificar `EpigraphRenderer` e `AcknowledgmentsRenderer` — remover se tiverem o mesmo padrão

**Arquivos:** `DedicationRenderer.java`, `EpigraphRenderer.java`, `AcknowledgmentsRenderer.java`

---

### A-5 — Errata em tabela

**Causa:** `ErrataRenderer` emite parágrafos de texto com template. O manual prescreve tabela com colunas: **Folha | Linha | Onde se lê | Leia-se**.

- [ ] Adicionar campo `tableHeaders: List<String>` em `ErrataComponentRule`
- [ ] Adicionar estilos `errata.tableHeader` e `errata.tableCell` em `StyleRule` do perfil
- [ ] Adicionar `"tableHeaders": ["Folha", "Linha", "Onde se lê", "Leia-se"]` em `abnt-unip-profile.json`
- [ ] Reescrever `ErrataRenderer` para emitir `DocxTableBlock` com cabeçalho + linhas das entradas
- [ ] Remover `entryTemplate` de `ErrataComponentRule` (obsoleto com a mudança)
- [ ] Atualizar `ErrataComponentRuleRequest` correspondentemente

**Arquivos:** `ErrataRenderer.java`, `ErrataComponentRule.java`, `abnt-unip-profile.json`, `ErrataComponentRuleRequest.java`

---

### A-6 — Duplo ponto nas referências

**Causa:** `formatBook()` monta segmento de edição com ponto final (`" " + ed + " ed."`), e o separador antes da cidade também começa com ponto — resultado: `"3 ed.. Cidade"`.

- [ ] Corrigir `formatBook()`: segmento de edição → `ed + ". ed."`, separador cidade → `" "` quando edição presente, `". "` quando ausente
- [ ] Revisar `formatBookChapter()` e demais métodos pelo mesmo padrão

**Arquivo:** `ReferencesEntryFormatter.java`

---

### A-7 — Estilos TOC no Docx4jWriter

**Causa:** O campo `TOC \o "1-N" \h \z \u` depende dos estilos Word `TOC 1`, `TOC 2`, etc. para formatar pontilhado e indentação por nível. O `Docx4jWriter` não os define — o Word usa defaults da instalação.

**Solução:** Criar método `applyTocStyleDefinitions()` em `Docx4jWriter`, chamado durante a escrita. Para cada nível TOC 1..N, criar estilo Word com:
- Fonte e tamanho do estilo correspondente em `entryStyleIdsByLevel` do `SummaryComponentRule`
- Indentação esquerda crescente por nível (0.5 cm por nível a partir do 2)
- Tab stop alinhado à direita na posição da área de texto com líder de ponto (`.`)

Adicionar campo `List<StyleRule> entryStylesByLevel` ao `DocxTocBlock` para que o writer possa criar os estilos.

- [ ] Adicionar campo `List<StyleRule> entryStylesByLevel` em `DocxTocBlock`
- [ ] Atualizar `SummaryRenderer` para preencher `entryStylesByLevel` ao emitir o `DocxTocBlock`
- [ ] Implementar `applyTocStyleDefinitions()` em `Docx4jWriter`
- [ ] Chamar o método na sequência de escrita do documento

**Arquivos:** `Docx4jWriter.java`, `DocxTocBlock.java`, `SummaryRenderer.java`

---

### A-8 — Comportamentos indefinidos

**P10 — null-coerce silencioso em `BodyContentRequest.toDomain()`**

- [ ] Remover branch `sections == null ? List.of()` de `BodyContentRequest.toDomain()`
- [ ] Deixar `sections.stream()...` diretamente — null explode como NPE no chamador onde pertence

**P12 — `selectedComponents: []` com semântica indefinida**

- [ ] Ler `ComponentSelectionResolver` e verificar comportamento atual para lista vazia
- [ ] Se `[]` = "todos": documentar e adicionar teste explícito
- [ ] Se `[]` = "nenhum": adicionar validação que rejeita com erro claro

**P13 — Componente em `componentOrder` ausente no request**

- [ ] Verificar comportamento atual no loop de `DocumentRenderer` para esse caso
- [ ] Definir e documentar a política: ausente = ignorado sempre, ou depende de `validationMode`?
- [ ] Adicionar teste cobrindo: componente em `componentOrder`, ausente no request, modo `STRICT` vs `FLEXIBLE`

**Arquivos:** `BodyContentRequest.java`, `ComponentSelectionResolver.java`, `DocumentRenderer.java`

---

### A-9 — ReferenceEntry expandido (ABNT NBR 6023:2018)

**Causa:** `ReferenceEntry` usa hacks de campos para tipos como `BOOK_CHAPTER` (`url` para autor do livro, `subtitle` para título do livro). Tipos frequentes como `CONFERENCE_PAPER`, `REPORT`, `STANDARD` não existem no enum. Campos `volume`, `issue`, `doi`, `degree`, `institutionName` ausentes.

- [ ] Adicionar `CONFERENCE_PAPER`, `REPORT`, `STANDARD` em `ReferenceType`
- [ ] Adicionar campos opcionais em `ReferenceEntry`: `volume`, `issue`, `doi`, `degree`, `institutionName`, `bookTitle`, `bookAuthors`
- [ ] Corrigir `formatBookChapter()`: usar `bookTitle` e `bookAuthors` (não mais hacks de `url`/`subtitle`)
- [ ] Corrigir `formatJournal()`: usar `volume`, `issue`, `doi`
- [ ] Corrigir `formatThesis()`: usar `degree`, `institutionName`
- [ ] Adicionar `formatConferencePaper()`, `formatReport()`, `formatStandard()` e os casos no switch
- [ ] Atualizar `ReferenceEntryRequest` com os novos campos opcionais
- [ ] Atualizar `toDomain()` do request para mapear os novos campos
- [ ] Escrever testes: `shouldFormatJournalWithVolumeIssueDoi`, `shouldFormatThesisWithDegreeAndInstitution`, `shouldFormatBookChapterWithOwnFields`, `shouldThrowForBookChapterWithoutBookTitle`, `shouldFormatConferencePaper`, `shouldFormatStandardWithoutAuthors`
- [ ] Criar/atualizar samples: `references-journal.json`, `references-thesis.json`, `references-chapter.json`, `references-conference.json`, `references-standard.json`

**Arquivos:** `ReferenceType.java`, `ReferenceEntry.java`, `ReferencesEntryFormatter.java`, `ReferenceEntryRequest.java`, samples de referências

---

### A-10 — AbstractComponent multilíngue

**Causa:** O sistema suporta apenas um abstract. Programas de pós-graduação exigem PT + EN; programas internacionais adicionam um terceiro idioma.

**Decisão de design:** `headingText` e `keywordsLabel` ficam no `AbstractEntry` (conteúdo), não na rule do perfil. A rule controla apenas estilos tipográficos — que são iguais para todos os idiomas.

- [ ] Criar `AbstractEntry` com campos: `headingText`, `text`, `keywords`, `keywordsLabel`
- [ ] Reescrever `AbstractComponent` para conter `List<AbstractEntry>` (não mais text/keywords diretos)
- [ ] Atualizar `AbstractComponentRule`: remover `headingText` e `keywordsLabel`, adicionar `keywordsTerminator` (unifica com A-3)
- [ ] Reescrever `AbstractRenderer`: itera `entries`, cada entrada em página própria (exceto a primeira), aplica P3 (linha em branco após heading) e P4/P5 (label bold + terminator)
- [ ] Atualizar `AbstractRequest` para conter `List<AbstractEntryRequest>`
- [ ] Criar `AbstractEntryRequest` com `headingText`, `text`, `keywords`, `keywordsLabel`
- [ ] Atualizar `AbstractComponentRuleRequest`: remover `headingText`/`keywordsLabel`, adicionar `keywordsTerminator`
- [ ] Atualizar `abnt-unip-profile.json` para o novo formato de rule
- [ ] Atualizar sample `resumo-simple.json` com dois idiomas (EN + ES)

**Arquivos:** `AbstractEntry.java` (novo), `AbstractComponent.java`, `AbstractComponentRule.java`, `AbstractRenderer.java`, `AbstractRequest.java`, `AbstractEntryRequest.java` (novo), `AbstractComponentRuleRequest.java`, `abnt-unip-profile.json`, samples

---

### A-11 — BodyListItem com sublistas aninhadas

**Causa:** `BodyListItem` aceita apenas `List<BodyInline>` — estrutura plana, sem aninhamento possível.

- [ ] Adicionar `Optional<BodyList> subList` em `BodyListItem`
- [ ] Adicionar construtor de conveniência `BodyListItem(List<BodyInline> content)` para retrocompatibilidade
- [ ] Atualizar `renderList()` em `BodyContentRenderer`: após emitir cada `DocxListItemParagraph`, verificar `item.subList()` e renderizar recursivamente com `nestingLevel + 1`
- [ ] Verificar assinatura de `DocxListItemParagraph` — adicionar parâmetro `nestingLevel` se necessário; garantir que `Docx4jWriter` usa o nível para `ilvl` no OOXML
- [ ] Atualizar `BodyListItemRequest`: adicionar campo `subList` nullable
- [ ] Escrever `NestedListRenderingTest`: verificar que DOCX contém parágrafos com `ilvl` 0, 1, 1

**Arquivos:** `BodyListItem.java`, `BodyContentRenderer.java`, `BodyListItemRequest.java`, `DocxListItemParagraph.java` (verificar)

---

### A-12 — BodyTableCell com colspan/rowspan

**Causa:** `BodyTableRow` contém `List<String>` — células sem metadado de mesclagem. Validação rejeita qualquer linha com célula mesclada. `Docx4jWriter` não emite `<w:gridSpan>` nem `<w:vMerge>`.

- [ ] Criar `BodyTableCell` com campos: `text`, `colspan` (int, mínimo 1), `rowspanStart` (boolean), `rowspanContinuation` (boolean); validar que não são ambos true; construtor de conveniência `BodyTableCell(String text)` para célula normal
- [ ] Reescrever `BodyTableRow` para `List<BodyTableCell>`; adicionar `effectiveColumnCount()` que soma colspans
- [ ] Atualizar validação em `BodyTable`: usar `row.effectiveColumnCount()` em vez de `row.cells().size()`
- [ ] Atualizar `Docx4jWriter`: emitir `<w:gridSpan>` quando `colspan > 1`, `<w:vMerge w:val="restart"/>` quando `rowspanStart`, `<w:vMerge/>` (sem val) quando `rowspanContinuation`
- [ ] Criar `BodyTableCellRequest`; atualizar `BodyTableRowRequest` para `List<BodyTableCellRequest>`
- [ ] Migrar samples de tabelas existentes para o novo formato de célula
- [ ] Criar sample `body-content-table-merged.json` com colspan e rowspan
- [ ] Escrever `MergedTableRenderingTest`: verificar `gridSpan` e `vMerge` no XML gerado

**Arquivos:** `BodyTableCell.java` (novo), `BodyTableRow.java`, `BodyTable.java`, `Docx4jWriter.java`, `BodyTableCellRequest.java` (novo), `BodyTableRowRequest.java`, samples

---

### A-13 — Suite final do Bloco A

- [ ] `mvn compile -q && mvn test -q` — todos os testes passam
- [ ] Validação visual no Word/LibreOffice: cada bug corrigido verificado visualmente
- [ ] Commit do bloco

---

## Bloco B — Phase 0: DisplayObjectCollector

**Goal:** Extrair a pré-passagem de análise do `BodyContentRenderer` para um `DisplayObjectCollector` standalone. Resolver P1 (listas pré-textuais vazias e TOC sem headings) e implementar P11 corretamente (`numberingStrategy` declarado no profile).

**Pré-requisito:** Bloco A concluído.

---

### B-1 — Adicionar numberingStrategy nas rules de display objects

Conforme a taxonomia: cada rule de display object declara `numberingStrategy`, `label` e `separator`. O `DisplayObjectCollector` lê esses campos para construir o índice corretamente.

- [ ] Adicionar em `FigureRule`: `NumberingStrategy numberingStrategy`, `String label`, `String separator`
- [ ] Adicionar o mesmo em `TableRule`, `FrameRule`, `CodeListingRule`, `ChartRule`
- [ ] Criar enum `NumberingStrategy { GLOBAL_SEQUENTIAL, BY_CHAPTER }`
- [ ] Atualizar os records com validação no compact constructor: se `numberingStrategy` é não-nulo, `label` não pode ser blank
- [ ] Atualizar `FigureRuleRequest`, `TableRuleRequest`, `FrameRuleRequest`, `CodeListingRuleRequest`, `ChartRuleRequest` com os novos campos
- [ ] Atualizar `abnt-unip-profile.json`: para cada tipo, declarar `numberingStrategy: "GLOBAL_SEQUENTIAL"`, `label: "Figura"/"Tabela"/etc.`, `separator: "."` (usado apenas em `BY_CHAPTER`)

**Arquivos:** `FigureRule.java`, `TableRule.java`, `FrameRule.java`, `CodeListingRule.java`, `ChartRule.java`, `NumberingStrategy.java` (novo), requests correspondentes, `abnt-unip-profile.json`

---

### B-2 — Criar DisplayObjectCollector

Extração da lógica de pré-passagem que hoje vive dentro de `BodyContentRenderer.buildCrossReferenceIndex()` e `DisplayObjectRenderingState`.

- [ ] Criar `DisplayObjectCollector` em `rendering/phase0/`
- [ ] O collector recebe `List<DocumentComponent>` (todos os componentes selecionados) e `DocumentProfile`
- [ ] Varre todos os componentes que possuem display objects (atualmente só `BodyContentComponent`, mas a interface deve aceitar qualquer componente)
- [ ] Constrói:
  - Índice de cross-references: `Map<String, String>` de `targetId → texto resolvido` (ex: `"fig-form" → "Figura 1"` ou `"Figura 1.2"` se `BY_CHAPTER`)
  - Metadados de figuras, tabelas, frames, gráficos, listagens (para os engines INDEX)
  - Hierarquia de seções com títulos e níveis (para o `tableOfContents`)
  - Numeração de notas de rodapé
- [ ] O resultado é um `Phase0Index` record: `crossReferenceIndex`, `figuresMeta`, `tablesMeta`, `framesMeta`, `chartsMeta`, `codeListingsMeta`, `sectionsMeta`, `footnotesMeta`
- [ ] Usar `numberingStrategy` + `label` + `separator` das rules para calcular numeração corretamente:
  - `GLOBAL_SEQUENTIAL`: contador único por tipo no documento inteiro
  - `BY_CHAPTER`: contador reinicia por nível 1, número gerado como `{chapter}.{count}` com `separator`

**Arquivos:** `DisplayObjectCollector.java` (novo), `Phase0Index.java` (novo), em pacote `rendering/phase0/`

---

### B-3 — Remover buildCrossReferenceIndex do BodyContentRenderer

O `BodyContentRenderer` atual faz a própria pré-passagem internamente. Após o Bloco B, ele recebe o `Phase0Index` pronto.

- [ ] Remover método `buildCrossReferenceIndex()` do `BodyContentRenderer`
- [ ] Remover `CrossReferenceIndex` (record interno) do `BodyContentRenderer` — substituído por `Phase0Index`
- [ ] Atualizar `renderWithMetadata()` para receber `Phase0Index` como parâmetro
- [ ] Atualizar `toDocxRun()`: usar `Phase0Index.crossReferenceIndex()` para resolver cross-refs
- [ ] Atualizar `MetadataEmittingRenderer` se a assinatura do método mudar

**Arquivos:** `BodyContentRenderer.java`, `MetadataEmittingRenderer.java`

---

### B-4 — Atualizar DocumentRenderer com pré-passo Phase 0

- [ ] Injetar `DisplayObjectCollector` no `DocumentRenderer`
- [ ] No início de `render()`, antes do loop de componentes: chamar `displayObjectCollector.collect(selectedComponents, profile)` → `Phase0Index`
- [ ] Passar o `Phase0Index` para os renderers que o consomem:
  - `BodyContentRenderer` via `renderWithMetadata(component, profile, phase0Index)`
  - `MetadataConsumingRenderer`s (listas pré-textuais, sumário) recebem dados de `phase0Index` diretamente em vez de `BodyContentMetadata`
- [ ] Remover o padrão de cache/pre-render de BodyContent (o P1 original do 5.5 não foi implementado — nada a desfazer)

> **Resultado:** `ListOfFiguresRenderer`, `ListOfTablesRenderer`, etc. passam a ter dados disponíveis independentemente da ordem de renderização na `componentOrder`. TOC passa a ter hierarquia de seções para indexar.

**Arquivos:** `DocumentRenderer.java`, `DisplayObjectCollector.java`, renderers de listas pré-textuais, `SummaryRenderer.java`

---

### B-5 — Atualizar MetadataConsumingRenderer

Com o `Phase0Index` disponível globalmente, os `MetadataConsumingRenderer`s não precisam mais esperar pelo `BodyContentMetadata` emitido durante o loop.

- [ ] Revisar a interface `MetadataConsumingRenderer`: substituir dependência de `BodyContentMetadata` por `Phase0Index`
- [ ] Atualizar `AbstractIndexListRenderer` e todos os `ListOf*Renderer`s para usar `Phase0Index`
- [ ] Atualizar `SummaryRenderer` para usar `phase0Index.sectionsMeta()` em vez de `bodyContentMetadata`

**Arquivos:** `MetadataConsumingRenderer.java`, `AbstractIndexListRenderer.java`, todos os `ListOf*Renderer.java`, `SummaryRenderer.java`

---

### B-6 — Testes e validação do Bloco B

- [ ] Escrever `DisplayObjectCollectorTest`: verificar numeração `GLOBAL_SEQUENTIAL` e `BY_CHAPTER` com `label` e `separator` corretos
- [ ] Escrever `CrossReferenceIndexTest`: verificar que `targetId` resolve para o texto correto após coleta
- [ ] Teste de integração: documento com `listOfFigures` no pré-textual e figuras no `bodyContent` — verificar que a lista não está vazia
- [ ] `mvn compile -q && mvn test -q`
- [ ] Validação visual: lista de figuras populada, TOC com seções indexadas
- [ ] Commit do bloco

---

## Bloco C — FlowLayoutEngine

**Goal:** Construir o engine genérico `FLOW`. Extrair a lógica central do `BodyContentRenderer` para o engine. Migrar os componentes de fluxo contínuo para usar o engine diretamente. Implementar o modo `collection` com heading por segmentos — resolvendo Apêndice e Anexo sem workaround.

**Pré-requisito:** Bloco B concluído.

---

### C-1 — TextTypeRegistry e RunProcessor

Infraestrutura de despacho por tipo de nó.

- [ ] Criar interface `BlockTypeProcessor<T>` em `rendering/flow/`: `List<DocxBlock> process(T block, FlowRenderingContext context)`
- [ ] Criar `TextTypeRegistry`: mapa de `Class<? extends BodyBlock> → BlockTypeProcessor<?>`. Registra processadores para `BodyParagraph`, `BodyFigure`, `BodyTable`, `BodyLongQuote`, `BodyCodeListing`, `BodyList`, `BodyFrame`, `BodyChart`, `BodyEquation`
- [ ] Criar `RunProcessor`: processa `List<BodyInline>` e retorna `List<DocxRun>`. Distingue os três tipos:
  - `BodyText` → `DocxRun` com formatação inline
  - `BodyCrossReference` → resolve via `Phase0Index.crossReferenceIndex()`
  - `BodyFootnote` → `DocxFootnoteReferenceBlock` + acumula conteúdo em `FlowRenderingContext`
- [ ] Criar `FlowRenderingContext`: objeto mutável passado durante toda a renderização de um componente FLOW. Contém: `Phase0Index`, `DocumentProfile`, `FlowRule`, estado de numeração de seções, acumulador de footnotes

**Arquivos:** Todos em `rendering/flow/` (novo pacote)

---

### C-2 — FlowLayoutEngine

O engine genérico que substitui a lógica central do `BodyContentRenderer`. Não conhece ABNT, TCC, ou qualquer estrutura acadêmica. Processa uma árvore de seções e delega elementos folha ao `TextTypeRegistry`.

- [ ] Criar `FlowLayoutEngine` em `rendering/flow/`
- [ ] Assinatura: `List<DocxBlock> render(FlowInput input, FlowRenderingContext context)`
- [ ] `FlowInput` contém: `List<BodySection> sections` (ou `List<Object>` para suportar coleções heterogêneas)
- [ ] O engine processa recursivamente: para cada seção, emite heading (usando regras do nível declaradas no `FlowRule`) + quebra de página se `pageBreakBefore: true` + filhos recursivos
- [ ] Para elementos folha, despacha ao `TextTypeRegistry`
- [ ] Numeração de seções: lê `numberingStyle` por nível do `FlowRule` (`NUMERIC_DOT`, `ALPHA_NUMERIC`, `NONE`)
- [ ] Heading: tipo determinado por `titleSource` do nível (`PROFILE`, `CONTENT`, `HYBRID`)
  - `PROFILE`: título fixo declarado no profile
  - `CONTENT`: título vem do `BodySection.title()`
  - `HYBRID`: combina segmentos com `source` e `styleId`

**Arquivos:** `FlowLayoutEngine.java`, `FlowInput.java`, `FlowRule.java` (todos em `rendering/flow/`)

---

### C-3 — BodyContentRenderer delega ao FlowLayoutEngine

O `BodyContentRenderer` vira um wrapper fino. Mantém a interface existente para não quebrar o sistema.

- [ ] Mover lógica central de renderização de seções/blocos do `BodyContentRenderer` para `FlowLayoutEngine`
- [ ] `BodyContentRenderer.renderWithMetadata()` passa a: resolver rule, construir `FlowRenderingContext`, chamar `FlowLayoutEngine.render()`, empacotar resultado em `BodyContentRenderResult`
- [ ] Verificar que todos os testes existentes de `BodyContentRenderer` continuam passando

**Arquivos:** `BodyContentRenderer.java`, `FlowLayoutEngine.java`

---

### C-4 — Prova de conceito: ResumoRenderer migra para FLOW

O `Resumo` é o candidato ideal: FLOW singular, sem cross-refs, sem display objects, sem collection.

- [ ] Criar `FlowRule` para o resumo no profile (declarando regras por tipo de elemento: paragraph → `body.paragraph`, list → `body.keywords` com `display: inline`, `prefixLabel: "Palavras-chave:"`, `itemSeparator: ". "`)
- [ ] Reescrever `ResumoRenderer` para chamar `FlowLayoutEngine` diretamente com os dados do `ResumoComponent`
- [ ] Remover lógica de rendering manual de `ResumoRenderer`
- [ ] Verificar resultado visual: o documento gerado deve ser idêntico ao anterior

**Arquivos:** `ResumoRenderer.java`, `abnt-unip-profile.json` (seção resumo com flowRules)

---

### C-5 — Migração em batch dos componentes FLOW simples

Com a prova de conceito validada, migrar os demais componentes FLOW singular.

- [ ] `SummaryRenderer` (abstract multilíngue — já refatorado em A-10)
- [ ] `AcknowledgmentsRenderer`
- [ ] `DedicationRenderer`
- [ ] `ReferencesRenderer`
- [ ] `GlossaryRenderer`

Para cada um:
- [ ] Declarar `flowRules` no profile para o componente
- [ ] Reescrever o renderer para delegar ao `FlowLayoutEngine`
- [ ] Verificar que os testes existentes passam

**Arquivos:** Renderers listados + `abnt-unip-profile.json`

---

### C-6 — Apêndice e Anexo via collection (substitui F6 Task 3)

Esta é a implementação correta do que a Fase 6 Task 3 tentava resolver com um workaround. O modo `collection` do `FlowLayoutEngine` processa um array de itens independentes, cada um com heading gerado por segmentos.

- [ ] Implementar modo `collection: true` no `FlowLayoutEngine`:
  - Itera sobre o array de itens
  - Para cada item, constrói o heading combinando segmentos declarados no `itemHeading` do profile: `PREFIX` (literal do profile) + `SEQUENCE` (gerado pelo motor, estilo declarado por `sequenceStyle`) + `LITERAL` (separador do profile) + `CONTENT` (título do item)
  - Aplica `pageBreakBefore: true` antes de cada item quando declarado
  - Processa `children` do item como fluxo recursivo com contexto de numeração `ALPHA_NUMERIC` ou `NUMERIC_DOT`
- [ ] Declarar o componente `appendix` no profile com `collection: true`, `itemHeading` com segmentos, `flowRules` com numeração `ALPHA_NUMERIC`
- [ ] Reescrever `AppendixRenderer` para chamar `FlowLayoutEngine` no modo collection
- [ ] Reescrever `AnnexRenderer` com o mesmo padrão
- [ ] Verificar resultado: seções do apêndice A numeradas "A 1", "A 1.1", "B 1", "B 2" etc.

**Arquivos:** `FlowLayoutEngine.java`, `AppendixRenderer.java`, `AnnexRenderer.java`, `AppendixComponent.java` (verificar se precisa de ajuste), `abnt-unip-profile.json`

---

### C-7 — Profile ganha campo engine e flowRules

Os componentes FLOW agora declaram explicitamente seu engine.

- [ ] Adicionar campo `engine` em `ComponentRule` (enum: `SINGLE_PAGE`, `FLOW`, `INDEX`)
- [ ] Adicionar `flowRules: FlowRule` em `ComponentRule` para componentes FLOW
- [ ] Atualizar `abnt-unip-profile.json` com `"engine": "FLOW"` em todos os componentes migrados
- [ ] `ComponentRuleResolver` valida que o engine declarado é compatível com o renderer registrado

**Arquivos:** `ComponentRule.java`, `FlowRule.java`, `ComponentRuleResolver.java`, `abnt-unip-profile.json`

---

### C-8 — Testes e validação do Bloco C

- [ ] `FlowLayoutEngineTest`: singular + collection, cada tipo de nó, heading PROFILE/CONTENT/HYBRID
- [ ] `RunProcessorTest`: text, cross-ref resolvido, footnote acumulado
- [ ] `TextTypeRegistryTest`: despacho correto por tipo de bloco
- [ ] Testes de integração para cada renderer migrado
- [ ] `mvn compile -q && mvn test -q`
- [ ] Validação visual completa do documento
- [ ] Commit do bloco

---

## Bloco D — Engine INDEX Standalone + Profile Declarativo

**Goal:** Extrair a lógica de geração de listas indexadas para um engine `INDEX` standalone. Migrar os `ListOf*Renderers` e o `SummaryRenderer` (TOC) para usar o engine. Generalizar `contentBindings` substituindo assemblers hardcoded das single-page components.

**Pré-requisito:** Bloco C concluído.

---

### D-1 — Engine INDEX standalone

A lógica hoje está fragmentada em `AbstractIndexListRenderer` (7 subclasses concretas) e em `SummaryRenderer`.

- [ ] Criar `IndexLayoutEngine` em `rendering/index/`
- [ ] Assinatura: `List<DocxBlock> render(IndexInput input, IndexRule rule, StyleRule entryStyle)`
- [ ] `IndexInput` contém: `List<IndexEntry>` — cada entrada tem `label` (texto), `pageNumberPlaceholder` (para TOC) ou número já resolvido (para listas de figuras etc.)
- [ ] `IndexRule` declarado no profile: `source` (qual índice da Phase 0 alimenta), `entryStyleId`, `leaderStyle`, `pageNumberPlaceholder`
- [ ] Para TOC: placeholder substituído na Phase 3. Para listas: número já existe no `Phase0Index`

**Arquivos:** `IndexLayoutEngine.java`, `IndexInput.java`, `IndexEntry.java`, `IndexRule.java` (todos em `rendering/index/`)

---

### D-2 — Migrar ListOf*Renderers para INDEX

- [ ] Reescrever `AbstractIndexListRenderer` para chamar `IndexLayoutEngine`
- [ ] Verificar que `ListOfFiguresRenderer`, `ListOfTablesRenderer`, `ListOfChartsRenderer`, `ListOfCodeListingsRenderer`, `ListOfFramesRenderer`, `ListOfAbbreviationsRenderer`, `ListOfSymbolsRenderer` continuam funcionando com a nova base
- [ ] Declarar `"engine": "INDEX"` no profile para esses componentes

**Arquivos:** `AbstractIndexListRenderer.java`, renderers de lista, `abnt-unip-profile.json`

---

### D-3 — Migrar SummaryRenderer para INDEX

- [ ] `SummaryRenderer` passa a chamar `IndexLayoutEngine` com `source: SECTIONS` e `pageNumberPlaceholder` declarado no profile
- [ ] A hierarquia de seções vem de `Phase0Index.sectionsMeta()`
- [ ] Declarar `"engine": "INDEX"` no profile para o sumário

**Arquivos:** `SummaryRenderer.java`, `abnt-unip-profile.json`

---

### D-4 — contentBindings genérico para single-page components

Os assemblers de Cover, TitlePage e ApprovalSheet mapeiam hardcoded `"title" → cover.title()`, etc. O profile deveria declarar esses bindings.

- [ ] Definir `contentBindings` no profile para `CoverComponentRule`, `TitlePageComponentRule`, `ApprovalSheetComponentRule`: mapa de `slotId → content path`
- [ ] Criar `ContentBindingResolver`: lê os bindings do profile e busca valores no content por chave
- [ ] Substituir os assemblers hardcoded por `ContentBindingResolver`
- [ ] Remover classes de assembler específicas que se tornarem obsoletas

**Arquivos:** `ContentBindingResolver.java` (novo), `CoverComponentRule.java`, `TitlePageComponentRule.java`, `ApprovalSheetComponentRule.java`, assemblers correspondentes

---

### D-5 — Testes e validação do Bloco D

- [ ] `IndexLayoutEngineTest`: listas com entradas numeradas, TOC com placeholder
- [ ] Teste de integração: documento completo com todos os engines (SINGLE_PAGE, FLOW, INDEX)
- [ ] `mvn compile -q && mvn test -q`
- [ ] Validação visual: sumário com pontilhado e indentação por nível, listas indexadas completas
- [ ] Commit do bloco

---

## Bloco E — Pós-processamento UNO (Fase 7)

**Goal:** Estender o pós-processador LibreOffice para aplicar correções que só são possíveis após o layout tipográfico completo: rótulos continua/continuação em tabelas longas, detecção de títulos órfãos, verificação de integridade, geração de PDF.

**Pré-requisito:** Bloco D concluído. LibreOffice instalado e testado no ambiente de deploy (Fase 5 Task 7 validada).

**Status:** Bloco de conceito. Requer decisões arquiteturais (D1–D4 abaixo) antes de iniciar implementação.

---

### Decisões a tomar antes de implementar o Bloco E

**D1 — Interface de pós-processamento:**
A interface atual `DocxPostProcessor` recebe apenas `byte[] docxBytes`. O pós-processador precisa do `DocumentProfile` para ler labels de continua/continuação etc.

```java
// Interface expandida candidata:
public interface DocxPostProcessor {
    byte[] process(byte[] docxBytes, DocumentProfile profile);
}
```

- [ ] Decidir e atualizar `DocxPostProcessor`, `NoOpDocxPostProcessor` e `LibreOfficeDocxPostProcessor`

**D2 — Profile ganha seção postProcessing:**
```json
"postProcessing": {
  "tableContinuationLabels": {
    "enabled": true,
    "continuesLabel": "continua",
    "continuationLabel": "continuação",
    "conclusionLabel": "conclusão",
    "labelStyleId": "table.continuation"
  },
  "orphanTitleCorrection": { "enabled": true }
}
```

- [ ] Modelar `PostProcessingRule` em `profile/model/`
- [ ] Adicionar em `DocumentProfile`
- [ ] Atualizar `abnt-unip-profile.json`

**D3 — Mecanismo UNO:**
- Opção A: Macro LibreOffice Basic embarcada (sem dependência de Python, API mais limitada)
- Opção B: Python-UNO como processo filho via `ProcessBuilder` (API mais rica, adiciona Python como dependência)

- [ ] Decidir mecanismo antes de implementar qualquer case de uso

**D4 — Modelo de resposta para avisos de integridade:**
- Header HTTP `X-Formatter-Warnings`: não quebra contrato existente
- Envelope JSON com base64 do DOCX + lista de avisos: quebra o contrato atual

- [ ] Decidir antes de implementar verificação de integridade

---

### E-1 — Rótulos continua/continuação em tabelas longas

Implementar apenas após as decisões D1–D3.

- [ ] Detectar via UNO quais linhas de tabela ficam na última posição antes de uma quebra de página
- [ ] Inserir linha com rótulo "continua" antes da quebra
- [ ] Inserir linha com rótulo "continuação" no início das páginas intermediárias
- [ ] Inserir linha com rótulo "conclusão" no início da última página da tabela
- [ ] Passagem única (MVP) — não iterar até estabilizar

---

### E-2 — Detecção de títulos órfãos

- [ ] Via UNO, comparar página do heading com página do primeiro parágrafo filho
- [ ] Quando título está isolado no final da página: inserir `<w:pageBreakBefore>` antes do título
- [ ] Apenas para headings de nível 1 (MVP) — estender aos demais níveis se necessário

---

### E-3 — Verificação de integridade (sem modificação)

- [ ] Verificar se alguma figura extrapolou a margem da página
- [ ] Verificar se o número de páginas está dentro do limite declarado no profile
- [ ] Verificar se alguma fonte foi substituída pelo LibreOffice
- [ ] Retornar avisos via mecanismo decidido em D4

---

### E-4 — Geração de PDF

- [ ] Segunda chamada `--convert-to pdf` após resolver campos
- [ ] Retornar DOCX + PDF ou apenas PDF conforme parâmetro na request

---

### E-5 — Testes e validação do Bloco E

- [ ] Testes de integração com LibreOffice real (não mockado)
- [ ] Verificar empiricamente se `w:footnote` sobrevive às modificações de paginação da Phase 3
- [ ] `mvn test -q`
- [ ] Validação visual: tabelas longas com rótulos, sumário com números de página reais
- [ ] Commit do bloco

---

## Checklist de conclusão

| Bloco | Critério de done |
|---|---|
| A | Todos os bugs visuais corrigidos, modelo de domínio completo, suite verde |
| B | Listas pré-textuais populadas, TOC com headings, Phase0Index standalone, suite verde |
| C | FlowLayoutEngine funcional, todos os componentes FLOW migrados, apêndice com numeração correta, suite verde |
| D | Engine INDEX funcional, contentBindings genérico, nenhum assembler hardcoded restante, suite verde |
| E | Tabelas longas com rótulos, sumário com páginas reais, suite verde |

---

## Referências

- Taxonomia e modelo alvo: `docs/motor-generico-taxonomia.md`
- Guia arquitetural: `docs/guide/formatter-service-rules.md`
- Naming conventions: `docs/guide/naming-conventions.md`
