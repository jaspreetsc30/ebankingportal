package com.example.ebankingportal.web.ebanking;

import com.example.ebankingportal.service.EBankingService;
import com.example.ebankingportal.service.JwtService;
import com.example.ebankingportal.web.ebanking.domain.CreditDebitRequest;
import com.example.ebankingportal.web.ebanking.domain.MonthlyTransactionsResponse;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("api/v1/banking")
@RequiredArgsConstructor
public class EBankingController {
    @Autowired
    StreamsBuilderFactoryBean factoryBean;
    @Autowired
    JwtService jwtService;

    @Autowired
    private EBankingService eBankingService;



    private boolean isIBANValid(HttpHeaders headers,String iban){
        String jwtToken = headers.getFirst(HttpHeaders.AUTHORIZATION).substring(7);
        Claims claim = jwtService.extractAllClaims(jwtToken);
        return iban.equals(claim.get("IBAN"));
    }




    @PostMapping("/debit")
    private String debit(@RequestHeader HttpHeaders headers, @Valid @RequestBody CreditDebitRequest request){
        if (!isIBANValid(headers, request.getIban())) throw new SecurityException("Invalid IBAN");
        return eBankingService.processDebit(request);
    }

    @PostMapping("/credit")
    private String credit(@RequestHeader HttpHeaders headers,@Valid @RequestBody CreditDebitRequest request){
        if (!isIBANValid(headers, request.getIban())) throw new SecurityException("Invalid IBAN");
        return eBankingService.processCredit(request);
    }

    @GetMapping("/inquire/{iban}")
    private MonthlyTransactionsResponse inquire(@RequestHeader HttpHeaders headers
            ,@PathVariable String iban
            ,@Range(min = 1,max = 12) @RequestParam(required = true) int month
            ,@RequestParam(required = true) int year
            ,@Range(min = 1,max = 12) @RequestParam(required = false) Integer page
            ,@RequestParam(required = false) Integer pageSize
            ,@RequestParam(required = false) Boolean exchangeRateFlag

    ){

        if (!isIBANValid(headers,iban)) throw new SecurityException("Invalid IBAN");
        long currYear = Calendar.getInstance().get(Calendar.YEAR);
        if (year > currYear || year < currYear - 10) throw new RuntimeException("Invalid Year");
        String key = Integer.toString(month)+ Integer.toString(year)+ "_" + iban;
        if (page == null) page = 1;
        if (pageSize == null) pageSize = 10;
        if (exchangeRateFlag == null) exchangeRateFlag = false;
        MonthlyTransactionsResponse response = eBankingService.getMonthlyTransactions(key,page,pageSize,exchangeRateFlag);
        return response;
    }


}
