# Cover como componente de referencia excelente

## 1. Objetivo deste documento

Este documento define o que falta para o componente de capa deixar de ser apenas um
componente funcional e se tornar uma referencia de excelencia para os demais
componentes do formatter.

O ponto de partida e:

```text
A capa ja funciona.
```

O objetivo agora e:

```text
A capa deve ensinar o restante do sistema a funcionar corretamente.
```

Isso significa que o componente deve ser:

- previsivel;
- auditavel;
- testavel;
- reutilizavel como padrao;
- livre de decisoes visuais escondidas no codigo;
- capaz de falhar antes de gerar um DOCX visualmente quebrado;
- claro o suficiente para orientar folha de rosto, folha de aprovacao e outros componentes de pagina unica.

---

## 2. Regra central de arquitetura

A regra central que deve guiar o cover e os proximos componentes e:

```text
Perfil define modelo visual e estrutural.
Usuario fornece conteudo e preferencias permitidas.
Codigo calcula tudo que deriva desses dados.
```

Essa regra e mais importante que qualquer detalhe de implementacao.

Ela impede que o codigo vire um conjunto de excecoes especificas, ajustes manuais e
valores tecnicos escondidos.

---

## 3. Responsabilidades corretas

### 3.1 O perfil decide

O perfil deve definir tudo que for decisao visual, institucional ou estrutural.

Exemplos:

```text
tamanho da pagina
margens
fonte
tamanho da fonte
espacamento entre linhas
alinhamento
negrito
italico
caixa alta
indentacao
espacamento antes/depois
pesos visuais entre grupos
mapeamento de estilos por bloco
```

Se um perfil ABNT de uma instituicao exige cidade em caixa alta, isso deve estar no
perfil:

```json
{
  "id": "cover.bottom",
  "uppercase": true
}
```

Se outro perfil quiser cidade sem caixa alta, o mesmo componente deve respeitar:

```json
{
  "id": "cover.bottom",
  "uppercase": false
}
```

O cover nao deve saber qual decisao institucional esta correta. Ele deve aplicar o
perfil recebido.

### 3.2 O usuario fornece conteudo

O usuario ou consumidor da API deve fornecer os dados da capa.

Exemplos:

```text
instituicao
curso
disciplina
autores
titulo
subtitulo
cidade
ano
```

O usuario tambem pode fornecer preferencias permitidas pelo perfil, se o sistema
vier a suportar isso no futuro.

O usuario nao deve fornecer valores tecnicos de layout fisico.

### 3.3 O codigo calcula

O codigo deve calcular tudo que deriva do perfil e do conteudo.

Exemplos:

```text
largura util
altura util
largura disponivel de texto
altura exata de linha
capacidade fisica de linhas
reserva tecnica de borda
capacidade segura de linhas
quebra visual de texto
linhas ocupadas por bloco
linhas totais de conteudo
linhas vazias disponiveis
distribuicao final dos espacos
overflow vertical
overflow horizontal
conversoes entre cm, pt e twips
```

Esses valores devem nascer do calculo, nao do request e nao do perfil.

---

## 4. O que nao deve ir para o perfil

Campos tecnicos nao devem aparecer no perfil, ainda que parecam convenientes.

Nao adicionar:

```text
maxCharactersPerLine
pageFitGuardLines
safeLineCapacity
availableLines
bottomPaddingLineSlots
bottomEndOffsetLines
lineHeightTwips
exactLineHeightPt
physicalLineCapacity
boundarySafetyLineCount
```

Esses campos sao consequencias do perfil e do conteudo. Portanto, pertencem ao
codigo.

---

## 5. Decisoes visuais nao pertencem ao componente

Um componente excelente nao deve conter decisoes visuais escondidas.

Errado:

```text
CoverLayoutCalculator decide que cidade deve ser maiuscula.
CoverRenderer decide que titulo deve ser negrito.
DocxWriter aplica regra ABNT especifica para um bloco da capa.
```

Correto:

```text
Perfil informa uppercase=true para cover.bottom.
Perfil informa bold=true para cover.title.
Perfil informa fonte, tamanho, margens e alinhamento.
Cover mede e calcula com base nesses estilos.
Renderer renderiza exatamente o plano calculado.
DocxWriter escreve fielmente o bloco recebido.
```

Regra de excelencia:

```text
Nenhuma decisao visual institucional deve estar escondida no codigo do componente.
```

---

## 6. Contrato ideal do cover

O cover deve ter um contrato simples e forte.

Entrada:

```text
CoverComponent
DocumentProfile
```

Processo:

```text
resolver estilos
medir texto
validar conteudo obrigatorio
calcular area segura
validar se cabe
distribuir espacos
montar plano
renderizar plano
```

Saida:

```text
List<DocxBlock>
```

Ou falha:

```text
erro explicito antes da geracao do DOCX
```

Garantia principal:

```text
Se o cover retorna blocos DOCX, a capa deve caber dentro da capacidade segura de uma pagina.
Se nao couber, o cover deve falhar antes de renderizar um documento quebrado.
```

---

## 7. Estado atual do cover

O cover ja tem fundamentos bons:

- removeu valores tecnicos do profile/request;
- mede texto internamente;
- calcula layout antes da renderizacao;
- usa area segura;
- falha quando o conteudo excede a capacidade segura;
- renderiza linhas vazias como paragrafos reais;
- usa altura de linha exata;
- removeu o bloco multiline que deixava o Word decidir demais;
- permite que pesos visuais venham do perfil;
- preserva a regra de uma pagina.

Esse estado e bom para funcionamento.

Mas excelencia exige mais do que funcionamento.

---

## 8. O que falta para excelencia

### 8.1 Diagnostico de layout

Hoje o sistema sabe que a capa cabe ou nao cabe, mas ainda nao explica a decisao
com riqueza suficiente.

Um componente excelente deve conseguir diagnosticar:

```text
capacidade fisica de linhas
reserva tecnica de borda
capacidade segura
linhas ocupadas por conteudo
linhas ocupadas por bloco
linhas restantes
linhas distribuidas por gap
altura exata de linha
bloco que causou falha
motivo da falha
```

Exemplo de diagnostico desejado:

```text
Cover layout overflow.
safeLineCapacity=31
contentLineCount=34
overflowLines=3
lineHeightPt=18
blocks:
  cover.top=3
  cover.authors=18
  cover.title=8
  cover.bottom=2
```

Esse diagnostico pode ser interno no inicio. Depois pode virar resposta de erro
mais amigavel na API.

### 8.2 Plano auditavel

O `CoverLayoutPlan` deve ser uma prova do layout, nao apenas uma lista de elementos.

Ele deveria expor ou conter um diagnostico com:

```text
physicalLineCapacity
boundarySafetyLineCount
safeLineCapacity
contentLineCount
availableGapLines
gapLineCounts
blockLineCounts
exactLineHeightPt
```

Assim o teste pode provar:

```text
totalLines == contentLineCount + sum(gapLineCounts)
totalLines == safeLineCapacity
contentLineCount <= safeLineCapacity
```

### 8.3 Erros semanticos

Hoje ainda existem falhas importantes representadas como `IllegalArgumentException`.

Para excelencia, os erros deveriam expressar a natureza do problema.

Exemplos:

```text
InvalidCoverContentException
InvalidSinglePageStyleException
TextMeasurementException
SinglePageLayoutOverflowException
```

Isso ajuda:

- controller;
- logs;
- testes;
- mensagens de API;
- manutencao futura.

### 8.4 Base generica unica para pagina unica

O cover nao deve ser uma ilha.

Ele deve virar referencia para componentes de pagina unica, como:

```text
folha de rosto
folha de aprovacao
dedicatoria
agradecimentos curtos
epigrafe
```

Para isso, a base generica precisa ser consolidada.

O sistema deve ter uma unica forma oficial de calcular:

```text
area segura
altura de linha
capacidade de linhas
distribuicao de gaps
overflow
renderizacao de linhas vazias
```

Se existirem duas implementacoes concorrentes, os proximos componentes podem copiar
a errada.

### 8.5 Remover duplicacao conceitual

Qualquer classe generica antiga que calcule area segura por conta propria deve ser
atualizada para usar a mesma regra do cover.

O calculo oficial deve ser:

```text
physicalLineCapacity = linhas que cabem na altura util
boundarySafetyLineCount = reserva tecnica derivada das bordas/margens
safeLineCapacity = max(physicalLineCapacity - boundarySafetyLineCount, 0)
```

Essa regra nao deve ser reimplementada de forma diferente em cada componente.

### 8.6 Testes de contrato

O cover precisa de testes que validem invariantes, nao apenas exemplos.

Invariantes recomendadas:

```text
totalLines deve ser igual a soma das linhas dos elementos
totalLines nao pode exceder safeLineCapacity
todo paragrafo da capa deve ter altura de linha exata
toda linha vazia deve ser um DocxBlankLine real
bottom deve ocupar exatamente duas linhas visuais
cidade que quebra em duas linhas deve invalidar bottom
conteudo vertical excessivo deve falhar antes do DOCX
perfil com spacingBefore/After diferente de zero deve falhar em layout de pagina unica
palavra maior que largura disponivel deve falhar antes do DOCX
```

### 8.7 Testes de XML DOCX

Como a capa depende de detalhes do Word, testes unitarios nao bastam.

Devem existir testes que geram DOCX e inspecionam `word/document.xml`.

Validar:

```text
w:lineRule="exact"
w:t xml:space="preserve" em linhas vazias
ausencia de page break dentro da capa
ausencia de paragrafo default vazio no inicio
quantidade esperada de paragrafos
conteudo esperado em ordem
```

Esses testes nao substituem teste visual, mas protegem contra regressao tecnica.

### 8.8 Matriz de cenarios extremos

O cover deve ter uma matriz de cenarios manuais ou automatizados.

Cenarios que devem gerar:

```text
capa curta
capa sem subtitulo
capa com varios autores dentro do limite
titulo longo dentro do limite
subtitulo longo dentro do limite
cidade longa real que ainda cabe
palavra longa real que ainda cabe
conteudo no limite geravel
```

Cenarios que devem falhar:

```text
muitos autores
titulo/subtitulo que excedem capacidade segura
cidade que quebra para mais de uma linha visual
ano ausente
bottom com quantidade incorreta de linhas
palavra maior que largura disponivel
perfil com area util insuficiente
perfil de pagina unica com spacingBefore/After diferente de zero
```

### 8.9 Documentacao do padrao

O cover deve ter documentacao curta e objetiva dizendo como criar outro componente
de pagina unica.

Essa documentacao deve explicar:

```text
como resolver estilos
como medir texto
como calcular area segura
como calcular linhas ocupadas
como distribuir gaps
quando falhar
como renderizar linhas vazias
o que nao colocar no perfil
o que nao colocar no request
como testar DOCX
```

Sem essa documentacao, a referencia fica implicita no codigo.

Codigo bom ensina quem le.

Codigo excelente tambem deixa um caminho claro.

---

## 9. Arquitetura alvo recomendada

Uma arquitetura de excelencia para componentes de pagina unica poderia seguir este
desenho:

```text
component/
  cover/
    CoverRenderer
    CoverLayoutCalculator
    CoverLayoutPlan
    CoverLayoutDiagnostic

layout/
  text/
    TextMeasurer
    MeasuredText
    FontMetricsTextMeasurer
    ConservativeTextMeasurer

  singlepage/
    SinglePageRenderableAreaCalculator
    SinglePageRenderableArea
    SinglePageLineMetrics
    SinglePageGapDistributor
    SinglePageLayoutPlan
    SinglePageLayoutDiagnostic
    SinglePageDocxBlockMapper
```

O cover deve ser especifico no que e semantica da capa:

```text
topLines
authors
title
subtitle
bottomLines
bottom deve conter cidade e ano
```

A base generica deve cuidar do que e comum a pagina unica:

```text
medir capacidade
contar linhas
distribuir gaps
validar overflow
gerar blocos com linha exata
gerar linhas vazias reais
```

---

## 10. Ordem recomendada para evolucao

### Etapa 1: diagnostico de area segura

Criar:

```java
public record SinglePageRenderableArea(
        int physicalLineCapacity,
        int boundarySafetyLineCount,
        int safeLineCapacity
) {
}
```

Adicionar metodo:

```java
public SinglePageRenderableArea calculate(PageRule pageRule, int lineHeightTwips)
```

Manter o metodo atual como atalho:

```java
public int calculateSafeLineCapacity(PageRule pageRule, int lineHeightTwips)
```

### Etapa 2: diagnostico do cover

Criar:

```java
public record CoverLayoutDiagnostic(
        int physicalLineCapacity,
        int boundarySafetyLineCount,
        int safeLineCapacity,
        int contentLineCount,
        int availableGapLines,
        Map<String, Integer> blockLineCounts,
        Map<String, Integer> gapLineCounts,
        BigDecimal exactLineHeightPt
) {
}
```

Adicionar ao `CoverLayoutPlan`.

### Etapa 3: erros mais expressivos

Substituir erros genericos por erros semanticos.

Exemplos:

```text
bottom invalido
texto impossivel de medir
estilo invalido para pagina unica
overflow de pagina unica
```

### Etapa 4: consolidar base single-page

Atualizar classes genericas antigas para usar:

```text
SinglePageRenderableAreaCalculator
SinglePageGapDistributor
SinglePageLineMetrics
```

Evitar que cada classe recalcule area segura de um jeito.

Status atual:

```text
SinglePageRenderableAreaCalculator criado e usado pelo cover.
SinglePageGapDistributor criado e usado pelo cover.
CoverGapDistributor permanece apenas como adaptador de compatibilidade.
```

### Etapa 5: medidor de texto preferido

O cover deve usar `FontMetricsTextMeasurer` como medidor padrao, atras da interface
`TextMeasurer`.

O `ConservativeTextMeasurer` permanece valido como fallback explicito e como
implementacao substituivel, mas nao deve ser tratado como o destino final da
excelencia.

Regra:

```text
CoverLayoutCalculator depende de TextMeasurer.
O default pode ser FontMetricsTextMeasurer.
Testes e cenarios especiais podem injetar outro TextMeasurer.
```

Mesmo com metricas de fonte, a validacao visual no Microsoft Word continua sendo
necessaria, porque o motor de layout do Word nao e identico ao motor Java.

### Etapa 6: fortalecer testes

Adicionar testes de:

```text
diagnostico
invariantes
XML DOCX
overflow real
bottom quebrado
word width overflow
```

### Etapa 7: documentar como criar o proximo componente

Criar um guia curto:

```text
como implementar componentes de pagina unica usando o cover como referencia
```

---

## 11. Criterios de aceite para excelencia

O cover pode ser considerado uma referencia excelente quando:

- nenhuma decisao visual institucional estiver hardcoded no componente;
- o profile continuar sendo a fonte das decisoes visuais;
- o request nao aceitar valores tecnicos de layout;
- toda capacidade de pagina for calculada a partir de `PageRule` e estilos;
- toda quebra de texto for calculada antes da renderizacao;
- todo overflow for detectado antes do DOCX;
- o plano explicar por que coube ou por que falhou;
- os testes validarem invariantes, nao apenas exemplos felizes;
- o DOCX gerado tiver altura de linha exata;
- as linhas vazias forem paragrafos reais editaveis no Word;
- nao existir duplicacao de regra de area segura em outras classes;
- houver documentacao suficiente para implementar outro componente seguindo o mesmo padrao.

---

## 12. Anti-padroes proibidos

Nao repetir:

```text
linhas vazias fixas espalhadas no renderer
maxCharactersPerLine no perfil
bottomPaddingLineSlots como correcao tecnica
pageFitGuardLines no request
DocxMultilineParagraph para resolver pagina unica
calculo de area segura duplicado em cada componente
decisao visual ABNT hardcoded no codigo
ajuste manual especifico para uma cidade, titulo ou instituicao
gerar DOCX e torcer para o Word nao quebrar pagina
```

---

## 13. Principio final

O cover excelente nao e aquele que apenas gera uma capa bonita.

O cover excelente e aquele que:

```text
recebe um perfil,
recebe conteudo,
calcula o layout,
prova que cabe,
renderiza fielmente,
ou falha com explicacao antes de gerar um documento errado.
```

Esse e o padrao que os proximos componentes devem seguir.
