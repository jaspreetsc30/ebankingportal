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
## API Guide
Information on all restful API operations in this project can be accessed via http://localhost:8080/swagger-ui/index.html
![Swagger-UI](/diagram/Swagger-UI.PNG)

## /api/v1/auth
This endpoint does not require authentication as it is for authentication and registration

**GET** **/api/v1/auth/register**

**Description**: An endpoint to register users and provide a JWT token upon successful registration

**Request Body**

| Field    | Data Type | Description                                         |Required|
|----------|-----------|-----------------------------------------------------|---|
| iban     | String    | unique IBAN of every user                           |Y|
| userName | String    | username to register with                           |Y|
| password | String    | password to register with  |Y|

**Response**

| Field    | Data Type | Description                                         |
|----------|-----------|-----------------------------------------------------|
| token     | String    | jwt Token                           |


**GET** **/api/v1/auth/authentication**

Description: An endpoint to authenticate users and provide a JWT token upon successful authentication

**Request Body**

| Field    | Data Type | Description                                         | Required|
|----------|-----------|-----------------------------------------------------|---------|
| iban     | String    | unique IBAN of every user                           | Y|
| userName | String    | username to register with                           |Y|
| password | String    | password to register with  |Y|

**Response**

| Field    | Data Type | Description                                         |
|----------|-----------|-----------------------------------------------------|
| token     | String    | jwt Token                           |


## /api/v1/banking

**POST** **/api/v1/banking/debit**

**POST** **/api/v1/banking/credit**


Description: An endpoint to post debit/credit transactions.The two endpoints have the same request and response body.

**Request Body**

| Field    | Data Type | Description                                                                    | Required |
|----------|-----------|--------------------------------------------------------------------------------|----------|
| currency | String    | currency                                                                       |Y |
| amount   | Double    | amount to register </br> The amount cannot exceed 100,000 and 2 decimal places |Y|
| message  | String    | Transaction Message       (Optional)                                           |N(Nullable)|

**Response**

| Field             | Data Type | Description                                           |
|-------------------|-----------|-------------------------------------------------------|
| iban              | String    | Unique IBAN of user                                   |
| transactionId     | String    | ID of the transaction                                 |
| currency          | String    | Currency the transaction was processed in             |
| amount            | Double    | Amount                                                |
| time              | String    | Time of the transaction in DD:MM:YY HH:MM:SS format   |
| transactionId     | String    | ID of the transaction                                 |
| message           | String    | Message of the transaction                            |
| transactionType   | String    | ENUM of either credit or debit but returned as String |



**GET** **/api/v1/auth/inquire**

**Query Params**

| Query Param     | Data Type | Description                                                                   |Required|
|-----------------|-----------|-------------------------------------------------------------------------------|-----|
| month           | Integer   | Month of transaction to inquire                                               |Y|
| year            | Integer   | Year of transaction to inquire                                                |Y|
| page            | Integer   | which page to inquire  , range from 1-100, default 1                          |N
| pageSize        | Integer   | size of page  range from 1-100, default 5                                     |N
| isRateRequired  | Boolean   | flag for calling exchange rate service</br> (to limit API calls due to limits) |N|

**Sample Response**

The Response is nested as is better illustrated via a sample response.

```agsl
{
    "balances": {
        "HKD": 620.0,
        "HKDdebit": 620.0
    },
    "exchangeRates": null,
    "transactions": [
        {
            "transactionId": "8eeee9fa-bb6d-4ae1-b6b1-fe0fed6c52db",
            "amount": 124.0,
            "currency": "HKD",
            "timestamp": 1680278823100,
            "message": null,
            "iban": "checkingonthis"
        },
        {
            "transactionId": "2d58be07-eebe-4683-ab69-083cb587dcc3",
            "amount": 124.0,
            "currency": "HKD",
            "timestamp": 1680278956323,
            "message": null,
            "iban": "checkingonthis"
        },
        {
            "transactionId": "5e8cefb5-df1e-4145-8c08-9d2c7f329ae0",
            "amount": 124.0,
            "currency": "HKD",
            "timestamp": 1680279017197,
            "message": null,
            "iban": "checkingonthis"
        },
        {
            "transactionId": "6fb0430a-e378-4a67-949e-a50b7879dad6",
            "amount": 124.0,
            "currency": "HKD",
            "timestamp": 1680284814625,
            "message": null,
            "iban": "checkingonthis"
        },
        {
            "transactionId": "a775aacd-b2db-4935-ab68-3a89341ea803",
            "amount": 124.0,
            "currency": "HKD",
            "timestamp": 1680340905562,
            "message": null,
            "iban": "checkingonthis"
        }
    ],
    "message": null
}
```

Description: An endpoint to post debit/credit transactions.The two endpoints have the same request and response body.

**Query Params**

| Field    | Data Type | Description                                                                    | Required |
|----------|-----------|--------------------------------------------------------------------------------|----------|
| currency | String    | currency                                                                       |Y |
| amount   | Double    | amount to register </br> The amount cannot exceed 100,000 and 2 decimal places |Y|
| message  | String    | Transaction Message       (Optional)                                           |N(Nullable)|

**Response**


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

Package  | Description|
------------- | -------------|
config | Contains spring boot class annotated with @configuration|
model  | Contains DTO classes|
repository  | Repository for CRUD operations to DB|
service | Contains core business and processing logic|
web | Contains web controllers|
util | Contains basic utility functions (eg CalculatorUtil)|

The top level structure further contains sub-packages that are mainly grouped into three modules ```(ebanking,authentication,kafka)```


Package  | Description|
------------- | -------------|
ebanking | Handles ebanking functionality (credit,debit,getbalances)|
authentication  | Handles Authentication functionality|
kafka  | Handles Kafka Infrastructure|

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