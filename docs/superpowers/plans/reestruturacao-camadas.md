# Plano — Reestruturação de Camadas e Responsabilidades

## Pré-requisito

Este plano depende da conclusão completa de `limpeza-nomes-componentes.md`. Todos os tipos com
nomes de componentes ABNT devem ter sido eliminados antes de iniciar qualquer passo aqui.

---

## Problema

O projeto cresceu com critério de **origem** como princípio de organização:

- O que veio do profile foi para `profile/`
- O que veio do conteúdo do usuário foi para `document/`
- O que renderiza foi para `rendering/`

Isso ignora o que cada coisa *é* no motor. O resultado são três sintomas:

**1. `profile/model` virou dependência de tudo.**
`StyleRule`, `FlowItem`, `SinglePageComponentRule`, `BodyContentComponentRule` estão em
`profile/model` porque "vêm do profile". Mas são tipos que o rendering usa diretamente. O
`FlowTextualRenderer` importa `profile.model.component.flowtextual.FlowItem`. O `SinglePageRenderer`
importa `profile.model.component.singlepage.SinglePageComponentRule`. O rendering depende do
profile para acessar seus próprios tipos fundamentais — a dependência está invertida.

**2. `document/component` mistura entrada com domínio.**
`BodyFigure`, `BodyTable`, `BodySection`, `BodyParagraph`, `BodyInline` estão em
`document/component/bodycontent/` porque "vêm do conteúdo do usuário". Mas o `FlowLayoutEngine`,
o `TextTypeRegistry`, o `DisplayObjectCollector` e o `Phase0Index` os usam como tipos do motor.
São elementos textuais que o motor conhece como mecanismo — não "documentos de entrada".

**3. `rendering/` mistura contratos, mecanismos e estado.**
`ComponentRenderer`, `ComponentRendererRegistry` (contratos do motor), `FlowLayoutEngine`,
`SinglePageLayoutEngine` (mecanismos de execução), `Phase0Index`, `BodyContentMetadata`,
`SectionNumberingState` (estado de runtime) convivem no mesmo pacote sem separação clara.

---

## Princípio da reestruturação

O critério de organização passa a ser **o que a coisa é**, não de onde ela veio.

- `StyleRule` não é "modelo do profile" — é um tipo do motor que o profile preenche.
- `FlowItem` não é "dado do profile" — é o vocabulário declarativo do mecanismo flowtextual.
- `BodyFigure` não é "documento de entrada" — é um tipo de elemento textual que o motor conhece.
- `ComponentRenderer` não é "rendering" — é o contrato central do motor.

---

## Estrutura alvo

```
com.abntbuilder.formatter
│
├── engine/                     — núcleo do motor: tipos e contratos
│   ├── model/                  — todos os tipos que o motor conhece
│   │   ├── profile/            — DocumentProfile, StyleRule, ComponentRule e subtipos,
│   │   │                         PageRule, PageNumberingRule, PostProcessingRule,
│   │   │                         FlowItem, SinglePageComponentRule, BodyContentComponentRule,
│   │   │                         ReferencesComponentRule, SectionedComponentRule,
│   │   │                         ElementIndexComponentRule, SectionIndexComponentRule,
│   │   │                         e todos os tipos de layout e sub-rules
│   │   ├── content/            — tipos de conteúdo de componente:
│   │   │                         SinglePageContent, ContentValue e subtipos,
│   │   │                         FlowTextualContent,
│   │   │                         BodyContentComponent, BodySection, BodyBlock e subtipos,
│   │   │                         BodyInline e subtipos, elementos textuais (BodyFigure,
│   │   │                         BodyTable, BodyFrame, BodyChart, BodyCodeListing,
│   │   │                         BodyEquation, BodyList, BodyParagraph, BodyLongQuote, ...),
│   │   │                         ReferencesComponent, ReferenceEntry, ReferenceType,
│   │   │                         SectionedContent, SectionedItem,
│   │   │                         ElementIndexContent, SectionIndexContent
│   │   └── output/             — tipos de output abstrato:
│   │                             DocxBlock e todos os subtipos (DocxParagraph, DocxRun,
│   │                             DocxBlankLine, DocxPageBreak, DocxTableBlock, DocxImageBlock,
│   │                             DocxTocBlock, DocxSectionBreak, DocxPageNumbering,
│   │                             DocxFootnoteContent, DocxListItemParagraph, ...)
│   │                             DocxDocument
│   └── contract/               — contratos do motor:
│                                 ComponentRenderer, ComponentRenderResult,
│                                 MetadataConsumingRenderer, MetadataEmittingRenderer,
│                                 Phase0ConsumingRenderer, ComponentRendererRegistry,
│                                 DocxWriter, DocxWriterException,
│                                 DocxPostProcessor, PostProcessorResult
│
├── input/                      — converte entrada externa em domínio do motor
│   ├── profile/                — deserialização de profile JSON:
│   │                             ProfileDefinition, ProfileProvider,
│   │                             ClasspathJsonProfileProvider, InMemoryProfileProvider,
│   │                             ComponentRuleResolver, StyleResolver
│   └── api/                    — HTTP: controller, DTOs, tratamento de erros
│                                 DocxExportController, ExportDocxRequest e todos os *Request,
│                                 ApiExceptionHandler, ApiErrorResponse
│
├── rendering/                  — mecanismos de execução
│   ├── singlepage/             — SinglePageRenderer, SinglePageLayoutCalculator,
│   │                             SinglePageLayoutAssembler, SinglePageContentValidator,
│   │                             SinglePageLayoutEngine, SinglePageGapDistributor,
│   │                             SinglePageLayoutRenderer, SinglePageRenderableAreaCalculator,
│   │                             SinglePageLayoutLineMetrics, HorizontalPlacementResolver,
│   │                             OrderedLayoutGapResolver, MarginBasedSinglePageSafetyPolicy,
│   │                             SinglePageSafetyPolicy, e todos os tipos intermediários
│   │                             de layout (SinglePageLayoutInput, SinglePageLayoutPlan, ...)
│   ├── flowtextual/            — FlowTextualRenderer
│   ├── bodycontent/            — BodyContentRenderer, FlowLayoutEngine, FlowRenderingContext,
│   │                             TextTypeRegistry, RunProcessor,
│   │                             BodyContentRenderResult, BodyContentMetadata,
│   │                             BodySectionMetadata, BodyDisplayObjectMetadata,
│   │                             BodyAbbreviationMetadata, SectionNumberingState,
│   │                             DisplayObjectRenderingState, DisplayObjectContinuationPart
│   ├── references/             — ReferencesRenderer, ReferencesEntryFormatter, ReferenceSegment
│   ├── sectioned/              — SectionedRenderer
│   ├── elementindex/           — ElementIndexRenderer
│   ├── sectionindex/           — SectionIndexRenderer
│   ├── phase0/                 — DisplayObjectCollector, Phase0Index
│   └── text/                   — TextMeasurer, FontMetricsTextMeasurer,
│                                 ConservativeTextMeasurer, MissingFontPolicy,
│                                 MeasuredText, TextMeasurementArea
│
├── output/                     — tradução de output abstrato para formato concreto
│   └── docx/                   — Docx4jWriter, LibreOfficeDocxPostProcessor,
│                                 NoOpDocxPostProcessor
│
├── application/                — orquestração do caso de uso (sem alteração)
│   └── export/                 — DocxExportService, ExportDocxCommand,
│                                 GeneratedDocxExport, GeneratedDocxExportStore,
│                                 InMemoryGeneratedDocxExportStore
│
├── shared/                     — utilitários e exceções transversais (sem alteração)
│   ├── exception/
│   └── measurement/
│
└── config/                     — configuração Spring (sem alteração estrutural,
                                  mas simplificada após limpeza de nomes)
```

---

## O que move para onde — detalhado por pacote atual

### `profile/model/` → `engine/model/profile/`

Tudo que hoje está em `profile/model/` move para `engine/model/profile/`, mantendo a
estrutura interna de subpacotes (`component/singlepage/`, `component/flowtextual/`,
`component/bodycontent/`, `component/references/`, `layout/singlepage/`).

Novos subpacotes adicionados (criados pelo plano de limpeza):
- `component/sectioned/` — `SectionedComponentRule`
- `component/elementindex/` — `ElementIndexComponentRule`, `ElementType`
- `component/sectionindex/` — `SectionIndexComponentRule`

Subpacotes removidos (eliminados pelo plano de limpeza):
- `component/annex/`, `component/appendix/`, `component/summary/`, `component/indexlist/`

`ComponentContentBindings` também é removido — contém lista hardcoded de `work.*` sources
que é conhecimento de domínio ABNT. Com o motor genérico, bindings de campos são declarados
livremente pelo profile sem validação contra lista fixa.

### `profile/provider/` → `input/profile/`

`ProfileDefinition` e `ClasspathJsonProfileProvider` movem para `input/profile/`. Junto com eles:
- `profile/resolution/ProfileProvider` → `input/profile/ProfileProvider`
- `profile/resolution/InMemoryProfileProvider` → `input/profile/InMemoryProfileProvider`
- `profile/resolution/ComponentRuleResolver` → `input/profile/ComponentRuleResolver`
- `profile/resolution/StyleResolver` → `input/profile/StyleResolver`

Justificativa: `ComponentRuleResolver` e `StyleResolver` são ferramentas de consulta ao domínio
carregado — pertencem à camada de input junto com quem carrega o profile.

### `document/component/` → dividido entre `engine/model/content/` e remoção

**Move para `engine/model/content/`:**
- `DocumentComponent` (interface)
- `ComponentType` enum (após limpeza pelo plano de limpeza)
- `singlepage/` inteiro — `SinglePageContent`, `ContentValue` e subtipos
- `flowtextual/` inteiro — `FlowTextualContent`
- `bodycontent/` inteiro — `BodyContentComponent`, `BodySection`, `BodyBlock`, `BodyInline`,
  `BodyFigure`, `BodyTable`, `BodyFrame`, `BodyChart`, `BodyCodeListing`, `BodyEquation`,
  `BodyList`, `BodyParagraph`, `BodyLongQuote`, e todos os tipos auxiliares
- `references/` inteiro — `ReferencesComponent`, `ReferenceEntry`, `ReferenceAuthor`,
  `ReferenceType`
- Novos tipos criados pelo plano de limpeza: `sectioned/`, `elementindex/`, `sectionindex/`

**Removidos (pelo plano de limpeza):**
- `annex/`, `appendix/`, `summary/`, `listof*/`

### `output/docx/api/` → `engine/model/output/` e `engine/contract/`

Os tipos `DocxBlock` e subtipos (`DocxParagraph`, `DocxRun`, `DocxBlankLine`, etc.) e
`DocxDocument` movem para `engine/model/output/` — são o vocabulário de output abstrato
que o rendering produz e que o `DocxWriter` consome.

As interfaces e contratos (`DocxWriter`, `DocxWriterException`, `DocxPostProcessor`,
`PostProcessorResult`) movem para `engine/contract/` junto com os contratos de renderer.

### `output/docx/docx4j/` e `output/docx/postprocess/` → `output/docx/`

Ficam em `output/docx/` sem subpacotes — são a implementação concreta de saída. Não há
nada mais aqui além de `Docx4jWriter`, `LibreOfficeDocxPostProcessor`, `NoOpDocxPostProcessor`.

### `rendering/component/` → `engine/contract/` + `rendering/`

**Move para `engine/contract/`:**
- `ComponentRenderer`
- `ComponentRenderResult`
- `MetadataConsumingRenderer`
- `MetadataEmittingRenderer`
- `Phase0ConsumingRenderer`
- `ComponentRendererRegistry`

**Fica em `rendering/` (reorganizado por mecanismo):**
- `rendering/singlepage/` — `SinglePageRenderer` e colaboradores
- `rendering/flowtextual/` — `FlowTextualRenderer`
- `rendering/bodycontent/` — `BodyContentRenderer` e colaboradores
- `rendering/references/` — `ReferencesRenderer` e colaboradores
- Novos mecanismos criados pelo plano de limpeza

### `rendering/flow/` → `rendering/bodycontent/`

`FlowLayoutEngine`, `FlowRenderingContext`, `RunProcessor`, `TextTypeRegistry` são internos
ao mecanismo de bodycontent. Ficam em `rendering/bodycontent/` junto com o renderer.

### `rendering/layout/singlepage/` → `rendering/singlepage/`

Todos os tipos de layout de singlepage ficam em `rendering/singlepage/` junto com o renderer.
Não há razão para subpacote separado quando tudo pertence ao mesmo mecanismo.

### `rendering/layout/text/` → `rendering/text/`

### `rendering/phase0/` → `rendering/phase0/`

Fica no mesmo lugar, apenas muda o pacote pai de `rendering.phase0` para continuar em
`rendering/phase0/`.

### `rendering/orchestration/` → `rendering/orchestration/`

`DocumentRenderer` e `ComponentSelectionResolver` ficam em `rendering/orchestration/`.

### `api/` → `input/api/`

Todo o pacote `api/` move para `input/api/`. O controller, os DTOs e o handler de erros
são entrada — pertencem à camada de input.

---

## Regras de dependência após a reestruturação

```
engine/         ←  não depende de ninguém
input/          ←  depende de engine/
rendering/      ←  depende de engine/
output/         ←  depende de engine/model/output/ e engine/contract/
application/    ←  depende de engine/, rendering/, output/
config/         ←  depende de tudo (ponto de fiação Spring)
shared/         ←  não depende de ninguém (só Java padrão)
```

`rendering/` não depende de `input/` — o rendering nunca sabe como o profile foi carregado.
`output/` não depende de `rendering/` — o writer não sabe quem gerou os blocos.
`input/api/` não depende de `rendering/` diretamente — passa pelo `application/`.

---

## O que não muda

- `application/export/` — já está correto. `DocxExportService`, `ExportDocxCommand`,
  `GeneratedDocxExportStore` ficam como estão.
- `shared/exception/` e `shared/measurement/` — ficam como estão.
- A lógica interna de cada renderer, engine de layout, e medidor de texto — nenhuma linha
  de lógica muda neste plano. É exclusivamente reorganização de pacotes.

---

## Ordem de execução sugerida

1. Criar `engine/model/output/` — mover os `Docx*` tipos (sem dependências internas)
2. Criar `engine/contract/` — mover interfaces de renderer e `DocxWriter`/`DocxPostProcessor`
3. Criar `engine/model/content/` — mover `document/component/` inteiro
4. Criar `engine/model/profile/` — mover `profile/model/` inteiro
5. Criar `input/profile/` — mover `profile/provider/` e `profile/resolution/`
6. Criar `input/api/` — mover `api/` inteiro
7. Reorganizar `rendering/` — achatar subpacotes por mecanismo, mover contratos para `engine/`
8. Simplificar `output/docx/` — remover subpacote `api/`
9. Remover pacotes vazios — `profile/`, `document/`, `api/`
10. Atualizar `config/RenderingConfig` — ajustar imports

Cada passo é um conjunto de moves de arquivo com ajuste de `package` e `import`. Nenhum
passo altera lógica. Todos os testes passam em cada passo intermediário.
