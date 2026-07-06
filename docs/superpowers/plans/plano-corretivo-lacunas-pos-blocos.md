# Plano Corretivo — Lacunas pós-Blocos A–F

**Data:** 2026-07-06
**Branch:** feature/elementos-textuais
**Contexto:** Após os Blocos A–F, o motor genérico está substancialmente completo. Este plano corrige a violação arquitetural do `abstract` e as lacunas de cobertura (samples + testes).

---

## Resumo das lacunas

| # | Lacuna | Risco | Esforço |
|---|---|---|---|
| **L1** | `AbstractComponent / AbstractRenderer / AbstractComponentRule` existem — violação da lei central | Alto — o sistema SABE que existe "abstract" | Médio |
| L2 | `listOfSymbols` sem sample nem teste end-to-end | Médio — slots `terms`/`definitions` nunca validados | Baixo |
| L3 | `listOfAbbreviations` + Phase0 nunca exercitados juntos | Médio | Baixo |
| L4 | `summary` sem sample standalone | Baixo | Baixo |
| L5 | Listas de display sem sample composto | Médio — `AbstractIndexListRenderer` nunca exercitado via API | Médio |
| L6 | Fluxo ABNT completo nunca integrado num único sample | Alto — caminho principal nunca testado end-to-end | Médio |

A L1 é a única que exige mudanças de código. As demais são samples + testes.

---

## L1 — Migração do `abstract` para o motor genérico

### Por que é uma violação

`AbstractComponent`, `AbstractEntry`, `AbstractComponentRule`, `AbstractRenderer` dizem ao sistema que existe um documento chamado "abstract". Isso é o mesmo que ter `CoverComponent` antes do Bloco D. O nome da classe é a violação — o motor não deve saber o que é um resumo em outra língua.

### Mecanismo necessário: `RepeatGroupItem` + `EntryListValue`

O `abstract` é diferente do `resumo` em um único aspecto: ele repete o mesmo template de FlowItems N vezes (uma por língua), com page-break entre as repetições. O `headingText` de cada repetição vem do **content** (não do profile), pois cada língua tem seu próprio título ("ABSTRACT", "RESUMEN").

Dois novos tipos resolvem isso com zero-Java para novos perfis:

**`EntryListValue`** (document layer — `ContentValue`):
```java
// Lista de entradas, cada uma sendo um Map<slot, value> independente.
record EntryListValue(List<Map<String, ContentValue>> entries) implements ContentValue
```

**`FlowItem.RepeatGroupItem`** (profile layer):
```java
// Repete um grupo de FlowItems para cada entrada em um EntryListValue slot.
record RepeatGroupItem(String entriesSlotName, boolean pageBreakBetweenEntries, List<FlowItem> group)
        implements FlowItem
```

O `FlowTextualRenderer` itera sobre `EntryListValue.entries()`, renderizando o grupo de `FlowItem` de cada vez com os slots da entrada atual sobrepondo os slots do componente.

### Como o profile declara o abstract após a migração

O JSON do profile **não muda**. O `AbstractComponentRuleDefinition.toDomain()` em `ProfileDefinition` passa a gerar `FlowTextualComponentRule` em vez de `AbstractComponentRule`:

```json
"abstract": {
  "componentId": "abstract",
  "headingStyleId": "resumo.heading",
  "textStyleId": "resumo.text",
  "keywordsStyleId": "resumo.keywords",
  "keywordsSeparator": "; ",
  "keywordsTerminator": ".",
  "blankLinesAfterHeading": 1
}
```

→ `toDomain()` gera:
```
FlowTextualComponentRule("abstract", [
  RepeatGroupItem("entries", pageBreakBetweenEntries=true, group=[
    PlainTextItem(headingStyleId, "headingText"),
    BlankLinesItem(headingStyleId, blankLinesAfterHeading),
    PlainTextItem(textStyleId, "text"),
    BoldLabeledKeywordsItem(keywordsStyleId, "keywordsLabel", "keywords", keywordsSeparator, keywordsTerminator)
  ])
])
```

### Como o content chega (JSON não muda)

O JSON do `document.abstract` **não muda**. A estrutura `entries: [{headingText, text, keywords, keywordsLabel}, ...]` já existe no sample `resumo-simple.json` e continua igual.

`FlowTextualContentRequest` passa a detectar `List<Map<String, Object>>` e criar `EntryListValue`. Cada Map interno é convertido com as mesmas regras: `String → TextValue`, `List<String> → TextListValue`.

### Escopo completo das mudanças

**Criar (2 arquivos):**
- `document/component/singlepage/EntryListValue.java` — record com `List<Map<String, ContentValue>>`, validação (não nulo, não vazio, maps não nulos)

**Modificar (10 arquivos):**

| Arquivo | Mudança |
|---|---|
| `ContentValue.java` | Adicionar `EntryListValue` ao `sealed permits` |
| `FlowItem.java` | Adicionar `RepeatGroupItem` ao `sealed permits` + record |
| `FlowTextualRenderer.java` | Adicionar `case RepeatGroupItem` — itera sobre `EntryListValue`, renderiza grupo com slots da entrada atual |
| `FlowTextualContentRequest.java` | Detectar `List<Map<String,Object>>` em `toContentValue()` → `EntryListValue` |
| `ProfileDefinition.java` | `AbstractComponentRuleDefinition.toDomain()` retorna `FlowTextualComponentRule` em vez de `AbstractComponentRule` |
| `DocumentContentRequest.java` | Campo `abstractEn` muda de `AbstractRequest` para `FlowTextualContentRequest` |
| `ComponentRulesRequest.java` | Campo `abstractEn` muda de `AbstractComponentRuleRequest` para `FlowTextualComponentRuleRequest` |
| `ComponentType.java` | Remover `ABSTRACT_EN` |
| `RenderingConfig.java` | Remover `abstractRenderer()` bean, adicionar `FlowTextualRenderer("abstract")` bean |

**Deletar (7 arquivos):**
- `document/component/abstracten/AbstractComponent.java`
- `document/component/abstracten/AbstractEntry.java`
- `profile/model/component/abstracten/AbstractComponentRule.java`
- `rendering/component/abstracten/AbstractRenderer.java`
- `api/export/dto/request/AbstractRequest.java`
- `api/export/dto/request/AbstractEntryRequest.java`
- `api/export/dto/request/AbstractComponentRuleRequest.java`

**Atualizar testes (5 arquivos):**

| Arquivo | Mudança |
|---|---|
| `ClasspathJsonProfileProviderTest.java` | Assertar que a rule do `abstract` no perfil é `FlowTextualComponentRule` (não `AbstractComponentRule`) |
| `DocumentRendererComponentSelectionTest.java` | O test `shouldRejectUnsupportedSelectedComponent` usa `"abstract"` — o test passa a ser inválido porque `abstract` passa a ser um componentId suportado no registry. Remover ou substituir por outro componentId inexistente. |
| `ComponentSelectionResolverTest.java` | Mesmo motivo — test usa `"abstract"` como componentId não suportado. Atualizar. |
| `DocumentProfileTest.java` | Mesmo motivo — test usa `"abstract"` como componentId desconhecido no order. Atualizar. |

**Criar testes novos (3 arquivos):**

| Arquivo | O que cobre |
|---|---|
| `ContentValueTest.java` (já existe — adicionar caso) | `EntryListValue` — construção válida, rejeição de lista vazia, imutabilidade |
| `FlowItemTest.java` (já existe — adicionar caso) | `RepeatGroupItem` — construção válida, rejeição de grupo vazio |
| `FlowTextualRendererTest.java` (já existe — adicionar caso) | `RepeatGroupItem` com 2 entradas: verifica page-break entre elas, e que slots por entrada funcionam independentemente |

---

## L2 — Sample + teste para `listOfSymbols`

**Novo arquivo:** `docs/samples/list-of-symbols/list-of-symbols-simple.json`

```json
{
  "fileName": "list-of-symbols-simple.docx",
  "profileId": "abnt-unip-profile",
  "options": { "selectedComponents": ["listOfSymbols"] },
  "document": {
    "listOfSymbols": {
      "terms": ["α", "β", "σ²", "μ"],
      "definitions": [
        "Nível de significância",
        "Coeficiente de regressão",
        "Variância amostral",
        "Média populacional"
      ]
    }
  },
  "paragraphs": []
}
```

**Teste:** Adicionar ao `ComponentSampleValidationTest` (ver seção final).

---

## L3 — Sample + teste para `listOfAbbreviations` com bodyContent

**Novo arquivo:** `docs/samples/list-of-abbreviations/list-of-abbreviations-with-body.json`

Precisa de `titlePage` (por restrição de paginação), `listOfAbbreviations` (slots vazios — a data vem do Phase0) e `bodyContent` com abreviaturas inline.

```json
{
  "fileName": "list-of-abbreviations-with-body.docx",
  "profileId": "abnt-unip-profile",
  "options": {
    "selectedComponents": ["titlePage", "listOfAbbreviations", "bodyContent"]
  },
  "document": {
    "listOfAbbreviations": {},
    "titlePage": { ... campos mínimos ... },
    "bodyContent": {
      "sections": [{
        "id": "s1", "level": 1, "title": "Introdução",
        "blocks": [{
          "type": "PARAGRAPH",
          "content": [
            { "type": "ABBREVIATION", "text": "TCC", "expansion": "Trabalho de Conclusão de Curso" },
            { "type": "TEXT", "text": " e " },
            { "type": "ABBREVIATION", "text": "ABNT", "expansion": "Associação Brasileira de Normas Técnicas" }
          ]
        }]
      }]
    }
  }
}
```

**Teste:** verificar que o XML contém `"TCC"` e `"ABNT"` na lista gerada.

---

## L4 — Sample + teste para `summary`

**Novo arquivo:** `docs/samples/summary/summary-simple.json`

Com `useTocField: true` no perfil, `summary` funciona standalone — não precisa de `bodyContent`. O componente não tem slots de content (é um marcador de presença).

```json
{
  "fileName": "summary-simple.docx",
  "profileId": "abnt-unip-profile",
  "options": { "selectedComponents": ["summary"] },
  "document": { "summary": {} },
  "paragraphs": []
}
```

**Teste:** verificar que o XML contém `"SUMÁRIO"`.

---

## L5 — Sample + teste para listas de display

**Novo arquivo:** `docs/samples/composed/body-content-with-index-lists.json`

Inclui `bodyContent` com figuras, tabelas, quadros, gráficos e listagens + todas as 5 listas de display em `selectedComponents`.

Reutiliza o conteúdo de `body-content-figures.json`, `body-content-tables.json`, `body-content-frames.json`, `body-content-charts.json`, `body-content-code-listings.json` — mesclado numa única seção de bodyContent.

`selectedComponents`: `["titlePage", "listOfFigures", "listOfTables", "listOfFrames", "listOfCharts", "listOfCodeListings", "bodyContent"]`

`document`: `"listOfFigures": {}`, `"listOfTables": {}`, etc. — todos slots vazios, dados vêm do Phase0.

**Teste:** verificar que o XML contém `"LISTA DE ILUSTRAÇÕES"`, `"LISTA DE TABELAS"`, `"LISTA DE QUADROS"`, `"LISTA DE GRÁFICOS"`, `"LISTA DE LISTAGENS"`.

---

## L6 — Sample de fluxo ABNT completo

**Novo arquivo:** `docs/samples/composed/full-document-abnt-complete.json`

`selectedComponents` em ordem ABNT completa:
```
cover, titlePage, errata, approvalSheet, dedication, acknowledgments, epigraph,
resumo, abstract, listOfAbbreviations, listOfSymbols, summary,
listOfFigures, listOfTables, bodyContent, references, appendix, annex, glossary
```

Usa dados mínimos mas válidos para cada componente. Cobre o caminho completo do sistema num único request.

**Teste:** verificar que a resposta é 200 e contém `"RESUMO"`, `"ABSTRACT"`, `"SUMÁRIO"`, `"REFERÊNCIAS"`.

---

## Novo arquivo de teste de integração: `ComponentSampleValidationTest`

**Localização:** `src/test/java/com/abntbuilder/formatter/rendering/component/ComponentSampleValidationTest.java`

**Por que novo arquivo:** `BodyContentSampleValidationTest` está no pacote `bodycontent` — errado para componentes de outros tipos.

```java
@SpringBootTest
@AutoConfigureMockMvc
class ComponentSampleValidationTest {

    @Test void shouldRenderAbstractSample() { /* resumo-simple.json já existe */ }
    @Test void shouldRenderListOfSymbolsSample() { }
    @Test void shouldRenderListOfAbbreviationsWithBodyContentSample() { }
    @Test void shouldRenderSummarySample() { }
    @Test void shouldRenderBodyContentWithAllIndexListsSample() { }
    @Test void shouldRenderFullAbntCompleteDocumentSample() { }
}
```

---

## Ordem de execução recomendada

1. **L1** — A migração do `abstract` é a única que toca código. Deve vir primeiro para garantir que os testes de base continuam passando.
2. **L2, L3, L4** — Independentes entre si. Podem ser feitas em qualquer ordem.
3. **L5** — Requer bodyContent rico; mais trabalhoso.
4. **L6** — Depende de todos os anteriores como referência de padrões.

---

## Nota: `resumo-simple.json` já exercita o `abstract`

O sample `docs/samples/resumo/resumo-simple.json` já inclui `abstract` com duas entradas. Ele **não é executado automaticamente** por nenhum teste. Após a migração da L1, ele será adicionado ao `ComponentSampleValidationTest`.
