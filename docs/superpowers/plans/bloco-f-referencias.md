# Bloco F — Referências Bibliográficas Declarativas

**Data:** 2026-07-03  
**Pré-requisito:** Bloco E concluído.  
**Complexidade:** Comparável ao Bloco B (DisplayObjectCollector).

---

## Objetivo

Substituir o `ReferencesEntryFormatter` (180 linhas, lógica ABNT hardcoded) por um executor de template declarativo. O profile declara a estrutura de cada tipo de referência. Suporte a ABNT, APA, Vancouver e qualquer outra norma via profile — zero Java novo.

---

## Problema atual

`ReferencesEntryFormatter` hardcoda em Java:
- A ordem dos campos para cada tipo (`BOOK`, `JOURNAL`, `THESIS`, etc.)
- Formatação de autores (sobrenome maiúsculas, "et al.", separadores)
- Prefixos/sufixos ("ed.", "In:", "Disponível em:", etc.)
- Tratamento especial para `BOOK_CHAPTER` (dois grupos de autores)

`ReferencesFormattingRule` já existe com metade dos campos necessários (`etAlLabel`, `inLabel`, `authorSurnameUppercase`, etc.) — é uma extensão desse record.

`ReferenceSegment` já existe como tipo intermediário de retorno — continua existindo, apenas é preenchido pelo executor de template em vez de código Java específico.

---

## Modelo alvo

### No profile — entryFormats por tipo

```json
"references": {
  "formattingRule": {
    "authorFormat": {
      "surnameUppercase": true,
      "surnamePrefixSeparator": ", ",
      "nameTerminator": ".",
      "multiAuthorJoiner": "; ",
      "etAlThreshold": 4,
      "etAlLabel": "et al."
    },
    "labels": {
      "availableAt": "Disponível em:",
      "accessedAt": "Acesso em:",
      "in": "In:",
      "edition": "ed."
    },
    "entryFormats": {
      "BOOK": [
        { "source": "authors",    "type": "AUTHOR_LIST",  "terminator": "." },
        { "source": "title",      "type": "TEXT",  "bold": true,  "terminator": "." },
        { "source": "subtitle",   "type": "TEXT",  "prefix": ": ", "optional": true },
        { "source": "edition",    "type": "TEXT",  "suffix": ". ed.", "optional": true, "terminator": "." },
        { "source": "city",       "type": "TEXT",  "terminator": ":" },
        { "source": "publisher",  "type": "TEXT",  "terminator": "," },
        { "source": "year",       "type": "TEXT",  "terminator": "." }
      ],
      "JOURNAL": [
        { "source": "authors",    "type": "AUTHOR_LIST",  "terminator": "." },
        { "source": "title",      "type": "TEXT",  "terminator": "." },
        { "source": "journal",    "type": "TEXT",  "bold": true, "terminator": "," },
        { "source": "city",       "type": "TEXT",  "optional": true, "terminator": "," },
        { "source": "volume",     "type": "TEXT",  "prefix": "v. ", "optional": true, "terminator": "," },
        { "source": "issue",      "type": "TEXT",  "prefix": "n. ", "optional": true, "terminator": "," },
        { "source": "pages",      "type": "TEXT",  "prefix": "p. ", "optional": true, "terminator": "," },
        { "source": "year",       "type": "TEXT",  "terminator": "." },
        { "source": "doi",        "type": "TEXT",  "prefix": "DOI: ", "optional": true, "terminator": "." }
      ],
      "BOOK_CHAPTER": [
        { "source": "chapterAuthors", "type": "AUTHOR_LIST", "terminator": "." },
        { "source": "chapterTitle",   "type": "TEXT", "terminator": "." },
        { "source": "_IN_LABEL",      "type": "LITERAL" },
        { "source": "bookAuthors",    "type": "AUTHOR_LIST", "optional": true, "terminator": "." },
        { "source": "bookTitle",      "type": "TEXT", "bold": true, "terminator": "." },
        ...
      ],
      "THESIS": [...],
      "WEBSITE": [...],
      "LEGISLATION": [...],
      "CONFERENCE_PAPER": [...],
      "REPORT": [...],
      "STANDARD": [...]
    }
  }
}
```

### Tipos de segmento no executor

| Tipo | Comportamento |
|---|---|
| `AUTHOR_LIST` | Aplica `authorFormat` para formatar lista de `ReferenceAuthor` |
| `TEXT` | Campo simples — aplica prefix/suffix/terminator |
| `LITERAL` | String literal do profile (para "In:", etc.) |

### Campos especiais em BOOK_CHAPTER

`BOOK_CHAPTER` tem dois grupos de autores (`authors` = autores do capítulo, `bookAuthors` = autores do livro). O executor trata `source` como chave no `ReferenceEntry` — `"chapterAuthors"` mapeia para `entry.authors()`, `"bookAuthors"` para `entry.bookAuthors()`. O mapeamento é declarado no profile ou convencionado por campo.

---

## Steps de implementação

### F-1 — EntrySegmentRule no profile

**Novos:**
- `profile/model/component/references/EntrySegmentRule.java`
- `profile/model/component/references/AuthorFormatRule.java` — substitui e estende `ReferencesFormattingRule`
- `profile/model/component/references/ReferenceEntryFormat.java` — `Map<ReferenceType, List<EntrySegmentRule>>`

**Modificado:** `ReferencesFormattingRule.java` — campos migram para `AuthorFormatRule`, campo `entryFormats` adicionado

### F-2 — ReferenceEntryFormatExecutor

**Novo:** `rendering/component/references/ReferenceEntryFormatExecutor.java`

Recebe `ReferenceEntry` + `ReferenceEntryFormat` + `AuthorFormatRule`. Para cada segmento na sequência do tipo:
1. Resolve o valor do campo (`entry.title()`, `entry.authors()`, etc.)
2. Se `optional` e ausente: pula
3. Aplica formatação de autor se `AUTHOR_LIST`
4. Aplica prefix/suffix/terminator
5. Retorna `ReferenceSegment` com `bold` conforme declarado

**Normalização final:** remove espaços duplos, ` .` → `.`, ` ,` → `,` (já existe no `ApprovalSheetTextTemplateResolver`, extrair para utilitário compartilhado).

### F-3 — Substituir ReferencesEntryFormatter

**Modificado:** `ReferencesEntryFormatter.java` — lógica Java removida, delega ao `ReferenceEntryFormatExecutor`  
Ou deletar e usar o executor diretamente no `ReferencesRenderer`.

### F-4 — abnt-unip-profile.json

Adicionar `entryFormats` completo para todos os 9 tipos suportados (`BOOK`, `BOOK_CHAPTER`, `JOURNAL`, `WEBSITE`, `LEGISLATION`, `THESIS`, `CONFERENCE_PAPER`, `REPORT`, `STANDARD`).

### F-5 — Testes e validação

- `ReferenceEntryFormatExecutorTest`: cada tipo, campo opcional ausente, campo obrigatório ausente, et al. threshold, sobrenome maiúsculas
- Migrar testes do `ReferencesEntryFormatterTest` para o executor
- `mvn test -q`
- Validação visual: referências geradas com formatação ABNT correta
- Commit do bloco

---

## Resultado

Para suportar APA: declarar `entryFormats` com a ordem APA e `authorFormat` com regras APA no profile. Zero Java novo.

Para suportar Vancouver: idem. Zero Java.

O `ReferenceType` enum ainda existe — os tipos de referência são estáveis entre normas (livro é livro em ABNT e em APA). O que muda entre normas é a formatação, não os tipos.
