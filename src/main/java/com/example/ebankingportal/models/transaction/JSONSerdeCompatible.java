package com.example.ebankingportal.models.transaction;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = Transaction.class)
// TODO explore to make it generic
public interface JSONSerdeCompatible {}