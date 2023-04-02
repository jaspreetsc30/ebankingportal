package com.example.ebankingportal.service.exchangerateservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;

@Data
@AllArgsConstructor
public class ExchangeRateResponse {
    String base;
    String date;
    String timestamp;
    HashMap<String,String>  rates;


}
