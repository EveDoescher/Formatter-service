# Plano: pós-processamento ativo para sumário, listas, orphan e continua/continuação/conclusão

## Diagnóstico atual

### Sumário (TOC)
O `Docx4jWriter` emite o campo TOC corretamente: `BEGIN → instrText → SEPARATE → END` com `dirty=true`.
`settings.xml` recebe `<w:updateFields w:val="true"/>`.
O Word **precisa atualizar o campo ao abrir** — mas campos TOC sem cache não exibem nada enquanto fechados. O comportamento esperado é: abrir → Word detecta `updateFields=true` → atualiza automaticamente → sumário aparece.

**O problema reportado ("campo existe mas não atualiza")** pode ser uma de duas coisas:
1. O Word está abrindo e **não está atualizando** mesmo com `updateFields=true` (possível se o modo de edição estiver protegido ou se o campo não tiver o `dirty` propagado corretamente).
2. O campo está correto mas o cliente está vendo o arquivo **antes** de abrir no Word (ex: preview, Google Docs, LibreOffice).

**Solução garantida:** pós-processador Java puro que abre o ZIP e injeta cache de texto no campo SEPARATE→END. Isso faz o TOC aparecer **sem** precisar que o Word atualize.

### Listas de elementos (PAGEREF)
Os campos `PAGEREF` estão emitidos com `BEGIN → instrText → SEPARATE → END` + `dirty=true`. O problema é o mesmo: o Word precisa atualizar ao abrir. Se o usuário vê o arquivo sem abrir no Word, ou o Word não está propagando `updateFields`, os números ficam em branco.

**Solução:** o pós-processador não pode resolver PAGEREFs sem renderizar o documento — não temos o mapeamento bookmark→página. A única solução garantida é injetar os números via renderização. Isso requer uma abordagem diferente: mudar a arquitetura do índice de elementos.

### Orphan title correction
A regra existe no profile (`orphanTitleCorrection: {enabled: true}`) mas nunca foi implementada após remover o UNO script. Correção de órfão em DOCX = adicionar `keepWithNext` nos parágrafos de heading que seriam o último elemento de uma página. Sem renderização do layout não sabemos onde as páginas quebram.

**Solução: `keepWithNext` preventivo.** Todos os headings já recebem `keepWithNext=true` implicitamente? Verificar. Se não, é simples: o `SectionIndexRenderer` ou o `BodyContentRenderer` deve emitir `keepWithNext=true` em todo parágrafo de heading. Isso é semanticamente correto (um título nunca fica sozinho no final da página) e não requer saber onde a página quebra.

### Continua / continuação / conclusão
Já implementado na camada de rendering (`DisplayObjectRenderingState`, `DisplayObjectContinuationLabels`, `TextTypeRegistry`). O label é emitido como sufixo da legenda. **O que falta?** Verificar se as tabelas com `repeatHeaderOnPageBreak=true` estão exibindo o label de forma adequada, e se o label do footer ("continua") aparece na linha após a tabela antes da quebra de página.

Atualmente o label é injetado na legenda como `" (continua)"`. A ABNT exige que o label apareça na **linha abaixo da tabela** (antes do page break), alinhado à direita. Isso é diferente de colocar na legenda.

---

## Plano de implementação

### Etapa 1 — Orphan title correction via `keepWithNext` preventivo

**Onde:** `Docx4jWriter.writeParagraph` ou melhor, no ponto de emissão dos headings no rendering.

**O que fazer:**
- Em `SectionIndexRenderer` e em todos os renderers que emitem headings, garantir que o `DocxParagraph` do heading tenha `keepWithNext=true`.
- No `BodyContentRenderer` / `FlowLayoutEngine`, já existe suporte a `keepWithNext` no `DocxParagraph`. Verificar se está sendo usado nos headings.
- Se não estiver, adicionar `keepWithNext=true` na emissão de heading no `FlowLayoutEngine`.

Isso é uma mudança de rendering puro — sem pós-processamento, sem ZIP.

**Nenhuma dependência do `PostProcessingRule.orphanTitleCorrection`**: pode virar comportamento padrão do motor, não precisando de flag no profile.

---

### Etapa 2 — Continua/continuação/conclusão como parágrafo separado

**Problema atual:** o label `(continua)` é apendado ao texto da legenda. ABNT quer o label em linha própria, alinhada à direita, após a tabela e antes do page break.

**Onde:** `TextTypeRegistry.renderTable` e `TextTypeRegistry.renderFrame`.

**O que fazer:**
- Quando `part.continuationLabel().isPresent()` e o label é `continua` ou `continuação` (não a última parte), emitir um `DocxParagraph` adicional **após** o bloco de tabela com o label alinhado à direita.
- O label `conclusão` (última parte) também vai em linha própria após a tabela.
- Remover o sufixo `" (" + label + ")"` da legenda.
- O estilo do parágrafo do label vem do `PostProcessingRule.TableContinuationLabelsRule.labelStyleId`.
- **Mas espera:** `TextTypeRegistry` não tem acesso ao `PostProcessingRule`. Ele recebe `FlowRenderingContext`. O `PostProcessingRule` está no `DocumentProfile`. Precisa ser passado pelo contexto.

**Alteração no `FlowRenderingContext`:** adicionar `Optional<PostProcessingRule> postProcessingRule`.

**No `BodyContentRenderer`:** popular o `postProcessingRule` a partir do `profile.postProcessingRule()`.

**Em `TextTypeRegistry`:** quando emitindo tabela/quadro com continuationLabel, verificar se `postProcessingRule.tableContinuationLabels.enabled`. Se sim, emitir parágrafo de label separado com o estilo declarado.

---

### Etapa 3 — TOC: garantir atualização ao abrir

**Análise:** o campo TOC com `dirty=true` + `updateFields=true` no `settings.xml` **deveria** atualizar ao abrir no Word. O problema pode estar no fato de que o `dirty` está no `BEGIN` fldChar, mas o Word às vezes requer que o `dirty` esteja no nível do field como atributo separado.

**O que fazer:**
- Verificar se o campo TOC tem `w:fldChar w:fldCharType="begin" w:dirty="1"` (correto) — já está.
- Adicionar cache de texto placeholder no SEPARATE→END: um run com texto `"Atualize o sumário ao abrir"` entre o `SEPARATE` e o `END`. Isso não resolve o sumário, mas pelo menos mostra algo ao usuário e confirma que o campo existe.
- **Alternativa robusta:** emitir o TOC como **campo complexo em parágrafo separado** com o estilo `TOC1`/`TOC2` já definido nos estilos do documento, ao invés de estilo Normal. O Word pode ignorar campos em parágrafos de estilo errado.

**Investigação necessária antes de implementar:** gerar um DOCX mínimo com apenas um TOC field, abrir no Word e confirmar se atualiza. Se sim, o problema está em outro lugar do documento gerado.

**Diagnóstico proposto (Etapa 3a):** adicionar endpoint de diagnóstico ou test que gera um DOCX mínimo com apenas o campo TOC, para isolar o problema.

---

### Etapa 4 — PAGEREF: injetar números via pós-processamento ZIP

**Por que é necessário:** sem renderização visual, não temos como saber em qual página cada bookmark está. O Word precisa calcular isso ao renderizar o layout.

**Abordagem alternativa — não usar PAGEREF:**
Em vez de campos `PAGEREF`, usar texto estático `"?"` por padrão durante rendering, e aceitar que o número de página não é resolvido automaticamente.

**Abordagem correta — aceitar dependência do Word:**
Os campos `PAGEREF` são a solução correta. O Word **vai** resolvê-los ao abrir com `updateFields=true`. Se não estiver resolvendo, o problema está em como o `updateFields` está sendo aplicado ou em como os campos estão estruturados.

**O que verificar:**
1. O `settings.xml` está recebendo `updateFields=true`?
2. Os PAGEREFs têm `dirty=true` no BEGIN?
3. Os bookmarks têm o nome correto (`elem_<id>`)?

**Diagnóstico proposto:** inspecionar o ZIP gerado, extrair `word/settings.xml` e verificar se `updateFields` está lá. Extrair `word/document.xml` e contar os campos PAGEREF e os bookmarks.

---

## Ordem de execução recomendada

1. **Diagnóstico (não é código):** inspecionar o ZIP e confirmar o que está e o que não está. Isso resolve a incerteza sobre TOC e PAGEREF antes de codificar.

2. **Etapa 1:** orphan title correction via `keepWithNext` nos headings — simples, sem dependências, muda o rendering.

3. **Etapa 2:** continua/continuação/conclusão como parágrafo separado — muda `TextTypeRegistry` + `FlowRenderingContext`.

4. **Etapa 3:** baseado no diagnóstico, corrigir o campo TOC se necessário.

5. **Etapa 4:** baseado no diagnóstico, corrigir os PAGEREFs se necessário.

---

## Decisão arquitetural sobre o pós-processador

O `DocxPostProcessor` atual passa os bytes direto (NoOp ou LibreOffice só para PDF). **Não há necessidade de criar um pós-processador ZIP** para os itens 1, 2 e 3 — tudo é resolvido na camada de rendering.

O único cenário onde um pós-processador ZIP seria necessário é se precisarmos injetar números de página reais nos PAGEREFs — o que exigiria renderizar o layout, algo que está fora do escopo do serviço atual.

A decisão de aceitar que TOC e PAGEREF são resolvidos pelo Word ao abrir é arquiteturalmente correta: o serviço gera o DOCX semanticamente correto, o Word aplica o layout. Essa é a divisão natural de responsabilidades.
