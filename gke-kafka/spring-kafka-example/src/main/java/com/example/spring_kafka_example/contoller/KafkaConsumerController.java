package com.example.spring_kafka_example.contoller;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kafka")
public class KafkaConsumerController {

    private String consumedMessage = "";

    @KafkaListener(topics = "sumantopic", groupId = "group_id")
    public void listen(String message) {
        this.consumedMessage = message;
        System.out.println("Consumed message: " + message);
    }

    @GetMapping("/consume")
    public String getConsumedMessage() {
        return "Consumed message: " + consumedMessage;
    }
}
