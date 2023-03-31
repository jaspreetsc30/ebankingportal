package com.example.ebankingportal;



import com.example.ebankingportal.configurations.kafka.StreamProcessor;
import com.example.ebankingportal.models.transaction.JSONSerde;
import com.example.ebankingportal.models.transaction.ListTransactionsSerde;
import com.example.ebankingportal.models.transaction.Transaction;
import com.example.ebankingportal.models.transaction.TransactionSerde;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.*;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.apache.kafka.streams.StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TopologyTest {

    @Value("${kafka.inputtopic}")
    String INPUT_TOPIC;
    @Value("${kafka.outputtopic}")
    String OUTPUT_TOPIC;
    @Autowired
    private StreamProcessor streamProcessor;
    @BeforeEach
    void setUp() {
        streamProcessor = new StreamProcessor();
    }

    @Test
    public void givenInputMessages_whenProcessedWithTopology_thenOutputShouldMatchWithExpected() {

        StreamsBuilder streamsBuilder = new StreamsBuilder();
        streamProcessor.pipeline(streamsBuilder);
        Topology topology = streamsBuilder.build();

        Properties props = new Properties();
        props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, TransactionSerde.class);
        try (TopologyTestDriver topologyTestDriver = new TopologyTestDriver(topology, props)) {
            TestInputTopic<String, Transaction> inputTopic =
                    topologyTestDriver.createInputTopic(
                            INPUT_TOPIC, new StringSerializer(), new TransactionSerde());

            TestOutputTopic<String, List<Transaction>> outputTopic =
                    topologyTestDriver.createOutputTopic(
                            OUTPUT_TOPIC,
                            new StringDeserializer(),
                            new Serdes.ListSerde<>(ArrayList.class,new TransactionSerde()).deserializer());;

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
                            "Online payment CHF"));

            transactions.forEach(tr -> inputTopic.pipeInput(tr.getIBAN(), tr));
            KeyValue<String, List<Transaction>> actualOutput =
                    outputTopic.readKeyValuesToList().get(1);

            assertThat(2).isEqualTo(actualOutput.value.size());
        }
    }
}