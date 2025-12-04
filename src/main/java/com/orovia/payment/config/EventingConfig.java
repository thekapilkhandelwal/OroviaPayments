package com.orovia.payment.config;

import com.orovia.payment.event.DomainEventPublisher;
import com.orovia.payment.event.DomainEventSubscriber;
import com.orovia.payment.event.InMemoryDomainEventBus;
import com.orovia.payment.event.KafkaDomainEventPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class EventingConfig {

    @Bean
    @ConditionalOnProperty(prefix = "eventing.kafka", name = "enabled", havingValue = "true")
    public DomainEventPublisher kafkaDomainEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        return new KafkaDomainEventPublisher(kafkaTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(DomainEventPublisher.class)
    public InMemoryDomainEventBus inMemoryDomainEventBus() {
        return new InMemoryDomainEventBus();
    }

    @Bean
    @ConditionalOnMissingBean(DomainEventSubscriber.class)
    public DomainEventSubscriber inMemoryDomainEventSubscriber(InMemoryDomainEventBus bus) {
        return bus;
    }
}

