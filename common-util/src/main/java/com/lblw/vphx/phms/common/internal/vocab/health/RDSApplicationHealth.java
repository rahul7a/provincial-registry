package com.lblw.vphx.phms.common.internal.vocab.health;

import com.lblw.vphx.phms.common.exceptions.InternalAPIClientException;
import com.lblw.vphx.phms.common.internal.config.InternalApiConfig;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Actuator End Point controller for RDS Application Health
 *
 * <p>Accessible at GET: /actuator/health
 */
@Component("rds-application-health")
@AllArgsConstructor
@Slf4j
public class RDSApplicationHealth implements HealthIndicator {

  private InternalApiConfig internalApiConfig;

  @Override
  public Health health() {
    try {
      String actuatorURL =
          internalApiConfig.getRds().getBaseUrl().concat(internalApiConfig.getRds().getActuator());
      WebClient.builder()
          .baseUrl(actuatorURL)
          .build()
          .get()
          .retrieve()
          .onStatus(
              httpStatus -> !httpStatus.is2xxSuccessful(),
              clientResponse -> {
                throw new InternalAPIClientException(
                    String.format(
                        "Connection to rds service failed with status code: %s, for host: %s",
                        clientResponse.statusCode(), internalApiConfig.getRds().getBaseUrl()));
              })
          .toEntity(String.class)
          .block();

    } catch (Exception e) {
      log.warn("Failed to connect to: {}", internalApiConfig.getRds().getBaseUrl());
      return Health.down().withDetail("error", e.getMessage()).build();
    }
    return Health.up()
        .withDetails(Map.of("rds-app-url", internalApiConfig.getRds().getBaseUrl()))
        .build();
  }
}
