package com.orovia.payment.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight in-memory event bus used as a fallback when Kafka is unavailable.
 */
public class InMemoryDomainEventBus implements DomainEventPublisher, DomainEventSubscriber {

    private static final Logger log = LoggerFactory.getLogger(InMemoryDomainEventBus.class);

    private final Map<String, List<Consumer<Object>>> subscribers = new ConcurrentHashMap<>();

    @Override
    public void publish(String topic, Object event) {
        subscribers.getOrDefault(topic, List.of()).forEach(consumer -> {
            try {
                consumer.accept(event);
            } catch (Exception e) {
                log.warn("In-memory event consumer threw", e);
            }
        });
    }

    @Override
    public void subscribe(String topic, Consumer<Object> consumer) {
        subscribers.computeIfAbsent(topic, t -> new ArrayList<>()).add(consumer);
    }
}

