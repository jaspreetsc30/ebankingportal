package com.example.ebankingportal.web.ebanking;

import com.example.ebankingportal.service.EBankingService;
import com.example.ebankingportal.service.JwtService;
import com.example.ebankingportal.web.ebanking.domain.CreditDebitRequest;
import com.example.ebankingportal.web.ebanking.domain.CreditDebitResponse;
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



    private String getIBAN(HttpHeaders headers){
        String jwtToken = headers.getFirst(HttpHeaders.AUTHORIZATION).substring(7);
        Claims claim = jwtService.extractAllClaims(jwtToken);
        return  claim.get("IBAN").toString();
    }




    @PostMapping("/debit")
    private CreditDebitResponse debit(@RequestHeader HttpHeaders headers, @Valid @RequestBody CreditDebitRequest request){
        String iban = getIBAN(headers);
        return eBankingService.processDebit(request,iban);
    }

    @PostMapping("/credit")
    private CreditDebitResponse credit(@RequestHeader HttpHeaders headers,@Valid @RequestBody CreditDebitRequest request){
        String iban = getIBAN(headers);
        return eBankingService.processCredit(request,iban);
    }

    @GetMapping("/inquire")
    private MonthlyTransactionsResponse inquire(@RequestHeader HttpHeaders headers

            ,@Range(min = 1,max = 12) @RequestParam(required = true) int month
            ,@RequestParam(required = true) int year
            ,@Range(min = 1,max = 12) @RequestParam(required = false) Integer page
            ,@Range(min = 1,max = 100)@RequestParam(required = false) Integer pageSize
            ,@RequestParam(required = false) Boolean isRateRequired

    ){
        String iban = getIBAN(headers);
        long currYear = Calendar.getInstance().get(Calendar.YEAR);
        if (year > currYear || year < currYear - 10) throw new RuntimeException("Invalid Year");
        String key = Integer.toString(month)+ Integer.toString(year)+ "_" + iban;
        if (page == null) page = 1;
        if (pageSize == null) pageSize = 10;
        if (isRateRequired == null) isRateRequired = false;
        MonthlyTransactionsResponse response = eBankingService.getMonthlyTransactions(key,page,pageSize,isRateRequired);
        return response;
    }


}
