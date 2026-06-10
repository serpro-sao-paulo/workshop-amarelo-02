<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Feature Specification: Geração do Ciclo de Pagamento Mensal

**Feature Branch**: `002-geracao-ciclo-pagamento`

**Created**: 2026-06-10

**Status**: Draft

**Input**: User description: "Geração do ciclo de pagamento mensal" — contexto Payment & Cycle do SIFAP 2.0, derivado do legado `BATCHPGT.NSN`/`CALCBENF.NSN`.

> **Rastreabilidade (Constituição, Princípio I):** esta spec deriva de regras
> confirmadas/inferidas no Estágio 1. Cada requisito carrega `source_legacy:`
> apontando para `01-arqueologia/legado-sifap/` ou `[GREENFIELD] + justificativa`.
> Artefatos-fonte: [business-rules-catalog.md](../../01-arqueologia/business-rules-catalog.md) (BR-004, BR-005, BR-006),
> [bounded-contexts.md](../../02-spec-moderna/bounded-contexts.md) (contexto Payment & Cycle),
> [mysteries-found.md](../../01-arqueologia/mysteries-found.md) (MYS-001, MYS-006, MYS-007, MYS-011).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Gerar a folha mensal de pagamentos (Priority: P1)

Como operador de pagamentos, ao iniciar o ciclo de uma competência (mês/ano),
o sistema gera automaticamente um registro de pagamento para cada beneficiário
elegível, com o valor do benefício calculado, para que a folha possa ser enviada
ao banco.

**Why this priority**: É o coração financeiro do SIFAP — sem a geração do ciclo,
nenhum beneficiário recebe. Concentra as regras CRÍTICAS de cálculo (BR-004) e de
elegibilidade do ciclo (BR-005).

**Independent Test**: Pode ser testado de ponta a ponta executando o ciclo para
uma competência sobre uma base de teste de beneficiários e verificando a contagem
e os valores dos pagamentos gerados — entrega valor mesmo sem as demais histórias.

**Acceptance Scenarios**:

1. **Given** 100 beneficiários com status `ACTIVE` e 30 com status diferente de `ACTIVE` na data de corte, **When** o ciclo da competência é gerado, **Then** o sistema cria exatamente 100 registros de pagamento.
2. **Given** um beneficiário `ACTIVE` cujo programa social está ativo, **When** o ciclo é gerado, **Then** o valor bruto do pagamento é igual ao cálculo do benefício (base × fator regional × fator familiar × fator renda × fator idade × (1 + reajuste), truncado em 2 casas).
3. **Given** um beneficiário que já possui pagamento na competência corrente, **When** o ciclo é executado novamente para a mesma competência, **Then** nenhum pagamento duplicado é criado para ele.

---

### User Story 2 - Não pagar quem é inelegível no ciclo (Priority: P2)

Como gestor, quero que o ciclo ignore beneficiários inelegíveis (inativos, sem
programa válido, ou com programa inativo), para que a folha não gere pagamentos
indevidos.

**Why this priority**: Previne prejuízo financeiro e pagamentos indevidos;
complementa a geração principal mas pode ser entregue/testada em separado.

**Independent Test**: Submeter uma base com beneficiários em vários status e
programas e verificar que apenas os elegíveis recebem pagamento, com os demais
contabilizados como "ignorados".

**Acceptance Scenarios**:

1. **Given** um beneficiário com status diferente de `ACTIVE`, **When** o ciclo é gerado, **Then** ele é contabilizado como ignorado e não recebe pagamento.
2. **Given** um beneficiário cujo programa social não existe ou está inativo, **When** o ciclo é gerado, **Then** ele é contabilizado como erro/ignorado e não recebe pagamento, e o motivo é registrado.

---

### User Story 3 - Aplicar 13º e abono em dezembro (Priority: P3)

Como beneficiário, na competência de dezembro quero receber o 13º (e o abono,
quando aplicável ao tipo do meu programa), para cumprir a regra histórica de
benefício natalino.

**Why this priority**: Regra sazonal de alto valor para o beneficiário, mas só se
aplica a uma competência por ano; pode ser entregue após o fluxo base.

**Independent Test**: Gerar o ciclo para uma competência de dezembro e verificar
que o tipo do pagamento e os valores adicionais (13º e abono) são aplicados
conforme o tipo do programa.

**Acceptance Scenarios**:

1. **Given** a competência é dezembro e o beneficiário é elegível, **When** o ciclo é gerado, **Then** o pagamento é marcado com o tipo correspondente ao décimo e o valor do 13º é somado ao bruto.
2. **Given** a competência é dezembro e o programa do beneficiário é do tipo assistencial, **When** o ciclo é gerado, **Then** o abono natalino também é somado ao bruto.

### Edge Cases

- O que acontece quando a base não tem nenhum beneficiário elegível? (Esperado: ciclo conclui com 0 pagamentos gerados e um resumo consistente.)
- Como o sistema trata um beneficiário elegível cujo valor líquido resultaria negativo? (Esperado: líquido é zerado, nunca negativo.)
- O que acontece se o ciclo for reexecutado após falha parcial? (Esperado: idempotência por competência — não duplica pagamentos já gerados.)
- Como o sistema trata uma competência inválida (mês fora de 1–12)? (Esperado: rejeição com mensagem clara, sem gerar pagamentos.)

## Requirements *(mandatory)*

### Functional Requirements

- **REQ-PAY-001** *(event-driven)*: Quando o ciclo de pagamento de uma competência é iniciado, o sistema MUST criar um registro de pagamento para cada beneficiário com status `ACTIVE` na data de corte que ainda não possua pagamento naquela competência.
  - `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L195-L210`
  - `business_rule: BR-005`
- **REQ-PAY-002** *(ubiquitous)*: O sistema MUST calcular o valor bruto do benefício como `base × fator_regional × fator_familiar × fator_renda × fator_idade × (1 + fator_reajuste)`, truncado em 2 casas decimais.
  - `source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L221-L233`
  - `business_rule: BR-004`
- **REQ-PAY-003** *(state-driven)*: Enquanto um beneficiário não estiver com status `ACTIVE`, o sistema MUST NOT gerar pagamento para ele, contabilizando-o como ignorado.
  - `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L195-L198`
  - `business_rule: BR-005`
- **REQ-PAY-004** *(unwanted)*: Se o programa social do beneficiário não existir ou não estiver ativo, o sistema MUST NOT gerar pagamento, registrar o motivo e contabilizar o caso.
  - `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L220-L230`
  - `business_rule: BR-005`
- **REQ-PAY-005** *(unwanted)*: Se já existir pagamento do beneficiário na competência corrente, o sistema MUST NOT gerar um pagamento duplicado (idempotência do ciclo).
  - `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L202-L210`
  - `business_rule: BR-005`
- **REQ-PAY-006** *(event-driven)*: Quando a competência for dezembro, o sistema MUST marcar o pagamento como tipo "décimo" e somar o 13º ao valor bruto; e, quando o programa for do tipo assistencial, MUST somar o abono natalino.
  - `source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L238-L260`
  - `business_rule: BR-006`
  - ⚠️ Ver MYS-011/MYS-015: a fórmula do 13º e o percentual do abono precisam de confirmação do PO antes da implementação.
- **REQ-PAY-007** *(unwanted)*: Se o valor líquido calculado for negativo, o sistema MUST zerá-lo; o valor líquido MUST ser truncado em 2 casas decimais.
  - `source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L266-L273`
  - `business_rule: BR-004`
- **REQ-PAY-008** *(unwanted)*: Se a competência informada tiver mês fora do intervalo 1–12, o sistema MUST rejeitar a geração do ciclo sem criar pagamentos.
  - `source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L141-L144`
  - `business_rule: BR-004`
- **REQ-PAY-009** *(ubiquitous)*: Ao final do ciclo, o sistema MUST produzir um resumo com totais de processados, gerados, ignorados e erros, e o somatório dos valores bruto, desconto e líquido.
  - `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L350-L365`
  - `business_rule: BR-005`
- **REQ-PAY-010** *(ubiquitous)*: O estado inicial de um pagamento recém-gerado MUST ser um valor único e bem definido da máquina de estados do pagamento.
  - `source_legacy: [GREENFIELD] — o legado grava 'G' (BATCHPGT) enquanto a doc usa 'P'=pendente; MYS-001 exige decisão de modelo de estados. Greenfield até a decisão do PO/ADR.`
  - `business_rule: BR-005`

### Key Entities *(include if feature involves data)*

- **Payment Cycle (Ciclo de Pagamento)**: Execução da geração de pagamentos para uma competência (`AAAAMM`). Atributos-chave: competência, data de execução, totais (processados, gerados, ignorados, erros) e somatórios financeiros.
- **Payment (Pagamento)**: Registro financeiro gerado por beneficiário por competência. Atributos: beneficiário (CPF), programa, competência, valor bruto, desconto, líquido, abono, tipo (normal/décimo) e estado.
- **Beneficiary (Beneficiário)**: Sujeito do pagamento; possui status (`ACTIVE`/outros), região, número de dependentes, renda familiar e programa. *Possuído pelo contexto Beneficiary Management; consultado em modo leitura.*
- **Social Program (Programa Social)**: Define valor-base, fator de reajuste, tipo e parâmetros. *Possuído pelo contexto Social Program; consultado em modo leitura.*

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% dos beneficiários elegíveis (status `ACTIVE`, programa ativo, sem pagamento na competência) recebem exatamente um pagamento gerado por ciclo.
- **SC-002**: 0 pagamentos duplicados ao reexecutar o ciclo da mesma competência (idempotência comprovada por reexecução).
- **SC-003**: O valor bruto gerado é idêntico ao do cálculo de referência do legado em pelo menos 99,9% dos casos de uma base de equivalência (teste de equivalência ao `CALCBENF`).
- **SC-004**: O resumo do ciclo reconcilia: `processados = gerados + ignorados + erros` em 100% das execuções.
- **SC-005**: Um ciclo sobre a base de teto operacional conclui dentro da janela definida pelo negócio sem perda de registros.

## Assumptions

- A "data de corte" para o status `ACTIVE` é o último dia do mês anterior à competência (padrão do legado para o ciclo mensal); a confirmar com o PO.
- O cálculo do valor do benefício é **único** (uma única regra canônica reutilizada), eliminando a duplicação `BATCHPGT`↔`CALCBENF` do legado (MYS-007). O contexto Payment & Cycle possui esse motor.
- O cálculo de desconto **não** faz parte desta feature: é responsabilidade do motor de descontos (BR-001/BR-003, `CALCDSCT`), consumido por este ciclo. Esta spec assume desconto = 0 até a feature de descontos estar disponível, e NÃO replica o desconto inline de 3% do legado (MYS-006).
- O modelo de estados do pagamento (estado inicial) será fixado por ADR antes da implementação (MYS-001) — por isso REQ-PAY-010 é greenfield.
- Beneficiário e Programa Social são consultados via interface de leitura dos seus contextos (Modular Monolith, comunicação in-process), conforme [bounded-contexts.md](../../02-spec-moderna/bounded-contexts.md).
- O envio ao banco (arquivo CNAB) e a conciliação estão **fora de escopo** desta feature.
