package com.transitflow.payment.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Retry a failing record a bounded number of times with a fixed backoff, then
 * route it to {@code <topic>.DLT} instead of blocking the partition forever
 * (ADR-0005). A single {@code CommonErrorHandler} bean is picked up automatically
 * by Spring Boot's Kafka listener container factory.
 */
@Configuration
public class KafkaErrorHandlingConfig {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {
        var recoverer = new DeadLetterPublishingRecoverer(template,
                (record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition()));
        // 2 retries, 1s apart, before the record is dead-lettered.
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2L));
    }
}
