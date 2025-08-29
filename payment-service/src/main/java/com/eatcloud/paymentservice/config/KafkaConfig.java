package com.eatcloud.paymentservice.config;

import com.eatcloud.paymentservice.event.OrderCreatedEvent;
import com.eatcloud.paymentservice.event.PaymentCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class KafkaConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    // Producer 설정
    @Bean
    public ProducerFactory<String, PaymentCreatedEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    @Bean
    public KafkaTemplate<String, PaymentCreatedEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
    
    // Consumer 설정
    @Bean
    public ConsumerFactory<String, OrderCreatedEvent> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "payment-service");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<OrderCreatedEvent> valueDeserializer = new JsonDeserializer<>(OrderCreatedEvent.class);
        valueDeserializer.addTrustedPackages("*");
        valueDeserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(), valueDeserializer);
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        
        // 에러 핸들러 설정
        factory.setCommonErrorHandler(new DefaultErrorHandler(
            (consumerRecord, exception) -> {
                log.error("메시지 처리 실패 - 토픽: {}, 파티션: {}, 오프셋: {}, 에러: {}", 
                    consumerRecord.topic(), consumerRecord.partition(), 
                    consumerRecord.offset(), exception.getMessage());
                
                // Dead Letter Queue로 전송
                sendToDeadLetterQueue(consumerRecord, exception);
            },
            new ExponentialBackOff(1000L, 2.0)
        ));
        
        return factory;
    }
    
    /**
     * Dead Letter Queue로 실패한 메시지 전송
     */
    private void sendToDeadLetterQueue(org.apache.kafka.clients.consumer.ConsumerRecord<?, ?> record, Exception exception) {
        try {
            String dlqTopic = record.topic() + ".DLQ";
            String errorMessage = String.format(
                "처리 실패 - 원본 토픽: %s, 파티션: %d, 오프셋: %d, 키: %s, 값: %s, 에러: %s",
                record.topic(), record.partition(), record.offset(),
                record.key(), record.value(), exception.getMessage()
            );
            
            // DLQ용 KafkaTemplate 생성 (String 타입)
            KafkaTemplate<String, String> dlqTemplate = new KafkaTemplate<>(
                new DefaultKafkaProducerFactory<>(getDlqProducerConfig())
            );
            
            // DLQ로 전송
            dlqTemplate.send(dlqTopic, record.key().toString(), errorMessage);
            log.info("Dead Letter Queue로 전송 완료: {}", dlqTopic);
            
        } catch (Exception dlqException) {
            log.error("Dead Letter Queue 전송 실패", dlqException);
        }
    }
    
    /**
     * DLQ 전송용 Producer 설정
     */
    private Map<String, Object> getDlqProducerConfig() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        return configProps;
    }
} 