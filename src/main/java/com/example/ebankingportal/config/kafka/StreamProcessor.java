package com.example.ebankingportal.config.kafka;


import com.example.ebankingportal.model.Transaction;
import com.example.ebankingportal.util.CalculatorUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

@Configuration
@NoArgsConstructor
@Data
public class StreamProcessor {

    @Value("${kafka.topics.input}")
    String inputTopic;
    @Value("${kafka.topics.output.transaction}")
    String transactionsOutputTopic;

    @Value("${kafka.topics.output.balance}")
    String balancesOutputTopic;



    @Value("${kafka.transactionsstorename}")
    String transactionStoreName;

    @Value("${kafka.balancesstorename}")
    String balancesstorename;



    @Autowired
    public void pipeline(StreamsBuilder streamsBuilder){
        Serde<HashMap<String,Double>> balanceSerde = new JsonSerde<>(HashMap.class);

        KStream<String, Transaction> accountTransactionsStream =
                streamsBuilder.stream(
                      inputTopic  , Consumed.with(Serdes.String(), new JsonSerde<>(Transaction.class)));
        accountTransactionsStream.print(Printed.toSysOut());
        accountTransactionsStream
                .peek((key,value) -> System.out.println(key + value))
                .groupBy((key, value) ->
                        key)
                .aggregate(
                        HashMap<String,Double>::new,
                        (key, value, aggregate) -> CalculatorUtil.calculateBalances(aggregate,value),


                        Materialized.<String, HashMap<String,Double>, KeyValueStore<Bytes, byte[]>>as(balancesstorename)
                                .withValueSerde(balanceSerde)).toStream()
                .mapValues(AbstractMap::toString).to(balancesOutputTopic, Produced.with(Serdes.String(), Serdes.String()));



        accountTransactionsStream
                .groupBy((key, value) ->
                        StreamProcessor.getMonthAndYear(value.getTimestamp())+ "_" +value.getIBAN())
                .aggregate(
                        (Initializer<ArrayList<Transaction>>) ArrayList::new,
                        (key, value, aggregate) -> {
                            aggregate.add(value);
                            return aggregate;
                        },
                        Materialized.<String,ArrayList<Transaction>,KeyValueStore<Bytes, byte[]>>as(transactionStoreName)
                                .withValueSerde(
                                        new Serdes.ListSerde(ArrayList.class, new JsonSerde(Transaction.class)))).toStream().to(transactionsOutputTopic,Produced.with(Serdes.String(),new Serdes.ListSerde<>(ArrayList.class, new JsonSerde<>(Transaction.class))));

    }

    public static String getMonthAndYear(Long timestamp){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        return Integer.toString(cal.get(Calendar.MONTH) + 1) + cal.get(Calendar.YEAR);
    }
    public HashMap<String, Double> calculateBalances(HashMap<String, Double> balances, Transaction value){
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
