# Bloco D — Motor SINGLE_PAGE Genérico

**Data:** 2026-07-03  
**Branch:** feature/elementos-textuais  
**Pré-requisito:** Bloco C concluído. Suite: 355 testes, BUILD SUCCESS.  
**Próximo bloco:** C-corrigido

---

## Objetivo

Eliminar completamente do motor qualquer conhecimento de "capa", "folha de rosto" ou "folha de aprovação". O motor passa a saber que existe um componente `SINGLE_PAGE` com slots declarados no profile. Não sabe mais nada.

**Critério de done:** `grep -r "CoverComponent\|TitlePageComponent\|ApprovalSheetComponent\|CoverComponentRule\|TitlePageComponentRule\|ApprovalSheetComponentRule" src/main/java` → zero resultados.

---

## Modelo alvo

### ContentValue — sealed interface

```java
// document/component/singlepage/
public sealed interface ContentValue
    permits TextValue, TextListValue, ComposedTextValue, SignatureBlockListValue {}

public record TextValue(String text) implements ContentValue {}
// text não-blank obrigatório

public record TextListValue(List<String> items) implements ContentValue {}
// items não-vazio, cada item não-blank

public record ComposedTextValue(Map<String, String> fields) implements ContentValue {}
// fields não-vazio, cada chave/valor não-blank
// Ex: nature = {"workType":"TCC","degreeObjective":"...","courseName":"...","institutionName":"..."}
// Ex: advisor = {"academicTitle":"Prof. Dr.","name":"..."}

public record SignatureBlockListValue(List<Map<String, String>> entries) implements ContentValue {}
// entries não-vazia, cada entry não-vazio, cada campo não-blank
// Ex: committeeMembers = [{"name":"...","title":"...","institutionName":"...","role":"..."}]
```

### SinglePageContent — substitui os 3 typed components

```java
// document/component/singlepage/
public record SinglePageContent(
    String componentId,             // "cover", "titlePage", "approvalSheet" — ID arbitrário
    Map<String, ContentValue> slots // "title"→TextValue, "authors"→TextListValue, etc.
) implements DocumentComponent {}
```

### SlotRule — declaração no profile por slot

```java
// profile/model/component/singlepage/
public sealed interface SlotRule
    permits TextSlotRule, TextListSlotRule, ComposedTextSlotRule, SignatureBlockListSlotRule {}

public record TextSlotRule(boolean required) implements SlotRule {}
public record TextListSlotRule(boolean required) implements SlotRule {}

public record ComposedTextSlotRule(
    boolean required,
    String template,          // ex: "{workType} para {degreeObjective} em {courseName}..."
    List<String> fieldNames   // campos esperados no ComposedTextValue
) implements SlotRule {}

public record SignatureBlockListSlotRule(
    boolean required,
    boolean signatureLineEnabled,
    String signatureLineText,   // null se disabled
    List<String> lineTemplates, // ex: ["{title} {name}", "{institutionName}", "{role}"]
    List<String> knownFieldNames
) implements SlotRule {}
```

### SinglePageComponentRule — substitui os 3 ComponentRules

```java
// profile/model/component/singlepage/
public record SinglePageComponentRule(
    String componentId,
    Map<String, SlotRule> slots,       // "title"→TextSlotRule, "committeeMembers"→SignatureBlockListSlotRule
    Map<String, String> styleMapping,  // "title"→"cover.title", "authors"→"cover.author"
    SinglePageLayoutRule layoutRule    // já existe e já é genérico — sem mudança
) implements ComponentRule {}
```

---

## Novo formato JSON do profile para os 3 componentes

```json
"cover": {
  "componentId": "cover",
  "slots": {
    "institutionalLines": { "type": "TEXT_LIST", "required": true },
    "authors":            { "type": "TEXT_LIST", "required": true },
    "title":              { "type": "TEXT",      "required": true },
    "subtitle":           { "type": "TEXT",      "required": false },
    "city":               { "type": "TEXT",      "required": true },
    "year":               { "type": "TEXT",      "required": true }
  },
  "styleMapping": {
    "institutionalLines": "cover.top",
    "authors":            "cover.author",
    "title":              "cover.title",
    "subtitle":           "cover.subtitle",
    "city":               "cover.bottom",
    "year":               "cover.bottom"
  },
  "layoutRule": { ... }  // igual ao atual
}
```

```json
"titlePage": {
  "componentId": "titlePage",
  "slots": {
    "authors":    { "type": "TEXT_LIST", "required": true },
    "title":      { "type": "TEXT",      "required": true },
    "subtitle":   { "type": "TEXT",      "required": false },
    "nature":     {
      "type": "COMPOSED_TEXT", "required": true,
      "template": "{workType} para {degreeObjective} em {courseName} apresentado à {institutionName}.",
      "fieldNames": ["workType","degreeObjective","courseName","institutionName"]
    },
    "advisor":    {
      "type": "COMPOSED_TEXT", "required": false,
      "template": "Orientador(a): {academicTitle} {name}.",
      "fieldNames": ["academicTitle","name"]
    },
    "coadvisor":  {
      "type": "COMPOSED_TEXT", "required": false,
      "template": "Coorientador(a): {academicTitle} {name}.",
      "fieldNames": ["academicTitle","name"]
    },
    "city":       { "type": "TEXT", "required": true },
    "year":       { "type": "TEXT", "required": true }
  },
  "styleMapping": { ... },
  "layoutRule": { ... }
}
```

```json
"approvalSheet": {
  "componentId": "approvalSheet",
  "slots": {
    "authors":         { "type": "TEXT_LIST", "required": true },
    "title":           { "type": "TEXT",      "required": true },
    "subtitle":        { "type": "TEXT",      "required": false },
    "nature":          {
      "type": "COMPOSED_TEXT", "required": true,
      "template": "{workType} para {degreeObjective} em {courseName} apresentado à {institutionName}.",
      "fieldNames": ["workType","degreeObjective","courseName","institutionName"]
    },
    "approvalText":    { "type": "TEXT", "required": true },
    "committeeHeading":{ "type": "TEXT", "required": true },
    "committeeMembers":{
      "type": "SIGNATURE_BLOCK_LIST", "required": false,
      "signatureLineEnabled": true,
      "signatureLineText": "________________________________________",
      "lineTemplates": ["{title} {name}", "{institutionName}", "{role}"],
      "knownFieldNames": ["name","title","institutionName","role"]
    }
  },
  "styleMapping": { ... },
  "layoutRule": { ... }
}
```

**Mudança no content JSON:** `nature` e `advisor` passam de objetos tipados para `Map<String, String>`:
```json
"nature": {
  "workType": "Trabalho de conclusão de curso",
  "degreeObjective": "obtenção do título de graduação",
  "courseName": "Análise e Desenvolvimento de Sistemas",
  "institutionName": "Universidade Fictícia de Limeira"
}
```
`committeeMembers` continua com a mesma estrutura JSON (já era lista de objetos com campos nomeados).

---

## Steps de implementação

### D-1 — ContentValue + SinglePageContent (domain)

**Novos:**
- `document/component/singlepage/ContentValue.java`
- `document/component/singlepage/TextValue.java`
- `document/component/singlepage/TextListValue.java`
- `document/component/singlepage/ComposedTextValue.java`
- `document/component/singlepage/SignatureBlockListValue.java`
- `document/component/singlepage/SinglePageContent.java`

**Modificado:**
- `document/component/ComponentType.java` — substituir `COVER`, `TITLE_PAGE`, `APPROVAL_SHEET` por `SINGLE_PAGE`

**Testes:** `SinglePageContentTest`, `ContentValueTest`

---

### D-2 — SlotRule + SinglePageComponentRule (profile)

**Novos:**
- `profile/model/component/singlepage/SlotRule.java`
- `profile/model/component/singlepage/TextSlotRule.java`
- `profile/model/component/singlepage/TextListSlotRule.java`
- `profile/model/component/singlepage/ComposedTextSlotRule.java`
- `profile/model/component/singlepage/SignatureBlockListSlotRule.java`
- `profile/model/component/singlepage/SinglePageComponentRule.java`

**Modificado:**
- `profile/model/DocumentProfile.java` — `styleIdsFor()`: novo case `SinglePageComponentRule` usando `rule.styleMapping().values()`, remove os 3 cases específicos

**Testes:** `SinglePageComponentRuleTest`

---

### D-3 — SinglePageLayoutAssembler

**Novo:** `rendering/component/singlepage/SinglePageLayoutAssembler.java`

Recebe `SinglePageContent`, `DocumentProfile`, `SinglePageComponentRule`. Para cada item no `layoutRule`:
1. Busca `SlotRule` pelo `itemId`
2. Busca `ContentValue` em `content.slots().get(itemId)`
3. Converte para `List<String>` conforme tipo:
   - `TextValue` → `List.of(value.text())`
   - `TextListValue` → `value.items()`
   - `ComposedTextValue` + `ComposedTextSlotRule` → aplica template → `List.of(resolved)`
   - `SignatureBlockListValue` + `SignatureBlockListSlotRule` → cada entry gera linhas via templates, com separação por `blankLinesAfter` do `SinglePageItemRule`
4. Mede cada valor com `TextMeasurer`, constrói `SinglePageLayoutItem`

Normalização inline (sem classe separada):
```java
private static String normalize(String s) {
    return s.replaceAll("\\s+", " ").replace(" .", ".").replace(" ,", ",").trim();
}
```

**Testes:** `SinglePageLayoutAssemblerTest` — todos os 4 tipos de slot, optional ausente OK, required ausente lança exceção

---

### D-4 — SinglePageContentValidator + exceção

**Novos:**
- `rendering/component/singlepage/SinglePageContentValidator.java`
- `shared/exception/InvalidSinglePageContentException.java`

Valida: slot required presente, tipo compatível com SlotRule, styleMapping referencia styles existentes.

**Testes:** `SinglePageContentValidatorTest`

---

### D-5 — SinglePageLayoutCalculator + SinglePageRenderer

**Novos:**
- `rendering/component/singlepage/SinglePageLayoutCalculator.java`
- `rendering/component/singlepage/SinglePageRenderer.java`

`SinglePageRenderer` recebe `componentId` no construtor — registrado 3 vezes no `RenderingConfig` (um por componentId: "cover", "titlePage", "approvalSheet"). Usa o `componentId` para resolver a rule correta.

---

### D-6 — API: SinglePageContentRequest

**Novo:** `api/export/dto/request/SinglePageContentRequest.java`

Usa `@JsonAnySetter Map<String, Object> rawSlots` para deserializar campos arbitrários. `toDomain()` converte:
- `String` → `TextValue`
- `List<String>` → `TextListValue`
- `Map<String, String>` → `ComposedTextValue`
- `List<Map<String, String>>` → `SignatureBlockListValue`

Integra com `WorkContentBindingResolver` existente para suporte a `work` bindings.

**Modificados:**
- `DocumentContentRequest.java` — campos `cover`, `titlePage`, `approvalSheet` passam a ser `SinglePageContentRequest`
- `ComponentRulesRequest.java` — idem para as rules
- `AcademicWorkRequest.valueFor()` — `nature` retorna `Map<String, String>` em vez de `AcademicWorkNatureRequest`

**Removidos:** `CoverRequest`, `TitlePageRequest`, `ApprovalSheetRequest`, `TitlePageNatureRequest`, `ApprovalSheetNatureRequest`, `AcademicPersonRequest` (se não usada em outro lugar), todos os `*ComponentRuleRequest` dos 3 componentes

---

### D-7 — ProfileDefinition: SinglePageComponentRuleDefinition

**Modificado:** `profile/provider/ProfileDefinition.java` — `CoverComponentRuleDefinition`, `TitlePageComponentRuleDefinition`, `ApprovalSheetComponentRuleDefinition` substituídos por `SinglePageComponentRuleDefinition`

**Modificado:** `src/main/resources/profiles/abnt-unip-profile.json` — seções cover, titlePage, approvalSheet com novo formato de slots

---

### D-8 — RenderingConfig + limpeza final

- Remover todos os beans específicos dos 3 componentes
- Adicionar beans: `SinglePageContentValidator`, `SinglePageLayoutAssembler`, `SinglePageLayoutCalculator`
- Adicionar 3 beans `SinglePageRenderer` (um por componentId)
- Deletar os 53 arquivos de produção e 28 de teste listados no diagnóstico
- Atualizar os 18 samples JSON de cover/titlePage/approvalSheet

---

### D-9 — Testes e validação final

- `mvn compile -q && mvn test -q` — suite verde
- `grep -r "CoverComponent\|TitlePageComponent\|ApprovalSheetComponent" src/main/java` → zero
- Validação visual: documento com cover, titlePage e approvalSheet gerados corretamente
- Commit do bloco

---

## Arquivos que somem (53 produção + 28 teste)

Ver listagem completa no diagnóstico da sessão. Resumo por pacote:
- `document/component/cover/`, `document/component/titlepage/`, `document/component/approvalsheet/`
- `profile/model/component/cover/`, `profile/model/component/titlepage/`, `profile/model/component/approvalsheet/`
- `rendering/component/cover/`, `rendering/component/titlepage/`, `rendering/component/approvalsheet/`
- `api/export/dto/request/*Cover*`, `api/export/dto/request/*TitlePage*`, `api/export/dto/request/*ApprovalSheet*`
- `shared/exception/InvalidCoverContentException`, `InvalidTitlePageContentException`, `InvalidApprovalSheetContentException`
