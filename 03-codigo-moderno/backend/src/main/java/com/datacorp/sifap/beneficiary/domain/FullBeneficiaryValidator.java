package com.datacorp.sifap.beneficiary.domain;

import java.util.Set;

/**
 * Validacao completa de dados cadastrais, traducao de VALBENEF.
 *
 * <p>source_legacy:
 * {@code 01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L100-L280}.
 * <br>Regras: BR-015 (CPF digitos iguais, excecao 000), BR-018 (data, 29/02
 * fixo), validacao de nome/UF/status. MYS-009, MYS-021.
 *
 * <p>Fidelidade preservada:
 * <ul>
 *   <li>CPF com todos os digitos iguais e invalido, EXCETO se inicia com
 *       {@code 000} (bypass de teste — permite {@code 00000000000}).</li>
 *   <li>Fevereiro aceita ate 29 dias em QUALQUER ano (sem calculo de bissexto
 *       real — MYS-021).</li>
 *   <li>Nome valido exige um espaco em posicao &gt; 1.</li>
 * </ul>
 */
public class FullBeneficiaryValidator {

    private static final int[] DIAS_MES =
            {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    private static final Set<String> UF_VALIDAS = Set.of(
            "AC", "AL", "AM", "AP", "BA", "CE", "DF", "ES", "GO", "MA", "MG", "MS",
            "MT", "PA", "PB", "PE", "PI", "PR", "RJ", "RN", "RO", "RR", "RS", "SC",
            "SE", "SP", "TO");

    private static final Set<Character> STATUS_VALIDO = Set.of('A', 'S', 'C', 'I', 'D');

    private final CpfValidator mod11 = new CpfValidator();

    /** VALIDA-CPF-COMPLETO (VALBENEF.NSN#L185-L240). */
    public boolean isCpfValid(long cpf) {
        int[] dig = digits(cpf);
        boolean todosIguais = true;
        for (int i = 1; i < 11; i++) {
            if (dig[i] != dig[0]) {
                todosIguais = false;
                break;
            }
        }
        if (todosIguais) {
            // Excecao: iniciados com 000 sao validos (teste governo)
            return dig[0] == 0 && dig[1] == 0 && dig[2] == 0;
        }
        return mod11.isValid(cpf);
    }

    /** VALIDA-DATA (VALBENEF.NSN#L243-L262). */
    public boolean isDateValid(int dtNasc, int anoAtual) {
        int ano = dtNasc / 10000;
        int mes = (dtNasc - (ano * 10000)) / 100;
        int dia = dtNasc - (ano * 10000) - (mes * 100);
        if (ano < 1900 || ano > anoAtual) {
            return false;
        }
        if (mes < 1 || mes > 12) {
            return false;
        }
        return dia >= 1 && dia <= DIAS_MES[mes - 1];
    }

    /** VALIDA-NOME: nao vazio e com espaco em posicao > 1 (VALBENEF.NSN#L265-L279). */
    public boolean isNameValid(String nome) {
        if (nome == null || nome.isBlank()) {
            return false;
        }
        int pos = nome.indexOf(' ');
        return pos > 0; // EXAMINE GIVING POSITION e 1-based; pos>1 no Natural == indexOf>0 em 0-based
    }

    public boolean isUfValid(String uf) {
        return uf == null || uf.isBlank() || UF_VALIDAS.contains(uf);
    }

    public boolean isStatusValid(char status) {
        return STATUS_VALIDO.contains(status);
    }

    private int[] digits(long cpf) {
        String s = String.format("%011d", cpf);
        int[] d = new int[11];
        for (int i = 0; i < 11; i++) {
            d[i] = s.charAt(i) - '0';
        }
        return d;
    }
}
