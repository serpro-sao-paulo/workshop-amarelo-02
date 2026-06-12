package com.datacorp.sifap.beneficiary.service;

import com.datacorp.sifap.beneficiary.domain.Beneficiary;
import com.datacorp.sifap.beneficiary.domain.BeneficiaryStatusRule;
import com.datacorp.sifap.beneficiary.domain.CpfValidator;
import com.datacorp.sifap.beneficiary.repository.BeneficiaryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Function;

/**
 * Service do contexto Beneficiary Management.
 * REQ-001..REQ-004 (validacao), REQ-020 (auditoria de mutacoes).
 *
 * <p>Toda logica de negocio fica aqui; {@code @Transactional} somente nesta camada.
 */
@Service
public class BeneficiaryService {

    private final BeneficiaryRepository repo;
    private final CpfValidator cpfValidator;
    private final BeneficiaryStatusRule statusRule;

    public BeneficiaryService(BeneficiaryRepository repo,
                              CpfValidator cpfValidator,
                              BeneficiaryStatusRule statusRule) {
        this.repo = repo;
        this.cpfValidator = cpfValidator;
        this.statusRule = statusRule;
    }

    /** REQ-002: CPF obrigatorio; REQ-001: CPF valido por modulo 11. */
    private void validateCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF obrigatorio");
        }
        long cpfNum;
        try {
            cpfNum = Long.parseLong(cpf.replaceAll("\\D", ""));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("CPF invalido");
        }
        if (!cpfValidator.isValid(cpfNum)) {
            throw new IllegalArgumentException("CPF invalido - digito verificador incorreto");
        }
    }

    /**
     * Inclui um beneficiario (REQ-001..REQ-004, REQ-016 status inicial).
     * source_legacy: CADBENEF.NSN#L99-L215.
     */
    @Transactional
    public Beneficiary create(Beneficiary beneficiary) {
        validateCpf(beneficiary.getCpf());
        if (beneficiary.getFullName() == null || beneficiary.getFullName().isBlank()) {
            throw new IllegalArgumentException("Nome obrigatorio"); // REQ-003
        }
        if (beneficiary.getBirthDate() == null) {
            throw new IllegalArgumentException("Data de nascimento obrigatoria"); // REQ-003
        }
        if (repo.existsByCpf(beneficiary.getCpf())) {
            throw new IllegalStateException("Beneficiario ja cadastrado"); // REQ-004
        }
        // REQ status inicial (BR-016)
        int anoAtual = LocalDate.now().getYear();
        int dtNascimento = beneficiary.getBirthDate().getYear() * 10000
                + beneficiary.getBirthDate().getMonthValue() * 100
                + beneficiary.getBirthDate().getDayOfMonth();
        char status = statusRule.initialStatus(dtNascimento, anoAtual);
        beneficiary.setStatus(String.valueOf(status));
        return repo.save(beneficiary);
    }

    /**
     * Altera um beneficiario existente (REQ-004).
     * source_legacy: CADBENEF.NSN#L199-L220.
     */
    @Transactional
    public Beneficiary update(String cpf, Beneficiary patch) {
        Beneficiary existing = repo.findByCpf(cpf)
                .orElseThrow(() -> new IllegalArgumentException("Beneficiario nao encontrado")); // REQ-004
        if (patch.getFullName() != null) existing.setFullName(patch.getFullName());
        if (patch.getEmail() != null) existing.setEmail(patch.getEmail());
        if (patch.getPhoneMobile() != null) existing.setPhoneMobile(patch.getPhoneMobile());
        if (patch.getFamilyIncome() != null) existing.setFamilyIncome(patch.getFamilyIncome());
        if (patch.getStreet() != null) existing.setStreet(patch.getStreet());
        if (patch.getCity() != null) existing.setCity(patch.getCity());
        if (patch.getUf() != null) existing.setUf(patch.getUf());
        return repo.save(existing);
    }

    /** REQ-006: consulta com CPF mascarado (a mascara e aplicada no controller). */
    public Optional<Beneficiary> findByCpf(String cpf) {
        return repo.findByCpf(cpf);
    }

    /**
     * REQ-006: consulta detalhada. O mapeamento ocorre dentro da transacao para
     * inicializar a colecao lazy de dependentes (grupo PE GRP-DEPENDENTE).
     */
    @Transactional
    public <T> Optional<T> findDetailByCpf(String cpf, Function<Beneficiary, T> mapper) {
        return repo.findByCpf(cpf)
                .map(b -> {
                    b.getDependents().size(); // forca inicializacao da colecao lazy
                    return mapper.apply(b);
                });
    }
}
