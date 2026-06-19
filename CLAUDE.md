# Formatter Service — Claude Instructions

This file tells you what this project is, how to think about it, and what rules to follow when working on it. Read it before touching any code.

The full architectural guide is at `docs/guide/formatter-service-rules.md`. The naming specification is at `docs/guide/naming-conventions.md`. When in doubt, those files are authoritative.

---

## What This Project Is

A document rendering engine. It receives a formatting profile, user preferences, and document content — and returns a formatted file (currently DOCX).

It is a dumb executor. It does not make decisions. It executes what the profile declares.

---

## The Central Law

**Any decision embedded in Java code instead of in the profile is an architectural bug.**

This applies to: layout values, component behavior, section structure, ordering, label text, font names, spacing, validation thresholds — anything that could vary across profiles.

If you are about to write a value or a rule in Java that could come from the profile, stop and put it in the profile instead.

---

## The Three Inputs

The service receives exactly three things:

- **Profile** — declares everything: what components exist, how sections are structured, what each component may contain, all layout strategies, all labels, all defaults and allowed values.
- **Preferences** — user choices within limits the profile allows (font choice, selected components, validation mode).
- **Content** — the actual document data (authors, title, sections, references, etc.).

The service assembles these into a model and renders. It does not fetch, store, or infer anything.

---

## How Data Arrives (and Will Change)

Currently all input comes via JSON. This is intentional for testing.

In the future, a feeder service will provide profiles, preferences, and content from a database. When that happens, only the input/application layer changes. The rendering engine must not change.

Never couple the rendering engine to the input mechanism.

---

## Document Structure

A document is a recursive tree:

```
Document
└── Section
    ├── Component (heading, paragraph, citation, figure, ...)
    ├── Component
    └── Subsection
        ├── Component
        └── Component
```

The profile declares what each section may contain and how deep nesting may go. The service does not assume any structure.

---

## Two Types of Components

**Spatial layout components** — tied to a specific page, have positional groups and overflow constraints. Examples: cover, title page, approval sheet.

**Textual atomic components** — inline or block content elements, reusable in any profile. Examples: paragraph, heading, citation, figure, table, code block.

---

## Component Rules (Non-Negotiable)

A component does not know what document surrounds it. It receives rules and content, executes, and returns output.

A component must never contain:

- `if (profile == ABNT)` or any equivalent;
- hardcoded font names, spacing, labels, or layout values;
- assumptions about the surrounding document structure.

**A component is done when it would work correctly in a different profile with different rules, without changing its code.**

---

## The System Does Not Know the Document Type

There is no `if (type == TCC)` or `if (profile == ABNT)` anywhere in the engine. The system does not know and must not know whether it is formatting a TCC, an article, or a custom document. The profile handles all of that.

---

## Layer Rules — Quick Reference

| Layer | May | Must Not |
|---|---|---|
| `api` | receive DTOs, call application, return files | know layout, know docx4j, invent content |
| `application` | coordinate use case, assemble model | render components, contain docx4j code |
| `document` | represent content structure | contain formatting or profile logic |
| `profile` | declare rules, defaults, component definitions | contain docx4j or rendering logic |
| `rendering` | transform content+profile into abstract output ops | import docx4j, hardcode any profile value |
| `output/docx` | translate abstract ops to DOCX/OpenXML | leak docx4j outside its own package |

Renderers call `DocxWriter`. They never call docx4j directly.

---

## What You Must Not Hardcode

Never hardcode in Java:

- page dimensions or margins;
- font names (Arial, Times New Roman, Consolas, etc.);
- font sizes or line spacing;
- indentation values;
- label text (REFERÊNCIAS, Orientador, Palavras-chave, etc.);
- component order or section structure;
- gap counts or blank line counts for layout;
- required/optional component decisions.

These belong in the profile.

Allowed hardcoded values: technical constants only (twips per inch, DOCX media type, OpenXML internal values).

---

## No Silent Defaults

Records and models must not silently fill missing values. If a required value is missing, throw a clear exception immediately.

---

## Validation Is Layered

- Profile layer: validates internal consistency of declared rules.
- Rendering layer: validates compatibility between content and profile before rendering.
- Layout engine: validates physical viability (overflow, single-page fit).

Each layer validates its own responsibility. Redundancy is intentional.

---

## Shared Data

Fields used by multiple components (authors, title, advisor, city, year) must not be duplicated across components. They come from `work` via `contentBindings` declared in the profile.

---

## Layout

No `addSpacing(N)`. No fixed blank lines. Layout is calculated from content + rules.

Single-page components (cover, title page) use semantic groups: top, center, bottom (anchored). If content does not fit, fail clearly.

---

## Performance

Current phase: make it work correctly with excellent code quality.

Do not optimize prematurely. Do not introduce async job complexity before it is needed. Do not design for hypothetical scale before the basic flow is correct.

The clean separation between input assembly and rendering engine already prepares the ground for async in the future.

---

## Naming

Follow `docs/guide/naming-conventions.md` exactly.

Pattern: `<ComponentName><Responsibility>.java`

Do not use: Manager, Processor, Handler, Helper, Utils, Common, Base, Impl, New, Old, V2, Temp, Final — unless no more precise name exists.

---

## Before Writing Any Code

Ask:

1. Is the decision I am about to write in code something that could vary across profiles? If yes, it belongs in the profile.
2. Does this component know anything about the document surrounding it? If yes, remove that knowledge.
3. Would this component work in a completely different profile without code changes? If no, redesign it.
4. Am I importing docx4j outside of `output/docx/docx4j`? If yes, stop.
5. Am I filling a missing value silently? If yes, throw an exception instead.

---

## Test Data

All test data must be generic, fictional, and free of legal risk.

**Names** — use clearly fictional names that no real person is likely to have. Do not use names of real people, public figures, celebrities, or anyone who could be identified. Example pattern: `Ana Souza`, `Carlos Lima`, `Beatriz Rocha` — common, generic, unambiguous as fictional.

**Institutions** — use fictional institution names. Do not use real university names, company names, or any registered brand. Example: `Universidade Fictícia de Limeira`, `Instituto Exemplo`.

**Locations** — use `Limeira` as the default city in academic samples. Do not use real addresses, real neighborhoods, or any location that could identify a real person or place in a sensitive way.

**Titles and content** — use neutral, obviously fictional academic titles and abstract text. Do not use titles or text excerpts from real published works, theses, or articles.

**Images** — only use images that are confirmed public domain with no copyright restrictions. Never use images fetched from arbitrary URLs, stock photo services, or any source that does not explicitly release the image to public domain. Prefer programmatically generated test images (solid color blocks, simple shapes) or images from verified public domain sources such as Wikimedia Commons with explicit public domain licensing. Never use illustrations, artworks, photographs, or logos that belong to a person, institution, or brand.

**Any data that could be mistaken for real** — if a test value could conceivably identify a real person, institution, or protected work, replace it with something more obviously fictional.
