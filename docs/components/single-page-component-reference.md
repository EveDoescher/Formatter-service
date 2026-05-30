# Padrao para componentes de pagina unica

Este guia define como implementar novos componentes que precisam caber em uma unica
pagina, usando a capa como referencia.

Exemplos de componentes:

```text
capa
folha de rosto
folha de aprovacao
dedicatoria curta
agradecimentos curtos
epigrafe
```

## Regra central

```text
Perfil define modelo visual e estrutural.
Usuario fornece conteudo e preferencias permitidas.
Codigo calcula tudo que deriva dessas informacoes.
```

Essa regra vale para todos os componentes de pagina unica.

## Responsabilidades

### Perfil

O perfil define decisoes visuais e estruturais:

```text
tamanho da pagina
orientacao
margens
fonte
tamanho da fonte
espacamento entre linhas
alinhamento
negrito
italico
uppercase
indentacao
spacingBeforePt
spacingAfterPt
mapeamento de estilos
pesos visuais entre grupos
```

O perfil nao deve receber valores tecnicos calculados.

### Usuario

O usuario fornece conteudo:

```text
instituicao
autores
titulo
subtitulo
cidade
ano
texto livre do componente
```

O usuario nao deve fornecer capacidade de linhas, altura de linha, reserva tecnica
ou valores de encaixe fisico.

### Codigo

O codigo calcula:

```text
largura util
altura util
largura disponivel de texto
linhas visuais
altura exata de linha
capacidade fisica de linhas
reserva tecnica de borda
capacidade segura de linhas
linhas ocupadas por bloco
linhas disponiveis para gaps
distribuicao dos gaps
overflow
blocos DOCX finais
```

## Fluxo recomendado

Um componente de pagina unica deve seguir este fluxo:

```text
Component + DocumentProfile
  -> resolver regra do componente
  -> resolver estilos
  -> medir texto
  -> validar semantica do componente
  -> calcular altura de linha
  -> calcular area segura
  -> validar overflow
  -> distribuir espacos
  -> montar plano auditavel
  -> renderizar blocos DOCX
```

O renderer nao deve calcular layout.

O writer nao deve conhecer regra institucional.

O perfil nao deve receber calculo tecnico.

## Base single-page

Preferir as classes compartilhadas em `rendering.layout.singlepage`.

### Area renderizavel

Usar:

```text
SinglePageRenderableAreaCalculator
SinglePageRenderableArea
```

Conceito:

```text
physicalLineCapacity = linhas que cabem na altura util
boundarySafetyLineCount = reserva tecnica derivada de PageRule + lineHeightTwips
safeLineCapacity = max(physicalLineCapacity - boundarySafetyLineCount, 0)
```

Nao recalcular area segura dentro do componente.

### Altura de linha

Usar a maior altura exata de linha dos estilos envolvidos:

```text
fontSizePt * lineSpacing
```

O DOCX deve receber `lineRule=exact`.

### Linhas vazias

Usar `DocxBlankLine` para espacos verticais editaveis.

Nao usar blocos gigantes de espaco vertical para componentes que devem continuar
editaveis no Word.

### Distribuicao de espacos

Usar:

```text
SinglePageGapDistributor
```

Ele distribui as linhas vazias disponiveis entre os grupos, preserva o total de
linhas da area segura e reserva uma linha minima por gap quando houver espaco para
isso.

Componentes especificos podem ter adaptadores proprios por compatibilidade, mas o
algoritmo de distribuicao nao deve ser duplicado dentro do componente.

### Texto medido

O texto deve ser medido antes da renderizacao.

Usar a interface:

```text
TextMeasurer
```

Implementacao preferida:

```text
FontMetricsTextMeasurer
```

Fallback disponivel:

```text
ConservativeTextMeasurer
```

Fonte ausente nao deve ser mascarada por padrao. Se o perfil pedir uma familia de
fonte indisponivel no ambiente, o medidor preferido deve falhar claramente.
Fallback de fonte so deve ser usado quando injetado de forma explicita para teste,
migracao ou ambiente controlado.

O componente deve depender da interface, nao de uma implementacao concreta. Isso
permite trocar o medidor sem mudar a semantica do componente.

Regra obrigatoria:

```text
Tudo que foi medido deve ser renderizado como foi medido.
```

Se o medidor retornou tres linhas visuais, renderizar tres `DocxParagraph`.

Nao renderizar texto medido como um paragrafo livre esperando que o Word quebre do
mesmo jeito.

Mesmo com `FontMetricsTextMeasurer`, validar visualmente no Microsoft Word quando a
mudanca afetar quebra de linha. A metrica Java e uma aproximacao melhor que fator
medio por caractere, mas ainda nao e o proprio motor de layout do Word.

## Contrato do plano

Todo componente de pagina unica deve produzir um plano antes de renderizar.

Um plano excelente deve permitir provar:

```text
totalLines == soma das linhas dos elementos
totalLines <= safeLineCapacity
contentLineCount + availableGapLines == safeLineCapacity
sum(blockLineCounts) == contentLineCount
sum(gapLineCounts) == availableGapLines
```

O plano deve ser imutavel e defensivo.

## Diagnostico

Componentes novos devem carregar diagnostico suficiente para explicar sucesso ou
falha.

Diagnostico recomendado:

```text
renderableArea
contentLineCount
availableGapLines
blockLineCounts
gapLineCounts
exactLineHeightPt
```

Falhas podem carregar diagnostico parcial quando o erro ocorrer antes do plano
estar completo.

## Erros recomendados

Usar erros semanticos quando o dominio estiver claro.

Exemplos existentes:

```text
InvalidCoverContentException
InvalidSinglePageStyleException
TextMeasurementException
SinglePageLayoutOverflowException
```

Nao criar excecoes demais antes de conhecer os casos reais.

## Testes obrigatorios

Um novo componente de pagina unica deve ter testes para:

```text
plano valido
overflow vertical
texto que quebra linha
palavra que nao cabe
spacingBeforePt invalido
spacingAfterPt invalido
linhas vazias editaveis
lineRule exact
ausencia de page break interno
```

Quando gerar DOCX, inspecionar `word/document.xml` para validar invariantes
tecnicas.

Nao comparar o XML inteiro.

Comparar apenas o que importa:

```text
w:lineRule="exact"
w:t xml:space="preserve" em linhas vazias
quantidade de paragrafos
ausencia de w:type="page"
conteudo esperado
```

## Samples visuais

Todo componente delicado deve ter samples oficiais em `docs/samples`.

A capa ja possui:

```text
docs/samples/cover
```

Os samples devem documentar:

```text
cenario
resultado esperado
motivo esperado de falha, quando falhar
criterios de aceite visual
```

Nao versionar DOCX gerado por padrao. Versionar JSON e expectativa.

Os testes automatizados devem usar os JSONs oficiais como entrada real da API ou
do fluxo publico equivalente. Montar objetos manualmente no teste nao valida o
contrato dos samples.

## Componentes com rodape semantico

Quando um componente de pagina unica tiver um rodape semantico, como a capa atual:

```text
city
year
```

essa estrutura pertence ao contrato do componente, nao ao perfil. O perfil decide
como renderizar cidade e ano. O componente valida que a estrutura recebida e
semanticamente compativel com o layout que promete entregar.

Nao transformar quantidade de linhas obrigatorias em parametro tecnico do perfil
sem um caso real que mude o dominio do componente.

## Anti-padroes proibidos

Nao usar:

```text
maxCharactersPerLine no perfil ou request
safeLineCapacity no perfil ou request
pageFitGuardLines no perfil ou request
bottomPaddingLineSlots como correcao tecnica
lineHeightTwips no perfil ou request
exactLineHeightPt no perfil ou request
linhas vazias fixas espalhadas no renderer
calculo de area segura duplicado no componente
texto medido renderizado como paragrafo livre
DocxMultilineParagraph para resolver pagina unica
gerar DOCX e torcer para o Word nao quebrar
decisao visual institucional hardcoded no componente
```

## Checklist para novo componente

Antes de considerar um novo componente de pagina unica pronto, verificar:

```text
1. O perfil contem apenas decisoes visuais/estruturais.
2. O request nao contem valores tecnicos de layout.
3. O texto e medido antes da renderizacao.
4. A area segura vem de SinglePageRenderableAreaCalculator.
5. O plano valida suas invariantes.
6. O renderer apenas transforma plano em DocxBlock.
7. O writer nao conhece a semantica do componente.
8. O overflow falha antes do DOCX.
9. Os testes XML validam lineRule exact e linhas vazias reais.
10. Existem samples oficiais para validacao visual.
```

## Referencias atuais

Usar como referencia:

```text
CoverLayoutCalculator
CoverLayoutPlan
CoverLayoutDiagnostic
CoverRenderer
SinglePageRenderableAreaCalculator
SinglePageGapDistributor
SinglePageLayoutEngine
SinglePageLayoutRenderer
OrderedLayoutGapResolver
docs/samples/cover
```

O antigo motor baseado em medidas fisicas em centimetros foi removido da base de
referencia. Novos componentes de pagina unica devem usar apenas o fluxo por linhas
renderizaveis.

`SinglePageLayoutDocxMapper` foi removido do caminho de referencia. Novos
componentes de pagina unica devem montar entrada generica, calcular com
`SinglePageLayoutEngine` e renderizar o plano com `SinglePageLayoutRenderer`,
preservando validacoes semanticas na camada do proprio componente.
