<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Catálogo de Regras de Negócio — SIFAP Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **business-rules-catalog**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Registre aqui todas as regras de negócio extraídas do código Natural/Adabas.
> Cada regra precisa ter rastreabilidade até o código-fonte.
>
> **REGRA DURA:** linhas com `Programa Fonte` vazio são **inválidas** e não contam para o gate do Estágio 2. Use o formato `01-arqueologia/legado-sifap/natural-programs/ARQUIVO.NSN#L<inicio>-L<fim>` sempre que possível. Mínimo aceito: nome do arquivo .NSN.

## Como pensar em "regra de negócio"

O que conta:

- Um `IF` que decide algo no domínio (ex.: _"se a UF é do Nordeste e o programa é Seca, valor base × 1.2"_)
- Uma constante numérica sem explicação (ex.: `0.075` num cálculo de imposto)
- Uma transição de status com regra (ex.: _"só de A para S, nunca de I para A"_)
- Um tratamento especial para um caso (ex.: _"se o CPF começa com 999, é teste"_)

O que NÃO conta: paginação de relatório, formatação de saída, manipulação de cursor Adabas, abertura de arquivo. Ignore esses detalhes de implementação.

## Níveis de Risco

| Nível       | Descrição                                                     |
| ----------- | ------------------------------------------------------------- |
| **CRÍTICO** | Regra financeira ou de segurança — erro causa prejuízo direto |
| **ALTO**    | Regra de negócio central — afeta fluxo principal              |
| **MÉDIO**   | Regra de validação ou formatação — afeta qualidade dos dados  |
| **BAIXO**   | Regra de apresentação ou conveniência — impacto limitado      |

## Regras Encontradas

> Tabela-resumo consolidada a partir das seções detalhadas por programa (abaixo).
> Prioriza as regras CRÍTICAS/ALTAS e os mistérios mais relevantes. As seções
> `## Regras de <programa>.NSN` trazem o catálogo completo (~100 regras, 15/15 programas).

| ID     | Regra de Negócio | Programa Fonte | Campos DDM | Nível de Risco | Notas |
| ------ | ---------------- | -------------- | ---------- | -------------- | ----- |
| BR-001 | Total de descontos não judiciais não pode exceder 30% do bruto; judicial (tipo `J`) não tem teto | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L101-L169` | `PAGAMENTO.VLR-BRUTO`, `PAGAMENTO.VLR-DESCONTO`, `BENEFICIARIO.DESCONTOS.TIPO-DSCT` | CRÍTICO | Motor oficial de descontos. Teto aplicado por item no loop (ordem do PE afeta resultado) |
| BR-002 | CPF validado por módulo 11 (dois dígitos verificadores; resto<2 ⇒ DV=0) | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L224-L269` | `BENEFICIARIO.CPF` | ALTO | Lógica **triplicada** em CADBENEF, VALBENEF, VALDOCS |
| BR-003 | Contribuição social progressiva por faixa de bruto: ≤500=3%, ≤1000=5%, ≤2000=7%, ≤9999,99=9% | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L57-L65` | `PAGAMENTO.VLR-BRUTO` | CRÍTICO | Header "NOVAS ALIQUOTAS 2015"; topo 9% (kit cita 10% — divergência de versão) |
| BR-004 | Valor do benefício = base × fator regional × familiar × renda × idade × (1+reajuste), truncado 2 casas | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L221-L233` | `PROGRAMA-SOCIAL.VLR-BASE`, `BENEFICIARIO.COD-REGIAO`, `BENEFICIARIO.NUM-DEPENDENTES`, `BENEFICIARIO.RENDA-FAMILIAR` | CRÍTICO | Motor de cálculo; **duplicado inline** no BATCHPGT |
| BR-005 | Ciclo mensal gera pagamento para todo beneficiário ativo (`STATUS='A'`) sem pagamento na competência | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L195-L210` | `BENEFICIARIO.STATUS`, `PAGAMENTO.COMPETENCIA`, `PAGAMENTO.STATUS-PGTO` | CRÍTICO | BATCHPGT reimplementa CALCBENF; grava status `'G'` |
| BR-006 | Em dezembro: tipo `'D'`, soma 13º (base×regional×idade); programa tipo `'A'` recebe abono de 15% | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L238-L260` | `PAGAMENTO.VLR-ABONO`, `PAGAMENTO.TIPO-PGTO`, `PROGRAMA-SOCIAL.TIPO` | CRÍTICO | Comentário do 13º ≠ fórmula real; abono 15% sem base documental |
| BR-007 | Valor-base do programa é ajustado por FATOR-K: `base × (1,00 + reajuste × 0,347215)` na inclusão | `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L86-L93` | `PROGRAMA-SOCIAL.VLR-BASE`, `PROGRAMA-SOCIAL.FATOR-REAJUSTE` | CRÍTICO | É o "FATOR-K" misterioso da doc §2.1; 0,347215 sem origem |
| BR-008 | Correção retroativa por IPCA acumulado; grava só se diferença positiva e ainda não corrigido | `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L144-L189` | `PAGAMENTO.VLR-CORRECAO`, `PAGAMENTO.IND-CORRIGIDO` | ALTO | Tabela IPCA só 2010–2012 → correção zero para anos recentes |
| BR-009 | Conciliação bancária: divergência > R$ 0,01 vira auditoria; retorno `00→P`, `01→D`, `02→E` | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L160-L202` | `PAGAMENTO.STATUS-PGTO`, `PAGAMENTO.COD-RETORNO`, `AUDITORIA.ACAO` | CRÍTICO | Tolerância 0,01 e `COD-BANCO=1` magic; `'P'`=pago aqui |
| BR-010 | Prefixo de CPF na lista especial (`000,001,002,010,011,099,100,999`) força documento válido | `01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L168-L182` | `BENEFICIARIO.CPF` | CRÍTICO | **Backdoor**: anula todas as validações anteriores |
| BR-011 | Eventos de auditoria com ação `'EX'` (exclusão) nunca aparecem no relatório de auditoria | `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN#L102-L108` | `AUDITORIA.ACAO` | CRÍTICO | "LIMPEZA RELATORIO 2014"; integridade da trilha comprometida |
| BR-012 | CPF é mascarado na consulta, mas o mascaramento é reconhecidamente inconsistente | `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN#L176-L189` | `BENEFICIARIO.CPF` | CRÍTICO | Autoadmitido no código; risco LGPD |
| BR-013 | Elegibilidade por tipo: `A` renda≤600+docs; `P` idade≥60; `T` idade 16–65 | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L168-L201` | `PROGRAMA-SOCIAL.TIPO`, `BENEFICIARIO.RENDA-FAMILIAR`, `BENEFICIARIO.DT-NASCIMENTO` | ALTO | Magic numbers 600/60/16/65 sem doc |
| BR-014 | Região 99 concede elegibilidade automática, ignorando status/idade/renda/docs | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L104-L111` | `BENEFICIARIO.COD-REGIAO` | ALTO | Bypass amplo "internacional/diplomático" (incluído 2013) |
| BR-015 | CPF com todos os dígitos iguais é inválido, exceto se iniciar com `000` (teste de governo) | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L187-L203` | `BENEFICIARIO.CPF` | ALTO | Bypass; permite `00000000000` |
| BR-016 | Idade > 75 anos define status do beneficiário como `'S'` (suspenso) na gravação | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L166-L169` | `BENEFICIARIO.STATUS`, `BENEFICIARIO.DT-NASCIMENTO` | MÉDIO | Contraintuitivo ("AJUSTE STATUS IDOSO 2011") |
| BR-017 | Limite de 5 dependentes por titular | `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L63-L66` | `BENEFICIARIO.NUM-DEPENDENTES`, `BENEFICIARIO.DEPENDENTES` | MÉDIO | Teto magic; origem normativa desconhecida |
| BR-018 | Data de nascimento aceita 29/02 em qualquer ano (fevereiro = 29 fixo) | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L242-L259` | `BENEFICIARIO.DT-NASCIMENTO` | MÉDIO | Sem cálculo de bissexto real |
| BR-019 | Relatório consolidado arredonda (round half-up) enquanto o cálculo trunca | `01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L136-L140` | `PAGAMENTO.VLR-BRUTO` | MÉDIO | Totais do relatório divergem da base |
| BR-020 | Status do pagamento traduzido G/P/C/D/E; `'P'`=PAGO (conflita com doc que diz pendente) | `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN#L128-L141` | `PAGAMENTO.STATUS-PGTO` | ALTO | Modelo de estados ambíguo entre programas e docs |

> Esta é a lista-resumo. O catálogo completo por programa está nas seções abaixo. Existiam **mistérios escondidos** no código — ~25 estão marcados com `<!-- mystery: ... -->`.

## Exemplo de linha bem preenchida

| ID     | Regra de Negócio                                                                        | Programa Fonte                                   | Campos DDM                                                               | Nível de Risco | Notas                                      |
| ------ | --------------------------------------------------------------------------------------- | ------------------------------------------------ | ------------------------------------------------------------------------ | -------------- | ------------------------------------------ |
| BR-013 | Desconto total não pode exceder 30% do valor bruto, exceto descontos judiciais (tipo J) | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L142-L148` | `PAGAMENTO.VLR-BRUTO`, `PAGAMENTO.VLR-TOTAL-DSCT`, `PAGAMENTO.TIPO-DSCT` | CRÍTICO        | Regra financeira. Tipo 'J' = exceção legal |

## Regras por Categoria

### Cálculos Financeiros

- BR-001 (teto 30% descontos), BR-003 (contribuição progressiva), BR-004 (cálculo do benefício), BR-006 (13º + abono), BR-007 (FATOR-K), BR-008 (correção IPCA), BR-009 (conciliação), BR-019 (round vs truncate). Motores: `CALCBENF`, `CALCDSCT`, `CALCCORR`, `BATCHPGT`, `CADPROG`, `BATCHCON`, `BATCHREL`.

### Validações de Status

- BR-005 (só ativos geram pagamento), BR-016 (>75 → `'S'`), BR-020 (G/P/C/D/E), BR-013/BR-014 (elegibilidade por status/região). Estados do beneficiário: `A/S/C/I/D`. Estados do pagamento: `G/P/C/D/E` (com **conflito** `'P'` pendente vs pago).

### Regras de Autorização

- BR-010 (backdoor de documento por prefixo de CPF), BR-014 (região 99 = elegível), BR-015 (CPF `000` aceito), BR-011 (auditoria oculta exclusões). Concentram os riscos de segurança/integridade.

### Regras de Negócio Temporais

- BR-006 (dezembro/13º), BR-008 (período de correção IPCA), BR-018 (validação de data/29-02), idempotência por competência (BR-005). Datas-chave: competência mensal (`AAAAMM`), 1º dia útil (batch).

## Resumo Estatístico

- Total de regras encontradas: **~100** (15/15 programas; 20 consolidadas como BR-001..BR-020)
- Regras críticas: **10** (BR-001, BR-003, BR-004, BR-005, BR-006, BR-007, BR-009, BR-010, BR-011, BR-012)
- Regras com duplicação: **2 grandes** — cálculo do benefício (CALCBENF ↔ BATCHPGT) e validação de CPF módulo 11 (CADBENEF ↔ VALBENEF ↔ VALDOCS, triplicada)
- Regras sem documentação (escondidas): **~25** marcadas com `<!-- mystery: ... -->`; destaques: FATOR-K, backdoor VALDOCS, auditoria oculta exclusões, modelo de estados `'P'` ambíguo, região 99, CPF `000`, tabela IPCA incompleta

---

## Regras de BATCHPGT.NSN

> Extraído via `/extract-business-rules` a partir do fonte real
> `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN` (378 linhas).
> Cross-reference com `legacy-docs/MANUAL-TECNICO-SIFAP-2008.md` (§3.5.1) e
> `legacy-docs/REGRAS-NEGOCIO-2012.md` (§2.1, §5.1, §5.2).
> Variáveis-chave (DEFINE DATA): views `BENEFICIARIO-V`, `PAGAMENTO-V`,
> `PROGRAMA-V`; tabela `#TAB-REG(27)`; arrays `#FAIXA-RENDA(5)`/`#FATOR-FAIXA(5)`;
> `#LOG-WORK/#LOG-ERRO` (declarados e **nunca usados**).

| #  | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
|----|---------------------|----------------|-------|---------------|-------|
| 1 | Quando um CPF lido for igual ao CPF imediatamente anterior, o sistema deve ignorá-lo (contar como ignorado) e não gerar pagamento. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L188-L191` | Inferida | Dedup apenas de registros **adjacentes** (depende da leitura `BY CPF`). Não detecta duplicatas não adjacentes. |
| 2 | Enquanto o beneficiário não estiver com status `'A'` (ativo), o sistema não deve gerar pagamento para ele (conta como ignorado). | State-driven | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L195-L198` | Confirmada | REGRAS-NEGOCIO-2012 §5.1 ("beneficiários ativos `BN-CD-SIT = 'A'`") e MANUAL-2008 §3.5.1. |
| 3 | Se já existir pagamento do beneficiário na competência corrente, o sistema deve ignorá-lo (idempotência do ciclo). | Unwanted | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L202-L210` | Inferida | `FIND PAGAMENTO` + `IF COMPETENCIA = #COMPETENCIA`. Doc descreve geração mensal mas não a checagem explícita de reprocessamento. |
| 4 | Se o programa social do beneficiário não for encontrado, o sistema deve registrar erro em log, contar como erro e não gerar pagamento. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L220-L226` | Inferida | Único caso que incrementa `#QTD-ERROS`. <!-- mystery: o erro só vai para WRITE; o array #LOG-ERRO declarado nunca é gravado --> |
| 5 | Enquanto o programa social não estiver com `STATUS-PROG = 'A'`, o sistema não deve gerar pagamento (conta como ignorado). | State-driven | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L227-L230` | Inferida | Sem suporte documental direto. |
| 6 | Quando a região do beneficiário estiver entre 1 e 25, o sistema deve aplicar o fator regional da tabela; caso contrário, deve usar fator 1,0000. | Optional | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L240-L244` | Mistério | <!-- mystery: #TAB-REG é dimensionada com 27 posições e inicializada até (27), mas a condição só aceita 1..25; posições 26 e 27 nunca são lidas. Significado das regiões 26/27 desconhecido --> |
| 7 | O sistema deve calcular o fator familiar por faixas de dependentes: 0 dep = 1,0000; 1–2 dep = 1,0000 + (dep × 0,05); 3–4 dep = 1,1000 + ((dep−2) × 0,03); ≥5 dep = 1,1600 + ((dep−4) × 0,02). | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L247-L259` | Inferida | Constantes hardcoded; sem documentação das faixas. |
| 8 | O sistema deve calcular o fator idade: ≥65 anos = 1,1500; 60–64 = 1,1000; <18 = 1,0500; demais = 1,0000. | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L265-L277` | Inferida | Idade derivada só do ano (`#ANO − #ANO-NASC`), sem considerar mês/dia. |
| 9 | O valor do benefício é o produto de valor-base × fator regional × fator familiar × fator renda × fator idade, depois multiplicado por (1 + fator de reajuste) e truncado em 2 casas decimais. | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L279-L285` | Mistério | Reimplementa o cálculo do CALCBENF inline (comentário L123 "MESMA DO CALCBENF"). <!-- mystery: o header diz "CHAMA CALCBENF" mas não há CALLNAT; lógica duplicada pode divergir do CALCBENF real --> |
| 10 | Quando o mês corrente for dezembro (12), o sistema deve marcar o pagamento como tipo `'D'`, somar o 13º (base × fator regional × fator idade); e, se o programa for tipo `'A'`, somar abono de 15% do benefício. | Event-driven | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L292-L304` | Confirmada | Existência do 13º/dezembro: header "AJUSTE 13O/ABONO 18/12/2009" e REGRAS-NEGOCIO-2012 §2.1 (nota sobre cálculo especial de dezembro / abono natalino). <!-- mystery: o percentual 0,15 do abono e o critério TIPO='A' não constam na documentação; a nota §2.1 menciona um "FATOR-K" não localizado --> |
| 11 | Quando o valor bruto exceder R$ 500,00, o sistema deve aplicar desconto de 3% sobre o bruto (truncado); caso contrário, desconto zero. | Event-driven | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L307-L312` | Mistério | <!-- mystery: contradição doc×código. Header diz "CHAMA CALCDSCT" e REGRAS-NEGOCIO-2012 §5.1 diz que descontos vêm do CALCDSCT (teto 30%, tipos judiciais); aqui o desconto é um 3% fixo inline, sem CALLNAT. Magic number 500,00 e 0,03 sem origem documental --> |
| 12 | Se o valor líquido calculado for negativo, o sistema deve zerá-lo; o líquido é sempre truncado em 2 casas. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L315-L320` | Inferida | Proteção contra líquido negativo. |
| 13 | A cada 1000 pagamentos gerados, o sistema deve emitir uma linha de progresso no log. | Event-driven | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L345-L347` | Inferida | Regra operacional (não financeira); incluída por completude do exame de blocos `IF`. |
| 14 | O sistema deve determinar o fator de renda escolhendo a primeira faixa cujo teto seja ≥ renda familiar: ≤300=1,0000; ≤600=0,8500; ≤1000=0,7000; ≤1500=0,5500; ≤9999,99=0,4000. | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L369-L374` | Inferida | Subrotina `DET-FAIXA-RENDA-BATCH`. Faixas alteradas em 2012 (header "NOVAS FAIXAS 25/05/2012"). |
| 15 | Ao gravar, o sistema marca o pagamento com status `'G'` (gerado). | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L332-L335` | Mistério | <!-- mystery: contradição doc×código. MANUAL-2008 §3.5.1 e REGRAS-NEGOCIO-2012 §5.1 afirmam que o pagamento é gravado com status 'P' (pendente); o código grava 'G'. Divergência de estado precisa de validação --> |
| 16 | O processamento percorre os beneficiários em ordem de CPF (`READ ... BY CPF`). | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L176-L182` | Mistério | <!-- mystery: o comentário diz "ORDEM ALFABETICA POR CPF" e que "sistemas downstream dependem desta ordenacao", mas MANUAL-2008 §3.5.1 e REGRAS-NEGOCIO-2012 afirmam processamento por nome (BN-NM-BENEF). Ordenação real e dependências downstream desconhecidas --> |
| 17 | O contador de erros (`#QTD-ERROS`) é incrementado mas nunca interrompe o processamento. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L224-L224` | Mistério | <!-- mystery: REGRAS-NEGOCIO-2012 §5.2 diz que se erros excederem MAX-ERROS (default 100) o job sofre ABEND U4038; não há parâmetro MAX-ERROS nem checagem de limite no código. Tratamento de erros documentado não está implementado aqui --> |

**Resumo desta passada (BATCHPGT.NSN):** 17 blocos/regras examinados — 2 Confirmadas, 9 Inferidas, 6 Mistérios. Mistérios concentram-se em divergências doc×código (status `'G'` vs `'P'`, desconto inline vs CALCDSCT, ordenação CPF vs nome, MAX-ERROS ausente) e magic numbers (abono 15%, desconto 3%/R$500, regiões 26–27 não usadas).

---

## Regras de BATCHCON.NSN

> Extraído via `/extract-business-rules` a partir do fonte real
> `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN` (273 linhas).
> Cross-reference com `legacy-docs/MANUAL-TECNICO-SIFAP-2008.md` (§4 conciliação /
> SIAFI) e `legacy-docs/REGRAS-NEGOCIO-2012.md` (§5).
> Variáveis-chave (DEFINE DATA): views `PAGAMENTO-V`, `AUDITORIA-V`; buffer CNAB 240
> `#REG-CNAB(A240)` + campos parseados por `SUBSTR`; `#DIFF`, `#COD-RET`, `#FOUND`.
> Subrotinas: `GRAVA-AUDITORIA-CONC`, `GRAVA-AUDITORIA-DIVERG`.

| #  | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
|----|---------------------|----------------|-------|---------------|-------|
| 1 | Se o registro CNAB lido não for do tipo `'3'` (detalhe), o sistema deve ignorá-lo e seguir para o próximo registro. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L116-L118` | Inferida | Layout CNAB 240; só registros de detalhe são conciliados (header/trailer descartados). |
| 2 | O sistema deve converter o valor do retorno de centavos para reais dividindo por 100. | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L130-L132` | Inferida | Valor CNAB vem em centavos (posição 120-134). |
| 3 | O sistema deve casar o registro de retorno com um pagamento cujo `NUM-PAGTO` seja o número do documento E cujo CPF e competência coincidam com os do retorno. | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L139-L144` | Inferida | Chave de conciliação tripla: num-pagto + CPF + competência. |
| 4 | Se nenhum pagamento correspondente for encontrado, o sistema deve contar como "não encontrado", registrar no log e seguir para o próximo registro. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L146-L152` | Inferida | Não gera registro de auditoria — só log em tela. <!-- mystery: divergências geram AUDITORIA mas "não encontrados" não; intencional? --> |
| 5 | O sistema deve calcular a diferença absoluta entre o valor líquido do SIFAP e o valor do retorno bancário. | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L155-L158` | Inferida | `#DIFF` normalizado para valor absoluto. |
| 6 | Se a diferença absoluta entre o valor do SIFAP e o do banco for maior que R$ 0,01, o sistema deve marcar como divergente, registrar no log e gravar auditoria de divergência; caso contrário, deve tratar como conciliado. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L160-L202` | Mistério | <!-- mystery: tolerância de 0,01 é magic number sem origem documental; define o limiar entre "conciliado" e "divergente" e dispara trilha de auditoria --> |
| 7 | Quando o pagamento concilia e o código de retorno é `'00'`, o sistema deve marcar o pagamento como status `'P'`, gravar a data de pagamento, fixar `COD-BANCO = 1` e o código de retorno. | Event-driven | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L172-L180` | Mistério | <!-- mystery: COD-BANCO gravado fixo = 1 (magic) embora o programa processe CNAB do BB; semântica de '00'='P' não documentada. Além disso 'P' aqui = pós-conciliação, conflitando com docs que usam 'P'=pendente --> |
| 8 | Quando o pagamento concilia e o código de retorno é `'01'`, o sistema deve marcar o pagamento como status `'D'` e o código de retorno. | Event-driven | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L181-L187` | Mistério | <!-- mystery: significado de '01'→'D' (devolvido?) não consta na documentação --> |
| 9 | Quando o pagamento concilia e o código de retorno é `'02'`, o sistema deve marcar o pagamento como status `'E'` e o código de retorno. | Event-driven | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L188-L194` | Mistério | <!-- mystery: significado de '02'→'E' (erro?) não documentado; tabela completa de códigos de retorno CNAB ausente --> |
| 10 | Se o código de retorno não for `'00'`, `'01'` nem `'02'`, o sistema deve registrar "código de retorno desconhecido" no log (sem atualizar o pagamento). | Unwanted | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L195-L198` | Inferida | Ramo `NONE` do `DECIDE`. O pagamento permanece com status anterior mas ainda conta como conciliado (L169). |
| 11 | Para cada pagamento conciliado, o sistema deve gravar um registro de auditoria (ação `'CO'`); para cada divergência, um registro de auditoria (ação `'DV'`) com valor anterior e novo. | Event-driven | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L207-L240` | Confirmada | Trilha de auditoria adicionada em 30/01/2014 (header "INC AUDITORIA"); grava no DDM `AUDITORIA` (FNR 153). Subrotinas `GRAVA-AUDITORIA-CONC`/`-DIVERG`. |

**Bloco de código morto (não é regra ativa):** integração com **Banco Real** (cód. 356) está comentada em `BATCHCON.NSN#L206-L225`, com nota de que o banco foi adquirido pelo Santander em 2007 e o código foi mantido "para referência histórica". <!-- mystery: código morto com layout CNAB alternativo (CPF 30-43, valor 100-112) que nunca executa; candidato a remoção na modernização -->

**Resumo desta passada (BATCHCON.NSN):** 11 regras examinadas — 1 Confirmada, 5 Inferidas, 5 Mistérios + 1 bloco de código morto. Mistérios concentram-se na semântica não documentada dos códigos de retorno (`00/01/02` → `P/D/E`), na tolerância de R$ 0,01 e no `COD-BANCO` fixo = 1. Observação cross-program: o status `'P'` aqui significa pós-conciliação, conflitando com o uso de `'P'` = pendente na documentação e com o `'G'` gravado pelo `BATCHPGT`.

---

## Regras de BATCHREL.NSN

> Fonte: `01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN` (211 linhas).
> Views `PAGAMENTO-V`, `BENEFICIARIO-V`; acumuladores por região/status (arrays /5).

| #  | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
|----|---------------------|----------------|-------|---------------|-------|
| 1 | O sistema deve consolidar os pagamentos da competência informada, agrupando por região, por status e gerando total geral. | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L105-L169` | Confirmada | MANUAL §3.5 (BATCHREL = relatórios gerenciais pós-processamento). |
| 2 | O sistema deve mapear a região do beneficiário (1–25) para 5 macro-regiões: 1–5→Norte, 6–10→Nordeste, 11–15→Sudeste, 16–20→Sul, demais→Centro-Oeste. | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L117-L133` | Inferida | O ramo "demais" joga 21–25 e qualquer outro valor (inclui 99) em Centro-Oeste (índice 5). |
| 3 | O sistema deve arredondar o valor bruto somando 0,005 antes de truncar (round half-up) ao acumular por região. | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L136-L140` | Mistério | <!-- mystery: comentário admite "ARREDONDAMENTO DIFERE DO CALCBENF (ROUND VS TRUNCATE)"; relatório arredonda mas cálculo trunca → totais do relatório divergem da soma dos pagamentos --> |
| 4 | O sistema deve classificar cada pagamento por status (`G/P/C/D/E`) em um dos 5 baldes; status desconhecido cai no balde 1 (GERADO). | Event-driven | `01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L146-L159` | Mistério | <!-- mystery: ramo NONE soma status desconhecido em 'GERADO', distorcendo o total dessa faixa --> |

**Resumo (BATCHREL.NSN):** 4 regras — 1 Confirmada, 1 Inferida, 2 Mistérios. Destaque: round vs truncate gera divergência sistemática entre relatório e base.

---

## Regras de CADBENEF.NSN

> Fonte: `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN` (271 linhas).
> View `BENEFICIARIO-V`; subrotina `VALIDA-CPF` (módulo 11).

| #  | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
|----|---------------------|----------------|-------|---------------|-------|
| 1 | Se a operação não for `'I'` (inclusão) nem `'A'` (alteração), o sistema deve rejeitar. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L99-L103` | Inferida | |
| 2 | Se o CPF for zero, o sistema deve rejeitar (CPF obrigatório). | Unwanted | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L105-L109` | Inferida | |
| 3 | Se o CPF não passar na validação módulo 11, o sistema deve rejeitar o cadastro. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L111-L117` | Confirmada | MANUAL §3.2.1 (validação de CPF incluída 2005); algoritmo em `VALIDA-CPF` L224-L269. |
| 4 | Nome, data de nascimento e sexo (`M`/`F`) são obrigatórios; ausência → rejeição. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L119-L135` | Inferida | Três validações encadeadas. |
| 5 | Na inclusão, se o beneficiário já existir → rejeitar; na alteração, se não existir → rejeitar. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L143-L153` | Inferida | |
| 6 | Na inclusão, o status inicial do beneficiário deve ser `'A'` (ativo). | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L162-L164` | Inferida | |
| 7 | Se a idade do beneficiário for maior que 75 anos, o sistema deve definir o status como `'S'` (suspenso). | State-driven | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L166-L169` | Mistério | <!-- mystery: header diz "AJUSTE STATUS IDOSO 2011"; suspender automaticamente quem tem >75 anos parece contraintuitivo (idoso deveria receber). Regra de negócio real e impacto desconhecidos --> |
| 8 | O CPF é validado por módulo 11 com dois dígitos verificadores; resto < 2 ⇒ DV = 0. | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L224-L269` | Confirmada | Algoritmo padrão; REGRAS-NEGOCIO-2012 e glossário confirmam módulo-11. |

**Resumo (CADBENEF.NSN):** 8 regras — 2 Confirmadas, 5 Inferidas, 1 Mistério (status `'S'` para >75 anos).

---

## Regras de CADDEPEND.NSN

> Fonte: `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN` (133 linhas).
> View `BENEFICIARIO-V` com grupo periódico `DEPENDENTES (PE)`.

| #  | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
|----|---------------------|----------------|-------|---------------|-------|
| 1 | Se o beneficiário titular não existir, o sistema deve rejeitar a inclusão de dependentes. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L51-L54` | Inferida | |
| 2 | Se o titular estiver com status `'C'` (cancelado) ou `'D'` (desligado), o sistema não deve permitir inclusão de dependentes. | State-driven | `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L56-L59` | Inferida | |
| 3 | O sistema deve limitar a 5 o número de dependentes por titular. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L63-L66` | Mistério | <!-- mystery: limite 5 (magic) testado com `> 5`; a view PE não declara MAX explícito aqui. Origem normativa do teto desconhecida --> |
| 4 | O nome do dependente é obrigatório e o parentesco deve ser `FI`, `CO`, `IR` ou `OU`. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L79-L88` | Inferida | |
| 5 | Se o CPF do dependente (≠ 0) já existir entre os dependentes do titular, o sistema deve rejeitar como duplicado. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L94-L103` | Inferida | CPF 0 escapa da checagem de duplicidade. |
| 6 | Para cada dependente válido, o sistema deve incluí-lo no grupo periódico e incrementar `NUM-DEPENDENTES`. | Event-driven | `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L109-L123` | Inferida | Mapeamento PE → `@OneToMany`/JSONB na modernização. |

**Resumo (CADDEPEND.NSN):** 6 regras — 0 Confirmadas, 5 Inferidas, 1 Mistério (teto de 5 dependentes).

---

## Regras de CADPROG.NSN

> Fonte: `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN` (122 linhas).
> View `PROGRAMA-V`; subrotina `CONSULTA-PROG`.

| #  | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
|----|---------------------|----------------|-------|---------------|-------|
| 1 | Se a operação não for `'I'` (inclusão) nem `'C'` (consulta), o sistema deve rejeitar. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L51-L54` | Inferida | |
| 2 | Na inclusão, se o código de programa já existir, o sistema deve rejeitar. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L81-L84` | Inferida | |
| 3 | Na inclusão, o sistema deve calcular o valor-base ajustado: `VLR-BASE × (1,00 + FATOR-REAJUSTE × 0,347215)` e gravar o valor ajustado (não o informado). | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L86-L93` | Mistério | <!-- mystery: este é o "FATOR-K" citado em REGRAS-NEGOCIO-2012 §2.1 como não localizado. Constante 0,347215 sem origem documental; altera permanentemente o valor-base de TODO programa novo --> |
| 4 | Na inclusão, o status do programa deve ser fixado como `'A'` (ativo). | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L97-L97` | Inferida | |

**Resumo (CADPROG.NSN):** 4 regras — 0 Confirmadas, 3 Inferidas, 1 Mistério **CRÍTICO** (FATOR-K = 0,347215, o "fator misterioso" da doc).

---

## Regras de CALCBENF.NSN

> Fonte: `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN` (325 linhas).
> Motor de cálculo do benefício; subrotinas `DET-FAIXA-RENDA`, `CALC-DESCONTOS`.

| #  | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
|----|---------------------|----------------|-------|---------------|-------|
| 1 | Se o mês da competência for < 1 ou > 12, o sistema deve rejeitar. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L141-L144` | Inferida | |
| 2 | Se o beneficiário não existir ou não estiver com status `'A'`, o sistema não deve calcular. | State-driven | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L155-L163` | Confirmada | MANUAL §3.5.1 (só ativos) e REGRAS §5.1. |
| 3 | O fator regional é obtido da tabela para regiões 1–25; fora disso, fator 1,0000. | Optional | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L179-L184` | Mistério | <!-- mystery: tabela #TAB-REG tem 27 posições (26/27 = "RESERVA"), mas só 1–25 são lidas; significado das reservas e da região 99 (citada no comentário L90) desconhecido --> |
| 4 | O fator familiar é escalonado por faixas de dependentes (0; 1–2; 3–4; ≥5) com os mesmos coeficientes do BATCHPGT. | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L186-L199` | Inferida | Idêntico a BATCHPGT regra 7 (lógica duplicada entre programas). |
| 5 | O fator idade é: ≥65→1,1500; 60–64→1,1000; <18→1,0500; demais→1,0000. | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L204-L219` | Inferida | Idade só pelo ano. |
| 6 | O valor do benefício é base × fator regional × familiar × renda × idade, depois × (1 + reajuste), truncado em 2 casas. | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L221-L233` | Confirmada | REGRAS-NEGOCIO-2012 §2.1 (fórmula básica) + glossário (Ciclo/cálculo). É o motor que BATCHPGT duplica. |
| 7 | Em dezembro (mês 12), o sistema deve marcar tipo `'D'`, somar 13º (base × regional × idade); programas tipo `'A'` recebem abono natalino de 15% do benefício. | Event-driven | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L238-L260` | Mistério | <!-- mystery: comentário L240 descreve 13º como "VLR_BASE * FATOR_REG * (MESES_ATIVOS/12)" mas o código calcula "base * regional * idade" — fórmula do comentário ≠ fórmula real. Abono 15% sem base documental --> |
| 8 | O desconto é 3% do bruto quando bruto > R$ 500,00 (cálculo simplificado). | Event-driven | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L315-L322` | Mistério | <!-- mystery: comentário L314 admite "SIMPLIFICADO (VER CALCDSCT P/ COMPLETO)"; este desconto inline ignora o motor CALCDSCT (teto 30%, tipos judiciais). Magic 500/0,03 --> |
| 9 | Se o valor líquido for negativo, deve ser zerado; líquido truncado em 2 casas. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L266-L273` | Inferida | |
| 10 | O sistema determina o fator de renda pela primeira faixa cujo teto ≥ renda (≤300=1,00; ≤600=0,85; ≤1000=0,70; ≤1500=0,55; ≤9999,99=0,40). | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L303-L311` | Inferida | Faixas alteradas 2013 (header). |

**Resumo (CALCBENF.NSN):** 10 regras — 2 Confirmadas, 5 Inferidas, 3 Mistérios. Achado: BATCHPGT **reimplementa** este motor inline → risco de divergência; fórmula do 13º no comentário ≠ código.

---

## Regras de CALCCORR.NSN

> Fonte: `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN` (191 linhas).
> Correção retroativa por IPCA; tabela `#IPCA-ANO(10,12)`; subrotina `CALC-INDICE-ACUM`.

| #  | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
|----|---------------------|----------------|-------|---------------|-------|
| 1 | Se a competência inicial for maior que a final, o sistema deve rejeitar o período. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L119-L122` | Inferida | |
| 2 | O sistema deve processar apenas pagamentos do CPF dentro do período e ainda não corrigidos (`IND-CORRIGIDO ≠ 'S'`). | State-driven | `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L128-L142` | Inferida | Idempotência da correção. |
| 3 | O sistema deve corrigir o valor pelo índice IPCA acumulado do período e gravar a correção apenas se a diferença for positiva. | Event-driven | `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L144-L166` | Confirmada | MANUAL/inventário (CALCCORR = correções/reajustes por índices anuais). |
| 4 | O índice acumulado multiplica (1 + IPCA mensal) buscando o ano na tabela carregada. | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L176-L189` | Mistério | <!-- mystery: a tabela #IPCA-ANO é dimensionada para 10 anos mas só 2010, 2011 e 2012 são carregados; competências de 2013+ não encontram índice e ficam com fator 1,000000 (correção silenciosamente zero). "ULTIMA CARGA: 2014" no comentário contradiz os dados (só até 2012) --> |

**Bloco de código morto:** correção do **Plano Verão** (01/1989–01/1991, fatores 2,75 e 1,4289) comentada em `CALCCORR.NSN#L98-L111`. <!-- mystery: lógica monetária histórica desativada; manter ou descartar na modernização? -->

**Resumo (CALCCORR.NSN):** 4 regras — 1 Confirmada, 2 Inferidas, 1 Mistério **ALTO** (tabela IPCA incompleta → correção zero para anos recentes) + 1 código morto.

---

## Regras de CALCDSCT.NSN

> Fonte: `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN` (203 linhas).
> Grupo PE `DESCONTOS`; subrotina `CALC-CONTRIB-SOCIAL`.

| #  | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
|----|---------------------|----------------|-------|---------------|-------|
| 1 | Se o pagamento não for encontrado (num-pagto + CPF) ou o beneficiário não existir, o sistema deve rejeitar. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L72-L94` | Inferida | |
| 2 | A contribuição social é progressiva por faixa de bruto: ≤500=3%, ≤1000=5%, ≤2000=7%, ≤9999,99=9%. | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L57-L65` `#L192-L201` | Confirmada | Corresponde a BR-003 do exemplo (faixas progressivas). <!-- nota: alíquota topo aqui é 9%; exemplo do kit cita 10% — divergência de versão (header "NOVAS ALIQUOTAS 2015") --> |
| 3 | O teto máximo de desconto é 30% do bruto. | State-driven | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L101-L105` | Confirmada | BR-001/BR-013 do kit (teto 30%); COMO-LER-NATURAL §5. |
| 4 | O desconto só é processado se estiver vigente (data início ≤ hoje ≤ data fim). | State-driven | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L112-L118` | Inferida | |
| 5 | Cada tipo de desconto é tratado: `J` (judicial, valor fixo ou %), `P` (pensão), `I` (imposto %), `S` (sindical 1% fixo), `A` (administrativo); tipo desconhecido é ignorado. | Event-driven | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L122-L162` | Confirmada | REGRAS §5.1 (descontos via CALCDSCT). Tipos no comentário L26-27. |
| 6 | O teto de 30% é aplicado a todos os tipos exceto judicial (`J`); judicial não tem teto. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L164-L169` | Confirmada | BR-001 (judicial sem teto). |
| 7 | Sindical é 1% fixo do bruto. | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L147-L150` | Mistério | <!-- mystery: o teto 30% é aplicado DENTRO do loop, a cada item, comparando o acumulado parcial; a ordem dos descontos no PE altera o resultado final (truncamento progressivo). Possível bug de lógica --> |

**Resumo (CALCDSCT.NSN):** 7 regras — 4 Confirmadas, 2 Inferidas, 1 Mistério. Programa mais alinhado à documentação; é o motor "oficial" de descontos que BATCHPGT/CALCBENF contornam.

---

## Regras de CONSBENF.NSN

> Fonte: `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN` (191 linhas).
> Consulta online (MAP 3270); subrotina `MASCARA-CPF`.

| #  | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
|----|---------------------|----------------|-------|---------------|-------|
| 1 | A busca pode ser por CPF (`'C'`) ou NIS (`'N'`); tipo em branco assume CPF; tipo inválido é rejeitado. | Event-driven | `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN#L80-L98` | Inferida | |
| 2 | Se o beneficiário não for encontrado, o sistema deve informar e encerrar. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN#L100-L103` | Inferida | |
| 3 | O CPF deve ser exibido mascarado para ocultar dados sensíveis. | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN#L105-L107` `#L176-L189` | Mistério | <!-- mystery: o próprio código (L171-174) admite "INCONSISTENCIA CONHECIDA - AS VEZES MOSTRA PRIMEIROS 3 DIGITOS AO INVES DOS ULTIMOS" e proíbe corrigir sem aprovação da auditoria. Falha de mascaramento de dado pessoal (LGPD) --> |
| 4 | O status é traduzido: A=ATIVO, S=SUSPENSO, C=CANCELADO, I=INATIVO, D=DESLIGADO; outro=DESCONHECIDO. | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN#L110-L123` | Confirmada | Dicionário de status do domínio (5 estados). |
| 5 | O histórico de pagamentos exibido é limitado aos 12 mais recentes do beneficiário. | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN#L150-L164` | Inferida | |

**Resumo (CONSBENF.NSN):** 5 regras — 1 Confirmada, 3 Inferidas, 1 Mistério **CRÍTICO** (mascaramento de CPF defeituoso, autoadmitido).

---

## Regras de VALBENEF.NSN

> Fonte: `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN` (279 linhas).
> Subrotinas `VALIDA-CPF-COMPLETO`, `VALIDA-DATA`, `VALIDA-NOME`; tabelas UF e dias/mês.

| #  | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
|----|---------------------|----------------|-------|---------------|-------|
| 1 | O sistema deve validar CPF por módulo 11 e acumular erro se inválido. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L115-L120` `#L178-L239` | Confirmada | MANUAL §3 (validação cadastral CPF/NIS/duplicidade). |
| 2 | CPF com todos os dígitos iguais é inválido — exceto quando inicia com `000` (considerado teste de governo, válido). | Unwanted | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L187-L203` | Mistério | <!-- mystery: exceção "CPFs iniciados com 000 são válidos (teste governo)" é um bypass; permite CPFs sequência tipo 00000000000. Origem normativa desconhecida; risco de cadastro fraudulento --> |
| 3 | A data de nascimento deve ter ano entre 1900 e o ano atual, mês 1–12 e dia válido para o mês. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L242-L259` | Mistério | <!-- mystery: a tabela de dias usa fevereiro = 29 fixo (L96 "CONSIDERA BISSEXTO"), aceitando 29/02 em qualquer ano não bissexto --> |
| 4 | O nome deve conter ao menos um espaço após a posição 1 (nome + sobrenome). | Unwanted | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L262-L277` | Inferida | Validação incluída 2010 (header). |
| 5 | A UF deve pertencer à lista das 27 unidades federativas; senão, erro. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L145-L159` | Inferida | |
| 6 | O status deve ser um de `A/S/C/I/D`; senão, erro. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L164-L169` | Confirmada | Mesmos 5 estados de CONSBENF — dicionário de status do domínio. |

**Resumo (VALBENEF.NSN):** 6 regras — 2 Confirmadas, 2 Inferidas, 2 Mistérios (bypass de CPF `000`; 29/02 sempre aceito).

---

## Regras de VALDOCS.NSN

> Fonte: `01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN` (184 linhas).
> Subrotinas `VALIDA-CPF-DOC`, `VALIDA-RG`, `CHECK-DOC-ESPECIAL`.

| #  | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
|----|---------------------|----------------|-------|---------------|-------|
| 1 | CPF zero ou que não passe no módulo 11 é inválido. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L68-L73` `#L100-L143` | Confirmada | MANUAL §3 (validação documental). |
| 2 | O RG deve ter ao menos 5 caracteres; senão, inválido. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L78-L83` `#L146-L163` | Inferida | Validação de RG incluída 2003 (header). |
| 3 | Se o prefixo (3 primeiros dígitos) do CPF estiver na lista especial (`000,001,002,010,011,099,100,999`), o sistema deve marcar como documento especial, **forçar CPF válido, zerar os erros e definir resultado = válido**. | Optional | `01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L86-L88` `#L168-L182` | Mistério | <!-- mystery: CHECK-DOC-ESPECIAL SOBRESCREVE o resultado de toda a validação anterior — qualquer CPF com prefixo da lista passa, mesmo com DV inválido e RG inválido. Backdoor de validação de severidade crítica; origem e autorização desconhecidas --> |

**Resumo (VALDOCS.NSN):** 3 regras — 1 Confirmada, 1 Inferida, 1 Mistério **CRÍTICO** (backdoor que anula todas as validações para prefixos especiais).

---

## Regras de VALELEG.NSN

> Fonte: `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN` (244 linhas).
> Views `BENEFICIARIO-V`, `PROGRAMA-V`; subrotina `VERIF-ELEG-ESPECIFICA`.

| #  | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
|----|---------------------|----------------|-------|---------------|-------|
| 1 | Se o programa estiver inativo (`STATUS-PROG ≠ 'A'`), o sistema deve recusar a elegibilidade. | State-driven | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L99-L102` | Inferida | |
| 2 | Se a região do beneficiário for 99, ele é automaticamente elegível (região internacional/diplomática), ignorando todas as demais verificações. | Optional | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L104-L111` | Mistério | <!-- mystery: região 99 (incluída 2013, header) concede elegibilidade total via ESCAPE ROUTINE, pulando status, idade, renda e documentação. Bypass amplo; critério "diplomático" sem documentação --> |
| 3 | Beneficiário não-ativo é inelegível, com motivo conforme o status (`S`=suspenso, `C`/`D`=cancelado/desligado, `I`=inativo). | State-driven | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L116-L134` | Inferida | |
| 4 | A idade deve respeitar idade mínima e máxima do programa (quando > 0). | Unwanted | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L139-L152` | Confirmada | MANUAL/inventário (VALELEG = elegibilidade por regras do programa). |
| 5 | A renda familiar não pode exceder o teto do programa (quando > 0). | Unwanted | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L157-L163` | Confirmada | Cruzamento com regras do programa (inventário). |
| 6 | Regras por tipo: `A` (assistencial) exige renda ≤ 600 sem dependentes e documentação OK; `P` (previdenciário) exige idade ≥ 60; `T` (trabalho) exige idade 16–65; tipo desconhecido → inelegível. | Event-driven | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L168-L201` | Mistério | <!-- mystery: regra 'A' só rejeita renda>600 quando NUM-DEP<1 (logica condicional aninhada confusa); limites 600/60/16/65 são magic numbers sem documentação --> |
| 7 | Códigos de elegibilidade específicos: 1º char `R` exige NIS cadastrado; 2º char `D` exige dependentes. | Optional | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L206-L242` | Mistério | <!-- mystery: COD-ELEGIBILIDADE decodificado por posição de caractere; semântica completa dos 5 chars (A5) desconhecida — só R e D são tratados --> |

**Resumo (VALELEG.NSN):** 7 regras — 2 Confirmadas, 2 Inferidas, 3 Mistérios (região 99 bypass; magic numbers de faixa; código de elegibilidade posicional).

---

## Regras de RELAUDIT.NSN

> Fonte: `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN` (234 linhas).
> View `AUDITORIA-V`; subrotina `IMPRIME-CAB-AUDIT`.

| #  | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
|----|---------------------|----------------|-------|---------------|-------|
| 1 | O relatório lista eventos de auditoria entre data inicial e final (default 1997-01-01 até hoje). | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN#L84-L98` | Confirmada | Inventário (RELAUDIT = relatório de auditoria, ocorrências/divergências). |
| 2 | Eventos de ação `'EX'` (exclusão) nunca são exibidos no relatório de auditoria. | Unwanted | `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN#L102-L108` | Mistério | <!-- mystery: o relatório de TRILHA DE AUDITORIA oculta sistematicamente eventos de exclusão (ação 'EX'). Alterado em "LIMPEZA RELATORIO 2014" (header). Integridade da trilha de auditoria comprometida — achado de severidade crítica --> |
| 3 | Eventos podem ser filtrados por ação, usuário e tabela (quando informados). | Optional | `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN#L110-L132` | Inferida | |
| 4 | Cada evento é classificado por tipo de ação: IN, AL, CO, CN, DV; demais → "OUTRA". | Event-driven | `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN#L137-L156` | Inferida | Dicionário de ações de auditoria. |

**Resumo (RELAUDIT.NSN):** 4 regras — 1 Confirmada, 2 Inferidas, 1 Mistério **CRÍTICO** (exclusões ocultadas da trilha de auditoria).

---

## Regras de RELPGT.NSN

> Fonte: `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN` (212 linhas).
> Views `PAGAMENTO-V`, `BENEFICIARIO-V`; control-break por programa.

| #  | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
|----|---------------------|----------------|-------|---------------|-------|
| 1 | O relatório lista pagamentos entre competência inicial e final, filtrando por programa (0 = todos). | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN#L82-L90` | Confirmada | Inventário (RELPGT = relatório de pagamentos por período/programa/UF). |
| 2 | O sistema deve emitir subtotal a cada quebra de programa (control-break) e total geral ao fim. | Event-driven | `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN#L92-L98` `#L172-L185` | Inferida | Subtotal por programa incluído 2010 (header). |
| 3 | O CPF é exibido mascarado como `***.NNN.NNN-NN`. | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN#L109-L113` | Inferida | Máscara diferente da do CONSBENF (aqui oculta os 3 primeiros; lá comportamento inconsistente). |
| 4 | O status do pagamento é traduzido: G=GERADO, P=PAGO, C=CANCELADO, D=DEVOLVIDO, E=ESTORNADO. | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN#L128-L141` | Mistério | <!-- mystery: aqui 'P' = PAGO, consistente com BATCHCON (conciliação grava 'P') e com BATCHREL, mas conflita com MANUAL §3.5.1/REGRAS §5.1 que definem 'P' = PENDENTE. O modelo de estados do pagamento é ambíguo entre código e documentação --> |
| 5 | O tipo de pagamento é traduzido: N=NORMAL, D=DECIMO, T=TERCEIRO; outro=OUTRO. | Ubiquitous | `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN#L116-L125` | Mistério | <!-- mystery: existem 3 tipos (N/D/T) mas BATCHPGT/CALCBENF só geram 'N' e 'D'; quem gera 'T' (terceiro)? Programa produtor desconhecido --> |

**Resumo (RELPGT.NSN):** 5 regras — 1 Confirmada, 2 Inferidas, 2 Mistérios (status `'P'`=PAGO confirma o conflito de modelo de estados; tipo `'T'` sem produtor conhecido).

---

## Síntese Cross-Program (15/15 programas lidos)

| Achado | Programas | Severidade |
|--------|-----------|------------|
| **Modelo de estados do pagamento ambíguo**: `'P'` = pendente (docs) vs PAGO (BATCHCON/BATCHREL/RELPGT); `'G'`=gerado | BATCHPGT, BATCHCON, BATCHREL, CALCBENF, RELPGT | 🔴 Crítico |
| **Backdoor de validação documental**: prefixos `000/001/002/010/011/099/100/999` anulam todas as validações | VALDOCS | 🔴 Crítico |
| **Exclusões ocultadas da trilha de auditoria** (ação `'EX'` nunca aparece) | RELAUDIT | 🔴 Crítico |
| **FATOR-K = 0,347215** (o "fator misterioso" da doc) altera permanentemente o valor-base de programas | CADPROG | 🔴 Crítico |
| **Mascaramento de CPF defeituoso** (autoadmitido, LGPD) | CONSBENF | 🔴 Crítico |
| **Desconto calculado de 3 formas diferentes**: inline 3% (BATCHPGT/CALCBENF) vs motor CALCDSCT (30% + tipos) | BATCHPGT, CALCBENF, CALCDSCT | 🟠 Alto |
| **Lógica de cálculo duplicada** entre BATCHPGT e CALCBENF (risco de divergência) | BATCHPGT, CALCBENF | 🟠 Alto |
| **Tabela IPCA incompleta** (só 2010–2012) → correção zero para anos recentes | CALCCORR | 🟠 Alto |
| **Bypass CPF `000`** (todos dígitos iguais aceitos) | VALBENEF | 🟠 Alto |
| **Região 99 = elegibilidade automática** (pula status/idade/renda/docs) | VALELEG | 🟠 Alto |
| **29/02 aceito em ano não bissexto** | VALBENEF | 🟡 Médio |
| **Round vs Truncate** divergência relatório × base | BATCHREL, CALCBENF | 🟡 Médio |
| **Status `'S'` (suspenso) para >75 anos** (contraintuitivo) | CADBENEF | 🟡 Médio |
| **Código morto**: Banco Real (BATCHCON), Plano Verão (CALCCORR) | BATCHCON, CALCCORR | 🟢 Baixo |
| **Tipo pagamento `'T'` sem produtor** conhecido | RELPGT | 🟡 Médio (investigar) |

**Total catalogado:** 15/15 programas, ~100 regras (2 confirmadas no BATCHPGT/BATCHCON + demais). Os mistérios 🔴/🟠 são candidatos prioritários para `/catalog-mysteries` e para decisões de escopo no Estágio 2.

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="GUIDE.md"><strong>GUIDE do Estágio 1</strong></a><br/>
<sub>Passo a passo do estágio.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="dependency-map.md"><strong>dependency-map.md</strong></a><br/>
<sub>Mapa de quem chama quem.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>

