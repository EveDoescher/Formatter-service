# Roadmap IA para componentes ABNT

Este documento resume o estado atual do `formatter-service`, o padrao arquitetural
que foi consolidado com `cover` e `titlePage`, e a ordem recomendada para os
proximos componentes.

O objetivo e permitir que uma IA ou pessoa continue o desenvolvimento sem depender
do historico do chat.

## Estado atual

O projeto ja possui dois componentes academicos reais:

```text
cover
titlePage
```

Tambem existe:

```text
paragraphs
```

`paragraphs` e um componente interno/provisorio para texto simples e smoke tests.
Ele pode aparecer em `componentOrder`, mas nao deve ser usado como modelo de
componente academico.

## O que ja foi consolidado

### Perfil oficial em JSON

O perfil oficial fica em:

```text
src/main/resources/profiles/abnt-unip-profile.json
```

O perfil define:

```text
pageRule
styleRules
componentRules
componentOrder
```

`componentOrder` e obrigatorio. O codigo nao deve inventar uma ordem padrao.

### Contrato profile-driven

Regra central:

```text
Perfil define modelo visual e estrutural.
Usuario fornece conteudo semantico.
Codigo calcula tudo que deriva dessas informacoes.
```

Isso significa:

```text
Nao hardcodar decisoes visuais no componente.
Nao deixar request receber valores tecnicos de layout.
Nao usar valores magicos para empurrar conteudo.
Nao fazer fallback silencioso quando o perfil esta incompleto.
```

O usuario pode fornecer conteudo e preferencias permitidas pelo contrato do
componente, mas nao deve fornecer:

```text
quantidade fixa de linhas vazias globais
lineHeightTwips
safeLineCapacity
bottomPaddingLineSlots
maxCharactersPerLine
indentacao tecnica para caber na pagina
```

### Style rules rigidas

No loader de perfil JSON, os campos abaixo sao obrigatorios:

```text
bold
italic
uppercase
```

Eles nao devem virar `false` automaticamente se forem omitidos. Perfil incompleto
deve falhar cedo.

### Component order obrigatorio

`DocumentProfile` exige:

```text
componentOrder
```

Nao existe mais construtor alternativo que gera ordem automaticamente.

`ProfileRequest` inline tambem exige `componentOrder`.

### Single-page reutilizavel

Foi criada uma base reutilizavel para componentes que precisam caber em uma unica
pagina.

Exemplos de componentes que podem usar essa base:

```text
cover
titlePage
approvalSheet
dedication curta
epigraph
agradecimentos curtos
```

A base single-page trabalha com:

```text
SinglePageLayoutRule
SinglePageGroupRule
SinglePageItemRule
LayoutGapRule
SinglePageLayoutPolicy
SinglePageLayoutEngine
SinglePageLayoutPlan
SinglePageLayoutRenderer
SinglePageLayoutDiagnostic
```

### Medicao vs renderizacao

Decisao atual:

```text
Textos naturais podem ser renderizados como um unico DocxParagraph.
O Word pode quebrar o paragrafo.
```

Mas isso so e aceitavel se:

```text
a medicao usou a mesma largura horizontal;
o paragrafo renderizado usa o mesmo estilo;
os mesmos recuos sao aplicados;
o mesmo espacamento e aplicado;
a area segura possui folga para pequenas diferencas do Word.
```

O medidor estima altura e risco de overflow. Ele nao substitui validacao visual
no Microsoft Word quando a mudanca afeta quebra de linha.

### Cidade e ano

Para componentes single-page com bloco final:

```text
city
year
```

A regra visual de colocar `year` na ultima linha segura e `city` imediatamente
acima deve ser derivada da politica de layout, nao de padding hardcoded.

`city` e `year` podem ter limite visual declarado no perfil:

```text
maxVisualLinesPerValue = 1
```

Se nao couberem no limite declarado, a geracao deve falhar antes do DOCX.

### blankLinesAfter

`blankLinesAfter` existe no perfil como microespacamento interno de item.

Uso correto:

```text
separar natureza, orientador e coorientador dentro do mesmo bloco;
criar uma linha de respiro entre itens semanticamente proximos.
```

Uso incorreto:

```text
empurrar grupo para baixo;
simular distancia global entre blocos;
corrigir overflow;
substituir gapRules.
```

Distancia entre grupos deve ser feita com:

```text
gapRules
```

### Renderer por registry

`DocumentRenderer` nao deve conhecer cada componente academico diretamente.

O fluxo correto e:

```text
ExportDocxCommand.documentComponents()
+ ComponentRendererRegistry
+ componentOrder
+ ComponentRenderer
```

`paragraphs` continua sendo um componente interno especial, porque ainda nao tem
`DocumentComponent` proprio.

Novos componentes academicos devem entrar registrando um novo `ComponentRenderer`.
Nao adicionar novo `case "novoComponente"` em `DocumentRenderer`.

Antes de adicionar qualquer novo componente academico, confirmar que o
`DocumentRenderer` renderiza componentes via:

```text
ExportDocxCommand.documentComponents()
+ ComponentRendererRegistry
+ componentOrder
```

`paragraphs` pode continuar como caso interno especial ate virar uma estrutura
textual propria.

## Componentes existentes

### cover

`cover` e uma pagina unica.

Modelo semantico:

```text
institutionalLines
authors
title
subtitle
city
year
```

Principios:

```text
Nao existe bottomLines no dominio principal.
Cidade e ano sao semanticos.
Perfil decide estilos, grupos, gaps e limites visuais.
Componente calcula layout e falha se nao couber em uma pagina segura.
```

### titlePage

`titlePage` e uma pagina unica.

Modelo semantico:

```text
authors
title
subtitle
nature.workType
nature.degreeObjective
nature.courseName
nature.institutionName
advisor
coadvisor
city
year
```

Principios:

```text
Usuario nao fornece texto final da natureza.
Perfil define templates de natureza, orientador e coorientador.
Perfil define posicionamento horizontal, incluindo meia pagina direita.
blankLinesAfter separa natureza/orientador/coorientador quando necessario.
```

## Proxima implementacao recomendada

O proximo componente deve ser:

```text
approvalSheet
```

Em portugues:

```text
folha de aprovacao
```

Motivo:

```text
confirma que single-page nao ficou viciado em cover/titlePage;
continua no universo pre-textual controlado;
reaproveita medicao, grupos, gaps, safe area e renderer;
e uma boa prova de que a arquitetura serve para o terceiro componente.
```

Essa recomendacao e arquitetural. Ela prova reutilizacao do `single-page` antes
de novas familias de componentes. Nao significa necessariamente que
`approvalSheet` seja a maior prioridade comercial do produto.

## Ordem de implementacao vs ordem de renderizacao

A ordem recomendada neste documento nao e a ordem final do documento academico.

Exemplo:

```text
summary
listOfFigures
listOfTables
```

podem ser implementados depois da base textual porque dependem de metadados e
numeracao de paginas, mas continuam sendo elementos pre-textuais e devem ser
renderizados na posicao definida por `componentOrder` do perfil.

## Como implementar approvalSheet

### 1. Criar modelo semantico

Criar pacote:

```text
src/main/java/com/abntbuilder/formatter/document/component/approvalsheet
```

Modelo sugerido:

```text
ApprovalSheetComponent
ApprovalCommitteeMember
ApprovalSheetNature
ApprovalEvent
```

Campos sugeridos para `ApprovalSheetComponent`:

```text
authors
title
subtitle
nature
approvalEvent
committeeMembers
city
year
```

`approvalEvent` pode representar dados de aprovacao que variam por instituicao:

```text
location
date
approvalTextData
```

Esses campos devem ser opcionais conforme o perfil. A obrigatoriedade deve ser
validada contra a regra do componente e os templates usados, nao fixada no
dominio como regra universal.

Campos sugeridos para `ApprovalSheetNature`:

```text
workType
degreeObjective
courseName
institutionName
```

Campos sugeridos para `ApprovalCommitteeMember`:

```text
name
title
institutionName
role
```

`role` deve ser opcional ou tratado como ponto de evolucao para enum/configuracao
futura. Algumas folhas exibem `Orientador`, `Examinador`, `Convidado`; outras nao
exibem papel. O perfil decide se o papel aparece no template do membro da banca.

Observacao:

```text
Nao hardcodar "Aprovado em" no renderer se isso puder variar por perfil.
Nao hardcodar "Banca examinadora" no renderer se isso puder variar por perfil.
Textos institucionais devem vir de templates do perfil.
```

Ficha catalografica nao faz parte de `titlePage` nem de `approvalSheet`. Ela deve
ser componente proprio futuro, com regra propria, especialmente por envolver verso
ou posicao especifica em algumas instituicoes.

### 2. Criar request semantico

Criar DTOs em:

```text
src/main/java/com/abntbuilder/formatter/api/export/dto/request
```

Sugestoes:

```text
ApprovalSheetRequest
ApprovalCommitteeMemberRequest
ApprovalSheetNatureRequest
```

O request deve converter para dominio.

Ele nao deve receber:

```text
linhas vazias globais
pesos
recuos calculados
textos finais montados
flags para caber na pagina
```

### 3. Adicionar no document content

Atualizar `DocumentContentRequest` para aceitar:

```text
approvalSheet
```

Atualizar `ExportDocxRequest` para resolver o novo componente e inclui-lo no
`ExportDocxCommand`.

Importante:

```text
Nao adicionar switch em DocumentRenderer.
Adicionar o componente na colecao retornada por ExportDocxCommand.documentComponents().
```

### 4. Criar rule de perfil

Criar pacote:

```text
src/main/java/com/abntbuilder/formatter/profile/model/component/approvalsheet
```

Classes sugeridas:

```text
ApprovalSheetComponentRule
ApprovalSheetStyleMapping
ApprovalSheetTextTemplateRule
```

O layout deve reutilizar:

```text
SinglePageLayoutRule
SinglePageGroupRule
SinglePageItemRule
LayoutGapRule
SinglePageLayoutPolicy
```

Nao criar um layout engine proprio se o single-page resolver o problema.

### 5. Definir grupos no perfil

Grupos iniciais sugeridos:

```text
approvalSheet.authors
approvalSheet.titleBlock
approvalSheet.natureBlock
approvalSheet.approvalBlock
approvalSheet.committeeBlock
approvalSheet.bottom
```

Itens sugeridos:

```text
authors
title
subtitle
nature
approvalText
committeeMembers
city
year
```

O perfil deve decidir:

```text
styleMapping
textTemplates
groups
items
gapRules
policy
horizontalPlacement
blankLinesAfter quando for microespacamento interno
maxVisualLinesPerValue quando houver limite semantico/visual claro
```

Templates esperados para `approvalSheet` podem incluir:

```text
natureTemplate
approvalTextTemplate
committeeMemberTemplate
signatureLineTemplate
```

Cada `committeeMember` deve ser convertido por template do perfil.

Exemplo conceitual:

```text
{title} {name}
{institutionName}
{role}
```

O assembler deve gerar uma instancia de item por membro ou um subgrupo calculado,
sem hardcodar linhas de assinatura, nomes ou instituicoes no renderer.

Linhas de assinatura, quando existirem, devem ser itens estruturais do perfil,
nao strings hardcoded no renderer.

Possivel modelo futuro:

```text
signatureLine.enabled
signatureLine.widthCm
signatureLine.position
```

Para MVP, a assinatura pode ser representada por texto/template, por exemplo
underscores vindos do perfil. Se essa solucao for usada, documentar como
transitoria.

### 6. Criar assembler/calculator/renderer

Pacote sugerido:

```text
src/main/java/com/abntbuilder/formatter/rendering/component/approvalsheet
```

Classes sugeridas:

```text
ApprovalSheetRenderer
ApprovalSheetLayoutCalculator
ApprovalSheetLayoutAssembler
ApprovalSheetProfileContentValidator
ApprovalSheetTextTemplateResolver
```

Padrao esperado:

```text
Renderer nao calcula gaps.
Renderer nao mede texto.
Renderer nao monta decisoes visuais.
Assembler transforma conteudo semantico + perfil em SinglePageLayoutInput.
SinglePageLayoutEngine calcula plano.
SinglePageLayoutRenderer renderiza plano.
```

### 7. Registrar renderer

Atualizar configuracao de renderizacao para incluir:

```text
ApprovalSheetRenderer
```

O `DocumentRenderer` deve funcionar sem alteracao estrutural.

### 8. Atualizar profile JSON

Em `abnt-unip-profile.json`, adicionar:

```text
approvalSheet
```

Em:

```text
componentOrder
componentRules
styleRules
```

Exemplo de ordem futura:

```text
cover
titlePage
approvalSheet
paragraphs
```

Se o componente ainda nao for obrigatorio no documento, ele pode aparecer na ordem
e simplesmente nao renderizar quando o request nao trouxer conteudo.

### 9. Criar samples

Criar pasta:

```text
docs/samples/approval-sheet
```

Samples minimos:

```text
approval-sheet-short.json
approval-sheet-long-title.json
approval-sheet-many-committee-members.json
approval-sheet-overflow.json
approval-sheet-bottom-wrap-invalid.json
cover-title-page-approval-sheet.json
README.md
```

Os invalidos devem falhar antes da geracao DOCX.

### 10. Criar testes

Testes esperados:

```text
ApprovalSheetRequestTest
ApprovalSheetComponentTest
ApprovalSheetTextTemplateResolverTest
ApprovalSheetProfileContentValidatorTest
ApprovalSheetLayoutAssemblerTest
ApprovalSheetLayoutCalculatorTest
ApprovalSheetRendererTest
ApprovalSheetRendererDocxSanityTest
ApprovalSheetSampleValidationTest
DocxExportControllerIntegrationTest cobrindo o componente no endpoint
```

Tambem atualizar testes de registry se necessario.

## Depois da approvalSheet

Depois que `approvalSheet` provar que `single-page` e reutilizavel, a ordem
recomendada e mudar para a base textual.

## Base textual

Criar uma base para elementos que fluem por multiplas paginas.

Nome conceitual:

```text
body-content
flow-section
```

Ela deve atender:

```text
introducao
desenvolvimento
conclusao
secoes
subsecoes
paragrafos
citacoes longas
listas
legendas
notas simples
```

Principio importante:

```text
Componentes textuais nao devem falhar so porque passaram de uma pagina.
Eles devem fluir naturalmente.
```

O que essa base precisa produzir para o futuro:

```text
metadados de titulos
metadados de figuras
metadados de tabelas
metadados de secoes
identificadores estaveis
ordem documental
```

Esses metadados serao usados depois por:

```text
sumario
lista de ilustracoes
lista de tabelas
```

`resumo` e `abstract` devem reutilizar a base textual/flow, mas nao devem virar
simples `body-content` generico. Eles sao componentes pre-textuais proprios, com:

```text
titulo proprio
texto de resumo/abstract
palavras-chave ou keywords
regras de inclusao no perfil
posicao definida por componentOrder
```

`agradecimentos` tambem pode usar flow quando for longo, mas ainda deve ser
tratado como componente pre-textual proprio se tiver regra institucional.

## Pos-textuais

Depois da base textual, implementar:

```text
references
appendix
annex
glossary
```

### references

`references` provavelmente precisa de base propria.

Nao tratar referencias como simples paragrafo solto se o objetivo for ABNT bem
formatada.

Modelo futuro pode receber:

```text
autores
titulo
edicao
local
editora
ano
url
acesso
tipo de fonte
```

Ou, em uma etapa intermediaria, pode receber entradas ja normalizadas, mas isso
deve ser explicitamente documentado como transicao.

### appendix e annex

Podem reaproveitar a base textual, mas precisam de titulo especial:

```text
APENDICE A - ...
ANEXO A - ...
```

A numeracao/letras devem ser calculadas pelo codigo, nao digitadas manualmente
pelo usuario, se o componente prometer normalizacao automatica.

## Voltar aos pre-textuais derivados

So depois de existir base textual e metadados confiaveis, voltar para:

```text
summary
listOfFigures
listOfTables
listOfAbbreviations
listOfSymbols
```

Motivo:

```text
sumario depende de titulos e paginas;
lista de figuras depende de legendas e paginas;
lista de tabelas depende de legendas e paginas;
listas derivadas dependem de estrutura rastreavel.
```

Nao implementar sumario antes de existir uma fonte confiavel de secoes.

Alerta importante:

```text
metadados nao bastam;
generated-index tambem depende de estrategia para page numbers.
```

Possiveis estrategias futuras:

```text
campos do Word;
TOC field;
atualizacao manual pelo Word/usuario;
etapa futura de pos-processamento;
renderizacao em duas fases.
```

Nao prometer sumario ou listas 100% automaticos antes de definir como os numeros
de pagina serao obtidos/atualizados com confiabilidade.

## Familias de componentes

### single-page

Usar quando:

```text
o componente deve caber em uma pagina;
se nao couber, deve falhar;
layout depende de grupos e gaps;
posicao vertical e controlada.
```

Exemplos:

```text
cover
titlePage
approvalSheet
dedication curta
epigraph
```

### flow-content

Usar quando:

```text
o conteudo pode passar de uma pagina;
Word pode quebrar paginas;
o importante e manter estilos, hierarquia e fluxo.
```

Exemplos:

```text
agradecimentos longos
introducao
desenvolvimento
conclusao
glossario
```

Observacao:

```text
resumo e abstract usam a base flow-content,
mas devem ser componentes pre-textuais proprios.
```

### generated-index

Usar quando:

```text
o componente depende de outros elementos do documento;
precisa coletar metadados;
precisa gerar entradas automaticamente.
```

Exemplos:

```text
sumario
lista de figuras
lista de tabelas
indice
```

### references

Usar quando:

```text
a formatacao depende de tipo bibliografico;
ordem, pontuacao e campos sao especificos;
validacao semantica importa.
```

## Checklist obrigatorio para novo componente academico

Antes de considerar pronto:

```text
1. Modelo semantico criado.
2. Request nao contem parametros tecnicos de layout.
3. ComponentRule existe no perfil.
4. styleMapping e explicito.
5. componentOrder contem o novo id.
6. Renderer registrado em ComponentRendererRegistry.
7. DocumentRenderer nao foi alterado com switch novo.
8. Validadores rejeitam perfil incompleto.
9. Samples validos existem.
10. Samples invalidos existem e falham antes do DOCX.
11. Testes unitarios cobrem dominio, request, templates e layout.
12. Teste sanity DOCX confirma XML esperado.
13. Teste de integracao cobre endpoint.
14. Documento gerado foi validado visualmente no Word.
```

## Anti-padroes proibidos

Nao fazer:

```text
hardcode de texto institucional no renderer;
hardcode de espacamento visual no renderer;
fallback silencioso para perfil incompleto;
novo switch de componente academico em DocumentRenderer;
request com lineHeightTwips, safeLineCapacity ou maxCharactersPerLine;
layout calculado duplicado dentro do componente;
gerar DOCX para descobrir depois se quebrou pagina;
usar blankLinesAfter para posicionamento global;
misturar modelo legado com modelo semantico novo;
criar engine propria quando single-page ja resolve.
```

## Ordem recomendada a partir de agora

```text
1. approvalSheet usando single-page.
2. body-content/flow-section para corpo textual.
3. references, appendix, annex e glossario.
4. summary, listOfFigures e listOfTables usando metadados reais.
5. outros pre-textuais derivados ou opcionais.
```

## Regra de ouro

Se surgir duvida entre colocar algo no request, no perfil ou no codigo:

```text
Conteudo semantico pertence ao request.
Decisao visual/estrutural pertence ao perfil.
Calculo derivado pertence ao codigo.
```

Essa regra deve guiar todos os proximos componentes.
