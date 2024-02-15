package com.lblw.vphx.phms.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/** Sets up web client configurations. */
@Configuration
public class RestConfiguration {

  /** Bean instantiates RestTemplate for web client configuration */
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
