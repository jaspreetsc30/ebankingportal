echo "Waiting for Kafka to be up and running..."

cub kafka-ready -b kafka:9092 1 20

# create the users topic
kafka-topics \
  --bootstrap-server kafka:9092 \
  --topic bank-transactions \
  --replication-factor 1 \
  --create --if-not-exists

kafka-topics \
  --bootstrap-server kafka:9092 \
  --topic monthly-bank-transactions-aggregates \
  --replication-factor 1 \
  --create --if-not-exists

kafka-topics \
  --bootstrap-server kafka:9092 \
  --topic bank-balances \
  --replication-factor 1 \
  --create --if-not-exists