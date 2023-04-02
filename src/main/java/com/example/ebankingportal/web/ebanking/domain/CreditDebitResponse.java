package com.example.ebankingportal.web.ebanking.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreditDebitResponse {
    public Long time;
    public String transactionId;
    private String iban;
    private Double amount;
    private Currency currency;
    private String message;
    private TransactionType transactionType;
}
