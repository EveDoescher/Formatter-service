# Design: Reestruturação de Pacotes — Vertical Slices com Engine Compartilhada

**Data:** 2026-06-11
**Status:** Aprovado para planejamento

---

## Contexto

A estrutura atual é layered (por camada técnica). Funciona corretamente em termos de
separação de responsabilidades, mas gera atrito de navegação: para trabalhar em um
componente acadêmico, o desenvolvedor visita 5 pacotes diferentes espalhados pela
árvore. O pacote `api/export/dto/request/` tem 50+ arquivos flat, e
`document/component/bodycontent/` tem 22 arquivos flat e crescendo.

O projeto ainda está pequeno o suficiente para que a mudança seja barata. A janela
ideal é antes de adicionar novos componentes acadêmicos.

---

## Decisão

Migrar para **Vertical Slices por componente acadêmico**, com um pacote `engine/`
para motores de processamento compartilhados e `infrastructure/` para adaptadores
Spring/HTTP.

---

## Estrutura alvo

```
com.abntbuilder.formatter

├── cover/
│   ├── api/               ← DTOs de request do cover (CoverRequest, etc.)
│   ├── domain/            ← CoverComponent
│   ├── profile/           ← CoverComponentRule, CoverLayoutRule, CoverStyleMapping
│   └── rendering/         ← CoverRenderer, CoverLayoutAssembler, CoverLayoutCalculator,
│                             CoverLayoutPlan, CoverProfileContentValidator,
│                             CoverLayoutDiagnostic, CoverLayoutOverflowException
│
├── titlepage/
│   ├── api/               ← TitlePageRequest, TitlePageNatureRequest,
│   │                        TitlePageStyleMappingRequest, TitlePageTextTemplateRuleRequest,
│   │                        TitlePageComponentRuleRequest
│   ├── domain/            ← TitlePageComponent, TitlePageNature, AcademicPerson
│   ├── profile/           ← TitlePageComponentRule, TitlePageStyleMapping,
│   │                        TitlePageTextTemplateRule
│   └── rendering/         ← TitlePageRenderer, TitlePageLayoutAssembler,
│                             TitlePageLayoutCalculator, TitlePageLayoutPlan,
│                             TitlePageProfileContentValidator,
│                             TitlePageTextTemplateResolver
│
├── approvalsheet/
│   ├── api/               ← ApprovalSheetRequest, ApprovalCommitteeMemberRequest,
│   │                        ApprovalEventRequest, ApprovalSheetNatureRequest,
│   │                        ApprovalSheetComponentRuleRequest, etc.
│   ├── domain/            ← ApprovalSheetComponent, ApprovalCommitteeMember,
│   │                        ApprovalEvent, ApprovalSheetNature
│   ├── profile/           ← ApprovalSheetComponentRule, ApprovalSheetStyleMapping,
│   │                        ApprovalSheetTextTemplateRule, ApprovalSheetCommitteeMemberRule,
│   │                        ApprovalSheetSignatureLineRule
│   └── rendering/         ← ApprovalSheetRenderer, ApprovalSheetLayoutAssembler,
│                             ApprovalSheetLayoutCalculator, ApprovalSheetLayoutPlan,
│                             ApprovalSheetProfileContentValidator,
│                             ApprovalSheetTextTemplateResolver
│
├── bodycontent/
│   ├── api/               ← BodyContentRequest, BodySectionRequest, BodyBlockRequest,
│   │                        BodyBlockType, BodyInlineRequest, BodyInlineType,
│   │                        BodyFigureRequest, BodyTableRequest, BodyTableColumnRequest,
│   │                        BodyTableRowRequest, CitationSourceRequest,
│   │                        CitationAuthorRequest, ImageSourceRequest,
│   │                        BodyContentComponentRuleRequest, BodyContentLayoutRuleRequest,
│   │                        BodyContentNumberingRuleRequest, BodyContentStyleMappingRequest,
│   │                        FigureRuleRequest, TableRuleRequest,
│   │                        DisplayObjectContinuationLabelsRequest
│   ├── domain/            ← BodyBlock (sealed interface/base — raiz do domínio textual),
│   │   │                    BodySection, BodyContentComponent
│   │   ├── citation/      ← BodyCitation, BodyCitationCall, BodyCitationType,
│   │   │                    BodyCitationMode, CitationSource, CitationAuthor,
│   │   │                    CitationAuthorType
│   │   ├── inline/        ← BodyParagraph, BodyInline, BodyText, BodyQuoteText,
│   │   │                    BodyQuoteType
│   │   └── display/       ← BodyFigure, BodyTable, BodyTableColumn, BodyTableRow,
│   │                        BodyImageSource, ImageSourceType, NumberedDisplayObject
│   ├── profile/           ← BodyContentComponentRule, BodyContentLayoutRule,
│   │                        BodyContentNumberingRule, BodyContentStyleMapping,
│   │                        FigureRule, TableRule, ImageFitPolicy,
│   │                        DisplayObjectContinuationLabels, DisplayObjectSourcePlacement
│   └── rendering/         ← BodyContentRenderer, DisplayObjectRenderingState,
│                             DisplayObjectContinuationPart
│
├── engine/                ← motores de processamento compartilhados; os componentes
│   │                        delegam para cá, mas nenhum componente vive aqui
│   ├── singlepage/        ← SinglePageLayoutEngine, SinglePageGapDistributor,
│   │                        OrderedLayoutGapResolver, HorizontalPlacementResolver,
│   │                        SinglePageLayoutRenderer, SinglePageRenderableAreaCalculator,
│   │                        SinglePageLayoutInput, SinglePageLayoutPlan,
│   │                        SinglePageLayoutGroup, SinglePageLayoutItem,
│   │                        SinglePageLayoutElement, SinglePageLayoutLineMetrics,
│   │                        SinglePageLayoutDiagnostic, SinglePageLayoutFailureDiagnostic,
│   │                        SinglePageSpacerLines, SinglePageTextLines,
│   │                        SinglePageRenderableArea, ResolvedLayoutGap,
│   │                        MarginBasedSinglePageSafetyPolicy, SinglePageSafetyPolicy
│   └── text/              ← TextMeasurer, ConservativeTextMeasurer,
│                             FontMetricsTextMeasurer, MeasuredText,
│                             TextMeasurementArea, MissingFontPolicy
│
├── output/
│   ├── docx/
│   │   ├── api/           ← DocxWriter, DocxBlock, DocxParagraph, DocxBlankLine,
│   │   │                    DocxPageBreak, DocxSectionBreak, DocxImageBlock,
│   │   │                    DocxTableBlock, DocxPageNumbering, DocxDocument,
│   │   │                    ParagraphLayoutOverride, DocxWriterException
│   │   └── docx4j/        ← Docx4jWriter
│   └── pdf/               ← reservado para implementação futura
│
├── profile/               ← modelos de perfil base (sem vínculo com componente específico)
│   ├── model/             ← DocumentProfile, PageRule, StyleRule, StyleType,
│   │                        PageOrientation, TextAlignment, PageNumberingRule,
│   │                        PageNumberingPlacement, ComponentRule (interface/base),
│   │                        ComponentContentBindings
│   │   └── layout/
│   │       └── singlepage/ ← HorizontalPlacementRule, HorizontalPlacementStrategy,
│   │                         LayoutGapRule, SinglePageGroupRule, SinglePageItemRule,
│   │                         SinglePageLayoutPolicy, SinglePageLayoutRule,
│   │                         SinglePageAnchorStrategy, SinglePageLineHeightStrategy,
│   │                         SinglePageSafetyPolicyId, SpacerStylePolicy
│   ├── loading/           ← ProfileDefinition (deserialização do JSON)
│   └── resolution/        ← ProfileProvider, ClasspathJsonProfileProvider,
│                             InMemoryProfileProvider, ComponentRuleResolver,
│                             StyleResolver
│
├── shared/
│   ├── exception/         ← todas as exceções tipadas do projeto
│   └── measurement/       ← MeasurementConverter
│
└── infrastructure/        ← adaptadores Spring; único lugar onde Spring Boot é explícito
    ├── api/               ← DocxExportController, ApiExceptionHandler, ApiErrorResponse,
    │                        GenerateDocxResponse
    ├── application/       ← DocxExportService, ExportDocxCommand,
    │                        GeneratedDocxExport, GeneratedDocxExportStore,
    │                        InMemoryGeneratedDocxExportStore
    ├── rendering/         ← ComponentRenderer (interface), ComponentRendererRegistry,
    │                        DocumentRenderer, ComponentSelectionResolver
    └── config/            ← RenderingConfig, DocxWriterConfig, TextMeasurementProperties
```

---

## Regras de dependência (explícitas por design)

Essas regras existem hoje implicitamente. Na nova estrutura precisam ser documentadas
porque a organização por camada não as força mais visualmente.

| Pacote | Pode depender de | Não pode depender de |
|--------|-----------------|----------------------|
| `cover/domain`, `titlepage/domain`, etc. | `shared/` | Spring, docx4j, `profile/`, `engine/`, outros componentes |
| `cover/profile`, `titlepage/profile`, etc. | `profile/model/`, `shared/` | Spring, docx4j, `engine/`, outros componentes |
| `cover/rendering`, `titlepage/rendering`, etc. | próprio `domain/`, próprio `profile/`, `engine/`, `output/docx/api/`, `shared/` | docx4j diretamente, outros componentes |
| `engine/singlepage`, `engine/text` | `profile/model/layout/`, `shared/` | componentes acadêmicos, `output/`, Spring |
| `output/docx/docx4j` | `output/docx/api/`, `profile/`, `shared/` | componentes acadêmicos, `engine/`, Spring (exceto anotações mínimas) |
| `infrastructure/rendering` | todos os `*/rendering/`, `engine/`, `output/docx/api/`, `shared/` | docx4j diretamente |
| `infrastructure/` (demais) | tudo acima | nenhuma restrição adicional — é a camada de composição |

---

## O que não muda

- Nenhuma lógica de negócio muda — é renomeação de pacotes e movimentação de arquivos
- As regras de dependência são as mesmas de hoje, apenas tornadas explícitas
- O contrato da API HTTP não muda
- O profile JSON não muda
- Os testes acompanham os arquivos — o espelho de pacotes de `src/test` segue a mesma estrutura

---

## Itens fora do escopo desta reestruturação

- Correção dos gaps de implementação do `bodyContent` (GAP-1, GAP-2, DESVIO-2, etc.)
- Adição de novos elementos ao `bodyContent`
- Remoção do `ClasspathJsonProfileProvider` (será tratado como decisão de contrato separada)
- Adição de `ArchUnit` para enforcement de dependências (recomendado como passo posterior)

---

## Critério de conclusão

A reestruturação está completa quando:

1. Todos os arquivos estão nos pacotes alvo descritos acima
2. Nenhum import quebrado — build `./mvnw package -DskipTests` passa
3. Todos os testes passam — `./mvnw test`
4. Nenhum arquivo ficou órfão no pacote de origem
5. O espelho de testes em `src/test` segue a mesma estrutura de `src/main`
