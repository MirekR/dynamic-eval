How to start kafka with Docker:
Follow this article: https://itnext.io/how-to-install-kafka-using-docker-a2b7c746cbdc


Commands shotscuts from the article:

```
docker network create kafka-net --driver bridge
```

```
docker run -d --name zookeeper-server -p 2181:2181 --network kafka-net -e ALLOW_ANONYMOUS_LOGIN=yes bitnami/zookeeper:latest

```

```
docker run -d --name kafka-server1 --network kafka-net -e ALLOW_PLAINTEXT_LISTENER=yes -e KAFKA_CFG_ZOOKEEPER_CONNECT=zookeeper-server:2181 -e KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 -p 9092:9092 bitnami/kafka:latest
```

# Produce new test messages
Execute KafkaProducerSample.java