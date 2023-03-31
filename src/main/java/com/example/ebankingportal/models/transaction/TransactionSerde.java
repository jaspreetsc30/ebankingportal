package com.example.ebankingportal.models.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;
import java.util.Map;

public class TransactionSerde implements Serializer<Transaction> , Deserializer<Transaction> , Serde<Transaction> {
    ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public Transaction deserialize(String s, byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try {
            return objectMapper.readValue(bytes, Transaction.class);
        } catch (final IOException e) {
            throw new SerializationException(e);
        }
    }

    @Override
    public Serializer<Transaction> serializer() {
        return this;
    }

    @Override
    public Deserializer<Transaction> deserializer() {
        return this;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String s, Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsBytes(transaction);
        } catch (final Exception e) {
            throw new SerializationException("Error serializing JSON message", e);
        }
    }


    @Override
    public void close() {
        Serializer.super.close();
    }


}
