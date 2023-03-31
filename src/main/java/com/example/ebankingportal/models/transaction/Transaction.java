package com.example.ebankingportal.models.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction implements JSONSerdeCompatible {


    private String transactionId;
    private String IBAN;
    private Double amount;
    private String currency;
    private Long timestamp;
    private String message;


}