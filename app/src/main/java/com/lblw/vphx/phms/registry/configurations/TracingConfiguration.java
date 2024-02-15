package com.lblw.vphx.phms.registry.configurations;

import brave.baggage.BaggageField;
import brave.baggage.BaggagePropagation;
import brave.baggage.BaggagePropagationConfig;
import brave.baggage.CorrelationScopeConfig;
import brave.context.slf4j.MDCScopeDecorator;
import brave.propagation.B3Propagation;
import brave.propagation.Propagation;
import brave.propagation.ThreadLocalCurrentTraceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class TracingConfiguration {

  public static final String X_REQUEST_ID = "x-request-id";

  @Bean
  BaggageField xRequestIdField() {
    return BaggageField.create(X_REQUEST_ID);
  }

  @Bean
  ThreadLocalCurrentTraceContext currentTraceContext() {
    return ThreadLocalCurrentTraceContext.newBuilder()
        .addScopeDecorator(
            MDCScopeDecorator.newBuilder()
                .add(CorrelationScopeConfig.SingleCorrelationField.create(xRequestIdField()))
                .build())
        .build();
  }

  @Bean
  Propagation.Factory baggagePropagation() {
    return BaggagePropagation.newFactoryBuilder(B3Propagation.FACTORY)
        .add(BaggagePropagationConfig.SingleBaggageField.remote(xRequestIdField()))
        .build();
  }
}
