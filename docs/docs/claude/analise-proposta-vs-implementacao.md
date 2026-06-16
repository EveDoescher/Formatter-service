# Análise: Proposta vs. Implementação — branch `feature/elementos-textuais`

Gerado por análise de três agentes paralelos independentes.

---

## Parte 1 — Gaps de Implementação (proposta promete, código não entregou)

### ALTA severidade

**GAP-1 — Citações inline colapsadas em texto plano (sem runs diferenciados no DOCX)**
A proposta especifica que `PARAGRAPH.content[]` pode ter inline `CITATION` + `QUOTE_TEXT` + `TEXT` com formatação diferenciada por runs. Na prática, `BodyParagraph.text()` concatena tudo em uma `String` única, gerando um único `w:r` no DOCX. Qualquer perfil futuro que queira estilo de caractere diferente no marcador autor-data não tem como fazê-lo.
- _Arquivo_: `BodyParagraph.java:29–33`, `BodyContentRenderer.java:134–136`

**GAP-2 — `BodyCitation` como bloco aceita tipos que deveriam ser apenas inline**
A proposta é explícita: `DIRECT_SHORT`, `INDIRECT` e `CITATION_OF_CITATION` devem ser inline em parágrafos. `DIRECT_LONG` é o único block-level. O código aceita `DIRECT_SHORT_QUOTE` e `INDIRECT_CITATION` como `BodyBlockType`, renderizando-os como parágrafos isolados — formatação ABNT incorreta.
- _Arquivo_: `BodyBlock.java`, `BodyBlockType.java`, `BodyBlockRequest.java:29–33`, `BodyContentRenderer.java:138–141`

---

### MÉDIA severidade

**GAP-3 — Ausência de `InvalidBodyContentException` dedicada**
Todos os outros componentes têm exceção semântica própria (`InvalidCoverContentException`, `InvalidApprovalSheetContentException`, etc.). O `bodyContent` lança `IllegalArgumentException` diretamente dos records — o handler não consegue diferenciar erros de bodyContent de outros contextos.
- _Arquivo_: `/shared/exception/` (classe inexistente)

**GAP-4 — Ausência de `BodyContentRendererDocxSanityTest`**
O checklist do roadmap exige teste que inspecione o XML interno do DOCX gerado. Existem testes unitários e de integração, mas nenhum que abra o `.docx` e valide `w:pStyle`, `w:lineRule`, heading styles, etc.
- _Arquivo_: `/test/.../bodycontent/` (classe inexistente)

**GAP-5 — Heading styles sem `<w:basedOn>` no writer**
A proposta exige que os estilos de heading no Word sejam completamente definidos pelo perfil, sem herdar defaults. `Docx4jWriter.createHeadingStyle()` não declara `<w:basedOn>`, o que pode fazer o Word herdar cor azul, espaçamento padrão ou outros defaults do tema em ambientes diferentes.
- _Arquivo_: `Docx4jWriter.java:429–443`

---

### BAIXA severidade

**GAP-8 — Numeração de seção frágil ao auto-iniciar contador de nível anterior zerado**
`SectionNumberingState.increment()` força `counters[0] = 1` quando o nível 1 nunca foi incrementado. A validação de hierarquia mitiga a maioria dos casos, mas seções que começam direto em nível 2 (com título vazio no nível 1) podem ter comportamento inesperado.
- _Arquivo_: `BodyContentRenderer.java:474–487`

**GAP-9 — `body-content-figures-url-visual.json` excluído do teste automático de samples**
O sample de figura com URL existe na pasta mas está fora da lista do `BodyContentSampleValidationTest`. Pode ficar desatualizado sem detecção.
- _Arquivo_: `BodyContentSampleValidationTest.java:32–37`

**GAP-10 — `previousRenderedTextWasBodyParagraph = true` após figura/tabela**
A proposta define `blankLinesBeforeSectionTitleWhenPrecededByContent` como espaçamento após parágrafo textual. O código ativa a flag para qualquer bloco (incluindo figuras e tabelas), inserindo blank line extra antes de títulos nesses casos.
- _Arquivo_: `BodyContentRenderer.java:109`

---

## Parte 2 — Desvios de Design (código diverge da proposta)

### Arquiteturais

**DESVIO-1 — Dois caminhos para o mesmo tipo de citação (bloco vs inline)**
`BodyBlock` aceita `BodyCitation` com tipos `DIRECT_SHORT`, `INDIRECT`, `CITATION_OF_CITATION` como blocos autônomos, paralelamente à rota inline correta via `BodyCitationCall` dentro de `BodyParagraph`. Dois contratos para o mesmo comportamento. O caminho de bloco produz formatação ABNT incorreta (parágrafo isolado).
- _Arquivo_: `BodyBlock.java`, `BodyBlockRequest.java:29–33`

**DESVIO-2 — `"p. "`, `"et al."`, `"; "`, `" apud "` hardcoded no domínio**
A proposta é taxativa: sem hardcode de valores acadêmicos, e o serviço deve suportar múltiplos perfis. Esses tokens variam por norma (ABNT vs APA vs Chicago) e por idioma. Estão fixos em `CitationSource.java` e `BodyCitationCall.java` em vez de virem do perfil.
- _Arquivo_: `CitationSource.java:39–49`, `BodyCitationCall.java:47,55`

---

### Funcionais

**DESVIO-3 — Flag `previousRenderedTextWasBodyParagraph` com nome e comportamento incorretos**
O nome diz "body paragraph" mas a flag é ativada por qualquer `BodyBlock` (figuras, tabelas, citações). Resultado: blank line extra inserida antes de títulos quando o bloco anterior foi uma figura ou tabela — viola a semântica do campo de perfil.
- _Arquivo_: `BodyContentRenderer.java:72,84,109`

**DESVIO-5 — `FigureRuleRequest` e `TableRuleRequest` sem anotações `@NotNull`**
A proposta exige que perfil incompleto falhe cedo com mensagem clara. Os campos de figura/tabela nos requests de perfil inline não têm `@NotNull`/`@NotBlank` — a validação só ocorre dentro do domínio, sem identificar o campo JSON de origem no erro.
- _Arquivo_: `FigureRuleRequest.java`, `TableRuleRequest.java`

**DESVIO-7 — `DIRECT_SHORT` bloco renderiza string pré-montada com aspas como parágrafo isolado**
Quando enviado como bloco (Desvio-1), `BodyCitation.renderedText()` monta o texto com aspas e a call autor-data e joga tudo como string única num `DocxParagraph`. Resultado no DOCX: citação direta curta aparece como parágrafo isolado com aspas, não inline no texto.
- _Arquivo_: `BodyContentRenderer.java:138–141`, `BodyCitation.java`

---

### Cosméticos / Minor

**DESVIO-6 — `BodyContentRequest.toDomain()` tem null-coerce silencioso para `List.of()`**
`sections == null ? List.of()` viola o padrão de fail-fast. A anotação `@NotEmpty` já cobre o caso, mas o código cria um caminho alternativo diferente da especificação.
- _Arquivo_: `BodyContentRequest.java:15`

**DESVIO-9 — `BodyCitation` constrói `BodyQuoteText` descartável como side-effect de validação**
```java
if (type == BodyCitationType.DIRECT_SHORT) {
    new BodyQuoteText(BodyQuoteType.SHORT, text); // objeto criado só para validar
}
```
Anti-padrão: usar construção de objeto como validação side-effect.
- _Arquivo_: `BodyCitation.java:25–27`

**DESVIO-10 — `BodyContentComponentRule` não tem `contentBindings`, quebrando consistência de `ComponentRule`**
Todos os outros `ComponentRule` têm `contentBindings()`. `BodyContentComponentRule` não implementa esse campo — o `bodyContent` genuinamente não usa `work`, mas a assimetria não está documentada.
- _Arquivo_: `BodyContentComponentRule.java`

---

## Parte 3 — Melhorias na Proposta (classificadas)

### Ambiguidades

| ID | Documento | Problema | Sugestão |
|----|-----------|----------|----------|
| A1 | `rules_ai_guide.md` §16 | "Componente presente mas vazio" — gera página em branco, é ignorado, ou falha em STRICT? | Documentar os três estados com exemplos de request e comportamento por modo de validação |
| A2 | `rules_ai_guide.md` §16-17 | Validation mode parece ser por componente (§16) e por geração (§17) ao mesmo tempo | Separar o que é atual (modo global) do que é futuro (modo por componente) |
| A3 | `roadmap.md` | Se componente está em `componentOrder` mas ausente no request: ignora silencioso ou falha? | Documentar as quatro combinações presente/ausente em componentOrder × request |
| A4 | `roadmap.md` + `single-page-reference.md` | Distinção "componente interno" vs "componente acadêmico" não é formal nem observável no perfil | Adicionar propriedade no perfil ou documentar o mecanismo de fallback para internos |
| A5 | `work-data-and-bindings.md` | Quando `contentBindings` é validado? Na carga do perfil, no início da geração, ou no uso? Campo opcional ausente = null ou erro? | Descrever o ciclo de vida da validação com exemplos de campo opcional vs obrigatório |
| A6 | `title-page-component.md` | `FROM_PAGE_CENTER_TO_RIGHT_MARGIN` — centro da área útil com margens assimétricas | Adicionar fórmula: `center = leftMargin + (pageWidth - leftMargin - rightMargin) / 2` |

### Lacunas

| ID | Documento | Problema | Sugestão |
|----|-----------|----------|----------|
| L1 | `work-data-and-bindings.md` | String vazia `""` em override: vence (omite campo) ou é tratado como ausente? | Documentar comportamento de `null` vs `""` vs campo ausente em cada contexto |
| L2 | `rules_ai_guide.md` §25,31 | Nenhuma especificação de códigos HTTP por categoria de erro nem shape do response de erro | Adicionar seção de contrato HTTP com exemplos de body de erro |
| L3 | `body-content-component.md` | Numeração de figuras/tabelas: global no documento ou por seção? O perfil controla? | Adicionar `bodyContent.figure.numberingStrategy` e `bodyContent.table.numberingStrategy` |
| L4 | `roadmap.md` | `TABLE` aparece em `body-content-component.md` mas não no roadmap nem no checklist de escopo atual | Sincronizar lista de blocos no roadmap para incluir `TABLE` |
| L5 | `roadmap.md` §numeração | O que acontece se `countFromComponentId` aponta para componente não presente no request? | Documentar política de resolução quando o componente referenciado está ausente |
| L6 | `work-data-and-bindings.md` | Onde exatamente `contentBindings` fica no JSON do perfil em relação a `groups`, `gapRules`, `styleMapping`? | Mostrar fragmento completo de um componentRule com contentBindings |
| L7 | `rules_ai_guide.md` §27 | `selectedComponents: []` — significa "todos" ou "nenhum"? Como interage com `document`? | Documentar semântica de lista vazia vs null vs lista explícita |

### Contradições

| ID | Documentos | Contradição |
|----|-----------|-------------|
| C1 | `rules_ai_guide.md` §19 vs `roadmap.md` §blankLinesAfter | §19 condena "skip N lines" como hardcode disfarçado de JSON, mas `blankLinesAfter` é exatamente isso para micro-espaçamentos |
| C2 | `body-content-component.md` vs `roadmap.md` | `section.title` é opcional no doc do componente, mas o roadmap não menciona opcionalidade |
| C3 | `roadmap.md` vs `naming-conventions.md` | Roadmap lista `README.md` dentro de pastas de samples; naming-conventions não menciona README |
| C4 | `roadmap.md` (modelo semântico) vs `roadmap.md` (regra UNIP) | Modelo semântico sugere campos `city`/`year` em `ApprovalSheetComponent`, mas logo depois diz que para UNIP esses campos não devem existir |
| C5 | `rules_ai_guide.md` §14,31 vs `roadmap.md` | Guia proíbe fallbacks silenciosos; roadmap diz que componente sem conteúdo no request "simplesmente não renderiza" |

### Oportunidades de Clareza e Estrutura

| ID | Documento | Problema | Sugestão |
|----|-----------|----------|----------|
| O1 | `rules_ai_guide.md` | 35 seções sem sumário, sem priorização, sem separação de "agora" vs "futuro" | Adicionar tabela de conteúdo, bloco "Leia primeiro", marcadores de regra atual vs diretriz futura |
| O2 | `roadmap.md` | Mistura estado atual, instruções táticas de `approvalSheet` e regras permanentes no mesmo fluxo | Separar em `architecture-decisions.md` (permanente) e `implementation-order.md` (tático) |
| O3 | `single-page-reference.md` | Não explica o algoritmo de distribuição de gaps em linguagem humana | Adicionar 1-2 parágrafos descrevendo como `SinglePageGapDistributor` distribui linhas por peso |
| O4 | `body-content-component.md` | Só tem exemplo de citação inline; sem exemplo completo de seção com título + parágrafo + figura | Embutir no documento um exemplo mínimo completo de request |
| O5 | `roadmap.md` + `single-page-reference.md` | Dois checklists com itens diferentes, sem indicar qual é autoritativo | Unificar em `single-page-reference.md`, referenciar no roadmap |
| O6 | `roadmap.md` §references | Usa "provavelmente" em ponto de arquitetura sem critério de decisão | Substituir por decisão explícita ou bloco marcado "Decisão Pendente" |
| O7 | `naming-conventions.md` | Descreve sufixos mas não quando criar `LayoutCalculator` próprio vs delegar direto à engine | Adicionar nota de quando `Calculator` é opcional para componentes single-page |
| O8 | Todos | Nenhum documento mostra o fluxo end-to-end de request HTTP até bytes DOCX em uma única sequência | Criar seção "Fluxo de execução" com 10-15 passos conectando todas as camadas |

---

## Resumo Executivo

| Categoria | Crítico / Alta | Médio | Baixo |
|-----------|---------------|-------|-------|
| Gaps de implementação | 2 | 3 | 3 |
| Desvios de design | 2 arquiteturais | 3 funcionais | 3 cosméticos |
| Melhorias na proposta | — | — | 26 pontos |

**Top 5 para endereçar primeiro:**
1. **GAP-2 / DESVIO-1** — Citações curtas/indiretas como blocos autônomos (mesmo problema, duas perspectivas): define o contrato errado e gera DOCX incorreto
2. **DESVIO-2** — Tokens `"apud"`, `"et al."`, `"p. "` hardcoded no domínio violam o princípio central do projeto de não-ABNT-only
3. **GAP-5** — Heading styles sem `<w:basedOn>` podem gerar inconsistências visuais entre ambientes
4. **DESVIO-3 / GAP-10** — Flag `previousRenderedTextWasBodyParagraph` com comportamento incorreto após display objects
5. **O8 (proposta)** — Ausência de fluxo de execução end-to-end é o gap de documentação que mais impacta novos desenvolvedores e IAs
