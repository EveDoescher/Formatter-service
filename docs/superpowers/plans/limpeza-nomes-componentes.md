# Plano — Eliminação de Nomes de Componentes do Motor

## Contexto

O engine não deve conhecer nenhum componente por nome. O que ele conhece são mecanismos —
ferramentas genéricas de renderização. O profile declara quais componentes existem, quais elementos
cada um contém, e como cada elemento textual deve ser formatado caso apareça.

O que existe hoje viola isso: há tipos Java, pastas, e um enum com nomes de componentes ABNT
(`appendix`, `annex`, `summary`, `listOfFigures`, etc.) embutidos no motor.

---

## O que deve existir no motor

**Componentes** — o profile os declara, têm ID, aparecem no `componentOrder`.

**Elementos** — o profile os declara dentro de um componente. Têm ID e formatação própria.
Exemplos: `título` dentro da capa — centralizado, negrito, tamanho 12. São representados
internamente pelos slots, FlowItems e grupos que o engine já usa.

**Elementos textuais** — figuras, tabelas, imagens, citações, equações. Inseridos pelo content
dentro de um componente. O profile não declara que vão existir — declara como formatá-los se
existirem. São tipos de mecanismo que o engine conhece legitimamente.

---

## Violações atuais

### `ComponentType` enum

Contém nomes de componentes ABNT:

```
APPENDIX, ANNEX, SUMMARY,
LIST_OF_FIGURES, LIST_OF_TABLES, LIST_OF_FRAMES, LIST_OF_CHARTS, LIST_OF_CODE_LISTINGS
```

Deve ficar apenas com tipos de mecanismo:

```
SINGLE_PAGE, FLOW_TEXTUAL, BODY_CONTENT, REFERENCES,
SECTIONED, ELEMENT_INDEX, SECTION_INDEX
```

### Pastas com nomes de componentes em `document/component/`

| Pasta | Violação |
|---|---|
| `appendix/` | `AppendixComponent`, `AppendixItem` |
| `annex/` | `AnnexComponent`, `AnnexItem` |
| `summary/` | `SummaryComponent` |
| `listoffigures/` | `ListOfFiguresComponent` |
| `listoftables/` | `ListOfTablesComponent` |
| `listofframes/` | `ListOfFramesComponent` |
| `listofcharts/` | `ListOfChartsComponent` |
| `listofcodelistings/` | `ListOfCodeListingsComponent` |

### Pastas com nomes de componentes em `profile/model/component/`

| Pasta | Violação |
|---|---|
| `appendix/` | `AppendixComponentRule` — idêntica a `AnnexComponentRule` |
| `annex/` | `AnnexComponentRule` — idêntica a `AppendixComponentRule` |
| `summary/` | `SummaryComponentRule` |
| `indexlist/` | `IndexListComponentRule` — genérica mas sem `elementType` |

### Pastas com nomes de componentes em `rendering/component/`

| Pasta | Violação |
|---|---|
| `appendix/` | `AppendixRenderer` — hardcoda `COMPONENT_ID = "appendix"` |
| `annex/` | `AnnexRenderer` — hardcoda `COMPONENT_ID = "annex"` |
| `summary/` | `SummaryRenderer` — hardcoda `COMPONENT_ID = "summary"` |
| `listoffigures/` | `ListOfFiguresRenderer` — hardcoda `COMPONENT_ID = "listOfFigures"` |
| `listoftables/` | `ListOfTablesRenderer` — hardcoda `COMPONENT_ID = "listOfTables"` |
| `listofframes/` | `ListOfFramesRenderer` — hardcoda `COMPONENT_ID = "listOfFrames"` |
| `listofcharts/` | `ListOfChartsRenderer` — hardcoda `COMPONENT_ID = "listOfCharts"` |
| `listofcodelistings/` | `ListOfCodeListingsRenderer` — hardcoda `COMPONENT_ID = "listOfCodeListings"` |
| `indexlist/` | `AbstractIndexListRenderer` — abstração genérica mas só usada pelos 5 acima |

### `ComponentRendererRegistry`

Resolve `AnnexComponent`, `AppendixComponent`, `SummaryComponent`, `ListOf*Component` por
`component.getClass()`. Enquanto isso existir, cada tipo precisa de uma classe Java própria.
`FlowTextualContent` e `SinglePageContent` já escaparam disso porque carregam `componentId`.

---

## Solução por mecanismo

---

### Mecanismo 1 — `SectionedContent`

Substitui `AppendixComponent` + `AnnexComponent`. São estruturalmente idênticos: lista de itens
com título e seções internas. A única diferença é o ID e o template de heading — que são
declarados no profile.

**Novas classes:**

```
document/component/sectioned/
    SectionedContent(String componentId, List<SectionedItem> items)
    SectionedItem(String title, List<BodySection> sections)

profile/model/component/sectioned/
    SectionedComponentRule(
        String componentId,
        String headingTemplate,   // {letter} e {title} como marcadores
        String headingStyleId,
        String paragraphStyleId,
        List<String> sectionTitleStyleIdsByLevel
    )

rendering/component/sectioned/
    SectionedRenderer(String componentId)   // construtor injetado, igual a FlowTextualRenderer
```

**Elimina:**
- `document/component/appendix/`, `document/component/annex/`
- `profile/model/component/appendix/`, `profile/model/component/annex/`
- `rendering/component/appendix/`, `rendering/component/annex/`

---

### Mecanismo 2 — `SectionIndexContent`

Substitui `SummaryComponent`. Componente que agrega a estrutura de seções coletadas de outro
componente durante o Phase0. A lógica de TOC field e entryStyleIdsByLevel é genérica.

**Novas classes:**

```
document/component/sectionindex/
    SectionIndexContent(String componentId)

profile/model/component/sectionindex/
    SectionIndexComponentRule(
        String componentId,
        String headingStyleId,
        String headingText,
        List<String> entryStyleIdsByLevel,
        boolean useTocField
    )

rendering/component/sectionindex/
    SectionIndexRenderer(String componentId)   // construtor injetado
```

**Elimina:**
- `document/component/summary/`
- `profile/model/component/summary/`
- `rendering/component/summary/`

---

### Mecanismo 3 — `ElementIndexContent`

Substitui os 5 `ListOf*Component`. Componente que agrega elementos textuais de um tipo específico
coletados do Phase0. `ElementType` é legítimo porque referencia tipos de elementos textuais que
o engine conhece como mecanismo — não conceitos de domínio ABNT.

**Novo enum:**

```java
// profile/model/component/elementindex/
enum ElementType { FIGURE, TABLE, FRAME, CHART, CODE_LISTING }
```

**Novas classes:**

```
document/component/elementindex/
    ElementIndexContent(String componentId)

profile/model/component/elementindex/
    ElementIndexComponentRule(
        String componentId,
        ElementType elementType,       // qual coleção do Phase0 agregar
        String headingStyleId,
        String headingText,
        String entryStyleId,
        String entryTemplate,          // deve conter {number} e {caption}
        int blankLinesAfterHeading
    )

rendering/component/elementindex/
    ElementIndexRenderer(String componentId)   // construtor injetado, lê elementType da rule
```

O renderer resolve internamente qual coleção do Phase0 usar via switch em `elementType` — o
que é despacho de mecanismo, não conhecimento de domínio.

**Elimina:**
- `document/component/listoffigures/`, `listoftables/`, `listofframes/`, `listofcharts/`, `listofcodelistings/`
- `profile/model/component/indexlist/`
- `rendering/component/listoffigures/`, `listoftables/`, `listofframes/`, `listofcharts/`, `listofcodelistings/`
- `rendering/component/indexlist/`

---

## `ComponentRendererRegistry` — mudança necessária

Adicionar `SectionedContent`, `SectionIndexContent` e `ElementIndexContent` ao mesmo tratamento
especial que `FlowTextualContent` e `SinglePageContent` já têm: ler `componentId()` do objeto
em vez de resolver por `component.getClass()`.

Consequência: múltiplos componentes do mesmo mecanismo com IDs diferentes passam a funcionar
corretamente — dois sumários, dois conjuntos de apêndices, qualquer combinação.

---

## `ComponentType` enum — estado final

```java
public enum ComponentType {
    SINGLE_PAGE,
    FLOW_TEXTUAL,
    BODY_CONTENT,
    REFERENCES,
    SECTIONED,
    ELEMENT_INDEX,
    SECTION_INDEX
}
```

---

## Mecanismo 4 — Migração do `ProfileDefinition.ComponentRulesDefinition`

Hoje é um record Java com 22 campos nomeados por ID de componente ABNT:

```java
public record ComponentRulesDefinition(
    SinglePageComponentRuleDefinition cover,
    SinglePageComponentRuleDefinition titlePage,
    SinglePageComponentRuleDefinition approvalSheet,
    BodyContentComponentRuleDefinition bodyContent,
    ErrataComponentRuleDefinition errata,
    ResumoComponentRuleDefinition resumo,
    // ... 16 campos restantes, todos com nomes ABNT
)
```

O Jackson resolve cada campo por nome — significa que um profile com `"minhaCapa"` em vez de
`"cover"` é simplesmente ignorado. O engine hardcoda os 22 IDs ABNT na camada de desserialização.

**Solução:**

Substituir `ComponentRulesDefinition` por `Map<String, ComponentRuleDefinition>` onde cada entrada
tem um campo `"ruleType"` discriminador:

```json
"componentRules": {
  "minhaCapa": {
    "ruleType": "SINGLE_PAGE",
    "componentId": "minhaCapa",
    "slots": { ... },
    "styleMapping": { ... },
    "layoutRule": { ... }
  },
  "meuConteudo": {
    "ruleType": "BODY_CONTENT",
    "componentId": "meuConteudo",
    ...
  }
}
```

O Jackson usa `@JsonTypeInfo(use = Id.NAME, property = "ruleType")` + `@JsonSubTypes` para
desserializar cada entrada para a `ComponentRuleDefinition` correta. O `componentId` dentro do
objeto deve ser idêntico à chave do mapa — validado ao carregar o profile.

**Tipos discriminados:**

| `ruleType` | `ComponentRuleDefinition` |
|---|---|
| `SINGLE_PAGE` | `SinglePageComponentRuleDefinition` |
| `BODY_CONTENT` | `BodyContentComponentRuleDefinition` |
| `REFERENCES` | `ReferencesComponentRuleDefinition` |
| `FLOW_TEXTUAL` | vários tipos atuais já convertidos para este mecanismo |
| `SECTIONED` | `SectionedComponentRuleDefinition` |
| `ELEMENT_INDEX` | `ElementIndexComponentRuleDefinition` |
| `SECTION_INDEX` | `SectionIndexComponentRuleDefinition` |

**Elimina:**
- Os 22 campos nomeados de `ComponentRulesDefinition`
- Todas as `*ComponentRuleDefinition` com nomes ABNT que não correspondem a um mecanismo
  (`ErrataComponentRuleDefinition`, `ResumoComponentRuleDefinition`, etc.) — substituídas pela
  desserialização correta para o tipo de mecanismo correspondente
- A mesma limpeza se aplica ao `ComponentRulesRequest` na camada de API

---

## Mecanismo 5 — `ReferencesRenderer` e `DisplayObjectCollector`

### `ReferencesRenderer`

Hardcoda `COMPONENT_ID = "references"`. Precisa receber o ID pelo construtor, igual ao
`FlowTextualRenderer` e ao `SinglePageRenderer`.

```java
// antes
public static final String COMPONENT_ID = "references";
public String componentId() { return COMPONENT_ID; }

// depois
private final String componentId;
public ReferencesRenderer(String componentId) { this.componentId = componentId; }
public String componentId() { return componentId; }
```

### `DisplayObjectCollector`

Dois problemas:

1. Hardcoda `"bodyContent"` ao resolver a rule:
   ```java
   new ComponentRuleResolver(profile).resolve("bodyContent", BodyContentComponentRule.class);
   ```
   Com motor genérico, o componente de conteúdo pode ter qualquer ID. A solução é passar o
   `componentId` do `BodyContentComponent` processado em vez de fixar `"bodyContent"`.

2. Assume que existe exatamente um `BodyContentComponent` entre os componentes selecionados.
   Com motor genérico, podem existir vários. O collector deve iterar sobre todos os componentes
   do tipo `BODY_CONTENT` e agregar o Phase0 de cada um.

---

## Mecanismo 6 — `RenderingConfig`

Hoje declara um bean Spring por componente ABNT com ID hardcoded:

```java
@Bean public FlowTextualRenderer dedicationRenderer() { return new FlowTextualRenderer("dedication"); }
@Bean public FlowTextualRenderer resumoRenderer()     { return new FlowTextualRenderer("resumo"); }
// ... 18 beans similares
```

Com motor genérico e `ProfileDefinition` migrado para mapa polimórfico, os renderers deixam de
ser declarados por componente. O `ComponentRendererRegistry` passa a ser populado a partir dos
tipos de rule presentes no profile carregado — um renderer por tipo de mecanismo, instanciado
com o `componentId` correto lido do profile.

O `RenderingConfig` fica apenas com beans de infraestrutura: `TextMeasurer`, `ProfileProvider`,
`SinglePageLayoutEngine` e seus colaboradores, `DocumentRenderer`. Os beans de renderer por nome
de componente desaparecem.

---

## Mecanismo 7 — Camada de API (`dto/request/`)

`DocumentContentRequest` e a pasta `api/export/dto/request/` espelham a mesma violação do
`ProfileDefinition`: campos com nomes de componentes ABNT fixos.

```java
// DocumentContentRequest — hoje
public record DocumentContentRequest(
    SinglePageComponentRuleRequest cover,
    SinglePageComponentRuleRequest titlePage,
    ReferencesRequest references,
    AnnexRequest annex,
    SummaryRequest summary,
    // ... 17 campos fixos
)
```

**Solução:** migrar para mapa aberto, equivalente ao que o Mecanismo 4 faz no `ProfileDefinition`:

```java
public record DocumentContentRequest(
    Map<String, ComponentContentRequest> components
)
```

Onde `ComponentContentRequest` é polimórfico por `contentType` declarado no JSON:

```json
{
  "components": {
    "minhaCapa":    { "contentType": "SINGLE_PAGE", "slots": { ... } },
    "meuConteudo":  { "contentType": "BODY_CONTENT", "sections": [ ... ] },
    "minhasRefs":   { "contentType": "REFERENCES",   "entries": [ ... ] },
    "meuApendice":  { "contentType": "SECTIONED",    "items": [ ... ] }
  }
}
```

**Elimina:** `AnnexRequest`, `AppendixRequest`, `SummaryRequest`, `SummaryComponentRuleRequest`,
`AnnexComponentRuleRequest`, `AppendixComponentRuleRequest`, `AnnexItemRequest`,
`AppendixItemRequest`, e todos os outros `*Request` com nomes de componentes ABNT.

---

## Ordem de execução sugerida

1. `SectionedContent` — menor escopo, resultado imediato, elimina duplicação
2. `ElementIndexContent` — elimina 5 classes + AbstractIndexListRenderer
3. `SectionIndexContent` — elimina SummaryComponent/Rule/Renderer
4. `ComponentType` enum — limpeza após os três acima
5. `ComponentRendererRegistry` — ajuste transversal, feito em paralelo com cada mecanismo novo
6. `ReferencesRenderer` + `DisplayObjectCollector` — remove os dois hardcodes restantes no motor
7. `ProfileDefinition.ComponentRulesDefinition` — migração para mapa polimórfico
8. `DocumentContentRequest` e camada de API — migração para mapa aberto, espelha o Mecanismo 7
9. `RenderingConfig` — beans de renderer por componente substituídos por registro dinâmico

Após o passo 9, nenhum arquivo Java no motor referencia qualquer nome de componente ABNT.
Apenas `ElementType` permanece — referenciando tipos de elementos textuais, não componentes.
