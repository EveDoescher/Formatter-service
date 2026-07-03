# Formatter Service — Ecosystem Contract

Este documento descreve o que os outros microserviços do ecossistema precisam saber sobre o
formatter-service: o que ele faz, como chamá-lo, quais partes do contrato são estáveis hoje e
quais ainda vão evoluir.

---

## O que o formatter-service faz

Recebe uma requisição com três partes — **profile**, **preferences** e **content** — e devolve
um arquivo DOCX formatado. Não persiste nada, não conhece usuários, não toma decisões de negócio.
É um executor puro: o profile declara as regras, o serviço executa.

---

## Endpoint

```
POST /api/v1/exports/docx
Content-Type: application/json
→ 200 application/vnd.openxmlformats-officedocument.wordprocessingml.document (bytes do DOCX)
→ 400 JSON de erro de validação
→ 500 falha de geração
```

**Autenticação:** ainda não implementada. Quando o IAM-service estiver integrado, o formatter
esperará um header `X-Account-Id` injetado pelo gateway após validação do token. O formatter não
valida JWT diretamente — apenas lê o header para fins de auditoria futuros.

---

## Estrutura da requisição

```json
{
  "fileName": "nome-do-arquivo-sem-extensao",
  "profileId": "abnt-unip-profile",
  "options": {
    "selectedComponents": ["cover", "titlePage", "bodyContent"]
  },
  "document": {
    "cover": { ... },
    "titlePage": { ... },
    "approvalSheet": { ... },
    "bodyContent": { ... }
  }
}
```

### `profileId`

ID do perfil de formatação a usar. Atualmente o único perfil existente é `abnt-unip-profile`.
O profile-service será responsável por fornecer esses perfis quando o formatter for integrado ao
ecossistema. Por enquanto o formatter carrega perfis do classpath.

O profile JSON completo está em:
`src/main/resources/profiles/abnt-unip-profile.json`

### `options.selectedComponents`

Lista de IDs dos componentes que devem aparecer no documento gerado. A ordem no documento é
sempre determinada pelo profile (campo `componentOrder`) — a lista aqui é apenas uma seleção,
não uma ordenação.

Componentes disponíveis no `abnt-unip-profile` (na ordem do documento):

```
cover, titlePage, errata, approvalSheet, dedication, acknowledgments, epigraph,
resumo, abstract, listOfAbbreviations, listOfSymbols, summary, listOfFigures,
listOfTables, listOfFrames, listOfCharts, listOfCodeListings, bodyContent,
references, appendix, annex, glossary
```

**Restrição de paginação:** o perfil declara que a contagem de páginas começa em `titlePage` e
os números aparecem a partir de `bodyContent`. Se `selectedComponents` incluir qualquer
componente após `titlePage` na ordem, `titlePage` também precisa estar presente na lista.
O formatter rejeita com 400 se essa regra for violada.

### `document`

Mapa com o conteúdo de cada componente selecionado. Componentes não presentes são ignorados.

---

## Componentes estáveis (safe to contract now)

Os componentes abaixo têm formato JSON definido e não serão alterados nos próximos blocos de
implementação.

### cover

```json
"cover": {
  "institutionalLines": ["UNIVERSIDADE FICTÍCIA DE LIMEIRA", "CURSO DE ADS"],
  "authors": ["PESSOA AUTORA TESTE 01"],
  "title": "TÍTULO DO TRABALHO EM MAIÚSCULAS",
  "subtitle": "Subtítulo opcional",
  "city": "Limeira",
  "year": "2026"
}
```

`institutionalLines` e `authors` são `List<String>`. `subtitle` é opcional.

Samples: `docs/samples/cover/`

### titlePage

```json
"titlePage": {
  "authors": ["Pessoa Autora Teste 01"],
  "title": "Título do Trabalho",
  "subtitle": "Subtítulo opcional",
  "nature": {
    "workType": "Trabalho de Conclusão de Curso",
    "degreeObjective": "obtenção do título de tecnólogo",
    "courseName": "Análise e Desenvolvimento de Sistemas",
    "institutionName": "Universidade Fictícia de Limeira"
  },
  "advisor": {
    "academicTitle": "Prof. Dr.",
    "name": "Pessoa Orientadora Teste"
  },
  "coadvisor": {
    "academicTitle": "Profa. Ma.",
    "name": "Pessoa Coorientadora Teste"
  },
  "city": "Limeira",
  "year": "2026"
}
```

`nature`, `advisor`, `coadvisor` são `Map<String, String>`. `subtitle` e `coadvisor` são opcionais.

Samples: `docs/samples/title-page/`

### approvalSheet

```json
"approvalSheet": {
  "authors": ["Pessoa Autora Teste 01"],
  "title": "Título do Trabalho",
  "subtitle": "Subtítulo opcional",
  "nature": {
    "workType": "Trabalho de Conclusão de Curso",
    "degreeObjective": "obtenção do título de tecnólogo",
    "courseName": "Análise e Desenvolvimento de Sistemas",
    "institutionName": "Universidade Fictícia de Limeira"
  },
  "approvalText": "Aprovado(a) em: ______/______/______",
  "committeeHeading": "BANCA EXAMINADORA",
  "committeeMembers": [
    {
      "name": "Pessoa Orientadora Teste",
      "title": "Prof. Dr.",
      "institutionName": "Universidade Fictícia de Limeira",
      "role": "Orientador"
    }
  ]
}
```

`committeeMembers` é `List<Map<String, String>>`. `subtitle` e `committeeMembers` são opcionais.

Samples: `docs/samples/approval-sheet/`

### bodyContent

Componente mais complexo e mais estável. Estrutura de seções recursivas com blocos de conteúdo.

```json
"bodyContent": {
  "sections": [
    {
      "id": "introducao",
      "level": 1,
      "title": "Introdução",
      "blocks": [
        {
          "type": "PARAGRAPH",
          "content": [
            { "type": "TEXT", "text": "Texto normal." },
            { "type": "TEXT", "text": "negrito", "formatting": { "bold": true } }
          ]
        }
      ],
      "subsections": [
        {
          "id": "sub1",
          "level": 2,
          "title": "Subseção",
          "blocks": []
        }
      ]
    }
  ]
}
```

`level` começa em 1. O primeiro nível da árvore deve ser nível 1 — iniciar em nível 2 é inválido
e retorna 400.

Tipos de bloco suportados: `PARAGRAPH`, `FIGURE`, `TABLE`, `FRAME`, `CHART`, `CODE_LISTING`,
`EQUATION`, `ORDERED_LIST`, `UNORDERED_LIST`, `DIRECT_LONG_QUOTE`, `FOOTNOTE`, `ABBREVIATION`,
`CROSS_REFERENCE`.

Tipos de inline dentro de `PARAGRAPH`: `TEXT` (com `formatting` opcional), `CITATION`,
`QUOTE_TEXT`, `CROSS_REFERENCE`.

Samples: `docs/samples/body-content/`  
Sample completo com todos os tipos: `docs/samples/composed/full-document-integration-test.json`

---

## Componentes em evolução (não contratar ainda)

Os componentes abaixo existem e funcionam, mas o **Bloco C-corrigido** (próximo a ser
implementado) vai mudar sua estrutura JSON para eliminar tipos Java específicos. O formato atual
será substituído por um modelo FLOW genérico declarado no profile.

Plano completo: `docs/superpowers/plans/bloco-c-corrigido.md`

| Componente | Formato atual | Mudará em |
|---|---|---|
| `dedication` | `{ "text": "..." }` | Bloco C-corrigido |
| `acknowledgments` | `{ "text": "..." }` | Bloco C-corrigido |
| `epigraph` | `{ "text": "...", "author": "...", "source": "..." }` | Bloco C-corrigido |
| `resumo` | `{ "text": "...", "keywords": [...] }` | Bloco C-corrigido |
| `abstract` | `{ "entries": [{ "headingText": "...", "text": "...", "keywords": [...] }] }` | Bloco C-corrigido |
| `glossary` | `{ "entries": [{ "term": "...", "definition": "..." }] }` | Bloco C-corrigido |
| `errata` | `{ "entries": [{ ... }] }` | Bloco C-corrigido |
| `listOfFigures` / `listOfTables` / `listOfFrames` / `listOfCharts` / `listOfCodeListings` / `listOfAbbreviations` / `listOfSymbols` | Gerados automaticamente do conteúdo — não precisam de dados na requisição | Bloco C-corrigido (estrutura interna muda, mas do ponto de vista da requisição continuarão sem campos) |

O componente `references` também funciona hoje mas o **Bloco F** vai reestruturar o sistema de
formatação das entradas. O formato JSON das entradas (`type`, `authors`, `title`, etc.) é estável;
o que muda é como o profile declara a ordem dos campos por norma.

Plano completo: `docs/superpowers/plans/bloco-f-referencias.md`

Formato atual de uma entrada de referência (estável):
```json
{
  "id": "ref1",
  "type": "BOOK",
  "authors": [{ "surname": "Lima", "givenNames": "Carlos Eduardo" }],
  "title": "Fundamentos de Sistemas Distribuídos",
  "year": "2021",
  "city": "São Paulo",
  "publisher": "Editora Exemplo"
}
```

Tipos suportados: `BOOK`, `BOOK_CHAPTER`, `JOURNAL`, `WEBSITE`, `LEGISLATION`, `THESIS`,
`CONFERENCE_PAPER`, `REPORT`, `STANDARD`.

Sample: `docs/samples/references/references-mixed.json`

---

## O que o work-service precisa saber

O work-service persiste os dados do trabalho acadêmico que serão enviados ao formatter. A
recomendação é persistir o campo `document` como blob (JSONB ou equivalente) e extrair para
colunas estruturadas apenas os campos necessários para busca — tipicamente `title` e `authors`.

O motivo: os campos de componentes como `dedication`, `resumo` e `abstract` ainda vão mudar no
Bloco C-corrigido. Persistir como blob isola o work-service dessas mudanças.

Os campos de `cover`, `titlePage` e `approvalSheet` são estáveis e podem ser persistidos
estruturadamente se o work-service precisar fazer queries sobre eles.

---

## O que o profile-service precisa saber

O profile-service armazena e entrega perfis de formatação por `profileId`. Do ponto de vista do
formatter, a integração é:

```
GET /profiles/{profileId}  →  JSON do profile completo
```

O formatter chamará esse endpoint no lugar de carregar do classpath (mudança apenas na camada de
input — o engine não muda).

O formato do profile JSON está em `src/main/resources/profiles/abnt-unip-profile.json`. Esse
formato ainda vai evoluir no Bloco C-corrigido (adição de `flowRules` para os componentes
textuais simples) e no Bloco F (adição de `entryFormats` para referências bibliográficas). A
recomendação é o profile-service tratar o profile como blob opaco até esses blocos estarem
concluídos.

---

## Sequência de blocos pendentes

| Bloco | O que faz | Impacto no contrato |
|---|---|---|
| **C-corrigido** | Migra dedication, resumo, abstract, epigraph, acknowledgments, glossary, errata para modelo FLOW genérico | Muda o JSON dos 7 componentes listados acima |
| **E** | Pós-processamento (numeração de páginas, índices automáticos) | Sem impacto no formato da requisição |
| **F** | Formatação de referências bibliográficas declarativa no profile | Sem impacto no JSON de `references.entries` — muda apenas o profile internamente |

Após C-corrigido + E + F: o contrato da requisição está congelado. Novos perfis (APA, Vancouver,
outros) não mudam o formato JSON — só o profile JSON muda.

Planos detalhados de cada bloco: `docs/superpowers/plans/`
