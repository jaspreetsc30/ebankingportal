package com.example.ebankingportal.service.exchangerateservice;

import com.example.ebankingportal.service.exchangerateservice.domain.ExchangeRateResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;

@Service
public class ExchangeRateService {
    private final RestTemplate restTemplate;

    @Value("${spring.exchangeservice.api-key}")
    private String apiKey;

    public ExchangeRateService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public HashMap<String,String> getExchangeRates(ArrayList<String> currencies) {
        String cur = String.join("%2C", currencies);
        String placeholder = "https://api.apilayer.com/exchangerates_data/latest?symbols=%s&base=USD";
        String url = String.format(placeholder,cur );

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey",apiKey);
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        ResponseEntity<ExchangeRateResponse> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, ExchangeRateResponse.class);
        return responseEntity.getStatusCode() == HttpStatus.OK?responseEntity.getBody().getRates(): new HashMap<>();
    }

}
