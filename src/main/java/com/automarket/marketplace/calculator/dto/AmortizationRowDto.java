package com.automarket.marketplace.calculator.dto;

import java.math.BigDecimal;

public record AmortizationRowDto(
    int month,
    BigDecimal emi,
    BigDecimal principal,
    BigDecimal interest,
    BigDecimal balance
) {
}

