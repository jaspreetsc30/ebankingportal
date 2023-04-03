package com.example.ebankingportal.web.ebanking.domain;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class CreditDebitRequest {

    @DecimalMin("0.01")
    @DecimalMax("100000")
    @Digits(integer = 6, fraction = 2)
    private Double amount;
    @NotNull
    private Currency currency;
    private String message;
}
