<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Data Model — Geração do Ciclo de Pagamento Mensal

**Feature**: `002-geracao-ciclo-pagamento` | **Date**: 2026-06-10

> Fase 1 do `/speckit.plan`. Entidades possuídas pelo contexto **Payment & Cycle**
> e modelos de leitura dos contextos vizinhos. Mapeamento a partir do DDM
> `PAGAMENTO` (ver [dependency-map.md](../../01-arqueologia/dependency-map.md)).

## Entidade: Payment (tabela `payment`) — **possuída por este contexto**

| Campo | Tipo | Restrições | Origem legado |
|-------|------|------------|---------------|
| `id` | UUID | PK | (greenfield — substitui `NUM-PAGTO` sequencial) |
| `beneficiary_cpf` | VARCHAR(11) | NOT NULL | `PAGAMENTO.CPF-BENEF` |
| `program_code` | INTEGER | NOT NULL | `PAGAMENTO.COD-PROGRAMA` |
| `competence` | CHAR(6) | NOT NULL, formato `AAAAMM` | `PAGAMENTO.COMPETENCIA` |
| `gross_amount` | NUMERIC(11,2) | NOT NULL, ≥ 0 | `PAGAMENTO.VLR-BRUTO` |
| `discount_amount` | NUMERIC(11,2) | NOT NULL, default 0 | `PAGAMENTO.VLR-DESCONTO` |
| `net_amount` | NUMERIC(11,2) | NOT NULL, ≥ 0 | `PAGAMENTO.VLR-LIQUIDO` |
| `bonus_amount` | NUMERIC(11,2) | NOT NULL, default 0 | `PAGAMENTO.VLR-ABONO` |
| `generation_date` | DATE | NOT NULL | `PAGAMENTO.DT-GERACAO` |
| `status` | VARCHAR(12) | NOT NULL | `PAGAMENTO.STATUS-PGTO` (era `'G'`) |
| `type` | VARCHAR(8) | NOT NULL | `PAGAMENTO.TIPO-PGTO` (`N`/`D`) |

**Constraints:**

- `UNIQUE (beneficiary_cpf, competence)` — garante idempotência do ciclo (REQ-PAY-005).
- `CHECK (net_amount >= 0)` — líquido nunca negativo (REQ-PAY-007).
- `CHECK (gross_amount >= 0)`.

**Índices:**

- `idx_payment_competence` em `(competence)` — leitura por ciclo (REQ-PAY-009, relatórios).
- (a constraint única já cria índice em `(beneficiary_cpf, competence)`).

### Enum: PaymentStatus

`GENERATED` (estado inicial — D1/research). Demais estados
(`PAID`, `RETURNED`, `REVERSED`, `CANCELLED`) definidos por ADR futuro (MYS-001) —
fora do escopo desta feature.

### Enum: PaymentType

`NORMAL` (`N`), `DECIMO` (`D`). `TERCEIRO` (`T`) do legado é mistério sem produtor
(MYS-025) — **não** modelado aqui.

## Modelo de leitura: BeneficiarySnapshot — *lido de Beneficiary Management*

> Não é tabela deste contexto. Obtido via `BeneficiaryReader` (interface in-process).

| Campo | Tipo | Uso |
|-------|------|-----|
| `cpf` | String | identificação |
| `status` | enum | elegibilidade (`ACTIVE`?) — REQ-PAY-003 |
| `regionCode` | int | fator regional — REQ-PAY-002 |
| `dependentsCount` | int | fator familiar — REQ-PAY-002 |
| `familyIncome` | BigDecimal | fator renda — REQ-PAY-002 |
| `birthDate` | LocalDate | fator idade — REQ-PAY-002 |
| `programCode` | int | vínculo ao programa |

## Modelo de leitura: ProgramParameters — *lido de Social Program*

> Obtido via `SocialProgramReader` (interface in-process).

| Campo | Tipo | Uso |
|-------|------|-----|
| `code` | int | identificação |
| `active` | boolean | elegibilidade — REQ-PAY-004 |
| `baseValue` | BigDecimal | base do cálculo — REQ-PAY-002 |
| `adjustmentFactor` | BigDecimal | reajuste — REQ-PAY-002 |
| `type` | enum (`A`/`P`/`T`) | abono de dezembro — REQ-PAY-006 |

## Entidade efêmera: PaymentCycleSummary (não persistida nesta feature)

Resultado da execução do ciclo (REQ-PAY-009): `competence`, `processed`,
`generated`, `ignored`, `errors`, `totalGross`, `totalDiscount`, `totalNet`.
Invariante: `processed = generated + ignored + errors` (SC-004).

## Transições de estado (escopo desta feature)

```text
(não existe) --REQ-PAY-001--> GENERATED
```

> A partir de `GENERATED`, as transições (→ PAID/RETURNED/...) pertencem à feature
> de conciliação e ao ADR de máquina de estados (MYS-001).
