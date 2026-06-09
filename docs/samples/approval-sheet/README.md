# Approval Sheet Samples

Samples for the `approvalSheet` single-page component.

- `approval-sheet-short.json`: baseline válido da folha de aprovação.
- `approval-sheet-long-title.json`: título e subtítulo maiores, ainda válidos.
- `approval-sheet-many-committee-members.json`: banca com três membros.
- `approval-sheet-without-approval-event.json`: valida template UNIP sem dados de aprovação.
- `approval-sheet-overflow.json`: esperado falhar por exceder uma página segura.

Samples compostos ficam em `docs/samples/composed`.

Expected behavior:

```text
gera DOCX;
folha de aprovação fica em uma página;
não renderiza cidade/ano;
renderiza Aprovado(a) em: ______/______/______;
renderiza BANCA EXAMINADORA;
renderiza cada membro da banca como bloco estruturado;
falha antes do DOCX se o conteúdo exceder a página segura.
```
