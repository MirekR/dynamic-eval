package cz.mirek.dynamic.javascript.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.mirek.dynamic.javascript.dto.CustomFunction;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class KafkaConsumers {
    private Logger LOGGER = Logger.getLogger(KafkaListener.class.getName());
    private ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

    private Map<String, KafkaMessageListenerContainer> listeners = new HashMap<>();

    @Scheduled(fixedDelay=5000)
    public void loadListeners() {
        LOGGER.info("Loading new listeners");
        loadCustomFunctions()
                .stream()
                .filter(customFunction -> !listeners.containsKey(customFunction.getTopic()))
                .forEach(listener -> {
                    LOGGER.info("Creating listener for " + listener.getTopic());
                    listeners.put(listener.getTopic(), getMessageListenerForTopic(listener));
                });
    }

    // RestCall to the Code Service
    private List<CustomFunction> loadCustomFunctions() {
        return List.of(
                new CustomFunction().setJavascript("print('Inside JS function id is' + data.id + ' and value is ' + data.data);").setTopic("mirek-topic")
        );
    }

    public KafkaMessageListenerContainer<String, String> getMessageListenerForTopic(CustomFunction customFunction) {

        ContainerProperties containerProperties = new ContainerProperties(customFunction.getTopic());
        containerProperties.setMessageListener((MessageListener<String, String>) record -> {
            try {
                Bindings bindings = engine.createBindings();

                bindings.put("data", OBJECT_MAPPER.readValue(record.value(), Map.class));

                engine.eval(customFunction.getJavascript(), bindings);
            } catch (ScriptException | JsonProcessingException e) {
                e.printStackTrace();
            }
        });

        ConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProperties());
        KafkaMessageListenerContainer<String, String> listenerContainer = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        listenerContainer.setAutoStartup(true);
        // bean name is the prefix of kafka consumer thread name
        listenerContainer.setBeanName("kafka-message-listener-" + customFunction.getTopic());
        listenerContainer.start();
        return listenerContainer;
    }

    private Map<String, Object> consumerProperties(){
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test");
        return props;
    }
}
