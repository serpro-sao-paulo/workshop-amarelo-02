<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Implementation Plan: Geração do Ciclo de Pagamento Mensal

**Branch**: `002-geracao-ciclo-pagamento` | **Date**: 2026-06-10 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/002-geracao-ciclo-pagamento/spec.md`

## Summary

Implementar a geração do ciclo de pagamento mensal do SIFAP 2.0: dada uma
competência (`AAAAMM`), criar um registro de pagamento por beneficiário elegível
(`ACTIVE`, programa ativo, sem pagamento na competência), com o valor do benefício
calculado por um **motor único** (eliminando a duplicação `BATCHPGT`<->`CALCBENF`
do legado). Abordagem: módulo `payment` de um Modular Monolith Spring Boot 3.3
(Java 21), com serviço de domínio `BenefitCalculator` (motor de cálculo), serviço
de aplicação `PaymentCycleService` (orquestra o ciclo, `@Transactional`),
persistência JPA/PostgreSQL e leitura dos contextos Beneficiary/Social Program via
interfaces in-process. Cálculo de **desconto fora de escopo**. Estado inicial do
pagamento e fórmula do 13o/abono pendentes de ADR (MYS-001, MYS-011).

## Technical Context

**Language/Version**: Java 21
**Primary Dependencies**: Spring Boot 3.3 (Web, Validation, Data JPA, Tx), Hibernate, Flyway
**Storage**: PostgreSQL 16
**Testing**: JUnit 5 + Testcontainers (PostgreSQL); testes de equivalencia ao `CALCBENF`
**Target Platform**: Linux server (container Docker)
**Project Type**: Web application (backend Java + frontend Next.js) -- esta feature e backend-only
**Performance Goals**: Processar a base de teste do workshop sem perda de registros (referencia do legado ~3,8M pagamentos/ciclo, fora do escopo de carga do workshop)
**Constraints**: Calculo monetario com truncamento em 2 casas (BigDecimal, RoundingMode.DOWN); idempotencia por competencia; sem `null` em metodos publicos
**Scale/Scope**: Backend; 1 endpoint de orquestracao + motor de calculo + 1 entidade nova (`payment`) e leitura de `beneficiary`/`social_program`

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio | Status | Evidencia / Como cumpre |
|-----------|--------|-------------------------|
| I. Spec-Driven & Legacy Traceability | PASS | Todos os REQ-PAY-001..010 tem `source_legacy:`; testes citarao REQ-IDs; mapeamento em research.md |
| II. Test-First Quality | PASS | Testes de equivalencia ao CALCBENF junto da implementacao; Testcontainers para integracao |
| III. Modular Monolith & Bounded Contexts | PASS | Feature no modulo `payment` (contexto Payment & Cycle); le Beneficiary/SocialProgram via interface, sem import cruzado |
| IV. Security by Default (OWASP) | PASS | Competencia validada com Bean Validation no controller; JPA/JPQL (sem SQL string); CPF mascarado em logs |
| V. Approved Toolchain & Layered Architecture | PASS | Java 21 (records p/ DTOs, Optional); @Transactional so no service; REST /api/v1 com OpenAPI |

Pendencias sinalizadas (nao violam o gate, mas bloqueiam requisitos especificos):
- MYS-001 (estado inicial do pagamento) -> ADR antes de implementar REQ-PAY-010.
- MYS-011/015 (formula do 13o/abono) -> confirmacao do PO antes de REQ-PAY-006.

Resultado: **PASS** (sem violacoes; Complexity Tracking nao aplicavel).

## Project Structure

### Documentation (this feature)

\`\`\`text
specs/002-geracao-ciclo-pagamento/
|-- plan.md              # Este arquivo
|-- research.md          # Fase 0 -- decisoes e mapeamento legado->regra
|-- data-model.md        # Fase 1 -- entidades, campos, transicoes
|-- quickstart.md        # Fase 1 -- guia de validacao executavel
|-- contracts/           # Fase 1 -- contrato do endpoint + interfaces de modulo
|   \`-- payment-cycle-api.md
\`-- checklists/
    \`-- requirements.md  # qualidade da spec (ja criado)
\`\`\`

### Source Code (repository root)

> A aplicacao ainda nao existe no repositorio (criada no Estagio 3). O layout-alvo
> abaixo e a referencia para a implementacao.

\`\`\`text
backend/
|-- src/main/java/br/gov/sifap/
|   |-- payment/                      # Bounded context Payment & Cycle
|   |   |-- domain/
|   |   |   |-- Payment.java          # entidade JPA
|   |   |   |-- PaymentStatus.java    # enum (estado inicial via ADR)
|   |   |   |-- PaymentType.java      # NORMAL, DECIMO
|   |   |   \`-- BenefitCalculator.java# motor unico de calculo (REQ-PAY-002)
|   |   |-- application/
|   |   |   \`-- PaymentCycleService.java  # orquestra o ciclo (@Transactional)
|   |   |-- api/
|   |   |   |-- PaymentCycleController.java
|   |   |   \`-- dto/                   # records (CycleRequest, CycleSummary)
|   |   |-- repository/
|   |   |   \`-- PaymentRepository.java
|   |   \`-- spi/                       # interfaces consumidas de outros contextos
|   |       |-- BeneficiaryReader.java
|   |       \`-- SocialProgramReader.java
|   \`-- ...
|-- src/main/resources/db/migration/
|   \`-- V2__create_payment.sql        # Flyway (modulo payment)
\`-- src/test/java/br/gov/sifap/payment/
    |-- BenefitCalculatorTest.java          # equivalencia ao CALCBENF (unit)
    \`-- PaymentCycleServiceIT.java          # integracao (Testcontainers)
\`\`\`

**Structure Decision**: Web application com backend Java. Feature backend-only no
modulo `payment` (package-by-feature). Beneficiary e Social Program sao lidos por
interfaces `spi/` (in-process), respeitando as fronteiras de contexto -- sem
import de classes internas de outros modulos.

## Complexity Tracking

> Nenhuma violacao da constituicao -- secao nao aplicavel.

---

## Phase 0 -- Outline & Research

Saida: [research.md](research.md). Resolve as decisoes abertas:

1. Estado inicial do pagamento (MYS-001): proposta `GENERATED`; maquina de estados
   completa fica para ADR. REQ-PAY-010 = greenfield ate o ADR.
2. Motor de calculo unico (MYS-007): `BenefitCalculator` e a unica fonte da formula
   (REQ-PAY-002 / BR-004); elimina a duplicacao inline do BATCHPGT.
3. Desconto fora de escopo (MYS-006): ciclo grava desconto = 0 ate o motor de
   descontos (CALCDSCT/BR-001); nao replicar o 3% inline.
4. Truncamento monetario: BigDecimal com RoundingMode.DOWN, 2 casas (BR-004).
5. Data de corte do status ACTIVE: ultimo dia do mes anterior (assumido; confirmar PO).
6. 13o/abono (MYS-011/015): formula do comentario != codigo; pendente de PO antes de REQ-PAY-006.

## Phase 1 -- Design & Contracts

Saidas: [data-model.md](data-model.md), [contracts/payment-cycle-api.md](contracts/payment-cycle-api.md), [quickstart.md](quickstart.md).

- data-model.md: entidade `Payment` (campos, tipos, indices), enums de status/tipo,
  modelos de leitura `BeneficiarySnapshot`/`ProgramParameters`.
- contracts/: endpoint `POST /api/v1/payment-cycles` + interfaces `BeneficiaryReader`/`SocialProgramReader`.
- quickstart.md: cenarios de validacao mapeados aos acceptance scenarios da spec.

Agent context update: atualizar o ponteiro entre `<!-- SPECKIT START/END -->` em
`.github/copilot-instructions.md` para este plano.

## Phase 2 -- Planning (proximo comando)

`/speckit.tasks` gera `tasks.md` (ordem de execucao, testes, dependencias).
