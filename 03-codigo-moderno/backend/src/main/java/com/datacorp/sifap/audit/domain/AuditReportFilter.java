package com.datacorp.sifap.audit.domain;

/**
 * Filtros do relatorio de auditoria, traducao de RELAUDIT.
 *
 * <p>source_legacy:
 * {@code 01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN#L102-L150}.
 * <br>Regra: BR-011 ("LIMPEZA RELATORIO 2014" — oculta exclusoes) / MYS-004.
 *
 * <p><b>AVISO:</b> este metodo reproduz o comportamento legado que OCULTA
 * eventos de exclusao ({@code ACAO = 'EX'}) do relatorio. E mantido apenas para
 * o teste de equivalencia. O comportamento desejado (trilha completa) esta em
 * REQ-019 e NAO deve ocultar exclusoes em producao.
 */
public class AuditReportFilter {

    /** {@code true} se o evento e suprimido do relatorio legado. */
    public boolean isHiddenLegacy(String acao, String acaoFiltro, String usuarioFiltro,
                                  String tabelaFiltro, String eventUsuario, String eventTabela) {
        // RELAUDIT.NSN#L106-L109: exclusoes nunca aparecem
        if ("EX".equals(acao)) {
            return true;
        }
        if (notBlank(acaoFiltro) && !acaoFiltro.equals(acao)) {
            return true;
        }
        if (notBlank(usuarioFiltro) && !usuarioFiltro.equals(eventUsuario)) {
            return true;
        }
        return notBlank(tabelaFiltro) && !tabelaFiltro.equals(eventTabela);
    }

    /** Descricao da acao (RELAUDIT.NSN#L160-L180). */
    public String actionDescription(String acao) {
        return switch (acao) {
            case "IN" -> "INCLUSAO";
            case "AL" -> "ALTERACAO";
            case "CO" -> "CONCILIACAO";
            case "CN" -> "CONSULTA";
            case "DV" -> "DIVERGENCIA";
            default -> "OUTRA";
        };
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
