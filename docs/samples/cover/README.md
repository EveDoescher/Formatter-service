# Samples oficiais da capa

Estes arquivos servem como cenarios fixos de validacao visual e regressao manual
do componente `cover`.

Cada JSON e uma requisicao completa para geracao de DOCX. Eles nao devem conter
valores tecnicos de encaixe fisico, como `maxCharactersPerLine`,
`safeLineCapacity`, `pageFitGuardLines` ou `bottomPaddingLineSlots`.

## Regra central

```text
Perfil define modelo visual e estrutural.
Usuario fornece conteudo.
Codigo calcula medicao, area segura, distribuicao de espacos e overflow.
```

## Como validar

Para cada JSON esperado como sucesso:

```text
1. enviar o JSON para o endpoint de criacao de DOCX;
2. abrir o DOCX no Microsoft Word;
3. confirmar que a capa ocupa uma unica pagina;
4. confirmar que cidade e ano permanecem na primeira pagina;
5. confirmar que os grupos estao visualmente separados;
6. confirmar que nao houve quebra inesperada para uma segunda pagina.
```

Para cada JSON esperado como falha:

```text
1. enviar o JSON para o endpoint de criacao de DOCX;
2. confirmar que o sistema falha antes de gerar um DOCX quebrado;
3. confirmar que a falha corresponde ao motivo esperado.
```

## Cenarios que devem gerar DOCX

### cover-short.json

Caso minimo e comum.

Esperado:

```text
gera DOCX;
capa fica em uma pagina;
cidade e ano ficam na primeira pagina;
espacos sao distribuidos com folga.
```

### cover-no-subtitle.json

Valida capa sem subtitulo.

Esperado:

```text
gera DOCX;
titulo permanece centralizado no conjunto visual;
ausencia de subtitulo nao deixa buraco fixo artificial;
bottom permanece ancorado dentro da area segura.
```

### cover-long-title.json

Valida titulo e subtitulo longos com palavra longa realista.

Esperado:

```text
gera DOCX;
titulo quebra em multiplas linhas;
subtitulo quebra se necessario;
cidade longa real ainda ocupa uma linha visual;
ano permanece na primeira pagina.
```

### cover-many-authors.json

Valida muitos autores dentro de um limite aceitavel.

Esperado:

```text
gera DOCX;
autores ocupam varias linhas;
titulo nao cola nos autores;
bottom permanece na primeira pagina.
```

### cover-long-title-many-authors.json

Valida combinacao de topLines extensas, varios autores, titulo e subtitulo longos.

Esperado:

```text
gera DOCX;
conteudo continua em uma pagina;
espacos ficam menores, mas ainda existem;
cidade e ano permanecem juntos no fim da capa.
```

### cover-limit.json

Valida um caso pesado, proximo do limite geravel.

Esperado:

```text
gera DOCX;
capa fica apertada, mas nao quebra;
bottom permanece na primeira pagina;
nao deve haver overflow vertical.
```

Este cenario e sensivel por natureza. Se ele comecar a falhar apos uma mudanca no
medidor de texto, a falha deve ser analisada antes de assumir regressao visual.

## Cenarios que devem falhar

### cover-overflow.json

Valida overflow vertical real.

Esperado:

```text
falha antes de gerar DOCX;
motivo esperado: SinglePageLayoutOverflowException;
bottomLines continua semanticamente valido;
a falha deve ocorrer por excesso de conteudo vertical.
```

O teste automatizado deve enviar este JSON pela API de exportacao e validar que a
falha acontece antes do DOCX ser retornado.

### cover-bottom-wrap-invalid.json

Valida bottom invalido porque a cidade quebra em mais de uma linha visual.

Esperado:

```text
falha antes de gerar DOCX;
motivo esperado: InvalidCoverContentException;
mensagem esperada: cover bottomLines must contain exactly city and year.
```

O teste automatizado deve enviar este JSON pela API de exportacao. Assim o sample
valida contrato publico, desserializacao, perfil, calculo e renderer no mesmo
fluxo.

## Criterios de aceite visual

Um ajuste no cover deve ser considerado seguro quando:

```text
todos os cenarios de sucesso geram DOCX;
nenhum cenario de sucesso joga cidade/ano para a segunda pagina;
os cenarios de falha falham antes da geracao do DOCX;
as falhas correspondem aos motivos esperados;
os testes automatizados continuam passando.
```

## Observacao sobre decisao visual

Nestes samples, `cover.bottom.uppercase` esta definido como `true` no perfil.
Isso significa que cidade e ano sao renderizados em caixa alta porque o perfil
mandou, nao porque o componente decidiu.

Se outro perfil usar `uppercase: false`, o mesmo componente deve respeitar esse
perfil.
