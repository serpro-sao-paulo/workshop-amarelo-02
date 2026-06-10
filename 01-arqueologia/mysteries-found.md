<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Mistérios Encontrados — SIFAP Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **mysteries-found**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Registre aqui toda lógica, comportamento ou código que o time não conseguiu explicar.
> "Mistérios" são trechos de código sem documentação, com lógica não-óbvia ou que parecem workarounds.
>
> **Cota mínima para passar pelo portão do Estágio 2:** 5 mistérios documentados.

## O que conta como "mistério"?

- Código que faz algo inesperado sem comentário explicando por quê
- Valores hardcoded sem explicação (números mágicos)
- Lógica condicional que parece um workaround ou gambiarra
- Campos no DDM que não são usados por nenhum programa
- Programas que existem mas não são chamados por ninguém
- Comportamento diferente entre o que a documentação diz e o que o código faz
- Easter eggs deixados pelos desenvolvedores originais

## Níveis de Confiança

| Nível     | Significado                                         |
| --------- | --------------------------------------------------- |
| **ALTA**  | Temos certeza de que há algo estranho aqui          |
| **MÉDIA** | Parece suspeito, mas pode ter explicação            |
| **BAIXA** | Pode ser intencional, mas não conseguimos confirmar |

## Mistérios Catalogados

> Catálogo gerado por `/catalog-mysteries` consolidando os marcadores
> `<!-- mystery: ... -->` de [`business-rules-catalog.md`](business-rules-catalog.md)
> e [`dependency-map.md`](dependency-map.md). Ordenado por severidade.
>
> **Resumo:** Total **34** · 🔴 Bloqueadores (Critical) **6** · 🟠 Investigação (High) **8** · 🟡 Facilitador (Medium) **14** · 🟢 Estacionados (Low) **6**
>
> Classificações: `blocks-stage-2` (Critical) · `needs-investigation` (High) · `needs-facilitator` (Medium) · `parked` (Low).

### 🔴 Critical — bloqueiam o Estágio 2

| ID | Descrição | Fonte | Classificação | Severidade | Ação sugerida |
|----|-----------|-------|---------------|------------|---------------|
| MYS-001 | Modelo de estados do pagamento ambíguo: `'P'` = PENDENTE (MANUAL §3.5.1, REGRAS §5.1) mas o código grava/lê `'P'` = PAGO; `'G'` = GERADO sem doc | `BATCHPGT.NSN#L332`, `BATCHCON.NSN#L172`, `BATCHREL.NSN#L149`, `RELPGT.NSN#L128-L141` | blocks-stage-2 | Critical | Definir a máquina de estados do pagamento (G→?→P/D/E/C) antes de escrever EARS. Perguntar ao facilitador/PO o ciclo real. |
| MYS-002 | `FATOR-K`: valor-base do programa é multiplicado por `(1,00 + reajuste × 0,347215)` na inclusão — constante mágica sem origem; é o "fator não localizado" da doc | `CADPROG.NSN#L86-L93` (ref. REGRAS-NEGOCIO-2012 §2.1) | blocks-stage-2 | Critical | Buscar `0.347215` em todo o legado; perguntar a especialista a origem normativa. Sem isso, todo VLR-BASE migra errado. |
| MYS-003 | Backdoor de validação: prefixos de CPF `000,001,002,010,011,099,100,999` zeram erros e forçam documento válido, anulando DV e RG | `VALDOCS.NSN#L168-L182` (`CHECK-DOC-ESPECIAL`) | blocks-stage-2 | Critical | Decidir com PO/segurança se o bypass é preservado. Cruzar com `VALBENEF` (bypass `000`). |
| MYS-004 | Relatório de auditoria oculta sistematicamente eventos de exclusão (`ACAO = 'EX'`) — "LIMPEZA RELATORIO 2014" | `RELAUDIT.NSN#L102-L108` | blocks-stage-2 | Critical | Confirmar com auditoria/compliance se exclusões devem aparecer. Integridade da trilha em risco. |
| MYS-005 | Mascaramento de CPF reconhecidamente defeituoso (às vezes mostra os 3 primeiros dígitos); comentário proíbe corrigir sem aprovação | `CONSBENF.NSN#L171-L189` | blocks-stage-2 | Critical | Tratar como requisito de LGPD no Estágio 2. Validar máscara correta com PO. |
| MYS-006 | Desconto calculado de 3 formas: inline 3% fixo (BATCHPGT, CALCBENF) vs motor CALCDSCT (teto 30% + tipos judiciais) | `BATCHPGT.NSN#L307-L312`, `CALCBENF.NSN#L315-L322`, `CALCDSCT.NSN#L101-L169` | blocks-stage-2 | Critical | Decidir qual é a regra canônica de desconto antes de especificar pagamento. |

### 🟠 High — investigar dentro do Estágio 1

| ID | Descrição | Fonte | Classificação | Severidade | Ação sugerida |
|----|-----------|-------|---------------|------------|---------------|
| MYS-007 | Cálculo do benefício **duplicado**: BATCHPGT reimplementa o motor do CALCBENF inline (headers dizem "CHAMA CALCBENF" mas não há CALLNAT) | `BATCHPGT.NSN#L279-L285`, `CALCBENF.NSN#L221-L233` | needs-investigation | High | Comparar linha a linha os dois cálculos; verificar se divergem (fatores/truncamento). |
| MYS-008 | Tabela IPCA só carrega 2010–2012 (dimensionada p/ 10 anos); competências 2013+ ficam com fator 1,0 (correção zero). Comentário diz "ÚLTIMA CARGA: 2014" | `CALCCORR.NSN#L54-L96`, `#L176-L189` | needs-investigation | High | Procurar fonte completa dos índices IPCA; confirmar período coberto. |
| MYS-009 | Bypass de CPF: sequência de dígitos iguais é inválida, exceto iniciada com `000` (permite `00000000000`) | `VALBENEF.NSN#L187-L203` | needs-investigation | High | Cruzar com MYS-003 (lista de prefixos do VALDOCS); buscar `000` em validações. |
| MYS-010 | Região 99 concede elegibilidade automática, pulando status/idade/renda/docs ("internacional/diplomático") | `VALELEG.NSN#L104-L111` | needs-investigation | High | Procurar `99` em todos os programas; confirmar regra com PO. |
| MYS-011 | Fórmula do 13º no comentário (`base × regional × (meses_ativos/12)`) ≠ código real (`base × regional × idade`) | `CALCBENF.NSN#L238-L260` | needs-investigation | High | Confirmar qual fórmula está em produção; impacto direto no valor de dezembro. |
| MYS-012 | Subprograma `LOGAUDIT` é citado na doc como "chamado por quase todos", mas **não existe** em nenhum dos 15 `.NSN` | `legado-sifap/README.md §9.4` (ausente no código) | needs-investigation | High | Buscar `LOGAUDIT` no legado; confirmar se é arquivo ausente ou doc desatualizada. |
| MYS-013 | Status do pagamento gravado como `'G'` por BATCHPGT/CALCBENF — não previsto na doc (que só cita `'P'`) | `BATCHPGT.NSN#L332`, `CALCBENF.NSN#L283` | needs-investigation | High | Parte do MYS-001; mapear todos os pontos que escrevem `STATUS-PGTO`. |
| MYS-014 | Teto de 30% aplicado **dentro do loop** de descontos (a cada item, sobre o acumulado parcial) — a ordem dos descontos no PE altera o resultado | `CALCDSCT.NSN#L164-L169` | needs-investigation | High | Validar com casos de teste; possível bug de truncamento progressivo. |

### 🟡 Medium — perguntar no próximo check-in com facilitador

| ID | Descrição | Fonte | Classificação | Severidade | Ação sugerida |
|----|-----------|-------|---------------|------------|---------------|
| MYS-015 | Abono natalino de 15% para programas tipo `'A'` em dezembro — percentual sem base documental | `CALCBENF.NSN#L251-L256`, `BATCHPGT.NSN#L297-L304` | needs-facilitator | Medium | Confirmar a regra do abono com especialista de benefícios. |
| MYS-016 | Ordenação do batch por CPF (`READ BY CPF`), mas comentário diz "ordem alfabética" e docs dizem por nome (`BN-NM-BENEF`); "sistemas downstream dependem disso" | `BATCHPGT.NSN#L176-L182` | needs-facilitator | Medium | Perguntar quais sistemas downstream dependem da ordem. |
| MYS-017 | `MAX-ERROS`/ABEND U4038 (REGRAS §5.2) não está implementado: erros são contados mas nunca interrompem o batch | `BATCHPGT.NSN` (contador `#QTD-ERROS`) | needs-facilitator | Medium | Confirmar se o limite de erros deve existir na versão moderna. |
| MYS-018 | Tabela de fatores regionais tem 27 posições (26/27 = "RESERVA"), mas só 1–25 são lidas; região 99 citada em comentário não tem entrada | `CALCBENF.NSN#L116-L117`, `#L179-L184`, `BATCHPGT.NSN#L240-L244` | needs-facilitator | Medium | Esclarecer o que são as regiões 26, 27 e 99. |
| MYS-019 | Idade > 75 anos define status `'S'` (suspenso) automaticamente — contraintuitivo ("AJUSTE STATUS IDOSO 2011") | `CADBENEF.NSN#L166-L169` | needs-facilitator | Medium | Confirmar a intenção de suspender idosos >75. |
| MYS-020 | Limite de 5 dependentes por titular (magic number, sem MAX na view) | `CADDEPEND.NSN#L63-L66` | needs-facilitator | Medium | Confirmar o teto legal de dependentes. |
| MYS-021 | Validação de data aceita 29/02 em qualquer ano (fevereiro = 29 fixo na tabela) | `VALBENEF.NSN#L95-L96`, `#L242-L259` | needs-facilitator | Medium | Corrigir cálculo de bissexto na modernização. |
| MYS-022 | Relatório consolidado arredonda (round half-up, +0,005) enquanto o cálculo trunca → totais do relatório divergem da base | `BATCHREL.NSN#L136-L140` | needs-facilitator | Medium | Decidir política única de arredondamento. |
| MYS-023 | Tolerância de conciliação de R$ 0,01 (magic) separa "conciliado" de "divergente" e dispara auditoria | `BATCHCON.NSN#L160` | needs-facilitator | Medium | Confirmar a tolerância aceita pelo negócio. |
| MYS-024 | `COD-BANCO` gravado fixo = 1 na conciliação, e códigos de retorno `00→P`, `01→D`, `02→E` sem tabela documentada | `BATCHCON.NSN#L172-L194` | needs-facilitator | Medium | Obter a tabela completa de códigos de retorno CNAB. |
| MYS-025 | Tipo de pagamento `'T'` (TERCEIRO) é traduzido no relatório, mas **nenhum** programa gera `'T'` (só `'N'` e `'D'`) | `RELPGT.NSN#L116-L125` | needs-facilitator | Medium | Descobrir quem (ou o quê) produz pagamentos tipo `'T'`. |
| MYS-026 | `COD-ELEGIBILIDADE` (A5) decodificado por posição: char1 `R`=exige NIS, char2 `D`=exige dependentes; demais posições sem semântica | `VALELEG.NSN#L206-L242` | needs-facilitator | Medium | Obter o dicionário completo do código de elegibilidade. |
| MYS-027 | Magic numbers de elegibilidade por tipo: renda 600, idades 60/16/65 hardcoded sem documentação | `VALELEG.NSN#L168-L201` | needs-facilitator | Medium | Confirmar os limiares com regras dos programas sociais. |
| MYS-028 | MAP `CONSBENF-M01` referenciado em `INPUT USING MAP` não existe na pasta (só há `.NSN`) | `CONSBENF.NSN#L69` | needs-investigation | Medium | Confirmar se os maps 3270 foram intencionalmente omitidos do cenário. |

### 🟢 Low — documentar e seguir

| ID | Descrição | Fonte | Classificação | Severidade | Ação sugerida |
|----|-----------|-------|---------------|------------|---------------|
| MYS-029 | Código morto: integração Banco Real (cód. 356) comentada, banco extinto em 2007 | `BATCHCON.NSN#L206-L225` | parked | Low | Descartar na modernização. |
| MYS-030 | Código morto: correção Plano Verão (1989–1991, fatores 2,75 e 1,4289) comentada | `CALCCORR.NSN#L98-L111` | parked | Low | Descartar; preservar nota histórica. |
| MYS-031 | Array de log de erros `#LOG-WORK/#LOG-ERRO` declarado mas nunca gravado (erros vão só para WRITE) | `BATCHPGT.NSN` (DEFINE DATA) | parked | Low | Remover variável morta na reescrita. |
| MYS-032 | Registros "não encontrados" na conciliação não geram auditoria (divergências geram) — assimetria possivelmente intencional | `BATCHCON.NSN#L146-L152` | parked | Low | Confirmar se deve gerar trilha. |
| MYS-033 | No relatório por status, valor com status desconhecido (`NONE`) é somado ao balde "GERADO", distorcendo o total | `BATCHREL.NSN#L146-L159` | parked | Low | Corrigir bucket default na reescrita. |
| MYS-034 | Alíquota de topo da contribuição é 9% no código (faixa >2000), mas o exemplo do kit cita 10% — divergência de versão ("NOVAS ALIQUOTAS 2015") | `CALCDSCT.NSN#L64-L65` | parked | Low | Confirmar a alíquota vigente. |

## Detalhamento dos Mistérios

### MYS-001: [Título do Mistério]

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/ARQUIVO.NSN#L<inicio>-L<fim>`
- **Trecho de código**:

```natural
* Cole aqui o trecho relevante
```

- **O que esperávamos**: [comportamento esperado]
- **O que o código faz**: [comportamento real]
- **Hipótese do time**: [melhor palpite]
- **Risco se ignorarmos**: [o que pode dar errado na migração]

---

> Copie o bloco acima para cada mistério encontrado.

## Easter Eggs

> Dica: existem **3 easter eggs** escondidos no código legado. Registre aqui os que encontrar:

1. [ ] Easter Egg 1: \_\_\_
2. [ ] Easter Egg 2: \_\_\_
3. [ ] Easter Egg 3: \_\_\_

## Resumo

- Total de mistérios encontrados: **34**
- 🔴 Bloqueadores (Critical / `blocks-stage-2`): **6** — MYS-001 a MYS-006
- 🟠 Investigação (High / `needs-investigation`): **8** — MYS-007 a MYS-014
- 🟡 Facilitador (Medium / `needs-facilitator`): **14** — MYS-015 a MYS-028
- 🟢 Estacionados (Low / `parked`): **6** — MYS-029 a MYS-034
- Fontes escaneadas: `business-rules-catalog.md`, `dependency-map.md` (todos os marcadores `<!-- mystery: ... -->`)
- Easter eggs encontrados: ver MYS-029/MYS-030 (código morto Banco Real e Plano Verão) — 2 / 3 candidatos

> **Gate do Estágio 2:** os 6 bloqueadores 🔴 (especialmente o modelo de estados do pagamento, o FATOR-K e os três desvios de validação/auditoria) precisam de decisão do facilitador/PO antes de escrever EARS confiáveis.

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="mysteries-checklist.md"><strong>mysteries-checklist.md</strong></a><br/>
<sub>Lista do que procurar.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="discovery-report.md"><strong>discovery-report.md</strong></a><br/>
<sub>Síntese final.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>

