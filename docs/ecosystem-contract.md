# Formatter Service — Ecosystem Contract

Este documento descreve o que os outros microserviços do ecossistema precisam saber sobre o
formatter-service: endpoints, formato completo da requisição, formato completo do profile, e
todos os contratos entre profile e conteúdo. O contrato está estável.

---

## O que o formatter-service faz

Recebe profile + preferences + content e devolve um DOCX formatado. Não persiste nada, não
conhece usuários, não toma decisões de negócio. É um executor puro: o profile declara todas as
regras, o serviço executa.

---

## Endpoints

### 1. Exportar DOCX diretamente

```
POST /api/v1/exports/docx
Content-Type: application/json
→ 200 application/vnd.openxmlformats-officedocument.wordprocessingml.document
→ 400 application/json
→ 500 application/json
```

Retorna os bytes do DOCX diretamente no corpo. Síncrono.

### 2. Gerar DOCX com download posterior

```
POST /api/v1/exports/docx/generated
Content-Type: application/json
→ 201 application/json
→ 400 application/json
→ 500 application/json
```

Gera o DOCX, armazena em memória e retorna uma referência:

```json
{
  "exportId": "uuid-gerado",
  "fileName": "nome-do-arquivo.docx",
  "downloadUrl": "/api/v1/exports/docx/generated/{exportId}/download",
  "sizeBytes": 48291
}
```

O export vive apenas em memória — não sobrevive a restart.

### 3. Baixar DOCX gerado

```
GET /api/v1/exports/docx/generated/{exportId}/download
→ 200 application/vnd.openxmlformats-officedocument.wordprocessingml.document
→ 404 application/json
```

### Formato de erro (400, 404, 500)

```json
{
  "message": "Descrição do erro.",
  "errors": [
    { "field": "document.cover.title", "message": "must not be blank" }
  ]
}
```

`errors` é preenchido em violações de validação estrutural (`@NotBlank`, `@NotNull`, etc.).
Para erros de regra de negócio (profile inválido, overflow de página, tipo sem formato), `errors`
vem vazio e `message` descreve o problema.

### Header `X-Formatter-Warnings`

Quando o pós-processamento detecta problemas não fatais, o formatter retorna 200 com o DOCX
**e** o header:

```
X-Formatter-Warnings: overflow de margem na página 3; fonte Arial substituída por Liberation Sans
```

Múltiplos avisos separados por `; `.

### Autenticação

Ainda não implementada. Quando o IAM-service estiver integrado, o formatter esperará um header
`X-Account-Id` injetado pelo gateway.

---

## Estrutura da requisição

```json
{
  "fileName": "nome-do-arquivo-sem-extensao",
  "profileId": "abnt-unip-profile",
  "options": {
    "selectedComponents": ["cover", "titlePage", "bodyContent", "references"]
  },
  "document": { ... }
}
```

`profileId` e `profile` são mutuamente exclusivos — exatamente um dos dois deve estar presente.

`selectedComponents` vazio `[]` equivale a "renderizar todos os componentes que têm conteúdo
em `document`". Para controle explícito, sempre listar os componentes desejados.

O campo `work` (legado) é aceito por compatibilidade retroativa mas **completamente ignorado**
pelo engine. Não construir integrações que dependam dele.

---

## Profile

### Dois modos de fornecer o profile

**Modo 1 — por ID (produção)**

```json
{ "profileId": "abnt-unip-profile" }
```

O formatter busca o profile no `ProfileProvider` configurado (atualmente: classpath). Quando o
profile-service estiver integrado, o formatter chamará:

```
GET /profiles/{profileId}  →  JSON do profile completo
```

O JSON entregue pelo profile-service deve ser idêntico ao formato descrito abaixo.

**Modo 2 — inline (testes)**

```json
{ "profile": { "id": "...", "pageRule": { ... }, ... } }
```

Mesmo formato JSON do modo 1 com uma diferença: **`postProcessing` é ignorado no modo inline**.
Pós-processamento LibreOffice só executa quando o profile vem do classpath ou do profile-service.

O profile de referência está em:
`src/main/resources/profiles/abnt-unip-profile.json`

---

## Formato do profile JSON

```json
{
  "id": "abnt-unip-profile",
  "displayName": "ABNT UNIP Profile",
  "componentOrder": [ ... ],
  "pageRule": { ... },
  "pageNumbering": { ... },
  "postProcessing": { ... },
  "styleRules": [ ... ],
  "componentRules": { ... }
}
```

### `componentOrder`

Lista ordenada dos IDs de componentes declarados neste profile. Define a ordem do documento final.
IDs em `selectedComponents` que não estiverem aqui são rejeitados com 400.

O engine não tem lista interna de IDs reconhecidos. O profile é a única fonte de verdade sobre
quais componentes existem e o que cada um significa. Qualquer ID pode aparecer em `componentOrder`
desde que tenha uma entrada correspondente em `componentRules`.

### `pageRule`

Obrigatório.

```json
"pageRule": {
  "widthCm": 21,
  "heightCm": 29.7,
  "marginTopCm": 3,
  "marginRightCm": 2,
  "marginBottomCm": 2,
  "marginLeftCm": 3,
  "orientation": "PORTRAIT"
}
```

| Campo | Tipo | Obrigatório |
|---|---|---|
| `widthCm` | `double` > 0 | sim |
| `heightCm` | `double` > 0 | sim |
| `marginTopCm` | `double` ≥ 0 | sim |
| `marginRightCm` | `double` ≥ 0 | sim |
| `marginBottomCm` | `double` ≥ 0 | sim |
| `marginLeftCm` | `double` ≥ 0 | sim |
| `orientation` | `PORTRAIT` \| `LANDSCAPE` | sim |

### `pageNumbering`

Opcional. Quando ausente, o documento não tem numeração de páginas.

```json
"pageNumbering": {
  "enabled": true,
  "countFromComponentId": "titlePage",
  "visibleFromComponentId": "bodyContent",
  "styleId": "pageNumber",
  "placement": "HEADER_RIGHT",
  "verticalDistanceFromPageEdgeCm": 2,
  "horizontalDistanceFromPageEdgeCm": 2
}
```

| Campo | Obrigatório quando `enabled: true` | Descrição |
|---|---|---|
| `enabled` | sempre | Liga ou desliga a numeração |
| `countFromComponentId` | sim | Componente cuja primeira página recebe número 1 |
| `visibleFromComponentId` | sim | Componente a partir do qual os números ficam visíveis |
| `styleId` | sim | Referência a um `styleRule` |
| `placement` | sim | `HEADER_RIGHT` \| `HEADER_CENTER` \| `FOOTER_RIGHT` \| `FOOTER_CENTER` |
| `verticalDistanceFromPageEdgeCm` | sim (≥ 0) | Distância vertical da borda da página |
| `horizontalDistanceFromPageEdgeCm` | sim (≥ 0) | Distância horizontal da borda da página |

**Restrições de seleção:** se `selectedComponents` incluir qualquer componente na posição de ou
após `countFromComponentId` no `componentOrder`, então `countFromComponentId` também deve estar
em `selectedComponents`. O mesmo vale para `visibleFromComponentId`. Violação retorna 400.

### `postProcessing`

Opcional. Ignorado no modo inline. Todos os sub-campos são opcionais.

```json
"postProcessing": {
  "tableContinuationLabels": {
    "enabled": true,
    "continuesLabel": "continua",
    "continuationLabel": "continuação",
    "conclusionLabel": "conclusão",
    "labelStyleId": "bodyContent.paragraph"
  },
  "orphanTitleCorrection": { "enabled": true },
  "integrityCheck": {
    "enabled": true,
    "checkMarginOverflow": true,
    "checkFontSubstitution": true,
    "maxPages": 500
  },
  "pdfOutput": { "enabled": false }
}
```

| Campo | Descrição |
|---|---|
| `tableContinuationLabels.enabled` | Injeta labels em tabelas que quebram entre páginas |
| `tableContinuationLabels.continuesLabel` | Label na primeira parte |
| `tableContinuationLabels.continuationLabel` | Label nas partes intermediárias |
| `tableContinuationLabels.conclusionLabel` | Label na última parte |
| `tableContinuationLabels.labelStyleId` | Estilo das labels |
| `orphanTitleCorrection.enabled` | Move títulos órfãos para a próxima página |
| `integrityCheck.enabled` | Executa verificações — avisos vão para `X-Formatter-Warnings` |
| `integrityCheck.checkMarginOverflow` | Verifica elementos que ultrapassam as margens |
| `integrityCheck.checkFontSubstitution` | Verifica fontes substituídas |
| `integrityCheck.maxPages` | Rejeita documentos com mais páginas que o limite |
| `pdfOutput.enabled` | Exporta também PDF além do DOCX |

### `styleRules`

Obrigatório, não vazio. Lista de estilos tipográficos. Todos os valores que variam entre perfis
ficam aqui.

Existem dois tipos de estilo com campos e contextos de uso distintos:

#### Estilos de bloco — `PARAGRAPH`, `HEADING_1`…`HEADING_6`

Aplicados a parágrafos e títulos inteiros. Todos os campos abaixo são obrigatórios.

```json
{
  "id": "bodyContent.paragraph",
  "type": "PARAGRAPH",
  "fontFamily": "Times New Roman",
  "fontSizePt": 12,
  "alignment": "JUSTIFIED",
  "lineSpacing": 1.5,
  "firstLineIndentCm": 1.25,
  "leftIndentCm": 0,
  "rightIndentCm": 0,
  "spacingBeforePt": 0,
  "spacingAfterPt": 0,
  "bold": false,
  "italic": false,
  "uppercase": false
}
```

| Campo | Tipo | Obrigatório |
|---|---|---|
| `id` | `string` | sim |
| `type` | `PARAGRAPH` \| `HEADING_1`…`HEADING_6` | sim |
| `fontFamily` | `string` | sim |
| `fontSizePt` | `double` > 0 | sim |
| `alignment` | `LEFT` \| `RIGHT` \| `CENTER` \| `JUSTIFIED` | sim |
| `lineSpacing` | `double` > 0 | sim |
| `firstLineIndentCm` | `double` ≥ 0 | sim |
| `leftIndentCm` | `double` ≥ 0 | sim |
| `rightIndentCm` | `double` ≥ 0 | sim |
| `spacingBeforePt` | `double` ≥ 0 | sim |
| `spacingAfterPt` | `double` ≥ 0 | sim |
| `bold` | `boolean` | sim |
| `italic` | `boolean` | sim |
| `uppercase` | `boolean` | sim |

#### Estilos de caractere — `CHARACTER`

Aplicados a trechos inline dentro de um parágrafo (ex: chamada de nota de rodapé, marcador de
citação). Não têm propriedades de layout — apenas aparência tipográfica do caractere.

```json
{
  "id": "bodyContent.footnoteCall",
  "type": "CHARACTER",
  "fontFamily": "Times New Roman",
  "fontSizePt": 10,
  "bold": false,
  "italic": false,
  "superscript": true,
  "underline": false
}
```

| Campo | Tipo | Obrigatório |
|---|---|---|
| `id` | `string` | sim |
| `type` | `CHARACTER` | sim |
| `fontFamily` | `string` | sim |
| `fontSizePt` | `double` > 0 | sim |
| `bold` | `boolean` | não |
| `italic` | `boolean` | não |
| `superscript` | `boolean` | não |
| `underline` | `boolean` | não |

Campos de layout (`alignment`, `lineSpacing`, `firstLineIndentCm`, etc.) não se aplicam a estilos
`CHARACTER` e são ignorados se presentes.

#### Qual tipo usar em cada campo

| Campo em `componentRules` | Tipo de style esperado |
|---|---|
| `styleMapping.paragraphStyleId` | `PARAGRAPH` |
| `styleMapping.sectionTitleStyleIdsByLevel` | `HEADING_1`…`HEADING_6` |
| `styleMapping.directLongQuoteStyleId` | `PARAGRAPH` |
| `styleMapping.directShortQuoteStyleId` | `CHARACTER` |
| `styleMapping.indirectCitationStyleId` | `CHARACTER` |
| `styleMapping.citationOfCitationStyleId` | `CHARACTER` |
| `styleMapping.listOrderedStyleId` | `PARAGRAPH` |
| `styleMapping.listUnorderedStyleId` | `PARAGRAPH` |
| `styleMapping.equationStyleId` | `PARAGRAPH` |
| `styleMapping.footnoteCallStyleId` | `CHARACTER` |
| `styleMapping.footnoteTextStyleId` | `PARAGRAPH` |
| `figure.captionStyleId` | `PARAGRAPH` |
| `figure.sourceStyleId` | `PARAGRAPH` |
| `table.headerStyleId` | `PARAGRAPH` |
| `table.cellStyleId` | `PARAGRAPH` |
| `codeListing.captionStyleId` | `PARAGRAPH` |
| `codeListing.sourceStyleId` | `PARAGRAPH` |
| `codeListing.codeStyleId` | `PARAGRAPH` |
| `pageNumbering.styleId` | `CHARACTER` |
| Demais `*StyleId` em componentRules | `PARAGRAPH` salvo indicação contrária |

Qualquer `styleId` referenciado em `componentRules` deve ter uma entrada correspondente em
`styleRules`. A camada de profile valida essa consistência interna ao carregar o profile — uma
referência inválida retorna 400 antes de qualquer renderização.

### `componentRules`

Obrigatório. Mapa de ID de componente para suas regras. O campo `ruleType` discrimina qual
mecanismo de renderização o engine usará para aquele componente. Se um componente selecionado
não tiver rule correspondente no mapa, retorna 400.

```json
"componentRules": {
  "meu-componente": {
    "ruleType": "SINGLE_PAGE",
    "componentId": "meu-componente",
    ...
  }
}
```

`componentId` dentro do objeto deve ser idêntico à chave do mapa — validado ao carregar o
profile.

---

## `componentRules` — tipos de rule disponíveis

O engine não tem lista de componentes reconhecidos. Cada entrada em `componentRules` associa um
ID arbitrário (definido pelo profile) a um **tipo de rule** via o campo `ruleType`. O tipo de
rule determina qual mecanismo de renderização o engine usa para aquele componente.

Todo profile pode usar qualquer combinação desses tipos com qualquer ID. Nenhum ID específico
é obrigatório ou tem significado especial para o engine.

Os `ruleType` disponíveis:

| `ruleType` | Mecanismo |
|---|---|
| `SINGLE_PAGE` | Página única com grupos posicionais e layout proporcional |
| `BODY_CONTENT` | Conteúdo textual em seções recursivas com elementos textuais |
| `REFERENCE_LIST` | Lista de referências bibliográficas com formatação declarativa |
| `FLOW_TEXTUAL` | Fluxo linear de itens declarados no profile |
| `SECTIONED` | Itens sequenciais com letra, título e seções internas |
| `ELEMENT_INDEX` | Índice derivado de elementos textuais coletados de outro componente |
| `SECTION_INDEX` | Índice derivado da estrutura de seções de outro componente |

---

### `ruleType: SINGLE_PAGE`

Componente que ocupa exatamente uma página. O engine distribui os grupos verticalmente usando os
pesos de `gapRules` para calcular os espaços entre eles. Se o conteúdo não couber em uma página,
retorna 400.

```json
"meu-componente": {
  "ruleType": "SINGLE_PAGE",
  "componentId": "meu-componente",
  "slots": { ... },
  "styleMapping": { ... },
  "layoutRule": { ... }
}
```

#### `slots`

Mapa de nome-do-slot para declaração. O nome do slot é a mesma chave usada no conteúdo da
requisição.

| `type` | Valor no conteúdo | Campos adicionais obrigatórios |
|---|---|---|
| `TEXT` | `"string"` | `required` |
| `TEXT_LIST` | `["a", "b"]` | `required` |
| `COMPOSED_TEXT` | `{"field": "valor", ...}` | `required`, `template`, `fieldNames` |
| `SIGNATURE_BLOCK_LIST` | `[{"name": "...", ...}]` | `required`, `signatureLineEnabled`, `signatureLineText`, `lineTemplates`, `knownFieldNames` |

`COMPOSED_TEXT`: o engine substitui cada `{fieldName}` em `template` pelo valor correspondente
no mapa do conteúdo. Se um campo de `fieldNames` estiver ausente e `required: true`, retorna 400.

`SIGNATURE_BLOCK_LIST`: cada membro é um mapa livre. `lineTemplates` gera uma linha por template,
substituindo `{fieldName}` pelos valores do membro. `signatureLineEnabled: true` gera a linha de
assinatura acima de cada membro.

#### `styleMapping`

Mapa de nome-do-slot para ID de `styleRule`.

#### `layoutRule`

```json
"layoutRule": {
  "groups": [
    {
      "id": "bloco-titulo",
      "required": true,
      "items": [
        {
          "id": "title",
          "required": true,
          "horizontalPlacement": { "strategy": "FULL_CONTENT_WIDTH" },
          "blankLinesAfter": 0,
          "maxVisualLinesPerValue": null
        }
      ]
    }
  ],
  "gapRules": [
    { "fromGroupId": "bloco-instituicao", "toGroupId": "bloco-titulo", "weight": 30 }
  ],
  "policy": {
    "anchorStrategy": "LAST_GROUP_AT_SAFE_AREA_END",
    "lineHeightStrategy": "MAX_EXACT_LINE_HEIGHT",
    "spacerStylePolicy": "NEXT_GROUP_STYLE",
    "safetyPolicy": "MARGIN_BASED"
  }
}
```

**`groups`** — obrigatório, não vazio. Cada grupo:

| Campo | Tipo | Obrigatório |
|---|---|---|
| `id` | `string` | sim |
| `required` | `boolean` | sim |
| `items` | lista | sim, não vazio |

Cada item:

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `id` | `string` | sim | Referencia um slot por nome |
| `required` | `boolean` | sim | |
| `horizontalPlacement.strategy` | `enum` | sim | `FULL_CONTENT_WIDTH` \| `FROM_PAGE_CENTER_TO_RIGHT_MARGIN` |
| `blankLinesAfter` | `int` | não (padrão 0) | Linhas em branco após o item dentro do grupo |
| `maxVisualLinesPerValue` | `int` | não | Se o valor exceder esse número de linhas, retorna 400 |

**`gapRules`** — obrigatório (pode ser lista vazia). Pesos proporcionais do espaço entre grupos:

| Campo | Tipo |
|---|---|
| `fromGroupId` | `string` |
| `toGroupId` | `string` |
| `weight` | `double` |

**`policy`** — obrigatório. Todos os campos obrigatórios:

| Campo | Valores disponíveis |
|---|---|
| `anchorStrategy` | `LAST_GROUP_AT_SAFE_AREA_END` |
| `lineHeightStrategy` | `MAX_EXACT_LINE_HEIGHT` |
| `spacerStylePolicy` | `NEXT_GROUP_STYLE` \| `PREVIOUS_GROUP_STYLE` |
| `safetyPolicy` | `MARGIN_BASED` |

---

### `ruleType: BODY_CONTENT`

Componente de conteúdo textual estruturado em seções recursivas com blocos e inlines. Suporta
parágrafos, citações, figuras, tabelas, listas, equações, notas de rodapé e cross-references.

```json
"meu-componente": {
  "ruleType": "BODY_CONTENT",
  "componentId": "meu-componente",
  "styleMapping": { ... },
  "numbering": { ... },
  "layout": { ... },
  "figure": { ... },
  "table": { ... },
  "frame": { ... },
  "codeListing": { ... },
  "chart": { ... },
  "citationFormatting": { ... },
  "crossReferenceLabels": { ... }
}
```

O engine só exige os sub-objetos que o profile declara — um profile que não suporta figuras não
precisa declarar `figure`.

#### `styleMapping`

| Campo | Tipo | Obrigatório |
|---|---|---|
| `sectionTitleStyleIdsByLevel` | `List<string>` | sim, não vazio |
| `paragraphStyleId` | `string` | não |
| `directShortQuoteStyleId` | `string` | não |
| `directLongQuoteStyleId` | `string` | não |
| `indirectCitationStyleId` | `string` | não |
| `citationOfCitationStyleId` | `string` | não |
| `listOrderedStyleId` | `string` | não |
| `listUnorderedStyleId` | `string` | não |
| `equationStyleId` | `string` | não |
| `footnoteCallStyleId` | `string` | não |
| `footnoteTextStyleId` | `string` | não |

#### `numbering`

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `enabled` | `boolean` | sim | Numeração automática de seções |
| `separator` | `string` | não | Separador entre níveis (ex.: `"."` gera `1.2.3`) |
| `primarySuffix` | `string` | não | Sufixo após o número do nível primário |

#### `layout`

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `blankLinesBeforeSectionTitleWhenPrecededByContent` | `int` | sim | Linhas em branco antes de título com conteúdo acima |
| `blankLinesAfterSectionTitle` | `int` | sim | Linhas em branco após título |
| `pageBreakBeforePrimarySection` | `boolean` | sim | Quebra de página antes de seção de nível 1 |
| `blankLineStyleId` | `string` | não | Estilo das linhas em branco inseridas pelo engine |

#### `figure`

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `captionStyleId` | `string` | sim | |
| `sourceStyleId` | `string` | sim | |
| `captionTemplate` | `string` | sim | Deve conter `{number}` e `{caption}` |
| `sourceTemplate` | `string` | sim | Deve conter `{source}` |
| `continuationLabels` | objeto | sim | `{ "first": "...", "middle": "...", "last": "..." }` |
| `sourcePlacement` | `LAST_PART_ONLY` \| `EVERY_PART` | sim | |
| `imageAlignment` | `LEFT` \| `CENTER` \| `RIGHT` | sim | |
| `maxWidthCm` | `double` > 0 | sim | |
| `maxHeightCm` | `double` > 0 | sim | |
| `defaultDpi` | `double` > 0 | sim | DPI para imagens sem metadado de resolução |
| `maxImageBytes` | `int` > 0 | sim | |
| `urlFetchTimeoutSeconds` | `int` > 0 | sim | |
| `fitPolicy` | `SCALE_DOWN_PRESERVE_ASPECT_RATIO` | sim | |
| `numberingStrategy` | `GLOBAL_SEQUENTIAL` \| `BY_CHAPTER` | sim | |
| `label` | `string` | sim | Label para cross-references |
| `separator` | `string` | não | Separador entre número do capítulo e sequencial quando `BY_CHAPTER` |

#### `table`

Mesmos campos de `figure`, exceto `imageAlignment`, `maxWidthCm`, `maxHeightCm`, `defaultDpi`,
`maxImageBytes`, `urlFetchTimeoutSeconds`, `fitPolicy`. Campos adicionais:

| Campo | Tipo | Obrigatório |
|---|---|---|
| `headerStyleId` | `string` | sim |
| `cellStyleId` | `string` | sim |
| `tableAlignment` | `LEFT` \| `CENTER` \| `RIGHT` | não |
| `widthPercent` | `double` | não |
| `repeatHeaderOnPageBreak` | `boolean` | não |

`frame` tem estrutura idêntica a `table`.

#### `codeListing`

| Campo | Tipo | Obrigatório |
|---|---|---|
| `captionStyleId` | `string` | sim |
| `sourceStyleId` | `string` | sim |
| `codeStyleId` | `string` | não |
| `captionTemplate` | `string` | sim — deve conter `{number}` e `{caption}` |
| `sourceTemplate` | `string` | sim — deve conter `{source}` |
| `continuationLabels` | objeto | sim |
| `sourcePlacement` | `enum` | sim |
| `numberingStrategy` | `enum` | sim |
| `label` | `string` | sim |
| `separator` | `string` | não |

#### `chart`

Campos de legenda/fonte/continuação de `figure` (sem os campos de imagem) mais:

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `imageRule` | objeto | sim | Sub-objeto com todos os campos de `figure` |

#### `citationFormatting`

Todos os campos são opcionais.

```json
"citationFormatting": {
  "pagePrefix": "p.",
  "multiAuthorJoiner": "; ",
  "etAl": " et al.",
  "apudConnector": " apud ",
  "suppressionMarker": "[...]",
  "emphasisOursLabel": "grifo nosso",
  "emphasisAuthorLabel": "grifo do autor",
  "verbalCitationLabel": "informação verbal"
}
```

#### `crossReferenceLabels`

Todos os campos são opcionais.

```json
"crossReferenceLabels": {
  "sectionLabel":     "Seção",
  "figureLabel":      "Figura",
  "tableLabel":       "Tabela",
  "frameLabel":       "Quadro",
  "chartLabel":       "Gráfico",
  "codeListingLabel": "Listagem",
  "equationLabel":    "Equação"
}
```

---

### `ruleType: REFERENCE_LIST`

Componente de lista de referências bibliográficas com formatação declarativa por tipo de entrada.

```json
"meu-componente": {
  "ruleType": "REFERENCE_LIST",
  "componentId": "meu-componente",
  "headingStyleId": "...",
  "headingText": "...",
  "entryStyleId": "...",
  "blankLinesBetweenEntries": 1,
  "blankLinesAfterHeading": 1,
  "formattingRule": {
    "authorFormat": { ... },
    "entryFormats": { "BOOK": [ ... ], ... }
  }
}
```

| Campo | Obrigatório |
|---|---|
| `componentId` | sim |
| `headingStyleId` | sim |
| `headingText` | sim |
| `entryStyleId` | sim |
| `blankLinesBetweenEntries` | sim |
| `formattingRule` | sim |
| `blankLinesAfterHeading` | não (padrão 0) |

#### `formattingRule.authorFormat`

| Campo | Obrigatório | Descrição |
|---|---|---|
| `surnameUppercase` | sim | |
| `etAlThreshold` | sim | A partir de quantos autores usar "et al." |
| `surnameGivenSeparator` | não | Separador entre sobrenome e prenome |
| `nameTerminator` | não | Terminador após o nome |
| `multiAuthorJoiner` | não | Separador entre autores |
| `etAlLabel` | não | Texto do "et al." |

#### `formattingRule.entryFormats`

Mapa de tipo de referência para lista de segmentos. Se o conteúdo tiver uma entrada de um tipo
que não está neste mapa, o formatter retorna 400.

Cada segmento:

```json
{ "source": "title", "bold": true, "prefix": "", "suffix": ".", "optional": false }
```

| Campo | Tipo | Descrição |
|---|---|---|
| `source` | `string` | Campo da entrada (ver tabela abaixo), ou `"literal:TEXTO"` para texto fixo |
| `bold` | `boolean` | Segmento em negrito |
| `prefix` | `string` | Texto antes do valor |
| `suffix` | `string` | Texto após o valor |
| `optional` | `boolean` | Se `true` e campo ausente, segmento omitido. Se `false` e ausente, retorna 400 |

Fontes disponíveis em `source`:

`authors`, `bookAuthors`, `title`, `subtitle`, `bookTitle`, `edition`, `city`, `publisher`,
`year`, `pages`, `url`, `accessDate`, `volume`, `issue`, `doi`, `degree`, `institutionName`

**Transformações automáticas** aplicadas pelo engine (não declarar no dado):
- `doi` → o engine prefixa `https://doi.org/` automaticamente. Fornecer apenas o identificador
  (ex.: `10.1000/xyz123`).
- `bookAuthors` → o engine sufija ` (org.). ` automaticamente após os nomes formatados.

---

### `ruleType: FLOW_TEXTUAL`

Componente de fluxo linear de itens declarados no profile. O profile declara a sequência de
itens — heading, texto simples, texto com template, lista de pares, tabela, keywords, grupos
repetidos — e o engine os renderiza em ordem. É o mecanismo mais flexível para componentes
textuais que não precisam de estrutura de seções recursivas.

O conteúdo é um mapa de slots cujos nomes e tipos são declarados pelos `FlowItem`s da rule.

```json
"meu-componente": {
  "ruleType": "FLOW_TEXTUAL",
  "componentId": "meu-componente",
  "items": [ ... ]
}
```

#### Tipos de `FlowItem`

| Tipo | Descrição | Campos obrigatórios |
|---|---|---|
| `HEADING` | Parágrafo fixo com texto do profile | `styleId`, `text` |
| `BLANK_LINES` | N linhas em branco semânticas | `styleId`, `count` |
| `PLAIN_TEXT` | Parágrafo cujo texto vem de um slot | `styleId`, `slotName` |
| `TEMPLATED_TEXT` | Parágrafo montado de template com slots | `styleId`, `template`, `fieldNames` |
| `BOLD_LABELED_KEYWORDS` | Label em negrito + lista de palavras-chave | `styleId`, `labelSlotName`, `keywordsSlotName`, `separator`, `terminator` |
| `PAIR_LIST` | Lista de pares termo/definição | `styleId`, `termsSlotName`, `definitionsSlotName`, `separator` |
| `TABLE_BLOCK` | Tabela com cabeçalhos do profile e linhas do conteúdo | `headerStyleId`, `cellStyleId`, `headers`, `rowsSlotName` |
| `REPEAT_GROUP` | Repete um grupo de itens para cada entrada de uma lista | `entriesSlotName`, `pageBreakBetweenEntries`, `group` |

**Conteúdo:** mapa livre de slots (`Map<string, ContentValue>`). As chaves e tipos esperados são
determinados pelos `FlowItem`s declarados na rule — não há schema fixo no engine.

---

### `ruleType: SECTIONED`

Componente com múltiplos itens sequenciais, cada um com letra gerada automaticamente (A, B,
C…), título e seções internas com a mesma estrutura do `BODY_CONTENT`.

```json
"meu-componente": {
  "ruleType": "SECTIONED",
  "componentId": "meu-componente",
  "headingTemplate": "{letter} — {title}",
  "headingStyleId": "...",
  "paragraphStyleId": "...",
  "sectionTitleStyleIdsByLevel": ["estilo-nivel-1", "estilo-nivel-2"]
}
```

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `componentId` | `string` | sim | |
| `headingTemplate` | `string` | sim | `{letter}` = A, B, C…; `{title}` = título do item |
| `headingStyleId` | `string` | sim | |
| `paragraphStyleId` | `string` | sim | |
| `sectionTitleStyleIdsByLevel` | `List<string>` | sim, não vazio | |

**Conteúdo esperado:** `items` (lista de objetos com `title` obrigatório e `sections` opcional —
mesma estrutura que o conteúdo de `BODY_CONTENT`)

---

### `ruleType: ELEMENT_INDEX`

Componente cujo conteúdo é derivado automaticamente de elementos textuais coletados de outro
componente durante o Phase0. Não requer dados no campo `document` da requisição.

`elementType` declara qual tipo de elemento textual coletar. É legítimo para o profile declarar
esse tipo porque o engine conhece os tipos de elemento textual como mecanismo — não como
conceito de domínio.

```json
"meu-componente": {
  "ruleType": "ELEMENT_INDEX",
  "componentId": "meu-componente",
  "elementType": "FIGURE",
  "sourceComponentId": "meu-conteudo",
  "headingStyleId": "...",
  "headingText": "...",
  "entryStyleId": "...",
  "entryTemplate": "{number} — {caption}",
  "blankLinesAfterHeading": 0
}
```

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `componentId` | `string` | sim | |
| `elementType` | `enum` | sim | `FIGURE` \| `TABLE` \| `FRAME` \| `CHART` \| `CODE_LISTING` |
| `sourceComponentId` | `string` | sim | ID do componente `BODY_CONTENT` ou `SECTIONED` de onde derivar |
| `headingStyleId` | `string` | sim | |
| `headingText` | `string` | sim | |
| `entryStyleId` | `string` | sim | |
| `entryTemplate` | `string` | sim | Deve conter `{number}` e `{caption}` |
| `blankLinesAfterHeading` | `int` | não (padrão 0) | |

`sourceComponentId` deve referenciar um componente com `ruleType` `BODY_CONTENT` ou `SECTIONED`.
Referência incompatível retorna 400 ao carregar o profile.

---

### `ruleType: SECTION_INDEX`

Componente cujo conteúdo é derivado automaticamente da estrutura de seções de outro componente.
Não requer dados no campo `document` da requisição.

```json
"meu-componente": {
  "ruleType": "SECTION_INDEX",
  "componentId": "meu-componente",
  "sourceComponentId": "meu-conteudo",
  "headingStyleId": "...",
  "headingText": "...",
  "entryStyleIdsByLevel": ["estilo-nivel-1", "estilo-nivel-2"],
  "useTocField": false
}
```

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `componentId` | `string` | sim | |
| `sourceComponentId` | `string` | sim | ID do componente `BODY_CONTENT` de onde derivar |
| `headingStyleId` | `string` | sim | |
| `headingText` | `string` | sim | |
| `entryStyleIdsByLevel` | `List<string>` | sim, não vazio | Índice 0 = nível 1 |
| `useTocField` | `boolean` | não | Usa campo TOC do DOCX — atualizado pelo pós-processamento |

`sourceComponentId` deve referenciar um componente com `ruleType` `BODY_CONTENT`. Referência
incompatível retorna 400 ao carregar o profile.

---

## Exemplos de componentRules — profile ABNT UNIP

Os exemplos abaixo mostram como o profile ABNT UNIP usa os tipos de rule acima. **Nenhum desses
IDs é obrigatório, reconhecido pelo engine, ou reservado.** São escolhas deste profile específico
e servem como referência de como mapear um `ruleType` a um componente concreto.

| ID no profile ABNT UNIP | `ruleType` |
|---|---|
| `cover` | `SINGLE_PAGE` |
| `titlePage` | `SINGLE_PAGE` |
| `approvalSheet` | `SINGLE_PAGE` |
| `bodyContent` | `BODY_CONTENT` |
| `references` | `REFERENCE_LIST` |
| `dedication` | `FLOW_TEXTUAL` |
| `acknowledgments` | `FLOW_TEXTUAL` |
| `epigraph` | `FLOW_TEXTUAL` |
| `resumo` | `FLOW_TEXTUAL` |
| `abstract` | `FLOW_TEXTUAL` |
| `errata` | `FLOW_TEXTUAL` |
| `glossary` | `FLOW_TEXTUAL` |
| `listOfSymbols` | `FLOW_TEXTUAL` |
| `listOfAbbreviations` | `FLOW_TEXTUAL` |
| `listOfFigures` | `ELEMENT_INDEX` — `elementType: FIGURE` |
| `listOfTables` | `ELEMENT_INDEX` — `elementType: TABLE` |
| `listOfFrames` | `ELEMENT_INDEX` — `elementType: FRAME` |
| `listOfCharts` | `ELEMENT_INDEX` — `elementType: CHART` |
| `listOfCodeListings` | `ELEMENT_INDEX` — `elementType: CODE_LISTING` |
| `summary` | `SECTION_INDEX` |
| `appendix` | `SECTIONED` |
| `annex` | `SECTIONED` |

---

## Input 2 — Preferences (`options`)

```json
"options": {
  "selectedComponents": ["cover", "titlePage", "bodyContent", "references"]
}
```

IDs devem existir no `componentOrder` do profile — caso contrário retorna 400.
Lista vazia renderiza todos os componentes que têm conteúdo em `document`.

---

## Input 3 — Content (`document`)

O campo `document` é um mapa onde cada chave é o ID de um componente declarado no profile. O
formato do valor de cada chave é determinado pelo tipo de rule do componente correspondente —
não pelo ID em si. O engine não conhece os IDs antecipadamente.

As subseções abaixo documentam o formato de conteúdo de cada tipo de rule, usando os IDs do
profile ABNT UNIP como exemplo. Um profile diferente com IDs distintos usaria os mesmos formatos
de conteúdo desde que os tipos de rule sejam os mesmos.

---

## Exemplos de conteúdo — profile ABNT UNIP

### cover (exemplo — `SinglePageComponentRule`)

```json
"cover": {
  "institutionalLines": ["UNIVERSIDADE FICTÍCIA DE LIMEIRA", "CURSO DE ADS"],
  "authors": ["PESSOA AUTORA TESTE 01"],
  "title": "TÍTULO DO TRABALHO EM MAIÚSCULAS",
  "subtitle": "Subtítulo opcional",
  "city": "Limeira",
  "year": "2026"
}
```

As chaves do mapa correspondem aos nomes dos slots declarados em `componentRules.cover.slots`.
Samples: `docs/samples/cover/`

### titlePage (exemplo — `SinglePageComponentRule`)

```json
"titlePage": {
  "authors": ["Pessoa Autora Teste 01"],
  "title": "Título do Trabalho",
  "subtitle": "Subtítulo opcional",
  "nature": {
    "workType": "Trabalho de Conclusão de Curso",
    "degreeObjective": "obtenção do título de tecnólogo",
    "courseName": "Análise e Desenvolvimento de Sistemas",
    "institutionName": "Universidade Fictícia de Limeira"
  },
  "advisor":   { "academicTitle": "Prof. Dr.", "name": "Pessoa Orientadora Teste" },
  "coadvisor": { "academicTitle": "Profa. Ma.", "name": "Pessoa Coorientadora Teste" },
  "city": "Limeira",
  "year": "2026"
}
```

Samples: `docs/samples/title-page/`

### approvalSheet (exemplo — `SinglePageComponentRule`)

```json
"approvalSheet": {
  "authors": ["Pessoa Autora Teste 01"],
  "title": "Título do Trabalho",
  "nature": { ... },
  "approvalText": "Aprovado(a) em: ______/______/______",
  "committeeHeading": "BANCA EXAMINADORA",
  "committeeMembers": [
    {
      "name": "Pessoa Orientadora Teste",
      "title": "Prof. Dr.",
      "institutionName": "Universidade Fictícia de Limeira",
      "role": "Orientador"
    }
  ]
}
```

Samples: `docs/samples/approval-sheet/`

---

### bodyContent (exemplo — `BodyContentRule`)

```json
"bodyContent": {
  "sections": [
    {
      "id": "introducao",
      "level": 1,
      "title": "Introdução",
      "blocks": [ ... ],
      "subsections": [ ... ]
    }
  ]
}
```

`sections` — obrigatório, não vazio.

**Campos de seção:**

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `id` | `string` | sim | |
| `level` | `int` ≥ 1 | sim | Nível hierárquico da seção. O nível máximo permitido é declarado no profile. |
| `title` | `string` | não | Seção sem título é válida |
| `blocks` | lista | não | Blocos de conteúdo da seção |
| `subsections` | lista de seções | não | Sub-seções recursivas |

#### Tipos de bloco (`blocks[].type`)

| Tipo | Campos obrigatórios | Campos opcionais |
|---|---|---|
| `PARAGRAPH` | `content` (lista de inlines) OU `text` (string simples) | — |
| `DIRECT_LONG_QUOTE` | `text`, `mode` | `source`, `originalSource`, `consultedSource`, `markers` |
| `FIGURE` | `figure` | — |
| `TABLE` | `table` | — |
| `FRAME` | `frame` | — |
| `CHART` | `chart` | — |
| `CODE_LISTING` | `codeListing` | — |
| `EQUATION` | `equation` | — |
| `ORDERED_LIST` | `list` | — |
| `UNORDERED_LIST` | `list` | — |

`mode` em `DIRECT_LONG_QUOTE`: `PARENTHETICAL` \| `NARRATIVE`

#### Estrutura de `FIGURE`

```json
{
  "type": "FIGURE",
  "figure": {
    "id": "fig-1",
    "caption": "Diagrama do sistema",
    "source": "Elaboração própria",
    "image": {
      "sourceType": "DATA_URI",
      "dataUri": "data:image/png;base64,...",
      "altText": "Diagrama"
    },
    "continuationGroupId": null
  }
}
```

| Campo | Obrigatório | Descrição |
|---|---|---|
| `id` | não | Usado em cross-references |
| `caption` | não | |
| `source` | não | |
| `image` | sim | Sub-objeto de imagem |
| `continuationGroupId` | não | Agrupa partes de uma mesma figura que quebra entre páginas |

**`image`:**

| Campo | Obrigatório | Descrição |
|---|---|---|
| `sourceType` | sim | `DATA_URI` \| `URL` |
| `dataUri` | quando `DATA_URI` | String base64 com prefixo `data:image/...;base64,...` |
| `url` | quando `URL` | URL pública da imagem |
| `altText` | não | Texto alternativo |

#### Estrutura de `TABLE` e `FRAME`

```json
{
  "type": "TABLE",
  "table": {
    "id": "tab-1",
    "caption": "Comparativo de resultados",
    "source": "Elaboração própria",
    "columns": [
      { "header": "Método" },
      { "header": "Precisão" }
    ],
    "rows": [
      { "cells": [
        { "text": "Algoritmo A", "colspan": 1, "rowspanStart": false, "rowspanContinuation": false },
        { "text": "95%",         "colspan": 1, "rowspanStart": false, "rowspanContinuation": false }
      ]}
    ],
    "continuationGroupId": null
  }
}
```

**`columns`:** lista de `{ "header": "string" }`. `header` pode ser null (coluna sem cabeçalho).

**`cells`:**

| Campo | Tipo | Padrão | Descrição |
|---|---|---|---|
| `text` | `string` | — | Conteúdo da célula |
| `colspan` | `int` | 1 | Número de colunas que a célula ocupa |
| `rowspanStart` | `boolean` | `false` | Célula inicia um span vertical |
| `rowspanContinuation` | `boolean` | `false` | Célula é continuação de um span vertical |

`FRAME` tem a mesma estrutura de `TABLE`. `columns` e `rows` são obrigatórios e não vazios em
`FRAME`.

#### Estrutura de `CODE_LISTING`

```json
{
  "type": "CODE_LISTING",
  "codeListing": {
    "id": "lst-1",
    "caption": "Algoritmo de ordenação",
    "language": "python",
    "code": "def sort(arr):\n    return sorted(arr)",
    "source": "Elaboração própria",
    "continuationGroupId": null
  }
}
```

| Campo | Obrigatório |
|---|---|
| `id` | sim |
| `caption` | sim |
| `code` | sim |
| `language` | não |
| `source` | não |
| `continuationGroupId` | não |

#### Estrutura de `CHART`

```json
{
  "type": "CHART",
  "chart": {
    "id": "grfico-1",
    "caption": "Distribuição dos resultados",
    "source": "Elaboração própria",
    "image": { "sourceType": "DATA_URI", "dataUri": "..." },
    "continuationGroupId": null
  }
}
```

`id` e `caption` obrigatórios. `source` opcional.

#### Estrutura de `EQUATION`

```json
{
  "type": "EQUATION",
  "equation": {
    "text": "E = mc^2",
    "label": "eq-energia"
  }
}
```

`text` obrigatório. `label` opcional — usado em cross-references.

#### Estrutura de `ORDERED_LIST` e `UNORDERED_LIST`

```json
{
  "type": "ORDERED_LIST",
  "list": {
    "type": "ORDERED",
    "items": [
      {
        "content": [ { "type": "TEXT", "text": "Primeiro item" } ],
        "subList": null
      }
    ]
  }
}
```

`items` — obrigatório, não vazio. Cada item tem `content` (lista de inlines, obrigatório, não
vazio) e `subList` (lista aninhada, opcional).

---

#### Tipos de inline dentro de `PARAGRAPH.content` e outros contextos

**`TEXT`**

```json
{ "type": "TEXT", "text": "Palavra", "formatting": { "bold": true, "italic": false, "underline": false, "superscript": false, "subscript": false } }
```

`text` é o único campo obrigatório. `formatting` opcional — todos os seus campos são booleans
opcionais: `bold`, `italic`, `underline`, `superscript`, `subscript`.

**`ABBREVIATION`**

```json
{ "type": "ABBREVIATION", "text": "ABNT", "expansion": "Associação Brasileira de Normas Técnicas" }
```

`text` e `expansion` obrigatórios. Alimenta automaticamente qualquer componente com `ruleType`
`FLOW_TEXTUAL` que use um `FlowItem` do tipo `PAIR_LIST` com `termsSlotName: "$abbreviations"`.

**`FOOTNOTE`**

```json
{
  "type": "FOOTNOTE",
  "content": [
    { "type": "TEXT", "text": "Texto da nota de rodapé." }
  ]
}
```

`content` obrigatório, não vazio. Lista de inlines recursivos.

**`QUOTE_TEXT`**

```json
{
  "type": "QUOTE_TEXT",
  "quoteType": "SHORT",
  "text": "texto citado entre aspas",
  "markers": [ ... ]
}
```

`text` obrigatório. `quoteType` opcional (único valor: `SHORT`). `markers` opcional.

**`CITATION`**

```json
{
  "type": "CITATION",
  "citationType": "INDIRECT",
  "mode": "PARENTHETICAL",
  "source": {
    "authors": [ { "type": "PERSON", "surname": "Lima", "displayName": null } ],
    "year": "2021",
    "page": null
  }
}
```

`citationType` e `mode` obrigatórios. `source`, `originalSource`, `consultedSource` opcionais.

`citationType`:

| Valor | Descrição |
|---|---|
| `DIRECT_SHORT` | Citação direta curta (inline com aspas) |
| `DIRECT_LONG` | Citação direta longa (bloco recuado) |
| `INDIRECT` | Paráfrase |
| `CITATION_OF_CITATION` | Apud |
| `VERBAL` | Informação verbal |

`mode`: `PARENTHETICAL` (autor, ano) \| `NARRATIVE` (autor (ano))

**`CitationSource`:**

| Campo | Obrigatório | Descrição |
|---|---|---|
| `authors` | não | Lista de `CitationAuthor` |
| `year` | sim | |
| `page` | não | |

**`CitationAuthor`:**

| Campo | Obrigatório | Descrição |
|---|---|---|
| `type` | sim | `PERSON` \| `ORGANIZATION` \| `TITLE` |
| `surname` | não | Para `PERSON` |
| `organizationName` | não | Para `ORGANIZATION` |
| `displayName` | não | Para `TITLE` ou forma de exibição customizada |
| `title` | não | Título acadêmico ou profissional |

**`CROSS_REFERENCE`**

```json
{
  "type": "CROSS_REFERENCE",
  "crossReference": {
    "targetId": "fig-1",
    "targetType": "FIGURE",
    "displayMode": "LABEL_AND_NUMBER"
  }
}
```

Todos os campos obrigatórios.

`targetType`: `SECTION` \| `FIGURE` \| `TABLE` \| `FRAME` \| `CHART` \| `CODE_LISTING` \| `EQUATION`

`displayMode`: `NUMBER_ONLY` \| `LABEL_AND_NUMBER` \| `CAPTION`

---

#### `BodyQuoteMarker`

Usado em `QUOTE_TEXT.markers` e `DIRECT_LONG_QUOTE.markers` para marcar supressões, interpolações
e ênfases dentro do texto citado.

```json
{ "type": "SUPPRESSION", "position": 15, "endPosition": 42 }
```

| Campo | Obrigatório | Descrição |
|---|---|---|
| `type` | sim | `SUPPRESSION` \| `INTERPOLATION` \| `EMPHASIS_OURS` \| `EMPHASIS_AUTHOR` |
| `position` | sim | Posição de início no texto (índice de caractere) |
| `endPosition` | não | Posição de fim (para marcadores de intervalo) |

---

### references (exemplo — `ReferenceListRule`)

```json
"references": {
  "entries": [
    {
      "id": "ref1",
      "type": "BOOK",
      "authors": [{ "surname": "Lima", "givenNames": "Carlos Eduardo" }],
      "title": "Fundamentos de Sistemas Distribuídos",
      "year": "2021",
      "city": "São Paulo",
      "publisher": "Editora Exemplo"
    }
  ]
}
```

`entries` obrigatório, não vazio.

**Campos obrigatórios em toda entrada:** `id`, `type`, `title`, `year`.

**`authors`** — lista de `{ "surname": "string" (obrigatório), "givenNames": "string" (opcional) }`.

**Campos opcionais por tipo:**

| Campo | Tipos que usam |
|---|---|
| `authors` | Todos exceto `LEGISLATION` |
| `subtitle` | `BOOK`, `BOOK_CHAPTER`, `JOURNAL`, `THESIS`, `WEBSITE`, `REPORT`, `STANDARD` |
| `city` | `BOOK`, `BOOK_CHAPTER`, `JOURNAL`, `THESIS`, `CONFERENCE_PAPER`, `REPORT`, `STANDARD` |
| `publisher` | `BOOK`, `BOOK_CHAPTER`, `JOURNAL`, `CONFERENCE_PAPER`, `REPORT` |
| `edition` | `BOOK`, `BOOK_CHAPTER` |
| `volume` | `JOURNAL` |
| `issue` | `JOURNAL`, `REPORT` |
| `pages` | `BOOK_CHAPTER`, `JOURNAL`, `CONFERENCE_PAPER` |
| `doi` | `JOURNAL` — fornecer apenas o identificador, o engine prefixa `https://doi.org/` |
| `url` | `WEBSITE`, `LEGISLATION` |
| `accessDate` | `WEBSITE` |
| `degree` | `THESIS` |
| `institutionName` | `THESIS`, `REPORT` |
| `bookTitle` | `BOOK_CHAPTER` |
| `bookAuthors` | `BOOK_CHAPTER` — não incluir "(org.)" nos nomes, o engine adiciona automaticamente |

Sample: `docs/samples/references/references-mixed.json`

---

### Exemplos de conteúdo — `FLOW_TEXTUAL`

O conteúdo de um componente `FLOW_TEXTUAL` é um mapa de slots cujas chaves são definidas pelos
`FlowItem`s declarados na rule do profile. Os exemplos abaixo usam os IDs do profile ABNT UNIP.

```json
"dedication":      { "text": "Dedico este trabalho à minha família." }

"acknowledgments": { "text": "Agradeço à orientadora e à família." }

"epigraph": {
  "text":   "A frase da epígrafe.",
  "author": "Pessoa Autora",
  "source": "Obra, 2020"
}

"resumo": {
  "text":          "Texto do resumo.",
  "keywordsLabel": "Palavras-chave:",
  "keywords":      ["palavra1", "palavra2", "palavra3"]
}

"abstract": {
  "entries": [
    {
      "headingText":   "ABSTRACT",
      "text":          "Abstract text.",
      "keywords":      ["keyword1", "keyword2"],
      "keywordsLabel": "Keywords:"
    }
  ]
}

"errata": {
  "rows": [
    ["23", "5", "analize", "análise"]
  ]
}

"glossary": {
  "terms":       ["API", "TCC"],
  "definitions": ["Interface de Programação de Aplicações.", "Trabalho de Conclusão de Curso."]
}

"listOfSymbols": {
  "terms":       ["λ", "μ"],
  "definitions": ["Comprimento de onda.", "Coeficiente de atrito."]
}
```

As chaves de cada mapa (`text`, `author`, `rows`, `terms`, etc.) são os `slotName`s declarados
nos `FlowItem`s da rule correspondente — o engine não conhece esses nomes antecipadamente.

---

### Exemplos de conteúdo — `ELEMENT_INDEX` e `SECTION_INDEX`

Componentes com `ruleType` `ELEMENT_INDEX` ou `SECTION_INDEX` derivam seu conteúdo de outro
componente e não requerem dados no campo `document`. Para sinalizá-los como presentes, usa-se
objeto vazio:

```json
"listOfFigures":      {}
"listOfTables":       {}
"listOfFrames":       {}
"listOfCharts":       {}
"listOfCodeListings": {}
"summary":            {}
"listOfAbbreviations": {}
```

Os IDs acima são exemplos do profile ABNT UNIP. O padrão é o mesmo para qualquer componente
derivado — o ID é definido pelo profile, não pelo engine.

---

### Exemplos de conteúdo — `SECTIONED`

```json
"appendix": {
  "items": [
    {
      "title": "Título do Item A",
      "sections": [
        {
          "id": "apx-s1",
          "level": 1,
          "title": "Subseção interna",
          "blocks": [ ... ]
        }
      ]
    }
  ]
}
```

`items` obrigatório, não vazio. Cada item tem `title` (obrigatório) e `sections` (opcional —
mesma estrutura que o conteúdo de `BODY_CONTENT`).

Samples: `docs/samples/appendix/`, `docs/samples/annex/`

---

## O que o work-service precisa saber

Persistir o campo `document` como blob (JSONB ou equivalente). Extrair para colunas estruturadas
apenas os campos necessários para busca — os IDs e campos relevantes dependem do profile usado.

O `document` é um mapa aberto — as chaves são IDs de componentes definidos pelo profile, não
pelo engine. O work-service não deve assumir que existem chaves fixas como `cover` ou `titlePage`.

Não persistir o campo `work` — é ignorado pelo engine.

---

## O que o profile-service precisa saber

Armazenar e entregar perfis por `profileId`. A integração futura:

```
GET /profiles/{profileId}  →  JSON do profile completo
```

O JSON entregue deve seguir exatamente o formato descrito neste documento. O profile de
referência está em `src/main/resources/profiles/abnt-unip-profile.json`.

**Atenção:** `postProcessing` só é executado quando o profile vem do profile-service (ou do
classpath). Perfis enviados inline na requisição ignoram `postProcessing`.

O formato do profile está estável. Novos perfis (APA, Vancouver, outros) declaram seus próprios
`styleRules` e `componentRules` com qualquer combinação de `ruleType`s e IDs arbitrários, sem
alterar código Java.
