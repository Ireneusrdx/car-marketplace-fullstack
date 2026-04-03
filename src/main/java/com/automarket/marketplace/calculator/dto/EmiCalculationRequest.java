package com.automarket.marketplace.calculator.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record EmiCalculationRequest(
    @NotNull @DecimalMin("0.01") BigDecimal price,
    @NotNull @DecimalMin("0.00") BigDecimal downPayment,
    @NotNull @DecimalMin("0.00") BigDecimal interestRate,
    @NotNull @Positive Integer tenureMonths
) {
}

