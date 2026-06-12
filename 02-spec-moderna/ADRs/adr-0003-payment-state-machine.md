<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-0003: Máquina de estados do pagamento (modelo do código legado)

## Status

Aceita

## Data

2026-06-11

## Contexto

O modelo de estados do pagamento é o principal bloqueador do Estágio 2 (MYS-001,
MYS-013, BR-020). O legado é ambíguo e conflitante:

- `BATCHPGT`/`CALCBENF` gravam `'G'` (gerado) — estado **não documentado**
  (`BATCHPGT.NSN#L332`, `CALCBENF.NSN#L283`).
- `BATCHCON` (conciliação) grava `'P'` = **pago** (retorno `00`), `'D'` = devolvido
  (`01`), `'E'` = erro (`02`) — `BATCHCON.NSN#L172-L194`.
- `RELPGT` traduz `G/P/C/D/E` com `'P'` = **PAGO** (`RELPGT.NSN#L128-L141`).
- Porém `MANUAL §3.5.1` e `REGRAS §5.1` afirmam `'P'` = **PENDENTE**.

Esta decisão precisa ser tomada agora porque restringe diretamente os requisitos de
geração de ciclo (REQ-009, REQ-010), dezembro/tipo de pagamento (REQ-014),
conciliação (REQ-016, REQ-017) e auditoria (REQ-018). Sem fixar a semântica dos
estados, nenhuma EARS de estado de pagamento é confiável.

## Opções Consideradas

### Opção 1: Adotar o modelo do código (`G → P/D/E/C`)

Usar os estados exatamente como gravados em produção hoje: `G` = gerado, `P` = pago,
`C` = (cancelado), `D` = devolvido, `E` = erro.

- **Prós:** Zero divergência com o que está em produção; a migração dos dados
  históricos de `STATUS-PGTO` é 1:1, sem reinterpretar o significado de nenhum
  registro existente; alinha com o comportamento real de `BATCHCON` e `RELPGT`.
- **Contras:** Perpetua a colisão da letra `'D'` (tipo de pagamento de dezembro em
  REQ-014 vs. status "devolvido" da conciliação) e mantém o conflito com a
  documentação (`'P'` pago vs. pendente); códigos de uma letra têm baixa clareza.
- **Risco:** Carrega para o sistema novo a ambiguidade documental que originou o
  bloqueador; mitigável separando claramente o **tipo de pagamento** (`N`/`D`) do
  **status do pagamento** (`G/P/D/E/C`) no modelo de domínio.
- **Esforço:** Lower.

### Opção 2: Adotar o modelo da documentação (`P` = pendente)

Seguir `MANUAL`/`REGRAS`: `'P'` = pendente; reinterpretar o estado que o código chama
de "pago".

- **Prós:** Alinha com a documentação oficial e com o entendimento de negócio
  registrado nos manuais.
- **Contras:** Contradiz os dados em produção — todo `'P'` histórico (que é "pago" no
  código) passaria a significar "pendente", corrompendo o significado dos registros
  existentes e exigindo reconciliação massiva.
- **Risco:** Alto — inverter a semântica de `'P'` pode marcar como pendentes
  pagamentos que já foram efetuados.
- **Esforço:** Higher (migração + reconciliação de dados).

### Opção 3: Novo modelo greenfield explícito

Enum de domínio com nomes claros e transições explícitas (ex.: `GENERATED →
CALCULATED → SENT_TO_BANK → PAID / RETURNED / ERROR / CANCELLED`), com tabela de
De-Para dos códigos legados feita uma vez na migração.

- **Prós:** Elimina a colisão `'D'` e o conflito `'P'`; máquina de estados verificável;
  De-Para documentado e auditável (apoia REQ-018/REQ-019).
- **Contras:** Exige acordar o mapeamento legado→novo com o PO antes de migrar; diverge
  dos códigos que a equipe operacional já conhece.
- **Risco:** Mapeamento legado mal definido migra estados errados; mitigável com tabela
  De-Para validada.
- **Esforço:** Same/higher que a Opção 1.

## Decisão

A equipe escolheu a **Opção 1 — adotar o modelo de estados do código legado
(`G/P/D/E/C`)**, porque mantém compatibilidade 1:1 com os dados em produção e evita o
risco de reinterpretar a semântica de pagamentos históricos durante a migração.

A ambiguidade da letra `'D'` será neutralizada no modelo de domínio separando o **tipo
de pagamento** (`N` normal, `D` dezembro/13º — REQ-014) do **status do pagamento**
(`G` gerado, `P` pago, `D` devolvido, `E` erro, `C` cancelado), em campos distintos.

## Consequências

### Positivas

- Migração de `STATUS-PGTO` é direta (1:1), sem transformação de significado.
- O comportamento de conciliação (REQ-016, REQ-017) e de relatório de pagamentos
  permanece consistente com o legado, reduzindo surpresas operacionais.
- Resolve o bloqueador MYS-001: a máquina de estados passa a ter definição única.

### Negativas

- Persiste o conflito com a documentação (`'P'` = pago no sistema vs. pendente nos
  manuais); a documentação precisará ser corrigida para refletir o sistema.
- Estados representados por código de uma letra exigem um enum/tradução na camada de
  API para legibilidade do frontend.
- A separação tipo × status precisa ser imposta no modelo para evitar a colisão `'D'`.

## Requisitos Relacionados

- REQ-009 — ciclo gera pagamento (estado inicial `G`)
- REQ-010 — idempotência por competência
- REQ-014 — tipo `D` em dezembro (distinto do status `D` = devolvido)
- REQ-016, REQ-017 — conciliação atualiza status (`P/D/E`)
- REQ-018 — auditoria das transições de status
