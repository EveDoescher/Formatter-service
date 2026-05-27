# Diretriz de Layout Automático para Capa em DOCX

## Objetivo

Este documento consolida a decisão técnica para implementar o layout automático da **capa** de um trabalho acadêmico em um formatter/microserviço de geração de documentos DOCX usando `docx4j`.

A capa deve ser um componente de **página única**. Ela deve se adaptar automaticamente ao tamanho do título, quantidade de autores e demais elementos textuais, desde que tudo caiba dentro da área útil da página.

A regra principal é:

> Se o conteúdo couber em uma única página, a capa deve ser gerada em uma única página.  
> Se o conteúdo não couber em uma única página, a geração deve falhar antes de renderizar o DOCX.

O sistema não deve deixar o Word, LibreOffice ou qualquer editor decidir quebrar a capa para a página seguinte.

---

## Contexto arquitetural do projeto

O projeto é um formatter/microserviço de geração de documentos. Inicialmente, o foco está em trabalhos acadêmicos em DOCX usando `docx4j`, mas a arquitetura deve permanecer desacoplada e extensível.

Diretrizes arquiteturais importantes:

- o perfil define regras, limites, estilos e estratégias de layout;
- o conteúdo vem da requisição ou do documento estruturado;
- o cálculo de layout acontece antes da renderização;
- o renderer apenas executa um plano de layout já validado;
- o `DocxWriter` expõe operações abstratas de escrita;
- o `Docx4jWriter` apenas traduz essas operações para OpenXML/docx4j;
- regras acadêmicas, margens, fontes, espaçamentos e comportamento visual não devem ficar hardcoded no renderer ou no writer;
- a capa, folha de rosto, folha de aprovação e outros elementos de página única devem usar uma estratégia genérica de layout vertical.

---

## Problema

A capa possui elementos textuais com tamanho variável, por exemplo:

- instituição;
- curso;
- autores;
- título;
- subtítulo;
- cidade;
- ano.

Esses elementos podem variar bastante:

- o título pode ter uma linha ou várias linhas;
- pode haver um autor ou muitos autores;
- a instituição pode ocupar mais de uma linha;
- a fonte pode mudar conforme o perfil;
- as margens podem mudar conforme o perfil;
- o tamanho da página pode mudar conforme o perfil.

Portanto, a capa não deve depender de espaçamentos fixos, como:

```text
Instituição
[pula 8 linhas]
Autores
[pula 12 linhas]
Título
[pula 20 linhas]
Limeira
2026
```

Isso é frágil, difícil de manter e quebra quando o conteúdo muda.

---

## Mudança de visão

A capa não deve ser tratada como uma sequência de parágrafos com linhas vazias fixas.

Ela deve ser tratada como um pequeno sistema de layout vertical, parecido com um `flexbox` vertical ou uma grade de linhas.

A pergunta correta não é:

> Quantas linhas fixas eu pulo entre os elementos?

A pergunta correta é:

> Quantas linhas o conteúdo ocupa, quantas linhas mínimas de espaçamento são obrigatórias e quantas linhas sobram para distribuir entre os espaços flexíveis?

---

## Conceito principal

A capa deve ser calculada a partir desta fórmula:

```text
linhas disponíveis na página
- linhas ocupadas pelo conteúdo textual
- linhas mínimas obrigatórias de espaçamento
= linhas restantes distribuíveis
```

Se o resultado for positivo ou zero, a capa cabe.

Se o resultado for negativo, a capa não cabe e a geração deve falhar.

---

## Grid vertical da capa

A capa deve trabalhar com uma grade vertical de linhas.

Exemplo conceitual:

```text
Página útil da capa: 38 linhas

Instituição: 1 linha
Autores: 3 linhas
Título: 5 linhas
Cidade: 1 linha
Ano: 1 linha

Conteúdo total: 11 linhas
```

Depois entram os gaps mínimos:

```text
Gap após autores: mínimo 2 linhas
Gap antes do título: mínimo 2 linhas
Gap antes do rodapé: mínimo 4 linhas

Gaps mínimos totais: 8 linhas
```

Então:

```text
Linhas mínimas necessárias = conteúdo + gaps mínimos
Linhas mínimas necessárias = 11 + 8 = 19 linhas
```

Se a página tem 38 linhas:

```text
38 - 19 = 19 linhas restantes
```

Essas 19 linhas restantes são distribuídas entre os gaps flexíveis conforme regras de peso.

---

## Regra absoluta de validação

A verificação principal da capa deve ser:

```text
contentLines + minimumGapLines <= availableLines
```

Se for verdadeiro:

```text
a capa cabe
o sistema distribui as linhas restantes nos gaps
um CoverLayoutPlan válido é criado
o renderer pode renderizar
```

Se for falso:

```text
a capa não cabe
o sistema lança erro
o DOCX não deve ser gerado com a capa quebrada
```

Exemplo de falha:

```text
availableLines = 38
contentLines = 34
minimumGapLines = 8

requiredLines = 42

42 > 38

Resultado: falha
```

Mensagem possível:

```text
A capa não cabe em uma única página.
Capacidade: 38 linhas.
Necessário: 42 linhas.
Reduza o título, reduza a quantidade de autores ou ajuste as regras do perfil.
```

---

## Onde a garantia deve acontecer

A garantia de que a capa cabe em uma página deve acontecer **antes do docx4j**.

O ponto correto é o cálculo de layout:

```text
CoverLayoutCalculator
```

Fluxo esperado:

```text
Controller
  ↓
FormatterService
  ↓
Profile + Content
  ↓
CoverLayoutCalculator
  ↓
CoverLayoutPlan validado
  ↓
CoverRenderer
  ↓
DocxWriter
  ↓
Docx4jWriter
```

O `docx4j` não deve decidir se a capa cabe.

O renderer também não deve decidir se a capa cabe.

O contrato deve ser:

```text
CoverLayoutCalculator calcula o layout.
Se couber, retorna CoverLayoutPlan.
Se não couber, lança erro.
CoverRenderer renderiza apenas planos válidos.
Docx4jWriter apenas traduz operações para DOCX.
```

---

## Contrato do CoverLayoutCalculator

O método principal de cálculo deve ter um contrato forte:

```text
Ou retorna uma capa válida que cabe em uma única página,
ou falha.
```

Ele nunca deve retornar uma capa parcialmente válida.

Exemplo conceitual:

```java
public CoverLayoutPlan calculate(
    CoverContent content,
    CoverLayoutRule layoutRule,
    PageRule pageRule,
    StyleCatalog styles
) {
    PageGrid grid = pageGridCalculator.calculate(pageRule, styles);

    List<MeasuredBlock> blocks = blockMeasurer.measure(
        content,
        layoutRule,
        styles,
        grid.availableWidth()
    );

    int contentLines = blocks.stream()
        .mapToInt(MeasuredBlock::occupiedLines)
        .sum();

    int minimumGapLines = layoutRule.gaps().stream()
        .mapToInt(FlexibleGapRule::minLines)
        .sum();

    int requiredLines = contentLines + minimumGapLines;

    if (requiredLines > grid.availableLines()) {
        throw new CoverOverflowException(
            "Cover content requires " + requiredLines +
            " lines, but page only has " + grid.availableLines() + " lines."
        );
    }

    int remainingLines = grid.availableLines() - requiredLines;

    List<ResolvedGap> resolvedGaps = gapDistributor.distribute(
        layoutRule.gaps(),
        remainingLines
    );

    return coverPlanAssembler.assemble(blocks, resolvedGaps, grid.availableLines());
}
```

---

## O CoverLayoutPlan também deve se proteger

Além do cálculo validar a capa, o próprio plano pode ter uma trava defensiva.

Exemplo:

```java
public record CoverLayoutPlan(
    List<CoverLayoutElement> elements,
    int totalLines,
    int pageCapacityLines
) {
    public CoverLayoutPlan {
        if (totalLines > pageCapacityLines) {
            throw new IllegalArgumentException(
                "Invalid cover layout plan: total lines exceed page capacity."
            );
        }
    }
}
```

Assim existem duas proteções:

```text
1. o calculator não cria plano inválido;
2. o plano não permite existir em estado inválido.
```

---

## Gaps flexíveis

Os espaços entre blocos não devem ser fixos.

Eles devem ser gaps flexíveis com:

- identificador;
- mínimo de linhas;
- máximo de linhas;
- peso de distribuição;
- estilo de linha vazia.

Exemplo:

```java
public record FlexibleGapRule(
    String id,
    int minLines,
    int maxLines,
    int weight,
    String spacerStyleId
) {}
```

Exemplo de configuração conceitual:

```java
List.of(
    new FlexibleGapRule("gap-after-authors", 2, 8, 1, "cover-spacer-line"),
    new FlexibleGapRule("gap-before-title", 2, 12, 2, "cover-spacer-line"),
    new FlexibleGapRule("gap-before-bottom", 4, 20, 3, "cover-spacer-line")
)
```

Interpretação:

```text
Depois dos autores:
mínimo 2 linhas, máximo 8, peso 1

Antes do título:
mínimo 2 linhas, máximo 12, peso 2

Antes do rodapé:
mínimo 4 linhas, máximo 20, peso 3
```

O perfil não diz:

```text
pule exatamente 10 linhas
```

O perfil diz:

```text
este espaço é flexível, tem limites e tem prioridade visual
```

A quantidade final de linhas é calculada.

---

## Distribuição das linhas restantes

Depois que o sistema valida que a capa cabe, as linhas restantes são distribuídas pelos gaps.

Exemplo:

```text
remainingLines = 18

gap-after-authors: peso 1
gap-before-title: peso 2
gap-before-bottom: peso 3

peso total = 6
```

Distribuição conceitual:

```text
gap-after-authors recebe 3 linhas extras
gap-before-title recebe 6 linhas extras
gap-before-bottom recebe 9 linhas extras
```

Somando com os mínimos:

```text
gap-after-authors = min 2 + extra 3 = 5 linhas
gap-before-title = min 2 + extra 6 = 8 linhas
gap-before-bottom = min 4 + extra 9 = 13 linhas
```

Resultado final:

```text
Instituição
Autores

[5 linhas vazias]

Título

[13 linhas vazias]

Limeira
2026
```

---

## Distribuição com limite máximo

A distribuição deve respeitar `maxLines`.

Se um gap atingir o máximo, a sobra deve ser redistribuída para os outros gaps que ainda podem crescer.

Exemplo conceitual:

```java
public List<ResolvedGap> distribute(
    List<FlexibleGapRule> gaps,
    int remainingLines
) {
    Map<String, Integer> resolved = new LinkedHashMap<>();

    for (FlexibleGapRule gap : gaps) {
        resolved.put(gap.id(), gap.minLines());
    }

    int undistributed = remainingLines;

    while (undistributed > 0) {
        List<FlexibleGapRule> expandable = gaps.stream()
            .filter(gap -> resolved.get(gap.id()) < gap.maxLines())
            .toList();

        if (expandable.isEmpty()) {
            throw new LayoutDistributionException(
                "No expandable gap available for remaining lines: " + undistributed
            );
        }

        int totalWeight = expandable.stream()
            .mapToInt(FlexibleGapRule::weight)
            .sum();

        boolean distributedSomething = false;

        for (FlexibleGapRule gap : expandable) {
            if (undistributed <= 0) {
                break;
            }

            int current = resolved.get(gap.id());

            if (current >= gap.maxLines()) {
                continue;
            }

            int share = Math.max(1, Math.round(
                undistributed * (gap.weight() / (float) totalWeight)
            ));

            int availableGrowth = gap.maxLines() - current;
            int toAdd = Math.min(share, availableGrowth);

            resolved.put(gap.id(), current + toAdd);
            undistributed -= toAdd;
            distributedSomething = true;
        }

        if (!distributedSomething) {
            throw new LayoutDistributionException(
                "Could not distribute remaining gap lines."
            );
        }
    }

    return gaps.stream()
        .map(gap -> new ResolvedGap(gap.id(), resolved.get(gap.id()), gap.spacerStyleId()))
        .toList();
}
```

---

## Gaps devem ser renderizados como linhas individuais

Uma decisão importante: os gaps **não devem** ser renderizados como um único bloco com altura X.

Não fazer:

```text
gap com altura de 8 cm
```

Motivo: se alguém abrir o DOCX no Word e apagar esse bloco, o espaçamento inteiro some de uma vez.

A solução correta é:

```text
gap resolvido em várias linhas vazias individuais
```

Exemplo:

```text
gap-before-title = 12 linhas
```

Renderização correta:

```text
linha vazia 1
linha vazia 2
linha vazia 3
...
linha vazia 12
```

Cada linha vazia deve ser um parágrafo separado no DOCX.

Assim, se o usuário editar manualmente e apagar uma linha, ele apaga apenas uma unidade do espaçamento, não o gap inteiro.

---

## Não usar um único parágrafo com vários breaks

Evitar:

```java
paragraph.addBreak();
paragraph.addBreak();
paragraph.addBreak();
```

Também evitar um único parágrafo vazio com altura grande.

Preferir:

```text
<w:p>linha vazia 1</w:p>
<w:p>linha vazia 2</w:p>
<w:p>linha vazia 3</w:p>
```

Cada spacer line deve ser um parágrafo próprio.

Na abstração do engine, isso poderia ser:

```java
public interface DocxWriter {
    void addParagraph(ParagraphBlock paragraph);
    void addSpacerLine(SpacerLine spacerLine);
    void addSpacerLines(int count, String styleId);
}
```

A implementação com docx4j fica escondida dentro do `Docx4jWriter`.

---

## Estilo das linhas vazias

As linhas vazias da capa devem usar um estilo específico, por exemplo:

```text
cover-spacer-line
```

Esse estilo deve ter regras previsíveis, como:

- fonte definida pelo perfil;
- tamanho definido pelo perfil;
- espaçamento antes zero;
- espaçamento depois zero;
- altura de linha controlada;
- sem conteúdo visível.

O ideal é que a altura da linha seja compatível com a grade usada no cálculo.

Não pode acontecer isto:

```text
o cálculo acha que uma linha tem 12 pt,
mas o DOCX renderiza a linha com 18 pt.
```

Isso quebraria a garantia de página única.

---

## Unidade comum entre cálculo e renderização

Para a garantia funcionar, cálculo e renderização precisam usar a mesma unidade.

A unidade recomendada é:

```text
linha da grade da capa
```

A linha da grade deve ser derivada das regras de estilo e página do perfil.

Exemplo conceitual:

```text
altura útil da página / altura da linha base = linhas disponíveis
```

Exemplo:

```text
altura útil = 24,7 cm
altura da linha = 0,635 cm
linhas disponíveis = 38 linhas
```

A partir disso, tudo é contado em linhas:

```text
instituição ocupa X linhas
autores ocupam Y linhas
título ocupa Z linhas
gaps ocupam N linhas
```

---

## Medição dos blocos textuais

O título não pode ser considerado automaticamente uma linha.

Ele precisa ser medido conforme:

- texto;
- fonte;
- tamanho da fonte;
- largura disponível;
- estilo;
- espaçamento;
- regras de quebra.

Exemplo:

```text
Título curto:
"Banco de Dados NoSQL"

ocupa 1 linha
```

Outro exemplo:

```text
Título longo:
"Banco de Dados NoSQL: Tipos, Casos de Uso e Aplicações em Sistemas Modernos"

pode ocupar 2, 3 ou mais linhas
```

Para isso, deve existir uma abstração de medição:

```java
public interface TextMeasurer {
    MeasuredText measure(
        String text,
        StyleRule style,
        Width availableWidth
    );
}
```

Retorno possível:

```java
public record MeasuredText(
    int visualLineCount,
    int occupiedGridLines
) {}
```

Para blocos:

```java
public record MeasuredBlock(
    String blockId,
    int occupiedLines,
    List<String> paragraphs,
    String styleId
) {}
```

---

## Observação sobre precisão

`docx4j` gera o DOCX, mas quem renderiza visualmente o arquivo é o Word, LibreOffice, OnlyOffice etc.

Portanto, a garantia perfeita em qualquer editor é difícil.

Porém, a garantia fica muito boa se o sistema controlar:

- tamanho da página;
- margens;
- largura útil;
- fonte;
- tamanho da fonte;
- altura de linha;
- espaçamento antes/depois;
- medição aproximada ou real do texto;
- quantidade de linhas vazias renderizadas.

A primeira versão pode usar uma medição aproximada conservadora.

Depois, o `TextMeasurer` pode evoluir para uma implementação mais precisa.

O importante é que essa medição fique atrás de uma interface, para poder ser substituída sem alterar o restante do sistema.

---

## Renderização da capa

O renderer deve ser simples.

Ele não deve calcular layout.

Ele apenas recebe um `CoverLayoutPlan` válido e escreve os elementos.

Exemplo:

```java
public void render(CoverLayoutPlan plan, DocxWriter writer) {
    for (CoverLayoutElement element : plan.elements()) {
        switch (element) {
            case TextElement text -> writer.addParagraph(text.paragraph());
            case SpacerLines gap -> writer.addSpacerLines(gap.lineCount(), gap.styleId());
        }
    }

    writer.addPageBreak();
}
```

O renderer pode assumir que o plano cabe porque recebeu um plano validado.

---

## Quebra de página

Para garantir que a capa seja uma página isolada, o renderer pode inserir uma quebra de página após renderizar a capa.

Regra:

```text
capa coube → renderiza capa → adiciona quebra de página → próximo componente
capa não coube → lança erro → não gera documento
```

A quebra de página não deve ser usada para resolver overflow.

Ela serve apenas para separar a capa do próximo componente.

---

## Modelo de elementos do plano

Um possível modelo:

```java
public sealed interface CoverLayoutElement permits TextElement, SpacerLines {}

public record TextElement(
    String blockId,
    String styleId,
    List<String> paragraphs
) implements CoverLayoutElement {}

public record SpacerLines(
    String gapId,
    int lineCount,
    String styleId
) implements CoverLayoutElement {}
```

O `CoverLayoutPlan` pode conter a sequência final já resolvida:

```java
public record CoverLayoutPlan(
    List<CoverLayoutElement> elements,
    int totalLines,
    int pageCapacityLines
) {}
```

Exemplo de plano:

```text
TextElement institution
TextElement authors
SpacerLines gap-after-authors com 5 linhas
TextElement title
SpacerLines gap-before-bottom com 13 linhas
TextElement city
TextElement year
```

---

## Modelo de regras sugerido

Exemplo de regra principal:

```java
public record CoverLayoutRule(
    List<CoverBlockRule> blocks,
    List<FlexibleGapRule> gaps,
    CoverOverflowPolicy overflowPolicy
) {}
```

Bloco:

```java
public record CoverBlockRule(
    String id,
    String styleId,
    boolean required,
    boolean keepTogether
) {}
```

Gap:

```java
public record FlexibleGapRule(
    String id,
    String afterBlockId,
    String beforeBlockId,
    int minLines,
    int maxLines,
    int weight,
    String spacerStyleId
) {}
```

Política de overflow:

```java
public enum CoverOverflowPolicy {
    FAIL
}
```

Neste momento, a política recomendada para a capa é `FAIL`.

Ou seja:

```text
se não couber, falha.
não reduz fonte automaticamente.
não quebra para segunda página.
não remove informação obrigatória.
```

Políticas mais flexíveis podem existir no futuro, mas devem ser explícitas no perfil.

---

## Exemplo completo de cálculo

Entrada:

```text
availableLines = 38
```

Conteúdo medido:

```text
institution = 1
authors = 4
title = 6
city = 1
year = 1
```

Total:

```text
contentLines = 13
```

Gaps mínimos:

```text
gap-after-authors = 2
gap-before-title = 2
gap-before-bottom = 4
```

Total:

```text
minimumGapLines = 8
```

Validação:

```text
requiredLines = 13 + 8 = 21
```

Como:

```text
21 <= 38
```

A capa cabe.

Sobram:

```text
remainingLines = 38 - 21 = 17
```

Distribuição final possível:

```text
gap-after-authors = 5 linhas
gap-before-title = 6 linhas
gap-before-bottom = 14 linhas
```

Plano final:

```text
Institution: 1 linha
Authors: 4 linhas
Gap after authors: 5 linhas
Title: 6 linhas
Gap before title/bottom: 14 linhas
City: 1 linha
Year: 1 linha
```

Total:

```text
1 + 4 + 5 + 6 + 14 + 1 + 1 = 32 linhas
```

Ainda há margem dependendo de como a distribuição for feita, ou a distribuição pode usar todas as 38 linhas se o objetivo visual for ocupar a página útil inteira.

---

## Exemplo de falha

Entrada:

```text
availableLines = 38
```

Conteúdo medido:

```text
institution = 2
authors = 12
title = 20
city = 1
year = 1
```

Total:

```text
contentLines = 36
```

Gaps mínimos:

```text
gap-after-authors = 2
gap-before-title = 2
gap-before-bottom = 4
```

Total:

```text
minimumGapLines = 8
```

Validação:

```text
requiredLines = 36 + 8 = 44
```

Como:

```text
44 > 38
```

Resultado:

```text
falha antes da renderização
```

Mensagem possível:

```text
A capa não cabe em uma única página.
Capacidade: 38 linhas.
Necessário mínimo: 44 linhas.
O conteúdo textual ocupa 36 linhas e os espaçamentos mínimos ocupam 8 linhas.
```

---

## Responsabilidades por camada

### Profile

Define:

- página;
- margens;
- estilos;
- altura de linha;
- blocos da capa;
- gaps flexíveis;
- limites mínimos e máximos;
- pesos de distribuição;
- política de overflow.

Não deve conter:

```text
pule exatamente 10 linhas sempre
```

Deve conter:

```text
este gap tem mínimo, máximo e peso
```

---

### Document/Request

Fornece o conteúdo:

- instituição;
- curso;
- autores;
- título;
- subtítulo;
- cidade;
- ano.

Não decide layout.

---

### CoverLayoutCalculator

Responsável por:

- calcular linhas disponíveis;
- medir blocos textuais;
- somar conteúdo;
- somar gaps mínimos;
- validar se cabe;
- distribuir linhas restantes;
- montar `CoverLayoutPlan`.

É aqui que fica a garantia principal.

---

### CoverRenderer

Responsável por:

- receber um `CoverLayoutPlan` válido;
- chamar operações do `DocxWriter`;
- renderizar texto e linhas vazias;
- inserir quebra de página após a capa, se aplicável.

Não calcula se cabe.

---

### DocxWriter

Interface abstrata de escrita.

Pode ter operações como:

```java
void addParagraph(ParagraphBlock paragraph);
void addSpacerLine(SpacerLine line);
void addSpacerLines(int count, String styleId);
void addPageBreak();
```

Não conhece regra acadêmica.

---

### Docx4jWriter

Implementação concreta usando `docx4j`.

Responsável apenas por converter operações abstratas em DOCX/OpenXML.

Não deve conter regras como:

- ABNT;
- UNIP;
- capa;
- cidade/ano;
- número fixo de linhas;
- fonte acadêmica padrão;
- margem padrão.

---

## Regra de ouro

A capa não deve ser construída como um monte de `addEmptyParagraph()` espalhado no renderer.

Ela deve ser construída assim:

```text
1. mede o conteúdo;
2. valida se cabe;
3. calcula os gaps;
4. cria um plano;
5. renderiza o plano.
```

O renderer pode até chamar `addSpacerLines`, mas a quantidade de linhas já deve vir calculada do plano.

---

## Decisão final

A solução aprovada conceitualmente é:

```text
FlexibleGap não é uma altura única.
FlexibleGap é uma quantidade calculada de SpacerLine.
```

O cálculo pode pensar em linhas, altura ou twips internamente, mas o resultado renderizado deve ser composto por várias linhas vazias individuais.

Isso garante:

- layout automático;
- edição manual menos destrutiva;
- ausência de espaçamentos fixos hardcoded;
- falha clara quando o conteúdo não couber;
- separação correta entre cálculo, renderização e implementação docx4j;
- reaproveitamento futuro para outros componentes de página única.

---

## Resumo operacional

A regra operacional da capa é:

```text
availableLines = calcularLinhasDisponiveis(pageRule, styleRule)
contentLines = medirLinhasDoConteudo(coverContent, styles)
minimumGapLines = somarMinimosDosGaps(coverLayoutRule)
requiredLines = contentLines + minimumGapLines

if requiredLines > availableLines:
    falhar antes de gerar DOCX
else:
    remainingLines = availableLines - requiredLines
    resolvedGaps = distribuir remainingLines entre gaps flexíveis
    plan = montar CoverLayoutPlan com textos + spacer lines
    renderer.render(plan)
```

O trecho central é:

```java
if (contentLines + minimumGapLines > availableLines) {
    throw new CoverOverflowException();
}
```

Essa é a parte que garante:

```text
se couber, fica em uma página;
se não couber, falha.
```

---

## Próxima implementação sugerida

A primeira versão pode ser implementada com os seguintes componentes:

```text
PageGridCalculator
TextMeasurer
CoverBlockMeasurer
GapDistributor
CoverLayoutCalculator
CoverPlanAssembler
CoverRenderer
DocxWriter.addSpacerLines(...)
Docx4jWriter.addSpacerLines(...)
```

Ordem recomendada:

1. criar `PageGridCalculator`;
2. criar modelo de `FlexibleGapRule`;
3. criar `MeasuredBlock`;
4. criar `TextMeasurer` simples e conservador;
5. criar `GapDistributor`;
6. criar `CoverLayoutCalculator` com falha se não couber;
7. criar `CoverLayoutPlan` validado;
8. adaptar o `CoverRenderer` para renderizar apenas o plano;
9. implementar `addSpacerLines` no writer abstrato;
10. implementar `addSpacerLines` no `Docx4jWriter` como múltiplos parágrafos vazios individuais.

---

## Observação importante para revisão do projeto

Essa abordagem não depende de hardcode de layout acadêmico dentro do renderer.

O perfil continua sendo responsável por fornecer as regras.

O cálculo é genérico o suficiente para ser reaproveitado em outros componentes de página única, como:

- capa;
- folha de rosto;
- folha de aprovação;
- dedicatória;
- epígrafe;
- outros elementos pré-textuais com layout vertical controlado.

O objetivo não é apenas resolver a capa, mas criar uma base correta para elementos de página única no formatter.
