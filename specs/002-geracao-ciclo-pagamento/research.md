<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Research — Geração do Ciclo de Pagamento Mensal

**Feature**: `002-geracao-ciclo-pagamento` | **Date**: 2026-06-10

> Fase 0 do `/speckit.plan`. Resolve as decisões abertas e mapeia cada requisito
> ao legado. Fonte: [business-rules-catalog.md](../../01-arqueologia/business-rules-catalog.md),
> [mysteries-found.md](../../01-arqueologia/mysteries-found.md).

## Mapeamento Requisito → Regra → Legado

| REQ-ID | BR | source_legacy | Decisão de design |
|--------|----|--------------|--------------------|
| REQ-PAY-001 | BR-005 | `BATCHPGT.NSN#L195-L210` | Seleção de elegíveis via `BeneficiaryReader` + checagem de idempotência |
| REQ-PAY-002 | BR-004 | `CALCBENF.NSN#L221-L233` | `BenefitCalculator` único (motor de domínio) |
| REQ-PAY-003 | BR-005 | `BATCHPGT.NSN#L195-L198` | Filtro por status `ACTIVE` |
| REQ-PAY-004 | BR-005 | `BATCHPGT.NSN#L220-L230` | Validação de programa ativo |
| REQ-PAY-005 | BR-005 | `BATCHPGT.NSN#L202-L210` | Constraint única `(cpf, competence)` |
| REQ-PAY-006 | BR-006 | `CALCBENF.NSN#L238-L260` | 13º/abono — **bloqueado por MYS-011** |
| REQ-PAY-007 | BR-004 | `CALCBENF.NSN#L266-L273` | Líquido nunca negativo; truncamento |
| REQ-PAY-008 | BR-004 | `CALCBENF.NSN#L141-L144` | Validação de competência |
| REQ-PAY-009 | BR-005 | `BATCHPGT.NSN#L350-L365` | Resumo do ciclo (totais) |
| REQ-PAY-010 | BR-005 | `[GREENFIELD]` | Estado inicial — **bloqueado por MYS-001** |

## Decisões

### D1 — Estado inicial do pagamento (resolve parcialmente MYS-001)

- **Decisão:** estado inicial = `GENERATED`. Não reusar o `'P'` ambíguo do legado.
- **Rationale:** o legado grava `'G'` (BATCHPGT) enquanto a doc usa `'P'`=pendente
  e a conciliação usa `'P'`=pago (RELPGT/BATCHCON). `GENERATED` é inequívoco.
- **Alternativas:** `PENDING` (rejeitada — colide com o uso pós-conciliação de
  `'P'`). A máquina de estados completa (GENERATED→PAID/RETURNED/REVERSED/CANCELLED)
  será fixada por **ADR** antes de implementar REQ-PAY-010.

### D2 — Motor de cálculo único (resolve MYS-007)

- **Decisão:** uma única classe `BenefitCalculator` no módulo `payment`; o ciclo
  (`PaymentCycleService`) a invoca. Não há lógica de cálculo duplicada.
- **Rationale:** o legado mantém o cálculo em dois lugares (CALCBENF e inline no
  BATCHPGT), com risco de divergência. O monolito modular permite uma fonte única.
- **Alternativas:** replicar inline (rejeitada — repete o anti-padrão do legado).

### D3 — Desconto fora de escopo (respeita MYS-006)

- **Decisão:** o ciclo grava `discount = 0` e `net = gross`. O cálculo de desconto
  (BR-001/BR-003, motor `CALCDSCT`) é feature separada que atualiza o pagamento depois.
- **Rationale:** evita reintroduzir o desconto inline de 3% (anti-padrão do legado);
  mantém a fronteira do contexto limpa.

### D4 — Aritmética monetária

- **Decisão:** `BigDecimal` com `RoundingMode.DOWN` (truncamento), escala 2.
- **Rationale:** BR-004 trunca (não arredonda); o relatório que arredonda (BR-019)
  é outro contexto. Usar `double` causaria divergência de centavos.

### D5 — Data de corte do status `ACTIVE`

- **Decisão (assunção):** último dia do mês anterior à competência.
- **Status:** a confirmar com PO; é a interpretação do ciclo mensal do legado.

### D6 — 13º e abono (não resolve MYS-011/015)

- **Decisão:** **adiar** a implementação de REQ-PAY-006 até confirmação do PO.
- **Rationale:** o comentário do `CALCBENF` descreve o 13º como
  `base × regional × (meses_ativos/12)`, mas o código calcula `base × regional × idade`;
  o abono de 15% não tem base documental. Implementar agora arriscaria valor errado.

## Best Practices aplicadas

- **Spring Data JPA**: repositório com derivação de query + constraint única no schema (idempotência no banco, não só no código).
- **Testcontainers**: integração contra PostgreSQL real (não H2), para validar a constraint e o truncamento.
- **Teste de equivalência**: `BenefitCalculatorTest` compara resultados com casos derivados do `CALCBENF` (Princípio II da constituição).
- **Modular Monolith**: dependências para outros contextos só via interfaces `spi/` (Beneficiary/SocialProgram), sem acoplamento de implementação.

## NEEDS CLARIFICATION resolvidos

Nenhum marcador `NEEDS CLARIFICATION` permanece. As duas pendências (estado inicial,
13º/abono) estão **isoladas em requisitos específicos** (REQ-PAY-010, REQ-PAY-006)
e não bloqueiam o design da estrutura nem o fluxo principal (REQ-PAY-001..005, 007..009).
