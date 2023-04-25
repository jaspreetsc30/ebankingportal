package com.example.ebankingportal.web.ebanking;

import com.example.ebankingportal.service.EBankingService;
import com.example.ebankingportal.service.JwtService;
import com.example.ebankingportal.web.ebanking.domain.CreditDebitRequest;
import com.example.ebankingportal.web.ebanking.domain.CreditDebitResponse;
import com.example.ebankingportal.web.ebanking.domain.MonthlyTransactionsResponse;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
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




    @Operation(summary = "Debits money into account")

    @PostMapping("/debit")
    private CreditDebitResponse debit(@RequestHeader HttpHeaders headers, @Valid @RequestBody CreditDebitRequest request){
        String iban = getIBAN(headers);
        return eBankingService.processDebit(request,iban);
    }

    @Operation(summary = "Credits money into account")
    @PostMapping("/credit")
    private CreditDebitResponse credit(@RequestHeader HttpHeaders headers,@Valid @RequestBody CreditDebitRequest request){
        String iban = getIBAN(headers);
        return eBankingService.processCredit(request,iban);
    }

    @Operation(summary = "Gets a list of paginated transactions in a particular month,year")
    @GetMapping("/transactions")

    private MonthlyTransactionsResponse inquire(@RequestHeader HttpHeaders headers

            ,@Parameter(description = "month" , example = "1") @Range(min = 1,max = 12) @RequestParam(required = true) int month
            ,@Parameter(description = "year" , example = "2023")@RequestParam(required = true) int year
            ,@Parameter(description = "page of the transaction list (contains values from (pagesize*page - page) to pagesize*page )" , example = "1") @RequestParam(required = false) Integer page
            ,@Parameter(description = "size of each page" , example = "5") @Range(min = 1,max = 100)@RequestParam(required = false) Integer pageSize
            ,@Parameter(description = "boolean Flag for getting exchange rates" , example = "False")@RequestParam(required = false) Boolean isRateRequired

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
