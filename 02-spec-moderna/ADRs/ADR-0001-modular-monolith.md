<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-0001: Modular Monolith em vez de Microservices

- **Status:** Aceito
- **Data:** 2026-06-11
- **Decisores:** Equipe SIFAP 2.0 (Estágio 2)
- **Relacionado:** [`../bounded-contexts.md`](../bounded-contexts.md), [`../SPECIFICATION.md`](../SPECIFICATION.md)

## Contexto

O legado SIFAP é composto por 15 programas Natural **sem nenhum `CALLNAT`/`INCLUDE`**: o
acoplamento entre programas é exclusivamente por **DDM compartilhado** (44 arestas
programa→dados, 0 arestas programa→programa — ver
[`../../01-arqueologia/dependency-map.md`](../../01-arqueologia/dependency-map.md)). O
recorte de domínio produziu **4 bounded contexts** (Beneficiary Management, Payment &
Cycle, Social Program, Audit Trail), com o DDM `PAGAMENTO` como ponto de maior
concorrência (8 programas).

A equipe é pequena, há forte consistência transacional entre contextos (o ciclo de
pagamento lê beneficiário e programa na mesma transação) e não há requisito não
funcional declarado de escala independente por contexto.

## Decisão

O sistema moderno será um **único Modular Monolith implantável** (Spring Boot 3.3),
com **um package Java top-level por bounded context** e fronteiras de módulo
explícitas. Não adotaremos microservices nesta fase.

## Consequências

**Positivas**

- Consistência transacional simples entre contextos que hoje compartilham `PAGAMENTO`.
- Refatoração das fronteiras é barata enquanto o domínio ainda está sendo validado
  (6 bloqueadores em aberto — MYS-001..006).
- Deploy e operação únicos; menor custo de infraestrutura.

**Negativas / trade-offs**

- Sem escala independente por contexto; mitigado pela ausência de requisito de escala.
- Disciplina de fronteira precisa ser imposta por convenção/arquitetura (somente
  `api/` e interfaces exportadas são públicas), não pela rede.

**Mitigações**

- Cada contexto expõe apenas uma interface pública (ver ADR-0002); acesso a dados de
  outro contexto é proibido fora dessa interface.
- A estrutura de packages mapeia 1:1 para os contextos, permitindo extração futura
  para serviços caso surja necessidade de escala.
