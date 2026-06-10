<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Quickstart — Validação do Ciclo de Pagamento Mensal

**Feature**: `002-geracao-ciclo-pagamento` | **Date**: 2026-06-10

> Guia de validação executável (Fase 1). Prova que a feature funciona ponta a
> ponta. Detalhes de entidade/contrato estão em [data-model.md](data-model.md) e
> [contracts/payment-cycle-api.md](contracts/payment-cycle-api.md) — aqui só os
> cenários de validação.

## Pré-requisitos

- Docker em execução (PostgreSQL via Testcontainers / Compose).
- Backend do módulo `payment` buildado (Java 21, Spring Boot 3.3).
- Base de teste carregada: 100 beneficiários `ACTIVE` + 30 não-`ACTIVE`, com 2
  apontando para programa inexistente/inativo.

## Cenário 1 — Gerar a folha (REQ-PAY-001/002) · mapeia Acceptance US1.1/US1.2

```bash
curl -X POST http://localhost:8080/api/v1/payment-cycles \
  -H 'Content-Type: application/json' \
  -d '{"competence":"202606"}'
```

**Esperado:** `201 Created`; no corpo, `generated == 100` e `ignored + errors == 30`.
Cada pagamento gerado tem `gross_amount` igual ao do `BenefitCalculator`.

## Cenário 2 — Idempotência (REQ-PAY-005) · mapeia Acceptance US1.3

```bash
# Reexecuta a mesma competência
curl -X POST http://localhost:8080/api/v1/payment-cycles \
  -H 'Content-Type: application/json' \
  -d '{"competence":"202606"}'
```

**Esperado:** nenhum pagamento duplicado é criado (`generated == 0` na 2ª execução,
ou `409 Conflict`). A constraint `UNIQUE(beneficiary_cpf, competence)` garante isso.

## Cenário 3 — Inelegíveis ignorados (REQ-PAY-003/004) · mapeia Acceptance US2.1/US2.2

**Esperado:** beneficiários não-`ACTIVE` e os com programa inexistente/inativo
**não** recebem pagamento; aparecem em `ignored`/`errors` no `CycleSummary`.

## Cenário 4 — Competência inválida (REQ-PAY-008) · mapeia Edge Case

```bash
curl -X POST http://localhost:8080/api/v1/payment-cycles \
  -H 'Content-Type: application/json' \
  -d '{"competence":"202613"}'
```

**Esperado:** `400 Bad Request`; nenhum pagamento criado.

## Cenário 5 — Resumo reconcilia (REQ-PAY-009 / SC-004)

**Esperado:** no `CycleSummary`, `processed == generated + ignored + errors`.

## Cenário 6 — Dezembro / 13º (REQ-PAY-006) · ⚠️ bloqueado

> **Não executar até resolução.** A fórmula do 13º e o abono de 15% dependem de
> confirmação do PO (MYS-011/015). Documentado para completude; o teste só vale
> após o esclarecimento.

## Como rodar os testes automatizados

```bash
# Unit (motor de cálculo, equivalência ao CALCBENF) + Integração (Testcontainers)
cd backend && ./mvnw test
```

**Esperado:** verdes — `BenefitCalculatorTest`, `PaymentCycleServiceIT` (geração,
idempotência, inelegíveis, líquido não-negativo, reconciliação do resumo).
