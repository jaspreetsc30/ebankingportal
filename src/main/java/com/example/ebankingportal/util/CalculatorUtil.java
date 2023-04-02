package com.example.ebankingportal.util;

import com.example.ebankingportal.model.Transaction;

import java.math.BigDecimal;
import java.util.HashMap;

public class CalculatorUtil {
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