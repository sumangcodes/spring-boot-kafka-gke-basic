package com.example.spring_kafka_example.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@EnableKafka
public class KafkaConfig {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConfig.class);

    private final String bootstrapServers;
    private final String username;
    private final String password;
    private final String topicName;
    private final int partitions;
    private final short replicationFactor;

    public KafkaConfig(@Value("${kafka.bootstrap-servers}") String bootstrapServers,
                       @Value("${kafka.username}") String username,
                       @Value("${kafka.password}") String password,
                       @Value("${kafka.topic.name:sumantopic}") String topicName,
                       @Value("${kafka.topic.partitions:1}") int partitions,
                       @Value("${kafka.topic.replication-factor:1}") short replicationFactor) {
        this.bootstrapServers = bootstrapServers;
        this.username = username;
        this.password = password;
        this.topicName = topicName;
        this.partitions = partitions;
        this.replicationFactor = replicationFactor;

        logger.debug("KafkaConfig initialized with bootstrapServers: {}, topicName: {}, partitions: {}, replicationFactor: {}",
                bootstrapServers, topicName, partitions, replicationFactor);
    }

    private Map<String, Object> getCommonKafkaConfig() {
        logger.info("Initializing Kafka configuration with bootstrap servers: {}", bootstrapServers);

        Map<String, Object> configProps = new HashMap<>();
        configProps.put("bootstrap.servers", bootstrapServers);
        configProps.put("security.protocol", "SASL_PLAINTEXT");
        configProps.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        configProps.put(SaslConfigs.SASL_JAAS_CONFIG,
                "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                        "username=\"" + username + "\" " +
                        "password=\"" + password + "\";");
        
        logger.debug("Kafka common configuration properties set: {}", configProps);
        return configProps;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        logger.info("Initializing Kafka ProducerFactory with bootstrap servers: {}", bootstrapServers);

        Map<String, Object> configProps = getCommonKafkaConfig();
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, ErrorHandlingDeserializer.class.getName());

        logger.info("Kafka ProducerFactory configured with properties: {}", configProps);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        logger.debug("Creating KafkaTemplate for message sending.");
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public NewTopic topic() {
        logger.info("Creating Kafka topic '{}'", topicName);
        return new NewTopic(topicName, partitions, replicationFactor);
    }

    @Bean
    public AdminClient adminClient() {
        logger.info("Initializing Kafka AdminClient with bootstrap servers: {}", bootstrapServers);

        Properties config = new Properties();
        config.putAll(getCommonKafkaConfig());

        logger.debug("Kafka AdminClient properties set: {}", config);
        return AdminClient.create(config);
    }


    @Bean
public ConsumerFactory<String, String> consumerFactory() {
    logger.info("Initializing Kafka ConsumerFactory with bootstrap servers: {}", bootstrapServers);

    Map<String, Object> configProps = getCommonKafkaConfig();
    configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "group_id");  // Use your group ID here
    configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    configProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, StringDeserializer.class.getName());
    
    // Optional: Adjust consumer behavior
    configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);  // Default is true
    configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");  // Start from earliest if no offset is present

    logger.info("Kafka ConsumerFactory configured with properties: {}", configProps);
    return new DefaultKafkaConsumerFactory<>(configProps);
}

@Bean
public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
    logger.info("Creating KafkaListenerContainerFactory for handling messages.");

    ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    
    // Optional: Concurrency for multithreading
    factory.setConcurrency(3);
    
    // Optional: Set ack mode for manual commits if required
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);

    return factory;
}

}