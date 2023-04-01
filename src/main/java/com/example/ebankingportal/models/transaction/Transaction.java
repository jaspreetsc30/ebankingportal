package com.example.ebankingportal.models.transaction;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonDeserialize(as = Transaction.class)
@JsonSerialize(as = Transaction.class)
public class Transaction  {


    private String transactionId;
    private String IBAN;
    private Double amount;
    private String currency;
    private Long timestamp;
    private String message;


}