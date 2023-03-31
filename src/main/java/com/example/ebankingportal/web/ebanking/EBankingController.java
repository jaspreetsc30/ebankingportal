package com.example.ebankingportal.web.ebanking;

import com.example.ebankingportal.models.transaction.Transaction;
import com.example.ebankingportal.services.EBankingService;
import com.example.ebankingportal.services.JwtService;
import com.example.ebankingportal.web.ebanking.domain.CreditDebitRequest;
import com.example.ebankingportal.web.ebanking.domain.MonthlyTransactionsResponse;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
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
    private KafkaTemplate<String, List<Transaction>> kafkaTemplate2;
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
            ,@RequestParam(required = false) Integer pageSize){

        if (!isIBANValid(headers,iban)) throw new SecurityException("Invalid IBAN");
        long currYear = Calendar.getInstance().get(Calendar.YEAR);
        if (year > currYear || year < currYear - 10) throw new RuntimeException("Invalid Year");
        String key = Integer.toString(month)+ Integer.toString(year)+ "_" + iban;
        if (page == null) page = 1;
        if (pageSize == null) pageSize = 10;
        MonthlyTransactionsResponse response = eBankingService.getMonthlyTransactions(key,page,pageSize);
        return response;
    }

    @GetMapping("/test")
    private void check(@Valid @RequestBody CreditDebitRequest request){
        String transactionId = UUID.randomUUID().toString();
        String IBAN = request.getIban();
        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .IBAN(IBAN)
                .currency(String.valueOf(request.getCurrency()))
                .amount(request.getAmount())
                .timestamp(System.currentTimeMillis() )
                .message(request.getMessage())
                .build();
        List<Transaction> list = new ArrayList<>();
        list.add(transaction);
        kafkaTemplate2.send("test", list);
    }

    @KafkaListener(topics ="test" , groupId = "1")
    public void process(ArrayList<Transaction> transactions){

        System.out.println(transactions);
        System.out.println(transactions.get(0));
    }


}
