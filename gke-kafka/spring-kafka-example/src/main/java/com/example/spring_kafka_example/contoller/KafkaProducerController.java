package com.example.spring_kafka_example.contoller;

import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.web.bind.annotation.*;

import com.example.spring_kafka_example.config.KafkaConfig;

@RestController
@RequestMapping("/api/kafka")
public class KafkaProducerController {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerController.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/publish")
    public String sendMessageToKafka(@RequestParam("message") String message) {
        ProducerFactory<String, String> producerFactory = kafkaTemplate.getProducerFactory(); 
        Map<String, Object> configProps = producerFactory.getConfigurationProperties(); 
        String bootstrapServers = (String) configProps.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG); 
        System.out.println("bootstrapServers"+bootstrapServers);
        logger.info("bootstrapServers"+bootstrapServers);
        kafkaTemplate.send("sumantopic", message);
        return "Message sent to Kafka: " + message+ "bootstrap server is "+bootstrapServers;
    }
}
