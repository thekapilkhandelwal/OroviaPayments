package com.orovia.payment.config;

import io.opentelemetry.api.OpenTelemetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Minimal telemetry wiring; can be swapped for real OTLP exporters in production.
 */
@Configuration
public class TelemetryConfig {

    @Bean
    public OpenTelemetry openTelemetry() {
        return OpenTelemetry.noop();
    }
}
