package com.example.ebankingportal.web.ebanking.domain;

import com.example.ebankingportal.models.transaction.Transaction;
import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

@Data
public class MonthlyTransactionsResponse {
    HashMap<String, Double> balances;
    List<Transaction> transactions;
    String message;
}
