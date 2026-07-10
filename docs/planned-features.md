# Planned Features

Este documento descreve funcionalidades que fazem parte da visão arquitetural do serviço mas que
ainda não foram implementadas. Cada item documenta o que existe hoje, o que faltaria construir,
e as questões em aberto que precisam ser respondidas antes de começar.

### Posições decididas

| Feature | Posição |
|---|---|
| Font Choice | **Implementar** — alternativa de perfis duplicados não escala |
| Validation STRICT/FLEXIBLE | **Não implementar** — custo alto, valor baixo no modelo atual |
| Content Bindings (`work`) | **Não implementar agora** — esperar integração com work-service |
| `orphanTitleCorrection` com flag | **Implementar** — trivial, elimina inconsistência real |
| `integrityCheck` | **Não implementar** — sintoma de problemas que devem ser prevenidos |
| `pdfOutput` conectado ao profile | **Implementar** — valor real, mas definir response primeiro |

---

## 1. User Preferences — Font Choice

### Por que implementar

O profile declara **o que é permitido**. As preferences declaram **o que foi escolhido**. Isso é
exatamente o papel arquitetural das preferences — e font choice é o exemplo mais claro disso.

A alternativa de perfis duplicados (ex: `abnt-unip-arial`, `abnt-unip-times`) não escala. Um
profile APA que permita Calibri, Times, Arial e Georgia resultaria em quatro perfis idênticos que
divergem apenas em `fontFamily`. Qualquer alteração de regra precisaria ser replicada em todos.
Isso é pior arquiteturalmente do que implementar o mecanismo de preferences.

### O que existe hoje

Nenhuma estrutura para isso. O campo `options` do request só aceita `selectedComponents`. O
profile declara `fontFamily` como valor fixo em cada `styleRule` — não há lista de opções nem
mecanismo de substituição.

### Design decidido

**No profile:** um campo `fontRoles` no topo do profile, mapeando nome do papel para suas
opções. O papel agrupa os `styleRule`s que devem ter a fonte substituída — o mapeamento fica
inteiramente no profile, os `styleRule`s não mudam.

```json
"fontRoles": {
  "baseFont": {
    "default": "Times New Roman",
    "allowedValues": ["Times New Roman", "Arial"],
    "styleIds": ["bodyContent.paragraph", "bodyContent.heading1", "cover.title", "..."]
  },
  "codeFont": {
    "default": "Courier New",
    "allowedValues": ["Consolas", "Courier New"],
    "styleIds": ["bodyContent.code"]
  }
}
```

Regras:
- `allowedValues` ausente ou vazio → fonte fixa, escolha não permitida para esse papel.
- Um `styleRule` não listado em nenhum `fontRole` → `fontFamily` fixo conforme declarado.
- Um profile sem `fontRoles` → nenhuma escolha de fonte disponível (comportamento padrão atual).
- Os papéis são definidos livremente pelo profile — `baseFont` e `codeFont` são exemplos, não
  nomes reservados. Uma norma que precise de papel separado para tabelas pode declarar
  `tableFont`, por exemplo.

**No request:**

```json
"options": {
  "selectedComponents": [...],
  "fonts": {
    "baseFont": "Arial"
  }
}
```

Se o usuário não enviar preference para um papel, o engine usa o `default` declarado no profile.
Se enviar uma fonte fora de `allowedValues`, retorna 400.

**No engine:** ao construir um `StyleRule`, verifica se o ID dele aparece em algum `fontRole`
do profile. Se sim, substitui `fontFamily` pelo valor resolvido (preference do usuário ou
`default` do papel). O `styleRule` em si não é alterado — a substituição acontece na resolução.

### Validação no carregamento do profile

Todo `styleId` declarado em `fontRoles` deve existir em `styleRules`. Validado junto com as
demais referências de `styleId` — referência inválida retorna 400 antes de qualquer renderização.

---

## 2. User Preferences — Validation Mode (STRICT / FLEXIBLE)

### O que foi prometido

O usuário poderia escolher entre dois modos de validação:

- `STRICT`: bloqueia a geração quando regras obrigatórias do profile não são atendidas.
- `FLEXIBLE`: gera o documento mesmo com desvios, emitindo avisos no header
  `X-Formatter-Warnings`.

### O que existe hoje

Toda validação é efetivamente strict — qualquer valor ausente ou inválido falha com exceção.
O campo `validationMode` não existe no request. Os modos não foram implementados.

### O que faltaria construir

1. **No request:** adicionar `validationMode` em `options`.
2. **No engine:** separar erros de validação em dois grupos — erros que bloqueiam renderização
   (sempre fatais) e avisos que podem ser tolerados em modo flexible.
3. **Na camada de output:** coletar avisos acumulados e devolvê-los no header
   `X-Formatter-Warnings`.

### Questões em aberto

- Quais validações são candidatas a serem warnings em vez de erros? (componente opcional sem
  conteúdo? fonte não encontrada? seção sem título?)
- O modo FLEXIBLE tem caso de uso real no produto? Ou é complexidade sem demanda?
- Se o documento gerado em modo FLEXIBLE tiver formatação incorreta, quem é responsável?

---

## 3. Content Bindings — Dados Compartilhados via `work`

### O que foi prometido

Um mecanismo declarativo no profile onde cada componente declara `contentBindings` mapeando seus
campos para campos de um objeto `work` central. Isso evitaria repetição do mesmo dado (ex: título,
autores) em todos os componentes da requisição.

```json
"cover": {
  "contentBindings": {
    "authors": "work.authors",
    "title": "work.title"
  }
}
```

O request ficaria limpo — componentes sem dados próprios seriam objetos vazios `{}` e o engine
resolveria os dados via bindings.

### O que existe hoje

O campo `work` é aceito pelo request por compatibilidade retroativa mas é completamente ignorado.
Cada componente deve receber todos os seus dados explicitamente. Não existe nenhuma classe
`ContentBindings` nem lógica de resolução via bindings.

### O que faltaria construir

1. **No profile:** adicionar `contentBindings` opcional em cada `componentRule`, com um mapa de
   campo local para caminho em `work` (ex: `"authors": "work.authors"`).
2. **No request:** reabilitar o campo `work` com semântica real.
3. **No engine:** um resolver que, para cada slot de um componente sem valor explícito, consulta
   o binding declarado no profile e extrai o valor do objeto `work`.
4. **Regra de precedência:** valor explícito no componente sempre ganha sobre o binding.
5. **Validação:** referência a caminho de `work` inexistente deve falhar na validação do profile.

### Questões em aberto

- Bindings são necessários agora? O custo da repetição de dados no request é problema real?
- O mecanismo de binding adiciona complexidade significativa ao engine sem benefício imediato?
- Quando o work-service existir, ele montará o request completo — os bindings serão resolvidos
  no lado do work-service antes de chamar o formatter, ou pelo formatter?

---

## 4. PostProcessing — Campos Não Lidos do Profile

### O que foi prometido

O campo `postProcessing` do profile pode declarar quatro opções:

- `tableContinuationLabels` — labels em tabelas que quebram entre páginas.
- `orphanTitleCorrection` — move títulos órfãos para a próxima página.
- `integrityCheck` — verifica overflow de margem, substituição de fonte, limite de páginas.
- `pdfOutput` — exporta também em PDF via LibreOffice.

### O que existe hoje

- `tableContinuationLabels`: **implementado** — lido do profile e executado.
- `orphanTitleCorrection`: **implementado** — executado via UNO API do LibreOffice, mas não lido
  do profile (sempre executado quando LibreOffice está disponível, sem configuração).
- `integrityCheck`: **não implementado** — estrutura não existe no código nem no profile JSON.
- `pdfOutput`: **não implementado como opção configurável** — o `LibreOfficeDocxPostProcessor`
  tem infraestrutura para conversão, mas a flag `pdfOutput.enabled` não é lida do profile.

### O que faltaria construir

**`orphanTitleCorrection`:** adicionar a flag ao profile JSON e condicionar a execução a ela.
Atualmente é executado incondicionalmente.

**`integrityCheck`:** construir do zero. Verificações de overflow de margem e substituição de
fonte requerem acesso ao DOCX já gerado via LibreOffice UNO ou análise do XML. Limite de páginas
é mais simples — contar páginas após geração.

**`pdfOutput`:** conectar a flag `pdfOutput.enabled` do profile à execução do
`LibreOfficeDocxPostProcessor`, que já tem a lógica de conversão.

### Questões em aberto

- `integrityCheck` tem valor real? Detectar substituição de fonte exige instrução de LibreOffice
  — qual é a complexidade?
- `pdfOutput` entrega ambos DOCX e PDF, ou substitui? O response precisaria mudar.
- `orphanTitleCorrection` sem flag é aceitável por enquanto, ou vale a pena parametrizar logo?
