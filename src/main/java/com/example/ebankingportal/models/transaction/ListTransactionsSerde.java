package com.example.ebankingportal.models.transaction;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


@Slf4j
public class ListTransactionsSerde implements Serializer<ArrayList<Transaction>>, Deserializer<ArrayList<Transaction>>, Serde<ArrayList<Transaction>> {



    @Value("${kafka.outputtopic}")
    private String outputTopic;
    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ArrayList<Transaction> deserialize(String s, byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return (ArrayList<Transaction>) new Serdes.ListSerde<>(ArrayList.class,new TransactionSerde()).deserializer().deserialize(s,bytes);
    }

    @Override
    public Serializer<ArrayList<Transaction>> serializer() {
        return this;
    }

    @Override
    public Deserializer<ArrayList<Transaction>> deserializer() {
        return this;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String s, ArrayList<Transaction> transaction) {
        if (transaction == null) {
            return null;
        }

        try {
            return new Serdes.ListSerde(ArrayList.class,new TransactionSerde()).serializer().serialize(outputTopic,transaction);
        } catch (final Exception e) {
            throw new SerializationException("Error serializing JSON message", e);
        }
    }


    @Override
    public void close() {
        Serializer.super.close();
    }

}
