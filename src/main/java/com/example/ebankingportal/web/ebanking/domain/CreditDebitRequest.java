package com.example.ebankingportal.web.ebanking.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.Data;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data

public class CreditDebitRequest {

    @DecimalMin("0.01")
    @DecimalMax("100000")
    @Digits(integer = 6, fraction = 2)
    @Schema(name = "amount",description = "amount credited/debited in a transaction" , example = "100")
    private Double amount;
    @NotNull
    @Schema(name = "currency" , description = "currency that the transaction is processed in" , example = "USD")
    private Currency currency;
    @Schema(name = "message", description = "description of the message" , example = "USD Deposit")
    private String message;
}
