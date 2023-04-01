package com.example.ebankingportal.util;

import com.example.ebankingportal.models.transaction.Transaction;

import java.math.BigDecimal;
import java.util.HashMap;

public class BalanceCalculator {
    public static HashMap<String, Double> calculateBalances(HashMap<String, Double> balances, Transaction value){
        String currency = value.getCurrency();
        BigDecimal amount = BigDecimal.valueOf(value.getAmount());
        if (!balances.containsKey(currency))
            balances.put(currency,amount.doubleValue());
        else {
            BigDecimal currentBalance = new BigDecimal(String.valueOf(balances.get(currency))).add(new BigDecimal(String.valueOf(amount))) ;
            balances.put(currency,currentBalance.doubleValue());
        }
        return balances;
    }

}
