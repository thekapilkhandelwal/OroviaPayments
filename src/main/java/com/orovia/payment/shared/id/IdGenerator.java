package com.orovia.payment.shared.id;

/**
 * Contract for generating distributed unique identifiers that avoid database sequences.
 */
public interface IdGenerator {

    /**
     * Generate the next time-sortable unique identifier.
     *
     * @return unique id
     */
    long nextId();
}
