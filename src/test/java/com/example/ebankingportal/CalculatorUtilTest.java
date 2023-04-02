package com.example.ebankingportal;

import com.example.ebankingportal.model.Transaction;
import com.example.ebankingportal.util.CalculatorUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class CalculatorUtilTest {

    @Test
    public void TestLogic(){
        List<Transaction> transactions = new ArrayList<>();
        HashMap<String,Double> balance = new HashMap<>();
        transactions.add(
                Transaction.builder().amount(100.4).currency("HKD").build());
        transactions.add(
                Transaction.builder().amount(-30.4).currency("HKD").build());
        transactions.add(
                Transaction.builder().amount(35.99).currency("USD").build());
        transactions.add(
                Transaction.builder().amount(-30.33).currency("USD").build());
        for (Transaction t: transactions) balance = CalculatorUtil.calculateBalances(balance,t);
        assertThat(balance.size()).isEqualTo(2);
        assertThat(balance.get("HKD")).isEqualTo(70.0);
        assertThat(balance.get("USD")).isEqualTo(5.66);

    }}
