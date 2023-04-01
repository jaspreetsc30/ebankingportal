package com.example.ebankingportal;



import com.example.ebankingportal.configurations.kafka.StreamProcessor;
import com.example.ebankingportal.models.transaction.Transaction;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.*;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.apache.kafka.streams.StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TopologyTest {

    @Value("${kafka.topics.input}")
    String inputTopic;
    @Value("${kafka.topics.output.transaction}")
    String transactionsOutputTopic;

    @Value("${kafka.topics.output.balance}")
    String balancesOutputTopic;
    @Autowired
    private StreamProcessor streamProcessor;

    @BeforeEach
    void setUp() {
        System.out.println("this is invoked");
        streamProcessor = new StreamProcessor();

    }

    @Test
    public void checkNumOfTransactions() {


        StreamsBuilder streamsBuilder = new StreamsBuilder();
        streamProcessor.pipeline(streamsBuilder);
        Topology topology = streamsBuilder.build();

        Properties props = new Properties();
        props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class);
        try (TopologyTestDriver topologyTestDriver = new TopologyTestDriver(topology, props)) {
            TestInputTopic<String, Transaction> inputTopic =
                    topologyTestDriver.createInputTopic(
                            this.inputTopic, new StringSerializer(), new JsonSerde<>(Transaction.class).serializer());

            TestOutputTopic<String,List<Transaction>> outputTopic =
                    topologyTestDriver.createOutputTopic(
                            transactionsOutputTopic,
                            new StringDeserializer(),
                            new Serdes.ListSerde<>(ArrayList.class , new JsonSerde<>(Transaction.class)).deserializer());;
            List<Transaction> transactions = new ArrayList<>();
            Long timestamp = System.currentTimeMillis();
            transactions.add(
                    new Transaction(
                            "3c04fcce-65fa-4115-9441-2781f6706ca7",
                            "test",
                            200.0,
                            "HKD",
                            timestamp,
                            "Online payment HKD"));
            transactions.add(
                    new Transaction(
                            "3c04fcce-65fa-4115-9441-2781f6706cb7",
                            "test",
                            -100.0,
                            "HKD",
                            timestamp,
                            "Online payment CHF"));

            transactions.forEach(tr -> inputTopic.pipeInput(tr.getIBAN(), tr));

            List<KeyValue<String,List<Transaction>>> outputs =
                    outputTopic.readKeyValuesToList();
            assertThat(outputs.size()).isEqualTo(2);
            assertThat(outputs.get(1).value.size()).isEqualTo(2);


        }
    }

    @Test
    public void checkBalances() {

        StreamsBuilder streamsBuilder = new StreamsBuilder();
        streamProcessor.pipeline(streamsBuilder);
        Topology topology = streamsBuilder.build();

        Properties props = new Properties();
        props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class);
        try (TopologyTestDriver topologyTestDriver = new TopologyTestDriver(topology, props)) {
            TestInputTopic<String, Transaction> inputTopic =
                    topologyTestDriver.createInputTopic(
                            this.inputTopic, new StringSerializer(), new JsonSerde<>(Transaction.class).serializer());

            TestOutputTopic<String,String> outputTopic =
                    topologyTestDriver.createOutputTopic(
                            balancesOutputTopic,
                            new StringDeserializer(),
                            new StringDeserializer());;
            List<Transaction> transactions = new ArrayList<>();
            Long timestamp = System.currentTimeMillis();
            transactions.add(
                    new Transaction(
                            "3c04fcce-65fa-4115-9441-2781f6706ca7",
                            "test",
                            200.0,
                            "HKD",
                            timestamp,
                            "Online payment CHF"));
            transactions.add(
                    new Transaction(
                            "3c04fcce-65fa-4115-9441-2781f6706cb7",
                            "test",
                            -100.0,
                            "HKD",
                            timestamp,
                            "Online payment HKD"));

            transactions.forEach(tr -> inputTopic.pipeInput(tr.getIBAN(), tr));

            List<KeyValue<String,String>> outputs =
                    outputTopic.readKeyValuesToList();
            assertThat(outputs.size()).isEqualTo(2);
            assertThat(outputs.get(1).value).contains("100.0");


        }
    }
}