# Formatter Service — AI-Friendly Architecture and Development Guide

> **Purpose of this file:**  
> This document is the single source of architectural and development rules for the Formatter Service project.  
> Any AI assistant, developer, or tool working on this project must follow this file carefully.  
> It replaces previous noisy or outdated ABNT-specific planning files.  
> The project must be built from these decisions, not from assumptions taken from earlier failed implementations.

---

# 1. Project Identity

The project is a **Formatter Service**.

It is an independent microservice responsible for receiving structured document/work information, applying a formatting profile, and returning a formatted output file.

The first output format is:

> DOCX

Future output formats may include:

- PDF;
- HTML preview;
- other document formats if needed.

The first real formatting profile is:

> `abnt-unip-profile`

However, the service must not be architected as an ABNT-only system.

ABNT and UNIP are **profiles**, not the identity of the service.

---

# 2. Core Product Vision

The system receives structured information for a document or academic work and, based on a prebuilt or supplied formatting profile, formats that information into an output document.

The core idea is:

> The user or another system provides the content.  
> The Formatter applies structure, style, layout, and output rules.  
> The Formatter returns a finished document file.

The service exists so that the user does not need to manually handle margins, fonts, spacing, title positions, page numbering, references, cover layout, title page layout, and other formatting details.

---

# 3. What the Formatter Service Is

The Formatter Service is:

- a formatting engine;
- a document generation microservice;
- a profile-driven document renderer;
- a DOCX generation service initially;
- a service that separates content, rules, rendering, and output implementation;
- a system designed for future change;
- a system that must support multiple profiles over time.

---

# 4. What the Formatter Service Is Not

The Formatter Service is not:

- the full academic work system;
- the login system;
- the user management system;
- the document dashboard;
- the persistence/history service;
- a Microsoft Word clone;
- a Google Docs clone;
- a free-form page editor;
- a visual drag-and-drop layout builder;
- an AI writing assistant;
- an ABNT-only hardcoded generator.

Authentication, users, plans, saved works, history, and frontend editing belong to other services/modules built later.

---

# 5. Main Architectural Rule

The main architectural rule is:

> Profile says what is allowed and what the defaults are.  
> Document data says what content exists.  
> Document options say what the user selected.  
> Factory/orchestration decides what will be rendered.  
> Renderers execute rendering logic.  
> Output writers translate abstract operations to a real file format.

No layer should assume responsibilities from another layer.

---

# 6. Maximum Decoupling Principle

Everything must be designed for change.

The code must be built as independent, well-separated pieces that communicate through clear contracts.

Expected substitutability:

- if the cover logic changes, the change must stay in the cover renderer/layout/rules area;
- if the title page logic changes, the change must stay in the title page renderer/layout/rules area;
- if docx4j is replaced by another DOCX library, the change must stay inside the DOCX output implementation;
- if ABNT or UNIP rules change, the change must happen mainly in profile/rule files;
- if a new output format is added, such as PDF, it must not contaminate renderers with PDF-specific logic;
- if a new component is added, existing components should not need deep changes;
- if a user chooses Arial instead of Times New Roman, renderers must not change;
- if a profile allows or disables a component, renderers must not hardcode that decision.

A good architecture means that a future change has one obvious place to happen.

---

# 7. ABNT/UNIP as Profile, Not Product Identity

The service must treat ABNT/UNIP as formatting profiles.

Examples of possible profiles:

- `abnt-unip-profile`;
- `abnt-generic-profile`;
- `article-profile`;
- `institution-x-profile`;
- custom profile supplied by request.

The initial practical focus is `abnt-unip-profile`, because it is the relevant profile for the current user. But the architecture must remain broader.

Do not name core services, controllers, or output abstractions in a way that makes the entire microservice ABNT-only.

---

# 8. Recommended Base Package and Project Naming

For the clean restart, prefer a neutral base package such as:

```text
com.abntbuilder.formatter
```

If the project already uses a legacy package name, rename while the project is still small.

Avoid names that imply the service is only an ABNT engine unless the project deliberately decides to keep that naming for product reasons.

---

# 9. Recommended Package Structure

Use a Spring-friendly structure, but preserve the formatter engine boundaries.

Recommended structure:

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

Do not create packages for persistence, security, users, repositories, or integrations before they are actually needed.

---

# 10. Dependency Direction Rules

Dependency direction must be controlled.

Rules:

- `api` may depend on `application`;
- `application` may coordinate `document`, `profile`, `rendering`, and output APIs;
- `rendering` may depend on `document`, `profile`, and output APIs;
- `output/docx/docx4j` may depend on `output/docx/api`, `profile`, and `shared`;
- `document` must not depend on rendering, output, Spring, docx4j, or profile providers;
- `profile/model` must not depend on rendering, output, Spring, or docx4j;
- renderers must not import docx4j/OpenXML classes;
- controllers must not import docx4j/OpenXML classes;
- profile providers may use infrastructure mechanisms, but profile models must stay clean.

---

# 11. Layer Responsibilities

## 11.1 API Layer

The API layer receives HTTP requests and returns HTTP responses.

It may:

- receive request DTOs;
- call application services;
- return files;
- set technical HTTP headers.

It must not:

- invent formatting rules;
- invent document content;
- know ABNT layout details;
- know docx4j;
- decide which components should exist;
- calculate cover layout;
- create hardcoded PageRule or StyleRule values.

## 11.2 Application Layer

The application layer coordinates use cases.

It may:

- receive a generation request;
- load or receive a profile;
- validate high-level generation flow;
- call rendering orchestration;
- return generated bytes.

It must not:

- manually render components;
- contain docx4j code;
- hardcode academic rules;
- become a giant god service.

## 11.3 Document Layer

The document layer represents content.

Examples:

- institution;
- course;
- authors;
- title;
- subtitle;
- city;
- year;
- sections;
- references;
- content blocks.

It must not contain formatting logic.

## 11.4 Profile Layer

The profile layer represents rules, options, allowed values, and defaults.

Examples:

- page size;
- page dimensions;
- margins;
- allowed fonts;
- default fonts;
- style rules;
- component rules;
- layout rules;
- label rules;
- page numbering rules;
- output-related profile rules.

It must not contain docx4j-specific logic.

## 11.5 Rendering Layer

The rendering layer transforms document data plus profile rules into abstract output operations.

It must:

- understand document components;
- select styles through resolvers;
- use layout calculators;
- call output writer interfaces.

It must not:

- import docx4j;
- create OpenXML objects;
- hardcode ABNT or UNIP values;
- decide rules that belong to the profile.

## 11.6 Output Layer

The output layer writes real files.

For DOCX:

- `DocxWriter` is the interface;
- `Docx4jWriter` is the implementation;
- docx4j must stay isolated under `output/docx/docx4j`.

The output implementation translates abstract writer operations to actual DOCX/OpenXML.

---

# 12. No Academic/Layout Hardcoding

The code must not hardcode academic or institutional values.

Forbidden hardcoded examples:

- A4 dimensions;
- margins;
- Arial;
- Times New Roman;
- Consolas;
- Courier New;
- font sizes;
- line spacing;
- indentation;
- page numbering position;
- labels such as `REFERÊNCIAS`, `Orientador`, `Palavras-chave`;
- section title behavior;
- component order;
- optional/required component decisions;
- cover vertical spacing;
- title page vertical spacing;
- whether a component appears in the summary/table of contents.

These must come from profile/rule/request/options structures.

---

# 13. Allowed Technical Constants

Only technical constants may be hardcoded.

Allowed examples:

- twips per inch;
- centimeters per inch;
- twips per point;
- half-points per point;
- DOCX media type;
- OpenXML internal values required by the file format;
- technical ZIP/DOCX entry names inside tests when inspecting generated DOCX.

Technical constants should be centralized in appropriate support classes.

Example:

```text
shared/measurement/MeasurementConverter
```

---

# 14. No Hidden Fallbacks in Records or Models

Records and models must enforce explicit required fields.

Do not create alternate constructors that silently fill defaults.

Forbidden examples:

- `PageRule` assuming A4 if width/height are missing;
- `DocumentProfile` converting `styles = null` to empty list;
- `ComponentLayoutRule` inventing gaps;
- `StyleRule` assuming Arial;
- `PageNumberRule` assuming top-right;
- any record silently filling academic/layout values.

If a required value is missing, fail early with a clear exception.

---

# 15. Profile, Document, and Options Separation

The system must separate three things.

## 15.1 Profile

Defines what is allowed and what is default.

Examples:

- allowed fonts;
- default font;
- page rules;
- style rules;
- layout strategies;
- component definitions;
- labels;
- validation mode support;
- output constraints.

## 15.2 Document Data

Defines the actual content.

Examples:

- authors;
- title;
- abstract text;
- sections;
- references;
- images;
- tables.

## 15.3 Document Options

Defines user choices inside the limits allowed by the profile.

Examples:

- chosen base font;
- chosen code font;
- selected components;
- output format;
- strict/flexible validation;
- language options.

---

# 16. Component Selection

Components must be selectable at document/options level.

The profile can say:

- component exists;
- component is supported;
- component is required in strict mode;
- component has a default selected state;
- component order;
- component title/label;
- component summary inclusion rule.

The document/options can say:

- component is selected for this generation;
- component is omitted;
- component is present but empty;
- component should be validated strictly or flexibly.

The factory/orchestrator decides what will be rendered.

Renderers render only what they receive.

---

# 17. Validation Modes

The formatter should support validation modes later.

Suggested modes:

- `STRICT`: block generation when required profile rules are not met;
- `FLEXIBLE`: allow generation with warnings when a document deviates from a complete institutional model.

Example:

A UNIP profile may consider abstract required for a complete academic work, but the user may generate a simpler ABNT-style document without abstract in flexible mode.

---

# 18. Font Handling

Fonts must be configurable.

The profile defines allowed fonts and defaults.

The document/options may choose within allowed values.

Examples:

- body text may allow Arial or Times New Roman;
- code blocks may allow Consolas or Courier New;
- profile may define default body font;
- user may choose a font exposed by the frontend later.

Renderers must not contain font-specific logic.

---

# 19. Layout Rules

Profiles must not provide fixed blank-line counts as layout.

Bad rule:

```text
skip 6 lines between institution and authors
```

This only transfers hardcoding from Java to JSON.

Good rule:

```text
cover has top, center, and bottom groups;
bottom group is anchored;
available space is distributed automatically;
if content grows, reduce gaps;
if content does not fit, fail clearly.
```

The profile should define:

- layout strategy;
- content groups;
- anchors;
- priorities;
- constraints;
- minimum/maximum allowed spacing when needed;
- overflow policy;
- fit-to-page policy.

The formatter calculates real positioning based on:

- page size;
- margins;
- usable height;
- styles;
- font size;
- line spacing;
- number of authors;
- title length;
- subtitle length;
- note length;
- other component content.

---

# 20. Single-Page Layout Components

Some components must fit on exactly one page.

Examples:

- cover;
- title page;
- approval sheet;
- dedication-style pages;
- epigraph-style pages;
- other pre-textual pages depending on profile.

These components must use a shared single-page layout strategy.

They must not use fixed `addSpacing(N)` layout hacks.

The layout engine should work with semantic groups:

- top group;
- center group;
- middle/intermediate group;
- bottom group.

If a group is configured as bottom anchored, it must remain at the bottom of the usable page area.

If content cannot fit, the system must fail clearly instead of silently pushing content to another page.

---

# 21. Cover Component Rules

The cover is the first real component to implement.

It must be completed before moving to the title page.

The cover must:

- receive all content externally;
- use profile/style/layout rules;
- not assume institution name;
- not assume course presence;
- not assume font;
- not assume page size;
- not assume margins;
- not hardcode vertical gaps;
- keep city/year anchored at the bottom;
- adapt to different title lengths and author counts;
- fail clearly if it cannot fit on one page.

The cover should be built around semantic groups such as:

- institution/course group;
- author group;
- title/subtitle group;
- city/year group.

The exact grouping must be configurable enough to support profile changes.

---

# 22. Title Page Component Rules

The title page must use the same single-page layout principles as the cover.

It must not be built with fixed blank lines.

It must support content variation such as:

- one author;
- multiple authors;
- long title;
- subtitle;
- project note;
- advisor;
- coadvisor in the future;
- city/year anchored at the bottom.

If content does not fit, fail clearly.

---

# 23. DOCX Output Rules

DOCX generation must be behind an interface.

Recommended structure:

```text
output/docx/api/DocxWriter
output/docx/docx4j/Docx4jWriter
```

Renderers must call `DocxWriter`, not docx4j.

`Docx4jWriter` must only translate abstract document operations into DOCX/OpenXML.

If docx4j is replaced later, most of the system should remain unchanged.

---

# 24. docx4j Dependency

For Java 21, use:

```xml
<dependency>
    <groupId>org.docx4j</groupId>
    <artifactId>docx4j-JAXB-ReferenceImpl</artifactId>
    <version>11.5.13</version>
</dependency>
```

Use a single JAXB implementation.

Do not separately declare `docx4j-core` unless a real need appears, because `docx4j-JAXB-ReferenceImpl` pulls the core transitively.

Do not include multiple JAXB implementations at the same time.

---

# 25. Endpoint Strategy

The Formatter Service should expose real generation endpoints, not dev-only generation endpoints.

Initial endpoint suggestion:

```text
POST /api/v1/exports/docx
```

The request should provide either:

- a profile id to load;
- a complete supplied profile;
- document data;
- document options.

The response should return the generated DOCX bytes synchronously at first.

Avoid `generate latest` + `download latest` in memory for the real API, because it does not scale well with multiple users or multiple service instances.

A future asynchronous design may use jobs:

```text
POST /api/v1/exports/jobs
GET  /api/v1/exports/jobs/{id}/download
```

But do not introduce job/storage complexity before needed.

---

# 26. Controller Rules

Controllers must stay thin.

Controllers may:

- receive request DTOs;
- call application service;
- return file response;
- set content type and content disposition.

Controllers must not:

- create PageRule with hardcoded A4;
- create StyleRule with hardcoded Arial;
- create sample content;
- decide layout;
- know ABNT rules;
- know docx4j.

---

# 27. Request Design Direction

The request should evolve toward this conceptual shape:

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

The exact DTO can be refined later, but the separation must remain:

- profile/profileId;
- output options;
- document options;
- document content.

---

# 28. Testing Strategy

Use tests from the beginning, but keep them useful and stable.

Test:

- model validation;
- measurement conversion;
- profile loading;
- style resolving;
- allowed value validation;
- component selection;
- layout calculations;
- overflow behavior;
- DOCX generation sanity;
- isolation of output implementation.

Avoid brittle tests that check long exact sequences of internal operations unless that sequence is the real behavior being guaranteed.

Visual validation should happen by generating DOCX through the real endpoint using controlled JSON.

---

# 29. Build Order for Clean Restart

Recommended implementation order:

1. clean Spring project;
2. docx4j dependency;
3. final package structure;
4. `PageOrientation`;
5. explicit `PageRule`;
6. `MeasurementConverter`;
7. `StyleType`, `TextAlignment`, `StyleRule`;
8. `DocumentProfile`;
9. `StyleResolver`;
10. `DocxWriter` interface;
11. `Docx4jWriter` minimal implementation;
12. real DOCX export endpoint;
13. cover data model;
14. cover profile/layout rules;
15. single-page layout engine;
16. cover renderer;
17. visual validation of cover;
18. title page;
19. textual sections;
20. references;
21. summary/table of contents;
22. other components.

Do not implement many components before the first one is correct.

---

# 30. Component Completion Rule

Do not move to the next component until the current component is:

- functional;
- testable;
- visually validated in DOCX;
- free from academic hardcoding;
- adaptive to variable content;
- isolated in the architecture;
- configurable by profile/options where appropriate.

The first component target is the cover.

---

# 31. Error Handling Philosophy

When the system cannot safely format something, it should fail clearly.

Examples:

- missing required profile rule;
- unsupported component type;
- unsupported output format;
- missing style;
- invalid page dimensions;
- content overflow in single-page layout;
- selected component without enough content in strict mode.

Do not silently generate incorrect formatting.

---

# 32. Naming Guidelines

Use neutral names where the concept is generic.

Prefer:

- `FormatterService`;
- `DocumentProfile`;
- `DocxWriter`;
- `ExportRequest`;
- `CoverRenderer`;
- `SinglePageLayoutEngine`.

Avoid making core names ABNT-specific unless the class truly represents an ABNT-specific profile or rule.

Acceptable ABNT-specific names:

- `abnt-unip-profile.json`;
- `AbntUnipProfileProvider`, if it loads specifically that profile;
- ABNT-specific validation rules, if isolated in profile/rule area.

---

# 33. Example and Test Data Rules

Use `Limeira` in examples and test data.

Avoid using `Campinas` in generated sample academic content for this project.

---

# 34. What Was Learned from the Previous Proof of Concept

The previous implementation was useful as proof of concept, but it must not be continued directly.

Lessons learned:

- docx4j can generate valid DOCX;
- section/page configuration is possible;
- headings require real DOCX style control;
- profile-driven rules are necessary;
- hardcoded layout quickly becomes unmaintainable;
- blank-line layout is fragile;
- fixed spacing in JSON is still a kind of hardcoding;
- too many brittle tests slow down visual validation;
- DOCX output implementation must stay isolated;
- cover/title page layout must be solved properly from the start.

The clean restart must keep the lessons, not the messy structure.

---

# 35. Final Principle

The Formatter Service must be built for change.

Every important decision should have a clear owner:

- content belongs to document data;
- formatting rules belong to profiles;
- user choices belong to document options;
- component inclusion belongs to factory/orchestration;
- component rendering belongs to renderers;
- layout calculation belongs to layout engine;
- file-specific implementation belongs to output writers;
- DOCX/OpenXML details belong only to the DOCX implementation.

If a future change would require editing many unrelated places, the design is not good enough yet.
