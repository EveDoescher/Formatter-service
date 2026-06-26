# Plano de Correções 5.5 — Pós-Fase 5

**Data:** 2026-06-26
**Branch:** feature/elementos-textuais
**Contexto:** Após implementação completa da Fase 5, teste visual do documento gerado revelou os problemas abaixo. O manual ABNT-UNIP (`docs/manual_de_normalizacao_abnt.pdf`) foi lido integralmente para embasar as correções.

---

## Bugs identificados

### P1 — Listas pré-textuais e Abreviaturas vazias (crítico)

**Componentes afetados:** listOfFigures, listOfTables, listOfFrames, listOfCharts, listOfCodeListings, listOfAbbreviations, summary

**Causa raiz arquitetural:** Todos esses componentes são `MetadataConsumingRenderer`. O `bodyContent` é o único `MetadataEmittingRenderer` — ele coleta figuras, tabelas, quadros, gráficos, listagens e abreviaturas durante a renderização. Porém, na `componentOrder` do perfil, todas as listas aparecem **antes** do `bodyContent` (correto pela ABNT). No loop de `DocumentRenderer.render()`, quando as listas rodam, o `bodyContentMetadata` ainda é `BodyContentMetadata.empty()`.

**Correção:** Adicionar pré-passo em `DocumentRenderer.render()` antes do loop principal:
1. Varrer os componentes selecionados e encontrar o primeiro cujo renderer implementa `MetadataEmittingRenderer`
2. Renderizá-lo uma vez, guardar `BodyContentRenderResult` em cache (blocks + metadata)
3. No loop principal, quando chegar nesse componentId, usar o resultado cacheado sem re-renderizar

**Arquivo:** `rendering/orchestration/DocumentRenderer.java`

---

### P2 — Título duplicado em Apêndice e Anexo

**Causa raiz:** O `headingTemplate` do perfil já monta `"APÊNDICE {letter} — {title}"`. O campo `title` no JSON de teste foi preenchido com `"APÊNDICE A — Pseudocódigo Completo do Experimento"`, incluindo o prefixo. Resultado: `"APÊNDICE A — APÊNDICE A — Pseudocódigo Completo do Experimento"`.

O manual usa travessão simples `–` (U+2013) no template, mas o perfil usa travessão longo `—` (U+2014).

**Correção:**
- Alterar `title` no JSON de teste para conter apenas o título sem prefixo
- Corrigir separador nos templates do perfil de `—` para `–`

**Arquivos:** `teste-fase5-completo.json`, `abnt-unip-profile.json` (templates de appendix e annex)

---

### P3 — Ausência de linha em branco após títulos de seção

**Componentes afetados:** references, listOfFigures, listOfTables, listOfFrames, listOfCharts, listOfCodeListings, listOfAbbreviations, listOfSymbols, resumo, abstract, acknowledgments, glossary

**Causa raiz:** Todos os renderers emitem o parágrafo de heading e imediatamente o primeiro elemento de conteúdo, sem intervalo. O manual prescreve: *"Os títulos das seções e subtítulos devem começar na parte superior da margem esquerda da folha e separados do texto por um espaço de 1,5 cm entrelinhas."*

**Correção:**
- Adicionar campo `blankLinesAfterHeading: 1` em cada `ComponentRule` dos componentes afetados no perfil
- Cada renderer lê esse campo e emite `DocxBlankLine(headingStyle)` após o heading

**Arquivos:** Renderers afetados (listados acima) + seus `ComponentRule` records + `abnt-unip-profile.json`

---

### P4 — Label "Palavras-chave:" e "Keywords:" sem negrito

**Componentes afetados:** resumo, abstract

**Causa raiz:** O renderer concatena label + palavras em um único `DocxRun`. O estilo `resumo.keywords` tem `bold: false`. É impossível negritar apenas o label assim.

**Correção:** No `ResumoRenderer` e `AbstractRenderer`, emitir dois `DocxRun` separados no mesmo `DocxParagraph`:
- Primeiro run: label com `InlineFormatting` bold=true
- Segundo run: palavras sem bold

Não requer mudança no perfil — o negrito do label é regra tipográfica fixa da ABNT.

**Arquivos:** `rendering/component/resumo/ResumoRenderer.java`, `rendering/component/abstracten/AbstractRenderer.java`

---

### P5 — Keywords sem ponto final

**Componentes afetados:** resumo, abstract

**Causa raiz:** O renderer constrói a linha de keywords como `label + " " + join(keywords, separator)` sem terminator. O manual confirma o formato correto: `"Palavras-chave: Turismo; Mercado de trabalho; Empreendedorismo."` — ponto final obrigatório.

**Correção:**
- Adicionar campo `keywordsTerminator: "."` nos `ComponentRule` de resumo e abstract no perfil
- Renderers leem o campo e o anexam ao final da string de keywords

**Arquivos:** `ResumoRenderer.java`, `AbstractRenderer.java`, `ResumoComponentRule.java`, `AbstractComponentRule.java`, `abnt-unip-profile.json`

---

### P6 — Página em branco antes da Dedicatória

**Causa raiz:** `DedicationRenderer` emite `new DocxPageBreak()` como primeiro bloco. O `DocumentRenderer` já insere quebras de página entre componentes. Resultado: duas quebras consecutivas, gerando uma página em branco antes do conteúdo.

**Correção:** Remover `DocxPageBreak` do `DedicationRenderer`. Verificar se o mesmo padrão existe em `EpigraphRenderer` e `AcknowledgmentsRenderer` e corrigir onde necessário.

**Arquivos:** `rendering/component/dedication/DedicationRenderer.java` (+ verificar epigraph e acknowledgments)

---

### P7 — Errata em texto simples em vez de tabela

**Causa raiz:** `ErrataRenderer` emite parágrafos de texto com template `"Folha {page}, linha {line}: onde se lê {incorrect}, leia-se {correct}."`. O manual prescreve explicitamente uma tabela com colunas: **Folha | Linha | Onde se lê | Leia-se**.

**Correção:**
- Mudar `ErrataRenderer` para emitir `DocxTableBlock`
- Adicionar campo `tableHeaders: ["Folha", "Linha", "Onde se lê", "Leia-se"]` no `ErrataComponentRule`
- Adicionar estilos de cabeçalho e célula de tabela no perfil
- Renderer monta as linhas da tabela a partir das entradas, removendo o `entryTemplate` que deixa de ser necessário

**Arquivos:** `rendering/component/errata/ErrataRenderer.java`, `profile/model/component/errata/ErrataComponentRule.java`, `abnt-unip-profile.json`

---

### P8 — Duplo ponto nas referências (`3 ed..`)

**Causa raiz:** Em `ReferencesEntryFormatter.formatBook()`, o segmento de edição é montado como `" " + ed + " ed."` (com ponto final). A cidade é adicionada logo após como `city + ": "`. O formato correto pela ABNT é `"4. ed."` — o ponto pertence ao número de edição, e deve haver separação antes da cidade.

**Correção:** Ajustar o segmento de edição para `ed + ". ed."` e garantir que o separador antes da cidade seja `. ` e não um ponto duplicado. Revisar também o `formatChapterInBook()` onde o padrão é semelhante.

**Arquivo:** `rendering/component/references/ReferencesEntryFormatter.java`

---

### P9a — Sumário vazio (pós-processamento LibreOffice)

**Causa raiz (duas):**
1. O sumário é afetado pelo P1 — mesmo com TOC field emitido, o `SummaryRenderer` não tem metadata quando roda antes do `bodyContent`, então nenhuma seção é registrada como heading para o Word indexar
2. O `LibreOfficeDocxPostProcessor` pode não estar configurado no ambiente de desenvolvimento, retornando o DOCX original com o campo TOC não resolvido

**Correção:** Resolver P1 primeiro (os headings passarão a ser renderizados antes de o TOC ser populado). Depois verificar a propriedade `formatter.libreoffice.enabled` em `application.properties` e confirmar que o executável está acessível.

**Arquivos:** `DocumentRenderer.java` (via P1), `application.properties`

---

### P9b — TOC sem pontilhado e sem indentação por nível

**Causa raiz:** O campo `TOC \o "1-N" \h \z \u` depende dos estilos Word internos `TOC 1`, `TOC 2`, etc. para formatar as entradas (pontilhado via tab stop com líder `.`, indentação crescente por nível). O `Docx4jWriter` **não define esses estilos no documento** — o Word usa os defaults da instalação, que variam e geralmente não têm pontilhado.

**Correção:** Em `Docx4jWriter`, criar método `applyTocStyleDefinitions()` chamado durante a escrita do documento. Para cada nível TOC 1 a N (N = tamanho de `entryStyleIdsByLevel` do `SummaryComponentRule`), criar estilo Word com id `TOC1`/`TOC2`/etc. contendo:
- Fonte e tamanho do estilo correspondente no perfil (`entryStyleIdsByLevel[level-1]`)
- Indentação esquerda crescente por nível (nível 2 → 0.5cm, nível 3 → 1.0cm, etc.)
- Tab stop alinhado à direita na posição da área de texto (largura − margens) com líder de ponto (`.`)

Isso requer que o `DocxTocBlock` carregue os estilos de entrada por nível para que o writer possa criá-los. Adicionar campo `List<StyleRule> entryStylesByLevel` ao `DocxTocBlock`.

**Arquivos:** `output/docx/docx4j/Docx4jWriter.java`, `output/docx/api/DocxTocBlock.java`, `rendering/component/summary/SummaryRenderer.java`

---

## Inconsistências de perfil identificadas no manual

| Campo | Perfil atual | Manual ABNT-UNIP | Ação |
|---|---|---|---|
| Separador apêndice/anexo | `—` (U+2014) | `–` (U+2013) | Corrigir no perfil |
| Keywords terminator | ausente | `.` obrigatório | Adicionar via P5 |
| Linha após heading | ausente | 1 linha de 1,5 | Adicionar via P3 |
| Errata | parágrafo | tabela | Corrigir via P7 |

Tudo o mais no perfil está alinhado ao manual: margens, recuo, espaçamento, tamanhos de fonte, espaço simples em referências.

---

## Ordem de implementação

| Ordem | Item | Justificativa |
|---|---|---|
| 1 | P1 | Desbloqueador: resolve listas + sumário + parte do P9a |
| 2 | P2 + P8 | Triviais, sem dependência |
| 3 | P6 | Simples, melhora visual imediata |
| 4 | P4 + P5 | Juntos — mesmos arquivos |
| 5 | P3 | Oito renderers + perfil |
| 6 | P7 | Requer novo tipo de output (tabela) |
| 7 | P9b | Mais complexo — writer DOCX + novos estilos TOC |
| 8 | P10–P13 | Após os anteriores — comportamentos indefinidos e violações menores |

---

## Itens adicionais — extraídos da análise proposta vs. implementação

### P10 — Null-coerce silencioso em `BodyContentRequest.toDomain()`

**Causa raiz:** `sections == null ? List.of()` cria um caminho que viola o padrão fail-fast do projeto. O `@NotEmpty` na entrada já cobre o caso via validação Bean Validation, mas o código permite que `toDomain()` seja chamado com `null` sem lançar exceção.

**Correção:** Remover o branch `null`, deixar apenas `sections.stream()...`. Se `sections` for `null` ao chegar aqui, é bug do chamador — deve explodir cedo.

**Arquivo:** `api/export/dto/request/BodyContentRequest.java`

---

### P11 — Numeração de figuras e tabelas nunca declarada no perfil

**Causa raiz:** Figuras e tabelas são numeradas globalmente no documento (Figura 1, Figura 2... ao longo de todas as seções). Esse comportamento está hardcoded no `DisplayObjectRenderingState` — o perfil nunca declara se a numeração é global ou reinicia por seção. Isso viola o princípio central: qualquer decisão que possa variar entre perfis deve vir do perfil.

**Correção:**
- Adicionar campo `numberingScope: "GLOBAL" | "PER_SECTION"` em `FigureRule`, `TableRule`, `FrameRule`, `ChartRule`, `CodeListingRule` no perfil
- `BodyContentRenderer` passa o escopo para `DisplayObjectRenderingState`, que reinicia o contador ao mudar de seção quando `PER_SECTION`
- Perfil ABNT-UNIP declara `"GLOBAL"` (comportamento atual mantido)

**Arquivos:** `profile/model/component/bodycontent/FigureRule.java` (e análogos), `rendering/component/bodycontent/DisplayObjectRenderingState.java`, `abnt-unip-profile.json`

---

### P12 — `selectedComponents: []` com semântica indefinida

**Causa raiz:** O campo `options.selectedComponents` aceita lista vazia `[]`. O comportamento atual para lista vazia não está documentado nem coberto por teste — pode significar "renderizar todos" ou "renderizar nenhum" dependendo de como o `ComponentSelectionResolver` interpreta.

**Correção:**
- Verificar o comportamento atual em `ComponentSelectionResolver`
- Se `[]` significa "todos": documentar no guide e adicionar teste explícito
- Se `[]` significa "nenhum": adicionar validação que rejeita lista vazia com erro claro, já que documento sem componentes não faz sentido

**Arquivo:** `rendering/orchestration/ComponentSelectionResolver.java`

---

### P13 — Componente em `componentOrder` mas ausente no request: comportamento indefinido

**Causa raiz:** O perfil lista componentes em `componentOrder`. O request pode omitir um desses componentes. O comportamento atual (ignora silenciosamente vs. falha) não está documentado nem coberto por teste para todos os casos — em particular para componentes obrigatórios pela norma.

**Correção:**
- Verificar o comportamento atual no loop de `DocumentRenderer`
- Definir e documentar a política: componente ausente no request é sempre ignorado, ou depende de `validationMode`?
- Adicionar teste que cobre: componente em `componentOrder`, ausente no request, `STRICT` vs `FLEXIBLE`

**Arquivo:** `rendering/orchestration/DocumentRenderer.java`
