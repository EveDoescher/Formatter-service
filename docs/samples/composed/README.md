# Composed Samples

Samples that exercise more than one component in document order.

- `cover-and-title-page.json`: capa + folha de rosto.
- `cover-title-page-approval-sheet.json`: capa + folha de rosto + folha de aprovação.
- `full-document-with-body-content.json`: capa + folha de rosto + folha de aprovação + corpo textual.
- `full-document-with-work-bindings.json`: documento completo usando `work` e `contentBindings` para evitar repeticao de dados comuns.

Component-specific samples must stay in their own folders:

```text
docs/samples/cover
docs/samples/title-page
docs/samples/approval-sheet
docs/samples/body-content
```
