# Body Content Component

`bodyContent` renders textual content that flows naturally across pages.

It is not a single-page component. Unlike `cover`, `titlePage` and `approvalSheet`, it must not fail just because the content exceeds one page.

## Semantic Model

```text
bodyContent
sections[]
section.id
section.level
section.title
section.paragraphs[]
```

`section.id` is required because future components such as summaries, lists and cross-references need stable document metadata.

`section.level` defines the document heading level. It must be between 1 and 6.

`section.title` is optional so the same base can represent unheaded textual blocks when a profile or component needs it.

## Profile Ownership

The profile owns:

```text
bodyContent.styleMapping.sectionTitleStyleIdsByLevel[]
bodyContent.styleMapping.paragraphStyleId
bodyContent.numbering.enabled
bodyContent.numbering.separator
bodyContent.numbering.primarySuffix
bodyContent.layout.blankLinesBeforeSectionTitleWhenPrecededByContent
bodyContent.layout.blankLinesAfterSectionTitle
bodyContent.layout.pageBreakBeforePrimarySection
bodyContent.layout.blankLineStyleId
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
primarySuffix = .0
```

This produces:

```text
1.0
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

## Current Scope

This first version intentionally renders only:

```text
section title paragraphs
body paragraphs
profile-driven section numbering
real Word heading paragraph styles
```

It does not yet implement:

```text
summary metadata extraction
figure/table metadata
citations
lists
footnotes
page headers
```

Those features should be built on top of this flow base, not inside the provisional `paragraphs` path.

## Samples

```text
docs/samples/body-content/body-content-short.json
```
