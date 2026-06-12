<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-0002: Comunicação inter-context (interfaces in-process + domain events)

- **Status:** Aceito
- **Data:** 2026-06-11
- **Decisores:** Equipe SIFAP 2.0 (Estágio 2)
- **Relacionado:** [`ADR-0001-modular-monolith.md`](ADR-0001-modular-monolith.md), [`../bounded-contexts.md`](../bounded-contexts.md)

## Contexto

Dentro do Modular Monolith (ADR-0001), os 4 contextos precisam se comunicar. O mapa de
contexto ([`../bounded-contexts.md`](../bounded-contexts.md)) identificou dois tipos de
interação:

1. **Leituras de dados de referência** — Payment consulta Beneficiary (status/dados) e
   Social Program (parâmetros); Social Program consulta Beneficiary para elegibilidade.
2. **Notificações de fato ocorrido** — auditoria de mutações e retorno bancário (a
   Conciliação é um *adapter* de entrada do Payment, não um contexto).

## Decisão

Adotamos um estilo **misto**:

- **Chamada de método in-process via interface** para leituras síncronas de dados de
  referência (troca de IDs/DTOs leves — Java `record`). Nunca via HTTP entre módulos.
- **Domain events in-process** para notificações assíncronas/desacopladas:
  `AuditEvent` (consumido por Audit Trail) e `BankReturnEvent` (consumido por Payment).

Nenhum contexto acessa o banco de dados ou as entidades de outro contexto diretamente —
somente através da interface pública exportada.

## Consequências

**Positivas**

- Leituras síncronas permanecem simples e fortemente tipadas.
- Auditoria e conciliação ficam desacopladas via eventos, evitando que a Conciliação
  escreva direto em `PAGAMENTO` (resolve a tensão de fronteira do DDM `PAGAMENTO`).

**Negativas / trade-offs**

- Dois mecanismos para manter (interfaces + barramento de eventos in-process).
- Eventos in-process exigem disciplina de idempotência no consumidor.

**Mitigações**

- Regra simples: *consulta de dados = interface; fato ocorrido = domain event*.
- DTOs de fronteira são `record` imutáveis no package `shared` ou no `api` do contexto
  produtor.
