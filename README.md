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


## Technologies
* Spring Boot 3.0
* Spring Security
* JSON Web Tokens (JWT)
* Kafka Streams
* Docker
* Circle CI