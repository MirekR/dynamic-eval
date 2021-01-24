package cz.mirek.dynamic.javascript.kafka;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Properties;
import java.util.Random;


public class KafkaProducerSample {
    public static void main(String[] args) throws InterruptedException, UnsupportedEncodingException {

        Properties properties = new Properties();
        properties.put("bootstrap.servers", "127.0.0.1:9092");
        properties.put("kafka.topic", "mirek-topic");
        properties.put("acks", "0");
        properties.put("retries", "1");
        properties.put("batch.size", "20971520");
        properties.put("linger.ms", "33");
        properties.put("max.request.size", "2097152");
        properties.put("compression.type", "gzip");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        runMainLoop(args, properties);
    }

    static void runMainLoop(String[] args, Properties properties) throws InterruptedException, UnsupportedEncodingException {

        // Create Kafka producer
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

        try {

            while (true) {

                Thread.sleep(1000);
                String id = "device-" + getRandomNumberInRange(1, 5);
                System.out.println("Sending event to topic " + properties.getProperty("kafka.topic"));
                producer.send(new ProducerRecord<String, String>(properties.getProperty("kafka.topic"), id, getMsg(id)));

            }

        } finally {

            producer.close();

        }

    }

    public static String getMsg(String id) throws UnsupportedEncodingException {

        Gson gson = new Gson();

        String timestamp = new Timestamp(System.currentTimeMillis()).toString();

        JsonObject obj = new JsonObject();
        obj.addProperty("id", id);
        obj.addProperty("data", Base64.getEncoder().encodeToString("this is my message data ...".getBytes("utf-8")));
        String json = gson.toJson(obj);

        return json;

    }

    private static int getRandomNumberInRange(int min, int max) {

        Random r = new Random();
        return r.ints(min, (max + 1)).findFirst().getAsInt();

    }

}