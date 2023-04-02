package com.example.ebankingportal.service;

import com.example.ebankingportal.model.Transaction;
import com.example.ebankingportal.service.exchangerateservice.ExchangeRateService;
import com.example.ebankingportal.util.CalculatorUtil;
import com.example.ebankingportal.web.ebanking.domain.CreditDebitRequest;
import com.example.ebankingportal.web.ebanking.domain.MonthlyTransactionsResponse;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EBankingService {

    @Value("${kafka.transactionsstorename}")
    String transactionsStoreName;

    @Value("${kafka.balancesstorename}")
    String balancesStoreName;

    @Value("${kafka.topics.input}")
    private String topic;
    @Autowired
    private KafkaTemplate<String, Transaction> kafkaTemplate;
    @Autowired
    StreamsBuilderFactoryBean factoryBean;
    @Autowired
    ExchangeRateService exchangeRateService;

    private HashMap<String, Double> getBalance(String IBAN){
        KafkaStreams kafkaStreams = factoryBean.getKafkaStreams();
        ReadOnlyKeyValueStore<String, HashMap<String,Double>> balance = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType(balancesStoreName, QueryableStoreTypes.keyValueStore())
        );
        HashMap<String,Double> userBalance = balance.get(IBAN);
        return balance.get(IBAN);
    }

    private Double getBalance(String IBAN, String currency){
        HashMap<String,Double> userBalance = getBalance(IBAN);
        if (userBalance== null || !userBalance.containsKey(currency)) return new Double("0");
        return new Double(String.valueOf(userBalance.get(currency)));
    }

    private MonthlyTransactionsResponse paginateTransactions(List<Transaction> transactions , int page, int pagesize, boolean exchangeRateFlag){
        MonthlyTransactionsResponse response = new MonthlyTransactionsResponse();
        HashMap<String,Double> balances = new HashMap<>();
        int numTransactions = transactions.size();
        if (numTransactions == 0 || numTransactions < pagesize * page - pagesize){
            response.setTransactions(transactions);
            response.setBalances(balances);
            response.setMessage("Balances and Transactions are empty, please either check \n1.whether the page and page size has been specified correctly (does not reach out of bound)\n2Transactions Have not been made yet");
            return response;
        }
        if (numTransactions < pagesize){
            response.setMessage("Since the number of transactions is less than the page size, all transactions have been returned");
        }
        else {
            int fromIndex = pagesize * page - pagesize;
            transactions = numTransactions < pagesize * page?transactions.subList(fromIndex,-1):transactions.subList(fromIndex,pagesize * page);
        }
        response.setTransactions(transactions);
        System.out.println(transactions);

        for (Transaction transaction: response.getTransactions()) {
             balances= CalculatorUtil.calculateBalances(balances,transaction);
        }
        response.setBalances(balances);
        if (exchangeRateFlag){
            HashMap<String,String> exchangeRates =exchangeRateService.getExchangeRates(new ArrayList<>(balances.keySet()));
            response.setExchangeRates(exchangeRates);;
        }


        return response;

    }

    public String processDebit(CreditDebitRequest request){
        String transactionId = UUID.randomUUID().toString();
        String IBAN = request.getIban();
        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .IBAN(IBAN)
                .currency(String.valueOf(request.getCurrency()))
                .amount(request.getAmount())
                .timestamp(System.currentTimeMillis() )
                .message(request.getMessage())
                .build();
        kafkaTemplate.send(topic,IBAN,transaction);
        return transactionId;
    }

    public String processCredit(CreditDebitRequest request){
        String IBAN = request.getIban();
        String currency = String.valueOf(request.getCurrency());
        Double amount = request.getAmount();
        if (Double.compare(getBalance(IBAN,currency),amount) != 1){ throw new RuntimeException("Not enough debit for the given currency");}
        String transactionId = UUID.randomUUID().toString();
        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .IBAN(IBAN)
                .currency(currency)
                .amount(-amount)
                .timestamp(System.currentTimeMillis() )
                .message(request.getMessage())
                .build();
        kafkaTemplate.send(topic,IBAN,transaction);
        return transactionId;

    }

    public MonthlyTransactionsResponse getMonthlyTransactions(String key, int page, int pageSize, boolean exchangeRateFlag){
        KafkaStreams kafkaStreams = factoryBean.getKafkaStreams();
        ReadOnlyKeyValueStore<String, List<Transaction>> transactions = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType(transactionsStoreName, QueryableStoreTypes.keyValueStore())
        );
            return paginateTransactions(transactions.get(key),page,pageSize,exchangeRateFlag);
    }



}
