# Ebanking Portal

## Description
This project implements a secure e-banking portal that supports simple multi-currency debit and credit transactions.

## Getting Started
To run this project locally , you will need to have the following installed on your local machine:
* JDK 9+
* Maven 3+
* Docker Desktop
* IntelliJ (Optional)

[IntelliJ IDEA Community Edition](https://www.jetbrains.com/idea/download/#section=windows) is optional to install but highly recommended due to its extension of JDK,Maven bundle plug ins

### 1. Spinning up Docker container
Kafka is required to run this project, therefore official docker images of kafka zookeeper and topics are specified in the [docker-compose.yaml](docker-compose.yaml) file of the repository.

After you have installed docker desktop, go to the root directory of this project and type ```docker compose up```
```
PATH_TO_PROJECT\ebankingportal> docker compose up
```

After the docker container is up and running, verify if 3 topics have been created by the following command.
 ```
 docker-compose exec kafka bash -c "kafka-topics --list --bootstrap-server kafka:9092"                 
 ```


The output should list out ```bank-balances``` , ```bank-transactions``` and ```monthly-bank-transaction-aggregates```
```
PATH_TO_PROJECT\ebankingportal> docker-compose exec kafka bash -c "kafka-topics --list --bootstrap-server kafka:9092"
bank-balances
bank-transactions
monthly-bank-transactions-aggregates
```

### 2. Running the project

Now that the Kafka Infrastructure has been set we can build run the Spring project

* Build the project: ```mvn clean install```
* Run the project: ```mvn spring-boot:run```

-> The application will be available at http://localhost:8080.

## Project Structure
The source code follows the following structure 
### Top Level Structure
```agsl
├───java                         
│   └───com
│       └───example
│           └───ebankingportal
│               ├───configurations
│               │   ├───authentication
│               │   └───kafka
│               ├───models
│               ├───repositories
│               ├───services
│               ├───util
│               └───web
│                   ├───authentication
│                   │   └───domain
│                   └───ebanking
│                       └───domain
└───resources

```
The top level structure of the source code is organized into 6 packages ```(configurations,models,repositories,services,web,util)```

Package  | Description
------------- | -------------
config | Contains spring boot class annotated with @configuration
model  | Contains DTO classes
repository  | Repository for CRUD operations to DB
service | Contains core business and processing logic
web | Contains web controllers
util | Contains basic utility functions (eg CalculatorUtil)

The top level structure further contains sub-packages that are mainly grouped into three modules ```(ebanking,authentication,kafka)```


Package  | Description
------------- | -------------
ebanking | Handles ebanking functionality (credit,debit,getbalances)
authentication  | Handles Authentication functionality
kafka  | Handles Kafka Infrastructure

## Implementation
## Authentication
In order for authentication to work and data to be persistent across builds and runs, a database is needed.For simplicity purposes, H2 database is used with the persistent data store in [data](/data)

### Spring Security
Spring security is used for Jwt based Authentication in this application. The following diagram provides an overview of Authentication workflow  
![SpringSecurityDiagram](/diagram/SpringSecurityDiagram.jpg)

In order to implement JWT authentication, the following is required
1. JWTAuthenticationFilter that extends OncePerRequestFilter that updates security context
2. Security Configuration that provides configures our security
3. Implementations of AuthenticationManager, AuthenticationProvider methods, PasswordEncoder
4. Implementations of  UserDetailsService
5. A data model that extends UserDetailsService
6. A jwtService for validating,creating,extracting claims
7. An endpoint for registering and authenticating

All these configurations and implementations for requirements 1-4 can be found under the config/authentication package  

The User class satisfies requirement 5 by extending UserDetailsService

Requirement 6 is satisfied via jwtService under the services package

Requirement 7 is satisfied by implementing a rest controller under web/authentication package.

### Details
1. Endpoints with a prefix of ```/api/v1/auth``` are whitelisted for registering and authenticating to get a JWT Token.
2. During token generation, a custom Claim called ```IBAN``` is set.The value of ```IBAN``` is the user's IBAN.This claim is embedded in the jwtT token,this is to ensure that a user can only access his own ebanking service.
## EbankingServices and Kafka Streams Processing
After authentication, the whole architecture of the ebanking portal is as follows
![Architecture](/diagram/architecture.drawio.png)

### Credit/Debit Transactions
Credit/Debit transaction requests are send to the kafka topic ```bank-transactions```
via a Producer configured in Spring Boot. The producer sends key,value pairs by serializing IBAN and Transaction into bytes.
In order to implement this, the following is required
1. Kafka Producer with factory method to create KafkaProducer during runtime
2. Binding KafkaTemplate to Kafka Producer
3. Controller for debit/credit transactions

All these configurations and implementations for requirements 1-2 can be found under the ```config/kafka``` package in the kafkaConfig.java file

A restcontroller ```(EBankingController.java)``` class is implemented under ```web/ebanking``` package

A key,value pair is produced in EBankingService.java which makes use of ```KafkaTemplate.send()``` API to send message. 
#### Details
1. Before crediting a transaction, a check is done to see if the user has enough balance by querying the ```balancesbyuser``` state store. More on states store will be explained in get section
### Kafka Stream Processing
A kafka stream is connected to the ```banking-transactions``` topic.Bytes are consumed from banking-transactions topic and deserialized to a key,value of string and Transactions type respectively.
The key is iban unique to each user. After deserialization, the stream undergoes two types of processing.The advantage of state store is that processing is already done on data unlike relational databases where
queries are performed to get an aggregate which puts a lot of load and unnecessary overhead.

#### Bank Balance Processing
1. The KStream does a groupBy operation which groups keys with the same IBAN into a KGroupedStream.
2. The KGroupedStream aggregates by having a hashmap as an initializer, the hashmap stores balances of each user and is updated every transaction.
3. The aggregation produces a Ktable and the result is stored in a persistent state store called ```balancesbyuser```
4. The KTable is then converted back to a KStream to send data to ```bank-balances``` topic (just for testing purposes)


##### Transactions Processing
1. The KStream does a groupBy operation. The groupBy logic is as follows:

   1. The IBAN is extracted from key while a timestamp is extracted from transactions.
   2. The month and year is then extracted from the timestamp.
   3. After that a key is formed by concatenating month,year,"_" and IBAN. For example an IBAN of value test with transaction  in mar,2023 would be
     would be "test_32023"
2. The KGroupedStream aggregates by having an arraylist as an initializer, a transaction is added to the arraylist everytime
5. The aggregation produces a Ktable and the result is stored in a persistent state store called ```transactionsbyuser```
4. The KTable is then converted back to a KStream to send data to ```monthly-bank-transaction-aggregates``` topic (just for testing purposes)

The implementation of Kafka kstream processor is present in ```StreamProcessor.java``` file  under ```config/kafka``` package.



### Get monthly transactions
Getting monthly transactions is fairly easy by accessing the ```transactionsbyuser``` state store and querying by key.
A restcontroller ```(EBankingController.java)``` class is implemented under ```web/ebanking``` package for this API while
```EBankingService``` layers contains the logic of creating a paginated API. There is an ```ExchangeRateService.java``` file under ```service``` package that contains a RestTemplate to retrieve exchange rates.
(Page,Size) pagination is used, the logic for pagination is is ```EBankingService.java```
#### Key Details
1. There is a limit on number of calls per month for external API.Therefore,there is an additional
```isRateRequired``` flag to turn off calls to external API to get rates.
2. All rates are referenced to USD.


## Technologies
* Spring Boot 3.0
* Spring Security
* JSON Web Tokens (JWT)
* H2 Database
* Kafka Streams
* Docker
* Circle CI