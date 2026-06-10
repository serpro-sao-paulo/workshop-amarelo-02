<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Contracts — Geração do Ciclo de Pagamento Mensal

**Feature**: `002-geracao-ciclo-pagamento` | **Date**: 2026-06-10

> Fase 1 do `/speckit.plan`. Contrato REST público + interfaces de módulo
> (in-process) consumidas de outros bounded contexts. Modular Monolith: a
> comunicação entre contextos é por **interface Java**, não HTTP.

## 1. REST API (contexto Payment & Cycle)

### `POST /api/v1/payment-cycles`

Inicia a geração do ciclo de pagamento para uma competência (REQ-PAY-001).

**Request body** (`application/json`):

```json
{
  "competence": "202612"
}
```

| Campo | Tipo | Validação |
|-------|------|-----------|
| `competence` | string | obrigatório; `^\d{6}$`; mês ∈ 01–12 (REQ-PAY-008) |

**Responses:**

| Status | Quando | Corpo |
|--------|--------|-------|
| `201 Created` | ciclo gerado | `CycleSummary` |
| `400 Bad Request` | competência inválida (mês fora 1–12 / formato) | `ProblemDetail` (REQ-PAY-008) |
| `409 Conflict` | ciclo já totalmente gerado para a competência | `ProblemDetail` (idempotência, REQ-PAY-005) |

**`CycleSummary` (201):**

```json
{
  "competence": "202612",
  "processed": 130,
  "generated": 100,
  "ignored": 28,
  "errors": 2,
  "totalGross": "141200.00",
  "totalDiscount": "0.00",
  "totalNet": "141200.00"
}
```

Invariante de contrato: `processed == generated + ignored + errors` (SC-004).

> OpenAPI/Swagger annotations obrigatórias (constituição, Princípio V).

## 2. Interfaces de módulo (in-process SPI)

### `BeneficiaryReader` — implementada pelo contexto Beneficiary Management

```java
public interface BeneficiaryReader {
    /** Beneficiários elegíveis (status ACTIVE na data de corte) para o ciclo. */
    List<BeneficiarySnapshot> findActiveForCompetence(String competence);
}
```

- Troca apenas DTO de leitura (`BeneficiarySnapshot`); sem expor entidade JPA do outro contexto.
- Suporta REQ-PAY-001/003 (seleção de elegíveis por status).

### `SocialProgramReader` — implementada pelo contexto Social Program

```java
public interface SocialProgramReader {
    /** Parâmetros do programa, se existir e estiver ativo. */
    Optional<ProgramParameters> findActiveByCode(int programCode);
}
```

- Retorna `Optional.empty()` quando o programa não existe ou está inativo (REQ-PAY-004).
- `Optional` em vez de `null` (constituição, Princípio V).

## 3. Interface interna do motor de cálculo

### `BenefitCalculator` — domínio do contexto Payment & Cycle

```java
public interface BenefitCalculator {
    /** Valor bruto do benefício, truncado em 2 casas (BR-004 / REQ-PAY-002). */
    BigDecimal calculateGross(BeneficiarySnapshot beneficiary,
                              ProgramParameters program,
                              YearMonth competence);
}
```

- Fonte **única** da fórmula (D2/research) — resolve a duplicação MYS-007.
- Determinístico e puro (sem I/O) → alvo dos testes de equivalência ao `CALCBENF`.

## Contratos de teste (mínimos)

| Teste | Tipo | REQ coberto |
|-------|------|-------------|
| `BenefitCalculatorTest` | unit / equivalência | REQ-PAY-002 (BR-004) |
| `PaymentCycleServiceIT` (gera 100 de 130) | integração (Testcontainers) | REQ-PAY-001/003 |
| idempotência (reexecução não duplica) | integração | REQ-PAY-005 |
| programa inexistente/inativo → ignorado | integração | REQ-PAY-004 |
| competência inválida → 400 | contrato (web) | REQ-PAY-008 |
| líquido nunca negativo | unit | REQ-PAY-007 |
| resumo reconcilia (processed = gen+ign+err) | integração | REQ-PAY-009 / SC-004 |
