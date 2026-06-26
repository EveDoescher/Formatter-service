# Análise: Proposta vs. Implementação — branch `feature/elementos-textuais`

Gerado por análise de três agentes paralelos independentes.

> **Status (verificado em 2026-06-26):** Todos os itens de Alta e Média severidade foram corrigidos durante a implementação das Fases 1–5. Os itens de Baixa severidade/Cosmético foram avaliados — a maioria foi corrigida; os remanescentes são intencionais ou irrelevantes. Seções mantidas para registro histórico.

---

## Parte 1 — Gaps de Implementação (proposta promete, código não entregou)

### ALTA severidade

**GAP-1 — ~~Citações inline colapsadas em texto plano (sem runs diferenciados no DOCX)~~** ✅ CORRIGIDO
`BodyParagraph` usa `List<BodyInline>` mapeada via `toDocxRun()` no renderer, gerando runs diferenciados por tipo de inline.

**GAP-2 — ~~`BodyCitation` como bloco aceita tipos que deveriam ser apenas inline~~** ✅ CORRIGIDO
`BodyCitation.java` foi removido. `BodyBlock` (sealed) não admite mais citações autônomas de qualquer tipo. Citações são exclusivamente inline via `BodyCitationCall` dentro de `BodyParagraph`.

---

### MÉDIA severidade

**GAP-3 — ~~Ausência de `InvalidBodyContentException` dedicada~~** ✅ CORRIGIDO
`InvalidBodyContentException` existe em `shared/exception/` com `InvalidBodyContentExceptionTest`.

**GAP-4 — ~~Ausência de `BodyContentRendererDocxSanityTest`~~** ✅ CORRIGIDO
`BodyContentRendererDocxSanityTest.java` existe em `rendering/component/bodycontent/`.

**GAP-5 — ~~Heading styles sem `<w:basedOn>` no writer~~** ✅ CORRIGIDO
`Docx4jWriter.createHeadingStyle()` declara `<w:basedOn>` com valor "Normal".

---

### BAIXA severidade

**GAP-8 — Numeração de seção frágil ao auto-iniciar contador de nível anterior zerado**
`SectionNumberingState.increment()` ainda força `counters[currentIndex] = 1` para níveis anteriores zerados. Comportamento intencional: previne numerações como "0.1.1". A validação de hierarquia de seções mitiga os casos problemáticos antes de chegar aqui.
- _Status:_ Mantido intencionalmente.

**GAP-9 — `body-content-figures-url-visual.json` excluído do teste automático de samples**
O arquivo existe na pasta mas não está na lista do `BodyContentSampleValidationTest`. Excluído intencionalmente por depender de fetch de URL externa — não adequado para testes automatizados sem mock de rede.
- _Status:_ Exclusão intencional.

**GAP-10 — ~~`previousRenderedTextWasBodyParagraph = true` após figura/tabela~~** ✅ CORRIGIDO
Flag renomeada para `previousBlockWasTextualContent` e lógica corrigida: ativa apenas para `BodyParagraph` e `BodyLongQuote`.

---

## Parte 2 — Desvios de Design (código diverge da proposta)

### Arquiteturais

**DESVIO-1 — ~~Dois caminhos para o mesmo tipo de citação (bloco vs inline)~~** ✅ CORRIGIDO
`BodyCitation.java` removido. `BodyBlock` sealed não admite mais citações. Caminho único: `BodyCitationCall` inline em `BodyParagraph`.

**DESVIO-2 — ~~`"p. "`, `"et al."`, `"; "`, `" apud "` hardcoded no domínio~~** ✅ CORRIGIDO
`CitationSource` usa `formatting.etAl()`, `formatting.pagePrefix()`. `BodyCitationCall` usa `formatting.apudConnector()`, `formatting.emphasisOursLabel()`, `formatting.emphasisAuthorLabel()`, `formatting.verbalCitationLabel()`. Todos os tokens vêm de `CitationFormattingRule` (perfil).

---

### Funcionais

**DESVIO-3 — ~~Flag `previousRenderedTextWasBodyParagraph` com nome e comportamento incorretos~~** ✅ CORRIGIDO
Renomeada para `previousBlockWasTextualContent`; ativa apenas após `BodyParagraph | BodyLongQuote`.

**DESVIO-5 — ~~`FigureRuleRequest` e `TableRuleRequest` sem anotações `@NotNull`~~** ✅ CORRIGIDO
Ambos os requests têm `@NotNull`/`@NotBlank` em todos os campos obrigatórios.

**DESVIO-7 — ~~`DIRECT_SHORT` bloco renderiza string pré-montada como parágrafo isolado~~** ✅ CORRIGIDO
`BodyCitation.java` removido junto com DESVIO-1.

---

### Cosméticos / Minor

**DESVIO-6 — `BodyContentRequest.toDomain()` tem null-coerce silencioso para `List.of()`**
`sections == null ? List.of()` ainda presente. O `@NotEmpty` na entrada cobre o caso na prática. Path silencioso tecnicamente contrário ao fail-fast, mas sem impacto real.
- _Status:_ Remanescente de baixo risco.

**DESVIO-9 — ~~`BodyCitation` constrói `BodyQuoteText` descartável como side-effect de validação~~** ✅ CORRIGIDO
`BodyCitation.java` foi removido; anti-padrão desapareceu junto.

**DESVIO-10 — ~~`BodyContentComponentRule` não tem `contentBindings`~~** ✅ CORRIGIDO
`BodyContentComponentRule` implementa `contentBindings()` retornando `Map.of()`.

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
