<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Relatório de Descoberta — Estágio 1: Arqueologia Digital

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **discovery-report**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Este documento consolida todas as descobertas do Estágio 1.
> Preencha cada seção com as conclusões do time. **Este é o input principal do Estágio 2** — sem ele, a especificação vira chute.

**Time**: [Nome do Time]
**Data**: 19/05/2026
**Edição**:
**Participantes**: [Liste os membros e suas personas]

---

## 1. Sumário Executivo

> Em 3 a 5 frases, resuma o que o time descobriu sobre o SIFAP legado.
> O que é este sistema? Qual sua criticidade? Qual o estado do código?

[Escreva aqui]

---

## 2. Visão Geral do Sistema

### 2.1 Propósito do SIFAP

[Descreva o que o sistema faz com base na análise do código]

### 2.2 Arquitetura Legada

[Descreva a arquitetura: quantos programas, DDMs, fluxos principais]

### 2.3 Usuários e Perfis

[Quem usa o sistema? Quais perfis de acesso existem?]

---

## 3. Principais Descobertas

### 3.1 Regras de Negócio Críticas

> Liste as 5 regras de negócio mais importantes encontradas.

1. [Regra + referência ao catálogo BR-XXX]
2.
3.
4.
5.

### 3.2 Dependências Complexas

> Quais programas estão mais acoplados? Onde há risco de efeito cascata?

[Descreva]

### 3.3 Dívida Técnica Identificada

> Que problemas no código legado vão complicar a migração?

- [ ] [Problema 1]
- [ ] [Problema 2]
- [ ] [Problema 3]

### 3.4 Gaps de Documentação

> O que a documentação existente NÃO cobre?

[Descreva]

---

## 4. Mistérios e Riscos

### 4.1 Mistérios Não Resolvidos (bloqueadores do Estágio 2)

> Extraídos de [`mysteries-found.md`](mysteries-found.md) — classificação `blocks-stage-2`.

| ID  | Descrição | Risco para Migração |
| --- | --------- | ------------------- |
| MYS-001 | Modelo de estados do pagamento ambíguo: `'P'` = pendente (docs) vs PAGO (código); `'G'` sem doc | Máquina de estados errada corrompe todo o ciclo de pagamento |
| MYS-002 | `FATOR-K = 0,347215` ajusta todo VLR-BASE de programa na inclusão (CADPROG) | Todos os valores-base migram errados se o fator não for replicado |
| MYS-003 | Backdoor: prefixos de CPF (`000,001,...,999`) anulam todas as validações (VALDOCS) | Cadastro fraudulento; decisão de segurança pendente |
| MYS-004 | Relatório de auditoria oculta exclusões (`ACAO='EX'`) (RELAUDIT) | Trilha de auditoria incompleta — risco de compliance |
| MYS-005 | Mascaramento de CPF defeituoso, autoadmitido (CONSBENF) | Exposição de dado pessoal (LGPD) |
| MYS-006 | Desconto calculado de 3 formas (3% inline vs motor CALCDSCT 30%+tipos) | Valor de desconto inconsistente entre fluxos |

### 4.2 Riscos para o Estágio 2

> O que o time de especificação precisa saber antes de começar:

1. **Modelo de estados do pagamento não é confiável** — `'P'`/`'G'` divergem entre código e docs (MYS-001, MYS-013). Definir a máquina de estados antes de qualquer EARS de pagamento.
2. **Lógica financeira duplicada e divergente** — BATCHPGT reimplementa CALCBENF inline (MYS-007) e há 3 cálculos de desconto (MYS-006). Escolher a regra canônica.
3. **Validação de CPF triplicada com bypasses** — CADBENEF/VALBENEF/VALDOCS (MYS-003, MYS-009). Unificar em um único serviço.
4. **Constantes mágicas sem origem** — FATOR-K 0,347215, abono 15%, tolerância R$0,01, faixas 600/60/16/65 (MYS-002, MYS-015, MYS-023, MYS-027). Precisam de confirmação do facilitador/PO.
5. **Tabela IPCA incompleta** (só 2010–2012) → correção silenciosamente zero para anos recentes (MYS-008).

---

## 5. Recomendações

### 5.1 O que migrar primeiro

> Com base na priorização do Par 1 (Product Owner), quais funcionalidades devem ser migradas primeiro?

| Prioridade | Funcionalidade | Justificativa |
| ---------- | -------------- | ------------- |
| 1          |                |               |
| 2          |                |               |
| 3          |                |               |

### 5.2 O que descartar

> Funcionalidades que provavelmente não precisam ser migradas:

- [Funcionalidade]: [Motivo para descartar]

### 5.3 O que evoluir

> Funcionalidades que devem ser migradas E melhoradas:

- [Funcionalidade]: [Como melhorar]

---

## 6. Métricas do Estágio

| Métrica                       | Valor        |
| ----------------------------- | ------------ |
| Programas analisados          | 15 / 15      |
| DDMs mapeados                 | 4 / 4        |
| Regras de negócio encontradas | ~100 (20 consolidadas BR-001..BR-020) |
| Regras escondidas encontradas | 10+ / 10     |
| Easter eggs encontrados       | 2 / 3 (Banco Real, Plano Verão) |
| Termos no glossário           | a preencher (glossary.md) |
| Mistérios catalogados         | 34 (6 bloqueadores) |
| Tempo total gasto             | \_\_\_ horas |

---

## 7. Notas para o Próximo Estágio

> Mensagens para o time no Estágio 2 (Especificação Moderna):

### Resumo Executivo (≤5 frases)

1. O legado SIFAP tem **15 programas Natural** (~2.800 linhas) e **4 DDMs** Adabas (BENEFICIARIO, PAGAMENTO, PROGRAMA-SOCIAL, AUDITORIA), todos lidos.
2. Foram extraídas **~100 regras de negócio** (20 consolidadas como BR-001..BR-020), das quais **~10 confirmadas** por documentação e ~25 marcadas como mistério.
3. O sistema é **fracamente acoplado por chamada** (zero CALLNAT/INCLUDE) e **fortemente acoplado por dados**: os 15 programas se comunicam só via DDMs (44 arestas programa→dados).
4. O maior risco é o **modelo de estados do pagamento ambíguo** (MYS-001: `'P'` pendente vs pago), seguido do FATOR-K e dos bypasses de validação/auditoria.
5. Confiança para modernizar: **Média** — o domínio está bem mapeado, mas 6 bloqueadores precisam de decisão do PO/facilitador antes das EARS.

### O Que Sabemos — Confirmado (cita fonte)

- **Teto de descontos 30%, judicial sem teto** — [business-rules-catalog.md BR-001](business-rules-catalog.md) (`CALCDSCT.NSN#L101-L169`). EARS: *Unwanted*.
- **CPF validado por módulo 11** — [BR-002](business-rules-catalog.md) (`CADBENEF.NSN#L224-L269`). EARS: *Unwanted*.
- **Contribuição social progressiva por faixa** — [BR-003](business-rules-catalog.md) (`CALCDSCT.NSN#L57-L65`). EARS: *Ubiquitous*.
- **Cálculo do benefício (motor)** — [BR-004](business-rules-catalog.md) (`CALCBENF.NSN#L221-L233`). EARS: *Ubiquitous*.
- **Só beneficiário ativo gera pagamento** — [BR-005](business-rules-catalog.md) (`BATCHPGT.NSN#L195-L210`). EARS: *State-driven*.
- **Dependências:** 0 arestas programa→programa; 44 arestas programa→dados — [dependency-map.md](dependency-map.md). DDM mais acessado: `BENEFICIARIO` (9 programas).
- **DDMs:** 4 documentados ([inventory.md](inventory.md)); `PAGAMENTO` é o ponto de maior concorrência (8 programas escrevem/leem).

### O Que Traz Risco — Regras inferidas (evidência fraca)

A maioria das ~100 regras é **Inferida** (só código, sem doc): fatores familiar/idade/renda, idempotência por competência, faixas de elegibilidade. Tratá-las como hipóteses ao escrever EARS — confirmar com PO antes de fixar acceptance.

### Hipóteses de Recorte (bounded contexts) — para o @architect avaliar

> ⚠️ **São hipóteses, não decisões.** Derivadas dos clusters de acesso a DDM (não há call graph).

- **Hipótese 1 — Cadastro de Beneficiário** *(DDM BENEFICIARIO)*: `CADBENEF`, `CADDEPEND`, `VALBENEF`, `VALDOCS`, `CONSBENF` — entidade central, validações e consulta giram em torno de um único agregado.
- **Hipótese 2 — Pagamento & Ciclo** *(DDM PAGAMENTO)*: `BATCHPGT`, `CALCBENF`, `CALCDSCT`, `CALCCORR`, `RELPGT` — geração, cálculo, desconto e correção do pagamento; coração financeiro.
- **Hipótese 3 — Programa Social** *(DDM PROGRAMA-SOCIAL)*: `CADPROG`, `VALELEG` — parâmetros e elegibilidade; fronteira natural por possuir as regras de programa.
- **Hipótese 4 — Conciliação & Auditoria** *(DDM AUDITORIA + PAGAMENTO)*: `BATCHCON`, `RELAUDIT` — integração bancária (CNAB) e trilha de auditoria; toca PAGAMENTO mas tem ciclo de vida próprio.
- **Hipótese 5 — Relatórios Gerenciais** *(leitura cross-DDM)*: `BATCHREL` (+ `RELPGT`/`RELAUDIT` como leitores) — candidato a *read model*/BI separado, já que só consolida dados.

> **Tensão de fronteira:** `PAGAMENTO` é tocado por 8 programas de 3 hipóteses distintas — será o principal ponto de negociação de contexto no Estágio 2.

---

## Definição de Pronto deste relatório

- [ ] Todas as seções acima preenchidas (sem placeholders).
- [ ] Pelo menos 5 regras críticas listadas em §3.1, cada uma referenciando uma `BR-XXX` do catálogo.
- [ ] Decisões de migrar/descartar/evoluir em §5 cobrem as 8+ funcionalidades principais.
- [ ] Métricas de §6 conferem com os outros artefatos (glossary.md, business-rules-catalog.md, mysteries-found.md).

— Paula


---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="mysteries-found.md"><strong>mysteries-found.md</strong></a><br/>
<sub>Lista de mistérios.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../02-spec-moderna/GUIDE.md"><strong>Estágio 2 — Spec</strong></a><br/>
<sub>Próximo estágio: spec moderna.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>

