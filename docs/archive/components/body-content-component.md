# Body Content Component

> **Arquivo histórico — não usar como referência ativa.**
> Escrito durante a implementação inicial do `bodyContent` (Fases 1–2, junho 2026).
> O código evoluiu significativamente desde então: block types foram adicionados e removidos,
> a seção "Current Scope" está desatualizada, e a lista de samples está incompleta.
> Para a especificação atual do sistema, consulte `docs/guide/formatter-service-rules.md`.

`bodyContent` renders textual content that flows naturally across pages.

It is not a single-page component. Unlike `cover`, `titlePage` and `approvalSheet`, it must not fail just because the content exceeds one page.

## Semantic Model

```text
bodyContent
sections[]
section.id
section.level
section.title
section.blocks[]
```

`section.id` is required because future components such as summaries, lists and cross-references need stable document metadata.

`section.level` defines the document heading level. It must be between 1 and 6.

`section.title` is optional so the same base can represent unheaded textual blocks when a profile or component needs it.

`section.blocks[]` is the semantic flow of the section. The current supported
block types are:

```text
PARAGRAPH
DIRECT_SHORT_QUOTE
DIRECT_LONG_QUOTE
INDIRECT_CITATION
CITATION_OF_CITATION
FIGURE
TABLE
```

`content[]` and `paragraphs[]` are still accepted only as compatibility inputs.
`paragraphs[]` is converted to `PARAGRAPH` blocks. New samples and new clients
should prefer `blocks[]`.

Short direct citations, indirect citations and apud citations must be modeled as
inline paragraph content. Do not send a ready-made citation marker such as
`(AUTOR TESTE UM, 2020, p. 10)` as free text. The request provides semantic
source data and the component formats the author-date call.

Minimum inline citation shape:

```json
{
  "type": "PARAGRAPH",
  "content": [
    {
      "type": "TEXT",
      "text": "Conforme "
    },
    {
      "type": "CITATION",
      "citationType": "DIRECT_SHORT",
      "mode": "NARRATIVE",
      "source": {
        "authors": [
          {
            "type": "PERSON",
            "surname": "Sobrenome Teste Um"
          }
        ],
        "year": "2020",
        "page": "10"
      }
    },
    {
      "type": "TEXT",
      "text": ", "
    },
    {
      "type": "QUOTE_TEXT",
      "quoteType": "SHORT",
      "text": "quoted text without manual quotation marks"
    }
  ]
}
```

For `CITATION_OF_CITATION`, use `originalSource` and `consultedSource`.
The consulted source is the one that must later connect to references.

`NARRATIVE` renders only the author-date call, for example
`Sobrenome Teste Um (2020, p. 10)`. Surrounding words such as `Segundo`,
`Conforme`, `Para` or `de acordo com` belong to `TEXT` inline content.

`QUOTE_TEXT` renders quotation marks for short direct quotes. The user should not
send manual quotation marks in the input; boundary quotation marks are rejected.
Long direct quotes remain block-level `DIRECT_LONG_QUOTE` items and must not
render quotation marks.

Citation modes:

```text
PARENTHETICAL
NARRATIVE
```

Direct short and direct long citations require `page`. Indirect citations may
omit `page`. Citation of citation requires `page` in the consulted source.

`FIGURE` is a numbered display object. It is rendered as one semantic block:

```text
caption
image
source
```

The figure `id` must be unique. Do not repeat `id` to indicate continuation.
Use `continuationGroupId` when one logical figure is split into explicit parts.
The renderer assigns one number to the continuation group and applies the
continuation labels declared by the profile:

```text
Figura 1 - Caption (continua)
Figura 1 - Caption (continuação)
Figura 1 - Caption (conclusão)
```

For two parts, the first receives `continua` and the second receives
`conclusão`. For three or more parts, intermediate parts receive `continuação`.

Bitmap images are not split automatically. If a figure needs to continue on
another page, the request must provide explicit image parts with the same
`continuationGroupId`. A single image is only scaled down according to the
profile fit policy.

For continued figures, `source` belongs to the logical group. It may be provided
in any part, but repeated values for the same `continuationGroupId` must be
identical.

Figure images currently support:

```text
DATA_URI
URL
```

`DATA_URI` is useful for deterministic offline tests. `URL` is useful for visual
development and future storage integrations, such as Supabase public object
URLs. URL fetching only accepts `http` and `https`, respects the profile timeout
and rejects images larger than `maxImageBytes`.

`TABLE` is also a numbered display object. It is rendered as one semantic block:

```text
caption
table grid
source
```

The table `id` must be unique. Do not repeat `id` to indicate continuation.
Use `continuationGroupId` when one logical table is split into explicit parts.
The renderer assigns one number to the continuation group and applies the
continuation labels declared by the profile:

```text
Tabela 1 - Caption (continua)
Tabela 1 - Caption (continuação)
Tabela 1 - Caption (conclusão)
```

For two parts, the first receives `continua` and the second receives
`conclusão`. For three or more parts, intermediate parts receive `continuação`.

Tables are not split semantically by the formatter. If a table needs to continue
on another page, the request must provide explicit table parts with the same
`continuationGroupId`. Each part must provide its own columns and rows. The row
cell count must match the column count.

For continued tables, `source` belongs to the logical group. It may be provided
in any part, but repeated values for the same `continuationGroupId` must be
identical. The profile decides whether the source is rendered in every part or
only in the last part.

## Profile Ownership

The profile owns:

```text
bodyContent.styleMapping.sectionTitleStyleIdsByLevel[]
bodyContent.styleMapping.paragraphStyleId
bodyContent.styleMapping.directShortQuoteStyleId
bodyContent.styleMapping.directLongQuoteStyleId
bodyContent.styleMapping.indirectCitationStyleId
bodyContent.styleMapping.citationOfCitationStyleId
bodyContent.figure.captionStyleId
bodyContent.figure.sourceStyleId
bodyContent.figure.captionTemplate
bodyContent.figure.sourceTemplate
bodyContent.figure.continuationLabels
bodyContent.figure.sourcePlacement
bodyContent.figure.imageAlignment
bodyContent.figure.maxWidthCm
bodyContent.figure.maxHeightCm
bodyContent.figure.defaultDpi
bodyContent.figure.maxImageBytes
bodyContent.figure.urlFetchTimeoutSeconds
bodyContent.figure.fitPolicy
bodyContent.table.captionStyleId
bodyContent.table.sourceStyleId
bodyContent.table.headerStyleId
bodyContent.table.cellStyleId
bodyContent.table.captionTemplate
bodyContent.table.sourceTemplate
bodyContent.table.continuationLabels
bodyContent.table.sourcePlacement
bodyContent.table.tableAlignment
bodyContent.table.widthPercent
bodyContent.table.repeatHeaderOnPageBreak
bodyContent.numbering.enabled
bodyContent.numbering.separator
bodyContent.numbering.primarySuffix
bodyContent.layout.blankLinesBeforeSectionTitleWhenPrecededByContent
bodyContent.layout.blankLinesAfterSectionTitle
bodyContent.layout.pageBreakBeforePrimarySection
bodyContent.layout.blankLineStyleId
pageNumbering.visibleFromComponentId
pageNumbering.countFromComponentId
pageNumbering.verticalDistanceFromPageEdgeCm
pageNumbering.horizontalDistanceFromPageEdgeCm
```

The renderer must not hardcode fonts, indentation, spacing, uppercase, bold or alignment.

Section title styles must use `StyleType.HEADING_1` through `StyleType.HEADING_6`
when they should become real Word headings. The DOCX writer must customize the
corresponding built-in Word style (`Heading1` through `Heading6`) with the
formatting declared by the profile, then apply that Word style to the title
paragraph.

Do not simulate headings by writing normal paragraphs with only `outlineLvl`.
Do not mask Word's default heading appearance with direct paragraph/run
formatting. The heading style itself must be defined from the profile, including
font, size, bold, italic, alignment, indentation, line spacing, spacing before,
spacing after and outline level.

The UNIP profile currently enables numbering with:

```text
separator = .
primarySuffix =
```

This produces:

```text
1
1.1
1.1.1
```

The current UNIP profile also declares:

```text
blankLinesBeforeSectionTitleWhenPrecededByContent = 1
blankLinesAfterSectionTitle = 1
pageBreakBeforePrimarySection = false
blankLineStyleId = bodyContent.paragraph
```

This means:

```text
first section title
blank line
paragraph
blank line
next section title
blank line
paragraph
```

`pageBreakBeforePrimarySection` exists because some institutional profiles may
require level-1 sections to start on a new page. Keep it profile-driven; do not
hardcode this behavior in the renderer.

Page numbering is document infrastructure, not a `bodyContent` rendering detail.
When the profile declares `pageNumbering.visibleFromComponentId = bodyContent`,
the document renderer starts a new DOCX section before `bodyContent` and the
writer applies the configured page-number header/footer to that section.

Counting and visibility are separate profile decisions:

```text
pageNumbering.countFromComponentId controls which rendered component is page 1.
pageNumbering.visibleFromComponentId controls where the number becomes visible.
```

Do not infer counting from visibility. The renderer only follows the component
ids declared by the profile: counting begins at `countFromComponentId`, remains
invisible until `visibleFromComponentId`, and becomes visible there. Position and
font size must come from the profile style/measurements, not from hardcoded
renderer or writer values.

## Current Scope

This first version intentionally renders only:

```text
section title paragraphs
body paragraphs
inline short direct citations
block long direct citations
inline indirect citations
inline citation of citation / apud
figure display objects with caption, image, source and continuation groups
table display objects with caption, table grid, source and continuation groups
profile-driven section numbering
real Word heading paragraph styles
```

It does not yet implement:

```text
summary metadata extraction
figure/table metadata
lists
footnotes
page headers
```

Citations are represented as semantic inline content or semantic block content,
depending on the citation type. The component does not hardcode font,
indentation or spacing: the profile maps each block-level citation type to the
style that must be rendered. The component does format citation punctuation and
author-date calls from semantic source data, because those are textual citation
rules rather than visual layout.

Those future features should be built on top of this flow base, not inside the
compatibility `paragraphs` path.

## Samples

```text
docs/samples/body-content/body-content-short.json
docs/samples/body-content/body-content-citations.json
docs/samples/body-content/body-content-figures.json
docs/samples/body-content/body-content-figures-url-visual.json
docs/samples/body-content/body-content-tables.json
docs/samples/body-content/body-content-title-only-section.json
docs/samples/body-content/body-content-section-hierarchy-invalid.json
docs/samples/body-content/body-content-citation-direct-missing-page-invalid.json
docs/samples/body-content/body-content-citation-manual-quotes-invalid.json
docs/samples/body-content/body-content-table-row-mismatch-invalid.json
docs/samples/body-content/body-content-selected-components-pagination-invalid.json
```
