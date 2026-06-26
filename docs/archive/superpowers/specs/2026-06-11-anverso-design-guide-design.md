# Design Guide — Anverso

**Data:** 2026-06-11
**Contexto:** Design guide do Anverso como ponto de partida do frontend em Next.js + Tailwind CSS. Serve tanto de referência interna de desenvolvimento quanto de apresentação da marca a stakeholders.

---

## Objetivo

Uma página web estática (mas interativa) que documenta a identidade visual, sistema de cores, tipografia, componentes e tom de voz do Anverso. Os componentes documentados aqui são os componentes reais que serão usados no app — o design guide é o embrião do frontend.

---

## Stack

- **Next.js** (App Router)
- **Tailwind CSS**
- Projeto separado em `frontend/` dentro do monorepo, ou novo repo `anverso-web`
- Sem dependências de UI externas — os componentes são todos custom

---

## Estrutura de Layout

**Padrão: Sidebar fixa + conteúdo em grid editorial**

- Sidebar vertical à esquerda (72px, fundo `#2D3228`) com ícone + label por seção, scroll spy ativo
- Topbar interna por seção com título e pill de contexto
- Conteúdo em grid assimétrico — células de tamanhos variados por seção (`grid-cols-2`, `col-span-2`, etc.)
- Background geral: `#FAF8F2` (creme papel)
- Células brancas com borda sutil `#E8E2D5`, border-radius 8px

**Interatividade (média):**
- Scroll spy na sidebar (highlight da seção ativa)
- Clicar num swatch copia o hex para o clipboard (toast de feedback)
- Hover states em todos os componentes documentados
- Smooth scroll ao clicar na nav

---

## Seções

### 1. Marca
- Logo (BookLogo.png) + nome "Anverso" em Playfair Display
- Tagline: "Seu trabalho em ordem, sem complicação."
- Três pilares: **Calma · Clareza · Confiança**
- O que evitar (lista): visual frio, formulários agressivos, jargão pesado, tons púrpura/neon

### 2. Cores
- **Paleta principal** (5 cores): Verde Escuro `#2D3228`, Verde Floresta `#4A7C59`, Dourado Suave `#C8A86A`, Creme Papel `#F8F5E8`, Areia `#E8E2D5`
- **Cores de estado**: Sucesso `#4A7C59`, Atenção `#D97706`, Erro `#B5323B`, Neutro `#6B7280`
- **Branco**: `#FFFFFF` para superfícies de formulário
- Cada swatch: mostra nome, hex, uso — clique copia o hex

### 3. Tipografia
- **Display/Headings**: Playfair Display (serif) — "Títulos Acolhedores"
- **Body/UI**: DM Sans ou similar humanista — "Texto claro e legível"
- Escala: H1 (48px) → H2 (32px) → H3 (24px) → Body (16px) → Small (14px) → Label (12px uppercase)
- Cada nível mostra: nome, tamanho, peso, exemplo de uso real no app

### 4. Componentes
Componentes documentados com estado padrão + variantes:
- **Botões**: Primary (`bg-[#4A7C59]`), Outline, Ghost, Disabled
- **Tags de status**: Completo (verde), Em andamento (âmbar), Rascunho (cinza), Atenção (vermelho)
- **Input de texto**: padrão, focus, erro, com dica
- **Checklist item**: checked, unchecked, com alerta
- **Toast/Dica rápida**: informação, sucesso, atenção
- **Card de trabalho** *(componente faltante do Frame 1)*: card com título do trabalho, subtítulo de perfil (ex: "ABNT UNIP"), barra de progresso colorida, status badge, timestamp relativo, menu "···" no canto. Estados: Em andamento, Quase pronto, Completo, Rascunho
- **Stepper de navegação** *(componente faltante do Frame 1)*: lista vertical de etapas "ETAPAS DO TRABALHO" — Perfil → Capa → Conteúdo → Revisão → DOCX. Cada etapa tem: ícone circular (completo=verde preenchido, ativo=anel verde, pendente=anel cinza), label, sublabel opcional. Linha conectora entre etapas.
- **Prévia do documento** *(componente faltante do Frame 1)*: miniatura em escala do documento sendo montado. Mostra folha A4 em proporção real com conteúdo simplificado (linhas de texto simuladas), título da seção em visualização, indicador de página. Aparece como painel lateral direito no editor.

### 5. Ícones
Grid dos ícones disponíveis em `docs/design/icons/`:
- Funcionais: bookmark, calendar, cloud-computing, delete, diskette, docs-icon, download, edit, info, more, search, view, warning, right-arrow
- Estados: OK, ATENTION, INFORMATION
- Inputs: check-box-on/off, radio-btn-on/off
- Decorativos: leaves, leaves-2, leaves-3, xicara, line
- Cada ícone: imagem + nome do arquivo + uso sugerido

### 6. Tom de Voz
- **Princípio**: fala como um colega que entende do processo, não como um sistema
- Exemplos de microcopy correto vs. evitar:
  - ✓ "Falta informar a cidade para montarmos a capa." vs. ✗ "Campo obrigatório não preenchido."
  - ✓ "Respira. Seu trabalho está formatado." vs. ✗ "Documento gerado com sucesso."
  - ✓ "Vamos por partes." vs. ✗ "Preencha o formulário."
  - ✓ "Quase pronto — confira a cidade da capa." vs. ✗ "Erro: campo cidade vazio."

---

## Decisões de Design

| Decisão | Justificativa |
|---|---|
| Fundo creme `#F8F5E8` em vez de branco | Remete a folha de papel, reduz fadiga visual, coerente com o produto |
| Tipografia serif no display | Contrapõe o ambiente acadêmico formal com acolhimento — não é fria |
| Verde escuro como primária, não azul | Diferencia do padrão "app institucional"; transmite organicidade e calma |
| Ícones PNG customizados, não biblioteca | Identidade própria, coerência com o estilo botânico/orgânico da marca |
| Sidebar escura sobre fundo claro | Ancora visualmente, cria contraste sem agredir — o "spine" do app |

---

## O Que Evitar

- Purple gradients ou qualquer coisa que lembre SaaS genérico
- Fonte Inter, Roboto, ou system fonts no display
- Bordas azuis em foco (usar verde `#4A7C59`)
- Modais de erro agressivos — preferir dicas inline calmas
- Ícones de biblioteca (Heroicons, Lucide) — usar os ícones customizados do projeto
