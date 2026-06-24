# bodyContent Fase 7 — Pós-processamento Avançado (Conceito)

> **Status:** Conceito pré-MVP. Não implementar antes de concluir a Fase 6.
>
> **Pré-requisito:** Fase 5 concluída — especificamente a Task 7 (LibreOffice `--convert-to` básico funcionando).

**Ideia central:** Estender o pós-processador LibreOffice para ir além da resolução de campos TOC. Usar a UNO API para inspecionar o layout real do documento (posição de elementos por página) e aplicar correções que só são possíveis depois que o processador de texto fez o layout tipográfico completo.

---

## Por que uma fase separada

Tudo que está nas Fases 1–6 acontece **antes** do layout: o formatter decide o que vai no documento, o docx4j escreve o XML, e o processador de texto decide onde as quebras de página caem. Há uma classe inteira de problemas que só pode ser resolvida **depois** do layout:

- Saber que uma tabela quebrou de página exige saber onde as páginas caem
- Saber que um título ficou órfão exige saber qual foi o último elemento de cada página
- Saber quantas páginas tem o documento exige render completo

A Fase 7 opera nessa janela: LibreOffice já fez o layout, o documento está aberto em memória via UNO, e o formatter pode inspecionar e corrigir antes de salvar.

---

## Casos de uso identificados

### 1. Tabelas com "continua / continuação / conclusão" (alta prioridade)

ABNT NBR 14724 e praticamente todos os manuais institucionais exigem que tabelas longas que quebram de página recebam:
- Última linha antes da quebra: célula com "continua" alinhada à direita
- Primeiras linhas das páginas intermediárias: célula com "continuação" alinhada à direita
- Primeira linha da última página: célula com "conclusão" alinhada à direita

Isso só pode ser inserido depois que o layout revelou quais linhas ficam em cada página.

**Complexidade:** Alta. Inserir linhas numa tabela aberta no UNO e re-layout é possível, mas pode mudar a paginação das tabelas seguintes — requer iteração até estabilizar ou aceitar uma única passagem.

### 2. Títulos órfãos no final de página (média prioridade)

Um título de seção que fica isolado na última linha de uma página sem nenhum parágrafo abaixo é considerado má formatação. A solução padrão é inserir uma quebra de página antes do título. Via UNO é detectável comparando a página do título com a página do primeiro parágrafo filho.

**Complexidade:** Média. Detectar é simples, corrigir (inserir `<w:pageBreakBefore>`) é direto.

### 3. Verificação de integridade pós-layout (baixa complexidade, alto valor)

Sem modificar o documento, apenas verificar e retornar avisos/erros:
- Alguma figura extrapolou a margem da página?
- Alguma seção ficou sem conteúdo (título sem parágrafos após)?
- O número de páginas está dentro do limite declarado no perfil?
- Alguma fonte foi substituída pelo LibreOffice (indica fonte não instalada no servidor)?

Esses avisos podem ser retornados como headers HTTP ou como um envelope de resposta com o DOCX + metadados de qualidade.

### 4. Geração de PDF junto com o DOCX (baixa complexidade)

Segunda chamada `--convert-to pdf` após resolver os campos. O PDF já sai com sumário e páginas corretos porque o LibreOffice já fez o layout na primeira passagem.

---

## Decisões arquiteturais a tomar antes de implementar

### D1 — UNO API: macro Basic embarcada ou Python-UNO?

**Macro Basic embarcada** (`--headless 'macro:///...'`):
- Lógica num arquivo `.bas` no servidor
- Sem dependência de Python
- API do LibreOffice Basic é mais limitada e menos documentada
- Difícil de testar unitariamente

**Python-UNO como processo filho:**
- Java chama um script Python via `ProcessBuilder`
- Python conecta ao LibreOffice via socket UNO
- API mais rica, melhor documentação, mais exemplos disponíveis
- Adiciona Python como dependência do servidor
- Testável isoladamente

**Recomendação a avaliar:** Python-UNO, pela facilidade de manutenção da lógica de inspeção de layout. Macro Basic para casos mais simples.

### D2 — Como o pós-processador recebe instruções do perfil?

O perfil atualmente não sabe nada de pós-processamento. Precisaria de uma seção nova, por exemplo:

```json
"postProcessing": {
  "tableContinuationLabels": {
    "enabled": true,
    "continuesLabel": "continua",
    "continuationLabel": "continuação",
    "conclusionLabel": "conclusão",
    "labelStyleId": "table.continuation"
  },
  "orphanTitleCorrection": {
    "enabled": true
  }
}
```

Ou o pós-processador recebe o perfil inteiro e extrai o que precisa.

Essa decisão afeta `ExportDocxCommand` (precisa passar o perfil para o pós-processador) e a interface `DocxPostProcessor` (que atualmente só recebe `byte[]`).

**Candidato para a interface expandida:**
```java
public interface DocxPostProcessor {
    byte[] process(byte[] docxBytes, DocumentProfile profile);
}
```

### D3 — Estratégia para tabelas com múltiplas quebras

Inserir linhas "continua/conclusão" muda o layout — pode fazer a tabela quebrar em mais ou menos páginas do que antes. Duas abordagens:

**Passagem única:** inserir as linhas, aceitar que o layout pode estar levemente errado na segunda visita. Resultado "bom o suficiente" para a maioria dos casos.

**Passagem iterativa:** inserir, re-layout, verificar novamente, repetir até estabilizar. Mais correto, mais lento, risco de loop infinito.

Para MVP da Fase 7, passagem única é suficiente.

### D4 — Modelo de resposta da API

Atualmente o endpoint retorna `byte[]` (o DOCX). Se o pós-processador de integridade gerar avisos, onde eles aparecem?

Opções:
- Header HTTP customizado: `X-Formatter-Warnings: tabela-quebrada,fonte-substituida`
- Endpoint separado: `/api/v1/exports/docx/validate` que retorna JSON com os avisos
- Envelope de resposta com base64 do DOCX + lista de avisos (quebra o contrato atual)

A decisão mais conservadora é um header HTTP — não quebra o contrato existente.

---

## O que precisa existir antes de começar a Fase 7

1. **LibreOffice instalado e testado no ambiente de deploy** — a Fase 5 Task 7 já exige isso para o `--convert-to` básico. Se isso funcionar no ambiente real, a Fase 7 tem base.

2. **Decisão sobre D1** (macro vs Python-UNO) — muda significativamente o setup do servidor e a estrutura dos testes.

3. **Perfil com seção `postProcessing`** — implica mudança no `DocumentProfile`, `ProfileRequest`, e `abnt-unip-profile.json`. Deve ser planejada antes de qualquer implementação de pós-processamento condicionado por perfil.

4. **Interface `DocxPostProcessor` expandida** (D3) — a Fase 5 Task 7 define a interface como `byte[] process(byte[] docxBytes)`. Se a Fase 7 precisar do perfil, a interface muda e o `NoOpDocxPostProcessor` e `LibreOfficeDocxPostProcessor` existentes precisam ser atualizados.

---

## O que NÃO pertence à Fase 7

- Qualquer lógica que possa ser decidida antes do layout (tamanho de fonte, margens, espaçamento) — pertence ao perfil
- Validação de conteúdo do usuário (campos obrigatórios, referências ausentes) — pertence à camada de rendering
- Geração de conteúdo novo que não depende de paginação — pertence às Fases 1–6
