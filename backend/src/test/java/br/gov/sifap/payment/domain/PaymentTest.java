package br.gov.sifap.payment.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

// Tests for Payment.generateNormal() — BR-005 (BATCHPGT.NSN#L195-L210)
class PaymentTest {

    private static final String CPF        = "12345678901";
    private static final int    PROG_CODE  = 10;
    private static final String COMPETENCE = "202601";
    private static final LocalDate DATE    = LocalDate.of(2026, 1, 5);

    private static BenefitBreakdown breakdown(String gross, String discount, String net, String bonus) {
        return new BenefitBreakdown(
                new BigDecimal(gross),
                new BigDecimal(discount),
                new BigDecimal(net),
                new BigDecimal(bonus));
    }

    @Test
    @DisplayName("generateNormal: status must be GENERATED — BR-005/BATCHPGT.NSN#L332-L335")
    void statusIsGenerated() {
        // Arrange
        var bd = breakdown("1000.00", "30.00", "970.00", "0.00");

        // Act
        var payment = Payment.generateNormal(CPF, PROG_CODE, COMPETENCE, bd, DATE);

        // Assert
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.GENERATED);
    }

    @Test
    @DisplayName("generateNormal: type must be NORMAL for non-December competence")
    void typeIsNormal() {
        var bd = breakdown("800.00", "0.00", "800.00", "0.00");

        var payment = Payment.generateNormal(CPF, PROG_CODE, COMPETENCE, bd, DATE);

        assertThat(payment.getType()).isEqualTo(PaymentType.NORMAL);
    }

    @Test
    @DisplayName("generateNormal: id is always assigned (non-null UUID)")
    void idIsAssigned() {
        var bd = breakdown("500.00", "0.00", "500.00", "0.00");

        var payment = Payment.generateNormal(CPF, PROG_CODE, COMPETENCE, bd, DATE);

        assertThat(payment.getId()).isNotNull();
    }

    @Test
    @DisplayName("generateNormal: two calls produce distinct IDs")
    void eachCallProducesDistinctId() {
        var bd = breakdown("500.00", "0.00", "500.00", "0.00");

        var p1 = Payment.generateNormal(CPF, PROG_CODE, COMPETENCE, bd, DATE);
        var p2 = Payment.generateNormal(CPF, PROG_CODE, COMPETENCE, bd, DATE);

        assertThat(p1.getId()).isNotEqualTo(p2.getId());
    }

    @Test
    @DisplayName("generateNormal: amounts match the BenefitBreakdown")
    void amountsMatchBreakdown() {
        var bd = breakdown("1200.00", "36.00", "1164.00", "120.00");

        var payment = Payment.generateNormal(CPF, PROG_CODE, COMPETENCE, bd, DATE);

        assertThat(payment.getGrossAmount()).isEqualByComparingTo("1200.00");
        assertThat(payment.getDiscountAmount()).isEqualByComparingTo("36.00");
        assertThat(payment.getNetAmount()).isEqualByComparingTo("1164.00");
        assertThat(payment.getBonusAmount()).isEqualByComparingTo("120.00");
    }

    @Test
    @DisplayName("generateNormal: scalar fields are copied from parameters")
    void scalarFieldsCopied() {
        var bd = breakdown("600.00", "18.00", "582.00", "0.00");

        var payment = Payment.generateNormal(CPF, PROG_CODE, COMPETENCE, bd, DATE);

        assertThat(payment.getBeneficiaryCpf()).isEqualTo(CPF);
        assertThat(payment.getProgramCode()).isEqualTo(PROG_CODE);
        assertThat(payment.getCompetence()).isEqualTo(COMPETENCE);
        assertThat(payment.getGenerationDate()).isEqualTo(DATE);
    }

    @Test
    @DisplayName("generateNormal: zero bonus is accepted (no December bonus for NORMAL payments)")
    void zeroBonusAccepted() {
        var bd = breakdown("400.00", "0.00", "400.00", "0.00");

        var payment = Payment.generateNormal(CPF, PROG_CODE, COMPETENCE, bd, DATE);

        assertThat(payment.getBonusAmount()).isEqualByComparingTo("0.00");
    }
}
