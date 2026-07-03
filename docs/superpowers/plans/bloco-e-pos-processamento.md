# Bloco E — Pós-processamento UNO (LibreOffice)

**Data:** 2026-07-03  
**Pré-requisito:** Bloco C-corrigido concluído.  
**Próximo bloco:** F  
**Status:** Bloco de conceito — requer 4 decisões antes de implementar (listadas abaixo).

---

## Objetivo

Estender o pós-processador LibreOffice para aplicar correções que só são possíveis após layout físico completo: sumário com números de página reais, rótulos continua/continuação em tabelas longas, detecção de títulos órfãos, geração de PDF.

---

## Decisões obrigatórias antes de implementar

### E-D1 — Interface do pós-processador

O `DocxPostProcessor` atual recebe apenas `byte[] docxBytes`. Precisa do `DocumentProfile` para ler labels.

```java
// Candidata:
public interface DocxPostProcessor {
    byte[] process(byte[] docxBytes, DocumentProfile profile);
}
```

Atualizar `NoOpDocxPostProcessor` e `LibreOfficeDocxPostProcessor`.

### E-D2 — Profile ganha seção postProcessing

```json
"postProcessing": {
  "tableContinuationLabels": {
    "enabled": true,
    "continuesLabel": "continua",
    "continuationLabel": "continuação",
    "conclusionLabel": "conclusão",
    "labelStyleId": "table.continuation"
  },
  "orphanTitleCorrection": { "enabled": true }
}
```

Modelar `PostProcessingRule` em `profile/model/`. Adicionar em `DocumentProfile`.

### E-D3 — Mecanismo UNO

- **Opção A:** Macro LibreOffice Basic embarcada — sem dependência Python, API mais limitada
- **Opção B:** Python-UNO via `ProcessBuilder` — API mais rica, adiciona Python como dependência

Decidir antes de qualquer implementação.

### E-D4 — Modelo de resposta para avisos

- **Opção A:** Header HTTP `X-Formatter-Warnings` — não quebra contrato existente
- **Opção B:** Envelope JSON com base64 do DOCX + lista de avisos — quebra contrato atual

---

## Steps de implementação (após decisões)

### E-1 — Sumário com números de página reais

O `SummaryRenderer` já emite `DocxTocBlock` com `TOC \o "1-N" \h \z \u`. O LibreOffice resolve o campo e substitui os placeholders por números reais.

- Chamar `--headless --invisible --convert-to docx` após geração
- Verificar empiricamente se `w:footnote` sobrevive às modificações de paginação (incógnita documentada na taxonomia)

### E-2 — Rótulos continua/continuação em tabelas longas

- Detectar via UNO quais linhas de tabela quebram página
- Inserir linha com rótulo "continua" antes da quebra
- Inserir linha com rótulo "continuação" no início de páginas intermediárias
- Inserir linha com rótulo "conclusão" no início da última página da tabela
- Passagem única (MVP) — não iterar até estabilizar

### E-3 — Detecção de títulos órfãos

- Via UNO, comparar página do heading com página do primeiro parágrafo filho
- Quando título isolado no final da página: inserir `pageBreakBefore` antes do título
- Apenas headings de nível 1 (MVP)

### E-4 — Verificação de integridade

- Verificar se alguma figura extrapolou a margem da página
- Verificar se o número de páginas está dentro do limite declarado no profile
- Verificar se alguma fonte foi substituída pelo LibreOffice
- Retornar avisos via mecanismo decidido em E-D4

### E-5 — Geração de PDF

- Segunda chamada `--convert-to pdf` após resolver campos
- Retornar DOCX + PDF ou apenas PDF conforme parâmetro na request

### E-6 — Testes e validação

- Testes de integração com LibreOffice real (não mockado)
- Verificar empiricamente se `w:footnote` sobrevive às modificações de paginação da Phase 3
- `mvn test -q`
- Validação visual: sumário com números de página reais, tabelas longas com rótulos
- Commit do bloco
