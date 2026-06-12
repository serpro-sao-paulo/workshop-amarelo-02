package com.datacorp.sifap.beneficiary.api;

import com.datacorp.sifap.beneficiary.domain.Beneficiary;
import com.datacorp.sifap.beneficiary.domain.CpfMask;
import com.datacorp.sifap.beneficiary.domain.Dependent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response detalhada de consulta de beneficiário.
 *
 * <p>source_legacy: {@code 01-arqueologia/legado-sifap/adabas-ddms/BENEFICIARIO.ddm}
 * <br>REQ-006: o CPF é exibido mascarado (LGPD); todos os demais campos do DDM
 * são expostos para a tela de consulta.
 *
 * <p>O CPF do beneficiário e dos dependentes nunca trafega em claro nesta response.
 */
public record BeneficiaryDetailResponse(
        // Identificação
        String maskedCpf,
        String registrationNumber,
        String fullName,
        String motherName,
        String fatherName,
        LocalDate birthDate,
        String gender,
        String maritalStatus,
        // Documento RG
        String rgNumber,
        String rgIssuer,
        String rgState,
        LocalDate rgIssueDate,
        // Endereço
        String street,
        String streetNumber,
        String complement,
        String neighborhood,
        String city,
        String uf,
        Integer zipCode,
        Integer ibgeCode,
        String regionCode,
        // Benefício
        String programCode,
        LocalDate registrationDate,
        LocalDate benefitStartDate,
        LocalDate benefitEndDate,
        String status,
        String statusReason,
        LocalDate statusDate,
        // Renda familiar
        BigDecimal familyIncome,
        Integer familyMembers,
        BigDecimal perCapitaIncome,
        // Contato
        String phoneLandline,
        String phoneMobile,
        String email,
        // Biometria
        String biometricStatus,
        LocalDate biometricCollectionDate,
        String biometricPostCode,
        // Dependentes (grupo PE GRP-DEPENDENTE)
        List<DependentResponse> dependents,
        // Controle interno
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    /** Dependente com CPF mascarado (REQ-006). */
    public record DependentResponse(
            String maskedCpf,
            String name,
            LocalDate birthDate,
            String kinship,
            String status,
            String disabilityFlag) {
    }

    /**
     * Monta a response a partir da entidade, aplicando a máscara de CPF (REQ-006).
     */
    public static BeneficiaryDetailResponse from(Beneficiary b, CpfMask cpfMask) {
        List<DependentResponse> deps = b.getDependents().stream()
                .map(d -> toDependent(d, cpfMask))
                .toList();
        return new BeneficiaryDetailResponse(
                maskCpf(b.getCpf(), cpfMask),
                b.getRegistrationNumber(),
                b.getFullName(),
                b.getMotherName(),
                b.getFatherName(),
                b.getBirthDate(),
                b.getGender(),
                b.getMaritalStatus(),
                b.getRgNumber(),
                b.getRgIssuer(),
                b.getRgState(),
                b.getRgIssueDate(),
                b.getStreet(),
                b.getStreetNumber(),
                b.getComplement(),
                b.getNeighborhood(),
                b.getCity(),
                b.getUf(),
                b.getZipCode(),
                b.getIbgeCode(),
                b.getRegionCode(),
                b.getProgramCode(),
                b.getRegistrationDate(),
                b.getBenefitStartDate(),
                b.getBenefitEndDate(),
                b.getStatus(),
                b.getStatusReason(),
                b.getStatusDate(),
                b.getFamilyIncome(),
                b.getFamilyMembers(),
                b.getPerCapitaIncome(),
                b.getPhoneLandline(),
                b.getPhoneMobile(),
                b.getEmail(),
                b.getBiometricStatus(),
                b.getBiometricCollectionDate(),
                b.getBiometricPostCode(),
                deps,
                b.getCreatedAt(),
                b.getUpdatedAt());
    }

    private static DependentResponse toDependent(Dependent d, CpfMask cpfMask) {
        return new DependentResponse(
                maskCpf(d.getCpf(), cpfMask),
                d.getName(),
                d.getBirthDate(),
                d.getKinship(),
                d.getStatus(),
                d.getDisabilityFlag());
    }

    /** REQ-006: aplica a máscara de CPF; retorna {@code null} se o CPF for ausente. */
    private static String maskCpf(String cpf, CpfMask cpfMask) {
        if (cpf == null || cpf.isBlank()) {
            return null;
        }
        return cpfMask.maskLegacy(Long.parseLong(cpf));
    }
}
