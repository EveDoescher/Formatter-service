# Title Page Component

`titlePage` renders the front side of the academic title page as a single-page component.

It is not a cover variant. It has its own semantic input, its own profile rule and its own renderer, while reusing the shared single-page layout base.

## Content Model

The user provides semantic content:

- `authors`
- `title`
- `subtitle`
- `nature.workType`
- `nature.degreeObjective`
- `nature.courseName`
- `nature.institutionName`
- `advisor`
- `coadvisor`
- `city`
- `year`

The user does not provide fixed blank lines, manual nature text or manual indentation.

## Profile Rule

The profile defines:

- styles for authors, title, subtitle, nature, advisor, coadvisor, city and year;
- text templates for nature, advisor and coadvisor;
- single-page groups, items, gaps and policy;
- horizontal placement for each item.

The UNIP profile currently declares these groups:

- `titlePage.authors`
- `titlePage.titleBlock`
- `titlePage.natureBlock`
- `titlePage.bottom`

The `nature`, `advisor` and `coadvisor` items use `FROM_PAGE_CENTER_TO_RIGHT_MARGIN`, so the engine calculates the right-half text area from the page rule instead of using a fixed indent.

In this engine, `FROM_PAGE_CENTER_TO_RIGHT_MARGIN` means the center of the useful
page area, after margins, not the physical center of the paper.

The spacing between `nature`, `advisor` and `coadvisor` is profile-driven through
`blankLinesAfter`. This is internal spacing inside `titlePage.natureBlock`; the
distance from the title block to the nature block, and from the nature block to
the bottom block, must stay in `gapRules`.

## Rendering Pipeline

The component follows the same separation used by the cover:

```text
TitlePageComponent
+ TitlePageComponentRule
+ TitlePageLayoutAssembler
+ SinglePageLayoutEngine
+ SinglePageLayoutPlan
+ SinglePageLayoutRenderer
+ Docx4jWriter
```

The renderer does not calculate gaps, does not measure text and does not hardcode labels such as `Orientador(a)`.

## Failure Rules

The component fails before DOCX generation when:

- required semantic content is missing;
- a profile item id is unknown;
- a declared max visual line limit is exceeded;
- the rendered content cannot fit inside one safe page;
- a template references an unknown placeholder.

`city` and `year` are constrained by profile to one visual line each.

## Official Samples

Samples live in `docs/samples/title-page`:

- `title-page-short.json`
- `title-page-no-subtitle.json`
- `title-page-with-coadvisor.json`
- `title-page-long-title.json`
- `title-page-long-nature.json`
- `title-page-many-authors.json`
- `title-page-overflow.json`
- `title-page-bottom-wrap-invalid.json`
- `cover-and-title-page.json`

The `overflow` and `bottom-wrap-invalid` samples are expected to fail.

## Visual Checklist

Validate generated DOCX files in Word:

- the title page occupies exactly one page;
- authors appear before title and subtitle;
- the nature block starts in the right half of the useful page area;
- nature/advisor/coadvisor use simple spacing;
- city and year stay at the bottom;
- no page break appears inside the title page;
- `cover-and-title-page.json` generates cover, page break, title page.
