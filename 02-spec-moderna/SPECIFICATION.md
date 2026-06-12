<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# SPECIFICATION — Sistema Moderno SIFAP 2.0

> Gerado por `/write-ears-spec` a partir de
> [`../01-arqueologia/business-rules-catalog.md`](../01-arqueologia/business-rules-catalog.md)
> (BR-001..BR-020) e [`bounded-contexts.md`](bounded-contexts.md) (4 contextos aprovados),
> com Open Questions extraídas de
> [`../01-arqueologia/mysteries-found.md`](../01-arqueologia/mysteries-found.md).
>
> **Notação:** EARS (Easy Approach to Requirements Syntax). Todo requisito carrega
> `source_legacy:` apontando para `.NSN` ou `[GREENFIELD] + justificativa`.
>
> ⚠️ **Status: PROPOSTA.** Requisitos derivados de regras **Confirmadas** entram
> direto; regras **Inferidas** promovidas estão marcadas com
> `(Promovida de inferred)` e dependem de validação da equipe. **Mistérios
> bloqueadores (MYS-001..006) NÃO são requisitos** — estão em *Open Questions*.

## Bounded Context: Beneficiary Management

### REQ-001: Validação de CPF por módulo 11

Se o CPF informado não passar na validação de módulo 11 (dois dígitos verificadores, com DV=0 quando resto < 2), então o sistema deverá rejeitar o cadastro do beneficiário.

- **EARS Pattern:** Unwanted
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L111-L117`, `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L224-L269`
- **Source Rule:** BR-002 / CADBENEF #3 (Confirmada — MANUAL §3.2.1)
- **Critérios de Aceite:**
  - [ ] Given um CPF com dígitos verificadores inválidos, when o cadastro é submetido, then o sistema rejeita com erro de validação.
  - [ ] Given um CPF válido por módulo 11, when o cadastro é submetido, then a validação de CPF passa.

### REQ-002: CPF obrigatório

Se o CPF informado for zero ou ausente, então o sistema deverá rejeitar o cadastro do beneficiário.

- **EARS Pattern:** Unwanted
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L105-L109`
- **Source Rule:** CADBENEF #2 (Promovida de inferred — decisão da equipe: CPF é a chave de negócio do beneficiário)
- **Critérios de Aceite:**
  - [ ] Given CPF = 0 ou vazio, when o cadastro é submetido, then o sistema rejeita.
  - [ ] Given CPF preenchido e válido, when o cadastro é submetido, then a obrigatoriedade é satisfeita.

### REQ-003: Campos obrigatórios do beneficiário

Se nome, data de nascimento ou sexo (`M`/`F`) não forem informados, então o sistema deverá rejeitar o cadastro do beneficiário.

- **EARS Pattern:** Unwanted
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L119-L135`
- **Source Rule:** CADBENEF #4 (Promovida de inferred — decisão da equipe: integridade mínima de dados pessoais)
- **Critérios de Aceite:**
  - [ ] Given um cadastro sem nome, sem data de nascimento ou com sexo diferente de `M`/`F`, when submetido, then o sistema rejeita indicando o campo ausente/ inválido.
  - [ ] Given todos os três campos preenchidos e válidos, when submetido, then a validação de obrigatoriedade passa.

### REQ-004: Unicidade na inclusão e existência na alteração

Se, na operação de inclusão, já existir beneficiário com o mesmo CPF, então o sistema deverá rejeitar a inclusão; e se, na operação de alteração, o beneficiário não existir, então o sistema deverá rejeitar a alteração.

- **EARS Pattern:** Unwanted
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L143-L153`
- **Source Rule:** CADBENEF #5 (Promovida de inferred — decisão da equipe: consistência de chave)
- **Critérios de Aceite:**
  - [ ] Given um CPF já cadastrado, when uma inclusão é submetida, then o sistema rejeita por duplicidade.
  - [ ] Given um CPF inexistente, when uma alteração é submetida, then o sistema rejeita por inexistência.

### REQ-005: Limite de dependentes por titular

Se o titular já possuir 5 dependentes, então o sistema deverá rejeitar a inclusão de um novo dependente.

- **EARS Pattern:** Unwanted
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L63-L66`
- **Source Rule:** BR-017 (Promovida de inferred). O valor `5` é magic number — origem normativa pendente em **MYS-020** (Open Questions).
- **Critérios de Aceite:**
  - [ ] Given um titular com 5 dependentes, when um novo dependente é incluído, then o sistema rejeita.
  - [ ] Given um titular com menos de 5 dependentes, when um novo dependente é incluído, then a inclusão é aceita.

### REQ-006: Mascaramento de CPF em consulta (LGPD)

Onde um usuário consulta dados de um beneficiário, o sistema deverá mascarar o CPF exibido conforme a política de privacidade (LGPD), revelando apenas os dígitos permitidos.

- **EARS Pattern:** Optional
- **source_legacy:** `[GREENFIELD]` — substitui o mascaramento defeituoso do legado (`CONSBENF.NSN#L171-L189`, MYS-005). Justificativa: conformidade LGPD; o comportamento legado é reconhecidamente inconsistente e não deve ser replicado.
- **Critérios de Aceite:**
  - [ ] Given um beneficiário com CPF completo, when exibido em consulta, then o CPF aparece mascarado segundo a política definida.
  - [ ] Given o formato de máscara configurado, when aplicado a qualquer CPF, then nenhum dígito além dos permitidos é exposto.

## Bounded Context: Social Program

### REQ-007: Manutenção de parâmetros do programa social

O sistema deverá manter os parâmetros de cada programa social (valor-base, tipo, faixas e vigência) e disponibilizá-los para consulta pelo contexto de Pagamento.

- **EARS Pattern:** Ubiquitous
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L77-L109`
- **Source Rule:** Derivada do CADPROG (cadastro/consulta de programa). O ajuste por FATOR-K (`BR-007`) **não** é especificado aqui — é bloqueador **MYS-002** (Open Questions).
- **Critérios de Aceite:**
  - [ ] Given um programa social novo, when cadastrado, then seus parâmetros ficam disponíveis para consulta por código.
  - [ ] Given um código de programa existente, when consultado, then o sistema retorna os parâmetros vigentes.

### REQ-008: Elegibilidade por tipo de programa

Onde o programa social for do tipo `A`, o sistema deverá conceder elegibilidade somente se a renda familiar for ≤ R$ 600,00 e os documentos forem válidos; onde for do tipo `P`, somente se a idade for ≥ 60 anos; onde for do tipo `T`, somente se a idade estiver entre 16 e 65 anos.

- **EARS Pattern:** Optional
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L168-L201`
- **Source Rule:** BR-013 (Promovida de inferred). Os limiares 600/60/16/65 são magic numbers — confirmação pendente em **MYS-027** (Open Questions). O bypass de região 99 (`BR-014`) **não** é especificado — ver **MYS-010**.
- **Critérios de Aceite:**
  - [ ] Given um programa tipo `A` e renda familiar de R$ 700,00, when a elegibilidade é avaliada, then o sistema nega.
  - [ ] Given um programa tipo `P` e idade 62, when a elegibilidade é avaliada, then o sistema concede.
  - [ ] Given um programa tipo `T` e idade 70, when a elegibilidade é avaliada, then o sistema nega.

## Bounded Context: Payment & Cycle

### REQ-009: Ciclo mensal somente para beneficiários ativos

Enquanto o beneficiário não estiver com status `A` (ativo), o sistema não deverá gerar pagamento para ele no ciclo mensal.

- **EARS Pattern:** State-driven
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L195-L198`
- **Source Rule:** BR-005 / BATCHPGT #2 (Confirmada — REGRAS-NEGOCIO-2012 §5.1, MANUAL §3.5.1)
- **Critérios de Aceite:**
  - [ ] Given um beneficiário com status diferente de `A`, when o ciclo mensal executa, then nenhum pagamento é gerado para ele.
  - [ ] Given um beneficiário com status `A` e sem pagamento na competência, when o ciclo executa, then um pagamento é gerado.

### REQ-010: Idempotência do ciclo por competência

Se já existir um pagamento do beneficiário na competência corrente, então o sistema não deverá gerar um pagamento duplicado.

- **EARS Pattern:** Unwanted
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L202-L210`
- **Source Rule:** BATCHPGT #3 (Promovida de inferred — decisão da equipe: evitar pagamento em duplicidade no reprocessamento)
- **Critérios de Aceite:**
  - [ ] Given um beneficiário com pagamento já existente na competência, when o ciclo reexecuta, then nenhum novo pagamento é gerado.
  - [ ] Given um beneficiário sem pagamento na competência, when o ciclo executa, then exatamente um pagamento é gerado.

### REQ-011: Teto de descontos de 30% (judicial sem teto)

Se o total de descontos não judiciais exceder 30% do valor bruto, então o sistema deverá limitar os descontos a 30% do bruto; descontos do tipo judicial (`J`) não estão sujeitos ao teto.

- **EARS Pattern:** Unwanted
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L101-L169`
- **Source Rule:** BR-001 (Confirmada — REGRAS-NEGOCIO-2012 §5.1). Este requisito fixa o motor `CALCDSCT` como **canônico**; as implementações inline divergentes são tratadas em **MYS-006** (Open Questions).
- **Critérios de Aceite:**
  - [ ] Given descontos não judiciais somando 40% do bruto, when o desconto é calculado, then o total aplicado é limitado a 30%.
  - [ ] Given um desconto judicial (`J`) de 50% do bruto, when calculado, then o teto de 30% não é aplicado a esse item.

### REQ-012: Contribuição social progressiva por faixa

O sistema deverá aplicar contribuição social progressiva sobre o valor bruto por faixa: ≤ R$ 500,00 = 3%; ≤ R$ 1.000,00 = 5%; ≤ R$ 2.000,00 = 7%; ≤ R$ 9.999,99 = 9%.

- **EARS Pattern:** Ubiquitous
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L57-L65`
- **Source Rule:** BR-003. A alíquota de topo (9% no código vs 10% citado no kit) está em divergência — ver **MYS-034** (Open Questions).
- **Critérios de Aceite:**
  - [ ] Given um bruto de R$ 400,00, when a contribuição é calculada, then a alíquota aplicada é 3%.
  - [ ] Given um bruto de R$ 1.500,00, when a contribuição é calculada, then a alíquota aplicada é 7%.

### REQ-013: Cálculo do valor do benefício

O sistema deverá calcular o valor do benefício como `base × fator regional × fator familiar × fator de renda × fator idade × (1 + reajuste)`, truncado em 2 casas decimais.

- **EARS Pattern:** Ubiquitous
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L221-L233`
- **Source Rule:** BR-004. O motor `CALCBENF` é o único cálculo válido; a duplicação inline em BATCHPGT é tratada em **MYS-007**. Os valores exatos das tabelas de fatores são inferidos (ver Open Questions).
- **Critérios de Aceite:**
  - [ ] Given os fatores e a base definidos, when o benefício é calculado, then o resultado é o produto especificado truncado em 2 casas.
  - [ ] Given dois caminhos de cálculo (ciclo batch e cálculo avulso), when executados com a mesma entrada, then produzem exatamente o mesmo valor (motor único).

### REQ-014: Décimo terceiro em dezembro

Quando o mês de processamento for dezembro, o sistema deverá marcar o pagamento como tipo `D` e somar o décimo terceiro (`base × fator regional × fator idade`).

- **EARS Pattern:** Event-driven
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L292-L304`, `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L238-L260`
- **Source Rule:** BR-006 / BATCHPGT #10 (existência do 13º Confirmada — REGRAS §2.1). O abono de 15% (`MYS-015`) e a divergência de fórmula (`MYS-011`) **não** são especificados aqui (Open Questions).
- **Critérios de Aceite:**
  - [ ] Given a competência de dezembro, when o pagamento é gerado, then o tipo é `D` e o 13º é somado.
  - [ ] Given uma competência de janeiro a novembro, when o pagamento é gerado, then nenhum 13º é somado.

### REQ-015: Proteção contra valor líquido negativo

Se o valor líquido calculado for negativo, então o sistema deverá ajustá-lo para zero.

- **EARS Pattern:** Unwanted
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L315-L320`
- **Source Rule:** BATCHPGT #12 (Promovida de inferred — decisão da equipe: nunca pagar valor negativo)
- **Critérios de Aceite:**
  - [ ] Given descontos que excedem o bruto, when o líquido é calculado, then o resultado é R$ 0,00.
  - [ ] Given um líquido positivo, when calculado, then o valor é mantido e truncado em 2 casas.

### REQ-016: Conciliação por chave tripla

O sistema deverá casar cada registro de retorno bancário (CNAB) com um pagamento cujo número, CPF e competência coincidam com os do retorno.

- **EARS Pattern:** Ubiquitous
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L139-L144`
- **Source Rule:** BATCHCON #3 (Promovida de inferred — decisão da equipe: chave de conciliação determinística)
- **Critérios de Aceite:**
  - [ ] Given um retorno com número, CPF e competência que correspondem a um pagamento, when conciliado, then o pagamento é localizado.
  - [ ] Given um retorno sem pagamento correspondente, when conciliado, then o sistema registra "não encontrado" e segue.

### REQ-017: Tolerância de divergência na conciliação

Se a diferença absoluta entre o valor líquido do SIFAP e o valor retornado pelo banco exceder a tolerância configurada, então o sistema deverá marcar o pagamento como divergente e registrar auditoria de divergência; caso contrário, deverá tratá-lo como conciliado.

- **EARS Pattern:** Unwanted
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L160-L202`
- **Source Rule:** BR-009 / BATCHCON #6. O valor da tolerância (R$ 0,01 no legado) é parametrizável — confirmação pendente em **MYS-023** (Open Questions).
- **Critérios de Aceite:**
  - [ ] Given uma diferença acima da tolerância, when conciliado, then o pagamento é marcado divergente e gera auditoria `DV`.
  - [ ] Given uma diferença dentro da tolerância, when conciliado, then o pagamento é tratado como conciliado.

### REQ-021: Status inicial do pagamento no ciclo

Quando o ciclo mensal gerar um pagamento, o sistema deverá gravá-lo com status inicial `G` (gerado).

- **EARS Pattern:** Event-driven
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L332-L335`, `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L283`
- **Source Rule:** BR-020 / BATCHPGT #15 (máquina de estados decidida em [ADR-0003](ADRs/adr-0003-payment-state-machine.md), Opção 1 — modelo do código legado `G/P/D/E/C`; resolve MYS-001)
- **Critérios de Aceite:**
  - [ ] Given um beneficiário ativo sem pagamento na competência, when o ciclo gera o pagamento, then o status persistido é `G`.
  - [ ] Given um pagamento recém-gerado, when consultado, then seu status é `G` e nunca `P` antes da conciliação.

### REQ-022: Transições de status pós-conciliação bancária

Quando o sistema processar um retorno bancário para um pagamento em status `G`, o sistema deverá transitar o status para `P` (pago) se o código de retorno for `00`, para `D` (devolvido) se for `01` e para `E` (erro) se for `02`.

- **EARS Pattern:** Event-driven
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L172-L194`
- **Source Rule:** BR-009 / BATCHCON #7-#9 (transições conforme [ADR-0003](ADRs/adr-0003-payment-state-machine.md)). O status `D` (devolvido) é distinto do **tipo** de pagamento `D` (dezembro/13º — REQ-014), em campos separados.
  - A tabela completa de códigos de retorno permanece pendente em **MYS-024** (Open Questions).
- **Critérios de Aceite:**
  - [ ] Given um pagamento `G` e retorno `00`, when conciliado, then o status passa a `P`.
  - [ ] Given um pagamento `G` e retorno `01`, when conciliado, then o status passa a `D` (devolvido), sem alterar o tipo de pagamento.
  - [ ] Given um pagamento `G` e retorno `02`, when conciliado, then o status passa a `E`.

### REQ-023: Transições de status inválidas são rejeitadas

Se for solicitada uma transição de status fora das transições válidas definidas (`G → P/D/E`, e `→ C` cancelamento), então o sistema não deverá aplicá-la e deverá registrar a tentativa.

- **EARS Pattern:** Unwanted
- **source_legacy:** `[GREENFIELD]` — torna explícita a máquina de estados que o legado aplicava implicitamente (decidida em [ADR-0003](ADRs/adr-0003-payment-state-machine.md)). Justificativa: integridade do ciclo de pagamento e auditabilidade das transições.
- **Critérios de Aceite:**
  - [ ] Given um pagamento em status `P` (pago), when uma transição para `G` é solicitada, then o sistema rejeita e não altera o status.
  - [ ] Given uma transição válida (ex.: `G → P`), when solicitada, then o sistema a aplica e registra evento de auditoria (REQ-018).

## Bounded Context: Audit Trail

### REQ-018: Registro de auditoria na conciliação

Quando um pagamento for conciliado, o sistema deverá registrar um evento de auditoria com ação `CO`; e quando houver divergência, deverá registrar um evento com ação `DV` contendo o valor anterior e o novo.

- **EARS Pattern:** Event-driven
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L207-L240`
- **Source Rule:** BATCHCON #11 (Confirmada — trilha adicionada 2014, grava no DDM `AUDITORIA`)
- **Critérios de Aceite:**
  - [ ] Given um pagamento conciliado, when o evento é gravado, then existe um registro de auditoria com ação `CO`.
  - [ ] Given uma divergência, when o evento é gravado, then existe um registro `DV` com valor anterior e novo.

### REQ-019: Trilha de auditoria completa (inclui exclusões)

O sistema deverá registrar e expor na trilha de auditoria todos os eventos, incluindo exclusões (ação `EX`), sem ocultá-los de relatórios.

- **EARS Pattern:** Ubiquitous
- **source_legacy:** `[GREENFIELD]` — corrige o comportamento legado que oculta exclusões (`RELAUDIT.NSN#L102-L108`, MYS-004). Justificativa: integridade e compliance da trilha de auditoria.
- **Critérios de Aceite:**
  - [ ] Given um evento de exclusão (`EX`), when a trilha é consultada, then o evento aparece no resultado.
  - [ ] Given qualquer ação registrada, when a trilha é consultada, then nenhum filtro oculta eventos por tipo de ação.

### REQ-020: Auditoria de mutações de beneficiário

Quando um beneficiário for incluído ou alterado, o sistema deverá registrar um evento de auditoria correspondente.

- **EARS Pattern:** Event-driven
- **source_legacy:** `[GREENFIELD]` — padroniza a auditoria de mutações; o legado referencia um `LOGAUDIT` inexistente (MYS-012). Justificativa: rastreabilidade de alterações de dados pessoais.
- **Critérios de Aceite:**
  - [ ] Given uma inclusão de beneficiário, when concluída, then um evento de auditoria é registrado com o ator e o instante.
  - [ ] Given uma alteração de beneficiário, when concluída, then um evento de auditoria é registrado.

## Open Questions (Not Requirements Yet)

> Extraídas de [`../01-arqueologia/mysteries-found.md`](../01-arqueologia/mysteries-found.md).
> Bloqueadores (🔴 `blocks-stage-2`) precisam de decisão do PO/facilitador antes de
> fixar acceptance criteria dependentes.

| ID | Pergunta em aberto | Severidade | O que é necessário para resolver |
|----|--------------------|------------|----------------------------------|
| MYS-001 | ✅ **RESOLVIDA** ([ADR-0003](ADRs/adr-0003-payment-state-machine.md), Opção 1 — modelo do código `G/P/D/E/C`). Formalizada em REQ-021..REQ-023. Doc oficial deve ser corrigida para refletir `'P'` = pago. | ~~🔴 blocks-stage-2~~ → resolvida | — (decisão registrada no ADR-0003) |
| MYS-002 | Origem e validade do `FATOR-K` (0,347215) que ajusta VLR-BASE na inclusão de programa | 🔴 blocks-stage-2 | Confirmar origem normativa; decide se REQ-007 ganha regra de ajuste de base |
| MYS-003 | O backdoor de prefixos de CPF deve ser preservado? | 🔴 blocks-stage-2 | Decisão de PO/segurança |
| MYS-004 | Exclusões (`EX`) devem aparecer na trilha? *(proposta: REQ-019 diz que sim)* | 🔴 blocks-stage-2 | Confirmação de compliance/auditoria |
| MYS-005 | Qual é a máscara de CPF correta? *(proposta: REQ-006 LGPD)* | 🔴 blocks-stage-2 | Política LGPD validada pelo PO |
| MYS-006 | Qual é a regra **canônica** de desconto? *(proposta: REQ-011 usa CALCDSCT)* | 🔴 blocks-stage-2 | Decisão de PO para eliminar os 3% inline divergentes |
| MYS-007 | O cálculo inline do BATCHPGT diverge do CALCBENF? | 🟠 needs-investigation | Comparação linha a linha; confirma REQ-013 (motor único) |
| MYS-008 | Período coberto pela tabela IPCA (só 2010–2012 no legado) | 🟠 needs-investigation | Fonte completa de índices antes de especificar correção retroativa |
| MYS-011 | Fórmula real do 13º (comentário ≠ código) | 🟠 needs-investigation | Confirma os fatores de REQ-014 |
| MYS-015 | Abono natalino de 15% para tipo `A` — base documental? | 🟡 needs-facilitator | Especialista de benefícios |
| MYS-020 | Origem do teto de 5 dependentes (REQ-005) | 🟡 needs-facilitator | Teto legal de dependentes |
| MYS-023 | Valor da tolerância de conciliação (REQ-017) | 🟡 needs-facilitator | Tolerância aceita pelo negócio |
| MYS-024 | Tabela completa de códigos de retorno CNAB (`00→P`,`01→D`,`02→E`, `COD-BANCO=1`) | 🟡 needs-facilitator | Documentação do banco |
| MYS-027 | Limiares de elegibilidade 600/60/16/65 (REQ-008) | 🟡 needs-facilitator | Confirmação das regras dos programas |
| MYS-010 | Região 99 concede elegibilidade automática — manter? | 🟠 needs-investigation | Decisão de PO/segurança |
| MYS-034 | Alíquota de topo da contribuição: 9% (código) vs 10% (kit) — REQ-012 | 🟢 parked | Confirmar alíquota vigente |

## Traceability Matrix

| REQ-ID | EARS Pattern | source_legacy | Source Rule # | Source File | Bounded Context |
|--------|--------------|---------------|---------------|-------------|-----------------|
| REQ-001 | Unwanted | `.NSN` | BR-002 / CADBENEF #3 | CADBENEF.NSN | Beneficiary Management |
| REQ-002 | Unwanted | `.NSN` | CADBENEF #2 | CADBENEF.NSN | Beneficiary Management |
| REQ-003 | Unwanted | `.NSN` | CADBENEF #4 | CADBENEF.NSN | Beneficiary Management |
| REQ-004 | Unwanted | `.NSN` | CADBENEF #5 | CADBENEF.NSN | Beneficiary Management |
| REQ-005 | Unwanted | `.NSN` | BR-017 | CADDEPEND.NSN | Beneficiary Management |
| REQ-006 | Optional | `[GREENFIELD]` | — (corrige MYS-005) | CONSBENF.NSN | Beneficiary Management |
| REQ-007 | Ubiquitous | `.NSN` | CADPROG (cadastro) | CADPROG.NSN | Social Program |
| REQ-008 | Optional | `.NSN` | BR-013 | VALELEG.NSN | Social Program |
| REQ-009 | State-driven | `.NSN` | BR-005 / BATCHPGT #2 | BATCHPGT.NSN | Payment & Cycle |
| REQ-010 | Unwanted | `.NSN` | BATCHPGT #3 | BATCHPGT.NSN | Payment & Cycle |
| REQ-011 | Unwanted | `.NSN` | BR-001 | CALCDSCT.NSN | Payment & Cycle |
| REQ-012 | Ubiquitous | `.NSN` | BR-003 | CALCDSCT.NSN | Payment & Cycle |
| REQ-013 | Ubiquitous | `.NSN` | BR-004 | CALCBENF.NSN | Payment & Cycle |
| REQ-014 | Event-driven | `.NSN` | BR-006 / BATCHPGT #10 | BATCHPGT.NSN, CALCBENF.NSN | Payment & Cycle |
| REQ-015 | Unwanted | `.NSN` | BATCHPGT #12 | BATCHPGT.NSN | Payment & Cycle |
| REQ-016 | Ubiquitous | `.NSN` | BATCHCON #3 | BATCHCON.NSN | Payment & Cycle |
| REQ-017 | Unwanted | `.NSN` | BR-009 / BATCHCON #6 | BATCHCON.NSN | Payment & Cycle |
| REQ-018 | Event-driven | `.NSN` | BATCHCON #11 | BATCHCON.NSN | Audit Trail |
| REQ-019 | Ubiquitous | `[GREENFIELD]` | — (corrige MYS-004) | RELAUDIT.NSN | Audit Trail |
| REQ-020 | Event-driven | `[GREENFIELD]` | — (deriva MYS-012) | (transversal) | Audit Trail |
| REQ-021 | Event-driven | `.NSN` | BR-020 / BATCHPGT #15 (ADR-0003) | BATCHPGT.NSN, CALCBENF.NSN | Payment & Cycle |
| REQ-022 | Event-driven | `.NSN` | BR-009 / BATCHCON #7-#9 (ADR-0003) | BATCHCON.NSN | Payment & Cycle |
| REQ-023 | Unwanted | `[GREENFIELD]` | — (ADR-0003, máquina de estados explícita) | (Payment domain) | Payment & Cycle |

## Aprovação

- Revisado por: Salles
- Data: 11/06/2026
- Confiança: Alta
