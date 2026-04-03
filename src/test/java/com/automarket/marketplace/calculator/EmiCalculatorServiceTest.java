package com.automarket.marketplace.calculator;

import com.automarket.marketplace.calculator.dto.EmiCalculationRequest;
import com.automarket.marketplace.calculator.dto.EmiCalculationResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class EmiCalculatorServiceTest {

    private final EmiCalculatorService service = new EmiCalculatorService();

    @Test
    void calculateShouldReturnPositiveEmiAndSchedule() {
        EmiCalculationRequest request = new EmiCalculationRequest(
            BigDecimal.valueOf(20000),
            BigDecimal.valueOf(5000),
            BigDecimal.valueOf(8.0),
            60
        );

        EmiCalculationResponse response = service.calculate(request);

        assertThat(response.loanAmount()).isEqualByComparingTo("15000.00");
        assertThat(response.emi()).isGreaterThan(BigDecimal.ZERO);
        assertThat(response.amortizationSchedule()).hasSize(60);
        assertThat(response.amortizationSchedule().get(59).balance()).isEqualByComparingTo("0.00");
    }

    @Test
    void calculateWithZeroInterestShouldSplitPrincipalEvenly() {
        EmiCalculationRequest request = new EmiCalculationRequest(
            BigDecimal.valueOf(12000),
            BigDecimal.valueOf(0),
            BigDecimal.ZERO,
            12
        );

        EmiCalculationResponse response = service.calculate(request);

        assertThat(response.emi()).isEqualByComparingTo("1000.00");
        assertThat(response.totalInterest()).isEqualByComparingTo("0.00");
        assertThat(response.totalAmount()).isEqualByComparingTo("12000.00");
    }
}

