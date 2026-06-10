<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Tasks: Geração do Ciclo de Pagamento Mensal

**Feature**: `002-geracao-ciclo-pagamento` | **Branch**: `002-geracao-ciclo-pagamento`
**Input**: [spec.md](spec.md) · [plan.md](plan.md) · [research.md](research.md) · [data-model.md](data-model.md) · [contracts/payment-cycle-api.md](contracts/payment-cycle-api.md) · [quickstart.md](quickstart.md)

> **Testes obrigatórios** por força da Constituição (Princípio II — Test-First Quality),
> incluindo testes de equivalência ao `CALCBENF`. Estão incluídos em cada user story.
> Módulo-alvo: `backend/.../payment` (Modular Monolith).

## Phase 1: Setup

- [ ] T001 Criar o módulo `payment` no backend (estrutura de pacotes `domain/`, `application/`, `api/`, `repository/`, `spi/`) em `backend/src/main/java/br/gov/sifap/payment/`
- [ ] T002 [P] Configurar dependências Spring Boot 3.3 (Web, Validation, Data JPA, Flyway) e Testcontainers no `backend/pom.xml`
- [ ] T003 [P] Configurar datasource PostgreSQL 16 e Flyway em `backend/src/main/resources/application.yml`

## Phase 2: Foundational (blocking prerequisites)

- [ ] T004 Criar migration Flyway da tabela `payment` com constraint `UNIQUE(beneficiary_cpf, competence)`, `CHECK(net_amount >= 0)`, `CHECK(gross_amount >= 0)` e índice `idx_payment_competence` em `backend/src/main/resources/db/migration/V2__create_payment.sql` (data-model.md)
- [ ] T005 [P] Criar enums `PaymentStatus` (valor inicial `GENERATED`) em `backend/src/main/java/br/gov/sifap/payment/domain/PaymentStatus.java` e `PaymentType` (`NORMAL`, `DECIMO`) em `.../domain/PaymentType.java` (research D1; data-model.md)
- [ ] T006 Criar entidade JPA `Payment` em `backend/src/main/java/br/gov/sifap/payment/domain/Payment.java` (campos/tipos de data-model.md; valores monetários `BigDecimal` escala 2)
- [ ] T007 [P] Criar `PaymentRepository` com método `existsByBeneficiaryCpfAndCompetence` em `backend/src/main/java/br/gov/sifap/payment/repository/PaymentRepository.java`
- [ ] T008 [P] Definir interfaces SPI `BeneficiaryReader` (`findActiveForCompetence`) e `SocialProgramReader` (`findActiveByCode`) + DTOs de leitura `BeneficiarySnapshot`/`ProgramParameters` em `backend/src/main/java/br/gov/sifap/payment/spi/` (contracts §2)

**Checkpoint**: schema + entidade + portas prontos — user stories podem começar.

## Phase 3: User Story 1 — Gerar a folha mensal (Priority: P1) 🎯 MVP

**Goal**: Gerar um pagamento por beneficiário elegível com valor calculado pelo motor único.
**Independent Test**: rodar o ciclo sobre 100 `ACTIVE` + 30 não-`ACTIVE` e verificar 100 pagamentos gerados com `gross_amount` correto.

- [ ] T009 [P] [US1] Teste de equivalência `BenefitCalculatorTest` (casos derivados do `CALCBENF`; truncamento 2 casas) em `backend/src/test/java/br/gov/sifap/payment/BenefitCalculatorTest.java` (REQ-PAY-002 / BR-004)
- [ ] T010 [P] [US1] Teste de integração `PaymentCycleServiceIT` cenário "100 de 130 gerados" com Testcontainers em `backend/src/test/java/br/gov/sifap/payment/PaymentCycleServiceIT.java` (REQ-PAY-001/003)
- [ ] T011 [US1] Implementar `BenefitCalculator` (motor único: base × regional × familiar × renda × idade × (1+reajuste), `BigDecimal RoundingMode.DOWN`) em `backend/src/main/java/br/gov/sifap/payment/domain/BenefitCalculator.java` (REQ-PAY-002; research D2/D4)
- [ ] T012 [US1] Implementar `PaymentCycleService.generate(competence)` (`@Transactional`): selecionar elegíveis via `BeneficiaryReader`, calcular, persistir `Payment` com status `GENERATED` em `backend/src/main/java/br/gov/sifap/payment/application/PaymentCycleService.java` (REQ-PAY-001/003/010)
- [ ] T013 [US1] Implementar `PaymentCycleController` `POST /api/v1/payment-cycles` com `@Valid` + DTOs record (`CycleRequest`, `CycleSummary`) e annotations OpenAPI em `backend/src/main/java/br/gov/sifap/payment/api/` (contracts §1; REQ-PAY-009)
- [ ] T014 [US1] Validação de competência (formato `AAAAMM`, mês 1–12) → `400` em `CycleRequest`/controller (REQ-PAY-008)

**Checkpoint**: MVP entregável — gera a folha e retorna o resumo.

## Phase 4: User Story 2 — Ignorar inelegíveis (Priority: P2)

**Goal**: Não gerar pagamento para inativos, programa inexistente/inativo, ou já pagos na competência.
**Independent Test**: base mista → apenas elegíveis recebem; demais em `ignored`/`errors`.

- [ ] T015 [P] [US2] Teste de integração: programa inexistente/inativo → ignorado/erro (REQ-PAY-004) em `PaymentCycleServiceIT`
- [ ] T016 [P] [US2] Teste de integração: idempotência — reexecutar a competência não duplica (REQ-PAY-005) em `PaymentCycleServiceIT`
- [ ] T017 [US2] No `PaymentCycleService`, integrar `SocialProgramReader.findActiveByCode` e pular quando vazio, contabilizando erro/ignorado (REQ-PAY-004)
- [ ] T018 [US2] No `PaymentCycleService`, aplicar idempotência via `existsByBeneficiaryCpfAndCompetence` (e tratar violação de constraint → `409`) (REQ-PAY-005)
- [ ] T019 [US2] Garantir líquido nunca negativo e truncado em 2 casas no fluxo de geração (REQ-PAY-007)

**Checkpoint**: ciclo robusto contra inelegíveis e reexecução.

## Phase 5: User Story 3 — 13º e abono de dezembro (Priority: P3) ⚠️ BLOQUEADA

**Goal**: Em dezembro, marcar tipo `DECIMO`, somar 13º e abono (programa tipo `A`).
**Independent Test**: ciclo de dezembro → 13º/abono aplicados conforme o tipo do programa.

> ⚠️ **Bloqueada até ADR/PO** (MYS-011/015): a fórmula do 13º (comentário ≠ código)
> e o abono de 15% não têm base documental. Não implementar antes do esclarecimento.

- [ ] T020 [US3] (BLOQUEADA) Registrar ADR/decisão do PO sobre a fórmula do 13º e o abono em `02-spec-moderna/` antes de codar (research D6)
- [ ] T021 [P] [US3] (BLOQUEADA) Teste: ciclo de dezembro marca `DECIMO` e soma 13º/abono (REQ-PAY-006) em `PaymentCycleServiceIT`
- [ ] T022 [US3] (BLOQUEADA) Implementar 13º + abono no fluxo de dezembro do `PaymentCycleService`/`BenefitCalculator` (REQ-PAY-006)

## Phase 6: Polish & Cross-Cutting

- [ ] T023 [P] Mascarar CPF em todos os logs do módulo `payment` (Constituição IV; evita repetir o defeito do CONSBENF) em `application/`/`api/`
- [ ] T024 [P] Garantir resumo reconciliando `processed = generated + ignored + errors` com teste dedicado (SC-004 / REQ-PAY-009)
- [ ] T025 [P] Validar os cenários do [quickstart.md](quickstart.md) (1–5) ponta a ponta
- [ ] T026 [P] Adicionar comentários `// REQ-PAY-00X` inline nos testes para rastreabilidade (Constituição I)

## Dependencies & Execution Order

- **Setup (T001–T003)** → **Foundational (T004–T008)** → user stories.
- **US1 (P1)** depende de Foundational. É o **MVP**.
- **US2 (P2)** depende de US1 (estende o `PaymentCycleService`).
- **US3 (P3)** depende de US1 **e** do desbloqueio (ADR/PO) — não iniciar antes.
- **Polish** depende das stories que toca.

## Parallel Opportunities

- Setup: T002, T003 em paralelo.
- Foundational: T005, T007, T008 em paralelo (T006 depende de T005).
- US1: T009 e T010 (testes) em paralelo antes de T011–T014.
- US2: T015 e T016 em paralelo antes de T017–T019.
- Polish: T023, T024, T025, T026 em paralelo.

## Implementation Strategy

- **MVP = User Story 1** (T001–T014): entrega a geração da folha com valor correto.
- Incremento 1: + US2 (robustez contra inelegíveis/idempotência).
- Incremento 2: + US3 **após** resolver MYS-011/015.
- Test-first em cada story (testes antes da implementação), conforme Constituição II.
