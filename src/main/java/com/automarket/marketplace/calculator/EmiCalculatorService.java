package com.automarket.marketplace.calculator;

import com.automarket.marketplace.auth.AuthException;
import com.automarket.marketplace.calculator.dto.AmortizationRowDto;
import com.automarket.marketplace.calculator.dto.EmiCalculationRequest;
import com.automarket.marketplace.calculator.dto.EmiCalculationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmiCalculatorService {

    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);

    public EmiCalculationResponse calculate(EmiCalculationRequest request) {
        BigDecimal loanAmount = request.price().subtract(request.downPayment(), MC);
        if (loanAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "Down payment must be less than car price");
        }

        int n = request.tenureMonths();
        BigDecimal annualRate = request.interestRate();
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), MC);

        BigDecimal emi;
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            emi = loanAmount.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
        } else {
            BigDecimal onePlusRPowerN = BigDecimal.ONE.add(monthlyRate, MC).pow(n, MC);
            BigDecimal numerator = loanAmount.multiply(monthlyRate, MC).multiply(onePlusRPowerN, MC);
            BigDecimal denominator = onePlusRPowerN.subtract(BigDecimal.ONE, MC);
            emi = numerator.divide(denominator, 2, RoundingMode.HALF_UP);
        }

        List<AmortizationRowDto> schedule = new ArrayList<>();
        BigDecimal balance = loanAmount;
        BigDecimal totalInterest = BigDecimal.ZERO;

        for (int month = 1; month <= n; month++) {
            BigDecimal interest = balance.multiply(monthlyRate, MC).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principal = emi.subtract(interest, MC).setScale(2, RoundingMode.HALF_UP);

            if (month == n || principal.compareTo(balance) > 0) {
                principal = balance.setScale(2, RoundingMode.HALF_UP);
                interest = emi.subtract(principal, MC).setScale(2, RoundingMode.HALF_UP);
            }

            balance = balance.subtract(principal, MC).setScale(2, RoundingMode.HALF_UP);
            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                balance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }

            totalInterest = totalInterest.add(interest, MC);
            schedule.add(new AmortizationRowDto(month, emi, principal, interest, balance));
        }

        BigDecimal totalAmount = loanAmount.add(totalInterest, MC).setScale(2, RoundingMode.HALF_UP);

        return new EmiCalculationResponse(
            emi,
            totalAmount,
            totalInterest.setScale(2, RoundingMode.HALF_UP),
            loanAmount.setScale(2, RoundingMode.HALF_UP),
            schedule
        );
    }
}

