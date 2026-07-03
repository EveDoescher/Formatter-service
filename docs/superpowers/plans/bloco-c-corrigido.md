# Bloco C-corrigido — FLOW Genérico para Componentes Textuais Restantes

**Data:** 2026-07-03  
**Pré-requisito:** Bloco D concluído.  
**Próximo bloco:** E

---

## Objetivo

Eliminar do motor os tipos específicos de componentes textuais que ainda existem após o Bloco D. O motor não sabe o que é "dedicatória", "resumo", "glossário" ou "errata". Sabe processar componentes FLOW com estrutura declarada no profile.

**Critério de done:** Os 8 componentes listados abaixo não existem mais como tipos Java próprios. Cada um é um componente FLOW (ou INDEX) com estrutura declarada no profile.

---

## Componentes a migrar

### Grupo 1 — Conteúdo simples (texto único)

| Componente | Estrutura atual | Estrutura alvo |
|---|---|---|
| `DedicationComponent(String text)` | Texto livre | FLOW com slot `text` de tipo parágrafo |
| `AcknowledgmentsComponent(String text)` | Texto livre | FLOW com slot `text` de tipo parágrafo + heading do profile |
| `EpigraphComponent(String text, String author, Optional<String> source)` | Texto + autor + fonte | FLOW com slots `text`, `author`, `source` — o profile declara template do autor |

### Grupo 2 — Conteúdo texto + lista inline

| Componente | Estrutura atual | Estrutura alvo |
|---|---|---|
| `ResumoComponent(String text, List<String> keywords)` | Texto + palavras-chave | FLOW com parágrafo + lista inline (palavras-chave são lista com `display:inline` declarado no profile) |
| `AbstractComponent(List<AbstractEntry> entries)` | Lista de entradas (heading + text + keywords) | FLOW collection — cada entry é um FLOW item com parágrafo + lista inline |

### Grupo 3 — Listas de entradas

| Componente | Estrutura atual | Estrutura alvo |
|---|---|---|
| `GlossaryComponent(List<GlossaryEntry> entries)` | term → definition | FLOW com lista de entradas; profile declara separador e estilo |
| `ListOfSymbolsComponent(List<SymbolEntry> entries)` | symbol → meaning | Mesmo padrão do glossário |

### Grupo 4 — Tabela declarativa

| Componente | Estrutura atual | Estrutura alvo |
|---|---|---|
| `ErrataComponent(List<ErrataEntry> entries)` | Tabela com 4 colunas fixas | FLOW com slot `entries` de tipo TABLE_OF_FIELDS — profile declara colunas, headers, estilos |

### Grupo 5 — Listas INDEX vazias

| Componente | Estrutura atual | Estrutura alvo |
|---|---|---|
| `ListOfFiguresComponent()` (e 4 irmãos) | Records vazios específicos | `IndexComponent(String componentId)` genérico — o componentId resolve o source no Phase0Index |
| `ListOfAbbreviationsComponent()` | Dados vêm do Phase0 | Idem |

---

## Modelo alvo: FlowComponent genérico

```java
// document/component/flow/
public record FlowComponent(
    String componentId,
    List<FlowContentNode> nodes      // lista de nós de conteúdo
) implements DocumentComponent {}
```

`FlowContentNode` é sealed: `FlowParagraphNode(List<BodyInline> runs)`, `FlowListNode(List<FlowListItem> items)`, `FlowTableNode(List<Map<String, String>> rows)`.

O profile declara via `flowRules` como cada tipo de nó é renderizado — estilo, display inline/block, separadores, templates.

**Nota:** `BodyContentComponent` continua como está — já é a forma correta para conteúdo com seções, cross-refs, figuras, etc. O `FlowComponent` genérico é para componentes mais simples (sem seções recursivas, sem display objects).

---

## Modelo alvo: IndexComponent genérico

```java
// document/component/index/
public record IndexComponent(String componentId) implements DocumentComponent {}
```

O `componentId` ("listOfFigures", "listOfTables", etc.) é usado pelo engine INDEX para buscar os dados corretos no `Phase0Index`. O profile declara `source: FIGURES | TABLES | FRAMES | CHARTS | CODE_LISTINGS | SECTIONS | ABBREVIATIONS`.

---

## Steps de implementação

### CC-1 — FlowComponent + FlowContentNode (domain)

**Novos:**
- `document/component/flow/FlowComponent.java`
- `document/component/flow/FlowContentNode.java` (sealed)
- `document/component/flow/FlowParagraphNode.java`
- `document/component/flow/FlowListNode.java`
- `document/component/flow/FlowListItem.java`
- `document/component/flow/FlowTableNode.java`

**Modificado:** `ComponentType.java` — adicionar `FLOW`, `INDEX`; remover os específicos migrados

---

### CC-2 — IndexComponent (domain)

**Novo:** `document/component/index/IndexComponent.java`

---

### CC-3 — FlowComponentRule no profile

O profile já tem `FlowRule` e `flowRules` declarados no `BodyContentComponentRule`. Para os componentes simples, a rule é mais leve — sem seções recursivas, só regras por tipo de nó.

**Novo:** `profile/model/component/flow/SimpleFlowComponentRule.java`

Campos: `componentId`, `headingStyleId`, `headingText` (opcional — pode vir do profile como `PROFILE` declared ou ausente), `blankLinesAfterHeading`, `flowRules` (regras por tipo de nó: parágrafo, lista inline, lista block, tabela), `styleMapping`.

---

### CC-4 — SimpleFlowRenderer (substitui os 8 renderers específicos)

**Novo:** `rendering/component/flow/SimpleFlowRenderer.java`

Implementa `ComponentRenderer<FlowComponent>`. Registrado múltiplas vezes no `RenderingConfig` (um por componentId: "dedication", "acknowledgments", "epigraph", "resumo", "abstract", "glossary", "listOfSymbols", "errata").

Para cada nó no `FlowComponent.nodes()`:
- `FlowParagraphNode` → delega ao `FlowLayoutEngine` (já existe)
- `FlowListNode` com `display:inline` → concatena items com separador declarado no profile
- `FlowListNode` com `display:block` → lista ABNT
- `FlowTableNode` → emite `DocxTableBlock` com colunas declaradas no profile

---

### CC-5 — IndexComponent engine + AbstractIndexListRenderer refactor

**Novo:** `rendering/component/index/IndexRenderer.java`

Registrado N vezes (um por componentId de lista). Lê `source` da rule do profile → busca no `Phase0Index` → delega ao `AbstractIndexListRenderer` (já existe).

`ListOfFiguresRenderer`, `ListOfTablesRenderer`, etc. — seus `componentId` e `metadataExtractor()` migram para configuração no profile. Os arquivos específicos somem.

---

### CC-6 — API: FlowContentRequest + IndexContentRequest

**Novos:**
- `api/export/dto/request/FlowContentRequest.java` — deserializa os campos dos 8 componentes para `FlowComponent`
- `api/export/dto/request/IndexContentRequest.java` — record vazio ou com dados específicos do componente

**Modificado:** `DocumentContentRequest.java` — campos tipados substituídos por `FlowContentRequest`/`IndexContentRequest` conforme o tipo

---

### CC-7 — ProfileDefinition atualizado

Os `DedicationComponentRuleDefinition`, `ResumoComponentRuleDefinition`, etc. — substituídos por `SimpleFlowComponentRuleDefinition` genérico.

---

### CC-8 — Limpeza + testes + validação

- Remover os 8 renderers específicos + seus testes
- Remover os 8 `*Component.java` específicos + seus testes  
- Remover os 8 `*ComponentRule.java` específicos
- `mvn compile -q && mvn test -q` — suite verde
- Validação visual: documento completo renderizado corretamente
- Commit do bloco
