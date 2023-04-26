package com.example.ebankingportal.web.ebanking.domain;

import com.example.ebankingportal.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyTransactionsResponse {
    Map<String, Double> balances;
    Map<String, Double> credits;
    Map<String, Double> debits;

    HashMap<String, String> exchangeRates;
    List<Transaction> transactions;
    String message;
}
