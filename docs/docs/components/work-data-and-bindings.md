# Work Data And Content Bindings

`work` is the shared semantic data source for repeated academic work data.

It exists to avoid repeating the same authors, title, subtitle, nature, advisor,
city and year inside every component request.

## Ownership

```text
work
document data entered by the user

profile.componentRules.*.contentBindings
profile rule that maps component fields to work data

document.<component>
component activation and explicit overrides
```

Components must not copy data from other components. For example,
`titlePage.authors` must not depend on `cover.authors`.

The correct relationship is:

```text
cover.authors        <- work.authors
titlePage.authors    <- work.authors
approvalSheet.authors <- work.authors
```

The profile owns those relationships through `contentBindings`.

## Request Shape

```json
{
  "fileName": "documento.docx",
  "profileId": "abnt-unip-profile",
  "work": {
    "authors": ["Nome do Aluno"],
    "title": "Título do trabalho",
    "subtitle": "Subtítulo do trabalho",
    "nature": {
      "workType": "Trabalho de conclusão de curso",
      "degreeObjective": "obtenção do título de tecnólogo",
      "courseName": "Análise e Desenvolvimento de Sistemas",
      "institutionName": "Universidade Paulista - UNIP"
    },
    "advisor": {
      "academicTitle": "Prof. Dr.",
      "name": "Nome do orientador"
    },
    "city": "Limeira",
    "year": "2026"
  },
  "document": {
    "cover": {},
    "titlePage": {},
    "approvalSheet": {},
    "bodyContent": {
      "sections": []
    }
  }
}
```

An empty component object means the component is selected and may resolve its
content from `work` through profile bindings.

## Resolution Rule

```text
1. explicit component value wins;
2. if the component value is absent, use the profile binding;
3. if the binding points to missing work data, the final component validation fails;
4. never infer equality between fields from different components;
5. never copy values from one component to another.
```

Example override:

```json
{
  "work": {
    "title": "Título comum"
  },
  "document": {
    "cover": {
      "title": "Título especial só para a capa"
    },
    "titlePage": {}
  }
}
```

In this case:

```text
cover.title = "Título especial só para a capa"
titlePage.title = work.title
```

## Profile Bindings

The official UNIP profile maps shared fields like this:

```json
{
  "cover": {
    "contentBindings": {
      "institutionalLines": "work.institutionalLines",
      "authors": "work.authors",
      "title": "work.title",
      "subtitle": "work.subtitle",
      "city": "work.city",
      "year": "work.year"
    }
  },
  "titlePage": {
    "contentBindings": {
      "authors": "work.authors",
      "title": "work.title",
      "subtitle": "work.subtitle",
      "nature": "work.nature",
      "advisor": "work.advisor",
      "coadvisor": "work.coadvisor",
      "city": "work.city",
      "year": "work.year"
    }
  },
  "approvalSheet": {
    "contentBindings": {
      "authors": "work.authors",
      "title": "work.title",
      "subtitle": "work.subtitle",
      "nature": "work.nature"
    }
  }
}
```

This is profile-authoring metadata. A future profile builder UI should expose
this as dropdown mappings, not as raw JSON for everyday users.
