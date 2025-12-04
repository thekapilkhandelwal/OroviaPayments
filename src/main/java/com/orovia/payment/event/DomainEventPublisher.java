package com.orovia.payment.event;

/**
 * Publishes domain events to downstream subscribers.
 */
public interface DomainEventPublisher {

    /**
     * Publish an event to a topic.
     *
     * @param topic topic or channel name
     * @param event event payload
     */
    void publish(String topic, Object event);
}

