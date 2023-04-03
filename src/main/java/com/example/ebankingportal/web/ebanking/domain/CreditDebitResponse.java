package com.example.ebankingportal.web.ebanking.domain;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(name = "time" , description = "time the transaction was processed", example = "2023/3/3 5:40:36")
    public String time;
    @Schema(name = "transactionId" , description = "the Id of the transaction, using a UUID format" )
    public String transactionId;
    @Schema(name = "iban", description = "unique IBAN of every user" , example = "89d3o179-abcd-465b-o9ee-e2d5f6ofEld46")
    private String iban;
    @Schema(name = "amount",description = "amount credited/debited in a transaction" , example = "100")
    private Double amount;
    @Schema(name = "currency" , description = "currency that the transaction is processed in" , example = "USD")
    private Currency currency;
    @Schema(name = "message", description = "description of the message" , example = "USD Deposit")
    private String message;
    @Schema(name = "transactionType", description = "type of transaction, enum of either DEBIT or CREDIT" , example = "DEBIT")
    private TransactionType transactionType;
}
