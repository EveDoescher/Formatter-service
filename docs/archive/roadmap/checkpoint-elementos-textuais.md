# Checkpoint: Elementos Textuais - Tabelas

Este documento registra onde a implementação parou na etapa de tabelas dentro do
`bodyContent`. Ele deve servir como contexto de retomada para outra IA ou para
continuidade manual do desenvolvimento.

## Contexto Geral

O projeto já possui uma base reutilizável para componentes acadêmicos:

- `cover`, `titlePage` e `approvalSheet` usam dados semânticos do trabalho via
  `work` e bindings definidos pelo perfil.
- `bodyContent` já renderiza seções textuais com headings reais do Word.
- A numeração de páginas é controlada pelo perfil, com separação entre onde a
  contagem começa e onde a numeração passa a aparecer.
- Citações já foram modeladas:
  - citação direta curta inline;
  - citação indireta inline;
  - apud inline;
  - citação direta longa como bloco próprio.
- Figuras já foram modeladas como objetos numerados compostos:
  - legenda;
  - imagem;
  - fonte;
  - continuação explícita via `continuationGroupId`.

A etapa atual é adicionar tabelas seguindo a mesma arquitetura de objetos
numerados, sem tratar tabela como texto comum.

## Decisão Arquitetural Para Tabelas

Tabela deve ser um bloco semântico próprio dentro de:

```text
bodyContent.sections[].blocks[]
```

O tipo do bloco é:

```text
TABLE
```

A tabela deve ser renderizada como um conjunto composto:

```text
caption
table grid
source
```

Isso segue a mesma família conceitual das figuras. A diferença é que a mídia da
figura é uma imagem, enquanto a tabela é uma estrutura tabular real no DOCX.

## Regras Importantes

Não renderizar tabela como parágrafo com tabs.

Não hardcodar fonte, tamanho, alinhamento, largura, legenda, texto de fonte ou
labels de continuação no renderer.

Não deixar o usuário digitar `Tabela 1`.

O número da tabela deve ser calculado pelo sistema.

O perfil controla:

```text
bodyContent.table.captionStyleId
bodyContent.table.sourceStyleId
bodyContent.table.headerStyleId
bodyContent.table.cellStyleId
bodyContent.table.captionTemplate
bodyContent.table.sourceTemplate
bodyContent.table.continuationLabels
bodyContent.table.sourcePlacement
bodyContent.table.tableAlignment
bodyContent.table.widthPercent
bodyContent.table.repeatHeaderOnPageBreak
```

O conteúdo enviado pelo usuário controla:

```text
id
continuationGroupId
caption
source
columns
rows
```

## Continuação De Tabelas

Assim como figuras, tabelas não devem ser divididas semanticamente pelo
formatter. Se uma tabela precisa continuar, o usuário deve enviar partes
explícitas com o mesmo `continuationGroupId`.

Exemplo conceitual:

```text
Tabela 1 - Resultados consolidados (continua)
Tabela 1 - Resultados consolidados (continuação)
Tabela 1 - Resultados consolidados (conclusão)
```

Para duas partes:

```text
continua
conclusão
```

Para três ou mais partes:

```text
continua
continuação
conclusão
```

A fonte pertence ao grupo lógico. Se `sourcePlacement = LAST_PART_ONLY`, a fonte
aparece apenas na última parte.

## Implementado Até Aqui

Foram adicionados os modelos de domínio:

```text
BodyTable
BodyTableColumn
BodyTableRow
NumberedDisplayObject
```

`NumberedDisplayObject` agora é a abstração reutilizável para objetos numerados
de corpo textual, permitindo:

```text
BodyFigure
BodyTable
```

`BodyContentComponent` valida IDs duplicados separadamente:

```text
bodyContent figure id must be unique
bodyContent table id must be unique
```

Foram adicionados os DTOs de request:

```text
BodyTableRequest
BodyTableColumnRequest
BodyTableRowRequest
```

`BodyBlockType` passou a aceitar:

```text
TABLE
```

`BodyBlockRequest` passou a converter blocos `TABLE` para `BodyTable`.

Foi adicionada a regra de perfil:

```text
TableRule
TableRuleRequest
ProfileDefinition.TableRuleDefinition
```

`BodyContentComponentRule` agora exige:

```text
figure
table
```

O perfil oficial `abnt-unip-profile.json` foi atualizado com estilos próprios
para tabela:

```text
bodyContent.table.caption
bodyContent.table.source
bodyContent.table.header
bodyContent.table.cell
```

E com a regra:

```text
bodyContent.table
```

Foi adicionado o bloco DOCX:

```text
DocxTableBlock
```

`DocxBlock` passou a permitir `DocxTableBlock`.

`BodyContentRenderer` passou a renderizar tabela como:

```text
DocxParagraph caption
DocxTableBlock table
DocxParagraph source
```

Foi extraída uma estrutura de estado reutilizável para numeração/continuação:

```text
DisplayObjectRenderingState
DisplayObjectContinuationPart
```

Ela é usada para figuras e tabelas.

## Samples Criados

Sample válido:

```text
docs/samples/body-content/body-content-tables.json
```

Esse sample cobre:

- tabela simples;
- legenda acima;
- fonte abaixo;
- tabela continuada com `continuationGroupId`;
- fonte apenas na parte final.

Sample inválido:

```text
docs/samples/body-content/body-content-table-row-mismatch-invalid.json
```

Esse sample valida que uma linha com quantidade de células diferente da
quantidade de colunas deve falhar antes da geração do DOCX.

`BodyContentSampleValidationTest` já foi atualizado para incluir os dois.

## Documentos De Apoio Atualizados

Ao retomar esta etapa, ler em conjunto:

```text
docs/checkpoint_elementos_textuais_tabelas.md
docs/components/body-content-component.md
docs/samples/body-content/README.md
docs/roadmap_componentes_abnt_ia.md
```

O checkpoint explica o ponto exato da implementação. O documento do componente
explica o contrato permanente do `bodyContent`. O README lista os samples
oficiais. O roadmap reforça a direção arquitetural para componentes futuros.

## Testes Atualizados

Foram ajustados ou adicionados testes para:

- conversão de `TABLE` em `BodyBlockRequest`;
- conversão de `TableRuleRequest`;
- rejeição de `BodyTable` com ID duplicado;
- renderização de tabela continuada como um único objeto numerado;
- samples oficiais de tabela.

## Ponto Exato Onde Paramos

A implementação Java foi escrita, mas ainda precisa de validação de compilação
local.

O Maven no ambiente do Codex falhou por restrição de rede/cache:

```text
Non-resolvable parent POM
Could not transfer artifact org.springframework.boot:spring-boot-starter-parent
Permission denied: getsockopt
```

Ou seja, a falha não veio dos testes nem da implementação de tabela; o build não
conseguiu resolver o parent do Spring Boot nesse ambiente.

Também foi feita uma tentativa de conferir a API do docx4j via `javap`, mas o
comando não está disponível no ambiente atual.

## Principal Ponto De Atenção Ao Retomar

Validar a compilação de `Docx4jWriter`.

A parte mais provável de precisar ajuste fino é a repetição do cabeçalho da
tabela no docx4j:

```java
objectFactory.createCTTrPrBaseTblHeader(objectFactory.createBooleanDefaultTrue())
```

Se a API local do docx4j reclamar, o ajuste deve ser feito apenas na forma de
criar `tblHeader`, sem mudar a arquitetura.

Também conferir:

```java
TblBorders
CTBorder
TblWidth
TrPr
TcPr
```

A intenção está correta: gerar tabela DOCX real com bordas, largura percentual,
alinhamento e cabeçalho repetível. Se algum nome de método/classe variar na
versão do docx4j, ajustar no writer mantendo o contrato de `DocxTableBlock`.

## Próximos Passos

1. Rodar:

```text
mvn clean test
```

2. Se houver erro de compilação no `Docx4jWriter`, ajustar apenas a chamada
   específica da API docx4j.

3. Se os testes passarem, gerar visualmente:

```text
body-content-tables.json
```

4. Conferir no Word:

- legenda acima da tabela;
- tabela como tabela real;
- fonte abaixo;
- fonte 10 no caption/source/header/cell conforme perfil;
- continuação com labels corretos;
- fonte aparecendo apenas na conclusão quando `LAST_PART_ONLY`;
- cabeçalho repetindo quando a tabela atravessar página.

5. Depois de validado, atualizar o roadmap geral marcando tabelas como
   implementadas.

## Regras Que Não Devem Ser Quebradas

Não mover decisões de perfil para código.

Não criar fallback silencioso para tabela.

Não repetir dados do trabalho quando o mecanismo de `work` e bindings resolver.

Não usar nomes reais em samples.

Não transformar tabela em texto normal.

Não automatizar continuação de tabela de forma invisível neste momento. A
continuação deve ser explícita via `continuationGroupId`.

Não gerar fonte em todas as partes se o perfil declarar `LAST_PART_ONLY`.

Não gerar `Tabela 1` a partir do usuário; o sistema calcula a numeração.
