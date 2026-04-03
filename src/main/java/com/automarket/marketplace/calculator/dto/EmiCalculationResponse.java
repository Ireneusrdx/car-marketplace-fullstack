package com.automarket.marketplace.calculator.dto;

import java.math.BigDecimal;
import java.util.List;

public record EmiCalculationResponse(
    BigDecimal emi,
    BigDecimal totalAmount,
    BigDecimal totalInterest,
    BigDecimal loanAmount,
    List<AmortizationRowDto> amortizationSchedule
) {
}

