# Motor Genérico — Taxonomia e Visão Arquitetural

**Data:** 28 de Junho de 2026
**Atualizado em:** 29 de Junho de 2026 — correções aplicadas com base na revisão arquitetural (`motor-generico-taxonomia-revisao.md`) e análise subsequente.
**Contexto:** Definições conceituais produzidas em sessão de planejamento arquitetural. Este documento não descreve o estado atual do sistema — descreve o modelo alvo.

---

## O Princípio Central

O motor não sabe o que é uma capa, um resumo, um apêndice ou qualquer estrutura acadêmica específica. O motor sabe montar peças. O profile é o manual que diz quais peças usar, em que ordem, com quais regras.

**Profile declara. Motor executa. Motor não decide nada.**

---

## Taxonomia

O sistema é organizado em cinco níveis hierárquicos. Cada nível tem responsabilidades exclusivas.

---

### Nível 1 — Configuração Global

Declarada uma vez no profile. Vale para o documento inteiro.

- Dimensões de página e margens
- Orientação
- Fontes permitidas e fonte padrão
- Espaçamento de linha global
- Numeração de páginas: posição, estilo, a partir de qual componente começa a contar, a partir de qual componente começa a aparecer
- Preferências que o usuário pode escolher dentro dos limites declarados

O que o usuário pode personalizar é definido aqui — não no motor. O motor apenas valida se a escolha está dentro dos `allowedValues` declarados e rejeita com erro claro se não estiver.

---

### Nível 2 — Componente

Um bloco nomeado do documento. O profile o cria, nomeia e configura. O motor não sabe o que é — sabe que existe um componente com aquele id usando tal engine.

**Atributos que o profile declara para cada componente:**

| Campo | Responsabilidade |
|---|---|
| `id` | Nome arbitrário. "capa", "resumo", "cover", qualquer string. |
| `engine` | Qual macro-estrutura processa este componente. |
| `required` | Se ausente no content, falha ou ignora. |
| `collection` | Se o componente itera sobre um array de itens independentes. |
| `contentBindings` | Quais chaves do content alimentam quais slots declarados no profile. |
| Regras do engine | Configuração específica do engine escolhido para este componente. |

O profile também declara `componentOrder` — a sequência de renderização dos componentes. A ordem é uma decisão da norma, não do motor.

---

### Nível 3 — Engine (Macro-estruturas)

O que o motor realmente sabe construir. São as peças de Lego. O profile escolhe qual usar por componente.

#### `SINGLE_PAGE`
Página única com grupos ancorados. Balanceia conteúdo verticalmente usando pesos declarados no profile. Falha explicitamente se o conteúdo não couber.

- Grupos: `TOP`, `CENTER`, `BOTTOM`
- Cada grupo contém elementos com pesos e estilos declarados no profile
- O motor calcula os espaços dinamicamente — nunca recebe uma altura fixa

**Já existe:** `SinglePageLayoutEngine`. Já é genérico. Não precisa de mudanças arquiteturais.

#### `FLOW`
Fluxo contínuo recursivo. Processa uma árvore de seções e elementos folha. Quebra página automaticamente. Não sabe o que são seções ABNT — sabe processar nós com nível, título e filhos conforme as regras do profile para aquele nível.

Dois modos:
- **Singular** — um único fluxo recursivo. Ex: bodyContent, resumo, references, glossary.
- **Collection** — array de itens independentes, cada um sendo um fluxo completo com heading gerado sequencialmente pelo motor. Ex: apêndice, anexo.

Configurações que o profile declara para o `FLOW`:
- Regras por nível de seção: estilo de heading, numeração, quebra de página forçada antes
- Configuração de colunas: `columns: 2`, `columnSpacing`
- Regras por tipo de elemento: como processar cada tipo de nó folha

**Não existe ainda.** O `BodyContentRenderer` atual concentra essa lógica de forma acoplada ao domínio acadêmico. Precisa ser extraído como engine genérico.

#### `INDEX`
Renderiza listas geradas pelo sistema a partir dos índices produzidos pela Phase 0. Não consome conteúdo declarado pelo usuário — consome dados coletados automaticamente durante a análise do document.

Componentes que usam `INDEX`:
- Lista de figuras (`listOfFigures`)
- Lista de tabelas (`listOfTables`)
- Sumário (`tableOfContents`)

Configurações que o profile declara para o `INDEX`:
- `source`: qual índice da Phase 0 alimenta este componente (`FIGURES`, `TABLES`, `SECTIONS`, ...)
- `entryStyle`: estilo tipográfico de cada entrada
- `leaderStyle`: estilo do pontilhado entre título e número de página
- `pageNumberPlaceholder`: texto a usar como placeholder de número de página (substituído na Phase 3)

> **Atenção:** `references` e `glossary` não usam `INDEX` — seu conteúdo é declarado pelo usuário no content, não gerado pela Phase 0. Esses componentes usam engine `FLOW` com `flowRules` específicas para o estilo de cada entrada.

**Não existe como engine standalone.** A lógica atual está dispersa em renderers específicos.

---

### Nível 4 — Seção

Exclusiva do engine `FLOW`. Container estrutural recursivo. Não é um elemento de conteúdo — não tem texto próprio. Apenas agrupa filhos e declara nível e título.

| Campo | Descrição |
|---|---|
| `level` | Nível hierárquico. Determina qual regra do profile se aplica. |
| `title` | String simples. O motor formata conforme as regras do nível. |
| `children` | Lista de seções ou elementos folha. Recursivo. |

**Três tipos de título:**

| Tipo | Origem | Exemplo |
|---|---|---|
| Profile-declared | Rótulo fixo no profile. O content não fornece nada. | "Resumo", "Referências", "Abstract" |
| Content-declared | Título definido pelo usuário no content. | "Introdução", "Metodologia" |
| Híbrido | Profile fornece estrutura e prefixo. Content fornece o texto. | "APÊNDICE A — Formulários de Pesquisa" |

No caso híbrido, o heading é composto por uma sequência de segmentos com origens distintas. O profile declara os segmentos, cada um com `source` e `styleId` próprio:

| `source` | Origem do conteúdo | Exemplo |
|---|---|---|
| `PREFIX` | String literal declarada no profile | `"APÊNDICE"` |
| `SEQUENCE` | Gerado pelo motor conforme `sequenceStyle` | `"A"` |
| `LITERAL` | String literal declarada no profile | `" — "` |
| `CONTENT` | Título fornecido pelo usuário no content | `"Formulários de Pesquisa"` |

O aluno não escreve "APÊNDICE A" — só fornece o título. O motor combina os segmentos. O profile ABNT pode usar o mesmo `styleId` para todos os segmentos; outros profiles podem diferenciar (ex: prefixo em negrito, título em peso normal).

**O profile declara por nível de seção:**
- `styleId` para o heading
- `numberingStyle`: `NUMERIC_DOT` (1, 1.1, 1.1.1), `ALPHA_NUMERIC` (A.1, A.1.1), `NONE`
- `pageBreakBefore`: se força quebra de página antes deste nível
- `includeParentPrefix`: se a numeração do item pai deve prefixar a numeração interna (para o modo collection)

---

### Nível 5 — Elemento

Folha da árvore. Existem dois grupos distintos: elementos de bloco e elementos inline. A distinção é estrutural — um elemento de bloco é filho de uma seção; um elemento inline é filho de `runs[]` dentro de um elemento de bloco.

#### Modelo de `runs[]`

`runs[]` é a unidade central de formatação inline. Todo elemento de bloco que contém texto usa `runs[]`. Um run é um item da sequência de conteúdo inline — existem três tipos:

| Tipo | Campos | Descrição |
|---|---|---|
| `text` | `text`, `bold?`, `italic?`, `underline?`, `strikethrough?`, `styleId?` | Texto com formatação inline |
| `cross-ref` | `targetId` | Referência cruzada. Resolvida na Phase 0 para o texto gerado (ex: "Figura 3.2") |
| `footnote` | `content: runs[]` | Marcador de nota de rodapé. O marcador aparece inline; o `content` é o texto que vai para o rodapé da página |

O aluno não escreve "Figura 3.2" — escreve um `cross-ref` com `targetId`. O número é gerado pelo sistema.
O aluno não insere o texto da nota no rodapé manualmente — declara um `footnote` inline onde o marcador deve aparecer, e o `content` é o texto da nota.

#### Elementos de bloco (filhos de seção)

**Regra de decisão:**
> Novo tipo quando a estrutura de dados é diferente. Configuração de profile quando só o comportamento de renderização é diferente.

Palavras-chave têm a mesma estrutura de uma lista comum — são uma `list` com `display: "inline"` e `prefixLabel` declarados no profile. Não é um novo tipo de elemento.

| Tipo | Campos estruturais |
|---|---|
| `paragraph` | `runs[]` |
| `figure` | `imageRef`, `caption`, `source?` |
| `table` | `rows[]` → `cells[]` com `content`, `colspan?`, `rowspan?` |
| `quote` | `runs[]`, `attribution?` |
| `code` | `text` (raw, sem formatação inline), `language?` |
| `list` | `items[]` com `runs[]` e `sublist?` recursivo |

#### Elementos inline (filhos de `runs[]`)

Não são filhos de seção. Aparecem dentro de `runs[]` de qualquer elemento que contenha texto.

| Tipo | Campos | Contrato de falha |
|---|---|---|
| `cross-ref` | `targetId` | **STRICT:** Phase 0 falha imediatamente com erro identificando o `targetId` quebrado. **FLEXIBLE:** Phase 1 substitui por `[?]` e emite aviso. O documento é gerado com indicação visual do problema. A validação no frontend durante edição é uma camada complementar que não elimina a garantia do backend. |
| `footnote` | `content: runs[]` | Renderizado na Phase 2 via `w:footnote`. Ver incógnita registrada na Phase 3. |

---

## O Pipeline de Processamento

O processamento ocorre em quatro fases sequenciais.

```
Phase 0 — Análise
Phase 1 — Rendering
Phase 2 — Output
Phase 3 — Post-processing
```

### Phase 0 — Análise

Varredura completa do content antes de qualquer renderização. Produz índices que as fases seguintes consomem.

**O que coleta:**
- Todos os display objects (figuras, tabelas, quadros, gráficos, listagens de código) com numeração sequencial conforme as regras do profile
- Títulos de todas as seções com nível e hierarquia — usados pelo componente `tableOfContents` via engine `INDEX`
- Todas as notas de rodapé com numeração
- Todos os ids declarados e seus tipos

**Regras de numeração de display objects:**

A numeração é declarada no profile, por tipo de objeto. Cada regra (`FigureRule`, `TableRule`, `CodeListingRule`, etc.) deve declarar:

| Campo | Valores | Descrição |
|---|---|---|
| `numberingStrategy` | `GLOBAL_SEQUENTIAL` \| `BY_CHAPTER` | Sequência única no documento ou reiniciada por capítulo com prefixo numérico |
| `label` | string | Rótulo que precede o número. Ex: `"Figura"`, `"Tabela"` |
| `separator` | string | Separador entre número de capítulo e número do objeto em `BY_CHAPTER`. Ex: `"."` |

Exemplo: `BY_CHAPTER` com `label: "Figura"` e `separator: "."` gera "Figura 1.1", "Figura 1.2", "Figura 2.1". Sem essa declaração, a Phase 0 não sabe como numerar e o índice de cross-references fica incorreto para qualquer profile que use numeração por capítulo.

**O que resolve:**
- Índice de cross-references: `"fig-formulario" → "Figura 3.2"` (usando as regras declaradas no profile)
- Dados para o engine `INDEX`: lista de figuras, lista de tabelas, hierarquia de seções para o sumário
- Numeração de notas de rodapé

**Sumário (`tableOfContents`):**

O sumário usa engine `INDEX` com `source: SECTIONS`. A Phase 0 coleta a hierarquia completa de seções com títulos e níveis. A Phase 1 renderiza o sumário usando esses dados, com números de página como placeholder conforme `pageNumberPlaceholder` declarado no profile. A Phase 3 resolve os números de página reais via LibreOffice após o layout físico estar completo.

**Por que isso importa:**

Sem Phase 0, uma lista de figuras que aparece antes do bodyContent não tem dados para renderizar. Um cross-reference para uma figura que aparece depois do parágrafo que o referencia não teria número ainda. Um sumário no pré-textual não teria hierarquia de seções. A Phase 0 resolve todos: o índice existe antes de qualquer renderização.

### Phase 1 — Rendering

O motor lê o profile, consome o índice da Phase 0, e transforma o content em blocos abstratos (`DocxBlock`, `DocxParagraph`, etc.). Não conhece docx4j.

### Phase 2 — Output

`Docx4jWriter` traduz os blocos abstratos para o arquivo DOCX. Único ponto de contato com docx4j.

**Notas de rodapé:** A Phase 2 insere cada `footnote` como `w:footnote` vinculado ao parágrafo correto. O processador de texto (Word, LibreOffice) posiciona o conteúdo no rodapé da página certa automaticamente — não é necessária intervenção da Phase 3 para isso.

**Prevenção de headings órfãos:** A Phase 2 aplica `<w:keepNext/>` em cadeia: no heading e na linha em branco imediatamente após o heading. A cadeia "heading → linha em branco → parágrafo" permanece unida em qualquer quebra de página. O `DocxBlankLine` emitido após um heading deve carregar um sinalizador para que o `Docx4jWriter` saiba aplicar `<w:keepNext/>` nele. A propriedade sobrevive a modificações de paginação da Phase 3 porque o processador de texto a reaplica a cada novo layout.

### Phase 3 — Post-processing

LibreOffice via UNO API. Resolve o que só é conhecido depois do layout físico:

- Números de página no sumário
- Labels "Continua..." e "Continuação..." em tabelas que quebram página

> **Incógnita a verificar:** As modificações da Phase 3 — inserção de linhas continua/continuação e outras correções — alteram a paginação do documento. Verificar empiricamente se o LibreOffice recalcula os vínculos `w:footnote` corretamente após essas modificações. Se recalcular automaticamente, nenhuma ação adicional é necessária. Se não recalcular, é um problema a resolver na implementação da Phase 3.

---

## O que o Profile declara vs o que o Content declara

| Profile declara | Content declara |
|---|---|
| Que existe um componente com tal id | Os valores que preenchem os slots desse componente |
| Qual engine processa o componente | — |
| Que o componente é obrigatório ou opcional | Se o componente está presente ou não |
| Quais slots existem e onde ficam na página | O texto de cada slot |
| Que heading nível 1 é Arial 12 negrito maiúsculo | Que existe uma seção de nível 1 com tal título |
| Que figure tem legenda abaixo da imagem | A imagem, a legenda, a fonte opcional |
| Que list tem `display: "inline"` e `prefixLabel: "Palavras-chave:"` | Os itens da lista |
| Que o apêndice usa prefixo "APÊNDICE" com sequência alfabética | O título e conteúdo de cada item |
| Que `source` de figura é opcional | Se existe ou não source naquela figura |
| Que figures usam `BY_CHAPTER` com `label: "Figura"` | As figuras em si, com seus ids e captions |
| Que `tableOfContents` usa `source: SECTIONS` | — (dados vêm da Phase 0, não do content) |

---

## Mapeamento: o que existe e o que falta

### Já existe e é reaproveitável sem mudança

| O que | Onde |
|---|---|
| Engine `SINGLE_PAGE` | `SinglePageLayoutEngine` — já completamente genérico |
| Medição de texto | `FontMetricsTextMeasurer` — genérico |
| Abstração de output | `DocxWriter` interface + `Docx4jWriter` — isolados |
| Estilos tipográficos | `StyleRule` (13 campos, 100% profile-driven) |
| Dimensões de página | `PageRule` |
| Numeração de páginas | `PageNumberingRule` |
| Elementos folha | `BodyParagraph`, `BodyFigure`, `BodyTable`, `BodyLongQuote`, `BodyCodeListing`, `BodyList` |
| Regras de elementos | `FigureRule`, `TableRule`, `CodeListingRule`, `CitationFormattingRule`, `CrossReferenceLabelsRule` |

### Existe mas precisa ser generalizado

| O que | Problema | O que muda |
|---|---|---|
| `BodySection` | Não distingue título profile-declared de content-declared | Adicionar `titleSource: PROFILE \| CONTENT \| HYBRID`; no caso `HYBRID`, substituir campo de string única por lista de segmentos com `source` e `styleId` |
| `BodyList` | Sem `display inline/block`, `prefixLabel`, `itemSeparator` | Novos campos na regra de lista no profile |
| Assemblers de single-page | Mapeamento hardcoded `"title" → cover.title()` | Substituir por `contentBindings` genérico lido do profile |
| `FigureRule`, `TableRule`, `CodeListingRule` | Sem `numberingStrategy`, `label`, `separator` | Adicionar os três campos; Phase 0 lê para construir o índice corretamente |

### Não existe — precisa ser construído

| O que | Descrição |
|---|---|
| `FlowLayoutEngine` | Engine genérico que substitui `BodyContentRenderer`. Lê regras do profile, processa seções e elementos, delega tipos ao registry. |
| `TextTypeRegistry` | Registry de processadores por tipo de elemento folha. O motor despacha para o processador certo conforme o tipo do nó. |
| `DisplayObjectCollector` (Phase 0) | Varredura pré-renderização que constrói o índice de cross-references e dados para os engines `INDEX`. |
| `RunProcessor` | Processador do modelo de `runs[]`: distingue `text`, `cross-ref` e `footnote`; delega cada tipo ao handler correto. |
| Engine `INDEX` standalone | Engine para componentes gerados pela Phase 0 (listOfFigures, listOfTables, tableOfContents). |
| Campo `engine` no profile | Declaração explícita de qual macro-estrutura cada componente usa. Hoje é implícito pelo tipo da `ComponentRule`. |
| `contentBindings` genérico | Resolver que lê os bindings declarados no profile e busca valores no content por chave. Substitui assemblers específicos. |
| `flowRules` no profile | Configuração de regras por tipo de elemento para o `FlowLayoutEngine`. |
| Modo `collection` no `FLOW` | Array de itens independentes com heading gerado sequencialmente por segmentos. |
| Sinalizador `keepNextAfter` no `DocxBlankLine` | Indica que a linha em branco é imediatamente posterior a um heading — o `Docx4jWriter` aplica `<w:keepNext/>` nela para fechar a cadeia anti-órfão. |

---

## Regras de decisão para casos futuros

**Novo tipo de elemento quando:** a estrutura de campos é diferente — o tipo determina quais dados existem.

**Configuração de profile quando:** os dados são os mesmos, só o comportamento visual muda.

**Optional no tipo existente quando:** é uma variação conhecida que aparece em pelo menos um perfil real.

**Fora de escopo quando:** o caso é absurdamente específico de um único perfil e não justifica abstração. O sistema se propõe a cobrir quase todos os perfis — não todos.

---

## Exemplo conceitual completo — Apêndice

Para ilustrar como a taxonomia se aplica a um componente real e conceitualmente complexo.

**Profile declara:**
```json
"appendix": {
  "engine": "FLOW",
  "required": false,
  "collection": true,
  "itemHeading": {
    "segments": [
      { "source": "PREFIX",   "value": "APÊNDICE",    "styleId": "appendix.heading" },
      { "source": "SEQUENCE", "style": "ALPHA_UPPER",  "styleId": "appendix.heading" },
      { "source": "LITERAL",  "value": " — ",          "styleId": "appendix.heading" },
      { "source": "CONTENT",                            "styleId": "appendix.heading" }
    ],
    "pageBreakBefore": true
  },
  "flowRules": {
    "sectionNumbering": {
      "includeItemPrefix": true,
      "style": "ALPHA_NUMERIC"
    }
  }
}
```

**Content declara:**
```json
"appendix": [
  {
    "title": "Formulários de Pesquisa",
    "children": [
      { "type": "paragraph", "runs": [{ "type": "text", "text": "O formulário foi aplicado..." }] },
      { "type": "figure", "id": "fig-form-principal", "imageRef": "...", "caption": "Formulário principal" }
    ]
  },
  {
    "title": "Dados Coletados",
    "children": [
      { "type": "table", "rows": [...] }
    ]
  }
]
```

**O motor:**
1. Lê `collection: true` — itera sobre o array
2. Para cada item, combina os segmentos do `itemHeading`: `PREFIX` + `SEQUENCE` + `LITERAL` + título do content → "APÊNDICE A — Formulários de Pesquisa"
3. Aplica `pageBreakBefore` antes de cada item
4. Processa os `children` como fluxo recursivo com contexto de numeração `ALPHA_NUMERIC` — seções internas ficam A.1, A.1.1
5. Não sabe o que é um apêndice — sabe processar um `FLOW collection` com essas regras

---

## Exemplo conceitual completo — Resumo

**Profile declara:**
```json
"resumo": {
  "engine": "FLOW",
  "required": true,
  "collection": false,
  "heading": {
    "titleSource": "PROFILE",
    "title": "RESUMO",
    "styleId": "pretextual.heading",
    "numberingStyle": "NONE"
  },
  "flowRules": {
    "paragraph": { "styleId": "body.paragraph" },
    "list": {
      "display": "inline",
      "prefixLabel": "Palavras-chave:",
      "itemSeparator": ". ",
      "styleId": "body.keywords"
    }
  }
}
```

**Content declara:**
```json
"resumo": {
  "children": [
    { "type": "paragraph", "runs": [{ "type": "text", "text": "Este trabalho apresenta..." }] },
    { "type": "list", "items": [
      { "runs": [{ "type": "text", "text": "formatação" }] },
      { "runs": [{ "type": "text", "text": "documentos acadêmicos" }] },
      { "runs": [{ "type": "text", "text": "motor genérico" }] }
    ]}
  ]
}
```

O aluno não escreve "RESUMO" nem "Palavras-chave:". O profile declara tudo isso. O aluno só fornece o texto do parágrafo e os itens da lista.
