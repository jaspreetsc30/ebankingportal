package com.example.ebankingportal.web.ebanking.domain;

import com.example.ebankingportal.model.Transaction;
import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Data
public class MonthlyTransactionsResponse {
    HashMap<String, Double> balances;
    HashMap<String, String> exchangeRates;
    List<Transaction> transactions;
    String message;
}
