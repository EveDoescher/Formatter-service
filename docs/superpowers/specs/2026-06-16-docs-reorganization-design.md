# Docs Reorganization Design

**Date:** 2026-06-16
**Branch:** feature/elementos-textuais

## Problem

`docs/` has two structural issues:
1. **Duplication** — `docs/` and `docs/docs/` mirror most of the same files, with a handful of differences between them (no single folder is fully up to date).
2. **Mixed root** — loose markdown files (`formatter_service_rules_ai_guide.md`, `roadmap_componentes_abnt_ia.md`) sit alongside folders without semantic grouping.
3. **Foreign assets** — `docs/design/` belongs to the Anverso frontend repo, not this service.

## Target Structure

```
docs/
├── guide/
│   ├── formatter-service-rules.md        ← was docs/formatter_service_rules_ai_guide.md
│   └── naming-conventions.md             ← was docs/architecture/naming-conventions.md
│
├── components/
│   ├── body-content-component.md         ← docs/ root version (has table section)
│   ├── single-page-component-reference.md
│   ├── title-page-component.md
│   └── work-data-and-bindings.md
│
├── samples/
│   ├── cover/
│   ├── title-page/
│   ├── approval-sheet/
│   ├── body-content/
│   └── composed/
│
├── roadmap/
│   ├── roadmap-componentes-abnt.md       ← docs/ root version (has table rules)
│   └── checkpoint-elementos-textuais.md  ← was docs/checkpoint_elementos_textuais_tabelas.md
│
├── ai-context/
│   ├── resumo-do-projeto.md              ← was docs/docs/claude/resumo-do-projeto.md
│   └── analise-proposta-vs-implementacao.md
│
├── superpowers/
│   ├── specs/
│   └── plans/
│
└── manual_de_normalizacao_abnt.pdf
```

## Merge Strategy

Files that differ between `docs/` and `docs/docs/`:
- `body-content-component.md` — `docs/` root is authoritative (has table block type + table display object rules)
- `roadmap_componentes_abnt_ia.md` — `docs/` root is authoritative (has full table rules section)

All other shared files are identical — keep one copy.

## Deletions

- `docs/design/` — deleted entirely (up-to-date version lives in the Anverso frontend repo)
- `docs/docs/` — deleted after merge (superseded by unified structure)
- `docs/architecture/` — deleted after moving `naming-conventions.md` to `guide/`

## Completion Criteria

- No file exists in two places
- `docs/docs/` does not exist
- `docs/design/` does not exist
- `docs/architecture/` does not exist
- All content accessible under a semantically named folder
