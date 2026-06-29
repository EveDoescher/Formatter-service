# Motor Genérico — Revisão da Taxonomia
**Data:** 28 de Junho de 2026
**Contexto:** Análise crítica do documento `motor-generico-taxonomia.md` conduzida em sessão de revisão arquitetural. Este documento lista os problemas identificados, o resultado da discussão sobre cada um, e as correções a aplicar no modelo alvo.

---

## Problema 1 — `contentBindings` e validação de schema

**Problema levantado:** A substituição de classes tipadas por bindings genéricos baseados em id string eliminaria a validação em tempo de compilação, transferindo erros para runtime.

**Resultado da discussão:** Não é um problema do modelo. No modelo alvo o id declarado no profile é a própria chave do content — o profile declara que existe um elemento `"titulo"`, o content tem `"titulo"`, o motor liga os dois diretamente. Dados compartilhados entre componentes funcionam naturalmente porque o mesmo id pode ser declarado em múltiplos componentes e o backend armazena e serve pelo id. O mecanismo de `work.title` do sistema atual é uma implementação específica de um problema que o modelo novo resolve por design. A validação de profiles malformados é responsabilidade do microserviço de gestão antes de o profile chegar ao Formatter.

**Correção no documento:** Nenhuma.

---

## Problema 2 — Colisão de nomes entre engine `LIST` e elemento folha `list`

**Problema levantado:** O documento define `LIST` como engine de Nível 3 para componentes inteiros como `references` e `glossary`, e também define `list` como elemento folha de Nível 5 usado dentro de fluxos. O mesmo nome para conceitos distintos cria ambiguidade na hora de modelar novos componentes.

**Resultado da discussão:** São coisas conceitualmente diferentes mal nomeadas. O engine de Nível 3 renderiza índices gerados pelo sistema a partir dos dados coletados na Phase 0 — lista de figuras, referências, glossário, sumário. O elemento folha de Nível 5 é conteúdo escrito pelo aluno — lista com marcadores, numerada, inline. Não há sobreposição de responsabilidade.

**Correção no documento:** Renomear o engine de Nível 3 de `LIST` para `INDEX`. O elemento folha de Nível 5 mantém o nome `list`.

---

## Problema 3 — Notas de rodapé delegadas à Phase 3

**Problema levantado:** O documento lista notas de rodapé como responsabilidade da Phase 3 via LibreOffice. Notas de rodapé são vinculadas ao parágrafo onde o marcador aparece — não dependem de conhecimento do layout físico. O docx4j suporta `w:footnote` nativamente.

**Resultado da discussão:** A adição das notas de rodapé pertence à Phase 2 via `Docx4jWriter`. O motor escreve a nota vinculada ao parágrafo correto usando `w:footnote` e o processador de texto posiciona no rodapé da página correta automaticamente.

Porém existe um risco pós Phase 3: as modificações da Phase 3 — inserção de linhas "continua/continuação" em tabelas e outras correções — alteram a paginação do documento. Um parágrafo que estava na página 25 pode ir para a página 26 após as correções, e a nota vinculada a ele pode não acompanhar corretamente dependendo de como o LibreOffice via UNO recalcula vínculos de footnote após modificações. Esse comportamento precisa ser verificado empiricamente na implementação da Phase 3. Se o LibreOffice recalcula vínculos automaticamente, nenhuma ação adicional é necessária. Se não recalcula, é um problema a resolver naquele momento.

**Correção no documento:**
- Remover notas de rodapé da Phase 3.
- Adicionar notas de rodapé como responsabilidade da Phase 2.
- Adicionar `footnote` como elemento folha de Nível 5 — estava ausente da taxonomia. Estrutura: marcador inline no parágrafo pai e texto associado da nota.
- Registrar como incógnita a verificar: estabilidade de vínculos de footnote após modificações da Phase 3.

---

## Problema 4 — Heading órfão na Phase 3

**Problema levantado:** O documento lista correção de headings órfãos como responsabilidade da Phase 3. O OOXML tem `<w:keepNext/>`, propriedade aplicada ao parágrafo do heading que instrui o processador de texto a nunca separar o heading do parágrafo seguinte — resolvível na Phase 2.

**Resultado da discussão:** A solução via Phase 2 é viável mas não trivial. A estrutura comum nos documentos é "heading → linha em branco → parágrafo de conteúdo". Aplicar `<w:keepNext/>` apenas no heading une heading e linha em branco, mas linha em branco e parágrafo podem se separar — o resultado visual ainda é ruim. Para funcionar corretamente, `<w:keepNext/>` precisa ser aplicado também na linha em branco após o heading, criando a cadeia "heading → linha em branco → parágrafo" unida. Isso requer que o `DocxBlankLine` emitido após um heading carregue um sinalizador e que o `Docx4jWriter` aplique `<w:keepNext/>` nele também.

Adicionalmente, mesmo que a Phase 2 resolva corretamente, as modificações da Phase 3 alteram a paginação — um heading bem posicionado antes das correções pode se tornar órfão depois delas. O `<w:keepNext/>` sobrevive a esse recálculo porque o processador de texto o aplica a cada novo layout, então a propriedade continua eficaz após as modificações da Phase 3.

**Correção no documento:** Remover heading órfão da Phase 3 e registrar como responsabilidade da Phase 2 via `<w:keepNext/>` em cadeia — heading e linha em branco imediatamente após o heading. Documentar que o `DocxBlankLine` pós-heading precisa de sinalizador para que o `Docx4jWriter` saiba aplicar a propriedade.

---

## Problema 5 — Ausência de contrato de falha para `cross-ref` com `targetId` inválido

**Problema levantado:** O documento define o elemento `cross-ref` com resolução na Phase 0 mas não declara o comportamento quando o `targetId` não existe no índice.

**Resultado da discussão:** O `validationMode` já existente no sistema resolve isso diretamente. Em modo `STRICT` a Phase 0 detecta o `targetId` inválido e falha imediatamente com erro claro identificando qual id está quebrado. Em modo `FLEXIBLE` a Phase 0 registra o id como não resolvido, a Phase 1 substitui o `cross-ref` por placeholder visível — ex: `[?]` — e emite aviso. O documento é gerado com indicação visual do problema.

Adicionalmente, o frontend pode validar `cross-ref` em tempo real durante a edição do content — o editor conhece os ids existentes no documento naquele momento e pode alertar o aluno antes de ele tentar gerar. Isso evita processamento desnecessário e dá feedback imediato. A validação no backend existe como garantia independente da origem do request.

**Correção no documento:** Adicionar seção de contrato de falha ao elemento `cross-ref` declarando o comportamento por `validationMode`. Registrar a validação no frontend como camada complementar.

---

## Problema 6 — Formatação inline perdida no título híbrido

**Problema levantado:** O título híbrido — ex: `"APÊNDICE A — Formulários de Pesquisa"` — é modelado como string única com um único `styleId`. Se um profile precisar de formatação diferente por segmento (prefixo em negrito, título em peso normal), o modelo não consegue expressar.

**Resultado da discussão:** O título híbrido é conceitualmente uma sequência de segmentos distintos com origens distintas — prefixo vindo do profile, sequência gerada pelo motor, separador vindo do profile, título vindo do content. O modelo de `DocxParagraph` com `DocxRun` já suporta múltiplos runs com formatações independentes no mesmo parágrafo — a infraestrutura existe. O custo de modelar como segmentos agora é baixo. O custo de migrar depois, quando o schema do profile já estiver em uso por profiles reais, é maior.

**Correção no documento:** Substituir a declaração de `itemHeading` como string única por uma declaração de segmentos, cada um com `source` (`PREFIX`, `SEQUENCE`, `LITERAL`, `CONTENT`) e `styleId` próprio. O profile ABNT-UNIP pode usar o mesmo `styleId` para todos os segmentos — o comportamento atual é preservado — mas o modelo passa a suportar diferenciação quando necessário.

---

## Problema 7 — Ausência de estratégia de migração

**Problema levantado:** O documento descreve o modelo alvo mas não menciona como chegar lá a partir do código existente.

**Resultado da discussão:** Não é uma falha do documento. O objetivo do documento é definir o modelo alvo. O plano de migração é uma etapa posterior que só faz sentido depois que o destino está fechado e validado. O próprio cabeçalho do documento declara isso explicitamente.

**Correção no documento:** Nenhuma.

---

## Resumo das correções a aplicar no documento de taxonomia

| # | Correção |
|---|---|
| 1 | Renomear engine `LIST` para `INDEX` em todo o documento |
| 2 | Remover notas de rodapé da Phase 3 |
| 3 | Adicionar notas de rodapé como responsabilidade da Phase 2 |
| 4 | Adicionar `footnote` como elemento folha de Nível 5 |
| 5 | Registrar como incógnita: estabilidade de vínculos de footnote após modificações da Phase 3 |
| 6 | Remover heading órfão da Phase 3 |
| 7 | Registrar heading órfão como responsabilidade da Phase 2 via `<w:keepNext/>` em cadeia |
| 8 | Documentar sinalizador necessário no `DocxBlankLine` pós-heading para o `Docx4jWriter` |
| 9 | Adicionar contrato de falha do `cross-ref` por `validationMode` |
| 10 | Registrar validação de `cross-ref` no frontend como camada complementar |
| 11 | Substituir `itemHeading` de string única por declaração de segmentos com `source` e `styleId` por segmento |
