# Bugs visuais — full-document-abnt-complete

Problemas identificados ao gerar o documento com `full-document-abnt-complete.json` + `abnt-unip-profile.json`.
Ordem de resolução: 1 → 2 → 3 → 4 (pós-processamento por último).

---

## Bug 1 — Linha de assinatura abaixo das informações do membro

**Status:** resolvido

### Sintoma
Na folha de aprovação, cada membro aparece na ordem:
```
Prof. Nome Sobrenome
Universidade Fictícia de Limeira
Orientador
______________________________
```
O correto ABNT é linha de assinatura primeiro, depois as informações:
```
______________________________
Prof. Nome Sobrenome
Universidade Fictícia de Limeira
Orientador
```

### Origem
**Java** — `SinglePageLayoutAssembler.java`, método `resolveSignatureBlock` (linha ~215).

A lógica itera os `lineTemplates` (nome, instituição, papel) e só depois adiciona a `signatureLineText`. A ordem está hardcoded: templates → linha de assinatura.

```java
for (String lineTemplate : rule.lineTemplates()) { ... }
if (rule.signatureLineEnabled()) {
    lines.add(rule.signatureLineText()); // sempre ao final
}
```

### Correção esperada
Inverter a ordem: emitir a `signatureLineText` antes dos `lineTemplates` dentro de cada entrada.

---

## Bug 2 — Seções internas de apêndice e anexo mapeadas como Heading 1

**Status:** resolvido

### Sintoma
Dentro do apêndice A e do anexo A, os títulos de seções internas (ex: `"1 OBJETIVO DO INSTRUMENTO"`, `"2 QUESTÕES APLICADAS"`) são renderizados com estilo `HEADING_1`, fazendo o Google Docs e o Word tratá-los como títulos de nível 1 do documento — eles entram no sumário e recebem nível de outline errado.

### Origem
**Java** — `SectionedRenderer.java:48-62`.

O `SectionedRenderer` cria um `BodyContentRenderer` passando `bodyContentId = rule.bodyContentComponentId()`, que é `"bodyContent"`. O `BodyContentRenderer` resolve a regra pelo `component.componentId()` — que é `"bodyContent"` — e usa os estilos do bodyContent principal, incluindo `bodyContent.heading1` com `"type": "HEADING_1"`.

O campo `sectionTitleStyleIdsByLevel` que existe tanto na `SectionedComponentRule` quanto no JSON do profile (`appendix` e `annex`) é **declarado mas completamente ignorado** pelo renderer. O `BodyContentRenderer` nunca recebe esses estilos alternativos.

```java
// SectionedRenderer.java
String bodyContentId = rule.bodyContentComponentId(); // "bodyContent"
BodyContentRenderer contentRenderer = new BodyContentRenderer(bodyContentId);
// contentRenderer vai buscar bodyContent.heading1 (HEADING_1) — errado para apêndice
```

### Correção esperada
O `SectionedRenderer` precisa passar os `sectionTitleStyleIdsByLevel` da `SectionedComponentRule` ao renderer do conteúdo interno, de modo que os headings internos usem estilos com `"type": "PARAGRAPH"` em vez de `HEADING_*`. A estratégia exata a definir na hora da implementação (override de estilo, componentId alternativo no profile, etc.).

---

## Bug 3 — Listas de elementos sem líderes de pontos e número de página

**Status:** pendente

### Sintoma
As listas de ilustrações, quadros, gráficos e listagens aparecem com o formato simples:
```
1 — Distribuição dos tipos de bloco utilizados no documento de validação
```
O correto ABNT é entrada com líderes de pontos e número de página:
```
Figura 1 – Distribuição dos tipos de bloco ......... 23
```

### Estratégia
Cada elemento recebe um **bookmark** no parágrafo de legenda durante a renderização do body content. Na lista, cada entrada emite um novo bloco `DocxIndexEntryParagraph` que contém o texto da entrada, um tab com líder de pontos e um campo `PAGEREF <bookmarkName>`. O Word/LibreOffice resolve o número ao abrir/processar. A resolução dos campos `PAGEREF` pelo LibreOffice é implementada junto do sub-bug 4b.

### Plano de implementação

**Passo 1 — `BodyDisplayObjectMetadata`**
Adicionar campo `bookmarkName: String`. Derivado deterministicamente do `id` do elemento com prefixo fixo para evitar colisões (`"elem_" + id`). Atualizar todos os pontos de construção em `TextTypeRegistry` e `DisplayObjectCollector`.

**Passo 2 — `DocxBookmarkParagraph` (novo bloco de output)**
`engine/model/output/DocxBookmarkParagraph.java` — parágrafo que além do conteúdo normal carrega um `bookmarkName`. O writer envolve o parágrafo com `<w:bookmarkStart>` / `<w:bookmarkEnd>`.

**Passo 3 — `TextTypeRegistry`**
Nos métodos `renderFigure`, `renderTable`, `renderFrame`, `renderCodeListing`, `renderChart`: substituir o `DocxParagraph` do parágrafo de legenda por `DocxBookmarkParagraph`, passando o `bookmarkName` do `BodyDisplayObjectMetadata` correspondente. Apenas para a legenda da primeira parte (quando `part.continuationLabel().isEmpty()`).

**Passo 4 — `DocxIndexEntryParagraph` (novo bloco de output)**
`engine/model/output/DocxIndexEntryParagraph.java` — carrega: `entryText: String`, `bookmarkName: String`, `styleRule: StyleRule`, `contentWidthCm: double`. O writer emite: parágrafo com tab stop direito + líder de pontos na borda do conteúdo, run com o texto da entrada, run com `\t`, run com campo `PAGEREF <bookmarkName> \h`.

**Passo 5 — `Docx4jWriter`**
- `writeBlock`: adicionar casos `DocxBookmarkParagraph` e `DocxIndexEntryParagraph`
- `writeBookmarkParagraph`: escreve o parágrafo normalmente (delegando para `writeParagraph` com o conteúdo) e adiciona `<w:bookmarkStart w:id="N" w:name="bookmarkName"/>` antes do primeiro run e `<w:bookmarkEnd w:id="N"/>` após o último. IDs de bookmark devem ser únicos no documento — usar um contador interno no writer durante o `write()`.
- `writeIndexEntryParagraph`: emite parágrafo com `<w:tabs>` (tab stop RIGHT + DOT leader na borda direita), run de texto, run com `\t`, run com campo `PAGEREF bookmarkName \h \* MERGEFORMAT`.

**Passo 6 — `ElementIndexComponentRule`**
Adicionar campo `pageReferenceEnabled: boolean`. Remover a restrição que exige `{number}` e `{caption}` no `entryTemplate` quando `pageReferenceEnabled` é true (o template passa a ser apenas o prefixo antes do líder). Atualizar `ElementIndexComponentRuleRequest` e `ProfileDefinition` correspondentes.

**Passo 7 — `ElementIndexRenderer`**
Quando `rule.pageReferenceEnabled()`: substituir emissão de `DocxParagraph` por `DocxIndexEntryParagraph`, usando `rule.entryTemplate()` para o texto e `item.bookmarkName()` para a referência. Calcular `contentWidthCm` a partir de `profile.pageRule()`.

**Passo 8 — `abnt-unip-profile.json`**
Nos 5 componentes de lista (`listOfFigures`, `listOfTables`, `listOfFrames`, `listOfCharts`, `listOfCodeListings`):
- Adicionar `"pageReferenceEnabled": true`
- Atualizar `entryTemplate` para o formato com prefixo de tipo, ex:
  - `listOfFigures`: `"Figura {number} – {caption}"`
  - `listOfTables`: `"Tabela {number} – {caption}"`
  - `listOfFrames`: `"Quadro {number} – {caption}"`
  - `listOfCharts`: `"Gráfico {number} – {caption}"`
  - `listOfCodeListings`: `"Código-fonte {number} – {caption}"`

**Passo 9 — `docs/bugs-visuals-abnt-completo.md`**
Anotar no sub-bug 4b que a resolução de campos `PAGEREF` pelo LibreOffice deve ser feita junto com a atualização do TOC.

---

## Bug 4 — Listas de tabelas, listagens, gráficos, quadros e abreviaturas vazias / sumário em branco

**Status:** pendente

Dividido em dois sub-problemas com origens distintas.

### Sub-bug 4a — Listas de elementos e abreviaturas vazias (Phase0)

**Sintoma:** as listas de tabelas, listagens, gráficos, quadros e abreviaturas e siglas aparecem completamente vazias, mesmo o body content contendo tabelas, figuras, gráficos, listagens e abreviações.

**Origem:** a investigar em Java — `DisplayObjectCollector` e/ou pipeline de renderização.

O `DisplayObjectCollector.collect()` itera `command.documentComponents()` filtrando `instanceof BodyContentComponent`. O body content principal existe e deveria ser coletado. Hipóteses:

- O `componentId` do `BodyContentComponent` enviado não corresponde a uma chave que resolve para `BodyContentComponentRule`, causando falha silenciosa na coleta.
- A coleta funciona mas o `Phase0Index` não está sendo passado corretamente para os renderers das listas (`ElementIndexRenderer`, `FlowTextualRenderer`).
- O `DisplayObjectCollector` não está varrendo o conteúdo dos apêndices/anexos — `SectionedContent` não é `BodyContentComponent`, então figuras e tabelas dentro deles nunca entram no índice.

**Passo de diagnóstico:** adicionar log temporário após `displayObjectCollector.collect()` no `DocumentRenderer` para imprimir o conteúdo do `Phase0Index` (contagem de elementos por tipo, contagem de abreviações).

### Sub-bug 4b — Sumário em branco (pós-processamento)

**Sintoma:** o sumário aparece em branco.

**Origem:** pós-processamento — `LibreOfficeDocxPostProcessor`.

O `SectionIndexRenderer` emite corretamente um `DocxTocBlock` com instrução `TOC \o "1-6" \h \z \u`. O preenchimento do sumário depende do LibreOffice headless:

1. `resolveFieldsViaLibreOffice` converte o DOCX via `--convert-to docx`, o que deveria forçar atualização dos campos TOC.
2. O script UNO também tenta `doc.getIndexes()` e chama `idx.update()`.

Se o sumário está em branco no arquivo final, o LibreOffice não está atualizando o campo — seja porque não está instalado/acessível no ambiente, porque o processo falha silenciosamente e retorna o fallback, ou porque o `NoOpDocxPostProcessor` está sendo usado em vez do LibreOffice.

**Resolução:** implementar junto do bug 3 (pós-processamento). Além da atualização do TOC, o LibreOffice deve resolver os campos `PAGEREF` emitidos pelas listas de elementos (bug 3). As duas operações acontecem no mesmo passo do pós-processamento. Requer diagnóstico do ambiente (verificar qual `DocxPostProcessor` está ativo, se o LibreOffice está acessível, checar logs de warning do pós-processamento).
