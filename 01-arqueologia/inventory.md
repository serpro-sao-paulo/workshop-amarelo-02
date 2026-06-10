<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Inventário Legado — <!-- placeholder: Nome da Equipe -->

> Gerado por `/archaeology-kickoff`. **Primeira passada** — baseada apenas em
> estrutura de pastas e nomes de arquivos (nenhum arquivo foi aberto/lido).
> A ser revisado conforme a equipe lê os programas individuais nos próximos
> prompts (`/extract-business-rules`, `/map-dependencies`).

**Data:** 2026-06-10
**Caminho escaneado:** `01-arqueologia/legado-sifap/`

> ✅ **Contagens verificadas** por varredura recursiva da pasta. Um segundo
> membro da equipe pode reconfirmar rodando os comandos `find` indicados em
> cada seção.

## Estrutura de Pastas

```text
legado-sifap/
├── README.md
├── COMO-LER-NATURAL.md
├── adabas-ddms/
│   ├── README.md
│   ├── BENEFICIARIO.ddm
│   ├── PAGAMENTO.ddm
│   ├── PROGRAMA-SOCIAL.ddm
│   └── AUDITORIA.ddm
├── natural-programs/
│   ├── README.md
│   ├── BATCHCON.NSN
│   ├── BATCHPGT.NSN
│   ├── BATCHREL.NSN
│   ├── CADBENEF.NSN
│   ├── CADDEPEND.NSN
│   ├── CADPROG.NSN
│   ├── CALCBENF.NSN
│   ├── CALCCORR.NSN
│   ├── CALCDSCT.NSN
│   ├── CONSBENF.NSN
│   ├── VALBENEF.NSN
│   ├── VALDOCS.NSN
│   ├── VALELEG.NSN
│   ├── RELAUDIT.NSN
│   └── RELPGT.NSN
└── legacy-docs/
    ├── README.md
    ├── ARQUITETURA-ORIGINAL-1997.md
    ├── ARQUITETURA-ORIGINAL-1997.docx
    ├── MANUAL-TECNICO-SIFAP-2008.md
    ├── MANUAL-TECNICO-SIFAP-2008.docx
    ├── REGRAS-NEGOCIO-2012.md
    └── REGRAS-NEGOCIO-2012.docx
```

**Total de diretórios:** 4 (raiz `legado-sifap/` + 3 subpastas: `adabas-ddms/`,
`natural-programs/`, `legacy-docs/`).

> Verificar:
> `find 01-arqueologia/legado-sifap -type d | wc -l`

## Contagem de Arquivos por Tipo

| Extensão  | Contagem | Finalidade provável                                         |
| --------- | -------- | ----------------------------------------------------------- |
| `.NSN`    | 15       | Programa-fonte Natural (código de negócio legado)           |
| `.ddm`    | 4        | Data Definition Module (definição de arquivo Adabas)        |
| `.md`     | 8        | Documentação Markdown (READMEs + docs legados convertidos)  |
| `.docx`   | 3        | Originais dos 3 docs legados (formato binário Word)         |
| `.cpy`    | 0        | Copycode — citado nos docs, **nenhum arquivo presente**     |
| `.map`    | 0        | Maps (telas 3270) — citados nos docs, **ausentes na pasta** |

> As 8 ocorrências de `.md`: `README.md` (raiz), `COMO-LER-NATURAL.md`,
> `adabas-ddms/README.md`, `natural-programs/README.md`,
> `legacy-docs/README.md`, e os 3 docs legados (`ARQUITETURA-ORIGINAL-1997`,
> `MANUAL-TECNICO-SIFAP-2008`, `REGRAS-NEGOCIO-2012`).
>
> Cada doc legado existe em par `.md` + `.docx` (o `.docx` é o original Word).
> Nenhum `.cpy` (copycode) nem `.map` (telas 3270) está presente, embora a doc
> legada os mencione — provável recorte para o workshop.
>
> Verificar contagens por extensão:
> `find 01-arqueologia/legado-sifap -type f | sed 's/.*\.//' | sort | uniq -c`

## Padrões de Convenção de Nomes

Agrupamento pelos primeiros caracteres do nome (sem abrir os arquivos):

| Prefixo | Contagem | Hipótese                                                                |
| ------- | -------- | ---------------------------------------------------------------------- |
| `BATCH` | 3        | Programas batch (processamento agendado/em lote)                       |
| `CAD`   | 3        | Programas de cadastro/CRUD de entidades                                |
| `CALC`  | 3        | Programas de cálculo (provável núcleo de regras financeiras)           |
| `VAL`   | 3        | Programas de validação (entradas/regras → viram testes)                |
| `REL`   | 2        | Programas de relatório                                                  |
| `CONS`  | 1        | Programa de consulta — **prefixo de ocorrência única** (ver incomuns)  |

> Observação: os 4 DDMs (`BENEFICIARIO`, `PAGAMENTO`, `PROGRAMA-SOCIAL`,
> `AUDITORIA`) **não** seguem padrão de prefixo — usam nomes completos da
> entidade. Hipótese: `Desconhecido — convenção distinta da dos programas;
> investigar no mapeamento de dados`.
>
> Verificar agrupamento:
> `ls 01-arqueologia/legado-sifap/natural-programs/*.NSN | xargs -n1 basename`

## Itens Incomuns (Top 3)

| #   | Caminho do Arquivo                                            | O Que o Torna Incomum                                                                                                          | Investigação Sugerida                                                                                      |
| --- | ------------------------------------------------------------ | ---------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------- |
| 1   | `legado-sifap/natural-programs/CONSBENF.NSN`                 | Único programa com prefixo `CONS` (padrão de nome que ocorre só uma vez); todas as outras categorias têm 2–3 programas.       | Ler para verificar se a consulta é módulo isolado ou se reaproveita lógica de cadastro/validação (CALLNAT). |
| 2   | `legado-sifap/natural-programs/BATCHPGT.NSN`                 | **Maior programa por tamanho** (10.866 bytes — ~14% maior que o 2º), prefixo `BATCH` de entry point batch.                    | Ler primeiro entre os programas: tamanho sugere orquestração densa e muitos CALLNAT a mapear.              |
| 3   | `legado-sifap/adabas-ddms/AUDITORIA.ddm`                     | É o 4º DDM; documentação antiga (`MANUAL-TECNICO-SIFAP-2008`) lista apenas 3 DDMs e nota, em comentário, que foi adicionado em 2005 — divergência doc × realidade. | Mapear campos e descobrir qual programa escreve nele (provável `RELAUDIT`); reconciliar com a doc legada.   |

> Tamanhos medidos por varredura. Maior arquivo da pasta no geral é um doc
> (`legacy-docs/ARQUITETURA-ORIGINAL-1997.md`, 33.825 bytes); entre os programas
> `.NSN`, o maior é `BATCHPGT.NSN` (10.866 bytes), listado acima. Reconfirmar com
> `find 01-arqueologia/legado-sifap -type f -printf '%s\t%p\n' | sort -rn | head`.

## Ordem de Leitura Proposta

> **Isto é uma hipótese.** A ordem real mudará quando a equipe começar a
> rastrear dependências (CALLNAT) e fluxo de dados.

1. **DDMs primeiro (dados antes do código)** — `BENEFICIARIO.ddm`,
   `PROGRAMA-SOCIAL.ddm`, `PAGAMENTO.ddm`, `AUDITORIA.ddm`. Entender as entidades
   antes de ler a lógica que as manipula.
2. **Entry points batch** — `BATCHPGT.NSN` (provável orquestrador da folha
   mensal), depois `BATCHCON.NSN` e `BATCHREL.NSN`. Fluxos batch tendem a
   chamar vários subprogramas e revelam fronteiras de módulo.
3. **Programas mais conectados (cálculo)** — `CALCBENF.NSN`, `CALCDSCT.NSN`,
   `CALCCORR.NSN`. Os nomes de cálculo aparecem como prováveis alvos de CALLNAT
   a partir do batch e do cadastro; é onde a maioria das regras financeiras deve
   morar.
4. **Cadastro** — `CADBENEF.NSN`, `CADDEPEND.NSN`, `CADPROG.NSN`.
5. **Validação** — `VALBENEF.NSN`, `VALELEG.NSN`, `VALDOCS.NSN`.
6. **Consulta e relatórios** — `CONSBENF.NSN`, `RELPGT.NSN`, `RELAUDIT.NSN`.

**Justificativa:** prioriza (a) DDMs para fundamentar o modelo de dados,
(b) entry points batch identificados pelo prefixo `BATCH`, e (c) os programas de
`CALC*` como candidatos a "mais conectados" por serem alvos típicos de CALLNAT.

---

**Lembrete de Definição de Pronto:** inventário existe, contagens precisas
(verificáveis por `find`), 3+ padrões de nome identificados com contagens,
3 itens incomuns sinalizados com caminho e motivo, e ordem de leitura justificada.
