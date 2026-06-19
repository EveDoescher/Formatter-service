# Formatter Service — Architecture and Development Guide

> This document is the single source of architectural and development rules for the Formatter Service.
> Any AI assistant, developer, or tool working on this project must follow this file carefully.
> It replaces previous noisy or outdated planning files.
> The project must be built from these decisions, not from assumptions.

---

# 1. Project Identity

The Formatter Service is a document rendering engine.

It receives structured input, applies a formatting profile, and returns a formatted output file.

It is an independent microservice. It does not know who requested the document, does not store anything, and does not manage users, plans, or history.

The first output format is DOCX. Future formats may include PDF and HTML preview.

The first real formatting profile is `abnt-unip-profile`. However, the service must not be architected as an ABNT-only system. ABNT and UNIP are profiles, not the identity of the service.

---

# 2. The Central Law

> The service is a dumb executor.
> It does not make decisions.
> It executes what the profile declares, with the content it received, within the declared preferences.

Any decision embedded in Java code instead of in the profile is an architectural bug.

This applies to everything: layout values, component behavior, section structure, component ordering, label text, font choices, spacing rules, validation thresholds, and anything else that could vary across profiles.

If the profile changes, behavior must change without touching the code.

---

# 3. What the Service Receives

The service receives exactly three things:

## 3.1 Profile

Defines what is allowed, what is default, and how the document must be structured.

The profile declares:

- page rules (size, orientation, margins);
- allowed and default fonts;
- style rules;
- which components exist and are valid;
- what each component may contain;
- how sections are structured and what they may contain;
- component ordering;
- layout strategies;
- label text;
- validation rules;
- output constraints.

## 3.2 User Preferences

Defines user choices within limits allowed by the profile.

Examples:

- chosen base font (from allowed values);
- chosen code font (from allowed values);
- selected components;
- output format;
- validation mode (strict or flexible).

## 3.3 Content

Defines the actual document content.

Examples:

- institution, course, authors, title, subtitle;
- abstract text;
- sections and their inner content;
- references;
- figures, tables, code blocks.

---

# 4. What the Service Does Not Do

The service does not:

- decide what a document should contain;
- infer missing information;
- know whether it is formatting a TCC, an article, or a custom document;
- know whether the profile is ABNT, institutional, or custom;
- store documents, profiles, or preferences;
- manage users or authentication;
- validate the origin of the request;
- fetch data from databases or external services.

How the data arrives (JSON, database, external service) is the responsibility of the input layer, not the rendering engine. The engine always receives a fully assembled model regardless of origin.

---

# 5. The External Feeder Service

There is a separate service responsible for:

- composing and storing formatting profiles;
- collecting and storing user preferences;
- collecting and storing document content;
- delivering everything to the Formatter Service when generation is requested.

The Formatter Service is deliberately ignorant of this service. It only knows the contract: profile + preferences + content in, document out.

Currently the service receives all input via JSON. This is intentional for testing simplicity. When the feeder service and database integration arrive, the change must be contained in the input/application layer. The rendering engine must not change.

---

# 6. Document Structure

A document is a recursive tree.

At the top level there are sections. A section contains components and may contain subsections. Subsections contain the same types.

```
Document
└── Section
    ├── Component (heading, paragraph, citation, figure, table, ...)
    ├── Component
    └── Subsection
        ├── Component
        └── Component
```

The profile declares what a section may contain, which component types are valid in each context, and how deep nesting may go.

The service does not assume any structure. If the profile does not declare it, it does not exist.

---

# 7. Component Model

## 7.1 Two Types of Components

**Spatial layout components** are tied to a specific page or area. They have positional groups, anchors, and overflow constraints.

Examples: cover, title page, approval sheet, epigraph.

**Textual atomic components** are inline or block-level content elements. They are reusable in any profile and any section context.

Examples: paragraph, section heading, block citation, figure, table, code block, footnote, list item.

## 7.2 Components Are Context-Unaware

A component does not know what document surrounds it.

A paragraph component does not know it is inside a TCC. A citation component does not know anything about UNIP. A figure component does not know it is in an article.

Components receive rules and content. They execute. They return output. The profile is the one that decides where each component appears and what rules it receives.

## 7.3 Components Are Isolated and Reusable

A component is built once and reused wherever the profile places it.

The criterion for "done" includes: would this component work correctly in a different profile with different rules, without changing its code?

If a component only works well in one profile, it was built incorrectly.

## 7.4 No Profile Knowledge in Components

No component may contain:

- `if (profile == ABNT)` or any equivalent;
- hardcoded font names;
- hardcoded spacing or layout values;
- hardcoded label text;
- assumptions about surrounding document structure.

---

# 8. Profile as the Single Decision Maker

The profile is the only place where decisions live.

The profile defines:

- which sections exist and in what order;
- what each section may contain;
- which components exist;
- what each component may contain;
- how each component behaves;
- all layout strategies;
- all label text;
- all default and allowed values;
- all validation rules.

A section's behavior, a component's behavior, and the document structure are entirely profile-driven.

The service reads the profile and executes it. It does not interpret it, override it, or fill gaps in it.

---

# 9. Main Architectural Rule

> Profile declares.
> Content provides.
> Preferences choose within allowed values.
> Application layer assembles.
> Rendering layer executes.
> Output layer writes.

No layer assumes responsibilities from another layer.

---

# 10. Package Structure

```text
com.abntbuilder.formatter
│
├── FormatterApplication.java
│
├── config
│
├── api
│   └── export
│       ├── controller
│       └── dto
│           ├── request
│           └── response
│
├── application
│   └── export
│
├── document
│   ├── model
│   └── component
│
├── profile
│   ├── model
│   ├── provider
│   └── resolution
│
├── rendering
│   ├── orchestration
│   ├── component
│   │   ├── cover
│   │   ├── titlepage
│   │   ├── bodycontent
│   │   └── shared
│   └── layout
│
├── output
│   ├── docx
│   │   ├── api
│   │   └── docx4j
│   └── pdf
│       └── future
│
└── shared
    ├── exception
    └── measurement
```

---

# 11. Dependency Direction Rules

- `api` may depend on `application`;
- `application` may coordinate `document`, `profile`, `rendering`, and output APIs;
- `rendering` may depend on `document`, `profile`, and output APIs;
- `output/docx/docx4j` may depend on `output/docx/api`, `profile`, and `shared`;
- `document` must not depend on rendering, output, Spring, docx4j, or profile providers;
- `profile/model` must not depend on rendering, output, Spring, or docx4j;
- renderers must not import docx4j or OpenXML classes;
- controllers must not import docx4j or OpenXML classes.

---

# 12. Layer Responsibilities

## 12.1 API Layer

Receives HTTP requests. Returns HTTP responses.

May: receive request DTOs, call application services, return files, set HTTP headers.

Must not: invent content, invent rules, know ABNT, know docx4j, decide layout.

## 12.2 Application Layer

Coordinates the generation use case.

May: receive the generation request, assemble the full model from input sources (currently JSON, later potentially database or feeder service), call rendering orchestration, return generated bytes.

Must not: render components, contain docx4j code, hardcode academic rules.

## 12.3 Document Layer

Represents content structure.

Must not contain formatting logic, profile knowledge, or rendering concerns.

## 12.4 Profile Layer

Represents rules, options, allowed values, defaults, and component/section definitions.

Must not contain docx4j-specific logic or rendering logic.

## 12.5 Rendering Layer

Transforms document data and profile rules into abstract output operations.

Must: understand components, select styles through resolvers, apply layout strategies, call output writer interfaces.

Must not: import docx4j, create OpenXML objects, hardcode any profile-specific values.

## 12.6 Output Layer

Writes real files.

`DocxWriter` is the interface. `Docx4jWriter` is the implementation. docx4j stays isolated under `output/docx/docx4j`.

---

# 13. No Hardcoding of Profile-Owned Values

The code must not hardcode any value that belongs to a profile.

Forbidden examples:

- page dimensions (A4, letter);
- margins;
- font names (Arial, Times New Roman, Consolas);
- font sizes;
- line spacing;
- indentation;
- label text (REFERÊNCIAS, Orientador, Palavras-chave);
- component order;
- section structure;
- page numbering position;
- vertical spacing or gap counts;
- required or optional component decisions.

These must come from profile structures.

---

# 14. Allowed Technical Constants

Only format-technical constants may be hardcoded.

Allowed examples:

- twips per inch;
- centimeters per inch;
- twips per point;
- half-points per point;
- DOCX media type;
- OpenXML internal values required by the file format.

Centralize technical constants in `shared/measurement/MeasurementConverter` or equivalent.

---

# 15. No Silent Defaults in Records or Models

Records and models must enforce explicit required fields.

Do not create constructors that silently fill defaults.

If a required value is missing, fail early with a clear exception.

---

# 16. Validation in Layers

Each layer validates what is its responsibility:

- **Profile layer**: validates internal consistency of the declared rules;
- **Rendering layer**: validates compatibility between content and profile before rendering;
- **Layout engine**: validates physical viability (overflow, single-page fit).

Redundancy across layers is intentional. Each validation has a distinct responsibility.

Suggested modes:

- `STRICT`: block generation when required profile rules are not met;
- `FLEXIBLE`: allow generation with warnings when a document deviates from a complete profile model.

---

# 17. Shared Document Data

Fields that appear in multiple components (authors, title, subtitle, advisor, city, year) must not be duplicated across components.

Use `work` as the semantic source for shared data. Profiles declare `contentBindings` that map component fields to `work` fields.

Do not infer that two component fields are equal because they have similar names.

---

# 18. Layout Rules

Profiles must not provide fixed blank-line counts as layout. Fixed line counts only transfer hardcoding from Java to JSON.

The profile must define:

- layout strategy;
- content groups;
- anchors;
- priorities;
- constraints;
- minimum and maximum allowed spacing when needed;
- overflow policy;
- fit-to-page policy.

The layout engine calculates real positioning from page dimensions, margins, styles, font sizes, line spacing, and actual content.

## 18.1 No Large Spacing on Single Elements

Never use a single element with a large `spacingBeforePt` or `spacingAfterPt` to simulate vertical distance.

This produces a fragile invisible block: if the user deletes or edits that element in Word, all spacing disappears at once and the document layout breaks silently.

Vertical spacing between content areas must always be represented as a sequence of standardized blank lines (`DocxBlankLine`), each carrying the same style and line height as the surrounding content. The number of blank lines is declared in the profile rule — not hardcoded, and not embedded in a single element's spacing.

This rule applies everywhere without exception: cover groups, title page groups, element positioning near the bottom of a page, spacing between reference entries, or any other context requiring visual separation. A single paragraph carrying hundreds of points of `spacingBefore` is always an architectural mistake.

This was a real problem encountered in the cover and title page implementations, where the system attempted to create large monolithic spacing blocks instead of distributing the space as standardized blank lines.

---

# 19. Single-Page Layout Components

Some components must fit on exactly one page (cover, title page, approval sheet, epigraph).

These use a shared single-page layout strategy with semantic groups:

- top group;
- center group;
- bottom group (anchored).

No fixed `addSpacing(N)` layout. If content cannot fit, fail clearly.

---

# 20. DOCX Output

Renderers call `DocxWriter`, not docx4j.

`Docx4jWriter` translates abstract operations to DOCX/OpenXML.

docx4j dependency for Java 21:

```xml
<dependency>
    <groupId>org.docx4j</groupId>
    <artifactId>docx4j-JAXB-ReferenceImpl</artifactId>
    <version>11.5.13</version>
</dependency>
```

Use a single JAXB implementation.

---

# 21. API and Endpoint Strategy

Initial endpoint:

```text
POST /api/v1/exports/docx
```

Request shape:

```json
{
  "profileId": "abnt-unip-profile",
  "profile": null,
  "output": {
    "format": "DOCX",
    "fileName": "document.docx"
  },
  "options": {
    "validationMode": "FLEXIBLE",
    "selectedComponents": []
  },
  "document": {
    "type": "ACADEMIC_WORK",
    "components": []
  }
}
```

The separation must remain: profile/profileId, output options, document options, document content.

A future asynchronous design may use jobs:

```text
POST /api/v1/exports/jobs
GET  /api/v1/exports/jobs/{id}/download
```

Do not introduce job complexity before it is needed.

---

# 22. Performance Philosophy

Build in phases:

1. Make it work — correct output, clean architecture, no hardcoding;
2. Make it work well — reliability, good error messages, edge cases;
3. Make it work fast — async generation, optimization, large document handling.

Code quality must be excellent from phase 1. Performance optimization comes in phase 3.

Design must not block future async integration. The separation between input assembly and rendering engine is the foundation for async jobs later.

---

# 23. Error Handling Philosophy

When the service cannot safely format something, it must fail clearly.

Do not silently generate incorrect formatting.

Examples that must fail explicitly:

- missing required profile rule;
- content not compatible with declared component rules;
- content overflow in single-page layout;
- unsupported component type;
- unsupported output format;
- missing required style;
- selected component without sufficient content in strict mode.

---

# 24. Testing Strategy

Test:

- model validation;
- measurement conversion;
- profile loading;
- style resolving;
- component selection;
- layout calculations;
- overflow behavior;
- DOCX generation sanity;
- output implementation isolation.

Avoid brittle tests that check long exact sequences of internal operations.

Visual validation must happen by generating DOCX through the real endpoint using controlled JSON samples.

---

# 25. Build Order

1. Clean Spring project with final package structure;
2. docx4j dependency;
3. `MeasurementConverter`;
4. `PageRule`, `StyleRule`, `DocumentProfile`;
5. `DocxWriter` interface and `Docx4jWriter` minimal implementation;
6. Real DOCX export endpoint;
7. Cover: data model, profile rules, single-page layout engine, renderer, visual validation;
8. Title page: same sequence;
9. Body content: section/component tree model, textual atomic components;
10. References;
11. Table of contents;
12. Additional components.

Do not move to the next component until the current one is functional, testable, visually validated, free from hardcoding, and correctly isolated.

---

# 26. Example and Test Data

Use `Limeira` in examples and test data. Avoid `Campinas` in generated sample content.

---

# 27. Naming Guidelines

Use neutral names where the concept is generic.

Do not make core service names ABNT-specific. ABNT-specific names are acceptable only for classes that truly represent ABNT-specific profiles or rules.

See `naming-conventions.md` for the full naming specification.
