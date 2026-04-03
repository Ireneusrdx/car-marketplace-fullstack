package com.automarket.marketplace.calculator;

import com.automarket.marketplace.calculator.dto.EmiCalculationRequest;
import com.automarket.marketplace.calculator.dto.EmiCalculationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmiCalculatorController.class)
@AutoConfigureMockMvc
class EmiCalculatorControllerSecurityWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmiCalculatorService emiCalculatorService;

    @Test
    void emiShouldBeAccessibleWithoutAuthentication() throws Exception {
        EmiCalculationRequest request = new EmiCalculationRequest(
            BigDecimal.valueOf(20000),
            BigDecimal.valueOf(5000),
            BigDecimal.valueOf(8),
            60
        );

        EmiCalculationResponse response = new EmiCalculationResponse(
            BigDecimal.valueOf(300),
            BigDecimal.valueOf(18000),
            BigDecimal.valueOf(3000),
            BigDecimal.valueOf(15000),
            List.of()
        );

        when(emiCalculatorService.calculate(any(EmiCalculationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/calculator/emi")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        verify(emiCalculatorService).calculate(any(EmiCalculationRequest.class));
    }
}

