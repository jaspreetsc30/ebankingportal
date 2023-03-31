package com.example.ebankingportal.configurations.kafka;


import com.example.ebankingportal.models.transaction.JSONSerde;
import com.example.ebankingportal.models.transaction.ListTransactionsSerde;
import com.example.ebankingportal.models.transaction.Transaction;
import com.example.ebankingportal.models.transaction.TransactionSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

@Configuration
public class StreamProcessor {

    @Value("${kafka.inputtopic}")
    String inputTopic;
    @Value("${kafka.outputtopic}")
    String outputTopic;

    @Autowired
    public void pipeline(StreamsBuilder streamsBuilder){
        Serde<HashMap<String,Double>> balanceSerde = new JsonSerde<>(HashMap.class);
        Serde<ArrayList<Transaction>> transactionListSerde = new JsonSerde<>();


        KStream<String, Transaction> accountTransactionsStream =
                streamsBuilder.stream(
                      inputTopic  , Consumed.with(Serdes.String(), new TransactionSerde()));
        accountTransactionsStream.print(Printed.toSysOut());
        accountTransactionsStream.to(outputTopic);
        accountTransactionsStream
                .groupBy((key, value) ->
                        key)
                .aggregate(
                        HashMap<String,Double>::new,
                        (key, value, aggregate) -> {
                            return calculateBalances( aggregate,value);
                        },
                        Materialized.<String, HashMap<String,Double>, KeyValueStore<Bytes, byte[]>>as("balances").withValueSerde(balanceSerde));

        KTable aggregatedTransactions = accountTransactionsStream
                .groupBy((key, value) ->
                        StreamProcessor.getMonthandYear(value.getTimestamp())+ "_" +value.getIBAN())
                .aggregate(
                        (Initializer<ArrayList<Transaction>>) ArrayList::new,
                        (key, value, aggregate) -> {
                            aggregate.add(value);
                            return aggregate;
                        },
                        Materialized.<String,List<Transaction>,KeyValueStore<Bytes, byte[]>>as("transactionsbyuser")
                                .withValueSerde(
                                        new Serdes.ListSerde(ArrayList.class, new TransactionSerde())));
          Serde<List<Transaction>> checker =new Serdes.ListSerde(ArrayList.class, new TransactionSerde());

          aggregatedTransactions.toStream().peek((key, value) -> System.out.println("Outgoing record - key " +key +" value " + value + value.getClass().getName() ))
                  .peek((key,value) ->{
                      byte[] bytes = new Serdes.ListSerde(ArrayList.class,new TransactionSerde()).serializer().serialize(outputTopic,value);
                      ArrayList<Transaction> transactions = (ArrayList<Transaction>) new Serdes.ListSerde<>(ArrayList.class,new TransactionSerde()).deserializer().deserialize(outputTopic,bytes);
                      System.out.println(bytes);
                      System.out.println(transactions.get(0));
                      ;})

                  .to(outputTopic,Produced.with(Serdes.String(),new Serdes.ListSerde<Transaction>(ArrayList.class,new TransactionSerde())));

//        KTable<String,ArrayList<Transaction>> table = accountTransactionsStream
//                .groupBy((key, value) ->
//                        StreamProcessor.getMonthandYear(value.getTimestamp())+ "_" +value.getIBAN())
//                .aggregate(
//                        (Initializer<ArrayList<Transaction>>) ArrayList::new,
//                        (key, value, aggregate) -> {
//                            aggregate.add(value);
//                            return aggregate;
//                        },
//                        Materialized.<String, ArrayList<Transaction>, KeyValueStore<Bytes, byte[]>>as("transactions")
//                                .withValueSerde(
//                                        transactionListSerde));
//        table.toStream().to(outputTopic);
//        accountTransactionsStream
//                .groupBy((key, value) ->
//                        StreamProcessor.getMonthandYear(value.getTimestamp())+ "_" +value.getIBAN())
//                .aggregate(
//                        HashMap<String,BigDecimal>::new,
//                        (key, value, aggregate) -> {
//                            return calculator( aggregate,value);
//                        },
//                        Materialized.<String, HashMap<String,BigDecimal>, KeyValueStore<Bytes, byte[]>>as("monthlybalancebyuser").withValueSerde(balanceSerde));


    }
    @KafkaListener(topics = "monthly-bank-transactions-aggregates" , groupId = "1")
    public void process(Object transactions){

        System.out.println(transactions);

    }
    public static String getMonthandYear(Long timestamp){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        return Integer.toString(cal.get(Calendar.MONTH) + 1) + Integer.toString(cal.get(Calendar.YEAR))  ;
    }
    public HashMap<String, Double> calculateBalances(HashMap<String, Double> balances, Transaction value){
        String currency = value.getCurrency();
        BigDecimal amount = new BigDecimal(value.getAmount());
        if (!balances.containsKey(currency))
            balances.put(currency,amount.doubleValue());
        else {
            BigDecimal currentBalance = new BigDecimal(String.valueOf(balances.get(currency))).add(new BigDecimal(String.valueOf(amount))) ;
            balances.put(currency,currentBalance.doubleValue());
        }
        return balances;
    }

}
