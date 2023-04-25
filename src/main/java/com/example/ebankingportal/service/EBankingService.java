package com.example.ebankingportal.service;

import com.example.ebankingportal.model.Transaction;
import com.example.ebankingportal.service.exchangerateservice.ExchangeRateService;
import com.example.ebankingportal.util.CalculatorUtil;
import com.example.ebankingportal.web.ebanking.domain.CreditDebitRequest;
import com.example.ebankingportal.web.ebanking.domain.CreditDebitResponse;
import com.example.ebankingportal.web.ebanking.domain.MonthlyTransactionsResponse;
import com.example.ebankingportal.web.ebanking.domain.TransactionType;
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

import java.text.SimpleDateFormat;
import java.util.*;

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

    public static String convertTimeStamp(Long timestamp){
        Date date=new Date(timestamp);
        SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        String dateText = df2.format(date);
        return dateText;
    }
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

    private MonthlyTransactionsResponse paginateTransactions(List<Transaction> transactions , int page, int pagesize, boolean isRateRequired){
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

        for (Transaction transaction: response.getTransactions()) {
             balances= CalculatorUtil.calculateBalancesWithCreditDebit(balances,transaction);
        }
        response.setBalances(balances);
        if (isRateRequired){
            HashMap<String,String> exchangeRates =exchangeRateService.getExchangeRates(new ArrayList<>(balances.keySet()));
            response.setExchangeRates(exchangeRates);;
        }


        return response;

    }

    private CreditDebitResponse generateCreditDebitResponse(CreditDebitRequest request, String transactionId, Long timestamp, TransactionType transactionType,String IBAN){
        return CreditDebitResponse.builder()
                .iban(IBAN)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .message(request.getMessage())
                .transactionId(transactionId)
                .time(convertTimeStamp(timestamp))
                .transactionType(transactionType)
                .build();
    }

    public CreditDebitResponse processDebit(CreditDebitRequest request,String IBAN){
        String transactionId = UUID.randomUUID().toString();
        Long timestamp = System.currentTimeMillis();
        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .IBAN(IBAN)
                .currency(String.valueOf(request.getCurrency()))
                .amount(request.getAmount())
                .timestamp(timestamp )
                .message(request.getMessage())
                .build();
        kafkaTemplate.send(topic,IBAN,transaction);
        return generateCreditDebitResponse(request,transactionId, timestamp,TransactionType.DEBIT,IBAN);
    }

    public CreditDebitResponse processCredit(CreditDebitRequest request, String IBAN){
        String currency = String.valueOf(request.getCurrency());
        Double amount = request.getAmount();
        Long timestamp = System.currentTimeMillis();
        if (Double.compare(getBalance(IBAN,currency),amount) != 1){ throw new RuntimeException("Not enough debit for the given currency");}
        String transactionId = UUID.randomUUID().toString();
        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .IBAN(IBAN)
                .currency(currency)
                .amount(-amount)
                .timestamp(timestamp )
                .message(request.getMessage())
                .build();
        kafkaTemplate.send(topic,IBAN,transaction);
        return generateCreditDebitResponse(request,transactionId, timestamp,TransactionType.CREDIT,IBAN);

    }

    public MonthlyTransactionsResponse getMonthlyTransactions(String key, int page, int pageSize, boolean isRateRequired){
        KafkaStreams kafkaStreams = factoryBean.getKafkaStreams();
        ReadOnlyKeyValueStore<String, List<Transaction>> transactionStateStore = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType(transactionsStoreName, QueryableStoreTypes.keyValueStore())
        );
        List<Transaction> transactions = transactionStateStore.get(key);
            return (transactions!=null && transactions.size()> 0)?
                    paginateTransactions(transactions,page,pageSize,isRateRequired):
                    MonthlyTransactionsResponse.builder()
                            .balances(null)
                            .exchangeRates(null)
                            .transactions(null)
                            .message("No transactions in the specified month")
                            .build();
    }



}
