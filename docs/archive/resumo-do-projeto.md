# Resumo do Projeto — Formatter Service

## O que é

Um **microserviço de geração de documentos acadêmicos** no padrão ABNT.
Ele recebe conteúdo estruturado (título, autores, seções, citações, etc.) + um perfil de formatação, e devolve um arquivo `.docx` pronto, com toda a diagramação calculada automaticamente.

O primeiro perfil real é o `abnt-unip-profile`, mas a arquitetura é propositalmente genérica para suportar outros perfis no futuro (outras normas, outras instituições).

## Stack

- **Java 21 + Spring Boot 4**
- **docx4j** para geração do `.docx`
- Maven como build tool

## A ideia central

A separação de responsabilidades é o coração do projeto:

```
Conteúdo    → vem do usuário (autores, título, seções...)
Regras      → vem do perfil (fontes, margens, espaçamento, templates...)
Cálculo     → feito pelo código (linhas, gaps, overflow, blocos DOCX)
```

Nenhuma dessas camadas deve invadir a outra. Sem hardcode de valores acadêmicos no código Java.

## Fluxo de uma requisição

```
POST /api/v1/exports/docx
    → ExportDocxRequest (valida e monta o comando)
    → DocxExportService
    → DocumentRenderer (itera o componentOrder do perfil)
    → ComponentRendererRegistry (despacha para o renderer certo)
    → Renderer → Assembler → LayoutEngine → LayoutRenderer
    → List<DocxBlock> por componente
    → DocxDocument
    → Docx4jWriter.write() → byte[]
    → resposta HTTP com o arquivo
```

## Componentes acadêmicos implementados

| Componente | Tipo | Descrição |
|---|---|---|
| `cover` | single-page | Capa do trabalho |
| `titlePage` | single-page | Folha de rosto |
| `approvalSheet` | single-page | Folha de aprovação |
| `bodyContent` | flow-content | Corpo textual (seções, parágrafos, citações, figuras, tabelas) |
| `paragraphs` | legado interno | Smoke tests antigos, não usar como modelo |

## Dois tipos de componentes

### Single-page (`cover`, `titlePage`, `approvalSheet`)
- Devem caber em **exatamente uma página**
- Se não couberem → falha antes de gerar o DOCX
- Usam o motor compartilhado `SinglePageLayoutEngine`
- O perfil define grupos (`top`, `center`, `bottom`), pesos de gap e política de distribuição
- O motor calcula quantas linhas sobram e distribui os espaços

### Flow-content (`bodyContent`)
- Pode ocupar **quantas páginas forem necessárias**
- Word quebra as páginas naturalmente
- Renderiza seções com títulos reais de heading Word, parágrafos, citações inline e em bloco, figuras e tabelas numeradas

## Sistema de perfil

O perfil oficial fica em `src/main/resources/profiles/abnt-unip-profile.json`.

Ele define tudo que é visual/estrutural:
- `pageRule` — tamanho e margens da página
- `styleRules` — estilos por id (fonte, tamanho, negrito, espaçamento...)
- `componentRules` — regras específicas de cada componente (grupos, gaps, templates, mapeamento de estilos)
- `componentOrder` — ordem obrigatória de renderização
- `pageNumberingRule` — onde começa a contar e onde aparece o número
- `contentBindings` — mapeamento de campos do componente para dados do `work`

## Dados compartilhados (`work`)

Para evitar repetição, dados comuns (autores, título, natureza, orientador, cidade, ano) ficam no campo `work` do request. O perfil declara `contentBindings` mapeando quais campos de cada componente leem de `work`. Um componente pode ter um override explícito que vence o binding.

## Onde cada coisa fica

```
api/export/controller     → endpoint HTTP
api/export/dto/request    → DTOs de entrada, conversão para command
application/export        → DocxExportService (caso de uso)
document/component        → modelos semânticos (sem Spring, sem docx4j)
profile/model             → regras do perfil (sem docx4j)
profile/resolution        → carregamento e resolução do perfil
rendering/component       → renderers por componente
rendering/layout          → motor single-page + medição de texto
output/docx/api           → interface DocxWriter (abstração)
output/docx/docx4j        → Docx4jWriter (único lugar com docx4j)
shared                    → MeasurementConverter, exceções tipadas
```

## Regras que não podem ser quebradas

1. `document` e `profile/model` não importam docx4j nem Spring
2. Renderers não importam docx4j
3. Nenhum valor acadêmico hardcoded no Java (fontes, margens, labels como "Orientador")
4. Perfil incompleto → falha imediata com exceção clara, sem fallback silencioso
5. Novos componentes entram via `ComponentRendererRegistry`, nunca com `switch` em `DocumentRenderer`
6. `blankLinesAfter` é micro-espaçamento interno de item; distância entre grupos usa `gapRules`
7. Exemplos e testes usam `Limeira` como cidade (nunca `Campinas`)

## Branch atual

`feature/elementos-textuais` — está desenvolvendo o `bodyContent`, que é a base textual do documento (seções, parágrafos, citações, figuras, tabelas).
