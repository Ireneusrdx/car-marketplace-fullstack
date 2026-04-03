package com.automarket.marketplace.calculator;

import com.automarket.marketplace.calculator.dto.EmiCalculationRequest;
import com.automarket.marketplace.calculator.dto.EmiCalculationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calculator")
@RequiredArgsConstructor
public class EmiCalculatorController {

    private final EmiCalculatorService emiCalculatorService;

    @PostMapping("/emi")
    public ResponseEntity<EmiCalculationResponse> calculate(@Valid @RequestBody EmiCalculationRequest request) {
        return ResponseEntity.ok(emiCalculatorService.calculate(request));
    }
}

