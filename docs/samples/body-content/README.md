# Body Content Samples

Samples for the `bodyContent` flowing text component.

- `body-content-short.json`: valid text body with introduction, development and conclusion.
- `body-content-citations.json`: valid text body with inline short direct quote, inline indirect citation, inline apud citation and block long quote.
- `body-content-figures.json`: valid offline text body with figure blocks and explicit figure continuation.
- `body-content-figures-url-visual.json`: visual validation sample with URL-based figure blocks.
- `body-content-title-only-section.json`: valid text body with a primary grouping section that has no blocks of its own.
- `body-content-section-hierarchy-invalid.json`: invalid section hierarchy that starts at level 2.
- `body-content-citation-direct-missing-page-invalid.json`: invalid direct citation without page.
- `body-content-citation-manual-quotes-invalid.json`: invalid short direct citation with manual boundary quotation marks.
- `body-content-selected-components-pagination-invalid.json`: invalid selected component subset for the active page numbering rule.

Expected behavior:

```text
generates DOCX;
uses profile styles for section titles and body paragraphs;
uses profile styles for each block-level citation type;
renders short direct, indirect and apud citations inside paragraph flow;
renders figure caption, image and source as one display object;
does not apply single-page overflow validation;
allows content to flow naturally across pages.
```

Prefer `sections[].blocks[]` for new samples. `sections[].content[]` and
`sections[].paragraphs[]` remain accepted only as compatibility input.
