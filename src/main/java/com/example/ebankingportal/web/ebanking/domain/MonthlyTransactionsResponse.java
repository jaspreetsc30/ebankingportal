package com.example.ebankingportal.web.ebanking.domain;

import com.example.ebankingportal.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyTransactionsResponse {
    HashMap<String, Double> balances;
    HashMap<String, String> exchangeRates;
    List<Transaction> transactions;
    String message;
}
