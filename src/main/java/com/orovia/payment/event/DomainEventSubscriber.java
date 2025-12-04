package com.orovia.payment.event;

import java.util.function.Consumer;

/**
 * Subscribes to domain events emitted on a topic.
 */
public interface DomainEventSubscriber {

    /**
     * Subscribe to a topic with the provided consumer.
     *
     * @param topic topic name
     * @param consumer consumer that handles events published on the topic
     */
    void subscribe(String topic, Consumer<Object> consumer);
}

