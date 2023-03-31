package com.example.ebankingportal.web.ebanking.domain;

import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class CreditDebitRequest {
    @NotBlank
    private String iban;
    @DecimalMin("0.01")
    @DecimalMax("100000")
    private Double amount;
    @NotNull
    private Currency currency;
    private String message;
}
