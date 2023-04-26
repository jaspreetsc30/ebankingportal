package com.example.ebankingportal.util;

import com.example.ebankingportal.model.Transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
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

    public static HashMap<String, Double> calculateBalancesWithCreditDebit(HashMap<String, Double> balances, Transaction value){
        String currency = value.getCurrency();
        BigDecimal amount = BigDecimal.valueOf(value.getAmount());
        if (!balances.containsKey(currency))
            balances.put(currency,amount.doubleValue());

        else {
            BigDecimal currentBalance = new BigDecimal(String.valueOf(balances.get(currency))).add(new BigDecimal(String.valueOf(amount))) ;
            balances.put(currency,currentBalance.doubleValue());
        }
        if (!balances.containsKey(currency+"credit")) balances.put(currency+"credit",0.0);
        if (!balances.containsKey(currency+"debit")) balances.put(currency+"debit",0.0);
        if (amount.signum() ==-1){
                BigDecimal credit = new BigDecimal(String.valueOf(balances.get(currency+"credit"))).add(new BigDecimal(String.valueOf(amount.negate()))) ;
                balances.put(currency+"credit",credit.doubleValue());
        }
        else{
                BigDecimal debit = new BigDecimal(String.valueOf(balances.get(currency+"debit"))).add(new BigDecimal(String.valueOf(amount))) ;
                balances.put(currency+"debit",debit.doubleValue());

        }
        return balances;
    }

}
